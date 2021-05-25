package com.cnsj.neptunglasses.manager;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.util.ResourceUtil;

/**
 * create xiachenzhi
 * 2020-8-6
 * <p>
 * 1.先进行 init
 * 2.OnTtsInitListener，初始化回调监听
 * <p>
 * 3.调用方法为start开头
 */

public class TtsManager {

    private static final String TAG = TtsApp.getTAG() + TtsManager.class.getSimpleName();
    // 语音合成对象
    private SpeechSynthesizer mTts;
    private int ttsSpeed = 50;

    // 默认云端发音人
    public static String voicerCloud = "xiaoyan";

    // 默认本地发音人
    public static String voicerLocal = "xiaoyan";

    public static String voicerLocal2 = "xiaoFeng";
    public static final String DEFAULT_LANUAGE_PATH = "xtts/common.jet";
    public static final String DEFAULT_VOICE_PATH = "xtts/xiaoyan.jet";

    //缓冲进度
    private int mPercentForBuffering = 0;
    //播放进度
    private int mPercentForPlaying = 0;

    //listener tag
    int listenerTag = -1;

    private Context mContext;
    private static TtsManager mTtsManager;

    private TtsManager(Context context) {
        mContext = context;
    }

    public static TtsManager getInstance(Context context) {
        if (mTtsManager == null) {
            mTtsManager = new TtsManager(context);
        }
        return mTtsManager;
    }

    /**
     * 初始化
     */
    public TtsManager initTts() {
        listenerTag = -1;
        if (mTts == null) {
            mTts = SpeechSynthesizer.createSynthesizer(mContext, mTtsInitListener);
        }
        return mTtsManager;
    }

    /**
     * 设定语速级别
     *
     * @param level
     */
    public void setSpeed(int level) {
        int[] levels = {40, 50, 60, 70, 80, 90, 100};
        ttsSpeed = levels[level];
        setParam(SpeechConstant.TYPE_XTTS, voicerLocal);
    }

