package com.zfx.commonlib.network.extension

import android.content.SharedPreferences
import com.zfx.commonlib.network.config.NetworkConfig
import com.zfx.commonlib.network.config.NetworkConfigBuilder
import com.zfx.commonlib.network.config.NetworkEnvironment
import com.zfx.commonlib.network.config.NetworkEnvironmentManager
import com.zfx.commonlib.network.config.EnvironmentConfig
import com.zfx.commonlib.network.config.EnvironmentConfigBuilder
import com.zfx.commonlib.R
import com.zfx.commonlib.network.manager.NetworkManager
import com.zfx.commonlib.network.response.IBaseResponse
import com.zfx.commonlib.network.result.NetworkResult
import com.zfx.commonlib.util.StringResourceHelper
import me.jessyan.retrofiturlmanager.RetrofitUrlManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers

/**
 * 网络扩展函数
 * 提供便捷的网络请求方法和环境管理
 */

/**
 * 初始化网络管理器（使用配置对象）
 */
fun initNetworkManager(config: NetworkConfig) {
    NetworkManager.getInstance().init(config)
}

/**
 * 初始化网络管理器（使用构建器）
 */
fun initNetworkManager(builder: NetworkConfigBuilder.() -> Unit) {
    val config = NetworkConfigBuilder().apply(builder).build()
    NetworkManager.getInstance().init(config)
}

/**
 * 初始化网络管理器（简单方式）
 */
fun initNetworkManager(
    baseUrl: String,
    enableLogging: Boolean = true
) {
    val config = NetworkConfig(
        baseUrl = baseUrl,
        enableLogging = enableLogging
    )
    NetworkManager.getInstance().init(config)
}

/**
 * 获取API服务（泛型方式）
 */
inline fun <reified T> getApiService(): T {
    return NetworkManager.getInstance().getApiService<T>()
}

/**
 * 获取API服务（Class方式）
 */
fun <T> getApiService(serviceClass: Class<T>): T {
    return NetworkManager.getInstance().getApiService(serviceClass)
}

/**
 * 添加认证拦截器
 */
fun addAuthInterceptor(token: String) {
    NetworkManager.getInstance().addAuthInterceptor(token)
}

/**
 * 清除网络缓存
 */
fun clearNetworkCache() {
    NetworkManager.getInstance().clearCache()
}

/**
 * 检查网络管理器是否已初始化
 */
fun isNetworkManagerInitialized(): Boolean {
    return NetworkManager.getInstance().isInitialized()
}

// ==================== 环境管理扩展函数 ====================

/**
 * 初始化环境管理器
 */
fun initNetworkEnvironmentManager(sharedPreferences: SharedPreferences? = null) {
    NetworkEnvironmentManager.getInstance().init(sharedPreferences)
}

/**
 * 配置开发环境
 */
fun configureDevelopmentEnvironment(builder: EnvironmentConfigBuilder.() -> Unit) {
    val config = EnvironmentConfigBuilder(NetworkEnvironment.DEVELOPMENT).apply(builder).build()
    NetworkEnvironmentManager.getInstance().configureDevelopment(config)
}

/**
 * 配置预发布环境
 */
fun configurePreReleaseEnvironment(builder: EnvironmentConfigBuilder.() -> Unit) {
    val config = EnvironmentConfigBuilder(NetworkEnvironment.PRE_RELEASE).apply(builder).build()
    NetworkEnvironmentManager.getInstance().configurePreRelease(config)
}

/**
 * 配置生产环境
 */
fun configureProductionEnvironment(builder: EnvironmentConfigBuilder.() -> Unit) {
    val config = EnvironmentConfigBuilder(NetworkEnvironment.PRODUCTION).apply(builder).build()
    NetworkEnvironmentManager.getInstance().configureProduction(config)
}

/**
 * 配置所有环境（推荐使用）
 */
fun configureAllEnvironments(
    development: EnvironmentConfigBuilder.() -> Unit,
    preRelease: EnvironmentConfigBuilder.() -> Unit,
    production: EnvironmentConfigBuilder.() -> Unit
) {
    val devConfig = EnvironmentConfigBuilder(NetworkEnvironment.DEVELOPMENT).apply(development).build()
    val preConfig = EnvironmentConfigBuilder(NetworkEnvironment.PRE_RELEASE).apply(preRelease).build()
    val prodConfig = EnvironmentConfigBuilder(NetworkEnvironment.PRODUCTION).apply(production).build()
    
    NetworkEnvironmentManager.getInstance().configureEnvironments(devConfig, preConfig, prodConfig)
}

