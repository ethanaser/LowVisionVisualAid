package com.cnsj.sightaid.model

//import com.elvishew.xlog.XLog
import android.graphics.Bitmap
import com.baidu.ocr.sdk.OCR
import com.baidu.ocr.sdk.OnResultListener
import com.baidu.ocr.sdk.model.GeneralBasicParams
import com.baidu.ocr.sdk.model.GeneralResult

import com.cnsj.neptunglasses.app.CuiNiaoApp
import com.cnsj.neptunglasses.qr.BaseMode
import com.cnsj.neptunglasses.utils.CommonFileUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by MaFanwei on 2020/1/14.
 */
object OCRMode : BaseMode() {
    private var ocrJob: Job? = null
//    private val resultListener = object : OnResultListener<GeneralResult> {
//        override fun onError(p0: OCRError) {
//            cancel()
////            XLog.e("解析错误" + p0.message)
//            Log.d("TAG", "onError: ocrocr 解析错误" + p0.message)
//            VRApp.textSpeechManager.speakNow("OCR解析错误", Constant.OCR_LEVEL)
//        }
//
//        override fun onResult(p0: GeneralResult) {
//            cancel()
////            XLog.e("解析成功")
//            Log.d("TAG", "onResult: ocrocr 解析成功")
//            var s = StringBuilder()
//            for (wordSimple in p0.wordList) {
////                XLog.e("解析内容：" + wordSimple.words)
//                Log.d("TAG", "onResult: ocrocr 内容 |" + wordSimple.words)
//                s.append(wordSimple.words)
//            }
//            if (s == null || s.equals("")) {
//                VRApp.textSpeechManager.speakNow("未识别到文字", Constant.OCR_LEVEL)
//            } else {
//                VRApp.textSpeechManager.speakNow(s.toString(), Constant.OCR_LEVEL)
//            }
//        }
//    }

    fun start(bitmap: Bitmap, scale: Float,resultListener: OnResultListener<GeneralResult>) {
        if (ocrJob == null) {
            ocrJob = GlobalScope.launch {
                CommonFileUtils.saveOCRFile(bitmap, scale)
                var param = GeneralBasicParams()
                param.setDetectDirection(true)
                param.imageFile = CommonFileUtils.getOCRFile()
                delay(1500)
                OCR.getInstance(CuiNiaoApp.mAppContext).recognizeAccurateBasic(param, resultListener)
            }
        }
    }

    override fun start(bitmap: Bitmap) {
    }

    override fun cancel() {
        super.cancel()
        ocrJob = null
    }

    fun shutDownSpeak() {
//        VRApp.textSpeechManager.shutDownOCR()
    }

}