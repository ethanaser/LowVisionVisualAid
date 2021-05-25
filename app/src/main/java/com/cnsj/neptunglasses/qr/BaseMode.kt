package com.cnsj.neptunglasses.qr

import android.graphics.Bitmap

/**
 * Created by MaFanwei on 2020/1/14.
 */
abstract class BaseMode {
    protected var isPrepare = false

    abstract fun start(bitmap: Bitmap)

    open fun isPrepared(): Boolean {
        return isPrepare
    }

    open fun cancel() {
        isPrepare = false
    }

    open fun prepare(isPrepare: Boolean = true) {
        this.isPrepare = isPrepare
    }
}