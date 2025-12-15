package com.zfx.commonlib.ext

import androidx.lifecycle.viewModelScope
import com.zfx.commonlib.R
import com.zfx.commonlib.base.viewmodel.BaseViewModel
import com.zfx.commonlib.network.result.NetworkResult
import com.zfx.commonlib.util.StringResourceHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.timeout
import kotlin.time.Duration.Companion.seconds

/**
 * author: zhufeixiang
 * date: 2025/12/11
 * des: BaseViewModel 的 Flow 结果收集扩展，统一处理 Loading/Success/Error
 */

/**
 * 收集网络请求结果（推荐使用）
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
                    message = e.message ?: StringResourceHelper.getString(R.string.error_unknown_error_ext)
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
 * 自动管理 Loading 状态：收到 Loading 时显示，Success/Error 时关闭
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
                    message = e.message ?: StringResourceHelper.getString(R.string.error_unknown_error_ext)
                )
            )
        }
        .launchIn(viewModelScope)
}

/**
 * 只收集成功的数据（忽略 Loading 和 Error）
 */
fun <T> BaseViewModel.collectSuccess(
    flow: Flow<NetworkResult<T>>,
    onSuccess: (T) -> Unit
) {
    flow
        .filter { it is NetworkResult.Success }
        .onEach { result ->
            if (result is NetworkResult.Success) {
                onSuccess(result.data)
            }
        }
        .launchIn(viewModelScope)
}

/**
 * 只收集错误（忽略 Loading 和 Success）
 */
fun <T> BaseViewModel.collectError(
    flow: Flow<NetworkResult<T>>,
    onError: (NetworkResult.Error) -> Unit
) {
    flow
        .filter { it is NetworkResult.Error }
        .onEach { result ->
            if (result is NetworkResult.Error) {
                onError(result)
            }
        }
        .catch { e ->
            onError(
                NetworkResult.Error(
                    error = e,
                    message = e.message ?: StringResourceHelper.getString(R.string.error_unknown_error_ext)
                )
            )
        }
        .launchIn(viewModelScope)
}

/**
 * 收集网络请求结果（只收集一次，成功后自动取消）
 */
fun <T> BaseViewModel.collectResultOnce(
    flow: Flow<NetworkResult<T>>,
    onSuccess: (T) -> Unit,
    onError: ((NetworkResult.Error) -> Unit)? = null
) {
    flow
        .take(1)
        .onEach { result ->
            when (result) {
                is NetworkResult.Success -> {
                    onSuccess(result.data)
                }
                is NetworkResult.Error -> {
                    onError?.invoke(result)
                }
                is NetworkResult.Loading -> {
                    // 忽略 Loading
                }
            }
        }
        .catch { e ->
            onError?.invoke(
                NetworkResult.Error(
                    error = e,
                    message = e.message ?: StringResourceHelper.getString(R.string.error_unknown_error_ext)
                )
            )
        }
        .launchIn(viewModelScope)
}

/**
 * 收集网络请求结果（带超时）
 * 
 * @param timeoutSeconds 超时时间（秒），默认为 30
 */
fun <T> BaseViewModel.collectResultWithTimeout(
    flow: Flow<NetworkResult<T>>,
    timeoutSeconds: Long = 30,
    onLoading: ((String) -> Unit)? = null,
    onSuccess: (T) -> Unit,
    onError: ((NetworkResult.Error) -> Unit)? = null
) {
    flow
        .timeout(timeoutSeconds.seconds)
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
                    code = -1,
                    message = if (e is kotlinx.coroutines.TimeoutCancellationException) {
                        StringResourceHelper.getString(R.string.error_request_timeout_ext)
                    } else {
                        e.message ?: StringResourceHelper.getString(R.string.error_unknown_error_ext)
                    }
                )
            )
        }
        .launchIn(viewModelScope)
}

/**
 * 收集网络请求结果（带防抖）
 * 
 * @param debounceMillis 防抖时间（毫秒），默认为 500
 */
