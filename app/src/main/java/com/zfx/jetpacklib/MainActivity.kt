package com.zfx.jetpacklib


import android.os.Bundle
import android.view.LayoutInflater
import com.zfx.commonlib.base.activity.BaseVmVbActivity
import com.zfx.jetpacklib.databinding.ActivityMainBinding

class MainActivity : BaseVmVbActivity<MainViewModel,ActivityMainBinding>() {


    override fun initView(savedInstanceState: Bundle?) {
        mViewBind.tvMain.text = "hello  android！"
    }


    override fun createObserver() {

    }

    override fun initBinding(layoutInflater: LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }
}