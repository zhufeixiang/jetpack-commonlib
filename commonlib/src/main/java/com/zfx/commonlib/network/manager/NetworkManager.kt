package com.zfx.commonlib.network.manager

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.zfx.commonlib.network.config.NetworkConfig
import com.zfx.commonlib.network.interceptor.LoggingInterceptor
import com.zfx.commonlib.network.interceptor.DynamicBaseUrlInterceptor
import com.zfx.commonlib.network.adapter.HtmlEntityStringTypeAdapter
import com.zfx.commonlib.network.ssl.SSLUtils
import okhttp3.Cache
import okhttp3.ConnectionSpec
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * 网络管理器
 * 职责单一：负责创建和管理 Retrofit 和 API 服务实例
 * 使用单例模式确保全局唯一实例
 */
class NetworkManager private constructor() {
    
    companion object {
        @Volatile
        private var INSTANCE: NetworkManager? = null
        
        /**
         * 获取单例实例
         */
        fun getInstance(): NetworkManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NetworkManager().also { INSTANCE = it }
            }
        }
    }
    
    private var retrofit: Retrofit? = null
    private val apiServices = mutableMapOf<Class<*>, Any>()
    private var isInitialized = false
    private var config: NetworkConfig? = null
    private var gson: Gson? = null
    
    /**
     * 初始化网络管理器
     * @param config 网络配置
     * @param gson Gson 实例（可选，如果不提供则使用默认配置）
     */
    fun init(
        config: NetworkConfig,
        gson: Gson? = null
    ) {
        if (isInitialized && retrofit != null) {
            return // 已经初始化过了
        }
        
        this.config = config
        this.gson = gson ?: createDefaultGson()
        
        val okHttpClient = createOkHttpClient(config)
        retrofit = createRetrofit(config.baseUrl, okHttpClient, this.gson!!, config.useScalarsConverter)
        isInitialized = true
        
        // 清除旧的API服务缓存
        apiServices.clear()
    }
    
    /**
     * 获取默认缓存目录
     * 优先使用 Android 的 Context.getCacheDir()，如果不 available 则使用系统临时目录
     * 可使用cacheDirectory(File(context.cacheDir, "network-cache"))  // 自定义目录
     * @return 缓存目录 File 对象
     */
    private fun getDefaultCacheDirectory(): File {
        return try {
            // 尝试通过 StringResourceHelper 获取 Application Context
            val helperClass = Class.forName("com.zfx.commonlib.util.StringResourceHelper")
            val getContextMethod = helperClass.getDeclaredMethod("getContext")
            val context = getContextMethod.invoke(null) as? android.content.Context
            
            if (context != null) {
                // 使用 Android 的缓存目录（推荐）
                // 路径：/data/data/{package}/cache/okhttp-cache
                File(context.cacheDir, "okhttp-cache")
            } else {
                // 回退到系统临时目录（Context 未初始化或非 Android 环境）
                File(System.getProperty("java.io.tmpdir", "/tmp"), "okhttp-cache")
            }
        } catch (e: Exception) {
            // 如果获取失败（反射异常、类不存在等），回退到系统临时目录
            File(System.getProperty("java.io.tmpdir", "/tmp"), "okhttp-cache")
        }
    }
    
    /**
     * 创建默认的 Gson 实例
     * 自动解码 HTML 实体（如 &mdash; → ——）
     */
    private fun createDefaultGson(): Gson {
        return GsonBuilder()
            .setLenient()
            // 设置日期格式：用于解析 JSON 中的日期字符串
            // 当数据类中有 Date 类型的字段，且 JSON 中是字符串格式（如 "2025-12-25 10:30:00"）时使用
            // 示例：
            //   JSON: {"createTime": "2025-12-25 10:30:00"}
            //   数据类: data class User(val createTime: Date)
            //   Gson 会自动将字符串解析为 Date 对象
            // 
            // 注意：
            // - 如果数据类中没有 Date 类型字段，此设置不会生效，也不会影响其他类型的解析
            // - 如果 JSON 中是时间戳（Long），不需要此设置
            // - 如果数据类中使用的是 String 类型，不需要此设置
            // - 如果日期格式不同，可以自定义 TypeAdapter
            // - 此设置是"按需使用"的，即使配置了也不会影响没有 Date 字段的数据类
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            // 注册 HTML 实体解码适配器，自动将 &mdash; 等实体解码为对应字符
            .registerTypeAdapter(String::class.java, HtmlEntityStringTypeAdapter())
            .create()
    }
    
    /**
     * 创建 OkHttpClient
     */
    private fun createOkHttpClient(config: NetworkConfig): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(config.connectTimeout, TimeUnit.SECONDS)
            .readTimeout(config.readTimeout, TimeUnit.SECONDS)
            .writeTimeout(config.writeTimeout, TimeUnit.SECONDS)
        
        // 支持 HTTP（明文传输）- 用于内网服务器
        if (config.allowCleartextTraffic) {
            // 允许 HTTP 连接
            builder.connectionSpecs(listOf(
                ConnectionSpec.MODERN_TLS,
                ConnectionSpec.CLEARTEXT  // 允许 HTTP
            ))
        }
        
        // 信任所有 SSL 证书（包括自签名证书）
        // ⚠️ 警告：仅用于开发环境或内网，生产环境请勿使用
        if (config.trustAllCertificates) {
            val trustAllManager = SSLUtils.createTrustAllManager()
            val sslContext = SSLUtils.createTrustAllSSLContext()
            builder.sslSocketFactory(sslContext.socketFactory, trustAllManager)
            builder.hostnameVerifier(SSLUtils.createTrustAllHostnameVerifier())
        }
        
        // 使用内置的 DynamicBaseUrlInterceptor 实现动态 BaseUrl 切换
        // 先添加动态 BaseUrl 拦截器（需要在其他拦截器之前，这样后续拦截器才能看到替换后的 URL）
        if (config.enableDynamicBaseUrl) {
            builder.addInterceptor(DynamicBaseUrlInterceptor())
            // 设置全局 BaseUrl
            DynamicBaseUrlInterceptor.setGlobalBaseUrl(config.baseUrl)
        }
        
        // 添加日志拦截器（在 DynamicBaseUrlInterceptor 之后，这样能打印替换后的 URL）
        if (config.enableLogging) {
            val loggingInterceptor = LoggingInterceptor(
                tag = "OkHttp",
                level = config.logLevel
            )
            builder.addInterceptor(loggingInterceptor)
        }
        
        // 添加缓存
        if (config.enableCache) {
            val cacheDir = config.cacheDirectory ?: getDefaultCacheDirectory()
            
            // 确保缓存目录存在
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            
            val cache = Cache(cacheDir, config.cacheSize)
            builder.cache(cache)
        }
        
        // 添加 User-Agent
        if (config.userAgent.isNotEmpty()) {
            builder.addInterceptor { chain ->
                val originalRequest = chain.request()
                val newRequest = originalRequest.newBuilder()
                    .header("User-Agent", config.userAgent)
                    .build()
                chain.proceed(newRequest)
            }
        }
        
        // 添加自定义拦截器
        config.interceptors.forEach { builder.addInterceptor(it) }
        config.networkInterceptors.forEach { builder.addNetworkInterceptor(it) }
        
        return builder.build()
    }
    
    /**
     * 创建 Retrofit 实例
     * 
     * 注意：Retrofit 3.0.0+ 已经内置了对 Kotlin 协程的支持，无需额外添加 CoroutineCallAdapterFactory
     * 可以直接在接口方法中使用 suspend 函数
     * 
     * Converter 添加顺序很重要：
     * 1. ScalarsConverterFactory 必须在最前面（用于 String、Int 等简单类型）
     * 2. GsonConverterFactory 在后面（用于复杂对象）
     */
    private fun createRetrofit(
        baseUrl: String,
        okHttpClient: OkHttpClient,
        gson: Gson,
        useScalarsConverter: Boolean
    ): Retrofit {
        val retrofitBuilder = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
        
        // 如果启用 ScalarsConverter，需要先添加（Retrofit 会按顺序尝试转换器）
        if (useScalarsConverter) {
            retrofitBuilder.addConverterFactory(ScalarsConverterFactory.create())
        }
        
        // 添加 Gson 转换器
        retrofitBuilder.addConverterFactory(GsonConverterFactory.create(gson))
        
        return retrofitBuilder.build()
    }
    
    /**
     * 获取或创建API服务实例（使用泛型）
     */
    inline fun <reified T> getApiService(): T {
        return getApiService(T::class.java)
    }
    
    /**
     * 获取或创建API服务实例（使用Class参数）
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getApiService(serviceClass: Class<T>): T {
        // 检查是否已初始化
        if (retrofit == null || !isInitialized) {
            throw IllegalStateException("NetworkManager未初始化，请先调用init()方法")
        }
        
        // 从缓存中获取
        apiServices[serviceClass]?.let { 
            @Suppress("UNCHECKED_CAST")
            return it as T 
        }
        
        // 创建新实例
        val service = retrofit!!.create(serviceClass)
        apiServices[serviceClass] = service as Any
        return service
    }
    
    /**
     * 添加认证拦截器
     * 注意：这会重新初始化网络管理器
     */
    fun addAuthInterceptor(token: String) {
        val currentConfig = config ?: throw IllegalStateException("NetworkManager未初始化")
        
        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            chain.proceed(newRequest)
        }
        
        val newConfig = currentConfig.copy(
            interceptors = currentConfig.interceptors + authInterceptor
        )
        
        init(newConfig, gson)
    }
    
    /**
     * 清除API服务缓存
     */
    fun clearCache() {
        apiServices.clear()
    }
    
    /**
     * 检查是否已初始化
     */
    fun isInitialized(): Boolean = isInitialized
    
    /**
     * 获取当前配置
     */
    fun getConfig(): NetworkConfig? = config
    
    /**
     * 切换环境（重新初始化）
     * @param environmentConfig 新环境的配置
     */
    fun switchEnvironment(environmentConfig: com.zfx.commonlib.network.config.EnvironmentConfig) {
        val newConfig = environmentConfig.toNetworkConfig()
        // 重新初始化，清除旧的 API 服务缓存
        isInitialized = false
        init(newConfig, gson)
    }
    
    /**
     * 重置网络管理器（用于测试）
     */
    fun reset() {
        retrofit = null
        apiServices.clear()
        isInitialized = false
        config = null
        gson = null
        INSTANCE = null
    }
}

