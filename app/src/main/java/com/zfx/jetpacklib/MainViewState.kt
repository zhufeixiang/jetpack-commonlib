package com.zfx.jetpacklib

import com.zfx.commonlib.mvi.ViewState

/**
 * MainActivity 的 ViewState
 */
data class MainViewState(
    /**
     * 当前选中的 Tab（0: 首页, 1: 知识体系）
     */
    val selectedTab: Int = 0,
    
    /**
     * LinkScreen 的数据（title, linkUrl），null 表示不显示
     */
    val linkScreenData: Pair<String, String>? = null
) : ViewState {
    companion object {
        fun initial() = MainViewState()
    }
}

