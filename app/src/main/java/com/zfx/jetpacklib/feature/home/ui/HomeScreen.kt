package com.zfx.jetpacklib.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zfx.jetpacklib.feature.home.HomeViewModel
import com.zfx.jetpacklib.widget.Banner


@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
){
    Column(
       modifier = modifier
           .fillMaxWidth()
           .fillMaxHeight()
    ) {

        val bannerList  by viewModel.bannerList.collectAsState()
        val articleList by viewModel.articleList.collectAsState()


        if (bannerList.isNotEmpty()){
            Banner(
                items = bannerList,
                indicator = { curPage , pageCount ->
                    Row {
                        repeat(pageCount){ index ->
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
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

        if (articleList.isNotEmpty()){
            ArticleList(
                data = articleList
            )
        }
    }
}