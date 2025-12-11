package com.zfx.commonlib.mvi.example

import com.zfx.commonlib.mvi.*
import com.zfx.commonlib.network.repository.BaseRepository
import com.zfx.commonlib.network.result.NetworkResult
import com.zfx.commonlib.network.extension.getApiService
import com.zfx.commonlib.network.response.BaseResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import retrofit2.http.GET

/**
 * MVI 架构使用示例
 * 
 * 展示如何从 MVVM 迁移到 MVI
 */

// ==================== 1. 定义 State ====================

/**
 * 用户页面状态
 */
data class UserViewState(
    val userInfo: UserInfo? = null,
    val isLoading: Boolean = false,
    val loadingMessage: String = "加载中...",
    val error: com.zfx.commonlib.network.error.AppException? = null,
    val showError: Boolean = false
) : ViewState {
    companion object {
        fun initial() = UserViewState()
    }
}

// ==================== 2. 定义 Intent ====================

/**
 * 用户页面 Intent
 */
sealed class UserIntent : ViewIntent {
    /**
     * 初始化
     */
    object Init : UserIntent()
    
    /**
     * 刷新用户信息
     */
    object Refresh : UserIntent()
    
    /**
     * 更新用户名
     */
    data class UpdateUserName(val name: String) : UserIntent()
    
    /**
     * 清除错误
     */
    object ClearError : UserIntent()
}

// ==================== 3. 创建 Repository ====================

/**
 * 用户 Repository
 */
class UserRepository : BaseRepository() {
    
    private val apiService = getApiService<UserApiService>()
    
    fun getUserInfo(): Flow<NetworkResult<UserInfo>> {
        return requestFlow(
            apiCall = { apiService.getUserInfo() },
            showLoading = true,
            loadingMessage = "加载用户信息..."
        )
    }
}

// ==================== 4. 创建 ViewModel ====================

/**
 * 用户 ViewModel（MVI 版本）
 */
class UserMviViewModel : MviViewModel<UserIntent, UserViewState>() {
    
    private val repository = UserRepository()
    
    override fun initialState(): UserViewState {
        return UserViewState.initial()
    }
    
    override fun reduce(currentState: UserViewState, intent: UserIntent): UserViewState {
        return when (intent) {
            is UserIntent.Init -> {
                // 初始化时加载用户信息
                loadUserInfo()
                currentState.copy(isLoading = true, loadingMessage = "加载中...")
            }
            
            is UserIntent.Refresh -> {
                // 刷新用户信息
                loadUserInfo()
                currentState.copy(isLoading = true, loadingMessage = "刷新中...")
            }
            
            is UserIntent.UpdateUserName -> {
                // 更新用户名（本地状态更新）
                currentState.copy(
                    userInfo = currentState.userInfo?.copy(name = intent.name)
                )
            }
            
            is UserIntent.ClearError -> {
                // 清除错误
                currentState.copy(showError = false, error = null)
            }
        }
    }
    
    /**
     * 加载用户信息
     */
    private fun loadUserInfo() {
        handleNetworkResult(
            flow = repository.getUserInfo(),
            onLoading = { state, message ->
                state.copy(isLoading = true, loadingMessage = message)
            },
            onSuccess = { state, userInfo ->
                state.copy(
                    isLoading = false,
                    userInfo = userInfo,
                    showError = false,
                    error = null
                )
            },
            onError = { state, error ->
                val appException = com.zfx.commonlib.network.error.AppException(
                    error.code,
                    error.message
                )
                state.copy(
                    isLoading = false,
                    error = appException,
                    showError = true
                )
            }
        )
    }
}

// ==================== 5. 创建 Activity ====================

/**
 * 用户 Activity（MVI 版本）
 */
class UserMviActivity : MviActivity<UserMviViewModel, UserIntent, UserViewState>() {
    
    override fun layoutId(): Int {
        return 0 // 你的布局文件 ID
    }
    
    override fun initView(savedInstanceState: Bundle?) {
        // 初始化视图
        // findViewById<Button>(R.id.btnRefresh).setOnClickListener {
        //     dispatchIntent(UserIntent.Refresh)
        // }
    }
    
    override fun renderState(state: UserViewState) {
        // 根据状态更新 UI
        if (state.isLoading) {
            showLoading(state.loadingMessage)
        } else {
            dismissLoading()
        }
        
        state.userInfo?.let { userInfo ->
            // 更新用户信息显示
            // nameTextView.text = userInfo.name
            // emailTextView.text = userInfo.email
        }
        
        if (state.showError && state.error != null) {
            // 显示错误
            showError(state.error.errorMsg)
        }
    }
    
    override fun showLoading(message: String) {
        // 显示加载对话框
    }
    
    override fun dismissLoading() {
        // 隐藏加载对话框
    }
    
    override fun onResume() {
        super.onResume()
        // 页面显示时初始化
        dispatchIntent(UserIntent.Init)
    }
}

// ==================== 6. 对比：MVVM 版本 ====================

/**
 * 用户 ViewModel（MVVM 版本，用于对比）
 */
class UserMvvmViewModel : com.zfx.commonlib.base.viewmodel.BaseViewModel() {
    
    private val repository = UserRepository()
    
    val userInfo = androidx.lifecycle.MutableLiveData<UserInfo?>()
    val isLoading = androidx.lifecycle.MutableLiveData<Boolean>()
    val error = androidx.lifecycle.MutableLiveData<com.zfx.commonlib.network.error.AppException?>()
    
    fun loadUserInfo() {
        // MVVM 方式：直接调用方法
        repository.getUserInfo()
            .onEach { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        isLoading.value = true
                    }
                    is NetworkResult.Success -> {
                        isLoading.value = false
                        userInfo.value = result.data
                    }
                    is NetworkResult.Error -> {
                        isLoading.value = false
                        error.value = com.zfx.commonlib.network.error.AppException(
                            result.code,
                            result.message
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }
}

// 注意：这个示例需要导入 viewModelScope
// import androidx.lifecycle.viewModelScope

// ==================== 7. 数据类定义 ====================

data class UserInfo(
    val id: String,
    val name: String,
    val email: String
)

interface UserApiService {
    @retrofit2.http.GET("user/info")
    suspend fun getUserInfo(): BaseResponse<UserInfo>
}

