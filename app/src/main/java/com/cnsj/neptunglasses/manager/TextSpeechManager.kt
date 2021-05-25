package com.cnsj.neptunglasses.manager

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.cnsj.neptunglasses.app.Constant
import com.cnsj.neptunglasses.app.CuiNiaoApp
import com.cnsj.neptunglasses.manager.TtsApp
import com.cnsj.neptunglasses.manager.TtsManager
import java.util.*

/**
 * Created by MaFanwei on 2020/3/30.
 */
class TextSpeechManager {
    private lateinit var textToSpeech: TextToSpeech
    private var words = ""
    private var isInit = false
    private val rates = arrayOf(0.5f, 1f, 2f, 3f, 4f, 5f)
    private var nowRate: Int
    private val SP_KEY_RATE = "text_speech_manager_sp_key_rate"
    private var canRead = true;
    private var useDefaultTts = false;//是否使用默认的GoogleTTS
    private lateinit var ttsManager: TtsManager;

    companion object {
        //        var isOCRReading = false
        var isReading = false
        var lastLevel = 0;
        var isPause = false;
    }

    init {
        nowRate = CuiNiaoApp.sharedPreferences.getInt(SP_KEY_RATE, 1);//VRApp.sharedPreferences.getInt(SP_KEY_RATE, 1)
        useDefaultTts = false;
        if (useDefaultTts) {
            textToSpeech = TextToSpeech(CuiNiaoApp.mAppContext, TextToSpeech.OnInitListener { status ->
                if (status === TextToSpeech.SUCCESS) {
                    isInit = true
                    textToSpeech.setSpeechRate(rates[nowRate])
                    textToSpeech.setOnUtteranceProgressListener(Listener())
                    this.textToSpeech.setLanguage(Locale.CHINA)
                    if (words != "") {
                        speakNow(words)
                        words = ""
                    }
                }
            }, "com.google.android.tts")
        } else {
            TtsApp.setContext(CuiNiaoApp.mAppContext);
            ttsManager = TtsManager.getInstance(CuiNiaoApp.mAppContext);
            ttsManager.setOnTtsInitListener(object : TtsManager.OnTtsInitListener {
                override fun initError() {
                    isInit = false;
                }

                override fun initSuccess() {
                    isInit = true;
                }
            })
            ttsManager.initTts()
            ttsManager.setSpeed(nowRate);
            ttsManager.setOnSynthesizerListener(TtsListener());
        }
        canRead = true;
    }

    constructor(context: Context) {

    }

    fun speakNow(text: String, level: Int) {
        if (!canRead) return;
        if (level < lastLevel) {
            return;
        }
        lastLevel = level;
        if (isInit) {
            var result = -1;
            if (useDefaultTts) {
                result = textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "mafanwei")
            } else {
                result = ttsManager.startLocalSpeaking(text);
            }
            if (result != TextToSpeech.SUCCESS) {
                isInit = false
                words += text
                reStart()
            }

        } else {
            words += text
        }
    }


    fun speakNow(text: String) {
        speakNow(text, Constant.NORMAL_LEVEL)
    }

    fun reStart(isReStart: Boolean = true) {
        if (useDefaultTts) {
            textToSpeech.shutdown()
            textToSpeech = TextToSpeech(CuiNiaoApp.mAppContext, TextToSpeech.OnInitListener { status ->
                if (status === TextToSpeech.SUCCESS) {
                    isInit = true
                    textToSpeech.setSpeechRate(rates[nowRate])
                    textToSpeech.setOnUtteranceProgressListener(Listener())
                    this.textToSpeech.setLanguage(Locale.CHINA)
                    if (!isReStart) {
                        speakNow(words)
                    }
                    words = ""
                }
            }, "com.google.android.tts")
        } else {
            TtsApp.setContext(CuiNiaoApp.mAppContext);
            ttsManager = TtsManager.getInstance(CuiNiaoApp.mAppContext);
            ttsManager.setOnTtsInitListener(object : TtsManager.OnTtsInitListener {
                override fun initError() {
                    isInit = false;
                }

                override fun initSuccess() {
                    isInit = true;
                    if (!isReStart) {
                        speakNow(words)
                    }
                    words = ""
                }
            })
            ttsManager.initTts()
            ttsManager.setSpeed(nowRate);
            ttsManager.setOnSynthesizerListener(TtsListener());
        }

        textToSpeech = TextToSpeech(CuiNiaoApp.mAppContext, TextToSpeech.OnInitListener { status ->
            if (status === TextToSpeech.SUCCESS) {
                isInit = true
                textToSpeech.setSpeechRate(rates[nowRate])
                textToSpeech.setLanguage(Locale.CHINA)
            }
        }, "com.google.android.tts")

    }

    fun setSpeechRate(offset: Int): String {
        nowRate += offset
        if (nowRate >= rates.size) {
            nowRate = rates.size - 1
        } else if (nowRate < 1) {
            nowRate = 1
        }
        if (useDefaultTts) {
            textToSpeech.setSpeechRate(rates[nowRate])
        } else {
            ttsManager.setSpeed(nowRate);
        }
        saveRate()
        return nowRate.toString()
    }

    fun getSpeechRate(): String {
        return nowRate.toString()
    }

    fun getRates(): Float {
        return rates[nowRate];
    }

    fun resetSpeechRate() {
        nowRate = 1
        saveRate()
    }

    private fun saveRate() {
        val editor = CuiNiaoApp.sharedPreferences.edit()
        editor.putInt(SP_KEY_RATE, nowRate)
        editor.apply()
    }

