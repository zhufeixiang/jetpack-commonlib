package com.zfx.commonlib.base.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.zfx.commonlib.base.viewmodel.BaseViewModel

/**
 * author: zhufeixiang
 * date: 2025/12/11
 * des: ViewBinding + ViewModel 的 Fragment 基类，封装 VM 与 VB 注入
 */
abstract class BaseVmVbFragment<VM : BaseViewModel, VB : ViewBinding> : BaseVmFragment<VM>() {

    override fun layoutId() = 0

    //该类绑定的 ViewBinding
    private var _binding: VB? = null
    val mViewBind: VB get() = _binding!!

    protected abstract fun initBinding(inflater: LayoutInflater, container: ViewGroup?, attachToParent: Boolean = false): VB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding  = initBinding(inflater, container, false)
        return mViewBind.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}