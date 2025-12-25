# 网络请求框架使用指南

## 概述

这是一个基于 Kotlin Flow 的网络请求框架，遵循职责单一、可维护、可继承的设计原则。

## 架构设计

### 核心组件

1. **NetworkResult** - 网络请求结果封装类（密封类）
   - `Loading` - 加载中状态
   - `Success<T>` - 成功状态，包含数据
   - `Error` - 错误状态，包含错误信息

2. **IBaseResponse** - 网络响应接口，允许自定义响应结构
   - **BaseResponse** - 默认实现（字段：code、message、data）
   - 如果你的项目响应结构不同，可以实现 `IBaseResponse` 接口

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

### 0. 初始化字符串资源（必须）

在使用网络框架之前，需要在 Application 中初始化字符串资源工具类：

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 初始化字符串资源工具类（支持国际化）
        StringResourceHelper.init(this)
    }
}
```

**注意**：如果不初始化，所有错误消息将返回空字符串。

### 1. 初始化网络管理器

#### 基础用法

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

#### 支持 HTTP（内网服务器）

如果使用内网服务器（HTTP），需要：

**1. 在 AndroidManifest.xml 中配置网络安全策略**

```xml
<application
    android:usesCleartextTraffic="true"
    ...>
    <!-- 或者使用网络安全配置 -->
    <meta-data
        android:name="android.security.net.config"
        android:resource="@xml/network_security_config" />
</application>
```

创建 `res/xml/network_security_config.xml`：
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="true">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
</network-security-config>
```

**2. 在代码中启用 HTTP 支持**

```kotlin
initNetworkManager {
    baseUrl("http://192.168.1.100:8080/")  // HTTP 地址
    allowCleartextTraffic(true)  // 允许 HTTP
    enableLogging(true)
}
```

#### 信任所有 SSL 证书（自签名证书）

如果使用自签名证书（开发环境或内网），需要跳过证书验证：

```kotlin
initNetworkManager {
    baseUrl("https://192.168.1.100:8443/")  // HTTPS 自签名证书
    trustAllCertificates(true)  // 信任所有证书（包括自签名证书）
    enableLogging(true)
}
```

**⚠️ 安全警告**：
- `trustAllCertificates(true)` 会跳过所有 SSL 证书验证
- 这会信任所有证书，包括自签名证书和无效证书
- **仅用于开发环境或内网**，生产环境请勿使用
- 生产环境应该使用有效的 SSL 证书

**使用场景**：
- 开发环境使用自签名证书
- 内网服务器使用自签名证书
- 测试环境需要跳过证书验证

**之前的配置方式（已废弃）**：
```kotlin
// 旧方式（不再需要）
.sslSocketFactory(createSSLSocketFactory())
.hostnameVerifier(new TrustAllHostnameVerifier())
```

现在只需要：
```kotlin
trustAllCertificates(true)  // 一行搞定
```

#### 启用网络缓存

```kotlin
initNetworkManager {
    baseUrl("https://api.example.com/")
    enableCache(true)  // 启用缓存
    cacheSize(50 * 1024 * 1024)  // 缓存大小：50MB
    cacheDirectory(File(context.cacheDir, "network-cache"))  // 缓存目录（可选）
}
```

#### 使用 ScalarsConverterFactory（返回简单类型）

如果接口返回的是纯文本（String）或简单类型（Int、Boolean 等），需要启用：

```kotlin
initNetworkManager {
    baseUrl("https://api.example.com/")
    useScalarsConverter(true)  // 启用 ScalarsConverter
}

// 接口定义
interface ApiService {
    @GET("version")
    suspend fun getVersion(): String  // 直接返回 String
    
    @GET("count")
    suspend fun getCount(): Int  // 直接返回 Int
}
```

**注意**：ScalarsConverterFactory 必须在 GsonConverterFactory 之前添加（框架已自动处理）。

**重要：在 BaseRepository 中的使用方式**

使用 `ScalarsConverter` 返回简单类型时，**不能使用 `requestFlow`**，需要使用 `requestFlowRaw`：

