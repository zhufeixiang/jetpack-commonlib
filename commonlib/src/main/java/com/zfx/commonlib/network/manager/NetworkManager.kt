package com.zfx.commonlib.network.manager

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.zfx.commonlib.network.config.NetworkConfig
import me.jessyan.retrofiturlmanager.RetrofitUrlManager
import com.zfx.commonlib.network.interceptor.LoggingInterceptor
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
        retrofit = createRetrofit(config.baseUrl, okHttpClient, this.gson!!)
        isInitialized = true
        
        // 清除旧的API服务缓存
        apiServices.clear()
    }
    
    /**
     * 创建默认的 Gson 实例
     */
    private fun createDefaultGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
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
        
        // 添加日志拦截器
        if (config.enableLogging) {
            val loggingInterceptor = LoggingInterceptor(
                tag = "OkHttp",
                level = config.logLevel
            )
            builder.addInterceptor(loggingInterceptor)
        }
        
        // 添加缓存
        if (config.enableCache) {
            // 注意：这里需要 Context，如果启用缓存，应该在初始化时传入 Context
            // 暂时不实现，可以在外部通过拦截器添加
        }
        
        // 添加自定义拦截器
        config.interceptors.forEach { builder.addInterceptor(it) }
        config.networkInterceptors.forEach { builder.addNetworkInterceptor(it) }
        
        // 使用 RetrofitUrlManager 支持动态 BaseUrl
        return RetrofitUrlManager.getInstance().with(builder).build()
    }
    
    /**
     * 创建 Retrofit 实例
     * 
     * 注意：Retrofit 3.0.0+ 已经内置了对 Kotlin 协程的支持，无需额外添加 CoroutineCallAdapterFactory
     * 可以直接在接口方法中使用 suspend 函数
     */
    private fun createRetrofit(
        baseUrl: String,
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
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

