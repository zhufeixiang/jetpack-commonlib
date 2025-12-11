package com.zfx.commonlib.network.response

/**
 * 服务器返回数据的基类（数据类版本）
 * 支持 Flow 网络请求
 * 
 * @param T 响应数据类型
 * @param code 响应码
 * @param message 响应消息
 * @param data 响应数据
 */
data class BaseResponse<T>(
    val code: Int = 0,
    val message: String = "",
    val data: T? = null
) {
    companion object {
        /**
         * 成功码集合，默认包含 200，可根据项目自定义
         */
        @JvmStatic
        var successCodes: Set<Int> = setOf(200)

        /**
         * 是否允许 data 为空仍视为成功（部分接口返回无数据时仅关心 code）
         */
        @JvmStatic
        var allowNullData: Boolean = false

        /**
         * 自定义成功判定（优先级最高），返回 true 表示成功
         */
        @JvmStatic
        var customChecker: ((code: Int, data: Any?) -> Boolean)? = null

        /**
         * 配置成功判定
         */
        @JvmStatic
        fun configureSuccess(
            codes: Set<Int> = successCodes,
            allowNullData: Boolean = this.allowNullData,
            checker: ((code: Int, data: Any?) -> Boolean)? = null
        ) {
            successCodes = codes
            this.allowNullData = allowNullData
            customChecker = checker
        }
    }

    /**
     * 判断请求是否成功
     * 优先使用 customChecker；否则按 successCodes + allowNullData 判断
     */
    open fun isSuccess(): Boolean {
        customChecker?.let { checker ->
            return checker(code, data)
        }
        val codeOk = code in successCodes
        val dataOk = allowNullData || data != null
        return codeOk && dataOk
    }
    
    /**
     * 获取响应数据，如果数据为空则抛出异常
     */
    fun getDataOrThrow(): T {
        return data ?: throw IllegalStateException("响应数据为空")
    }
    
    /**
     * 获取响应数据，如果数据为空则返回默认值
     */
    fun getDataOrDefault(defaultValue: T): T {
        return data ?: defaultValue
    }
    
    /**
     * 获取错误消息
     */
    fun getErrorMessage(): String {
        return message.ifEmpty { "未知错误" }
    }
    
    /**
     * 获取响应码
     */
    fun getResponseCode(): Int = code
    
    /**
     * 获取响应消息
     */
    fun getResponseMsg(): String = message
    
}


