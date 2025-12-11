package com.zfx.commonlib.ext

import androidx.lifecycle.viewModelScope
import com.zfx.commonlib.base.viewmodel.BaseViewModel
import com.zfx.commonlib.network.result.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * author: zhufeixiang
 * date: 2025/12/11
 * des: BaseViewModel 的 Flow 结果收集扩展，统一处理 Loading/Success/Error
 */

/**
 * 收集网络请求结果（Flow 版本）
 * 这是推荐使用的方式，支持响应式编程
 * 
 * @param flow 网络请求的 Flow
 * @param onLoading 加载中回调
 * @param onSuccess 成功回调
 * @param onError 错误回调
 */
fun <T> BaseViewModel.collectResult(
    flow: Flow<NetworkResult<T>>,
    onLoading: ((String) -> Unit)? = null,
    onSuccess: (T) -> Unit,
    onError: ((NetworkResult.Error) -> Unit)? = null
) {
    flow
        .onEach { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    onLoading?.invoke(result.message)
                }
                is NetworkResult.Success -> {
                    onSuccess(result.data)
                }
                is NetworkResult.Error -> {
                    onError?.invoke(result)
                }
            }
        }
        .catch { e ->
            onError?.invoke(
                NetworkResult.Error(
                    error = e,
                    message = e.message ?: "未知错误"
                )
            )
        }
        .launchIn(viewModelScope)
}

/**
 * 收集网络请求结果（简化版本，只处理成功和错误）
 */
fun <T> BaseViewModel.collectResult(
    flow: Flow<NetworkResult<T>>,
    onSuccess: (T) -> Unit,
    onError: ((NetworkResult.Error) -> Unit)? = null
) {
    collectResult(
        flow = flow,
        onLoading = null,
        onSuccess = onSuccess,
        onError = onError
    )
}

/**
 * 收集网络请求结果（带加载提示）
 */
fun <T> BaseViewModel.collectResultWithLoading(
    flow: Flow<NetworkResult<T>>,
    showLoading: (String) -> Unit,
    dismissLoading: () -> Unit,
    onSuccess: (T) -> Unit,
    onError: ((NetworkResult.Error) -> Unit)? = null
) {
    flow
        .onEach { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    showLoading(result.message)
                }
                is NetworkResult.Success -> {
                    dismissLoading()
                    onSuccess(result.data)
                }
                is NetworkResult.Error -> {
                    dismissLoading()
                    onError?.invoke(result)
                }
            }
        }
        .catch { e ->
            dismissLoading()
            onError?.invoke(
                NetworkResult.Error(
                    error = e,
                    message = e.message ?: "未知错误"
                )
            )
        }
        .launchIn(viewModelScope)
}

