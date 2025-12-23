package com.zfx.jetpacklib.feature.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zfx.jetpacklib.R
import com.zfx.jetpacklib.data.Article


/**
 * 文章列表组件（支持分页加载）
 * 
 * @param modifier 修饰符
 * @param data 文章列表数据
 * @param isLoading 是否正在加载更多
 * @param hasMore 是否还有更多数据
 * @param header 列表头部内容（如 Banner）
 * @param onLoadMore 加载更多回调（当滚动到底部时触发）
 * @param onItemClick 文章项点击回调
 * @param onFavoriteClick 收藏点击回调
 */
@Composable
fun ArticleList(
    modifier: Modifier = Modifier.fillMaxWidth(),
    data: List<Article>,
    isLoading: Boolean = false,
    hasMore: Boolean = true,
    header: (@Composable () -> Unit)? = null,
    onLoadMore: () -> Unit = {},
    onItemClick: (Article) -> Unit = {},
    onFavoriteClick: (Article) -> Unit = {}
) {
    val listState = rememberLazyListState()
    
    // 监听滚动到底部，自动加载更多
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItemsCount = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            
            // 当滚动到倒数第 3 个 item 时，触发加载更多（提前加载，提升体验）
            totalItemsCount > 0 && lastVisibleItemIndex >= totalItemsCount - 3
        }
    }
    
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && !isLoading && hasMore) {
            onLoadMore()
        }
    }
    
    if (data.isEmpty() && header == null) {
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
    } else {
        LazyColumn(
            modifier = modifier,
            state = listState,
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            // 头部内容（如 Banner）
            if (header != null) {
                item {
                    header()
                }
            }
            
            // 文章列表项
            items(
                items = data,
                key = { article -> article.id } // 使用唯一 ID 作为 key，提升性能
            ) { article ->
                ArticleItem(
                    data = article,
                    favoriteClick = { onFavoriteClick(article) },
                    cardClick = { onItemClick(article) }
                )
            }
            
            // 加载更多指示器
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = colorResource(id = R.color.nav_selected)
                        )
                    }
                }
            } else if (!hasMore && data.isNotEmpty()) {
                // 没有更多数据提示
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "没有更多数据了",
                            fontSize = 14.sp,
                            color = colorResource(id = R.color.nav_unselected),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ArticleItem(
    modifier: Modifier = Modifier.fillMaxWidth(),
    data : Article,
    favoriteClick : () -> Unit,
    cardClick : () -> Unit
){

    Card(
        modifier = modifier
            .wrapContentHeight()
            .padding(PaddingValues(horizontal = 16.dp, vertical = 8.dp))
            .clickable { cardClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardColors(
            contentColor = colorResource(id = R.color.white),
            containerColor = colorResource(id = R.color.white),
            disabledContentColor = colorResource(id = R.color.white),
            disabledContainerColor = colorResource(id = R.color.white)
        )
    ) {
        Column(
            modifier = Modifier.padding(PaddingValues(horizontal = 16.dp, vertical = 8.dp)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.icon_article_logo),
                    contentDescription = "文章图标",
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .align(Alignment.CenterVertically),
                    text = if (data.author.isNotEmpty()){
                        "作者:${data.author}"
                    }else{
                        "分享人:${data.shareUser}"
                    },
                    fontSize = 12.sp,
                    color = colorResource(id = R.color.black)
                )
                Spacer(
                    modifier = Modifier.weight(1f)
                )
                Text(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    text = data.niceDate,
                    fontSize = 12.sp,
                    color = colorResource(id = R.color.nav_unselected)
                )
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                text = data.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.black)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterVertically),
                    text = "${data.chapterName}/${data.superChapterName}",
                    fontSize = 12.sp,
                    color = colorResource(id = R.color.nav_selected)
                )
                Spacer(
                    modifier = Modifier.weight(1f)
                )
                Image(
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { favoriteClick() },
                    painter = painterResource(id = if(data.collect){
                        R.drawable.icon_heart_blue
                    }else{
                        R.drawable.icon_heart_grey
                    }),
                    contentDescription = "收藏图标"
                )
            }

        }
    }
}

// Preview 辅助对象：提供预览用的 Article 数据
private object PreviewData {
    val article1 = Article(
        id = 1,
        author = "作者名称",
        niceDate = "2024-01-15",
        title = "这是一篇测试文章的标题，可能会比较长，用来测试文本显示效果",
        chapterName = "Android",
        superChapterName = "基础",
        collect = false
    )
    
    val article2 = Article(
        id = 2,
        author = "另一个作者",
        niceDate = "2024-01-14",
        title = "第二篇测试文章",
        chapterName = "Kotlin",
        superChapterName = "进阶",
        collect = true
    )
    
    val articleList = listOf(article1, article2)
}

@Preview
@Composable
private fun ArticleItemPreview() {
    ArticleItem(
        data = PreviewData.article1,
        favoriteClick = { },
        cardClick = { }
    )
}

@Preview
@Composable
private fun ArticleListPreview() {
    ArticleList(
        data = PreviewData.articleList,
        isLoading = false,
        hasMore = true,
        onLoadMore = { },
        onItemClick = { },
        onFavoriteClick = { }
    )
}

