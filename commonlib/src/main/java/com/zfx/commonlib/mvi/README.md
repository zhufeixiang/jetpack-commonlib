# MVI 架构使用指南

## 概述

MVI（Model-View-Intent）是一种单向数据流架构模式，所有状态集中在一个 State 对象中，通过 Intent 驱动状态变化。

## 核心概念

### 1. ViewState（视图状态）
- **单一状态源**：所有 UI 相关的状态都集中在一个不可变的状态对象中
- **不可变性**：状态更新通过创建新对象而不是修改现有对象
- **可预测性**：状态变化可追踪、可调试

### 2. ViewIntent（用户意图）
- **用户操作封装**：所有用户操作都封装为 Intent
- **不可变数据**：Intent 是不可变的数据类（sealed class）
- **类型安全**：通过 sealed class 确保类型安全

### 3. MviViewModel（状态管理）
- **Reducer 模式**：根据当前状态和 Intent 生成新状态
- **单向数据流**：View → Intent → ViewModel → State → View
- **状态流**：使用 StateFlow 管理状态，自动通知 UI 更新

### 4. SingleEvent（单次事件）
- **一次性事件**：用于 Toast、导航等不需要保存状态的事件
- **避免重复触发**：不会因为状态保留而重复触发

## 数据流

```
用户操作 → dispatchIntent(Intent) 
    ↓
ViewModel.reduce(currentState, intent) 
    ↓
生成新 State → updateState(newState) 
    ↓
StateFlow 通知 → renderState(state) 
    ↓
更新 UI
```

## 快速开始

### 步骤 1：定义 ViewState

```kotlin
data class UserViewState(
    val userInfo: UserInfo? = null,
    val isLoading: Boolean = false,
    val loadingMessage: String = "加载中...",
    val error: AppException? = null,
    val showError: Boolean = false
) : ViewState {
    companion object {
        fun initial() = UserViewState()
    }
}
```

**最佳实践：**
- 使用 `data class` 确保不可变性
- 提供默认值
- 提供 `initial()` 或 `empty()` 工厂方法

### 步骤 2：定义 ViewIntent

```kotlin
sealed class UserIntent : ViewIntent {
    /**
     * 初始化
     */
    object Init : UserIntent()
    
    /**
     * 刷新用户信息
     */
    object Refresh : UserIntent()
    
    /**
     * 更新用户名
     */
    data class UpdateUserName(val name: String) : UserIntent()
    
    /**
     * 清除错误
     */
    object ClearError : UserIntent()
}
```

**最佳实践：**
- 使用 `sealed class` 确保类型安全
- 每个用户操作对应一个 Intent
- Intent 应该包含执行操作所需的所有数据

### 步骤 3：创建 Repository

```kotlin
class UserRepository : BaseRepository() {
    
    private val apiService = getApiService<UserApiService>()
    
    fun getUserInfo(): Flow<NetworkResult<UserInfo>> {
        return requestFlow(
            apiCall = { apiService.getUserInfo() },
            showLoading = true,
            loadingMessage = "加载用户信息..."
        )
    }
}
```

### 步骤 4：创建 MviViewModel

```kotlin
class UserMviViewModel : MviViewModel<UserIntent, UserViewState>() {
    
    private val repository = UserRepository()
    
    override fun initialState(): UserViewState {
        return UserViewState.initial()
    }
    
    override fun reduce(currentState: UserViewState, intent: UserIntent): UserViewState {
        return when (intent) {
            is UserIntent.Init -> {
                // 初始化时加载用户信息
                loadUserInfo()
                currentState.copy(isLoading = true, loadingMessage = "加载中...")
            }
            
            is UserIntent.Refresh -> {
                // 刷新用户信息
                loadUserInfo()
                currentState.copy(isLoading = true, loadingMessage = "刷新中...")
            }
            
            is UserIntent.UpdateUserName -> {
                // 本地状态更新（不需要网络请求）
                currentState.copy(
                    userInfo = currentState.userInfo?.copy(name = intent.name)
                )
            }
            
            is UserIntent.ClearError -> {
                // 清除错误
                currentState.copy(showError = false, error = null)
            }
        }
    }
    
    /**
     * 加载用户信息
     * 使用 handleNetworkResult 处理网络请求结果
     */
    private fun loadUserInfo() {
        handleNetworkResult(
            flow = repository.getUserInfo(),
            onLoading = { state, message ->
                // 处理 Loading 状态
                state.copy(
                    isLoading = true,
                    loadingMessage = message
                )
            },
            onSuccess = { state, userInfo ->
                // 处理成功状态
                state.copy(
                    isLoading = false,
                    userInfo = userInfo,
                    showError = false,
                    error = null
                )
            },
            onError = { state, error ->
                // 处理错误状态
                val appException = AppException(
                    error.code,
                    error.message
                )
                state.copy(
                    isLoading = false,
                    error = appException,
                    showError = true
                )
            }
        )
    }
}
```

