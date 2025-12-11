package com.zfx.commonlib.ext.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.MutableLiveData

/**
 * author: zhufeixiang
 * date: 2025/12/11
 * des: 监听应用前后台切换，更新 isForeground 状态
 */
object KtxAppLifeObserver : LifecycleObserver {

    var isForeground = MutableLiveData<Boolean>()

    //在前台
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private  fun onForeground() {
        isForeground.value = true
    }

    //在后台
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun onBackground() {
        isForeground.value = false
    }

}