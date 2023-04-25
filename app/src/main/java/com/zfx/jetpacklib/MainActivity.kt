package com.zfx.jetpacklib


import android.os.Bundle
import com.zfx.commonlib.base.activity.BaseVmVbActivity
import com.zfx.jetpacklib.databinding.ActivityMainBinding

class MainActivity : BaseVmVbActivity<MainViewModel,ActivityMainBinding>() {


    override fun initView(savedInstanceState: Bundle?) {
        mViewBind.tvMain.text = "hello  android！"
    }

    override fun showLoading(message: String) {

    }

    override fun dismissLoading() {

    }


    override fun createObserver() {

    }
}