/**
 * 切换环境
 * @param environment 目标环境
 * @return 是否切换成功
 */
fun switchNetworkEnvironment(environment: NetworkEnvironment): Boolean {
    val manager = NetworkEnvironmentManager.getInstance()
    val config = manager.getEnvironmentConfig(environment)
    
    if (config == null) {
        return false
    }
    
    // 切换环境管理器中的当前环境
    val success = manager.switchEnvironment(environment)
    
    if (success) {
        // 重新初始化网络管理器
        NetworkManager.getInstance().switchEnvironment(config)
    }
    
    return success
}

/**
 * 使用当前环境配置初始化网络管理器
 */
fun initNetworkManagerWithCurrentEnvironment() {
    val manager = NetworkEnvironmentManager.getInstance()
    val config = manager.getCurrentNetworkConfig()
    
    if (config == null) {
        throw IllegalStateException("当前环境未配置，请先配置环境")
    }
    
    initNetworkManager(config)
}

/**
 * 获取当前环境
 */
fun getCurrentNetworkEnvironment(): NetworkEnvironment {
    return NetworkEnvironmentManager.getInstance().getCurrentEnvironment()
}

/**
 * 检查环境是否已配置
 */
fun isEnvironmentConfigured(environment: NetworkEnvironment): Boolean {
    return NetworkEnvironmentManager.getInstance().isEnvironmentConfigured(environment)
}

// ==================== 动态多域名（RetrofitUrlManager 包装） ====================

/**
 * 设置全局 BaseUrl（调用后需要重新创建服务或重新发起请求才生效）
 */
fun setGlobalBaseUrl(baseUrl: String) {
    RetrofitUrlManager.getInstance().setGlobalDomain(baseUrl)
}

/**
 * 为指定域名标识配置 BaseUrl
 * 使用方式：在接口方法上添加 @Headers("Domain-Name: {domainName}")
 */
fun putDomain(domainName: String, domainUrl: String) {
    RetrofitUrlManager.getInstance().putDomain(domainName, domainUrl)
}

/**
 * 移除指定域名标识的 BaseUrl 配置
 */
fun removeDomain(domainName: String) {
    RetrofitUrlManager.getInstance().removeDomain(domainName)
}

/**
 * 清除所有动态域名配置
 */
fun clearAllDomains() {
    RetrofitUrlManager.getInstance().clearAllDomain()
}

/**
 * Flow 扩展函数：将 IBaseResponse 转换为 NetworkResult
 * 这是一个便捷的扩展函数，可以直接在 Repository 中使用
 * 
 * 支持任何实现了 IBaseResponse 接口的响应类型
 */
fun <T, R : IBaseResponse<T>> Flow<R>.asNetworkResult(
    showLoading: Boolean = true,
    loadingMessage: String = StringResourceHelper.getString(R.string.network_requesting)
): Flow<NetworkResult<T>> = flow {
    try {
        if (showLoading) {
            emit(NetworkResult.Loading(loadingMessage))
        }
        
        collect { response ->
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
        }
    } catch (e: Exception) {
        emit(NetworkResult.Error(
            error = e,
            code = -1,
            message = e.message ?: StringResourceHelper.getString(R.string.error_unknown_error)
        ))
    }
}.flowOn(Dispatchers.IO)

/**
 * Flow 扩展函数：处理错误
 */
fun <T> Flow<NetworkResult<T>>.handleError(
    onError: (NetworkResult.Error) -> Unit
): Flow<NetworkResult<T>> = catch { e ->
    onError(NetworkResult.Error(error = e, message = e.message ?: "未知错误"))
}

/**
 * Flow 扩展函数：只收集成功的数据
 */
suspend fun <T> Flow<NetworkResult<T>>.collectSuccess(
    onSuccess: (T) -> Unit
) {
    collect { result ->
        if (result is NetworkResult.Success) {
            onSuccess(result.data)
        }
    }
}

/**
 * Flow 扩展函数：只收集错误
 */
suspend fun <T> Flow<NetworkResult<T>>.collectError(
    onError: (NetworkResult.Error) -> Unit
) {
    collect { result ->
        if (result is NetworkResult.Error) {
            onError(result)
        }
    }
}

/**
 * Flow 扩展函数：只收集加载状态
 */
suspend fun <T> Flow<NetworkResult<T>>.collectLoading(
    onLoading: (NetworkResult.Loading) -> Unit
) {
    collect { result ->
        if (result is NetworkResult.Loading) {
            onLoading(result)
        }
    }
}

