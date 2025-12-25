package com.zfx.jetpacklib.feature.home

import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.ToastUtils
import com.zfx.commonlib.base.viewmodel.BaseViewModel
import com.zfx.commonlib.ext.collectResult
import com.zfx.commonlib.network.result.NetworkResult
import com.zfx.jetpacklib.data.Article
import com.zfx.jetpacklib.data.BannerItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : BaseViewModel() {

    val repository = HomeRepository()

    private val _bannerList = MutableStateFlow<List<BannerItem>>(emptyList())
    val bannerList : StateFlow<List<BannerItem>> = _bannerList.asStateFlow()


    private val _articleList = MutableStateFlow<List<Article>>(emptyList())
    val articleList : StateFlow<List<Article>> = _articleList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // 示例：使用 NetworkResult 作为 UI 状态（可选，演示用法）
    // 如果使用这种方式，可以统一管理所有状态
    private val _articleListState = MutableStateFlow<NetworkResult<List<Article>>>(NetworkResult.loading())
    val articleListState: StateFlow<NetworkResult<List<Article>>> = _articleListState.asStateFlow()

    var curPage = 0

    init {
        loadData()
    }

    /**
     * 加载数据（初始加载或刷新）
     */
    private fun loadData() {
        curPage = 0
        getBannerList()
        getArticleList(isRefresh = true)
    }

    /**
     * 刷新数据
     */
    fun refresh() {
        loadData()
    }

    /**
     * 加载更多
     */
    fun loadMore() {
        if (_isLoading.value || !_hasMore.value) {
            return
        }
        curPage++
        getArticleList(isRefresh = false)
    }

    private fun getArticleList(isRefresh: Boolean = false) {
        if (isRefresh) {
            _isRefreshing.value = true
        } else {
            _isLoading.value = true
        }

        collectResult(
            flow = repository.getArticleList(curPage),
            onSuccess = { articleData ->
                if (isRefresh) {
                    // 刷新：替换数据
                    _articleList.value = articleData.datas
                    _isRefreshing.value = false
                } else {
                    // 加载更多：追加数据并去重（根据 ID）
                    val existingIds = _articleList.value.map { it.id }.toSet()
                    val newArticles = articleData.datas.filter { it.id !in existingIds }
                    _articleList.value = _articleList.value + newArticles
                    _isLoading.value = false
                }
                // 判断是否还有更多数据
                _hasMore.value = !articleData.over
            },
            onError = { error ->
                val errorMsg = error.message ?: "Unknown error"
                android.util.Log.e("HomeViewModel", "加载文章列表失败: $errorMsg", error.error)
                ToastUtils.showShort(errorMsg)
                if (isRefresh) {
                    _isRefreshing.value = false
                } else {
                    _isLoading.value = false
                    // 加载失败时，回退页码
                    curPage--
                }
            }
        )
    }

    fun getBannerList(){
        collectResult(
            flow = repository.getBannerList(),
            onSuccess = { bannerList ->
                _bannerList.value = bannerList
            },
            onError = { error ->
                val errorMsg = error.message ?: "Unknown error"
                android.util.Log.e("HomeViewModel", "加载Banner失败: $errorMsg", error.error)
                ToastUtils.showShort(errorMsg)
            }
        )
    }

    /**
     * 示例方法：演示如何使用 NetworkResult.loading() 和 NetworkResult.error()
     * 这是一个可选的方法，展示如何手动管理 NetworkResult 状态
     */
    fun loadArticleListWithManualState() {
        // 1. 使用 NetworkResult.loading() 手动设置 Loading 状态
        _articleListState.value = NetworkResult.loading()
        
        // 或者使用自定义消息
        // _articleListState.value = NetworkResult.Loading("正在加载文章列表...")
        
        collectResult(
            flow = repository.getArticleList(0),
            onSuccess = { articleData ->
                // 2. 设置成功状态
                _articleListState.value = NetworkResult.Success(articleData.datas)
            },
            onError = { error ->
                // 3. 使用 NetworkResult.error() 创建错误状态（使用默认消息）
                _articleListState.value = NetworkResult.error(
                    error = error.error,
                    code = error.code
                )
                
                // 或者使用自定义错误消息
                // _articleListState.value = NetworkResult.Error(
                //     error = error.error,
                //     code = error.code,
                //     message = "加载失败：${error.message}"
                // )
                
                val errorMsg = error.message ?: "Unknown error"
                android.util.Log.e("HomeViewModel", "加载文章列表失败: $errorMsg", error.error)
                ToastUtils.showShort(errorMsg)
            }
        )
    }

    /**
     * 示例方法：在 try-catch 中使用 NetworkResult.error()
     */
    fun loadDataWithTryCatch() {
        viewModelScope.launch {
            try {
                // 手动设置 loading 状态
                _articleListState.value = NetworkResult.loading()
                
                // 执行网络请求（这里只是示例，实际应该使用 repository）
                // val articleData = repository.getArticleList(0)
                // _articleListState.value = NetworkResult.Success(articleData.datas)
                
            } catch (e: Exception) {
                // 使用 NetworkResult.error() 创建错误状态
                _articleListState.value = NetworkResult.error(
                    error = e,
                    code = -1
                )
                
                android.util.Log.e("HomeViewModel", "加载失败", e)
                ToastUtils.showShort("加载失败：${e.message}")
            }
        }
    }



}