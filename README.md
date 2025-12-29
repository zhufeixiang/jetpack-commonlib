# Jetpack CommonLib

一个基于 Kotlin + Flow + Jetpack 的 Android 通用基础库，提供网络请求、MVVM/MVI 架构支持、Activity/Fragment 基类、工具类等功能，帮助快速搭建 Android 项目。

**核心特性**：
- 🌐 **网络请求框架**：基于 Retrofit + OkHttp + Flow，支持多环境切换、动态 BaseUrl、统一异常处理
- 🏗️ **架构支持**：MVVM 和 MVI 双架构支持，提供完整的基类和扩展函数
- 🎨 **UI 支持**：支持 ViewBinding、DataBinding、Jetpack Compose
- 🛠️ **工具库**：屏幕适配、权限管理、沉浸式状态栏、国际化等常用功能
- 📦 **零反射**：所有功能均不使用反射，性能更优

---

## ⚠️ 已知问题

### Kotlin 2.0.21 编译错误

**错误信息**：
```
org.jetbrains.kotlin.util.FileAnalysisException: While analysing ... 
java.lang.IllegalArgumentException: source must not be null
```

**原因**：
这是 Kotlin 官方的已知 bug（[KT-61418](https://youtrack.jetbrains.com/issue/KT-61418)）。在特定情况下，当检查不兼容的类表达式时，编译器会尝试报告诊断信息但找不到正确的源代码位置，导致抛出 `IllegalArgumentException: source must not be null`。

**状态**：✅ **已修复**

**修复版本**：
- Kotlin 2.0.22
- Kotlin 2.1.0-RC1 及更高版本

**解决方案**：
升级 Kotlin 版本到 2.0.22 或更高版本。

在项目的 `build.gradle` 或 `build.gradle.kts` 中更新：

```gradle
// build.gradle
buildscript {
    ext.kotlin_version = '2.0.22'  // 或更高版本
    dependencies {
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
    }
}
```

或

```kotlin
// build.gradle.kts
plugins {
    kotlin("android") version "2.0.22"  // 或更高版本
}
```

**相关链接**：
- [KT-61418 Issue](https://youtrack.jetbrains.com/issue/KT-61418)
- [Kotlin Release Notes](https://kotlinlang.org/docs/whatsnew20.html)

---

### AndroidX 与旧 Support Library 冲突

**错误信息**：
```
Duplicate class android.support.v4.app.INotificationSideChannel found in modules 
core-1.16.0.aar -> core-1.16.0-runtime (androidx.core:core:1.16.0) and 
support-compat-24.2.0.aar -> support-compat-24.2.0-runtime (com.android.support:support-compat:24.2.0)
```

**原因**：
- 库使用了 `androidx.core:core-ktx:1.16.0`（传递依赖 `androidx.core:core:1.16.0`）
- 库中的某些依赖（如 `AndroidAutoSize`）可能引入了旧的 `com.android.support:support-compat:24.2.0`
- AndroidX 和旧的 Support Library 不能同时存在

**解决方案**：

#### 方案 1：使用 Jetifier 自动转换（推荐，最简单）

在项目的 `gradle.properties` 文件中添加：

```properties
android.useAndroidX=true
android.enableJetifier=true
```

**说明**：
- `android.useAndroidX=true`：启用 AndroidX 支持
- `android.enableJetifier=true`：自动将旧的 Support Library 转换为 AndroidX（包括传递依赖）
- 这样 `AndroidAutoSize` 等库中的旧 Support Library 会被自动转换为 AndroidX
- **这是最简单的解决方案，推荐使用**

#### 方案 2：排除旧的 Support Library

如果不想使用 Jetifier，可以在项目的 `build.gradle` 或 `build.gradle.kts` 中添加全局排除规则：

**Gradle (Groovy)**：
```gradle
android {
    // ... 其他配置
}

// 全局排除旧的 Support Library
configurations.all {
    exclude group: 'com.android.support', module: 'support-compat'
    exclude group: 'com.android.support', module: 'support-v4'
    exclude group: 'com.android.support', module: 'support-annotations'
    exclude group: 'com.android.support', module: 'support-core-utils'
    exclude group: 'com.android.support', module: 'support-core-ui'
    exclude group: 'com.android.support', module: 'support-fragment'
}

dependencies {
    // ... 你的依赖
}
```

**Gradle Kotlin DSL (build.gradle.kts)**：
```kotlin
android {
    // ... 其他配置
}

// 全局排除旧的 Support Library
configurations.all {
    exclude(group = "com.android.support", module = "support-compat")
    exclude(group = "com.android.support", module = "support-v4")
    exclude(group = "com.android.support", module = "support-annotations")
    exclude(group = "com.android.support", module = "support-core-utils")
    exclude(group = "com.android.support", module = "support-core-ui")
    exclude(group = "com.android.support", module = "support-fragment")
}

dependencies {
    // 显式声明 core-ktx 版本，Gradle 会优先使用项目中的版本
    implementation(libs.androidx.core.ktx)  // 如果使用 Version Catalog
    implementation(libs.zfx.lib)
    // ... 其他依赖
}
```

#### 方案 3：统一版本（如果项目使用不同版本的 core-ktx）

如果项目需要使用 `androidx.core:core-ktx:1.10.1`，可以在项目中显式声明：

```gradle
dependencies {
    // 显式声明版本，Gradle 会优先使用项目中的版本
    implementation 'androidx.core:core-ktx:1.10.1'
    implementation 'com.github.zhufeixiang:jetpack-commonlib:Tag'
}
```

Gradle 会自动选择项目中声明的版本，避免版本冲突。

**注意**：
- `androidx.core:core-ktx` 已改为 `api` 依赖，允许使用库的项目控制版本
- 如果项目声明了特定版本，Gradle 会自动使用项目中的版本
- **推荐使用方案 1（Jetifier）**，最简单且无需手动排除

---

## 快速开始

### Step 1. 添加仓库配置

在项目的 `build.gradle` 或 `settings.gradle` 中添加：

```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

**注意**：
- 框架已移除 RetrofitUrlManager 依赖，使用内置的 DynamicBaseUrlInterceptor
- 无需添加 jcenter 仓库，所有功能均可正常使用

### Step 2. 添加依赖

```gradle
dependencies {
    implementation 'com.github.zhufeixiang:jetpack-commonlib:Tag'
}
```

### Step 3. 添加权限（必须）

**重要**：库模块不会声明权限，需要在使用该库的应用中声明。

在应用的 `AndroidManifest.xml` 中添加：

```xml
<!-- 网络请求权限（必须，如果使用网络功能） -->
<uses-permission android:name="android.permission.INTERNET" />

<!-- 网络状态检查权限（可选，如果需要检查网络状态） -->
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

**说明**：
- `INTERNET` 权限是**必须的**（如果使用网络功能）
- `ACCESS_NETWORK_STATE` 权限是**可选的**（仅当需要检查网络状态时）
- 这些是普通权限，不需要运行时请求

### Step 4. 配置 HTTP 支持（如果使用内网服务器）

如果使用内网服务器（HTTP），需要：

**1. 在 AndroidManifest.xml 中配置**

```xml
<application
    android:usesCleartextTraffic="true"
    ...>
</application>
```

**2. 在代码中启用**

```kotlin
initNetworkManager {
    baseUrl("http://192.168.1.100:8080/")
    allowCleartextTraffic(true)  // 允许 HTTP
}
```

详细说明请参考网络框架使用指南。

## 更新日志

#### 1. 移除 BaseViewModel 的 Loading 功能
- **变更**：`BaseViewModel` 不再包含 `loadingChange` 相关功能
- **影响**：`BaseVmActivity` 和 `BaseVmFragment` 中移除了 `showLoading()` 和 `dismissLoading()` 的抽象方法
- **原因**：Loading 状态应该由业务层自行管理，而不是在基础框架中强制实现
- **迁移**：如需全局 Loading，请在业务层自行封装

#### 2. BaseResponse 支持自定义响应结构
- **变更**：`BaseResponse` 改为接口 `IBaseResponse` + 默认实现
- **优势**：允许不同项目根据各自的响应结构实现接口
- **使用**：如果项目响应结构不同（如使用 `status`、`msg`、`result`），可以实现 `IBaseResponse` 接口
- **示例**：详见网络请求框架使用指南中的"自定义响应结构"章节

#### 3. 国际化支持（字符串资源化）
- **变更**：所有硬编码的中文字符串已提取到资源文件
- **支持语言**：中文简体、英文
- **初始化**：需要在 Application 的 `onCreate()` 中初始化 `StringResourceHelper`
  ```kotlin
  class MyApplication : Application() {
      override fun onCreate() {
          super.onCreate()
          StringResourceHelper.init(this)
      }
  }
  ```
- **资源文件**：
  - `values/strings.xml` - 中文简体
  - `values-en/strings.xml` - 英文

#### 4. 异常处理优化
- **修复**：修复了 `ExceptionHandle` 中的导入问题（`ParseException`、`ConnectTimeoutException`）
- **优化**：更新了所有异常处理相关的注释，使用规范的 KDoc 格式

#### 5. 代码注释优化
- **变更**：更新了 `AppException`、`Error`、`ExceptionHandle` 的注释
- **格式**：使用规范的 KDoc 格式，提供更清晰的文档说明

## 使用说明（无反射版本，Flow + MVI/MVVM）

0) 初始化字符串资源（Application onCreate，必须）
```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 初始化字符串资源工具类（支持国际化：中文简体、英文）
        StringResourceHelper.init(this)
    }
}
```

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
- 图片加载：建议在项目中使用 Coil 或其他图片加载库，不在基础库中提供

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