```kotlin
class AppRepository : BaseRepository() {
    private val apiService = getApiService<ApiService>()
    
    // ✅ 正确：使用 requestFlowRaw 处理简单类型
    fun getVersion(): Flow<NetworkResult<String>> {
        return requestFlowRaw {
            apiService.getVersion()
        }
    }
    
    // ❌ 错误：不能使用 requestFlow（因为 String 不实现 IBaseResponse）
    // fun getVersion(): Flow<NetworkResult<String>> {
    //     return requestFlow { apiService.getVersion() }  // 编译错误！
    // }
}
```

详细说明请参考下面的"BaseRepository 方法选择指南"章节。

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
        return requestFlow(
            apiCall = { apiService.getUserInfo() },
            showLoading = true,
            loadingMessage = "加载用户信息..."
        )
    }
    
    /**
     * 登录
     */
    fun login(username: String, password: String): Flow<NetworkResult<LoginResponse>> {
        return requestFlow(
            apiCall = { 
                apiService.login(LoginRequest(username, password)) 
            },
            showLoading = true,
            loadingMessage = "登录中..."
        )
    }
    
    /**
     * 不校验响应结果的请求（使用 requestFlowRaw）
     */
    fun getRawData(): Flow<NetworkResult<RawData>> {
        return requestFlowRaw(
            apiCall = { apiService.getRawData() },
            showLoading = false  // 不显示加载提示
        )
    }
}
```

### 4. 在 ViewModel 中使用

```kotlin
class UserViewModel : BaseViewModel() {
    
    private val repository = UserRepository()
    
    // 方式1：使用扩展函数 collectResult（推荐）
    fun loadUserInfo() {
        collectResult(
            flow = repository.getUserInfo(),
            onLoading = { message ->
                // 显示加载提示
                _loadingMessage.value = message
                _isLoading.value = true
            },
            onSuccess = { userInfo ->
                // 处理成功数据
                _isLoading.value = false
                _userInfo.value = userInfo
            },
            onError = { error ->
                // 处理错误
                _isLoading.value = false
                _errorMessage.value = error.message
            }
        )
    }
    
