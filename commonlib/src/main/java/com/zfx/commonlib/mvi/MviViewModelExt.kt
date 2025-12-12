package com.zfx.commonlib.mvi

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.zfx.commonlib.network.error.AppException
import com.zfx.commonlib.network.result.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * MVI ViewModel 扩展函数
 * 提供便捷的 Intent 处理和状态更新方法
 */

/**
 * 处理网络请求结果并更新状态
 * 
 * @param flow 网络请求的 Flow
 * @param onLoading 加载状态更新函数
 * @param onSuccess 成功状态更新函数
 * @param onError 错误状态更新函数
 */
fun <T, S : ViewState> MviViewModel<*, S>.handleNetworkResult(
    flow: Flow<NetworkResult<T>>,
    onLoading: (S, String) -> S,
    onSuccess: (S, T) -> S,
    onError: (S, NetworkResult.Error) -> S
) {
    viewModelScope.launch {
        flow
            .onEach { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        updateStateInternal { onLoading(it, result.message) }
                    }
                    is NetworkResult.Success -> {
                        updateStateInternal { onSuccess(it, result.data) }
                    }
                    is NetworkResult.Error -> {
                        updateStateInternal { onError(it, result) }
                    }
                }
            }
            .launchIn(this)
    }
}

/**
 * 处理网络请求结果（简化版本，使用 BaseViewState）
 */
fun <T> MviViewModel<*, BaseViewState>.handleNetworkResultSimple(
    flow: Flow<NetworkResult<T>>,
    onSuccess: (T) -> Unit
) {
    handleNetworkResult(
        flow = flow,
        onLoading = { state, message -> state.showLoading(message) },
        onSuccess = { state, data ->
            onSuccess(data)
            state.success()
        },
        onError = { state, error ->
            state.showError(
                AppException(
                    error.code,
                    error.message
                )
            )
        }
    )
}

/**
 * 在 LifecycleOwner 中观察状态
 */
fun <S : ViewState> MviViewModel<*, S>.observeState(
    owner: LifecycleOwner,
    onStateChange: (S) -> Unit
) {
    owner.lifecycleScope.launch {
        state.collect { state ->
            onStateChange(state)
        }
    }
}

/**
 * 在 LifecycleOwner 中观察单次事件
 */
fun <E : SingleEvent> MviViewModel<*, *>.observeSingleEvent(
    owner: LifecycleOwner,
    onEvent: (E) -> Unit
) {
    owner.lifecycleScope.launch {
        singleEvent.collect { event ->
            @Suppress("UNCHECKED_CAST")
            onEvent(event as E)
        }
    }
}