    /**
     * initTts 初始化回调见监听
     */
    InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "InitListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                if (onTtsInitListener != null) {
                    onTtsInitListener.initError();
                }
                Log.d(TAG, "初始化失败,错误码：" + code);
            } else {
                // 初始化成功，之后可以调用startSpeaking方法
                // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                // 正确的做法是将onCreate中的startSpeaking调用移至这里
                Log.d(TAG, "初始化成功");
                if (onTtsInitListener != null) {
                    Log.d(TAG, "可以合成。。。");
                    onTtsInitListener.initSuccess();
                }

            }
        }
    };


    private OnTtsInitListener onTtsInitListener;
    private OnSynthesizerListener onSynthesizerListener;

    public void setOnTtsInitListener(OnTtsInitListener ttsInitListener) {
        this.onTtsInitListener = ttsInitListener;
    }

    public void setOnSynthesizerListener(OnSynthesizerListener onSynthesizerListener) {
        this.onSynthesizerListener = onSynthesizerListener;
    }

    public interface OnTtsInitListener {

        void initError();

        void initSuccess();

    }

    public interface OnSynthesizerListener {
        void onSpeakBegin(int tag);

        void onBufferProgress(int tag, int percent, int beginPos, int endPos, String info);

        void onSpeakPaused(int tag);

        void onSpeakResumed(int tag);

        void onSpeakProgress(int tag, int percent, int beginPos, int endPos);

        void onCompleted(int tag, int errorCode);
    }


    /**
     * 默认本地
     *
     * @param text
     * @return
     */
    public int startLocalSpeaking(String text) {
//        setParam(SpeechConstant.TYPE_XTTS, voicerLocal);
        mTts.stopSpeaking();
        int code = mTts.startSpeaking(text, mTtsListener);
        if (code != ErrorCode.SUCCESS) {
            Log.d(TAG, "语音合成失败,错误码: " + code);
        }
        return code;
    }


    public int startLocalSpeaking(String text, int listenerTag) {
        this.listenerTag = listenerTag;
//        setParam(SpeechConstant.TYPE_XTTS, voicerLocal);
        int code = mTts.startSpeaking(text, mTtsListener);
        if (code != ErrorCode.SUCCESS) {
            Log.d(TAG, "语音合成失败,错误码: " + code);
        }
        return code;
    }

    /**
     * 只能整理文字界面进来不需要判断
     *
     * @param text
     * @return
     */
    public int startLocalSpeakingText(String text, int listenerTag) {
        this.listenerTag = listenerTag;

        int code = mTts.startSpeaking(text, mTtsListener);
        if (code != ErrorCode.SUCCESS) {
            Log.d(TAG, "语音合成失败,错误码: " + code);
        }
        return code;
    }

    /**
     * 开始合成/播放
     *
     * @param text   需要合成的 string
     * @param type   合成类型，本地or在线
     * @param voicer 发音人
     * @return
     */
    public int startSpeaking(String text, String type, String voicer) {
//        setParam(type, voicer);
        int code = mTts.startSpeaking(text, mTtsListener);
        if (code != ErrorCode.SUCCESS) {
            Log.d(TAG, "语音合成失败,错误码: " + code);
        }
        return code;
    }

    /**
     * 停止播放
     */
    public void stopSpeaking() {
        if (null != mTts) {
            mTts.stopSpeaking();
        }
    }

    /**
     * 暂停播放
     */
    public void pauseSpeaking() {
        if (null != mTts)
            mTts.pauseSpeaking();

    }

    /**
     * 继续播放
     */
    public void resumeSpeaking() {
        if (null != mTts)
            mTts.resumeSpeaking();
    }

    /**
     * 退出时释放连接
     */
    public void destroySpeaking() {
        if (null != mTts) {
            mTts.stopSpeaking();
            // 退出时释放连接
            mTts.destroy();
            mTtsManager = null;
        }
    }

    public boolean isSpeaking() {
        if (null != mTts) {
            return mTts.isSpeaking();
        }
        return false;
    }


    /**
     * 设置合成参数
     *
     * @param mEngineType
     * @param voicer
     */
    private void setParam(String mEngineType, String voicer) {
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        //设置合成
        //设置使用本地引擎
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        //设置发音人资源路径
        mTts.setParameter(ResourceUtil.TTS_RES_PATH, getResourcePath());
        //设置发音人
        mTts.setParameter(SpeechConstant.VOICE_NAME, voicerLocal);
        //mTts.setParameter(SpeechConstant.TTS_DATA_NOTIFY,"1");//支持实时音频流抛出，仅在synthesizeToUri条件下支持

//        int tts_speed = SPUtils.getInstance().getInt("tts_speed", 50);
//        int tts_parameter = SPUtils.getInstance().getInt("tts_parameter", 50);

        //设置合成语速
        mTts.setParameter(SpeechConstant.SPEED, String.valueOf(ttsSpeed));
        //设置合成音调
//        mTts.setParameter(SpeechConstant.PITCH, "50");
//        //设置合成音量
//        mTts.setParameter(SpeechConstant.VOLUME, String.valueOf(tts_parameter));
        //设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");
        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        //mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        //mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/tts.wav");


    }

    //获取发音人资源路径
    private String getResourcePath() {
        StringBuffer tempBuffer = new StringBuffer();
        //合成通用资源
        tempBuffer.append(ResourceUtil.generateResourcePath(mContext, ResourceUtil.RESOURCE_TYPE.assets, DEFAULT_LANUAGE_PATH));
        tempBuffer.append(";");
        //发音人资源
        tempBuffer.append(ResourceUtil.generateResourcePath(mContext, ResourceUtil.RESOURCE_TYPE.assets, DEFAULT_VOICE_PATH));
        return tempBuffer.toString();
    }

    /**
     * 合成回调监听。
     */
    private SynthesizerListener mTtsListener = new SynthesizerListener() {
        @Override
        public void onSpeakBegin() { //开始播放
            Log.d(TAG, "开始播放");
            onSynthesizerListener.onSpeakBegin(listenerTag);
        }

        @Override
        public void onSpeakPaused() { //暂停播放
            Log.d(TAG, "暂停播放");
            onSynthesizerListener.onSpeakPaused(listenerTag);
        }

        @Override
        public void onSpeakResumed() { //继续播放
            Log.d(TAG, "继续播放");
            onSynthesizerListener.onSpeakResumed(listenerTag);
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos, String info) {

            Log.d(TAG, "info" + info);
            onSynthesizerListener.onBufferProgress(listenerTag, percent, beginPos, endPos, info);
            //缓冲进度
            // 合成进度
//            mPercentForBuffering = percent;
//            Log.d(TAG, String.format(mContext.getString(R.string.tts_toast_format),
//                    mPercentForBuffering, mPercentForPlaying));
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            onSynthesizerListener.onSpeakProgress(listenerTag, percent, beginPos, endPos);

            // 播放进度
//            mPercentForPlaying = percent;
//            Log.d(TAG, String.format(mContext.getString(R.string.tts_toast_format),
//                    mPercentForBuffering, mPercentForPlaying));
        }

        @Override
        public void onCompleted(SpeechError error) {
            //播放完成
            if (error == null) {
                Log.d(TAG, "播放完成");
                onSynthesizerListener.onCompleted(listenerTag, 0);
            } else if (error != null) {
                Log.d(TAG, "播放error" + error.getPlainDescription(true));
                onSynthesizerListener.onCompleted(listenerTag, 1);
            }
        }

        @Override
        public void onEvent(int eventType, int i1, int i2, Bundle bundle) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            if (SpeechEvent.EVENT_SESSION_ID == eventType) {
                String sid = bundle.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
                Log.d(TAG, "session id =" + sid);
            }

            //实时音频流输出参考
         /*if (SpeechEvent.EVENT_TTS_BUFFER == eventType) {
            byte[] buf = obj.getByteArray(SpeechEvent.KEY_EVENT_TTS_BUFFER);
            Log.e("MscSpeechLog", "buf is =" + buf);
         }*/
        }
    };


}
