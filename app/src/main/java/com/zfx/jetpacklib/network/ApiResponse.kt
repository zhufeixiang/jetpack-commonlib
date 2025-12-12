package com.zfx.jetpacklib.network

import com.zfx.commonlib.network.response.IBaseResponse

class ApiResponse<T>(
    var  data : T? = null,
    var  errorCode : Int = -1,//未登录的错误码为-1001，其他错误码为-1，成功为0，建议对errorCode 判断当不为0的时候，均为错误
    var  errorMsg : String = ""
) : IBaseResponse<T> {
    override fun isSuccess(): Boolean {
        return errorCode == 0
    }

    override fun getDataOrThrow(): T {
        return data ?: throw IllegalStateException("响应数据为空")
    }

    override fun getDataOrDefault(defaultValue: T): T {
        return data ?: defaultValue
    }

    override fun getErrorMessage(): String {
        return errorMsg
    }

    override fun getResponseCode(): Int {
        return errorCode
    }

    override fun getResponseMsg(): String {
        return errorMsg
    }

    override fun getData(): T? {
        return data
    }
}