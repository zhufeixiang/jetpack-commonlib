package com.zfx.commonlib.network.interceptor

/**
 * 未登录拦截器接口
 * 
 * 用于处理未登录错误码的回调，当网络请求返回未登录错误码时，会调用此接口的方法
 * 
 * 使用场景：
 * - 清除用户登录信息
 * - 跳转到登录页面
 * - 显示登录提示
 * 
 * 注意：此回调只会被调用一次（在配置的时间窗口内），避免重复处理
 */
interface LoginInterceptor {
    /**
     * 处理未登录错误
     * 
     * @param errorCode 错误码
     * @param errorMessage 错误消息
     */
    fun onUnauthorized(errorCode: Int, errorMessage: String)
}

