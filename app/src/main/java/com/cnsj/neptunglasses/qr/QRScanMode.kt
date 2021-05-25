package com.cnsj.sightaid.model

import android.graphics.Bitmap
import android.util.Log
import com.cnsj.neptunglasses.R
import com.cnsj.neptunglasses.app.CuiNiaoApp
import com.cnsj.neptunglasses.qr.BaseMode
import com.cnsj.neptunglasses.qr.QrCodeUtils
//import com.cnsj.myapplication.xlog.XLog
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * Created by MaFanwei on 2020/1/15.
 */
class QRScanMode(private val mQRScanResultLinstener: QRScanResultListener) : BaseMode() {
    private var calculateJob: Job? = null

    interface QRScanResultListener {
        fun onWifiQRCode(ssid: String, password: String)
        fun onDecodingError();
    }

    override fun start(bitmap: Bitmap) {
        if (calculateJob == null || calculateJob!!.isCompleted) {
            calculateJob = GlobalScope.launch {
                val result = QrCodeUtils.parseQRCodeResult(bitmap)
                Log.e("QrCodeUtils", "result $result")
                if (result == QrCodeUtils.NO_DATA) {
                    solveResult(false, "")
                } else {
                    solveResult(true, result ?: "")
                }
            }
            calculateJob!!.start()
        }
    }

    override fun cancel() {
        super.cancel()
        calculateJob?.cancel()
        calculateJob = null
        isPrepare = false
    }

    private fun solveResult(success: Boolean, message: String) {
        if (success) {
            onReadingQRCodeSuccess(message)
        } else {
            mQRScanResultLinstener.onDecodingError();
        }
    }

    private fun onReadingQRCodeSuccess(s: String) {
//        val resultBean = OsdHintBean()
//        resultBean.bottom = CuiNiaoApp.appContext.getString(R.string.setting_wifi_scan)
        cancel()
//        XLog.e("onReadingQRCodeSuccess:$s")
        try {
            val jsonObject = JSONObject(s)
            val command = jsonObject["command"].toString()
            val length = jsonObject["length"] as Int
            val args = mutableListOf<String>()
            for (i in 0 until length) {
                args.add(jsonObject["arg${i}"].toString())
            }
            val msg: String =
                    when (command) {
                        "wifi" -> {
                            CuiNiaoApp.mWifiUtils.OpenWifi()
//                            resultBean.bottom = CuiNiaoApp.appContext.getString(R.string.setting_wifi_scan)
                            mQRScanResultLinstener.onWifiQRCode(args[0], args[1])
                            "连接中"
                        }
                        "phone", "pc" -> {
                            val editor = CuiNiaoApp.sharedPreferences.edit()
                            editor.putString(command, args[0])
                            editor.commit()
//                            resultBean.bottom = CuiNiaoApp.appContext.getString(R.string.setting_set_cast_address)
                            CuiNiaoApp.mAppContext.getString(R.string.set_success)
                        }
                        /* "param" -> {
                             if (glParamManager.Scan2SetAll(args[0].split("|").map { it.toInt() }.toIntArray())) {
                                 getString(R.string.set_param_success)
                             } else {
                                 getString(R.string.set_param_fail)
                             }
                         }
                         "login" -> {
                             login(args[0], args[1])
                             getResourceString(R.string.login_success)
                         }*/
                        else -> CuiNiaoApp.mAppContext.getString(R.string.not_support_command)
                    }
//            resultBean.center = msg
//            resultBean.keepShow = true
//            mQRScanResultLinstener.onQRScanResult(false, resultBean)
        } catch (e: Exception) {
//            resultBean.center = CuiNiaoApp.appContext.getString(R.string.qr_code_error)
//            mQRScanResultLinstener.onQRScanResult(true, resultBean)
            mQRScanResultLinstener.onDecodingError();
        }
    }
}
