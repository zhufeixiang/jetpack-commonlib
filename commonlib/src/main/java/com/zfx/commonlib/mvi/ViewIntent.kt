package com.zfx.commonlib.mvi

import com.zfx.commonlib.network.error.AppException

/**
 * ViewIntent 接口
 * MVI 架构中的用户意图接口，所有用户操作都应该封装为 Intent
 * 
 * 原则：
 * 1. Intent 应该是不可变的（immutable）
 * 2. 每个用户操作都应该对应一个 Intent
 * 3. Intent 应该包含执行操作所需的所有数据
 */
interface ViewIntent

/**
 * 空 Intent
 */
object EmptyIntent : ViewIntent

/**
 * 基础 Intent 实现
 * 包含常见的用户操作
 */
sealed class BaseIntent : ViewIntent {
    /**
     * 初始化 Intent
     */
    object Init : BaseIntent()
    
    /**
     * 刷新 Intent
     */
    object Refresh : BaseIntent()
    
    /**
     * 重试 Intent
     */
    object Retry : BaseIntent()
    
    /**
     * 加载更多 Intent
     */
    object LoadMore : BaseIntent()
    
    /**
     * 错误处理 Intent
     */
    data class HandleError(val error: AppException) : BaseIntent()
    
    /**
     * 清除错误 Intent
     */
    object ClearError : BaseIntent()
}

