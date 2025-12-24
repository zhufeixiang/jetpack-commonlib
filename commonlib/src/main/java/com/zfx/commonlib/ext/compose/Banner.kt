package com.zfx.commonlib.ext.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * @param items banner数据
 * @param autoPlay 是否自动播放
 * @param interval 播放间隔（毫秒） 默认2000
 * @param showIndicator 是否显示指示器 默认true
 * @param indicator 指示器
 * @param pageContent 每个page的view
 * */
@Composable
fun <T> Banner(
    modifier: Modifier = Modifier,
    items : List<T>,
    autoPlay : Boolean = true,
    interval : Long = 2000,
    showIndicator : Boolean = true,
    indicator : (@Composable (currentPage : Int,pageCount : Int) -> Unit)? = null,
    pageContent : @Composable (T) -> Unit
){
    // 空列表检查
    if (items.isEmpty()) {
        return
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { items.size }
    )

    // 自动播放逻辑
    LaunchedEffect(pagerState, autoPlay, interval) {
        if (items.size <= 1) {
            return@LaunchedEffect
        }
        
        while (autoPlay) {
            delay(interval)
            if (!pagerState.isScrollInProgress) {
                val nextPage = (pagerState.currentPage + 1) % items.size
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    Box(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            pageContent(items[page])
        }
        if (showIndicator && indicator != null){
            // indicator 覆盖在 content 上方，默认底部居中
            // 距离底部 52dp（避开底部标题栏 36dp + 间距 16dp）
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                indicator(pagerState.currentPage, items.size)
            }
        }

    }
}