    // 方式2：简化版本（不处理 Loading）
    fun loadUserInfoSimple() {
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
    
    // 方式3：手动收集
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
    
    // 方式4：使用 awaitResult（挂起函数，顺序执行）
    fun loadUserData() {
        viewModelScope.launch {
            try {
                // 顺序执行，如果任何一个失败，都会跳到 catch
                val userInfo = awaitResult(repository.getUserInfo())
                val posts = awaitResult(repository.getUserPosts(userInfo.id))
                val friends = awaitResult(repository.getUserFriends(userInfo.id))
                
                _userData.value = UserData(userInfo, posts, friends)
            } catch (e: Exception) {
                // ⚠️ 注意：所有异常都会统一走到这里
                // 如果第一个请求失败，后面的请求不会执行
                _errorMessage.value = e.message ?: "请求失败"
            }
        }
    }
    
    // 方式5：使用 awaitResultSafe（分别处理每个请求）
    fun loadUserDataSafe() {
        viewModelScope.launch {
            // 分别处理每个请求，允许部分失败
            val userInfoResult = awaitResultSafe(repository.getUserInfo())
            val postsResult = awaitResultSafe(
                repository.getUserPosts(userInfoResult.getOrNull()?.id ?: "")
            )
            val friendsResult = awaitResultSafe(
                repository.getUserFriends(userInfoResult.getOrNull()?.id ?: "")
            )
            
            // 分别处理每个结果
            userInfoResult.onSuccess { userInfo ->
                _userInfo.value = userInfo
            }.onFailure { e ->
                _errorMessage.value = "获取用户信息失败: ${e.message}"
            }
            
            // 或者组合处理
            if (userInfoResult.isSuccess && postsResult.isSuccess && friendsResult.isSuccess) {
                _userData.value = UserData(
                    userInfoResult.getOrThrow(),
                    postsResult.getOrThrow(),
                    friendsResult.getOrThrow()
                )
            } else {
                // 部分请求失败，但仍可以使用成功的数据
                val errors = mutableListOf<String>()
                userInfoResult.onFailure { errors.add("用户信息") }
                postsResult.onFailure { errors.add("用户文章") }
                friendsResult.onFailure { errors.add("用户好友") }
                _errorMessage.value = "以下请求失败: ${errors.joinToString(", ")}"
            }
        }
    }
    
    // 方式6：使用 awaitResultOrDefault（失败时使用默认值）
    fun loadUserDataWithDefault() {
        viewModelScope.launch {
            // 失败时返回默认值，不会抛出异常
            val userInfo = awaitResultOrDefault(
                flow = repository.getUserInfo(),
                defaultValue = UserInfo()  // 默认值
            )
            val posts = awaitResultOrDefault(
                flow = repository.getUserPosts(userInfo.id),
                defaultValue = emptyList<Post>()  // 默认值
            )
            
            // 总是有值，不需要 try-catch
            _userData.value = UserData(userInfo, posts)
        }
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

## BaseRepository 方法选择指南

`BaseRepository` 提供了三个主要的网络请求方法，根据不同的场景选择合适的方法：

### 方法对比

| 方法 | 返回类型要求 | 是否校验成功/失败 | 适用场景 |
|------|------------|----------------|---------|
| `requestFlow` | 必须实现 `IBaseResponse<T>` | ✅ 是 | JSON 对象响应（最常见） |
| `requestFlowNoData` | 必须实现 `IBaseResponse<*>` | ✅ 是 | JSON 对象响应，但 data 为空 |
| `requestFlowRaw` | 任意类型 `T` | ❌ 否 | 简单类型或原始响应对象 |

### 1. requestFlow（推荐，用于 JSON 对象响应）

**使用场景**：
- 接口返回 JSON 对象，格式如：`{"code": 200, "message": "success", "data": {...}}`
- 需要自动校验成功/失败状态
- 需要自动处理未登录错误码

**示例**：
```kotlin
interface ApiService {
    @GET("user/info")
    suspend fun getUserInfo(): BaseResponse<UserInfo>  // ✅ 返回 BaseResponse
}

class UserRepository : BaseRepository() {
    private val apiService = getApiService<ApiService>()
    
    fun getUserInfo(): Flow<NetworkResult<UserInfo>> {
        return requestFlow {
            apiService.getUserInfo()  // ✅ 可以使用
        }
    }
}
```

### 2. requestFlowNoData（用于无数据响应）

**使用场景**：
- 接口返回 JSON 对象，但 data 字段为空或不需要
- 只需要判断操作是否成功（如删除、更新、创建等）

**示例**：
```kotlin
interface ApiService {
    @DELETE("user/{id}")
    suspend fun deleteUser(@Path("id") id: Long): BaseResponse<Unit>  // ✅ 返回 BaseResponse
}

class UserRepository : BaseRepository() {
    private val apiService = getApiService<ApiService>()
    
    fun deleteUser(id: Long): Flow<NetworkResult<Unit>> {
        return requestFlowNoData {
            apiService.deleteUser(id)  // ✅ 可以使用
        }
    }
}
```

### 3. requestFlowRaw（用于简单类型或原始响应）

**使用场景**：
1. **简单类型响应**（需要启用 `useScalarsConverter(true)`）：
   - 接口返回纯文本（String）
   - 接口返回简单数字（Int、Long）
   - 接口返回布尔值（Boolean）

2. **原始响应对象**：
   - 需要直接获取响应对象，不进行成功/失败判断
   - 自定义响应处理逻辑

**示例 1：简单类型（需要 ScalarsConverter）**：
```kotlin
// 1. 启用 ScalarsConverter
initNetworkManager {
    baseUrl("https://api.example.com/")
    useScalarsConverter(true)  // 启用
}

// 2. 接口定义
interface ApiService {
    @GET("version")
    suspend fun getVersion(): String  // 返回纯文本 "2.0.0"
    
    @GET("count")
    suspend fun getCount(): Int  // 返回纯数字 100
}

// 3. Repository 中使用
class AppRepository : BaseRepository() {
    private val apiService = getApiService<ApiService>()
    
    fun getVersion(): Flow<NetworkResult<String>> {
        return requestFlowRaw {
            apiService.getVersion()  // ✅ 可以使用
        }
    }
    
    fun getCount(): Flow<NetworkResult<Int>> {
        return requestFlowRaw {
            apiService.getCount()  // ✅ 可以使用
        }
    }
    
    // ❌ 错误：不能使用 requestFlow（因为 String/Int 不实现 IBaseResponse）
    // fun getVersion(): Flow<NetworkResult<String>> {
    //     return requestFlow { apiService.getVersion() }  // 编译错误！
    // }
}
```

**示例 2：原始响应对象**：
```kotlin
interface ApiService {
    @GET("data")
    suspend fun getData(): BaseResponse<Data>  // 返回 BaseResponse
}

class DataRepository : BaseRepository() {
    private val apiService = getApiService<ApiService>()
    
    fun getData(): Flow<NetworkResult<BaseResponse<Data>>> {
        return requestFlowRaw {
            apiService.getData()  // ✅ 直接返回响应对象
        }
    }
}
```

### 选择建议

1. **大多数情况**：使用 `requestFlow`
   - 接口返回 JSON 对象
   - 需要自动校验成功/失败

2. **无数据操作**：使用 `requestFlowNoData`
   - 删除、更新、创建等操作
   - 只需要判断成功/失败

3. **简单类型**：使用 `requestFlowRaw`
   - 接口返回纯文本、数字、布尔值
   - 需要启用 `useScalarsConverter(true)`

4. **自定义处理**：使用 `requestFlowRaw`
   - 需要直接获取响应对象
   - 自定义成功/失败判断逻辑

### 完整示例

```kotlin
// 配置
initNetworkManager {
    baseUrl("https://api.example.com/")
    useScalarsConverter(true)  // 启用（用于简单类型）
}

// 接口
interface ApiService {
    // JSON 对象响应
    @GET("user/info")
    suspend fun getUserInfo(): BaseResponse<UserInfo>
    
    // 无数据响应
    @DELETE("user/{id}")
    suspend fun deleteUser(@Path("id") id: Long): BaseResponse<Unit>
    
    // 简单类型响应（需要 ScalarsConverter）
    @GET("version")
    suspend fun getVersion(): String
}

// Repository
class UserRepository : BaseRepository() {
    private val apiService = getApiService<ApiService>()
    
    // ✅ JSON 对象：使用 requestFlow
    fun getUserInfo(): Flow<NetworkResult<UserInfo>> {
        return requestFlow {
            apiService.getUserInfo()
        }
    }
    
    // ✅ 无数据：使用 requestFlowNoData
    fun deleteUser(id: Long): Flow<NetworkResult<Unit>> {
        return requestFlowNoData {
            apiService.deleteUser(id)
        }
    }
    
    // ✅ 简单类型：使用 requestFlowRaw
    fun getVersion(): Flow<NetworkResult<String>> {
        return requestFlowRaw {
            apiService.getVersion()
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

### 动态 BaseUrl 切换（多域名支持）

框架使用内置的 `DynamicBaseUrlInterceptor` 实现动态 BaseUrl 切换，无需额外依赖，不依赖 jcenter 仓库。

**注意**：默认禁用（`enableDynamicBaseUrl = false`），单域名项目无需启用。只有多域名项目才需要启用。

#### 使用方式

```kotlin
// 1. 初始化时，启用动态 BaseUrl 功能（多域名项目需要）
initNetworkManager {
    baseUrl("https://api.example.com/")
    enableDynamicBaseUrl(true)  // 启用动态 BaseUrl（多域名项目需要）
}

// 2. 配置域名映射
putDomain("news", "https://news.example.com/")
putDomain("upload", "https://upload.example.com/")

// 2. 在接口中使用
interface ApiService {
    @Headers("Domain-Name: news")
    @GET("list")
    suspend fun getNews(): BaseResponse<List<News>>
    
    @Headers("Domain-Name: upload")
    @POST("file")
    suspend fun upload(@Body body: RequestBody): BaseResponse<Unit>
}

// 3. 设置全局 BaseUrl（可选）
setGlobalBaseUrl("https://api.example.com/")

// 4. 移除域名（可选）
removeDomain("news")

// 5. 清除所有域名（可选）
clearAllDomains()
```

**特点**：
- ✅ 无需额外依赖，纯 Kotlin 实现
- ✅ 不依赖 jcenter 仓库
- ✅ 功能完整，支持多域名切换
- ✅ 线程安全，使用 ConcurrentHashMap

### 添加认证拦截器

```kotlin
// 在登录成功后添加认证拦截器
addAuthInterceptor(token)
```

### 异常处理方式对比

框架提供了多种异常处理方式，适用于不同的场景：

#### 方式1：使用 `awaitResult`（统一异常处理）

```kotlin
fun loadUserData() {
    viewModelScope.launch {
        try {
            val userInfo = awaitResult(repository.getUserInfo())
            val posts = awaitResult(repository.getUserPosts(userInfo.id))
            val friends = awaitResult(repository.getUserFriends(userInfo.id))
            
            _userData.value = UserData(userInfo, posts, friends)
        } catch (e: Exception) {
            // ⚠️ 注意：所有异常都会统一走到这里
            // 如果第一个请求失败，后面的请求不会执行
            _errorMessage.value = e.message ?: "请求失败"
        }
    }
}
```

**特点**：
- ✅ 代码简洁
- ❌ 无法区分是哪个请求失败
- ❌ 第一个请求失败后，后续请求不会执行
- ✅ 适合所有请求必须成功的场景

#### 方式2：使用 `awaitResultSafe`（分别处理每个请求）

```kotlin
fun loadUserDataSafe() {
    viewModelScope.launch {
        // 分别处理每个请求，允许部分失败
        val userInfoResult = awaitResultSafe(repository.getUserInfo())
        val postsResult = awaitResultSafe(
            repository.getUserPosts(userInfoResult.getOrNull()?.id ?: "")
        )
        val friendsResult = awaitResultSafe(
            repository.getUserFriends(userInfoResult.getOrNull()?.id ?: "")
        )
        
        // 分别处理每个结果
        userInfoResult.onSuccess { userInfo ->
            _userInfo.value = userInfo
        }.onFailure { e ->
            _errorMessage.value = "获取用户信息失败: ${e.message}"
        }
        
        // 或者组合处理
        if (userInfoResult.isSuccess && postsResult.isSuccess && friendsResult.isSuccess) {
            _userData.value = UserData(
                userInfoResult.getOrThrow(),
                postsResult.getOrThrow(),
                friendsResult.getOrThrow()
            )
        } else {
            // 部分请求失败，但仍可以使用成功的数据
            val errors = mutableListOf<String>()
            userInfoResult.onFailure { errors.add("用户信息") }
            postsResult.onFailure { errors.add("用户文章") }
            friendsResult.onFailure { errors.add("用户好友") }
            _errorMessage.value = "以下请求失败: ${errors.joinToString(", ")}"
        }
    }
}
```

**特点**：
- ✅ 可以分别处理每个请求的成功/失败
- ✅ 允许部分请求失败，继续执行其他请求
- ✅ 可以知道具体是哪个请求失败了
- ✅ 适合部分请求失败仍可继续的场景

#### 方式3：使用 `awaitResultOrDefault`（失败时使用默认值）

```kotlin
fun loadUserDataWithDefault() {
    viewModelScope.launch {
        // 失败时返回默认值，不会抛出异常
        val userInfo = awaitResultOrDefault(
            flow = repository.getUserInfo(),
            defaultValue = UserInfo()  // 默认值
        )
        val posts = awaitResultOrDefault(
            flow = repository.getUserPosts(userInfo.id),
            defaultValue = emptyList<Post>()  // 默认值
        )
        
        // 总是有值，不需要 try-catch
        _userData.value = UserData(userInfo, posts)
    }
}
```

**特点**：
- ✅ 代码最简洁，不需要 try-catch
- ✅ 失败时使用默认值，不会中断执行
- ✅ 适合允许使用默认值的场景
- ❌ 无法知道哪些请求失败了

#### 方式4：分别使用 try-catch（精细控制）

```kotlin
fun loadUserDataFineGrained() {
    viewModelScope.launch {
        var userInfo: UserInfo? = null
        var posts: List<Post>? = null
        var friends: List<Friend>? = null
        val errors = mutableListOf<String>()
        
        // 分别处理每个请求
        try {
            userInfo = awaitResult(repository.getUserInfo())
        } catch (e: Exception) {
            errors.add("获取用户信息失败: ${e.message}")
        }
        
        try {
            posts = awaitResult(repository.getUserPosts(userInfo?.id ?: ""))
        } catch (e: Exception) {
            errors.add("获取用户文章失败: ${e.message}")
        }
        
        try {
            friends = awaitResult(repository.getUserFriends(userInfo?.id ?: ""))
        } catch (e: Exception) {
            errors.add("获取用户好友失败: ${e.message}")
        }
        
        // 根据结果决定如何处理
        if (errors.isNotEmpty()) {
            _errorMessage.value = errors.joinToString("\n")
        }
        
        // 即使部分失败，也可以使用成功的数据
        if (userInfo != null) {
            _userData.value = UserData(
                userInfo = userInfo,
                posts = posts ?: emptyList(),
                friends = friends ?: emptyList()
            )
        }
    }
}
```

**特点**：
- ✅ 最灵活，可以精细控制每个请求
- ✅ 可以收集所有错误信息
- ❌ 代码相对冗长

#### 方式5：并行执行（请求之间没有依赖关系）

如果请求之间没有依赖关系，可以并行执行以提高效率：

```kotlin
fun loadUserDataParallel() {
    viewModelScope.launch {
        try {
            // 先获取 userInfo（因为后续请求依赖它）
            val userInfo = awaitResult(repository.getUserInfo())
            
            // 然后并行执行两个不依赖的请求
            val postsDeferred = async { awaitResult(repository.getUserPosts(userInfo.id)) }
            val friendsDeferred = async { awaitResult(repository.getUserFriends(userInfo.id)) }
            
            // 等待所有并行请求完成
            val posts = postsDeferred.await()
            val friends = friendsDeferred.await()
            
            _userData.value = UserData(userInfo, posts, friends)
        } catch (e: Exception) {
            _errorMessage.value = e.message ?: "请求失败"
        }
    }
}
```

**注意**：
- ⚠️ 如果请求之间有依赖关系（如 `getUserPosts` 需要 `userInfo.id`），不能并行执行第一个请求
- ✅ 可以先执行有依赖的请求，然后并行执行没有依赖的请求
- ✅ 如果所有请求都没有依赖关系，可以全部并行执行

**完全并行执行示例**（所有请求都没有依赖关系）：

```kotlin
fun loadIndependentData() {
    viewModelScope.launch {
        try {
            // 所有请求都没有依赖关系，可以完全并行执行
            val userInfoDeferred = async { awaitResult(repository.getUserInfo()) }
            val configDeferred = async { awaitResult(repository.getAppConfig()) }
            val bannerDeferred = async { awaitResult(repository.getBanners()) }
            
            // 等待所有请求完成
            val userInfo = userInfoDeferred.await()
            val config = configDeferred.await()
            val banners = bannerDeferred.await()
            
            _data.value = CombinedData(userInfo, config, banners)
        } catch (e: Exception) {
            _errorMessage.value = e.message ?: "请求失败"
        }
    }
}
```

#### 选择建议

| 场景 | 推荐方式 |
|------|---------|
| 所有请求必须成功 | `awaitResult` + 统一 try-catch |
| 允许部分失败，需要知道具体失败 | `awaitResultSafe` |
| 允许部分失败，使用默认值 | `awaitResultOrDefault` |
| 需要精细控制每个请求 | 分别使用 try-catch |
| 请求之间没有依赖关系 | 并行执行（使用 `async`） |

### 未登录拦截器（全局错误码拦截）

框架支持针对未登录错误码的全局拦截，当检测到未登录错误码时，会自动调用拦截器回调，且**只拦截一次**（在配置的时间窗口内）。

#### 配置未登录拦截器

在 Application 中配置未登录拦截器：

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 初始化字符串资源工具类
        StringResourceHelper.init(this)
        
        // 配置未登录拦截器
        BaseRepository.setLoginInterceptor(
            interceptor = object : LoginInterceptor {
                override fun onUnauthorized(errorCode: Int, errorMessage: String) {
                    // 清除登录信息
                    UserManager.clearUserInfo()
                    // 清除 Token
                    TokenManager.clearToken()
                    // 跳转到登录页面
                    val intent = Intent(this@MyApplication, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            },
            unauthorizedCodes = setOf(401, 403), // 配置未登录错误码（可以配置多个）
            interceptWindowMillis = 5000 // 5 秒内只拦截一次，避免重复处理
        )
    }
}
```

#### 配置说明

- **interceptor**：未登录拦截器回调，当检测到未登录错误码时会调用此回调
- **unauthorizedCodes**：未登录错误码集合，默认为 `{401}`，可以根据项目实际情况配置多个错误码
- **interceptWindowMillis**：拦截时间窗口（毫秒），在此时间窗口内只拦截一次，默认 5 秒

#### 工作原理

1. 当网络请求返回错误时，框架会检查错误码是否在 `unauthorizedCodes` 集合中
2. 如果匹配，且距离上次拦截时间超过 `interceptWindowMillis`，则调用拦截器回调
3. 使用 `AtomicBoolean` 和 `AtomicLong` 确保线程安全，避免并发情况下的重复处理
4. 拦截器回调只会被调用一次（在时间窗口内），避免重复跳转登录页面

#### 清除拦截器

如果需要清除未登录拦截器（例如退出登录时）：

```kotlin
BaseRepository.clearLoginInterceptor()
```

#### 检查拦截器是否已配置

可以使用以下方法检查拦截器是否已配置：

```kotlin
if (BaseRepository.isLoginInterceptorConfigured()) {
    // 拦截器已配置
} else {
    // 拦截器未配置
}
```

#### 注意事项

1. **必须在 Application 中配置**：建议在 Application 的 `onCreate()` 中配置，确保全局生效
2. **配置顺序无关**：`setLoginInterceptor` 和 `initNetworkManager` 没有前后顺序要求，只要在发起第一个网络请求前配置好即可
3. **只拦截一次**：在配置的时间窗口内，相同的未登录错误码只会触发一次拦截器回调
4. **线程安全**：使用原子类确保并发安全，多个请求同时返回未登录错误码时，只会处理一次
5. **适用于所有请求**：拦截器会自动应用到所有使用 `requestFlow`、`requestFlowNoData` 和 `requestFlowRaw` 的请求

#### 确保配置完成的方案

**方案 1：在 Application 中统一配置（推荐）**

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 1. 初始化字符串资源（必须最先）
        StringResourceHelper.init(this)
        
        // 2. 配置未登录拦截器（顺序无关，但建议在 initNetworkManager 之前）
        BaseRepository.setLoginInterceptor(
            interceptor = object : LoginInterceptor {
                override fun onUnauthorized(errorCode: Int, errorMessage: String) {
                    // 处理未登录逻辑
                }
            }
        )
        
        // 3. 初始化网络管理器（顺序无关）
        initNetworkManager(
            baseUrl = "https://api.example.com/",
            enableLogging = BuildConfig.DEBUG
        )
    }
}
```

**方案 2：创建初始化工具类**

```kotlin
object NetworkInitializer {
    /**
     * 初始化网络框架（包含所有必要的配置）
     */
    fun init(context: Context) {
        // 1. 初始化字符串资源
        StringResourceHelper.init(context)
        
        // 2. 配置未登录拦截器
        BaseRepository.setLoginInterceptor(
            interceptor = object : LoginInterceptor {
                override fun onUnauthorized(errorCode: Int, errorMessage: String) {
                    // 处理未登录逻辑
                }
            }
        )
        
        // 3. 初始化网络管理器
        initNetworkManager(
            baseUrl = "https://api.example.com/",
            enableLogging = BuildConfig.DEBUG
        )
    }
    
