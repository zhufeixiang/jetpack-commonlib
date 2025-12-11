package com.zfx.commonlib.network.config

import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

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
    val logLevel: HttpLoggingInterceptor.Level = HttpLoggingInterceptor.Level.BODY,
    
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
     * User-Agent
     */
    val userAgent: String = "Android-App"
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
                logLevel = HttpLoggingInterceptor.Level.BODY
            )
        }
        
        /**
         * 生产环境配置
         */
        fun production(baseUrl: String): NetworkConfig {
            return NetworkConfig(
                baseUrl = baseUrl,
                enableLogging = false,
                logLevel = HttpLoggingInterceptor.Level.NONE
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
    private var logLevel: HttpLoggingInterceptor.Level = HttpLoggingInterceptor.Level.BODY
    private val interceptors = mutableListOf<Interceptor>()
    private val networkInterceptors = mutableListOf<Interceptor>()
    private var enableCache: Boolean = false
    private var cacheSize: Long = 10 * 1024 * 1024L
    private var userAgent: String = "Android-App"
    
    fun baseUrl(url: String) = apply { this.baseUrl = url }
    fun connectTimeout(timeout: Long) = apply { this.connectTimeout = timeout }
    fun readTimeout(timeout: Long) = apply { this.readTimeout = timeout }
    fun writeTimeout(timeout: Long) = apply { this.writeTimeout = timeout }
    fun enableLogging(enable: Boolean) = apply { this.enableLogging = enable }
    fun logLevel(level: HttpLoggingInterceptor.Level) = apply { this.logLevel = level }
    fun addInterceptor(interceptor: Interceptor) = apply { this.interceptors.add(interceptor) }
    fun addNetworkInterceptor(interceptor: Interceptor) = apply { this.networkInterceptors.add(interceptor) }
    fun enableCache(enable: Boolean) = apply { this.enableCache = enable }
    fun cacheSize(size: Long) = apply { this.cacheSize = size }
    fun userAgent(agent: String) = apply { this.userAgent = agent }
    
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
            userAgent = userAgent
        )
    }
}

