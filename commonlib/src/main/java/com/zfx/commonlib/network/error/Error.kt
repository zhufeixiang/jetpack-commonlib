package com.zfx.commonlib.network.error

import com.zfx.commonlib.R
import com.zfx.commonlib.util.StringResourceHelper

/**
 * 网络错误类型枚举
 * 
 * 定义应用中可能出现的各种网络错误类型，每个错误类型包含：
 * - 错误码：用于程序内部区分错误类型
 * - 错误消息资源 ID：用于获取展示给用户的友好提示信息
 * 
 * @property code 错误码（私有属性）
 * @property messageResId 错误消息资源 ID（私有属性）
 */
enum class Error(private val code: Int, private val messageResId: Int) {

    /**
     * 未知错误
     * 当无法识别具体错误类型时使用
     */
    UNKNOWN(1000, R.string.error_unknown),
    
    /**
     * 数据解析错误
     * 当服务器返回的数据格式不正确或无法解析时使用
     */
    PARSE_ERROR(1001, R.string.error_parse),
    
    /**
     * 网络连接错误
     * 当网络连接失败、无法连接到服务器时使用
     */
    NETWORK_ERROR(1002, R.string.error_network),

    /**
     * SSL 证书错误
     * 当 HTTPS 请求的 SSL 证书验证失败时使用
     */
    SSL_ERROR(1004, R.string.error_ssl),

    /**
     * 网络请求超时
     * 当请求在指定时间内未完成时使用
     */
    TIMEOUT_ERROR(1006, R.string.error_timeout);

    /**
     * 获取错误消息
     * 从字符串资源中获取本地化的错误消息
     * 
     * @return 错误消息字符串
     */
    fun getValue(): String {
        return StringResourceHelper.getString(messageResId)
    }

    /**
     * 获取错误码
     * 
     * @return 错误码整数
     */
    fun getKey(): Int {
        return code
    }
    
    /**
     * 获取错误消息资源 ID
     * 
     * @return 错误消息资源 ID
     */
    fun getMessageResId(): Int {
        return messageResId
    }

}