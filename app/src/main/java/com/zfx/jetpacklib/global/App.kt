package com.zfx.jetpacklib.global

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.request.crossfade
import com.zfx.commonlib.network.extension.initNetworkManager
import com.zfx.commonlib.network.interceptor.LoginInterceptor
import com.zfx.commonlib.network.repository.BaseRepository
import com.zfx.commonlib.util.StringResourceHelper

class App : Application(), SingletonImageLoader.Factory {


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

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .crossfade(true)
            .build()
    }
}