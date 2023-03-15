package com.zfx.commonlib.base.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.zfx.commonlib.base.viewmodel.BaseViewModel
import com.zfx.commonlib.ext.getVmClazz
import com.zfx.commonlib.ext.inflateBindingWithGeneric
import com.zfx.commonlib.ext.util.notNull
import com.zfx.commonlib.network.manager.NetState
import com.zfx.commonlib.network.manager.NetworkStateManager

/**
 * 作者　: zhufeixiang
 * 时间　: 2023/3/15
 * 描述　: ViewBindingActivity基类 (仅适用于不需要ViewModel的Activity)
 */
abstract class BaseVbActivity<VB : ViewBinding> : AppCompatActivity() {


    lateinit var mViewBind: VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(initDataBind())
        init(savedInstanceState)
    }

    /**
     * 创建ViewBinding
     */
    private fun initDataBind(): View {
        mViewBind = inflateBindingWithGeneric(layoutInflater)
        return mViewBind.root

    }

    private fun init(savedInstanceState: Bundle?) {
        initView(savedInstanceState)
        createObserver()
        initData()
        NetworkStateManager.instance.mNetworkStateCallback.observe(this, Observer {
            onNetworkStateChanged(it)
        })
    }

    abstract fun initView(savedInstanceState: Bundle?)

    /**
     * 初始化默认数据
     */
    abstract fun initData()

    /**
     * 网络变化监听 子类重写
     */
    open fun onNetworkStateChanged(netState: NetState) {}

    /**
     * 创建LiveData数据观察者
     */
    abstract fun createObserver()


}