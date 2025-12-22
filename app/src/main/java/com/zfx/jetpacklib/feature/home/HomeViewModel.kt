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

    var curPage = 0

    init {
        loadData()
    }

    private fun loadData() {
        curPage = 0
        getBannerList()
        getArticleList()
    }

    private fun getArticleList() {
        collectResult(
            flow = repository.getArticleList(curPage),
            onSuccess = { articleData ->
                _articleList.value = articleData.datas
            },
            onError = { error ->
                val errorMsg = error.message ?: "Unknown error"
                android.util.Log.e("HomeViewModel", "加载文章列表失败: $errorMsg", error.error)
                ToastUtils.showShort(errorMsg)
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