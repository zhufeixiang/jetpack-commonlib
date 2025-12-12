package com.zfx.commonlib.mvi

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.zfx.commonlib.ext.getVmClazz

/**
 * MVI + ViewBinding Activity 基类
 * 
 * 结合了 MVI 架构和 ViewBinding 的优势：
 * - 单向数据流：View -> Intent -> ViewModel -> State -> View
 * - 使用 ViewBinding 替代 findViewById，类型安全
 * - 所有状态都在一个 State 对象中
 * - 用户操作通过 Intent 发送
 * 
 * @param VM MviViewModel 类型
 * @param I Intent 类型
 * @param S State 类型
 * @param VB ViewBinding 类型
 */
abstract class MviVbActivity<VM : MviViewModel<I, S>, I : ViewIntent, S : ViewState, VB : ViewBinding> : AppCompatActivity() {

    lateinit var mViewModel: VM
    lateinit var mViewBind: VB

    /**
     * 创建 ViewBinding
     * 子类必须实现此方法
     */
    protected abstract fun initBinding(layoutInflater: LayoutInflater): VB

    abstract fun initView(savedInstanceState: Bundle?)

    abstract fun showLoading(message: String = "请求网络中...")

    abstract fun dismissLoading()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewBind = initBinding(layoutInflater)
        setContentView(mViewBind.root)
        init(savedInstanceState)
    }

    private fun init(savedInstanceState: Bundle?) {
        mViewModel = createViewModel()
        initView(savedInstanceState)
        observeState()
        observeSingleEvent()
    }

    /**
     * 创建 ViewModel
     */
    private fun createViewModel(): VM {
        return ViewModelProvider(this).get(getVmClazz(this))
    }

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
                // 显示 Toast，子类可以重写
                showToast(event.message)
            }
            is BaseSingleEvent.ShowError -> {
                // 显示错误，子类可以重写
                showError(event.message)
            }
            is BaseSingleEvent.Navigate -> {
                // 导航，子类可以重写
                navigate(event.destination, event.args)
            }
            is BaseSingleEvent.Finish -> {
                finish()
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
}