**关键点：**
- `initialState()`：返回初始状态
- `reduce()`：纯函数，根据 Intent 生成新状态
- `handleNetworkResult()`：处理网络请求，自动更新状态

### 步骤 5：创建 Activity（使用 MviActivity）

```kotlin
class UserMviActivity : MviActivity<UserMviViewModel, UserIntent, UserViewState>() {
    
    override fun layoutId(): Int = R.layout.activity_user
    
    override fun initView(savedInstanceState: Bundle?) {
        // 初始化视图
        findViewById<Button>(R.id.btnRefresh).setOnClickListener {
            dispatchIntent(UserIntent.Refresh)
        }
    }
    
    /**
     * 渲染状态（核心方法）
     * 当 ViewModel 中的 State 更新时，会自动调用此方法
     */
    override fun renderState(state: UserViewState) {
        // 处理 Loading 状态
        if (state.isLoading) {
            showLoading(state.loadingMessage)
        } else {
            dismissLoading()
        }
        
        // 处理数据
        state.userInfo?.let { userInfo ->
            findViewById<TextView>(R.id.tvName).text = userInfo.name
            findViewById<TextView>(R.id.tvEmail).text = userInfo.email
        }
        
        // 处理错误
        if (state.showError && state.error != null) {
            showError(state.error.errorMsg)
        }
    }
    
    override fun showLoading(message: String) {
        // 显示加载对话框（使用你项目中的 Loading 组件）
        // 例如：XPopup、Dialog 等
    }
    
    override fun dismissLoading() {
        // 隐藏加载对话框
    }
    
    override fun onResume() {
        super.onResume()
        // 发送 Intent 触发加载
        dispatchIntent(UserIntent.Init)
    }
}
```

### 步骤 6：创建 Activity（使用 MviVbActivity + ViewBinding）

```kotlin
class UserMviActivity : MviVbActivity<UserMviViewModel, UserIntent, UserViewState, ActivityUserBinding>() {
    
    override fun initBinding(layoutInflater: LayoutInflater): ActivityUserBinding {
        return ActivityUserBinding.inflate(layoutInflater)
    }
    
    override fun initView(savedInstanceState: Bundle?) {
        // 使用 mViewBind 访问视图，类型安全
        mViewBind.btnRefresh.setOnClickListener {
            dispatchIntent(UserIntent.Refresh)
        }
    }
    
    override fun renderState(state: UserViewState) {
        // 处理 Loading 状态
        if (state.isLoading) {
            showLoading(state.loadingMessage)
        } else {
            dismissLoading()
        }
        
        // 更新 UI（使用 ViewBinding）
        state.userInfo?.let { userInfo ->
            mViewBind.tvName.text = userInfo.name
            mViewBind.tvEmail.text = userInfo.email
        }
        
        // 处理错误
        if (state.showError && state.error != null) {
            showError(state.error.errorMsg)
        }
    }
    
    override fun showLoading(message: String) {
        // 显示加载对话框
    }
    
    override fun dismissLoading() {
        // 隐藏加载对话框
    }
    
    override fun onResume() {
        super.onResume()
        dispatchIntent(UserIntent.Init)
    }
}
```

### 步骤 7：创建 Fragment（使用 MviFragment）

```kotlin
class UserMviFragment : MviFragment<UserMviViewModel, UserIntent, UserViewState>() {
    
    override fun layoutId(): Int = R.layout.fragment_user
    
    override fun initView(savedInstanceState: Bundle?) {
        // 初始化视图
        view?.findViewById<Button>(R.id.btnRefresh)?.setOnClickListener {
            dispatchIntent(UserIntent.Refresh)
        }
    }
    
    /**
     * 懒加载
     * Fragment 可见时才会调用
     */
    override fun lazyLoadData() {
        dispatchIntent(UserIntent.Init)
    }
    
    override fun renderState(state: UserViewState) {
        // 处理 Loading 状态
        if (state.isLoading) {
            showLoading(state.loadingMessage)
        } else {
            dismissLoading()
        }
        
        // 更新 UI
        state.userInfo?.let { userInfo ->
            view?.findViewById<TextView>(R.id.tvName)?.text = userInfo.name
            view?.findViewById<TextView>(R.id.tvEmail)?.text = userInfo.email
        }
        
        // 处理错误
        if (state.showError && state.error != null) {
            showError(state.error.errorMsg)
        }
    }
    
    override fun showLoading(message: String) {
        // 显示加载对话框
    }
    
    override fun dismissLoading() {
        // 隐藏加载对话框
    }
}
```

