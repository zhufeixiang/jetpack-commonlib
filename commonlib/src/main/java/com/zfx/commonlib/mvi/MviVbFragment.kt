package com.zfx.commonlib.mvi

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.zfx.commonlib.ext.getVmClazz

/**
 * MVI + ViewBinding Fragment 基类
 * 
 * 结合了 MVI 架构和 ViewBinding 的优势：
 * - 单向数据流：View -> Intent -> ViewModel -> State -> View
 * - 使用 ViewBinding 替代 findViewById，类型安全
 * - 所有状态都在一个 State 对象中
 * - 用户操作通过 Intent 发送
 * - 支持懒加载，防止切换动画时数据加载导致的卡顿
 * 
 * @param VM MviViewModel 类型
 * @param I Intent 类型
 * @param S State 类型
 * @param VB ViewBinding 类型
 */
abstract class MviVbFragment<VM : MviViewModel<I, S>, I : ViewIntent, S : ViewState, VB : ViewBinding> : Fragment() {

    private val handler = Handler(Looper.getMainLooper())

    // 是否第一次加载
    private var isFirst: Boolean = true

    lateinit var mViewModel: VM
    lateinit var mActivity: AppCompatActivity

    // ViewBinding 实例（使用可空类型，在 onDestroyView 时置空）
    private var _binding: VB? = null
    val mViewBind: VB get() = _binding!!

    /**
     * 创建 ViewBinding
     * 子类必须实现此方法
     */
    protected abstract fun initBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        attachToParent: Boolean
    ): VB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = initBinding(inflater, container, false)
        return mViewBind.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = context as AppCompatActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isFirst = true
        mViewModel = createViewModel()
        initView(savedInstanceState)
        observeState()
        observeSingleEvent()
        initData()
    }

    /**
     * 创建 ViewModel
     */
    private fun createViewModel(): VM {
        return ViewModelProvider(this)[getVmClazz(this)]
    }

    /**
     * 初始化 view
     */
    abstract fun initView(savedInstanceState: Bundle?)

    /**
     * 懒加载
     * 在 Fragment 可见时才会调用，防止切换动画时数据加载导致的卡顿
     */
    abstract fun lazyLoadData()

    /**
     * 观察状态变化
     * 子类可以重写此方法来处理特定的状态变化
     */
    protected open fun observeState() {
        mViewModel.observeState(this) { state ->
            renderState(state)
        }
    }

    /**
     * 渲染状态
     * 子类必须实现此方法来更新 UI
     */
    protected abstract fun renderState(state: S)

    /**
     * 观察单次事件
     * 子类可以重写此方法来处理特定的事件
     */
    protected open fun observeSingleEvent() {
        mViewModel.observeSingleEvent<BaseSingleEvent>(this) { event ->
            handleSingleEvent(event)
        }
    }

    /**
     * 处理单次事件
     * 子类可以重写此方法来处理特定的事件
     */
    protected open fun handleSingleEvent(event: BaseSingleEvent) {
        when (event) {
            is BaseSingleEvent.ShowToast -> {
                showToast(event.message)
            }
            is BaseSingleEvent.ShowError -> {
                showError(event.message)
            }
            is BaseSingleEvent.Navigate -> {
                navigate(event.destination, event.args)
            }
            is BaseSingleEvent.Finish -> {
                activity?.finish()
            }
        }
    }

    /**
     * 发送 Intent
     * 便捷方法，用于发送用户操作
     */
    protected fun dispatchIntent(intent: I) {
        mViewModel.dispatchIntent(intent)
    }

    /**
     * 显示 Toast（默认实现，子类可以重写）
     */
    protected open fun showToast(message: String) {
        // 默认实现，子类可以重写
    }

    /**
     * 显示错误（默认实现，子类可以重写）
     */
    protected open fun showError(message: String) {
        // 默认实现，子类可以重写
    }

    /**
     * 导航（默认实现，子类可以重写）
     */
    protected open fun navigate(destination: String, args: Map<String, Any>? = null) {
        // 默认实现，子类可以重写
    }

    /**
     * 初始化数据
     */
    open fun initData() {}

    override fun onResume() {
        super.onResume()
        onVisible()
    }

    /**
     * 懒加载触发
     * 当 Fragment 可见时才会调用 lazyLoadData()
     */
    private fun onVisible() {
        if (lifecycle.currentState == Lifecycle.State.STARTED && isFirst) {
            handler.postDelayed({
                lazyLoadData()
                isFirst = false
            }, lazyLoadTime())
        }
    }

    /**
     * 延迟加载时间
     * 防止切换动画还没执行完毕时数据就已经加载好了，这时页面会有渲染卡顿
     * 这里传入你想要延迟的时间，延迟时间可以设置比转场动画时间长一点 单位： 毫秒
     * 不传默认 300毫秒
     * 
     * @return Long 延迟时间（毫秒）
     */
    open fun lazyLoadTime(): Long {
        return 300
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
        _binding = null
    }
}

