package com.zfx.commonlib.network.error

import com.zfx.commonlib.R
import com.zfx.commonlib.util.StringResourceHelper

/**
 * 应用自定义异常类
 * 
 * 用于封装网络请求中的各种错误信息，提供统一的异常处理接口
 * 包含错误码、错误消息、错误日志和原始异常对象
 * 
 * @property errorMsg 错误消息，展示给用户的友好提示信息
 * @property errCode 错误码，用于区分不同类型的错误
 * @property errorLog 错误日志，用于调试和问题排查
 * @property throwable 原始异常对象，保留完整的异常堆栈信息
 */
class AppException : Exception {

    /** 错误消息，展示给用户的友好提示信息 */
    var errorMsg: String
    
    /** 错误码，用于区分不同类型的错误 */
    var errCode: Int = 0
    
    /** 错误日志，用于调试和问题排查 */
    var errorLog: String?
    
    /** 原始异常对象，保留完整的异常堆栈信息 */
    var throwable: Throwable? = null

    /**
     * 构造函数
     * 
     * @param errCode 错误码
     * @param error 错误消息，如果为空则使用默认消息
     * @param errorLog 错误日志，如果为空则使用错误消息
     * @param throwable 原始异常对象
     */
    constructor(errCode: Int, error: String?, errorLog: String? = "", throwable: Throwable? = null) : super(error) {
        this.errorMsg = error ?: StringResourceHelper.getString(R.string.exception_default_message)
        this.errCode = errCode
        this.errorLog = errorLog ?: this.errorMsg
        this.throwable = throwable
    }

    /**
     * 构造函数（使用 Error 枚举）
     * 
     * @param error 错误枚举类型
     * @param e 原始异常对象
     */
    constructor(error: Error, e: Throwable?) {
        errCode = error.getKey()
        errorMsg = error.getValue()
        errorLog = e?.message
        throwable = e
    }
}