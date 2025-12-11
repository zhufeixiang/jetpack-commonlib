package com.zfx.commonlib.base.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.zfx.commonlib.base.viewmodel.BaseViewModel

/**
 * author: zhufeixiang
 * date: 2025/12/11
 * des: DataBinding + ViewModel 的 Fragment 基类，封装 VM 与 DB 注入
 */
abstract class BaseVmDbFragment<VM : BaseViewModel, DB : ViewDataBinding> : BaseVmFragment<VM>() {

    override fun layoutId() = 0

    //该类绑定的ViewDataBinding
    private var _binding: DB? = null
    val mDatabind: DB get() = _binding!!

    protected abstract fun initBinding(inflater: LayoutInflater, container: ViewGroup?, attachToParent: Boolean = false): DB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding  = initBinding(inflater, container, false)
        return mDatabind.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}