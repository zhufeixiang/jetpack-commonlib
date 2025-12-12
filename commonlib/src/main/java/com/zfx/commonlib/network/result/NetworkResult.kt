package com.zfx.commonlib.network.result

/**
 * 网络请求结果密封类
 * 使用 Flow 时返回此类型，支持 Loading、Success、Error 三种状态
 * 
 * @param T 响应数据类型
 */
sealed class NetworkResult<out T> {
    /**
     * 加载中状态
     * @param message 加载提示信息
     */
    data class Loading(val message: String = "请求中...") : NetworkResult<Nothing>()
    
    /**
     * 成功状态
     * @param data 响应数据
     */
    data class Success<out T>(val data: T) : NetworkResult<T>()
    
    /**
     * 错误状态
     * @param error 错误异常
     * @param code 错误码
     * @param message 错误消息
     */
    data class Error(
        val error: Throwable? = null,
        val code: Int = -1,
        val message: String = "请求失败"
    ) : NetworkResult<Nothing>()
    
    /**
     * 判断是否为成功状态
     */
    fun isSuccess(): Boolean = this is Success
    
    /**
     * 判断是否为错误状态
     */
    fun isError(): Boolean = this is Error
    
    /**
     * 判断是否为加载中状态
     */
    fun isLoading(): Boolean = this is Loading
    
    /**
     * 获取数据，如果当前不是 Success 状态则返回 null
     */
    fun getDataOrNull(): T? = (this as? Success)?.data
    
    /**
     * 获取错误消息
     */
    fun getErrorMessage(): String = when (this) {
        is Error -> message
        else -> ""
    }
}


