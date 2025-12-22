package com.zfx.commonlib.network.response

import com.zfx.commonlib.R
import com.zfx.commonlib.util.StringResourceHelper

/**
 * 网络响应接口
 * 允许不同项目根据各自的响应结构实现此接口
 * 
 * **注意**：
 * - 接口只定义契约，不包含具体实现细节
 * - 不需要 `configureSuccess` 方法（这是 `BaseResponse` 特有的配置功能）
 * - 不需要 `companion object`（只有需要全局配置的实现类才需要）
 * - 自定义实现时，直接在 `isSuccess()` 中实现自己的成功判定逻辑即可
 * 
 * @param T 响应数据类型
 */
interface IBaseResponse<T> {
    /**
     * 判断请求是否成功
     */
    fun isSuccess(): Boolean
    
    /**
     * 获取响应数据，如果数据为空则抛出异常
     */
    fun getDataOrThrow(): T
    
    /**
     * 获取响应数据，如果数据为空则返回默认值
     */
    fun getDataOrDefault(defaultValue: T): T
    
    /**
     * 获取错误消息
     */
    fun getErrorMessage(): String
    
    /**
     * 获取响应码
     */
    fun getResponseCode(): Int
    
    /**
     * 获取响应消息
     */
    fun getResponseMsg(): String
    
    /**
     * 获取响应数据（可能为空）
     * 注意：改名为 getDataOrNull() 以避免与数据类属性的 getData() getter 冲突
     */
    fun getDataOrNull(): T?
}

/**
 * 服务器返回数据的默认实现（数据类版本）
 * 支持 Flow 网络请求
 * 
 * **特点**：
 * - 提供 `companion object` 用于全局配置成功判定规则
 * - 提供 `configureSuccess()` 方法用于配置成功码、空数据策略等
 * - 如果你的项目响应结构不同，可以实现 `IBaseResponse` 接口创建自己的响应类
 * 
 * **自定义响应类说明**：
 * - 不需要 `companion object`：如果成功判定逻辑简单，直接在 `isSuccess()` 中实现即可
 * - 需要 `companion object`：如果需要在全局配置成功判定规则（类似 `BaseResponse`），可以添加
 * 
 * @param T 响应数据类型
 * @param code 响应码字段名（默认：code）
 * @param message 响应消息字段名（默认：message）
 * @param data 响应数据字段名（默认：data）
 * 
 * **示例1：简单实现（不需要 companion object）**
 * ```
 * data class MyResponse<T>(
 *     val status: Int = 0,
 *     val msg: String = "",
 *     val result: T? = null
 * ) : IBaseResponse<T> {
 *     override fun isSuccess(): Boolean = status == 200
 *     override fun getDataOrThrow(): T = result ?: throw IllegalStateException("响应数据为空")
 *     override fun getDataOrDefault(defaultValue: T): T = result ?: defaultValue
 *     override fun getErrorMessage(): String = msg.ifEmpty { "未知错误" }
 *     override fun getResponseCode(): Int = status
 *     override fun getResponseMsg(): String = msg
 *     override fun getDataOrNull(): T? = result
 * }
 * ```
 * 
 * **示例2：需要全局配置（需要 companion object）**
 * ```
 * data class MyResponse<T>(
 *     val status: Int = 0,
 *     val msg: String = "",
 *     val result: T? = null
 * ) : IBaseResponse<T> {
 *     companion object {
 *         var successStatus: Set<Int> = setOf(200, 201)
 *         
 *         fun configureSuccess(codes: Set<Int>) {
 *             successStatus = codes
 *         }
 *     }
 *     
 *     override fun isSuccess(): Boolean = status in successStatus
 *     // ... 其他方法实现
 * }
 * ```
 */
data class BaseResponse<T>(
    val code: Int = 0,
    val message: String = "",
    val data: T? = null
) : IBaseResponse<T> {
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
    override fun isSuccess(): Boolean {
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
    override fun getDataOrThrow(): T {
        return data ?: throw IllegalStateException(StringResourceHelper.getString(R.string.response_data_empty))
    }
    
    /**
     * 获取响应数据，如果数据为空则返回默认值
     */
    override fun getDataOrDefault(defaultValue: T): T {
        return data ?: defaultValue
    }
    
    /**
     * 获取错误消息
     */
    override fun getErrorMessage(): String {
        return message.ifEmpty { StringResourceHelper.getString(R.string.response_unknown_error) }
    }
    
    /**
     * 获取响应码
     */
    override fun getResponseCode(): Int = code
    
    /**
     * 获取响应消息
     */
    override fun getResponseMsg(): String = message

    /**
     * 获取响应数据（可能为空）
     */
    override fun getDataOrNull(): T? = data
}


