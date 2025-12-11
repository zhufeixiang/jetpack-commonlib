# MVVM 到 MVI 迁移指南

## 架构对比

### MVVM 架构
- **特点**：双向数据绑定，多个 LiveData/StateFlow
- **数据流**：View ↔ ViewModel ↔ Model
- **状态管理**：分散在多个 LiveData 中

### MVI 架构
- **特点**：单向数据流，单一状态源
- **数据流**：View → Intent → ViewModel → State → View
- **状态管理**：所有状态集中在一个 State 对象中

## 核心概念

### 1. ViewState（视图状态）
- 所有 UI 相关的状态都集中在一个不可变的状态对象中
- 状态更新通过创建新对象而不是修改现有对象

### 2. ViewIntent（用户意图）
- 所有用户操作都封装为 Intent
- Intent 是不可变的数据类

### 3. Reducer（状态处理）
- 根据当前状态和 Intent 生成新状态
- 纯函数，无副作用

## 迁移步骤

### 步骤 1：定义 State

**MVVM 方式：**
```kotlin
class UserViewModel : BaseViewModel() {
    val userInfo = MutableLiveData<UserInfo?>()
    val isLoading = MutableLiveData<Boolean>()
    val error = MutableLiveData<com.zfx.commonlib.network.error.AppException?>()
}
```

**MVI 方式：**
```kotlin
data class UserViewState(
    val userInfo: UserInfo? = null,
    val isLoading: Boolean = false,
    val error: com.zfx.commonlib.network.error.AppException? = null
) : ViewState
```

### 步骤 2：定义 Intent

**MVVM 方式：**
```kotlin
class UserViewModel : BaseViewModel() {
    fun loadUserInfo() { ... }
    fun refresh() { ... }
    fun updateName(name: String) { ... }
}
```

**MVI 方式：**
```kotlin
sealed class UserIntent : ViewIntent {
    object LoadUserInfo : UserIntent()
    object Refresh : UserIntent()
    data class UpdateName(val name: String) : UserIntent()
}
```

### 步骤 3：创建 ViewModel

**MVVM 方式：**
```kotlin
class UserViewModel : BaseViewModel() {
    fun loadUserInfo() {
        repository.getUserInfo()
            .onEach { result ->
                when (result) {
                    is NetworkResult.Loading -> isLoading.value = true
                    is NetworkResult.Success -> {
                        isLoading.value = false
                        userInfo.value = result.data
                    }
                    is NetworkResult.Error -> {
                        isLoading.value = false
                        error.value = result.error
                    }
                }
            }
            .launchIn(viewModelScope)
    }
}
```

**MVI 方式：**
```kotlin
class UserMviViewModel : MviViewModel<UserIntent, UserViewState>() {
    
    override fun initialState(): UserViewState {
        return UserViewState()
    }
    
    override fun reduce(currentState: UserViewState, intent: UserIntent): UserViewState {
        return when (intent) {
            is UserIntent.LoadUserInfo -> {
                loadUserInfo()
                currentState.copy(isLoading = true)
            }
            is UserIntent.Refresh -> {
                loadUserInfo()
                currentState.copy(isLoading = true)
            }
            is UserIntent.UpdateName -> {
                currentState.copy(
                    userInfo = currentState.userInfo?.copy(name = intent.name)
                )
            }
        }
    }
    
    private fun loadUserInfo() {
        handleNetworkResult(
            flow = repository.getUserInfo(),
            onLoading = { state, message -> state.copy(isLoading = true) },
            onSuccess = { state, data -> 
                state.copy(isLoading = false, userInfo = data) 
            },
            onError = { state, error -> 
                state.copy(isLoading = false, error = error) 
            }
        )
    }
}
```

### 步骤 4：更新 Activity/Fragment

**MVVM 方式：**
```kotlin
class UserActivity : BaseVmActivity<UserViewModel>() {
    
    override fun createObserver() {
        viewModel.userInfo.observe(this) { userInfo ->
            // 更新 UI
        }
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) showLoading() else dismissLoading()
        }
        viewModel.error.observe(this) { error ->
            // 显示错误
        }
    }
    
    override fun initView(savedInstanceState: Bundle?) {
        refreshButton.setOnClickListener {
            viewModel.refresh()
        }
    }
}
```

**MVI 方式：**
```kotlin
class UserMviActivity : MviActivity<UserMviViewModel, UserIntent, UserViewState>() {
    
    override fun renderState(state: UserViewState) {
        // 根据状态更新 UI
        if (state.isLoading) {
            showLoading()
        } else {
            dismissLoading()
        }
        
        state.userInfo?.let { userInfo ->
            // 更新用户信息显示
        }
        
        state.error?.let { error ->
            // 显示错误
        }
    }
    
    override fun initView(savedInstanceState: Bundle?) {
        refreshButton.setOnClickListener {
            dispatchIntent(UserIntent.Refresh)
        }
    }
}
```

## 迁移优势

### 1. 状态管理更清晰
- **MVVM**：状态分散在多个 LiveData 中，难以追踪状态变化
- **MVI**：所有状态集中在一个 State 对象中，状态变化一目了然

### 2. 可预测性更强
- **MVVM**：多个方法可能同时修改状态，难以预测最终状态
- **MVI**：单向数据流，状态变化可预测

### 3. 易于测试
- **MVVM**：需要 Mock 多个 LiveData
- **MVI**：只需测试 Reducer 函数，输入输出明确

### 4. 易于调试
- **MVVM**：难以追踪状态变化来源
- **MVI**：所有状态变化都通过 Intent，易于追踪

## 注意事项

1. **状态不可变**：State 应该是不可变的，每次更新都创建新对象
2. **单一状态源**：所有 UI 状态都应该在 State 中
3. **纯函数 Reducer**：Reducer 函数应该是纯函数，无副作用
4. **异步操作**：异步操作应该在 ViewModel 中处理，通过 `updateState` 更新状态
5. **单次事件**：Toast、导航等一次性事件使用 `SingleEvent`

## 最佳实践

1. **State 设计**：State 应该包含所有 UI 相关的状态
2. **Intent 设计**：每个用户操作对应一个 Intent
3. **Reducer 设计**：Reducer 应该只处理状态转换，不处理业务逻辑
4. **异步处理**：异步操作在 ViewModel 中处理，通过扩展函数更新状态
5. **测试**：重点测试 Reducer 函数，确保状态转换正确

## 兼容性

- MVI 架构与 MVVM 架构可以共存
- 可以逐步迁移，不需要一次性全部迁移
- 新功能建议使用 MVI，旧功能可以保持 MVVM

