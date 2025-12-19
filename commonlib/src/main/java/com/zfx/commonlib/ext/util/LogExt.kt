package com.zfx.commonlib.ext.util

import android.util.Log

/**
 * author: zhufeixiang
 * date: 2025/12/11
 * des: 日志扩展函数，简化日志打印
 */

/**
 * 打印 Debug 日志
 * @param tag 日志标签，默认为调用类名
 */
fun String.logd(tag: String? = null) {
    val logTag = tag ?: getCallerClassName()
    Log.d(logTag, this)
}

/**
 * 打印 Info 日志
 */
fun String.logi(tag: String? = null) {
    val logTag = tag ?: getCallerClassName()
    Log.i(logTag, this)
}

/**
 * 打印 Warning 日志
 */
fun String.logw(tag: String? = null) {
    val logTag = tag ?: getCallerClassName()
    Log.w(logTag, this)
}

/**
 * 打印 Error 日志
 */
fun String.loge(tag: String? = null) {
    val logTag = tag ?: getCallerClassName()
    Log.e(logTag, this)
}

/**
 * 获取调用者类名（用于日志标签）
 */
private fun getCallerClassName(): String {
    val stackTrace = Thread.currentThread().stackTrace
    // 跳过当前方法和 getCallerClassName 方法，获取真正的调用者
    for (i in 3 until stackTrace.size) {
        val className = stackTrace[i].className
        if (!className.startsWith("com.zfx.commonlib.ext.util")) {
            return className.substringAfterLast(".")
        }
    }
    return "Unknown"
}

