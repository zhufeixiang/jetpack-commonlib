package com.zfx.jetpacklib.feature.knowledge

import com.blankj.utilcode.util.ToastUtils
import com.zfx.commonlib.base.viewmodel.BaseViewModel
import com.zfx.commonlib.ext.collectResult
import com.zfx.commonlib.ext.collectResultWithLoading
import com.zfx.jetpacklib.data.KnowledgeItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.text.ifEmpty

class KnowledgeViewModel : BaseViewModel() {


    val repository = KnowledgeRepository()

    private val _treeList = MutableStateFlow<List<KnowledgeItem>>(emptyList())
    val treeList : StateFlow<List<KnowledgeItem>> = _treeList.asStateFlow()
    private val _showLoadingDialog = MutableStateFlow(false)
    val showLoadingDialog: StateFlow<Boolean> = _showLoadingDialog.asStateFlow()

    private val _loadingMessage = MutableStateFlow("")
    val loadingMessage: StateFlow<String> = _loadingMessage.asStateFlow()


    init {
        getTree()
    }

    private fun getTree() {
        val loadingMsg = "加载数据..."
        collectResultWithLoading(
            flow = repository.getKnowledgeTree(),
            showLoading = { showMsg ->
                // 显示全局 loading 对话框
                // 如果 showMsg 为空，使用自定义消息；否则使用传入的消息
                _loadingMessage.value = loadingMsg
                _showLoadingDialog.value = true
            },
            dismissLoading = {
                // 隐藏全局 loading 对话框
                _showLoadingDialog.value = false
            },
            onError = { error ->
                ToastUtils.showShort(error.message)
            },
            onSuccess = { treeList ->
                _treeList.value = treeList
            }
        )
    }

}