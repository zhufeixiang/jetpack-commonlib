package com.zfx.commonlib.network.ssl

import javax.net.ssl.*
import java.security.cert.X509Certificate

/**
 * SSL 工具类
 * 用于处理自签名证书或跳过证书验证（仅用于开发环境或内网）
 * 
 * ⚠️ 警告：信任所有证书在生产环境中是不安全的，应该只在开发环境或内网使用
 */
object SSLUtils {
    
    /**
     * 创建信任所有证书的 TrustManager
     * ⚠️ 警告：这会信任所有证书，包括自签名证书，存在安全风险
     */
    fun createTrustAllManager(): X509TrustManager {
        return object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                // 信任所有客户端证书
            }
            
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                // 信任所有服务器证书
            }
            
            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return emptyArray()
            }
        }
    }
    
    /**
     * 创建信任所有主机名的 HostnameVerifier
     * ⚠️ 警告：这会信任所有主机名，存在安全风险
     */
    fun createTrustAllHostnameVerifier(): HostnameVerifier {
        return HostnameVerifier { _, _ -> true }
    }
    
    /**
     * 创建信任所有证书的 SSLContext
     * ⚠️ 警告：这会信任所有证书，包括自签名证书，存在安全风险
     */
    fun createTrustAllSSLContext(): SSLContext {
        return try {
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, arrayOf(createTrustAllManager()), java.security.SecureRandom())
            sslContext
        } catch (e: Exception) {
            throw RuntimeException("创建 SSLContext 失败", e)
        }
    }
}