### 步骤 8：创建 Fragment（使用 MviVbFragment + ViewBinding）

```kotlin
class UserMviFragment : MviVbFragment<UserMviViewModel, UserIntent, UserViewState, FragmentUserBinding>() {
    
    override fun initBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        attachToParent: Boolean
    ): FragmentUserBinding {
        return FragmentUserBinding.inflate(inflater, container, attachToParent)
    }
    
    override fun initView(savedInstanceState: Bundle?) {
        // 使用 mViewBind 访问视图
        mViewBind.btnRefresh.setOnClickListener {
            dispatchIntent(UserIntent.Refresh)
        }
    }
    
    override fun lazyLoadData() {
        // 懒加载：Fragment 可见时才加载数据
        dispatchIntent(UserIntent.Init)
    }
    
    override fun renderState(state: UserViewState) {
        // 处理 Loading 状态
        if (state.isLoading) {
            showLoading(state.loadingMessage)
        } else {
            dismissLoading()
        }
        
        // 更新 UI（使用 ViewBinding）
        state.userInfo?.let { userInfo ->
            mViewBind.tvName.text = userInfo.name
            mViewBind.tvEmail.text = userInfo.email
        }
        
        // 处理错误
        if (state.showError && state.error != null) {
            showError(state.error.errorMsg)
        }
    }
    
    override fun showLoading(message: String) {
        // 显示加载对话框
    }
    
    override fun dismissLoading() {
        // 隐藏加载对话框
    }
}
```

## 使用 SingleEvent（单次事件）

### 在 ViewModel 中发送事件

```kotlin
class UserMviViewModel : MviViewModel<UserIntent, UserViewState>() {
    
    private fun loadUserInfo() {
        handleNetworkResult(
            flow = repository.getUserInfo(),
            onSuccess = { state, userInfo ->
                // 成功时发送 Toast 事件
                sendSingleEvent(BaseSingleEvent.ShowToast("加载成功"))
                
                state.copy(
                    isLoading = false,
                    userInfo = userInfo
                )
            },
            onError = { state, error ->
                // 错误时发送错误提示事件
                sendSingleEvent(BaseSingleEvent.ShowError(error.message))
                
                state.copy(
                    isLoading = false,
                    error = AppException(error.code, error.message),
                    showError = true
                )
            }
        )
    }
    
    // 登录成功后导航到主页
    private fun login() {
        handleNetworkResult(
            flow = repository.login(),
            onSuccess = { state, result ->
                // 发送导航事件
                sendSingleEvent(BaseSingleEvent.Navigate("MainActivity"))
                
                state.copy(isLoading = false)
            }
        )
    }
    
    // 保存成功后关闭页面
    private fun saveData() {
        handleNetworkResult(
            flow = repository.saveData(),
            onSuccess = { state, _ ->
                // 发送关闭页面事件
                sendSingleEvent(BaseSingleEvent.Finish)
                
                state.copy(isLoading = false)
            }
        )
    }
}
```

### 在 Activity/Fragment 中观察事件

```kotlin
class UserMviActivity : MviActivity<UserMviViewModel, UserIntent, UserViewState>() {
    
    // 重写 handleSingleEvent 处理自定义事件
    override fun handleSingleEvent(event: BaseSingleEvent) {
        when (event) {
            is BaseSingleEvent.ShowToast -> {
                // 显示 Toast（使用你项目中的 Toast 工具）
                Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show()
            }
            
            is BaseSingleEvent.ShowError -> {
                // 显示错误提示（使用你项目中的错误提示组件）
                showErrorDialog(event.message)
            }
            
            is BaseSingleEvent.Navigate -> {
                // 页面导航
                when (event.destination) {
                    "MainActivity" -> {
                        startActivity(Intent(this, MainActivity::class.java))
                    }
                    "DetailActivity" -> {
                        val intent = Intent(this, DetailActivity::class.java)
                        event.args?.forEach { (key, value) ->
                            intent.putExtra(key, value.toString())
                        }
                        startActivity(intent)
                    }
                }
            }
            
            is BaseSingleEvent.Finish -> {
                // 关闭页面
                finish()
            }
        }
    }
}
```

### 自定义 SingleEvent

```kotlin
// 在项目中定义自定义事件
sealed class UserSingleEvent : SingleEvent {
    data class ShowCustomDialog(val title: String, val message: String) : UserSingleEvent()
    data class OpenShareDialog(val content: String) : UserSingleEvent()
}

// 在 ViewModel 中使用
class UserMviViewModel : MviViewModel<UserIntent, UserViewState>() {
    
    private fun shareContent() {
        sendSingleEvent(UserSingleEvent.OpenShareDialog("分享内容"))
    }
}

// 在 Activity 中观察
override fun observeSingleEvent() {
    mViewModel.observeSingleEvent<UserSingleEvent>(this) { event ->
        when (event) {
            is UserSingleEvent.OpenShareDialog -> {
                // 打开分享对话框
            }
            is UserSingleEvent.ShowCustomDialog -> {
                // 显示自定义对话框
            }
        }
    }
}
```

