package com.zfx.commonlib.mvi

/**
 * ViewState 接口
 * MVI 架构中的状态接口，所有状态都应该实现此接口
 * 
 * 原则：
 * 1. 状态应该是不可变的（immutable）
 * 2. 所有 UI 相关的状态都应该包含在一个 State 对象中
 * 3. 状态更新应该通过创建新对象而不是修改现有对象
 */
interface ViewState {
    /**
     * 默认状态（可选）
     */
    companion object {
        /**
         * 创建空状态
         */
        fun empty(): ViewState = EmptyState
    }
}

/**
 * 空状态实现
 */
object EmptyState : ViewState

/**
 * 基础 ViewState 实现
 * 包含常见的状态属性
 */
data class BaseViewState(
    /**
     * 是否加载中
     */
    val isLoading: Boolean = false,
    
    /**
     * 加载提示信息
     */
    val loadingMessage: String = "加载中...",
    
    /**
     * 错误信息
     */
    val error: com.zfx.commonlib.network.error.AppException? = null,
    
    /**
     * 是否显示错误
     */
    val showError: Boolean = false
) : ViewState {
    /**
     * 显示加载状态
     */
    fun showLoading(message: String = "加载中..."): BaseViewState {
        return copy(
            isLoading = true,
            loadingMessage = message,
            showError = false,
            error = null
        )
    }
    
    /**
     * 隐藏加载状态
     */
    fun hideLoading(): BaseViewState {
        return copy(isLoading = false)
    }
    
    /**
     * 显示错误
     */
    fun showError(error: com.zfx.commonlib.network.error.AppException): BaseViewState {
        return copy(
            isLoading = false,
            error = error,
            showError = true
        )
    }
    
    /**
     * 隐藏错误
     */
    fun hideError(): BaseViewState {
        return copy(
            showError = false,
            error = null
        )
    }
    
    /**
     * 成功状态（清除所有错误和加载状态）
     */
    fun success(): BaseViewState {
        return copy(
            isLoading = false,
            showError = false,
            error = null
        )
    }
}

