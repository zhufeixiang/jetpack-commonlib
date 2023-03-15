package com.zfx.commonlib.loadsir


import android.view.View
import com.kingja.loadsir.core.LoadService
import com.kingja.loadsir.core.LoadSir

/**
 * 自定义一些常用的拓展函数
 * **/


/**
 * 初始化loadSir
 * */
fun loadServiceInit(view : View, callBack : () -> Unit) : LoadService<Any>{
    val loadSir = LoadSir.getDefault().register(view){
        //点击重试时触发的操作
        callBack.invoke()
    }
    return loadSir
}

/**
 * 设置加载中
 * */
fun LoadService<*>.showLoading(){
    this.showCallback(LoadingCallback::class.java)
}

/**
 * 设置错误布局
 * */
fun LoadService<*>.showError(){
    this.showCallback(ErrorCallback::class.java)
}

/**
 * 设置空布局
 * */
fun LoadService<*>.showEmptyView(){
    this.showCallback(EmptyCallback::class.java)
}