package com.zfx.jetpacklib.feature.knowledge.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blankj.utilcode.util.ToastUtils
import com.zfx.jetpacklib.R
import com.zfx.jetpacklib.feature.knowledge.KnowledgeViewModel


@Composable
fun KnowledgeScreen(
    modifier: Modifier = Modifier.fillMaxSize(),
    viewModel : KnowledgeViewModel = viewModel()
){

    val treeList by viewModel.treeList.collectAsState()
    val showLoadingDialog by viewModel.showLoadingDialog.collectAsState()
    val loadingMessage by viewModel.loadingMessage.collectAsState()

    // Loading 对话框（菊花 loading + 自定义消息）
    if (showLoadingDialog) {
        AlertDialog(
            onDismissRequest = { /* 不允许点击外部关闭 */ },
            title = null,
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 菊花 loading（CircularProgressIndicator）
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = androidx.compose.ui.res.colorResource(id = R.color.nav_selected),
                        strokeWidth = 4.dp
                    )
                    // 自定义 loading 消息（如果有）
                    if (loadingMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = loadingMessage,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            color = androidx.compose.ui.res.colorResource(id = R.color.nav_unselected)
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {},
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        )
    }

    if (treeList.isEmpty()){
        // 空列表状态（只有在没有 header 时才显示）
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "暂无数据",
                fontSize = 16.sp,
                color = colorResource(id = R.color.nav_unselected),
                textAlign = TextAlign.Center
            )
        }

    }else{
        KnowledgeList(
            modifier = modifier,
            treeList = treeList,
            onItemClick = { tree ->
                ToastUtils.showShort("点击了${tree.name}")
            }
        )
    }

}