## 使用 BaseViewState（简化版）

如果使用 `BaseViewState`，可以更简单：

```kotlin
class UserMviViewModel : MviViewModel<UserIntent, BaseViewState>() {
    
    private val repository = UserRepository()
    
    override fun initialState(): BaseViewState {
        return BaseViewState()
    }
    
    override fun reduce(currentState: BaseViewState, intent: UserIntent): BaseViewState {
        return when (intent) {
            is UserIntent.Init -> {
                loadUserInfo()
                currentState
            }
            else -> currentState
        }
    }
    
    private fun loadUserInfo() {
        // 使用简化版本，自动处理 Loading/Error
        handleNetworkResultSimple(
            flow = repository.getUserInfo(),
            onSuccess = { userInfo ->
                // 处理成功数据
                // 可以在这里更新其他状态
            }
        )
    }
}
```

## 使用 Compose + MVI

### BaseComposeMviActivity

```kotlin
class UserMviActivity : BaseComposeMviActivity<UserMviViewModel, UserIntent, UserViewState>() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContent 已在基类中调用
    }
    
    @Composable
    override fun Render(state: UserViewState, dispatch: (UserIntent) -> Unit) {
        Column {
            if (state.isLoading) {
                CircularProgressIndicator()
            }
            
            state.userInfo?.let { userInfo ->
                Text(text = userInfo.name)
                Text(text = userInfo.email)
            }
            
            Button(onClick = { dispatch(UserIntent.Refresh) }) {
                Text("刷新")
            }
        }
    }
}
```

### BaseComposeMviFragment

```kotlin
class UserMviFragment : BaseComposeMviFragment<UserMviViewModel, UserIntent, UserViewState>() {
    
    @Composable
    override fun Render(state: UserViewState, dispatch: (UserIntent) -> Unit) {
        Column {
            if (state.isLoading) {
                CircularProgressIndicator()
            }
            
            state.userInfo?.let { userInfo ->
                Text(text = userInfo.name)
                Text(text = userInfo.email)
            }
            
            Button(onClick = { dispatch(UserIntent.Refresh) }) {
                Text("刷新")
            }
        }
    }
}
```

## 最佳实践

### 1. State 设计
- ✅ 所有 UI 相关的状态都应该在 State 中
- ✅ State 应该是不可变的（使用 `data class`）
- ✅ 提供默认值和工厂方法

### 2. Intent 设计
- ✅ 每个用户操作对应一个 Intent
- ✅ Intent 应该包含执行操作所需的所有数据
- ✅ 使用 `sealed class` 确保类型安全

### 3. Reducer 设计
- ✅ Reducer 应该是纯函数，无副作用
- ✅ 只处理状态转换，不处理业务逻辑
- ✅ 异步操作在 ViewModel 中处理，通过 `updateState` 更新状态

### 4. 网络请求处理
- ✅ 使用 `handleNetworkResult` 处理网络请求
- ✅ Loading 状态自动管理
- ✅ 错误状态自动处理

### 5. 单次事件
- ✅ Toast、导航等一次性事件使用 `SingleEvent`
- ✅ 不要将一次性事件放在 State 中

## 注意事项

1. **状态不可变**：State 应该是不可变的，每次更新都创建新对象
2. **单一状态源**：所有 UI 状态都应该在 State 中
3. **纯函数 Reducer**：Reducer 函数应该是纯函数，无副作用
4. **异步操作**：异步操作应该在 ViewModel 中处理，通过 `updateState` 更新状态
5. **单次事件**：Toast、导航等一次性事件使用 `SingleEvent`，不要放在 State 中
6. **初始化字符串资源**：使用网络请求前，需要在 Application 中初始化 `StringResourceHelper`

## 与 MVVM 的对比

| 特性 | MVVM | MVI |
|------|------|-----|
| 状态管理 | 多个 LiveData/StateFlow | 单一 State 对象 |
| 数据流 | 双向 | 单向 |
| 可预测性 | 较低 | 高 |
| 测试难度 | 较难 | 较易 |
| 调试难度 | 较难 | 较易 |

## 迁移指南

详细的 MVVM 到 MVI 迁移指南，请参考 [MIGRATION_GUIDE.md](./MIGRATION_GUIDE.md)

## 完整示例

更多完整示例，请参考 [example/MviUsageExample.kt](./example/MviUsageExample.kt)

