package com.zfx.jetpacklib.feature.knowledge.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zfx.jetpacklib.R
import com.zfx.jetpacklib.data.KnowledgeItem

@Composable
fun KnowledgeList(
    modifier: Modifier = Modifier.fillMaxSize(),
    treeList : List<KnowledgeItem>,
    onItemClick: (KnowledgeItem) -> Unit
){
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
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            items(
                items = treeList,
                key = { tree -> tree.id } // 使用唯一 ID 作为 key，提升性能
            ) { tree ->
                KnowledgeItem(
                    data = tree,
                    cardClick = { onItemClick(tree) }
                )
            }
        }
    }






}


@Composable
fun KnowledgeItem(
    modifier: Modifier = Modifier.fillMaxWidth(),
    data : KnowledgeItem,
    cardClick : () -> Unit
){
    Card(
        modifier = modifier
            .wrapContentHeight()
            .heightIn(min = 60.dp)  // 设置最小高度为 60dp
            .padding(PaddingValues(horizontal = 16.dp, vertical = 8.dp))
            .clickable { cardClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardColors(
            containerColor = colorResource(id = R.color.white),
            contentColor = colorResource(id = R.color.white),
            disabledContentColor = colorResource(id = R.color.white),
            disabledContainerColor = colorResource(id = R.color.white)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(PaddingValues(horizontal = 8.dp, vertical = 8.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                Modifier.weight(1f)
            ) {

                Text(
                    text = "${data.name}",
                    color = colorResource(id = R.color.black),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal
                )
                Spacer(
                    modifier = Modifier.size(8.dp)
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalArrangement = Arrangement.Top
                ) {
                    data.children.forEach { child ->
                        Text(
                            modifier = Modifier.padding(PaddingValues(horizontal = 2.dp, vertical = 2.dp)),
                            text = child.name,
                            color = colorResource(id = R.color.nav_unselected),
                            fontSize = 12.sp
                        )
                    }
                }

            }

            Image(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.CenterVertically),
                painter = painterResource(id = R.drawable.icon_arrow_right_gray),
                contentDescription = "向右箭头"
            )
        }
    }
}