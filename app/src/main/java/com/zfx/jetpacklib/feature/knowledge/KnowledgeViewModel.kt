package com.zfx.jetpacklib.feature.knowledge

import com.blankj.utilcode.util.ToastUtils
import com.zfx.commonlib.base.viewmodel.BaseViewModel
import com.zfx.commonlib.ext.collectResult
import com.zfx.jetpacklib.data.KnowledgeItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class KnowledgeViewModel : BaseViewModel() {


    val repository = KnowledgeRepository()

    private val _treeList = MutableStateFlow<List<KnowledgeItem>>(emptyList())
    val treeList : StateFlow<List<KnowledgeItem>> = _treeList.asStateFlow()


    init {
        getTree()
    }

    private fun getTree() {
        collectResult(
            flow = repository.getKnowledgeTree(),
            onError = { error ->
                ToastUtils.showShort(error.message)
            },
            onSuccess = { treeList ->
                _treeList.value = treeList
            }
        )
    }

}