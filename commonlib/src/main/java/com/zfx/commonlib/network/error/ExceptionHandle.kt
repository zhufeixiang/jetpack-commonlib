package com.zfx.commonlib.network.error

import android.net.ParseException
import com.google.gson.JsonParseException
import com.google.gson.stream.MalformedJsonException
import org.apache.http.conn.ConnectTimeoutException
import org.json.JSONException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import retrofit2.HttpException

/**
 * 作者　: hegaojian
 * 时间　: 2019/12/17
 * 描述　: 根据异常返回相关的错误信息工具类
 * 
 * 职责单一：只负责异常处理和错误信息转换
 * 可维护：统一的异常处理逻辑，易于维护和扩展
 */
object ExceptionHandle {

    /**
     * 处理异常并返回 AppException
     * 
     * @param e 异常对象
     * @return AppException 应用异常对象
     */
    fun handleException(e: Throwable?): AppException {
        if (e == null) {
            return AppException(Error.UNKNOWN, "未知错误")
        }
        
        return when (e) {
            is HttpException -> {
                // HTTP 错误，根据状态码返回不同的错误信息
                val errorMessage = when (e.code()) {
                    401 -> "未授权，请重新登录"
                    403 -> "访问被拒绝"
                    404 -> "请求的资源不存在"
                    500 -> "服务器内部错误"
                    502, 503, 504 -> "服务器暂时不可用，请稍后重试"
                    else -> "网络请求失败: ${e.code()}"
                }
                AppException(Error.NETWORK_ERROR, errorMessage, e.message, e)
            }
            
            is JsonParseException, 
            is JSONException, 
            is ParseException, 
            is MalformedJsonException -> {
                AppException(Error.PARSE_ERROR, "数据解析错误", e.message, e)
            }
            
            is ConnectException -> {
                AppException(Error.NETWORK_ERROR, "网络连接失败，请检查网络设置", e.message, e)
            }
            
            is javax.net.ssl.SSLException -> {
                AppException(Error.SSL_ERROR, "SSL证书错误", e.message, e)
            }
            
            is ConnectTimeoutException,
            is SocketTimeoutException -> {
                AppException(Error.TIMEOUT_ERROR, "请求超时，请检查网络连接", e.message, e)
            }
            
            is UnknownHostException -> {
                AppException(Error.NETWORK_ERROR, "无法连接到服务器，请检查网络", e.message, e)
            }
            
            is AppException -> {
                // 如果已经是 AppException，直接返回
                e
            }
            
            else -> {
                AppException(Error.UNKNOWN, e.message ?: "未知错误", e.message, e)
            }
        }
    }
    
    /**
     * 获取友好的错误消息
     * 
     * @param e 异常对象
     * @return 友好的错误消息
     */
    fun getFriendlyMessage(e: Throwable?): String {
        return handleException(e).errorMsg
    }
}