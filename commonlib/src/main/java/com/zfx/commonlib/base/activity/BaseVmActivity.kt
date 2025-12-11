package com.zfx.commonlib.base.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.zfx.commonlib.base.viewmodel.BaseViewModel
import com.zfx.commonlib.ext.getVmClazz
import com.zfx.commonlib.ext.util.notNull

/**
 * author: zhufeixiang
 * date: 2025/12/11
 * des: 基于 ViewModel 的 Activity 基类，负责注入 VM、注册加载回调
 */
abstract class BaseVmActivity<VM : BaseViewModel> : AppCompatActivity() {

    lateinit var mViewModel: VM

    abstract fun layoutId(): Int

    abstract fun initView(savedInstanceState: Bundle?)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initDataBind().notNull({
            setContentView(it)
        }, {
            setContentView(layoutId())
        })
        init(savedInstanceState)
    }

    private fun init(savedInstanceState: Bundle?) {
        mViewModel = createViewModel()
        initView(savedInstanceState)
        createObserver()
    }

    /**
     * 创建viewModel
     */
    private fun createViewModel(): VM {
        return ViewModelProvider(this).get(getVmClazz(this))
    }

    /**
     * 创建LiveData数据观察者
     */
    abstract fun createObserver()

    /**
     * 供子类BaseVmDbActivity 初始化Databinding操作
     */
    open fun initDataBind(): View? {
        return null
    }
}