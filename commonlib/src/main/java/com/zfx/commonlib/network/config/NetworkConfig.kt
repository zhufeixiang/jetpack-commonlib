package com.zfx.commonlib.network.config

import com.zfx.commonlib.network.interceptor.DynamicBaseUrlInterceptor
import com.zfx.commonlib.network.interceptor.LoggingInterceptor
import okhttp3.Interceptor

/**
 * 网络配置类
 * 职责单一：只负责网络配置相关参数
 */
data class NetworkConfig(
    /**
     * 基础URL
     */
    val baseUrl: String,
    
    /**
     * 连接超时时间（秒）
     */
    val connectTimeout: Long = 30L,
    
    /**
     * 读取超时时间（秒）
     */
    val readTimeout: Long = 30L,
    
    /**
     * 写入超时时间（秒）
     */
    val writeTimeout: Long = 30L,
    
    /**
     * 是否启用日志
     */
    val enableLogging: Boolean = true,
    
    /**
     * 日志级别
     */
    val logLevel: LoggingInterceptor.LogLevel = LoggingInterceptor.LogLevel.BODY,
    
    /**
     * 自定义拦截器列表
     */
    val interceptors: List<Interceptor> = emptyList(),
    
    /**
     * 网络拦截器列表
     */
    val networkInterceptors: List<Interceptor> = emptyList(),
    
    /**
     * 是否启用缓存
     */
    val enableCache: Boolean = false,
    
    /**
     * 缓存大小（字节）
     */
    val cacheSize: Long = 10 * 1024 * 1024L, // 10MB
    
    /**
     * User-Agent（用户代理）
     * 用于标识客户端应用，服务器可以根据此信息识别请求来源
     * 
     * **作用**：
     * - 标识应用类型和版本
     * - 服务器可以根据 User-Agent 返回不同的内容（如移动端/PC端）
     * - 用于统计和分析
     * - 某些 API 可能要求特定的 User-Agent
     * 
     * **默认值**：`"Android-App"`（简单标识）
     * 
     * **自定义示例**：
     * ```kotlin
     * initNetworkManager {
     *     baseUrl("https://api.example.com/")
     *     userAgent("MyApp/1.0.0 (Android 12; SM-G991B)")  // 自定义 User-Agent
     * }
     * ```
     * 
     * **常见格式**：
     * - 简单：`"MyApp/1.0.0"`
     * - 详细：`"MyApp/1.0.0 (Android 12; Device Model)"`
     * - 标准：`"MyApp/1.0.0 (Linux; Android 12; SM-G991B) AppleWebKit/537.36"`
     * 
     * **注意**：
     * - 如果设置为空字符串，不会添加 User-Agent 头（OkHttp 会使用默认值）
     * - 某些服务器可能要求特定的 User-Agent 格式
     */
    val userAgent: String = "Android-App",
    
    /**
     * 是否允许 HTTP（明文传输）
     * 默认：false（Android 9+ 默认禁止 HTTP）
     * 如果使用内网服务器（HTTP），需要设置为 true
     * 注意：同时需要在 AndroidManifest.xml 中配置网络安全策略
     */
    val allowCleartextTraffic: Boolean = false,
    
    /**
     * 缓存目录（用于网络缓存）
     * 如果启用缓存但未提供，将使用默认缓存目录
     */
    val cacheDirectory: java.io.File? = null,
    
    /**
     * 是否添加 ScalarsConverterFactory（用于返回 String、Int 等简单类型）
     * 默认：false
     * 如果接口返回的是纯文本（String）或简单类型，需要设置为 true
     */
    val useScalarsConverter: Boolean = false,
    
    /**
     * 是否信任所有 SSL 证书（包括自签名证书）
     * 默认：false
     * ⚠️ 警告：设置为 true 会跳过所有证书验证，存在安全风险
     * 仅用于开发环境或内网自签名证书场景
     * 
     * 如果设置为 true，会：
     * 1. 信任所有服务器证书（包括自签名证书）
     * 2. 信任所有主机名
     * 
     * 使用场景：
     * - 开发环境使用自签名证书
     * - 内网服务器使用自签名证书
     * - 测试环境跳过证书验证
     */
    val trustAllCertificates: Boolean = false,
    
    /**
     * 是否启用动态 BaseUrl 功能
     * 默认：false（禁用）
     * 
     * 如果设置为 false：
     * - 不会添加 DynamicBaseUrlInterceptor 拦截器
     * - 可以避免不必要的拦截器开销（轻微性能提升）
     * - 无法使用动态 BaseUrl 切换功能
     * 
     * 如果设置为 true：
     * - 会添加 DynamicBaseUrlInterceptor 拦截器
     * - 支持多域名动态切换（使用 @Headers("Domain-Name: {domainName}")）
     * - 可以使用 putDomain() 配置多个域名
     * 
     * 性能说明：
     * - 即使启用，如果不需要动态 BaseUrl，拦截器会快速返回（开销很小）
     * - 如果确定不使用动态 BaseUrl，保持默认 false 可以完全避免拦截器开销
     * 
     * 使用场景：
     * - 单域名项目：保持默认 false（推荐）
     * - 多域名项目：设置为 true
     */
    val enableDynamicBaseUrl: Boolean = false
) {
    companion object {
        /**
         * 默认配置
         */
        fun default(baseUrl: String): NetworkConfig {
            return NetworkConfig(baseUrl = baseUrl)
        }
        
        /**
         * 开发环境配置
         */
        fun development(baseUrl: String): NetworkConfig {
            return NetworkConfig(
                baseUrl = baseUrl,
                enableLogging = true,
                logLevel = LoggingInterceptor.LogLevel.BODY
            )
        }
        
        /**
         * 生产环境配置
         */
        fun production(baseUrl: String): NetworkConfig {
            return NetworkConfig(
                baseUrl = baseUrl,
                enableLogging = false,
                logLevel = LoggingInterceptor.LogLevel.NONE
            )
        }
    }
}

