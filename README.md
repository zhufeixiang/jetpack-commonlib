Step 1. Add the JitPack repository to your build file
 Add it in your root build.gradle at the end of repositories:

	dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
			mavenCentral()
			maven { url 'https://jitpack.io' }
		}
	}
Step 2. Add the dependency

	dependencies {
	        implementation 'com.github.zhufeixiang:jetpack-commonlib:Tag'
	}

## 使用说明（无反射版本，Flow + MVI/MVVM）

1) 网络初始化（Application onCreate）
```kotlin
BaseResponse.configureSuccess(
    codes = setOf(200),       // 成功码
    allowNullData = false,    // 是否允许 data 为空
    checker = null            // 可选自定义判定
)

// 多环境配置 + 切换 + 初始化
initNetworkEnvironmentManager(getSharedPreferences("env", MODE_PRIVATE))

configureAllEnvironments(
    development = {
        baseUrl("https://dev-api.example.com/")
        enableLogging(true)
    },
    preRelease = {
        baseUrl("https://pre-api.example.com/")
        enableLogging(true)
    },
    production = {
        baseUrl("https://api.example.com/")
        enableLogging(false)
    }
)

switchNetworkEnvironment(
    if (BuildConfig.DEBUG) NetworkEnvironment.DEVELOPMENT
    else NetworkEnvironment.PRODUCTION
)

initNetworkManagerWithCurrentEnvironment()
```

2) Repository（统一使用 Flow）
```kotlin
class UserRepository : BaseRepository() {
    private val api = getApiService<ApiService>()
    fun userInfo() = requestFlow { api.getUserInfo() }
}
```

3) ViewModel 收集（MVVM 或 MVI 均可）
```kotlin
class UserViewModel : BaseViewModel() {
    private val repo = UserRepository()
    fun load() = collectResult(
        flow = repo.userInfo(),
        onLoading = { show("加载中") },
        onSuccess = { data -> /* update UI */ },
        onError = { err -> /* show error */ }
    )
}
```

4) UI 基类选择
- Compose：`BaseComposeMviActivity` / `BaseComposeMviFragment`
- 传统 View + MVI：`MviActivity` / `MviFragment`
- 传统 View + MVVM：`BaseVmActivity` / `BaseVmFragment`

5) ViewBinding / DataBinding（无反射）
- Activity/Fragment 需实现 `initBinding(inflater, container, attachToParent)` 并返回生成的 Binding（示例见下）。
- `ViewBindUtil` 已移除，不再使用反射创建 Binding。

6) 日志/工具
- 网络结果：`NetworkResult` + `requestFlow`/`requestFlowRaw`
- 异常处理：`network/error/*`
- Glide：`GlideUtil`（圆角/圆形/GIF/视频首帧）

### 单接口多域名（RetrofitUrlManager）

```kotlin
// 配置域名标识
putDomain("news", "https://news.example.com/")
putDomain("upload", "https://upload.example.com/")

// 在接口方法上标注域名
interface ApiService {
    @Headers("Domain-Name: news")
    @GET("list")
    suspend fun getNews(): BaseResponse<List<News>>

    @Headers("Domain-Name: upload")
    @POST("file")
    suspend fun upload(@Body body: RequestBody): BaseResponse<Unit>
}

// 示例：Repository 中使用
class NewsRepository : BaseRepository() {
    private val api = getApiService<ApiService>()
    fun fetchNews() = requestFlow { api.getNews() }
    fun uploadFile(body: RequestBody) = requestFlow { api.upload(body) }
}

// 切换/清理
setGlobalBaseUrl("https://api.example.com/")   // 全局 BaseUrl
removeDomain("news")                           // 移除单个域名
clearAllDomains()                              // 清空所有动态域名

> 调用顺序：先配置（putDomain / setGlobalBaseUrl / removeDomain / clearAllDomains），再发起请求。配置只对之后创建的请求生效，已创建的请求不会受影响。

### 示例：按接口切换 BaseUrl（完整调用顺序）

```kotlin
// 1) Application 启动或业务入口处：配置域名映射
putDomain("news", "https://news.example.com/")
putDomain("upload", "https://upload.example.com/")

// 2) 定义接口，按需标注 Domain-Name
interface ApiService {
    @Headers("Domain-Name: news")
    @GET("list")
    suspend fun getNews(): BaseResponse<List<News>>

    @Headers("Domain-Name: upload")
    @POST("file")
    suspend fun upload(@Body body: RequestBody): BaseResponse<Unit>
}

// 3) Repository 发起请求（仅使用 requestFlow 即可）
class NewsRepository : BaseRepository() {
    private val api = getApiService<ApiService>()
    fun fetchNews() = requestFlow { api.getNews() }          // 用 news 域名
    fun uploadFile(body: RequestBody) = requestFlow { api.upload(body) } // 用 upload 域名
}

// 4) 若需切换/清理域名
setGlobalBaseUrl("https://api.example.com/")   // 全局 BaseUrl
removeDomain("news")                           // 移除单个域名映射
clearAllDomains()                              // 清空所有动态域名

// 注意：先配置（put/set/remove/clear），后发请求；配置对之后创建的请求生效。
```
```

### ViewBinding 初始化示例（无反射）

**Activity（BaseVmVbActivity / BaseVbActivity）**
```kotlin
class DemoActivity : BaseVmVbActivity<DemoViewModel, ActivityDemoBinding>() {
    override fun initBinding(layoutInflater: LayoutInflater): ActivityDemoBinding =
        ActivityDemoBinding.inflate(layoutInflater)
    override fun layoutId(): Int = 0 // 使用 VB 时可不再依赖 layoutId
    // initView / createObserver / showLoading / dismissLoading ...
}
```

**Fragment（BaseVmVbFragment / BaseVbFragment）**
```kotlin
class DemoFragment : BaseVmVbFragment<DemoViewModel, FragmentDemoBinding>() {
    override fun initBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        attachToParent: Boolean
    ): FragmentDemoBinding = FragmentDemoBinding.inflate(inflater, container, attachToParent)

    override fun layoutId(): Int = 0 // 使用 VB 时可不再依赖 layoutId
    // initView / lazyLoadData / createObserver / showLoading / dismissLoading ...
}
```

**ViewPager2 懒加载（BaseViewPager2LazyVMFragment）**
```kotlin
class PagerItemFragment : BaseViewPager2LazyVMFragment<FragmentPagerItemBinding>(0) {
    override fun initBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        attachToParent: Boolean
    ): FragmentPagerItemBinding = FragmentPagerItemBinding.inflate(inflater, container, attachToParent)

    override fun initView() { /* ... */ }
    override fun startObserve() { /* ... */ }
    override fun initData() { /* ... */ }
}
```
