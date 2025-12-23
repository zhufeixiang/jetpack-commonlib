package com.zfx.jetpacklib.service

import com.zfx.jetpacklib.data.ArticlePageData
import com.zfx.jetpacklib.data.BannerItem
import com.zfx.jetpacklib.network.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * 首页的相关的api
 * */
interface HomeService {

    @GET("banner/json")
    suspend fun getBanner() : ApiResponse<List<BannerItem>>

    @GET("article/list/{page}/json")
    suspend fun getArticleList(@Path("page") page : Int) : ApiResponse<ArticlePageData>
}