//    fun speakLater(text: String) {
//        if (isInit) {
//            textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, "mafanwei")
//        } else {
//            words += text
//        }
//    }
//
//    fun speakOCR(text: String) {
//        if (isInit) {
//            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "ocr")
//        } else {
//            words += text
//        }
//    }

    fun shutDown() {
        shutDown(Constant.NORMAL_LEVEL)
    }

    /**
     * 根据播放等级打断相应级别的语音
     */
    fun shutDown(level: Int) {
        if (level == lastLevel) {
            Log.d("MainActivity", "shutDown level:终止 ")
            if (useDefaultTts) {
                textToSpeech.stop();
                isReading = false;
            } else {
                ttsManager.stopSpeaking();
            }
//            textToSpeech.shutdown();
            lastLevel = 0;

        }
    }

    /**
     * 打断所有
     */
    fun shutDownAll() {
        if (isReading()) {
            if (useDefaultTts) {
                textToSpeech.stop();
                isReading = false;
            } else {
                ttsManager.stopSpeaking();
            }
            lastLevel = 0;

        }
        canRead = false;
    }

    fun reset() {
        canRead = true;
    }

    fun isReading(): Boolean {
        if (useDefaultTts)
            return isReading
        else
            return ttsManager.isSpeaking;
    }

    /**
     * 正在读OCR
     */
    fun isOCRReading(): Boolean {
        return isReading() && lastLevel == Constant.OCR_LEVEL;
    }

    /**
     * 判断是否读完了
     */
    fun isPauseOCRReading(): Boolean {
        if (useDefaultTts) {
            return isReading && lastLevel == Constant.OCR_LEVEL;
        } else {
            Log.d("TAG", "ttsmanager: " + isPauseReading())
            return isPauseReading() && lastLevel == Constant.OCR_LEVEL;
        }
    }


    fun isPauseReading(): Boolean {
        return isPause
    }

    /**
     * 暂停播放
     */
    fun pauseReading() {
        if (useDefaultTts) {
            textToSpeech.stop()
        } else {
            ttsManager.pauseSpeaking();
        }
    }

    /**
     * 重新播放
     */
    fun restartReading() {
        if (useDefaultTts) {
            speakNow(words)
        } else {
            ttsManager.resumeSpeaking();
        }
    }

    //    fun isOCRReading(): Boolean {
//        return isOCRReading
//    }
    /**
     * 重置语音级别
     */
    fun resetLastLevel() {
        lastLevel = 0;
    }

    class Listener : UtteranceProgressListener() {
        var currentMills = 0L;
        override fun onDone(utteranceId: String?) {
            Log.d("MainActivity", "onDone: " + (System.currentTimeMillis() - currentMills))
            Log.d("texttospeech", "onDone: " + utteranceId)
            lastLevel = 0;
            isReading = false
//            utteranceId?.let {
//                if ("ocr" == utteranceId) {
//                    isOCRReading = false
//                }
//            }
        }

        override fun onError(utteranceId: String?) {
            Log.d("texttospeech", "onError: " + utteranceId)
            lastLevel = 0;
            isReading = false
//            if ("ocr" == utteranceId) {
//                isOCRReading = false
//            }
        }

        override fun onStart(utteranceId: String?) {
            Log.d("texttospeech", "onStart: " + utteranceId)
            isReading = true
            currentMills = System.currentTimeMillis();
//            if ("ocr" == utteranceId) {
//                isOCRReading = true
//            }
        }
    }

    class TtsListener : TtsManager.OnSynthesizerListener {
        override fun onBufferProgress(tag: Int, percent: Int, beginPos: Int, endPos: Int, info: String?) {
        }

        override fun onSpeakBegin(tag: Int) {
            isPause = false;
        }

        override fun onSpeakProgress(tag: Int, percent: Int, beginPos: Int, endPos: Int) {
        }

        override fun onSpeakPaused(tag: Int) {
            isPause = true;
        }

        override fun onSpeakResumed(tag: Int) {
            isPause = false;
        }

        override fun onCompleted(tag: Int, errorCode: Int) {
            lastLevel = 0;
            isPause = false;
        }

    }
}