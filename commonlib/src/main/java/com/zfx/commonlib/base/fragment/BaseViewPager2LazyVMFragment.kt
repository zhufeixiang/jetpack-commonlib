package com.zfx.commonlib.base.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.zfx.commonlib.ext.inflateBindingWithGeneric

/**
 * 适配vp2的懒加载的带刷新的fragment
 * */
abstract class BaseViewPager2LazyVMFragment<T : ViewBinding>(@LayoutRes open var layoutId : Int) :Fragment(layoutId){

    open lateinit var binding : T
    /**
     * 是否第一次加载
     * */
    private var isFirstLoad = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = inflateBindingWithGeneric(layoutInflater,container,false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView()
        startObserve() //liveData的数据在fragment中订阅
        super.onViewCreated(view, savedInstanceState)

    }

    /**
     * vp2自带懒加载所以只需在onResume的生命周期加载数据就行
     * */
    override fun onResume() {
        super.onResume()
        if(isFirstLoad){
            initData()
            isFirstLoad = false
        }

    }

    abstract fun initData()

    abstract fun initView()

    abstract fun startObserve()


}
