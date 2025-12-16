package com.zfx.commonlib.network.repository

import com.zfx.commonlib.R
import com.zfx.commonlib.network.error.ExceptionHandle
import com.zfx.commonlib.network.interceptor.LoginInterceptor
import com.zfx.commonlib.network.response.IBaseResponse
import com.zfx.commonlib.network.result.NetworkResult
import com.zfx.commonlib.util.StringResourceHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/**
 * 未登录拦截器配置类（支持链式调用）
 */
class LoginInterceptorConfig private constructor(
    private val interceptor: LoginInterceptor
) {
    /**
     * 设置未登录错误码集合
     */
    fun unauthorizedCodes(codes: Set<Int>): LoginInterceptorConfig {
        BaseRepository.unauthorizedCodes = codes
        return this
    }
    
    /**
     * 设置拦截时间窗口（毫秒）
     */
    fun interceptWindowMillis(millis: Long): LoginInterceptorConfig {
        BaseRepository.interceptWindowMillis = millis
        return this
    }
    
    companion object {
        /**
         * 创建配置对象
         */
        internal fun create(interceptor: LoginInterceptor): LoginInterceptorConfig {
            return LoginInterceptorConfig(interceptor)
        }
    }
}

/**
 * 基础Repository类
 * 职责单一：封装网络请求和错误处理逻辑
 * 可继承：子类可以继承此类并添加自定义的网络请求方法
 * 
 * 使用 Flow 来处理网络请求，提供响应式编程体验
 */
abstract class BaseRepository {
    
    companion object {
        /**
         * 未登录错误码集合（可配置多个错误码）
         * 默认包含 401（HTTP 未授权错误码）
         */
        @JvmStatic
        @Volatile
        var unauthorizedCodes: Set<Int> = setOf(401)
        
        /**
         * 未登录拦截器回调
         */
        @JvmStatic
        @Volatile
        internal var loginInterceptor: LoginInterceptor? = null
        
        /**
         * 检查未登录拦截器是否已配置
         * 
         * @return true 表示已配置，false 表示未配置
         */
        @JvmStatic
        fun isLoginInterceptorConfigured(): Boolean {
            return loginInterceptor != null
        }
        
        /**
         * 拦截时间窗口（毫秒），在此时间窗口内只拦截一次
         * 默认 5 秒，避免短时间内多次触发未登录拦截
         */
        @JvmStatic
        @Volatile
        var interceptWindowMillis: Long = 5000
        
        /**
         * 上次拦截的时间戳（用于确保只拦截一次）
         */
        @Volatile
        internal var lastInterceptTime: AtomicLong = AtomicLong(0)
        
        /**
         * 是否正在处理未登录（用于防止并发情况下的重复处理）
         */
        @Volatile
        internal var isIntercepting: AtomicBoolean = AtomicBoolean(false)
        
        /**
         * 配置未登录拦截器（支持链式调用）
         * 
         * @param interceptor 未登录拦截器回调，当检测到未登录错误码时会调用此回调
         * @param unauthorizedCodes 未登录错误码集合，默认为 {401}
         * @param interceptWindowMillis 拦截时间窗口（毫秒），在此时间窗口内只拦截一次，默认 5 秒
         * @return LoginInterceptorConfig 配置对象，支持链式调用
         * 
         * 使用示例（链式调用）：
         * ```kotlin
         * BaseRepository.setLoginInterceptor(
         *     object : LoginInterceptor {
         *         override fun onUnauthorized(errorCode: Int, errorMessage: String) {
         *             // 清除登录信息
         *             UserManager.clearUserInfo()
         *             // 跳转到登录页面
         *             startActivity(Intent(context, LoginActivity::class.java))
         *         }
         *     }
         * ).unauthorizedCodes(setOf(1001))
         *  .interceptWindowMillis(3000)
         * ```
         * 
         * 使用示例（传统方式，也支持链式调用）：
         * ```kotlin
         * BaseRepository.setLoginInterceptor(
         *     interceptor = object : LoginInterceptor {
         *         override fun onUnauthorized(errorCode: Int, errorMessage: String) {
         *             // 清除登录信息
         *             UserManager.clearUserInfo()
         *             // 跳转到登录页面
         *             startActivity(Intent(context, LoginActivity::class.java))
         *         }
         *     },
         *     unauthorizedCodes = setOf(401, 403), // 可以配置多个错误码
         *     interceptWindowMillis = 3000 // 3 秒内只拦截一次
         * )
         * ```
         */
        @JvmStatic
        @JvmOverloads
        fun setLoginInterceptor(
            interceptor: LoginInterceptor,
            unauthorizedCodes: Set<Int> = setOf(401),
            interceptWindowMillis: Long = 5000
        ): LoginInterceptorConfig {
            loginInterceptor = interceptor
            this.unauthorizedCodes = unauthorizedCodes
            this.interceptWindowMillis = interceptWindowMillis
            // 重置拦截状态
            lastInterceptTime.set(0)
            isIntercepting.set(false)
            // 返回配置对象，支持链式调用（值已设置，链式调用可以覆盖）
            return LoginInterceptorConfig.create(interceptor)
        }
        
        /**
         * 清除未登录拦截器
         */
        @JvmStatic
        fun clearLoginInterceptor() {
            loginInterceptor = null
            unauthorizedCodes = setOf(401)
            lastInterceptTime.set(0)
            isIntercepting.set(false)
        }
        
        /**
         * 检查并处理未登录错误码
         * 如果错误码匹配且满足拦截条件，则调用拦截器回调
         * 
         * @param errorCode 错误码
         * @param errorMessage 错误消息
         * @return 是否已处理（true 表示已拦截，false 表示未拦截）
         */
        @JvmStatic
        internal fun checkAndInterceptUnauthorized(errorCode: Int, errorMessage: String): Boolean {
            // 如果没有配置拦截器，直接返回
            val interceptor = loginInterceptor ?: return false
            
            // 如果错误码不在未登录错误码集合中，直接返回
            if (errorCode !in unauthorizedCodes) {
                return false
            }
            
            // 检查是否在时间窗口内
            val currentTime = System.currentTimeMillis()
            val lastTime = lastInterceptTime.get()
            val timeDiff = currentTime - lastTime
            
            // 如果距离上次拦截时间小于时间窗口，且正在处理中，则跳过
            if (timeDiff < interceptWindowMillis && isIntercepting.get()) {
                return false
            }
            
            // 尝试获取拦截锁（防止并发）
            if (!isIntercepting.compareAndSet(false, true)) {
                return false
            }
            
            try {
                // 更新拦截时间
                lastInterceptTime.set(currentTime)
                
                // 调用拦截器回调
                interceptor.onUnauthorized(errorCode, errorMessage)
                
                return true
            } finally {
                // 释放拦截锁
                isIntercepting.set(false)
            }
        }
    }
    