fun <T> BaseViewModel.collectResultWithDebounce(
    flow: Flow<NetworkResult<T>>,
    debounceMillis: Long = 500,
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
                    delay(debounceMillis)
                    onSuccess(result.data)
                }
                is NetworkResult.Error -> {
                    delay(debounceMillis)
                    onError?.invoke(result)
                }
            }
        }
        .catch { e ->
            delay(debounceMillis)
            onError?.invoke(
                NetworkResult.Error(
                    error = e,
                    message = e.message ?: StringResourceHelper.getString(R.string.error_unknown_error_ext)
                )
            )
        }
        .launchIn(viewModelScope)
}

/**
 * 等待网络请求完成并返回结果（挂起函数）
 * 
 * 与 collectResult 的区别：
 * - collectResult: 非 suspend，使用回调处理结果
 * - awaitResult: suspend，直接返回结果，失败时抛出异常
 * 
 * @return 成功时返回数据，失败时抛出异常
 * @throws Exception 如果请求失败
 */
suspend fun <T> BaseViewModel.awaitResult(flow: Flow<NetworkResult<T>>): T {
    return flow
        .filter { it is NetworkResult.Success || it is NetworkResult.Error }
        .first()
        .let { result ->
            when (result) {
                is NetworkResult.Success -> result.data
                is NetworkResult.Error -> {
                    throw result.error ?: RuntimeException(result.message)
                }
                is NetworkResult.Loading -> throw IllegalStateException(
                    StringResourceHelper.getString(R.string.error_illegal_state)
                )
            }
        }
}

/**
 * 等待网络请求完成并返回结果（挂起函数，带默认值）
 * 失败时返回默认值，不抛出异常
 */
suspend fun <T> BaseViewModel.awaitResultOrDefault(
    flow: Flow<NetworkResult<T>>,
    defaultValue: T
): T {
    return try {
        awaitResult(flow)
    } catch (e: Exception) {
        defaultValue
    }
}

/**
 * 等待网络请求完成并返回结果（挂起函数，使用 Result 包装）
 * 
 * 与 awaitResult 的区别：
 * - awaitResult: 失败时抛出异常
 * - awaitResultSafe: 失败时返回 Result.failure，不抛出异常
 * 
 * 适用场景：需要分别处理每个请求的成功/失败，允许部分请求失败
 */
suspend fun <T> BaseViewModel.awaitResultSafe(flow: Flow<NetworkResult<T>>): Result<T> {
    return try {
        Result.success(awaitResult(flow))
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/**
 * 请求网络数据并获取结果（非 suspend 版本，使用 Result 包装）
 * 
 * 与 awaitResultSafe 的区别：
 * - awaitResultSafe: suspend 函数，需要在协程中调用，返回 Result<T>
 * - requestResultSafe: 非 suspend 函数，自动启动协程，使用回调处理 Result
 * 
 * 与 collectResult 的区别：
 * - collectResult: 分别处理成功/失败回调
 * - requestResultSafe: 统一在 onSuccess 中处理 Result，可以更灵活地处理成功/失败
 */
fun <T> BaseViewModel.requestResultSafe(
    flow: Flow<NetworkResult<T>>,
    onLoading: ((String) -> Unit)? = null,
    onSuccess: (Result<T>) -> Unit,
    onError: ((NetworkResult.Error) -> Unit)? = null
) {
    flow
        .onEach { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    onLoading?.invoke(result.message)
                }
                is NetworkResult.Success -> {
                    onSuccess(Result.success(result.data))
                }
                is NetworkResult.Error -> {
                    val exception = result.error ?: RuntimeException(result.message)
                    val resultWrapper = Result.failure<T>(exception)
                    onSuccess(resultWrapper)
                    onError?.invoke(result)
                }
            }
        }
        .catch { e ->
            val resultWrapper = Result.failure<T>(e)
            onSuccess(resultWrapper)
            onError?.invoke(
                NetworkResult.Error(
                    error = e,
                    message = e.message ?: StringResourceHelper.getString(R.string.error_unknown_error_ext)
                )
            )
        }
        .launchIn(viewModelScope)
}
