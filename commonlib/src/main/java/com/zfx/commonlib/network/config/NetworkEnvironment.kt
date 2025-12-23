package com.zfx.commonlib.network.config

import com.zfx.commonlib.network.interceptor.LoggingInterceptor

/**
 * 网络环境枚举
 * 支持开发、预发布、生产三种环境
 */
enum class NetworkEnvironment {
    /**
     * 开发环境
     */
    DEVELOPMENT,
    
    /**
     * 预发布环境
     */
    PRE_RELEASE,
    
    /**
     * 生产环境
     */
    PRODUCTION
}

/**
 * 环境配置数据类
 * 每个环境对应一个配置
 */
data class EnvironmentConfig(
    /**
     * 环境名称
     */
    val environment: NetworkEnvironment,
    
    /**
     * 基础URL
     */
    val baseUrl: String,
    
    /**
     * 是否启用日志
     */
    val enableLogging: Boolean = true,
    
    /**
     * 日志级别
     */
    val logLevel: LoggingInterceptor.LogLevel = LoggingInterceptor.LogLevel.BODY,
    
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
     * 自定义拦截器列表
     */
    val interceptors: List<okhttp3.Interceptor> = emptyList(),
    
    /**
     * 网络拦截器列表
     */
    val networkInterceptors: List<okhttp3.Interceptor> = emptyList(),
    
    /**
     * 是否启用缓存
     */
    val enableCache: Boolean = false,
    
    /**
     * 缓存大小（字节）
     */
    val cacheSize: Long = 10 * 1024 * 1024L, // 10MB
    
    /**
     * User-Agent
     */
    val userAgent: String = "Android-App"
) {
    /**
     * 转换为 NetworkConfig
     */
    fun toNetworkConfig(): NetworkConfig {
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
            userAgent = userAgent
        )
    }
}

/**
 * 环境配置构建器
 */
class EnvironmentConfigBuilder(private val environment: NetworkEnvironment) {
    private var baseUrl: String = ""
    private var enableLogging: Boolean = true
    private var logLevel: LoggingInterceptor.LogLevel = LoggingInterceptor.LogLevel.BODY
    private var connectTimeout: Long = 30L
    private var readTimeout: Long = 30L
    private var writeTimeout: Long = 30L
    private val interceptors = mutableListOf<okhttp3.Interceptor>()
    private val networkInterceptors = mutableListOf<okhttp3.Interceptor>()
    private var enableCache: Boolean = false
    private var cacheSize: Long = 10 * 1024 * 1024L
    private var userAgent: String = "Android-App"
    
    fun baseUrl(url: String) = apply { this.baseUrl = url }
    fun enableLogging(enable: Boolean) = apply { this.enableLogging = enable }
    fun logLevel(level: LoggingInterceptor.LogLevel) = apply { this.logLevel = level }
    fun connectTimeout(timeout: Long) = apply { this.connectTimeout = timeout }
    fun readTimeout(timeout: Long) = apply { this.readTimeout = timeout }
    fun writeTimeout(timeout: Long) = apply { this.writeTimeout = timeout }
    fun addInterceptor(interceptor: okhttp3.Interceptor) = apply { this.interceptors.add(interceptor) }
    fun addNetworkInterceptor(interceptor: okhttp3.Interceptor) = apply { this.networkInterceptors.add(interceptor) }
    fun enableCache(enable: Boolean) = apply { this.enableCache = enable }
    fun cacheSize(size: Long) = apply { this.cacheSize = size }
    fun userAgent(agent: String) = apply { this.userAgent = agent }
    
    fun build(): EnvironmentConfig {
        require(baseUrl.isNotEmpty()) { "baseUrl 不能为空" }
        return EnvironmentConfig(
            environment = environment,
            baseUrl = baseUrl,
            enableLogging = enableLogging,
            logLevel = logLevel,
            connectTimeout = connectTimeout,
            readTimeout = readTimeout,
            writeTimeout = writeTimeout,
            interceptors = interceptors,
            networkInterceptors = networkInterceptors,
            enableCache = enableCache,
            cacheSize = cacheSize,
            userAgent = userAgent
        )
    }
}


