package com.zfx.commonlib.ext.util

/**
 * author: zhufeixiang
 * date: 2025/12/11
 * des: 空安全简化调用，替代早期 CommonExt 的 notNull
 */
inline fun <T> T?.notNull(
    notNullAction: (T) -> Unit,
    nullAction: () -> Unit = {}
) {
    if (this != null) {
        notNullAction(this)
    } else {
        nullAction()
    }
}

