package com.zfx.jetpacklib.feature.home

import com.blankj.utilcode.util.ToastUtils
import com.zfx.commonlib.base.viewmodel.BaseViewModel
import com.zfx.commonlib.ext.collectResult
import com.zfx.jetpacklib.data.Article
import com.zfx.jetpacklib.data.BannerItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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



}