package com.zfx.commonlib.base.compose

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle

/**
 * Compose Fragment 基类（无 ViewModel）
 * 
 * 适用于：
 * - 纯 UI 展示页面，不需要复杂的状态管理
 * - 简单的静态页面
 * - 不需要网络请求或数据处理的页面
 * - 支持懒加载，防止切换动画时数据加载导致的卡顿
 * 
 * 使用示例：
 * ```kotlin
 * class AboutFragment : BaseComposeFragment() {
 *     override fun Render() {
 *         AboutScreen()
 *     }
 *     
 *     override fun lazyLoadData() {
 *         // 如果需要加载数据，可以在这里实现
 *     }
 * }
 * ```
 */
abstract class BaseComposeFragment : Fragment() {
    
    private val handler = Handler(Looper.getMainLooper())
    
    // 是否第一次加载
    private var isFirst: Boolean = true
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return createComposeView().apply {
            setViewCompositionStrategy(getViewCompositionStrategy())
            setContent {
                Render()
            }
        }
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isFirst = true
        initData()
    }
    
    override fun onResume() {
        super.onResume()
        onVisible()
    }
    
    /**
     * 是否需要懒加载
     */
    private fun onVisible() {
        if (lifecycle.currentState == Lifecycle.State.STARTED && isFirst) {
            // 延迟加载，避免转场动画未结束的卡顿
            handler.postDelayed({
                lazyLoadData()
                isFirst = false
            }, lazyLoadTime())
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
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
     * 
     * 子类必须实现此方法来构建 UI
     * 此方法中可以直接使用 Compose 组件，无需 ViewModel
     * 
     * 如果需要状态管理，可以使用：
     * - remember { mutableStateOf() } 用于本地状态
     * - LaunchedEffect 用于副作用
     */
    @Composable
    protected abstract fun Render()
    
    /**
     * 懒加载数据
     * 
     * 子类可以重写此方法来实现懒加载逻辑
     * 此方法会在 Fragment 可见且第一次加载时调用
     * 用于避免在切换动画时加载数据导致的卡顿
     */
    protected open fun lazyLoadData() {
        // 默认空实现，子类可以重写
    }
    
    /**
     * Fragment 执行 onCreateView 后触发的方法
     * 子类可以重写此方法来初始化数据（非懒加载）
     */
    protected open fun initData() {
        // 默认空实现，子类可以重写
    }

    /**
     * 延迟加载时间
     * 
     * 防止切换动画还没执行完毕时数据就已经加载好了，这时页面会有渲染卡顿
     * 这里传入你想要延迟的时间，延迟时间可以设置比转场动画时间长一点
     * 
     * @return 延迟时间（毫秒），默认 300 毫秒
     */
    protected open fun lazyLoadTime(): Long {
        return 300
    }
}