/**
 * 网络配置构建器
 * 提供链式调用的方式构建配置
 */
class NetworkConfigBuilder {
    private var baseUrl: String = ""
    private var connectTimeout: Long = 30L
    private var readTimeout: Long = 30L
    private var writeTimeout: Long = 30L
    private var enableLogging: Boolean = true
    private var logLevel: LoggingInterceptor.LogLevel = LoggingInterceptor.LogLevel.BODY
    private val interceptors = mutableListOf<Interceptor>()
    private val networkInterceptors = mutableListOf<Interceptor>()
    private var enableCache: Boolean = false
    private var cacheSize: Long = 10 * 1024 * 1024L
    private var userAgent: String = "Android-App"
    private var allowCleartextTraffic: Boolean = false
    private var cacheDirectory: java.io.File? = null
    private var useScalarsConverter: Boolean = false
    private var trustAllCertificates: Boolean = false
    private var enableDynamicBaseUrl: Boolean = false
    
    fun baseUrl(url: String) = apply { this.baseUrl = url }
    fun connectTimeout(timeout: Long) = apply { this.connectTimeout = timeout }
    fun readTimeout(timeout: Long) = apply { this.readTimeout = timeout }
    fun writeTimeout(timeout: Long) = apply { this.writeTimeout = timeout }
    fun enableLogging(enable: Boolean) = apply { this.enableLogging = enable }
    fun logLevel(level: LoggingInterceptor.LogLevel) = apply { this.logLevel = level }
    fun addInterceptor(interceptor: Interceptor) = apply { this.interceptors.add(interceptor) }
    fun addNetworkInterceptor(interceptor: Interceptor) = apply { this.networkInterceptors.add(interceptor) }
    fun enableCache(enable: Boolean) = apply { this.enableCache = enable }
    fun cacheSize(size: Long) = apply { this.cacheSize = size }
    fun userAgent(agent: String) = apply { this.userAgent = agent }
    fun allowCleartextTraffic(allow: Boolean) = apply { this.allowCleartextTraffic = allow }
    fun cacheDirectory(directory: java.io.File) = apply { this.cacheDirectory = directory }
    fun useScalarsConverter(use: Boolean) = apply { this.useScalarsConverter = use }
    fun trustAllCertificates(trust: Boolean) = apply { this.trustAllCertificates = trust }
    fun enableDynamicBaseUrl(enable: Boolean) = apply { this.enableDynamicBaseUrl = enable }
    
    /**
     * 配置动态域名（便捷方法，在构建器 lambda 中直接调用）
     * 等同于调用 putDomain(domainName, domainUrl)
     * 
     * 使用示例：
     * ```kotlin
     * initNetworkManager {
     *     baseUrl("https://api.example.com/")
     *     putDomain("news", "https://news.example.com/")  // 可以直接调用，无需导入
     * }
     * ```
     */
    fun putDomain(domainName: String, domainUrl: String) = apply {
        DynamicBaseUrlInterceptor.putDomain(domainName, domainUrl)
    }
    
    fun build(): NetworkConfig {
        require(baseUrl.isNotEmpty()) { "baseUrl 不能为空" }
        return NetworkConfig(
            baseUrl = baseUrl,
            connectTimeout = connectTimeout,
            readTimeout = readTimeout,
            writeTimeout = writeTimeout,
            enableLogging = enableLogging,
            logLevel = logLevel,
            interceptors = interceptors,
            networkInterceptors = networkInterceptors,
            enableCache = enableCache,
            cacheSize = cacheSize,
            userAgent = userAgent,
            allowCleartextTraffic = allowCleartextTraffic,
            cacheDirectory = cacheDirectory,
            useScalarsConverter = useScalarsConverter,
            trustAllCertificates = trustAllCertificates,
            enableDynamicBaseUrl = enableDynamicBaseUrl
        )
    }
}


