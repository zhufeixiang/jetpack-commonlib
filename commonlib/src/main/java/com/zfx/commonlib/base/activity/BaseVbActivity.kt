package com.zfx.commonlib.base.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.zfx.commonlib.ext.util.notNull

/**
 * author: zhufeixiang
 * date: 2025/12/11
 * des: 纯 ViewBinding Activity 基类（无 ViewModel），封装 inflate/网络状态监听
 */
abstract class BaseVbActivity<VB : ViewBinding> : AppCompatActivity() {

    lateinit var mViewBind: VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initDataBind().notNull({
            setContentView(it)
        })
        init(savedInstanceState)
    }

    /**
     * 创建ViewBinding
     */
    protected abstract fun initBinding(layoutInflater: LayoutInflater): VB

    /**
     * 初始化 DataBinding，返回 ViewBinding 的 root View
     */
    open fun initDataBind(): View? {
        mViewBind = initBinding(layoutInflater)
        return mViewBind.root
    }

    private fun init(savedInstanceState: Bundle?) {
        initView(savedInstanceState)
        createObserver()
        initData()
    }

    abstract fun initView(savedInstanceState: Bundle?)

    /**
     * 初始化默认数据
     */
    abstract fun initData()

    /**
     * 创建LiveData数据观察者
     */
    abstract fun createObserver()


}