package com.zfx.commonlib.network.error

import com.zfx.commonlib.R
import com.zfx.commonlib.util.StringResourceHelper
import com.google.gson.JsonParseException
import com.google.gson.stream.MalformedJsonException
import org.json.JSONException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.ParseException
import retrofit2.HttpException

/**
 * 异常处理工具类
 * 
 * 负责将各种系统异常转换为统一的 AppException 异常对象
 * 提供统一的异常处理逻辑，便于维护和扩展
 * 
 * 支持的异常类型：
 * - HttpException: HTTP 状态码错误（401、403、404、500 等）
 * - JsonParseException/JSONException/ParseException/MalformedJsonException: JSON 解析错误
 * - ConnectException: 网络连接失败
 * - SSLException: SSL 证书错误
 * - SocketTimeoutException: 请求超时
 * - UnknownHostException: 无法解析主机名
 * - AppException: 如果已经是 AppException，直接返回
 * - 其他异常: 统一归类为未知错误
 */
object ExceptionHandle {

    /**
     * 处理异常并转换为 AppException
     * 
     * 根据异常类型进行分类处理，返回对应的 AppException 对象
     * 如果传入的异常为 null，返回未知错误异常
     * 
     * @param e 需要处理的异常对象，可以为 null
     * @return AppException 统一的应用异常对象
     */
    fun handleException(e: Throwable?): AppException {
        if (e == null) {
            return AppException(Error.UNKNOWN, null)
        }
        
        return when (e) {
            is HttpException -> {
                // HTTP 错误，根据状态码返回不同的错误信息
                val errorMessage = when (e.code()) {
                    401 -> StringResourceHelper.getString(R.string.error_http_401)
                    403 -> StringResourceHelper.getString(R.string.error_http_403)
                    404 -> StringResourceHelper.getString(R.string.error_http_404)
                    500 -> StringResourceHelper.getString(R.string.error_http_500)
                    502, 503, 504 -> StringResourceHelper.getString(R.string.error_http_502_503_504)
                    else -> StringResourceHelper.getString(R.string.error_http_other, e.code())
                }
                AppException(Error.NETWORK_ERROR.getKey(), errorMessage, e.message, e)
            }
            
            is JsonParseException, 
            is JSONException, 
            is ParseException, 
            is MalformedJsonException -> {
                AppException(Error.PARSE_ERROR.getKey(), StringResourceHelper.getString(R.string.error_parse_data), e.message, e)
            }
            
            is ConnectException -> {
                AppException(Error.NETWORK_ERROR.getKey(), StringResourceHelper.getString(R.string.error_connect_failed), e.message, e)
            }
            
            is javax.net.ssl.SSLException -> {
                AppException(Error.SSL_ERROR.getKey(), StringResourceHelper.getString(R.string.error_ssl_certificate), e.message, e)
            }
            
            is SocketTimeoutException -> {
                AppException(Error.TIMEOUT_ERROR.getKey(), StringResourceHelper.getString(R.string.error_request_timeout), e.message, e)
            }
            
            is UnknownHostException -> {
                AppException(Error.NETWORK_ERROR.getKey(), StringResourceHelper.getString(R.string.error_cannot_connect_server), e.message, e)
            }
            
            is AppException -> {
                // 如果已经是 AppException，直接返回
                e
            }
            
            else -> {
                AppException(Error.UNKNOWN.getKey(), e.message ?: StringResourceHelper.getString(R.string.error_unknown_error), e.message, e)
            }
        }
    }
    
    /**
     * 获取友好的错误消息
     * 
     * 将异常转换为 AppException 后，返回其中的错误消息
     * 这是一个便捷方法，用于快速获取用户友好的错误提示
     * 
     * @param e 需要处理的异常对象，可以为 null
     * @return 友好的错误消息字符串
     */
    fun getFriendlyMessage(e: Throwable?): String {
        return handleException(e).errorMsg
    }
}