package com.zfx.jetpacklib.global

import android.app.Application
import com.zfx.commonlib.network.extension.initNetworkManager
import com.zfx.commonlib.network.interceptor.LoginInterceptor
import com.zfx.commonlib.network.repository.BaseRepository
import com.zfx.commonlib.util.StringResourceHelper

class App : Application() {


    override fun onCreate() {
        super.onCreate()

        StringResourceHelper.init(this)
        initNetworkManager(
            baseUrl = "https://www.wanandroid.com"
        )

        BaseRepository.setLoginInterceptor(object : LoginInterceptor{
            override fun onUnauthorized(errorCode: Int, errorMessage: String) {

            }
        }).unauthorizedCodes(setOf(1001))
            .interceptWindowMillis(3000)
    }
}