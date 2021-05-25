package com.em3.vrhiddemos.tools

import android.os.CountDownTimer

/**
 * Created by Zph on 2020/7/02.
 * 蓝牙工具类
 */
class ShowHintTimer(millisInFuture: Long, countDownInterval: Long, private val onTimeFinish: () -> Unit) : CountDownTimer(millisInFuture, countDownInterval) {

    override fun onFinish() {
        onTimeFinish()
    }

    override fun onTick(millisUntilFinished: Long) {

    }
}