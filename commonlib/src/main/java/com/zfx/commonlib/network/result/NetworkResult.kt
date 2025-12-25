package com.zfx.commonlib.network.result

import com.zfx.commonlib.R
import com.zfx.commonlib.util.StringResourceHelper

/**
 * 网络请求结果密封类
 * 使用 Flow 时返回此类型，支持 Loading、Success、Error 三种状态
 * 
 * @param T 响应数据类型
 */
sealed class NetworkResult<out T> {
    
    companion object {
        /**
         * 创建加载中状态（使用默认的本地化消息）
         * 
         * **使用场景**：
         * 1. 手动触发 Loading 状态（在 ViewModel 中）
         * 2. 设置初始状态为 Loading
         * 3. 在 Flow 中手动发射 Loading 状态
         * 
         * **在 ViewModel 中的使用示例**：
         * ```kotlin
         * class ExampleViewModel : BaseViewModel() {
         *     private val _uiState = MutableStateFlow<NetworkResult<List<Data>>>(NetworkResult.loading())
         *     val uiState: StateFlow<NetworkResult<List<Data>>> = _uiState.asStateFlow()
         *     
         *     fun loadData() {
         *         // 手动设置 loading 状态（使用默认消息）
         *         _uiState.value = NetworkResult.loading()
         *         
         *         // 或者使用自定义消息
         *         _uiState.value = NetworkResult.Loading("正在加载数据...")
         *         
         *         collectResult(
         *             flow = repository.getData(),
         *             onSuccess = { data ->
         *                 _uiState.value = NetworkResult.Success(data)
         *             },
         *             onError = { error ->
         *                 _uiState.value = error
         *             }
         *         )
         *     }
         * }
         * ```
         * 
         * @return Loading 实例，消息为本地化的加载提示信息（"请求中..."）
         */
        fun loading(): Loading {
            return Loading(StringResourceHelper.getString(R.string.network_requesting))
        }
        
        /**
         * 创建错误状态（使用默认的本地化消息）
         * 
         * **使用场景**：
         * 1. 在 catch 块中创建错误状态
         * 2. 手动创建错误状态（在 ViewModel 中）
         * 3. 统一错误处理
         * 
         * **在 ViewModel 中的使用示例**：
         * ```kotlin
         * class ExampleViewModel : BaseViewModel() {
         *     fun loadData() {
         *         viewModelScope.launch {
         *             try {
         *                 _uiState.value = NetworkResult.loading()
         *                 val data = repository.getData()
         *                 _uiState.value = NetworkResult.Success(data)
         *             } catch (e: Exception) {
         *                 // 使用工厂方法创建错误状态（使用默认消息）
         *                 _uiState.value = NetworkResult.error(error = e)
         *                 
         *                 // 或者使用自定义消息
         *                 _uiState.value = NetworkResult.Error(
         *                     error = e,
         *                     code = -1,
         *                     message = "加载失败：${e.message}"
         *                 )
         *             }
         *         }
         *     }
         * }
         * ```
         * 
         * @param error 错误异常（可选）
         * @param code 错误码（默认 -1）
         * @return Error 实例，消息为本地化的错误提示信息（"网络请求失败"）
         */
        fun error(error: Throwable? = null, code: Int = -1): Error {
            return Error(
                error = error,
                code = code,
                message = StringResourceHelper.getString(R.string.network_request_failed)
            )
        }
    }
    
    /**
     * 加载中状态
     * @param message 加载提示信息（必须提供，建议使用 [loading] 工厂方法创建）
     */
    data class Loading(val message: String) : NetworkResult<Nothing>()
    
    /**
     * 成功状态
     * @param data 响应数据
     */
    data class Success<out T>(val data: T) : NetworkResult<T>()
    
    /**
     * 错误状态
     * @param error 错误异常
     * @param code 错误码
     * @param message 错误消息（必须提供，建议使用 [error] 工厂方法创建）
     */
    data class Error(
        val error: Throwable? = null,
        val code: Int = -1,
        val message: String
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


