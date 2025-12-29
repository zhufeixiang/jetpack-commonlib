package com.zfx.jetpacklib

import androidx.compose.runtime.Composable
import com.blankj.utilcode.util.ToastUtils
import com.zfx.commonlib.mvi.BaseSingleEvent
import com.zfx.commonlib.mvi.compose.BaseComposeMviActivity
import com.zfx.jetpacklib.data.Article
import com.zfx.jetpacklib.ui.MainContent

/**
 * MainActivity 的 MVI 版本
 * 继承 BaseComposeMviActivity，使用 MVI 架构
 */
class MainMviActivity : BaseComposeMviActivity<MainMviViewModel, MainIntent, MainViewState>() {
    
    override fun showToast(message: String) {
        ToastUtils.showShort(message)
    }
    
    override fun showError(message: String) {
        ToastUtils.showShort(message)
    }
    
    @Composable
    override fun Render(state: MainViewState, dispatch: (MainIntent) -> Unit) {
        MainContent(
            selectedTab = state.selectedTab,
            onTabSelected = { tabIndex ->
                dispatch(MainIntent.SelectTab(tabIndex))
            },
            linkScreenData = state.linkScreenData,
            onCloseLinkScreen = {
                dispatch(MainIntent.CloseLinkScreen)
            },
            onArticleClick = { article ->
                dispatch(MainIntent.OpenArticle(article))
            }
        )
    }
}

