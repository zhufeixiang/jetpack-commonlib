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
import com.zfx.commonlib.ext.getVmClazz

/**
 * MVI Fragment 基类
 * 
 * MVI 架构特点：
 * - 单向数据流：View -> Intent -> ViewModel -> State -> View
 * - 所有状态都在一个 State 对象中
 * - 用户操作通过 Intent 发送
 * 
 * @param VM MviViewModel 类型
 * @param I Intent 类型
 * @param S State 类型
 */
abstract class MviFragment<VM : MviViewModel<I, S>, I : ViewIntent, S : ViewState> : Fragment() {

    private val handler = Handler(Looper.getMainLooper())

    // 是否第一次加载
    private var isFirst: Boolean = true

    lateinit var mViewModel: VM

    lateinit var mActivity: AppCompatActivity

    /**
     * 当前Fragment绑定的视图布局
     */
    abstract fun layoutId(): Int

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(layoutId(), container, false)
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
     * 初始化view
     */
    abstract fun initView(savedInstanceState: Bundle?)

    /**
     * 懒加载
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
     * 
     * 子类需要实现此方法来显示 Toast
     * 可以根据项目需求使用自定义 Toast 样式
     */
    protected open fun showToast(message: String) {
        // 默认实现，子类可以重写
    }

    /**
     * 显示错误（默认实现，子类可以重写）
     * 
     * 子类需要实现此方法来显示错误
     * 可以根据项目需求使用自定义错误提示样式
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
     * 是否需要懒加载
     */
    private fun onVisible() {
        if (lifecycle.currentState == Lifecycle.State.STARTED && isFirst) {
            lazyLoadData()
            isFirst = false
        }
    }
}

