package com.zfx.commonlib.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * MVI ViewModel 基类
 * 
 * MVI 架构核心：
 * - ViewState: 不可变的状态对象
 * - ViewIntent: 用户意图
 * - Reducer: 处理 Intent 并生成新 State
 * 
 * 数据流：View -> Intent -> ViewModel -> State -> View
 * 
 * @param I Intent 类型
 * @param S State 类型
 */
abstract class MviViewModel<I : ViewIntent, S : ViewState> : ViewModel() {
    
    /**
     * Intent 流（用户操作）
     */
    private val _intent = MutableSharedFlow<I>(replay = 0, extraBufferCapacity = 64)
    
    /**
     * 当前状态
     */
    private val _state = MutableStateFlow<S>(initialState())
    
    /**
     * 状态流（对外暴露，只读）
     */
    val state: StateFlow<S> = _state.asStateFlow()
    
    /**
     * 单次事件流（用于 Toast、导航等一次性事件）
     */
    private val _singleEvent = MutableSharedFlow<SingleEvent>(replay = 0, extraBufferCapacity = 64)
    
    /**
     * 单次事件流（对外暴露，只读）
     */
    val singleEvent: SharedFlow<SingleEvent> = _singleEvent.asSharedFlow()
    
    init {
        // 处理 Intent 流
        viewModelScope.launch {
            _intent
                .onEach { intent ->
                    processIntent(intent)
                }
                .collect()
        }
    }
    
    /**
     * 获取初始状态
     */
    protected abstract fun initialState(): S
    
    /**
     * 处理 Intent
     * 子类可以重写此方法来自定义 Intent 处理逻辑
     */
    protected open fun processIntent(intent: I) {
        val currentState = _state.value
        val newState = reduce(currentState, intent)
        _state.value = newState
    }
    
    /**
     * Reducer 函数
     * 根据当前状态和 Intent 生成新状态
     * 
     * @param currentState 当前状态
     * @param intent 用户意图
     * @return 新状态
     */
    protected abstract fun reduce(currentState: S, intent: I): S
    
    /**
     * 发送 Intent
     * View 层调用此方法发送用户操作
     */
    fun dispatchIntent(intent: I) {
        viewModelScope.launch {
            _intent.emit(intent)
        }
    }
    
    /**
     * 更新状态
     * 用于直接更新状态（不通过 Intent）
     * protected：子类可以调用（包括其他模块的子类）
     */
    protected fun updateState(update: (S) -> S) {
        _state.value = update(_state.value)
    }
    
    /**
     * 更新状态（内部辅助方法）
     * 供同一模块内的扩展函数使用
     * internal：同一模块内可访问，扩展函数可以使用
     */
    internal fun updateStateInternal(update: (S) -> S) {
        updateState(update)
    }
    
    /**
     * 发送单次事件
     * 用于 Toast、导航等一次性事件
     */
    protected fun sendSingleEvent(event: SingleEvent) {
        viewModelScope.launch {
            _singleEvent.emit(event)
        }
    }
    
    /**
     * 获取当前状态
     */
    protected fun getCurrentState(): S = _state.value
}

/**
 * 单次事件接口
 * 用于 Toast、导航等一次性事件
 */
interface SingleEvent

/**
 * 基础单次事件实现
 */
sealed class BaseSingleEvent : SingleEvent {
    /**
     * 显示 Toast
     */
    data class ShowToast(val message: String) : BaseSingleEvent()
    
    /**
     * 显示错误提示
     */
    data class ShowError(val message: String) : BaseSingleEvent()
    
    /**
     * 导航事件
     */
    data class Navigate(val destination: String, val args: Map<String, Any>? = null) : BaseSingleEvent()
    
    /**
     * 关闭页面
     */
    object Finish : BaseSingleEvent()
}


