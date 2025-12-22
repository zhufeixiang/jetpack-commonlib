package com.zfx.jetpacklib.feature.home

import com.zfx.commonlib.network.extension.getApiService
import com.zfx.commonlib.network.repository.BaseRepository
import com.zfx.commonlib.network.result.NetworkResult
import com.zfx.jetpacklib.data.ArticlePageData
import com.zfx.jetpacklib.data.BannerItem
import com.zfx.jetpacklib.service.HomeService
import kotlinx.coroutines.flow.Flow

class HomeRepository : BaseRepository(){

    private val apiService: HomeService by lazy {
        getApiService<HomeService>()
    }


    /**
     * 获取banner数据
     * */
    fun getBannerList() : Flow<NetworkResult<List<BannerItem>>> {
        return requestFlow(
            apiCall = { apiService.getBanner() }
        )
    }

    /**
     * 分页获取文章数据
     * @param page 从0开始
     * */
    fun getArticleList(page : Int): Flow<NetworkResult<ArticlePageData>>{
        return requestFlow(
            apiCall = { apiService.getArticleList(page) }
        )
    }

}