package com.zfx.jetpacklib

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.sp
import com.blankj.utilcode.util.ToastUtils
import com.zfx.jetpacklib.feature.home.ui.HomeScreen
import com.zfx.jetpacklib.feature.knowledge.ui.KnowledgeScreen
import com.zfx.jetpacklib.feature.link.LinkScreen

class MainActivity : ComponentActivity(){
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            MainScreen(windowSizeClass)
        }
    }

    @Composable
    private fun MainScreen(windowSizeClass : WindowSizeClass) {
        when(windowSizeClass.widthSizeClass){
            WindowWidthSizeClass.Compact,
            WindowWidthSizeClass.Medium  ->{
                AppPortrait()
            }

            WindowWidthSizeClass.Expanded ->{
                AppLandscape()
            }
        }
    }

    /**
     * 横屏布局
     * */
    @Composable
    private fun AppLandscape() {

    }

    /**
     * 竖屏布局
     * */
    @Composable
    private fun AppPortrait() {
        var selectedTab by remember { mutableStateOf(0) }
        // 管理 LinkScreen 的显示状态
        var linkScreenData by remember { mutableStateOf<Pair<String, String>?>(null) }
        
        // 使用 AnimatedVisibility 实现类似 iOS 的滑动动画
        AnimatedVisibility(
            visible = linkScreenData != null,
            enter = slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth }, // 从右侧滑入
                animationSpec = tween(durationMillis = 300)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth }, // 向右侧滑出
                animationSpec = tween(durationMillis = 300)
            )
        ) {
            if (linkScreenData != null) {
                LinkScreen(
                    title = linkScreenData!!.first,
                    linkUrl = linkScreenData!!.second,
                    onBackClick = { linkScreenData = null }
                )
            }
        }
        
        AnimatedVisibility(
            visible = linkScreenData == null,
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
                    TopBar(selectedTab)
                },
                bottomBar = {
                    BottomNavigation(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it }
                    )
                }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    when (selectedTab) {
                        0 -> HomeScreen(
                            onArticleClick = { article ->
                                // 跳转到 LinkScreen
                                linkScreenData = Pair(article.title, article.link)
                            }
                        )  // 使用 HomeViewModel
                        1 -> KnowledgeScreen()  // 使用 KnowledgeViewModel
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
                    .clickable{ ToastUtils.showShort("点记了目录") },
                contentDescription = "目录",
            )

            Spacer(
                Modifier.size(24.dp)
            )

            Text(
                text = if(selectedTab == 0){
                    "玩Android"
                }else{
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
                    .clickable{ ToastUtils.showShort("点记了热点") },
                contentDescription = "热点"
            )

            Spacer(
                Modifier.size(24.dp)
            )

            Image(
                painter = painterResource(R.drawable.icon_search_white),
                modifier = Modifier
                    .size(24.dp)
                    .clickable{ ToastUtils.showShort("点记了搜索") },
                contentDescription = "搜索"
            )

            Spacer(
                Modifier.size(24.dp)
            )

        }
    }

    @Composable
    private fun BottomNavigation(modifier: Modifier = Modifier,selectedTab: Int, onTabSelected: (Int) -> Unit){
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


}