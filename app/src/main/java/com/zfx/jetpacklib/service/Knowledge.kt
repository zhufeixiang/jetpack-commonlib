package com.zfx.jetpacklib.service

import com.zfx.jetpacklib.data.KnowledgeItem
import com.zfx.jetpacklib.network.ApiResponse
import retrofit2.http.GET

/**
 * 知识体系相关的api
 * */
interface KnowledgeService {
    
    /**
     * 获取知识体系树
     * @return 知识体系列表（树形结构）
     */
    @GET("tree/json")
    suspend fun getKnowledgeTree(): ApiResponse<List<KnowledgeItem>>
}