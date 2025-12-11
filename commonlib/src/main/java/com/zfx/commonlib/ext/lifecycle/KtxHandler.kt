package com.zfx.commonlib.ext.lifecycle

import android.os.Handler
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent

/**
 * author: zhufeixiang
 * date: 2025/12/11
 * des: 随宿主生命周期清理消息的 Handler，避免内存泄漏
 */
class KtxHandler(lifecycleOwner: LifecycleOwner, callback: Callback) : Handler(callback), LifecycleObserver {

    private val mLifecycleOwner: LifecycleOwner = lifecycleOwner

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onDestroy() {
        removeCallbacksAndMessages(null)
        mLifecycleOwner.lifecycle.removeObserver(this)
    }
}