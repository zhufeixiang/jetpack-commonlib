package com.zfx.commonlib.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import java.lang.ref.WeakReference

/**
 * 字符串资源工具类
 * 
 * 用于在库代码中获取字符串资源，支持国际化
 * 需要在 Application 中初始化 Context
 * 
 * 使用弱引用存储 Application Context，避免内存泄漏
 * 
 * 线程安全说明：
 * - 使用 @Volatile 确保多线程环境下的可见性
 * - init() 可能在主线程调用，getString() 可能在任意线程调用（如网络请求的 IO 线程）
 * - @Volatile 确保一个线程对 contextRef 的修改对其他线程立即可见
 */
object StringResourceHelper {
    
    /**
     * 使用 @Volatile 确保多线程环境下的可见性
     * 
     * 原因：
     * 1. init() 通常在 Application.onCreate() 中调用（主线程）
     * 2. getString() 可能在任意线程调用（如网络请求的 IO 线程、协程的 Dispatchers.IO）
     * 3. 不使用 @Volatile 可能导致其他线程读取到未初始化的 null 值（可见性问题）
     * 
     * 虽然 init() 通常只调用一次，但为了线程安全和防御性编程，使用 @Volatile 是必要的
     */
    @Volatile
    private var contextRef: WeakReference<Context>? = null
    
    /**
     * 初始化 Context
     * 应该在 Application.onCreate() 中调用
     * 
     * @param ctx Application Context（会自动转换为 applicationContext）
     */
    @JvmStatic
    @SuppressLint("StaticFieldLeak")
    fun init(ctx: Context) {
        // 使用 Application Context，不会导致内存泄漏
        val appContext = ctx.applicationContext
        contextRef = WeakReference(appContext)
    }
    
    /**
     * 获取字符串资源
     * 
     * @param resId 字符串资源 ID
     * @return 字符串资源，如果未初始化或 Context 已被回收则返回空字符串
     */
    @JvmStatic
    fun getString(resId: Int): String {
        val context = contextRef?.get() ?: return ""
        return try {
            context.getString(resId)
        } catch (e: Exception) {
            ""
        }
    }
    
    /**
     * 获取字符串资源（带格式化参数）
     * 
     * @param resId 字符串资源 ID
     * @param vararg formatArgs 格式化参数
     * @return 格式化后的字符串资源，如果未初始化或 Context 已被回收则返回空字符串
     */
    @JvmStatic
    fun getString(resId: Int, vararg formatArgs: Any): String {
        val context = contextRef?.get() ?: return ""
        return try {
            context.getString(resId, *formatArgs)
        } catch (e: Exception) {
            ""
        }
    }
    
    /**
     * 检查是否已初始化
     */
    @JvmStatic
    fun isInitialized(): Boolean {
        return contextRef?.get() != null
    }
    
    /**
     * 获取 Resources 对象
     */
    @JvmStatic
    fun getResources(): Resources? {
        return contextRef?.get()?.resources
    }
    
    /**
     * 获取 Application Context（用于获取缓存目录等）
     * 
     * 注意：此方法返回的 Context 可能为 null（如果未初始化）
     * 调用方应该处理 null 情况
     * 
     * @return Application Context，如果未初始化则返回 null
     */
    @JvmStatic
    fun getContext(): Context? {
        return contextRef?.get()
    }
}

