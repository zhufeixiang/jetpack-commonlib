package com.zfx.jetpacklib.feature.knowledge.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
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