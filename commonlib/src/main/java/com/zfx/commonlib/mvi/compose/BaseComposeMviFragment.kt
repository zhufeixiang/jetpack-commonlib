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
import androidx.lifecycle.ViewModelProvider
import com.zfx.commonlib.ext.getVmClazz
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

    protected lateinit var viewModel: VM
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = createViewModel()
    }
    
    /**
     * 创建 ViewModel
     */
    private fun createViewModel(): VM {
        return ViewModelProvider(this)[getVmClazz(this)]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return createComposeView().apply {
            setViewCompositionStrategy(getViewCompositionStrategy())
            setContent {
                val state by viewModel.state.collectAsState()
                ObserveSingleEvents()
                Render(state = state) { intent ->
                    viewModel.dispatchIntent(intent)
                }
            }
        }
    }

    /**
     * 创建 ComposeView
     * 子类可以重写此方法来自定义 ComposeView 的创建
     * 
     * 例如：设置主题、系统 UI 等
     */
    protected open fun createComposeView(): ComposeView {
        return ComposeView(requireContext())
    }

    /**
     * 获取 ViewCompositionStrategy
     * 子类可以重写此方法来使用不同的策略
     * 
     * 可选策略：
     * - DisposeOnViewTreeLifecycleDestroyed：当 View 树生命周期销毁时释放（推荐用于 Fragment）
     * - DisposeOnLifecycleDestroyed：当 Fragment 生命周期销毁时释放
     * - DisposeOnDetachedFromWindow：当 View 从窗口分离时释放
     */
    protected open fun getViewCompositionStrategy(): ViewCompositionStrategy {
        return ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
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
     * 显示 Toast（默认实现，子类可以重写）
     * 
     * 子类需要实现此方法来显示 Toast
     * 可以根据项目需求使用自定义 Toast 样式或 Snackbar
     */
    protected open fun showToast(message: String) {
        // 默认实现，子类可以重写
    }

    /**
     * 显示错误（默认实现，子类可以重写）
     * 
     * 子类需要实现此方法来显示错误
     * 可以根据项目需求使用自定义错误提示样式或 Snackbar
     */
    protected open fun showError(message: String) {
        // 默认实现，子类可以重写
    }

    /**
     * 导航（默认空实现）
     */
    protected open fun navigate(destination: String, args: Map<String, Any>? = null) {}
}
