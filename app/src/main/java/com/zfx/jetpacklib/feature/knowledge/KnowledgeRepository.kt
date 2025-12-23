package com.zfx.jetpacklib.feature.knowledge

import com.zfx.commonlib.network.extension.getApiService
import com.zfx.commonlib.network.repository.BaseRepository
import com.zfx.commonlib.network.result.NetworkResult
import com.zfx.jetpacklib.data.KnowledgeItem
import com.zfx.jetpacklib.service.KnowledgeService
import kotlinx.coroutines.flow.Flow

class KnowledgeRepository : BaseRepository() {


    val apiService by lazy {
        getApiService<KnowledgeService>()
    }

    /**
     * 知识体系
     * */
    fun getKnowledgeTree() : Flow<NetworkResult<List<KnowledgeItem>>> {
        return requestFlow(
            apiCall = { apiService.getKnowledgeTree() },
        )
    }



}