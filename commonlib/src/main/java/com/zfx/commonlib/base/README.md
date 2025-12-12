# Base 基础组件使用指南

## 概述

Base 模块提供了 Android 开发中常用的基础组件，包括 Activity、Fragment、ViewModel 等基类，支持 MVVM 架构模式。

## 核心组件

### 1. BaseViewModel
- **职责**：ViewModel 基类，提供协程作用域
- **特点**：轻量级，不包含业务逻辑

### 2. BaseVmActivity / BaseVmFragment
- **职责**：MVVM 架构的 Activity/Fragment 基类
- **特点**：集成 ViewModel，支持 LiveData 观察

### 3. BaseVbActivity / BaseVbFragment
- **职责**：ViewBinding 的 Activity/Fragment 基类
- **特点**：类型安全的视图访问，无需 findViewById

### 4. BaseVmVbActivity / BaseVmVbFragment
- **职责**：MVVM + ViewBinding 的 Activity/Fragment 基类
- **特点**：结合 ViewModel 和 ViewBinding 的优势

## 快速开始

### 步骤 1：使用 BaseViewModel

```kotlin
class UserViewModel : BaseViewModel() {
    
    private val repository = UserRepository()
    
    val userInfo = MutableLiveData<UserInfo?>()
    val isLoading = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String?>()
    
    /**
     * 加载用户信息
     * 使用 collectResult 扩展函数处理网络请求
     */
    fun loadUserInfo() {
        collectResult(
            flow = repository.getUserInfo(),
            onLoading = { message ->
                isLoading.value = true
            },
            onSuccess = { userInfo ->
                isLoading.value = false
                this.userInfo.value = userInfo
            },
            onError = { error ->
                isLoading.value = false
                errorMessage.value = error.message
            }
        )
    }
    
    /**
     * 简化版本（不处理 Loading）
     */
    fun loadUserInfoSimple() {
        collectResult(
            flow = repository.getUserInfo(),
            onSuccess = { userInfo ->
                this.userInfo.value = userInfo
            },
            onError = { error ->
                errorMessage.value = error.message
            }
        )
    }
}
```

### 步骤 2：使用 BaseVmActivity

```kotlin
class UserActivity : BaseVmActivity<UserViewModel>() {
    
    override fun layoutId(): Int = R.layout.activity_user
    
    override fun initView(savedInstanceState: Bundle?) {
        // 初始化视图
        findViewById<Button>(R.id.btnRefresh).setOnClickListener {
            viewModel.loadUserInfo()
        }
    }
    
    /**
     * 创建观察者
     * 观察 ViewModel 中的 LiveData
     */
    override fun createObserver() {
        // 观察用户信息
        viewModel.userInfo.observe(this) { userInfo ->
            userInfo?.let {
                findViewById<TextView>(R.id.tvName).text = it.name
                findViewById<TextView>(R.id.tvEmail).text = it.email
            }
        }
        
        // 观察加载状态
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                showLoading()
            } else {
                dismissLoading()
            }
        }
        
        // 观察错误信息
        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                showError(it)
            }
        }
    }
    
    /**
     * 初始化数据
     */
    override fun initData() {
        viewModel.loadUserInfo()
    }
    
    override fun showLoading() {
        // 显示加载对话框
    }
    
    override fun dismissLoading() {
        // 隐藏加载对话框
    }
}
```

### 步骤 3：使用 BaseVbActivity（ViewBinding）

```kotlin
class UserActivity : BaseVbActivity<ActivityUserBinding>() {
    
    override fun initBinding(layoutInflater: LayoutInflater): ActivityUserBinding {
        return ActivityUserBinding.inflate(layoutInflater)
    }
    
    override fun initView(savedInstanceState: Bundle?) {
        // 使用 mViewBind 访问视图，类型安全
        mViewBind.btnRefresh.setOnClickListener {
            // 处理点击事件
        }
    }
    
    override fun createObserver() {
        // 创建观察者
    }
    
    override fun initData() {
        // 初始化数据
    }
}
```

### 步骤 4：使用 BaseVmVbActivity（MVVM + ViewBinding）

