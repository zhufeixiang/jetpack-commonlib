package com.zfx.jetpacklib.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.activity.ComponentActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blankj.utilcode.util.ToastUtils
import com.zfx.jetpacklib.R
import com.zfx.jetpacklib.data.Article
import com.zfx.jetpacklib.feature.home.ui.HomeScreen
import com.zfx.jetpacklib.feature.knowledge.ui.KnowledgeScreen
import com.zfx.jetpacklib.feature.link.LinkScreen

/**
 * MainContent - 主界面内容（可复用的 Composable）
 * 用于 MainActivity 和 MainMviActivity
 * 
 * @param selectedTab 当前选中的 Tab（0: 首页, 1: 知识体系），如果为 null 则使用内部状态管理（MVVM 模式）
 * @param onTabSelected Tab 切换回调，如果为 null 则使用内部状态管理（MVVM 模式）
 * @param linkScreenData LinkScreen 的数据（title, linkUrl），如果为 null 则使用内部状态管理（MVVM 模式）
 * @param onCloseLinkScreen 关闭 LinkScreen 回调，如果为 null 则使用内部状态管理（MVVM 模式）
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun MainContent(
    modifier: Modifier = Modifier,
    selectedTab: Int? = null,
    onTabSelected: ((Int) -> Unit)? = null,
    linkScreenData: Pair<String, String>? = null,
    onCloseLinkScreen: (() -> Unit)? = null,
    onArticleClick: (Article) -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val windowSizeClass = calculateWindowSizeClass(activity)
    
    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact,
        WindowWidthSizeClass.Medium -> {
            AppPortrait(
                modifier = modifier,
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                linkScreenData = linkScreenData,
                onCloseLinkScreen = onCloseLinkScreen,
                onArticleClick = onArticleClick
            )
        }
        WindowWidthSizeClass.Expanded -> {
            AppLandscape(
                modifier = modifier
            )
        }
    }
}

/**
 * 横屏布局
 */
@Composable
private fun AppLandscape(
    modifier: Modifier = Modifier
) {
    // 横屏布局暂未实现
}

/**
 * 竖屏布局
 */
