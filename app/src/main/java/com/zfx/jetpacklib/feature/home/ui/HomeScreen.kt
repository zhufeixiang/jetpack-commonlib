package com.zfx.jetpacklib.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blankj.utilcode.util.ToastUtils
import com.zfx.jetpacklib.feature.home.HomeViewModel
import com.zfx.jetpacklib.data.Article
import com.zfx.commonlib.ext.compose.Banner


@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
    onArticleClick: (Article) -> Unit = {}
){
    val bannerList  by viewModel.bannerList.collectAsState()
    val articleList by viewModel.articleList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val hasMore by viewModel.hasMore.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    ArticleList(
        modifier = modifier.fillMaxWidth(),
        data = articleList,
        isLoading = isLoading,
        hasMore = hasMore,
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refresh() },
        header = if (bannerList.isNotEmpty()) {
            {
                Banner(
                    modifier = Modifier.height(180.dp),
                    items = bannerList,
                    showIndicator = false,
                    indicator = { curPage , pageCount ->
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            repeat(pageCount){ index ->
                                Spacer(
                                    modifier = Modifier.size(1.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (index == curPage) Color.White else Color.Gray
                                        )
                                )
                            }
                        }
                    },
                    pageContent = { pageItem ->
                        BannerContent(pageData = pageItem)
                    }
                )
            }
        } else null,
        onLoadMore = { viewModel.loadMore() },
        onFavoriteClick = { article ->
            ToastUtils.showShort("收藏文章：${article.title}")
        },
        onItemClick = { article ->
            onArticleClick(article)
        }
    )
}