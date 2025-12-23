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
import androidx.lifecycle.ViewModelProvider
import com.zfx.commonlib.base.viewmodel.BaseViewModel
import com.zfx.commonlib.ext.getVmClazz

/**
 * Compose + MVVM Fragment 基类
 * 
 * MVVM 架构特点：
 * - 双向数据绑定，多个独立的 StateFlow
 * - ViewModel 管理业务逻辑和状态
 * - 使用 Compose 渲染 UI
 * - 支持懒加载，防止切换动画时数据加载导致的卡顿
 * 
 * 使用示例：
 * ```kotlin
 * class HomeFragment : BaseComposeVmFragment<HomeViewModel>() {
 *     override fun Render(viewModel: HomeViewModel) {
 *         val bannerList by viewModel.bannerList.collectAsState()
 *         val articleList by viewModel.articleList.collectAsState()
 *         
 *         HomeScreen(
 *             bannerList = bannerList,
 *             articleList = articleList
 *         )
 *     }
 *     
 *     override fun lazyLoadData() {
 *         viewModel.loadData()
 *     }
 * }
 * ```
 * 
 * @param VM ViewModel 类型，必须继承 BaseViewModel
 */
abstract class BaseComposeVmFragment<VM : BaseViewModel> : Fragment() {

    protected lateinit var viewModel: VM
    
    private val handler = Handler(Looper.getMainLooper())
    
    // 是否第一次加载
    private var isFirst: Boolean = true
    
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
                Render(viewModel = viewModel)
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
     * 在此方法中可以使用 viewModel 的 StateFlow 来收集状态
     * 
     * @param viewModel ViewModel 实例，用于访问状态和方法
     */
    @Composable
    protected abstract fun Render(viewModel: VM)
    
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

