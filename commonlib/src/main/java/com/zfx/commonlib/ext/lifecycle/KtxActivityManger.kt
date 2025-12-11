package com.zfx.commonlib.ext.lifecycle

import android.app.Activity
import java.util.*

/**
 * author: zhufeixiang
 * date: 2025/12/11
 * des: Activity 栈管理，支持入栈/出栈/关闭当前或指定 Activity
 */
object KtxActivityManger {
    //activity集合
    private val mActivityList = LinkedList<Activity>()

    //当前activity
    val currentActivity: Activity?
        get() =
            if (mActivityList.isEmpty()) null
            else mActivityList.last

    /**
     * activity入栈
     * @param activity Activity
     */
    fun pushActivity(activity: Activity) {
        if (mActivityList.contains(activity)) {
            if (mActivityList.last != activity) {
                mActivityList.remove(activity)
                mActivityList.add(activity)
            }
        } else {
            mActivityList.add(activity)
        }
    }

    /**
     * activity出栈
     * @param activity Activity
     */
    fun popActivity(activity: Activity) {
        mActivityList.remove(activity)
    }

    /**
     * 关闭当前activity
     */
    fun finishCurrentActivity() {
        currentActivity?.finish()
    }

    /**
     * 关闭传入的activity
     * @param activity Activity
     */
    fun finishActivity(activity: Activity) {
        mActivityList.remove(activity)
        activity.finish()
    }

    /**
     * 关闭传入的activity类名
     * @param clazz Class<*>
     */
    fun finishActivity(clazz: Class<*>) {
        for (activity in mActivityList) {
            if (activity.javaClass == clazz) {
                mActivityList.remove(activity)
                activity.finish()
                return
            }
        }
    }

    /**
     * 关闭所有的activity
     */
    fun finishAllActivity() {
        for (activity in mActivityList) {
            activity.finish()
        }
        mActivityList.clear()
    }
}