    /**
     * 检查是否已初始化
     */
    fun isInitialized(): Boolean {
        return BaseRepository.isLoginInterceptorConfigured() 
            && NetworkManager.getInstance().isInitialized()
    }
}

// 在 Application 中使用
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NetworkInitializer.init(this)
    }
}
```

**方案 3：在 Repository 中添加检查（可选）**

如果担心配置遗漏，可以在 Repository 的构造函数中添加检查：

```kotlin
class UserRepository : BaseRepository() {
    init {
        // 如果使用了 NetworkInitializer 工具类
        if (!NetworkInitializer.isInitialized()) {
            throw IllegalStateException("网络框架未初始化，请在 Application 中调用 NetworkInitializer.init()")
        }
        
        // 或者，如果没有使用 NetworkInitializer，可以单独检查
        // if (!NetworkManager.getInstance().isInitialized()) {
        //     throw IllegalStateException("NetworkManager 未初始化，请在 Application 中调用 initNetworkManager()")
        // }
        
        // 注意：拦截器是可选的，不需要强制检查
    }
}
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

### 自定义响应结构

如果你的项目响应结构不是 `code`、`message`、`data`，可以实现 `IBaseResponse` 接口：

```kotlin
// 示例：项目使用 status、msg、result 作为字段名
data class MyResponse<T>(
    val status: Int = 0,
    val msg: String = "",
    val result: T? = null
) : IBaseResponse<T> {
    override fun isSuccess(): Boolean = status == 200
    
    override fun getDataOrThrow(): T = result ?: throw IllegalStateException("响应数据为空")
    
    override fun getDataOrDefault(defaultValue: T): T = result ?: defaultValue
    
    override fun getErrorMessage(): String = msg.ifEmpty { "未知错误" }
    
    override fun getResponseCode(): Int = status
    
    override fun getResponseMsg(): String = msg
    
    override fun getDataOrNull(): T? = result
}

// 在 API 接口中使用
interface ApiService {
    @GET("user/info")
    suspend fun getUserInfo(): MyResponse<UserInfo>
}

// Repository 中使用方式不变
class UserRepository : BaseRepository() {
    private val apiService = getApiService<ApiService>()
    
    fun getUserInfo(): Flow<NetworkResult<UserInfo>> {
        return requestFlow(
            apiCall = { apiService.getUserInfo() }
        )
    }
}
```