    /**
     * 执行网络请求并返回 Flow<NetworkResult<T>>
     * 这是主要的网络请求方法，会自动处理加载、成功、错误状态
     * 
     * @param apiCall 网络请求的 suspend 函数，返回实现了 IBaseResponse 的响应对象
     * @param showLoading 是否显示加载状态，默认为 true
     * @param loadingMessage 加载提示信息
     * @return Flow<NetworkResult<T>> 网络请求结果流
     */
    protected fun <T, R : IBaseResponse<T>> requestFlow(
        apiCall: suspend () -> R,
        showLoading: Boolean = true,
        loadingMessage: String = StringResourceHelper.getString(R.string.network_requesting)
    ): Flow<NetworkResult<T>> = flow {
        try {
            // 发送加载状态
            if (showLoading) {
                emit(NetworkResult.Loading(loadingMessage))
            }
            
            // 执行API调用
            // 注意：由于使用了 .flowOn(Dispatchers.IO)，整个 Flow 的执行都在 IO 线程
            // 所以 apiCall() 已经在 IO 线程执行，不需要额外的 withContext
            val response = apiCall()
            
            // 处理响应
            if (response.isSuccess()) {
                try {
                    val data = response.getDataOrThrow()
                    emit(NetworkResult.Success(data))
                } catch (e: IllegalStateException) {
                    emit(NetworkResult.Error(
                        error = e,
                        code = response.getResponseCode(),
                        message = StringResourceHelper.getString(R.string.network_data_empty)
                    ))
                }
            } else {
                val errorCode = response.getResponseCode()
                val errorMessage = response.getErrorMessage()
                
                // 检查并处理未登录错误码
                checkAndInterceptUnauthorized(errorCode, errorMessage)
                
                emit(NetworkResult.Error(
                    error = null,
                    code = errorCode,
                    message = errorMessage
                ))
            }
            
        } catch (e: Exception) {
            // 处理异常
            val appException = ExceptionHandle.handleException(e)
            val errorCode = appException.errCode
            val errorMessage = appException.errorMsg
            
            // 检查并处理未登录错误码（异常情况也可能返回未登录错误码）
            checkAndInterceptUnauthorized(errorCode, errorMessage)
            
            emit(NetworkResult.Error(
                error = e,
                code = errorCode,
                message = errorMessage
            ))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * 执行网络请求（响应没有 data 字段）
     * 适用于只需要判断成功/失败，不需要返回数据的接口（如删除、更新等操作）
     * 
     * @param apiCall 网络请求的 suspend 函数，返回实现了 IBaseResponse 的响应对象（data 字段可以为空）
     * @param showLoading 是否显示加载状态，默认为 true
     * @param loadingMessage 加载提示信息
     * @return Flow<NetworkResult<Unit>> 网络请求结果流，成功时返回 Unit
     */
    protected fun <R : IBaseResponse<*>> requestFlowNoData(
        apiCall: suspend () -> R,
        showLoading: Boolean = true,
        loadingMessage: String = StringResourceHelper.getString(R.string.network_requesting)
    ): Flow<NetworkResult<Unit>> = flow {
        try {
            // 发送加载状态
            if (showLoading) {
                emit(NetworkResult.Loading(loadingMessage))
            }
            
            // 执行API调用
            // 注意：由于使用了 .flowOn(Dispatchers.IO)，整个 Flow 的执行都在 IO 线程
            // 所以 apiCall() 已经在 IO 线程执行，不需要额外的 withContext
            val response = apiCall()
            
            // 处理响应（只判断成功/失败，不获取 data）
            if (response.isSuccess()) {
                emit(NetworkResult.Success(Unit))
            } else {
                val errorCode = response.getResponseCode()
                val errorMessage = response.getErrorMessage()
                
                // 检查并处理未登录错误码
                checkAndInterceptUnauthorized(errorCode, errorMessage)
                
                emit(NetworkResult.Error(
                    error = null,
                    code = errorCode,
                    message = errorMessage
                ))
            }
            
        } catch (e: Exception) {
            // 处理异常
            val appException = ExceptionHandle.handleException(e)
            val errorCode = appException.errCode
            val errorMessage = appException.errorMsg
            
            // 检查并处理未登录错误码（异常情况也可能返回未登录错误码）
            checkAndInterceptUnauthorized(errorCode, errorMessage)
            
            emit(NetworkResult.Error(
                error = e,
                code = errorCode,
                message = errorMessage
            ))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * 执行网络请求（不校验响应数据）
     * 直接返回响应对象，不进行成功/失败判断
     * 
     * 注意：如果返回的响应对象实现了 IBaseResponse 接口，仍会检查未登录错误码
     * 
     * @param apiCall 网络请求的 suspend 函数
     * @param showLoading 是否显示加载状态
     * @param loadingMessage 加载提示信息
     * @return Flow<NetworkResult<T>> 网络请求结果流
     */
    protected fun <T> requestFlowRaw(
        apiCall: suspend () -> T,
        showLoading: Boolean = true,
        loadingMessage: String = StringResourceHelper.getString(R.string.network_requesting)
    ): Flow<NetworkResult<T>> = flow {
        try {
            if (showLoading) {
                emit(NetworkResult.Loading(loadingMessage))
            }
            
            // 执行API调用
            // 注意：由于使用了 .flowOn(Dispatchers.IO)，整个 Flow 的执行都在 IO 线程
            // 所以 apiCall() 已经在 IO 线程执行，不需要额外的 withContext
            val response = apiCall()
            
            // 如果响应对象实现了 IBaseResponse 接口，检查未登录错误码
            if (response is IBaseResponse<*>) {
                val errorCode = response.getResponseCode()
                val errorMessage = response.getErrorMessage()
                
                // 检查并处理未登录错误码
                checkAndInterceptUnauthorized(errorCode, errorMessage)
            }
            
            emit(NetworkResult.Success(response))
            
        } catch (e: Exception) {
            val appException = ExceptionHandle.handleException(e)
            val errorCode = appException.errCode
            val errorMessage = appException.errorMsg
            
            // 检查并处理未登录错误码（异常情况也可能返回未登录错误码）
            checkAndInterceptUnauthorized(errorCode, errorMessage)
            
            emit(NetworkResult.Error(
                error = e,
                code = errorCode,
                message = errorMessage
            ))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * 处理网络异常（扩展方法）
     * 子类可以重写此方法来自定义异常处理逻辑
     */
    protected fun handleNetworkException(e: Exception): NetworkResult.Error {
        val appException = ExceptionHandle.handleException(e)
        return NetworkResult.Error(
            error = e,
            code = appException.errCode,
            message = appException.errorMsg
        )
    }
}

