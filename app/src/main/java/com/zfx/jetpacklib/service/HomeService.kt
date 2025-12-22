package com.zfx.jetpacklib.service

import com.zfx.commonlib.network.response.BaseResponse
import com.zfx.jetpacklib.data.ArticlePageData
import com.zfx.jetpacklib.data.BannerItem
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * 首页的相关的api
 * */
interface HomeService {

    @GET("banner/json")
    suspend fun getBanner() : BaseResponse<List<BannerItem>>

    @GET("article/list/{page}/json")
    suspend fun getArticleList(@Path("page") page : Int) : BaseResponse<ArticlePageData>
}