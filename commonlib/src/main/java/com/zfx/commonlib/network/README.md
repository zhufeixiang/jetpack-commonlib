# 网络请求框架使用指南

## 概述

这是一个基于 Kotlin Flow 的网络请求框架，遵循职责单一、可维护、可继承的设计原则。

## 架构设计

### 核心组件

1. **NetworkResult** - 网络请求结果封装类（密封类）
   - `Loading` - 加载中状态
   - `Success<T>` - 成功状态，包含数据
   - `Error` - 错误状态，包含错误信息

2. **BaseResponse** - 服务器响应数据类（数据类版本，唯一使用）

3. **NetworkConfig** - 网络配置类
   - 职责单一：只负责配置相关参数
   - 支持构建器模式

4. **NetworkManager** - 网络管理器（唯一入口）
   - 单例模式
   - 负责创建和管理 Retrofit 和 API 服务实例

5. **BaseRepository** - 基础 Repository 类
   - `requestFlow`：标准请求（校验成功码）
   - `requestFlowRaw`：不校验结果，直接返回数据
   - 可继承，子类可以添加自定义方法

6. **ExceptionHandle** - 异常处理工具类（位于 `network/error` 包）
   - 统一的异常处理逻辑

## 快速开始

### 1. 初始化网络管理器

```kotlin
// 方式1：使用配置对象
val config = NetworkConfig(
    baseUrl = "https://api.example.com/",
    enableLogging = true
)
initNetworkManager(config)

// 方式2：使用构建器
initNetworkManager {
    baseUrl("https://api.example.com/")
    enableLogging(true)
    connectTimeout(30)
    readTimeout(30)
}

// 方式3：简单方式
initNetworkManager(
    baseUrl = "https://api.example.com/",
    enableLogging = true
)
```

### 2. 定义 API 接口

```kotlin
interface ApiService {
    @GET("user/info")
    suspend fun getUserInfo(): BaseResponse<UserInfo>
    
    @POST("user/login")
    suspend fun login(@Body request: LoginRequest): BaseResponse<LoginResponse>
}
```

### 3. 创建 Repository

```kotlin
class UserRepository : BaseRepository() {
    
    private val apiService = getApiService<ApiService>()
    
    /**
     * 获取用户信息
     */
    fun getUserInfo(): Flow<NetworkResult<UserInfo>> {
        return executeApiCall(
            apiCall = { apiService.getUserInfo() },
            showLoading = true,
            loadingMessage = "加载用户信息..."
        )
    }
    
    /**
     * 登录
     */
    fun login(username: String, password: String): Flow<NetworkResult<LoginResponse>> {
        return executeApiCall(
            apiCall = { 
                apiService.login(LoginRequest(username, password)) 
            }
        )
    }
}
```

### 4. 在 ViewModel 中使用

```kotlin
class UserViewModel : BaseViewModel() {
    
    private val repository = UserRepository()
    
    // 方式1：使用扩展函数（推荐）
    fun loadUserInfo() {
        collectNetworkResult(
            flow = repository.getUserInfo(),
            onLoading = { message ->
                // 显示加载提示
                showLoading(message)
            },
            onSuccess = { userInfo ->
                // 处理成功数据
                _userInfo.value = userInfo
            },
            onError = { error ->
                // 处理错误
                showError(error.message)
            }
        )
    }
    
    // 方式2：手动收集
    fun loadUserInfoManual() {
        repository.getUserInfo()
            .onEach { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        showLoading(result.message)
                    }
                    is NetworkResult.Success -> {
                        _userInfo.value = result.data
                    }
                    is NetworkResult.Error -> {
                        showError(result.message)
                    }
                }
            }
            .launchIn(viewModelScope)
    }
}
```

### 5. 在 Activity/Fragment 中观察

```kotlin
class UserActivity : BaseVmActivity<UserViewModel>() {
    
    override fun initData() {
        viewModel.userInfo.observe(this) { userInfo ->
            // 更新UI
        }
    }
}
```

## 高级用法

### 多环境切换

```kotlin
// 运行时切换环境
switchNetworkEnvironment(NetworkEnvironment.DEVELOPMENT)  // 切换到开发环境
switchNetworkEnvironment(NetworkEnvironment.PRE_RELEASE)   // 切换到预发布环境
switchNetworkEnvironment(NetworkEnvironment.PRODUCTION)    // 切换到生产环境

// 获取当前环境
val currentEnv = getCurrentNetworkEnvironment()

// 检查环境是否已配置
val isConfigured = isEnvironmentConfigured(NetworkEnvironment.DEVELOPMENT)
```

### 添加认证拦截器

```kotlin
// 在登录成功后添加认证拦截器
addAuthInterceptor(token)
```

### 自定义配置

```kotlin
val config = NetworkConfig(
    baseUrl = "https://api.example.com/",
    connectTimeout = 30L,
    readTimeout = 30L,
    writeTimeout = 30L,
    enableLogging = BuildConfig.DEBUG,
    interceptors = listOf(
        // 自定义拦截器
        CustomInterceptor()
    )
)
initNetworkManager(config)
```

### 继承 BaseRepository 添加自定义方法

```kotlin
class CustomRepository : BaseRepository() {
    
    /**
     * 自定义网络请求方法
     */
    fun customRequest(): Flow<NetworkResult<CustomData>> {
        return executeApiCall(
            apiCall = { /* 自定义请求逻辑 */ },
            showLoading = true
        )
    }
}
```

### 使用 Flow 扩展函数

```kotlin
// 只收集成功的数据
repository.getUserInfo()
    .collectSuccess { data ->
        // 处理数据
    }

// 只收集错误
repository.getUserInfo()
    .collectError { error ->
        // 处理错误
    }

// 只收集加载状态
repository.getUserInfo()
    .collectLoading { loading ->
        // 处理加载状态
    }
```

## 向后兼容

框架以新版 `NetworkManager` 为唯一入口，旧版接口已移除；如需兼容老代码，请在迁移后统一使用新版 `BaseResponse` 数据类与 Flow 封装。

## 最佳实践

1. **使用 Flow 而不是 LiveData**：新的代码应该使用 Flow 来处理网络请求
2. **使用 Repository 模式**：将网络请求逻辑封装在 Repository 中
3. **统一错误处理**：使用 `ExceptionHandle` 统一处理异常
4. **职责单一**：每个类只负责一个职责
5. **可继承性**：通过继承 `BaseRepository` 来扩展功能

## 注意事项

1. 使用前必须先初始化 `NetworkManager`
2. API 接口方法必须使用 `suspend` 关键字
3. 返回类型应该是 `BaseResponse<T>` 或 `T`
4. 在 ViewModel 中使用 `collectNetworkResult` 来收集结果
5. 多环境配置时，需要先配置所有环境，再切换并初始化
6. 环境切换会重新初始化网络管理器，会清除所有 API 服务缓存

## 环境配置最佳实践

1. **在 Application 中配置**：所有环境配置应该在 Application 的 `onCreate()` 中完成
2. **持久化环境选择**：传入 SharedPreferences 以持久化环境选择，下次启动时自动恢复
3. **根据 BuildConfig 自动选择**：根据构建类型自动选择合适的环境
4. **提供调试菜单**：在调试版本中提供环境切换功能，方便测试
5. **环境隔离**：确保不同环境的配置完全独立，避免相互影响

