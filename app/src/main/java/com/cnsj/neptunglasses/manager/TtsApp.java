package com.cnsj.neptunglasses.manager;

import android.content.Context;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;


public class TtsApp {


    private static final String TTS_APPID = "5f420b3e";
    private static Context mContext;
    public static final String TAG = "TtsApp";

    public static String getTAG() {
        return TAG;
    }

    public static void setContext(Context context) {
        mContext = context;
        if (mContext == null) {
            return;
        }
        initTts();
    }

    public static Context getContext() {
        if (mContext == null) {
            throw new NullPointerException("TtsApp Context is null , you need setContext in app Application");
        }
        return mContext;
    }

    private static void initTts() {
        // 将“12345678”替换成您申请的APPID，申请地址：http://www.xfyun.cn
        // 请勿在“=”与appid之间添加任何空字符或者转义符
        SpeechUtility.createUtility(mContext, SpeechConstant.APPID + "=" + TTS_APPID);
    }

}
