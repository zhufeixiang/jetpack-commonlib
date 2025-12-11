package com.zfx.commonlib.base.activity

import android.view.LayoutInflater
import android.view.View
import androidx.databinding.ViewDataBinding
import com.zfx.commonlib.base.viewmodel.BaseViewModel

/**
 * author: zhufeixiang
 * date: 2025/12/11
 * des: DataBinding + ViewModel 的 Activity 基类，负责绑定 VM 与 DB
 */
abstract class BaseVmDbActivity<VM : BaseViewModel, DB : ViewDataBinding> : BaseVmActivity<VM>() {

    override fun layoutId() = 0

    lateinit var mDatabind: DB

    /**
     * 创建DataBinding
     */
    protected abstract fun initBinding(layoutInflater: LayoutInflater): DB

    override fun initDataBind(): View? {
        mDatabind = initBinding(layoutInflater)
        return mDatabind.root
    }
}