@Composable
private fun AppPortrait(
    modifier: Modifier = Modifier,
    selectedTab: Int? = null,
    onTabSelected: ((Int) -> Unit)? = null,
    linkScreenData: Pair<String, String>? = null,
    onCloseLinkScreen: (() -> Unit)? = null,
    onArticleClick: (Article) -> Unit = {}
) {
    // 如果外部没有提供状态，则使用内部状态管理（MVVM 模式）
    var internalSelectedTab by remember { mutableStateOf(0) }
    var internalLinkScreenData by remember { mutableStateOf<Pair<String, String>?>(null) }
    
    val currentSelectedTab = selectedTab ?: internalSelectedTab
    val currentLinkScreenData = linkScreenData ?: internalLinkScreenData
    
    val handleTabSelected = { tabIndex: Int ->
        if (onTabSelected != null) {
            onTabSelected(tabIndex)
        } else {
            internalSelectedTab = tabIndex
        }
    }
    
    val handleCloseLinkScreen = {
        if (onCloseLinkScreen != null) {
            onCloseLinkScreen()
        } else {
            internalLinkScreenData = null
        }
    }
    
    // 使用 AnimatedVisibility 实现类似 iOS 的滑动动画
    AnimatedVisibility(
        visible = currentLinkScreenData != null,
        enter = slideInHorizontally(
            initialOffsetX = { fullWidth -> fullWidth }, // 从右侧滑入
            animationSpec = tween(durationMillis = 300)
        ),
        exit = slideOutHorizontally(
            targetOffsetX = { fullWidth -> fullWidth }, // 向右侧滑出
            animationSpec = tween(durationMillis = 300)
        )
    ) {
        if (currentLinkScreenData != null) {
            LinkScreen(
                title = currentLinkScreenData!!.first,
                linkUrl = currentLinkScreenData!!.second,
                onBackClick = handleCloseLinkScreen
            )
        }
    }
    
    AnimatedVisibility(
        visible = currentLinkScreenData == null,
        enter = slideInHorizontally(
            initialOffsetX = { fullWidth -> -fullWidth }, // 从左侧滑入（返回时）
            animationSpec = tween(durationMillis = 300)
        ),
        exit = slideOutHorizontally(
            targetOffsetX = { fullWidth -> -fullWidth }, // 向左侧滑出（跳转时）
            animationSpec = tween(durationMillis = 300)
        )
    ) {
        Scaffold(
            topBar = {
                TopBar(currentSelectedTab)
            },
            bottomBar = {
                BottomNavigation(
                    selectedTab = currentSelectedTab,
                    onTabSelected = handleTabSelected
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                when (currentSelectedTab) {
                    0 -> HomeScreen(
                        onArticleClick = { article ->
                            // 跳转到 LinkScreen
                            if (onCloseLinkScreen != null) {
                                // MVI 模式：通过回调通知外部，外部会 dispatch Intent
                                onArticleClick(article)
                            } else {
                                // MVVM 模式：直接更新内部状态
                                internalLinkScreenData = Pair(article.title, article.link)
                            }
                        }
                    )
                    1 -> KnowledgeScreen()
                }
            }
        }
    }
}

@Composable
fun TopBar(selectedTab: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(
                color = colorResource(R.color.theme)
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(
            Modifier.size(24.dp)
        )

        Image(
            painter = painterResource(R.drawable.icon_menu_white),
            modifier = Modifier
                .size(24.dp)
                .clickable { ToastUtils.showShort("点记了目录") },
            contentDescription = "目录",
        )

        Spacer(
            Modifier.size(24.dp)
        )

        Text(
            text = if (selectedTab == 0) {
                "玩Android"
            } else {
                "知识体系"
            },
            color = colorResource(R.color.white),
            fontSize = 18.sp
        )

        Spacer(
            Modifier.weight(1f)
        )

        Image(
            painter = painterResource(R.drawable.icon_hot_white),
            modifier = Modifier
                .size(24.dp)
                .clickable { ToastUtils.showShort("点记了热点") },
            contentDescription = "热点"
        )

        Spacer(
            Modifier.size(24.dp)
        )

        Image(
            painter = painterResource(R.drawable.icon_search_white),
            modifier = Modifier
                .size(24.dp)
                .clickable { ToastUtils.showShort("点记了搜索") },
            contentDescription = "搜索"
        )

        Spacer(
            Modifier.size(24.dp)
        )
    }
}

@Composable
private fun BottomNavigation(
    modifier: Modifier = Modifier,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        modifier = modifier.height(56.dp)
    ) {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.icon_menu_home_grey),
                    contentDescription = null
                )
            },
            label = {
                Text(
                    "首页",
                    color = colorResource(
                        id = if (selectedTab == 0) {
                            R.color.nav_selected
                        } else {
                            R.color.nav_unselected
                        }
                    )
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = colorResource(id = R.color.nav_selected),
                selectedTextColor = colorResource(id = R.color.nav_selected),
                indicatorColor = Color.Transparent,
                unselectedIconColor = colorResource(id = R.color.nav_unselected),
                unselectedTextColor = colorResource(id = R.color.nav_unselected)
            )
        )
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.icon_menu_knowledge_grey),
                    contentDescription = null
                )
            },
            label = {
                Text(
                    "知识体系",
                    color = colorResource(
                        id = if (selectedTab == 1) {
                            R.color.nav_selected
                        } else {
                            R.color.nav_unselected
                        }
                    )
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = colorResource(id = R.color.nav_selected),
                selectedTextColor = colorResource(id = R.color.nav_selected),
                indicatorColor = Color.Transparent,
                unselectedIconColor = colorResource(id = R.color.nav_unselected),
                unselectedTextColor = colorResource(id = R.color.nav_unselected)
            )
        )
    }
}