```kotlin
class UserActivity : BaseVmVbActivity<UserViewModel, ActivityUserBinding>() {
    
    override fun initBinding(layoutInflater: LayoutInflater): ActivityUserBinding {
        return ActivityUserBinding.inflate(layoutInflater)
    }
    
    override fun initView(savedInstanceState: Bundle?) {
        // 使用 mViewBind 访问视图
        mViewBind.btnRefresh.setOnClickListener {
            viewModel.loadUserInfo()
        }
    }
    
    override fun createObserver() {
        // 观察 ViewModel 中的 LiveData
        viewModel.userInfo.observe(this) { userInfo ->
            userInfo?.let {
                mViewBind.tvName.text = it.name
                mViewBind.tvEmail.text = it.email
            }
        }
        
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                showLoading()
            } else {
                dismissLoading()
            }
        }
    }
    
    override fun initData() {
        viewModel.loadUserInfo()
    }
    
    override fun showLoading() {
        // 显示加载对话框
    }
    
    override fun dismissLoading() {
        // 隐藏加载对话框
    }
}
```

### 步骤 5：使用 BaseVmFragment

```kotlin
class UserFragment : BaseVmFragment<UserViewModel>() {
    
    override fun layoutId(): Int = R.layout.fragment_user
    
    override fun initView(savedInstanceState: Bundle?) {
        // 初始化视图
        view?.findViewById<Button>(R.id.btnRefresh)?.setOnClickListener {
            viewModel.loadUserInfo()
        }
    }
    
    /**
     * 懒加载
     * Fragment 可见时才会调用
     */
    override fun lazyLoadData() {
        viewModel.loadUserInfo()
    }
    
    override fun createObserver() {
        // 观察 ViewModel 中的 LiveData
        viewModel.userInfo.observe(this) { userInfo ->
            userInfo?.let {
                view?.findViewById<TextView>(R.id.tvName)?.text = it.name
                view?.findViewById<TextView>(R.id.tvEmail)?.text = it.email
            }
        }
        
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                showLoading()
            } else {
                dismissLoading()
            }
        }
    }
    
    override fun showLoading() {
        // 显示加载对话框
    }
    
    override fun dismissLoading() {
        // 隐藏加载对话框
    }
}
```

### 步骤 6：使用 BaseVbFragment（ViewBinding）

```kotlin
class UserFragment : BaseVbFragment<FragmentUserBinding>() {
    
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
            // 处理点击事件
        }
    }
    
    /**
     * 懒加载
     */
    override fun lazyLoadData() {
        // Fragment 可见时才加载数据
    }
    
    override fun createObserver() {
        // 创建观察者
    }
}
```

### 步骤 7：使用 BaseVmVbFragment（MVVM + ViewBinding）

```kotlin
class UserFragment : BaseVmVbFragment<UserViewModel, FragmentUserBinding>() {
    
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
            viewModel.loadUserInfo()
        }
    }
    
    override fun lazyLoadData() {
        // Fragment 可见时才加载数据
        viewModel.loadUserInfo()
    }
    
    override fun createObserver() {
        // 观察 ViewModel 中的 LiveData
        viewModel.userInfo.observe(this) { userInfo ->
            userInfo?.let {
                mViewBind.tvName.text = it.name
                mViewBind.tvEmail.text = it.email
            }
        }
        
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                showLoading()
            } else {
                dismissLoading()
            }
        }
    }
    
    override fun showLoading() {
        // 显示加载对话框
    }
    
    override fun dismissLoading() {
        // 隐藏加载对话框
    }
}
```

## BaseViewModel 扩展函数

### collectResult（收集网络请求结果）

```kotlin
class UserViewModel : BaseViewModel() {
    
    fun loadUserInfo() {
        collectResult(
            flow = repository.getUserInfo(),
            onLoading = { message ->
                // 处理加载状态
                _loadingMessage.value = message
            },
            onSuccess = { userInfo ->
                // 处理成功数据
                _userInfo.value = userInfo
            },
            onError = { error ->
                // 处理错误
                _errorMessage.value = error.message
            }
        )
    }
}
```

### collectResult（简化版本）

```kotlin
class UserViewModel : BaseViewModel() {
    
    fun loadUserInfo() {
        collectResult(
            flow = repository.getUserInfo(),
            onSuccess = { userInfo ->
                _userInfo.value = userInfo
            },
            onError = { error ->
                _errorMessage.value = error.message
            }
        )
    }
}
```

### collectResultWithLoading（带加载提示）

