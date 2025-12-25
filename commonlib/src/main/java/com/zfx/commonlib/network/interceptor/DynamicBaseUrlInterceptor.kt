package com.zfx.commonlib.network.interceptor

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.util.concurrent.ConcurrentHashMap

/**
 * 动态 BaseUrl 拦截器
 * 不依赖 RetrofitUrlManager，通过 OkHttp Interceptor 实现动态切换 BaseUrl
 * 
 * 使用方式：
 * 1. 在接口方法上添加 @Headers("Domain-Name: {domainName}")
 * 2. 调用 putDomain(domainName, domainUrl) 配置域名映射
 * 3. 调用 setGlobalBaseUrl(baseUrl) 设置全局 BaseUrl
 * 
 * 示例：
 * ```kotlin
 * // 配置域名
 * DynamicBaseUrlInterceptor.putDomain("news", "https://news.example.com/")
 * DynamicBaseUrlInterceptor.putDomain("upload", "https://upload.example.com/")
 * 
 * // 在接口中使用
 * interface ApiService {
 *     @Headers("Domain-Name: news")
 *     @GET("list")
 *     suspend fun getNews(): BaseResponse<List<News>>
 * }
 * ```
 */
class DynamicBaseUrlInterceptor : Interceptor {
    
    companion object {
        // 域名映射表（线程安全）
        private val domainMap = ConcurrentHashMap<String, String>()
        
        // 全局 BaseUrl（当没有指定 Domain-Name 时使用）
        @Volatile
        private var globalBaseUrl: String? = null
        
        // Header 名称，用于指定域名
        private const val DOMAIN_NAME_HEADER = "Domain-Name"
        
        /**
         * 设置全局 BaseUrl
         * @param baseUrl 全局 BaseUrl（必须以 / 结尾）
         */
        fun setGlobalBaseUrl(baseUrl: String) {
            globalBaseUrl = baseUrl.ensureTrailingSlash()
        }
        
        /**
         * 添加域名映射
         * @param domainName 域名标识（如 "news"、"upload"）
         * @param domainUrl 域名 URL（必须以 / 结尾）
         */
        fun putDomain(domainName: String, domainUrl: String) {
            domainMap[domainName] = domainUrl.ensureTrailingSlash()
        }
        
        /**
         * 移除域名映射
         * @param domainName 域名标识
         */
        fun removeDomain(domainName: String) {
            domainMap.remove(domainName)
        }
        
        /**
         * 清除所有域名映射
         */
        fun clearAllDomains() {
            domainMap.clear()
        }
        
        /**
         * 获取域名 URL
         * @param domainName 域名标识
         * @return 域名 URL，如果不存在返回 null
         */
        fun getDomain(domainName: String): String? {
            return domainMap[domainName]
        }
        
        /**
         * 获取全局 BaseUrl
         */
        fun getGlobalBaseUrl(): String? {
            return globalBaseUrl
        }
        
        /**
         * 确保 URL 以 / 结尾
         */
        private fun String.ensureTrailingSlash(): String {
            return if (endsWith("/")) this else "$this/"
        }
    }
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val domainName = originalRequest.header(DOMAIN_NAME_HEADER)
        
        // 如果指定了域名，使用指定的域名
        val newBaseUrl = if (!domainName.isNullOrBlank()) {
            domainMap[domainName] ?: throw IllegalArgumentException(
                "域名 '$domainName' 未配置，请先调用 putDomain('$domainName', url) 配置"
            )
        } else {
            // 否则使用全局 BaseUrl
            globalBaseUrl
        }
        
        // 如果配置了新的 BaseUrl，则替换
        if (newBaseUrl != null) {
            val newUrl = replaceBaseUrl(originalRequest.url, newBaseUrl)
            
            // 创建新请求（移除 Domain-Name header，因为 OkHttp 不需要它）
            val newRequest = originalRequest.newBuilder()
                .url(newUrl)
                .removeHeader(DOMAIN_NAME_HEADER)
                .build()
            
            return chain.proceed(newRequest)
        }
        
        // 如果没有配置，使用原始请求
        return chain.proceed(originalRequest)
    }
    
    /**
     * 替换 BaseUrl
     * @param originalUrl 原始 URL
     * @param newBaseUrl 新的 BaseUrl
     * @return 新的 HttpUrl
     */
    private fun replaceBaseUrl(originalUrl: HttpUrl, newBaseUrl: String): HttpUrl {
        // 使用扩展函数替代废弃的 parse() 方法
        val baseUrl = newBaseUrl.toHttpUrlOrNull()
            ?: throw IllegalArgumentException("无效的 BaseUrl: $newBaseUrl")
        
        // 构建新的 URL（使用属性替代废弃的方法）
        return originalUrl.newBuilder()
            .scheme(baseUrl.scheme)
            .host(baseUrl.host)
            .port(baseUrl.port)
            .build()
    }
}

