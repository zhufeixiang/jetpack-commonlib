package com.zfx.jetpacklib

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import com.zfx.jetpacklib.feature.home.ui.HomeScreen
import com.zfx.jetpacklib.feature.knowledge.ui.KnowledgeScreen

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
        Scaffold(
            bottomBar = {
                BottomNavigation(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                when (selectedTab) {
                    0 -> HomeScreen()  // 使用 HomeViewModel
                    1 -> KnowledgeScreen()  // 使用 KnowledgeViewModel
                }
            }
        }

    }

    @Composable
    private fun BottomNavigation(modifier: Modifier = Modifier,selectedTab: Int, onTabSelected: (Int) -> Unit){
        NavigationBar(
            modifier = modifier
        ) {
            NavigationBarItem(
                selected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                icon = {
                    Icon(
                        painter = painterResource(
                            id = if (selectedTab == 0) {
                                R.drawable.icon_menu_home_blue
                            } else {
                                R.drawable.icon_menu_home_grey
                            }
                        ),
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
                }
            )
            NavigationBarItem(
                selected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                icon = {
                    Icon(
                        painter = painterResource(
                            id = if (selectedTab == 1) {
                                R.drawable.icon_menu_knowledge_blue
                            } else {
                                R.drawable.icon_menu_knowledge_grey
                            }
                        ),
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
                }
            )
        }
    }


}