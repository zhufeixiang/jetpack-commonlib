package com.zfx.commonlib.mvi.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.zfx.commonlib.mvi.BaseSingleEvent
import com.zfx.commonlib.mvi.MviViewModel
import com.zfx.commonlib.mvi.SingleEvent
import com.zfx.commonlib.mvi.ViewIntent
import com.zfx.commonlib.mvi.ViewState

/**
 * Compose + MVI Fragment 基类
 * - 单向数据流：View -> Intent -> ViewModel -> State -> View
 * - 使用 Compose 渲染 UI
 */
abstract class BaseComposeMviFragment<VM : MviViewModel<I, S>, I : ViewIntent, S : ViewState> : Fragment() {

    protected val viewModel: VM by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val state by viewModel.state.collectAsState(initial = viewModel.state.value)
                ObserveSingleEvents()
                Render(state = state) { intent ->
                    viewModel.dispatchIntent(intent)
                }
            }
        }
    }

    /**
     * 渲染 UI
     * @param state 当前状态
     * @param dispatch 发送 Intent 的函数
     */
    @Composable
    protected abstract fun Render(state: S, dispatch: (I) -> Unit)

    /**
     * 处理单次事件（Toast、导航等）
     */
    @Composable
    protected open fun ObserveSingleEvents() {
        LaunchedEffect(Unit) {
            viewModel.singleEvent.collect { event ->
                handleSingleEvent(event)
            }
        }
    }

    /**
     * 处理单次事件，子类可重写
     */
    protected open fun handleSingleEvent(event: SingleEvent) {
        when (event) {
            is BaseSingleEvent.ShowToast -> showToast(event.message)
            is BaseSingleEvent.ShowError -> showError(event.message)
            is BaseSingleEvent.Navigate -> navigate(event.destination, event.args)
            is BaseSingleEvent.Finish -> activity?.finish()
        }
    }

    /**
     * 显示 Toast（默认空实现）
     */
    protected open fun showToast(message: String) {}

    /**
     * 显示错误（默认空实现）
     */
    protected open fun showError(message: String) {}

    /**
     * 导航（默认空实现）
     */
    protected open fun navigate(destination: String, args: Map<String, Any>? = null) {}
}