```kotlin
class UserViewModel : BaseViewModel() {
    
    fun loadUserInfo() {
        collectResultWithLoading(
            flow = repository.getUserInfo(),
            showLoading = { message ->
                // 显示加载提示
                _showLoading.value = true
            },
            dismissLoading = {
                // 隐藏加载提示
                _showLoading.value = false
            },
            onSuccess = { userInfo ->
                _userInfo.value = userInfo
            },
            onError = { error ->
                _errorMessage.value = error.message
            }
        )
    }
}
```

## Fragment 懒加载

所有 Fragment 基类都支持懒加载功能：

```kotlin
class UserFragment : BaseVmFragment<UserViewModel>() {
    
    /**
     * 懒加载
     * Fragment 可见时才会调用
     */
    override fun lazyLoadData() {
        viewModel.loadUserInfo()
    }
    
    /**
     * 自定义延迟加载时间（可选）
     * 默认 300 毫秒，防止切换动画时数据加载导致的卡顿
     */
    override fun lazyLoadTime(): Long {
        return 500  // 自定义延迟时间
    }
}
```

**懒加载机制：**
- Fragment 在 `onResume()` 时检查是否可见
- 如果可见且是第一次加载，延迟 `lazyLoadTime()` 毫秒后调用 `lazyLoadData()`
- 防止切换动画时数据加载导致的卡顿

## ViewBinding 生命周期管理

使用 ViewBinding 的 Fragment 基类会自动管理 ViewBinding 的生命周期：

```kotlin
class UserFragment : BaseVbFragment<FragmentUserBinding>() {
    
    // ViewBinding 在 onCreateView 时创建
    // 在 onDestroyView 时自动置空，避免内存泄漏
    
    override fun initView(savedInstanceState: Bundle?) {
        // 使用 mViewBind 访问视图
        mViewBind.btnRefresh.setOnClickListener {
            // 安全访问，不会空指针
        }
    }
}
```

## 组件对比

| 组件 | ViewModel | ViewBinding | 特点 |
|------|-----------|-------------|------|
| **BaseVmActivity** | ✅ | ❌ | MVVM 架构，使用 findViewById |
| **BaseVbActivity** | ❌ | ✅ | 纯 ViewBinding，类型安全 |
| **BaseVmVbActivity** | ✅ | ✅ | MVVM + ViewBinding，推荐使用 |
| **BaseVmFragment** | ✅ | ❌ | MVVM 架构，支持懒加载 |
| **BaseVbFragment** | ❌ | ✅ | 纯 ViewBinding，支持懒加载 |
| **BaseVmVbFragment** | ✅ | ✅ | MVVM + ViewBinding，支持懒加载，推荐使用 |

## 最佳实践

### 1. ViewModel 设计
- ✅ 使用 LiveData 或 StateFlow 管理状态
- ✅ 使用 `collectResult` 处理网络请求
- ✅ 业务逻辑放在 ViewModel 中

### 2. Activity/Fragment 设计
- ✅ 优先使用 ViewBinding 版本（类型安全）
- ✅ 在 `createObserver()` 中观察 LiveData
- ✅ 在 `initData()` 中初始化数据

### 3. Fragment 懒加载
- ✅ 在 `lazyLoadData()` 中加载数据
- ✅ 根据动画时长调整 `lazyLoadTime()`

### 4. 错误处理
- ✅ 统一错误处理逻辑
- ✅ 使用 Toast 或 Dialog 显示错误信息

## 注意事项

1. **ViewModel 获取**：基类会自动创建 ViewModel，无需手动创建
2. **ViewBinding 生命周期**：Fragment 中的 ViewBinding 会在 `onDestroyView` 时自动置空
3. **懒加载时机**：Fragment 的懒加载在 `onResume()` 时触发
4. **网络请求**：使用 `collectResult` 扩展函数处理网络请求结果
5. **初始化字符串资源**：使用网络请求前，需要在 Application 中初始化 `StringResourceHelper`

## 与 MVI 架构的对比

| 特性 | MVVM (Base) | MVI |
|------|-------------|-----|
| 状态管理 | 多个 LiveData/StateFlow | 单一 State 对象 |
| 数据流 | 双向 | 单向 |
| 复杂度 | 较低 | 较高 |
| 适用场景 | 简单页面 | 复杂页面 |

**建议：**
- 简单页面使用 MVVM（Base 组件）
- 复杂页面使用 MVI 架构

## 完整示例

更多完整示例，请参考项目中的实际使用案例。