### 继承 BaseRepository 添加自定义方法

```kotlin
class CustomRepository : BaseRepository() {
    
    /**
     * 自定义网络请求方法
     */
    fun customRequest(): Flow<NetworkResult<CustomData>> {
        return requestFlow(
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

1. **必须初始化字符串资源**：使用前必须在 Application 中调用 `StringResourceHelper.init(this)`
2. 使用前必须先初始化 `NetworkManager`
3. API 接口方法必须使用 `suspend` 关键字
4. 返回类型应该是实现了 `IBaseResponse<T>` 的类型（如 `BaseResponse<T>`）或 `T`
5. 如果项目响应结构不同，实现 `IBaseResponse` 接口创建自己的响应类
6. 在 ViewModel 中使用 `collectNetworkResult` 来收集结果
7. 多环境配置时，需要先配置所有环境，再切换并初始化
8. 环境切换会重新初始化网络管理器，会清除所有 API 服务缓存
9. **国际化支持**：框架已支持中英文切换，系统会根据设备语言自动选择

## 环境配置最佳实践

1. **在 Application 中配置**：所有环境配置应该在 Application 的 `onCreate()` 中完成
2. **持久化环境选择**：传入 SharedPreferences 以持久化环境选择，下次启动时自动恢复
3. **根据 BuildConfig 自动选择**：根据构建类型自动选择合适的环境
4. **提供调试菜单**：在调试版本中提供环境切换功能，方便测试
5. **环境隔离**：确保不同环境的配置完全独立，避免相互影响

