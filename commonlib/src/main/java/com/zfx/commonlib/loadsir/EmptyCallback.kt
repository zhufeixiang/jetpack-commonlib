package com.zfx.commonlib.loadsir


import com.kingja.loadsir.callback.Callback
import com.zfx.commonlib.R


class EmptyCallback : Callback() {

    override fun onCreateView(): Int {
        return R.layout.layout_empty
    }

}