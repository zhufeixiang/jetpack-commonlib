package com.zfx.commonlib.ext

import java.lang.reflect.ParameterizedType

/**
 * author: zhufeixiang
 * date: 2025/12/11
 * des: ViewModel 泛型工具，返回 ViewModel 的 Class 类型用于 ViewModelProvider 获取
 */
@Suppress("UNCHECKED_CAST")
fun <VM> getVmClazz(obj: Any): Class<VM> {
    return (obj.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<VM>
}






