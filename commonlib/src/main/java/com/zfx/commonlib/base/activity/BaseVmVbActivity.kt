package com.zfx.commonlib.base.activity

import android.view.LayoutInflater
import android.view.View
import androidx.viewbinding.ViewBinding
import com.zfx.commonlib.base.viewmodel.BaseViewModel

/**
 * author: zhufeixiang
 * date: 2025/12/11
 * des: ViewBinding + ViewModel 的 Activity 基类，负责绑定 VM 与 VB
 */
abstract class BaseVmVbActivity<VM : BaseViewModel, VB : ViewBinding> : BaseVmActivity<VM>() {

    override fun layoutId(): Int = 0

    lateinit var mViewBind: VB

    /**
     * 创建DataBinding
     */
    protected abstract fun initBinding(layoutInflater: LayoutInflater): VB

    override fun initDataBind(): View? {
        mViewBind = initBinding(layoutInflater)
        return mViewBind.root
    }
}