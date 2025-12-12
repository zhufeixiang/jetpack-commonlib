package com.zfx.commonlib.network.repository

import com.zfx.commonlib.R
import com.zfx.commonlib.network.error.AppException
import com.zfx.commonlib.network.error.ExceptionHandle
import com.zfx.commonlib.network.response.IBaseResponse
import com.zfx.commonlib.network.result.NetworkResult
import com.zfx.commonlib.util.StringResourceHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * 基础Repository类
 * 职责单一：封装网络请求和错误处理逻辑
 * 可继承：子类可以继承此类并添加自定义的网络请求方法
 * 
 * 使用 Flow 来处理网络请求，提供响应式编程体验
 */
abstract class BaseRepository {
    
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
                emit(NetworkResult.Error(
                    error = null,
                    code = response.getResponseCode(),
                    message = response.getErrorMessage()
                ))
            }
            
        } catch (e: Exception) {
            // 处理异常
            val appException = ExceptionHandle.handleException(e)
            emit(NetworkResult.Error(
                error = e,
                code = appException.errCode,
                message = appException.errorMsg
            ))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * 执行网络请求（不校验响应数据）
     * 直接返回响应对象，不进行成功/失败判断
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
            
            val response = apiCall()
            emit(NetworkResult.Success(response))
            
        } catch (e: Exception) {
            val appException = ExceptionHandle.handleException(e)
            emit(NetworkResult.Error(
                error = e,
                code = appException.errCode,
                message = appException.errorMsg
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

