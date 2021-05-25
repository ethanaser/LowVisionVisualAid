package com.cnsj.neptunglasses.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.cnsj.neptunglasses.bean.MenuEyent;
import com.cnsj.neptunglasses.manager.TextSpeechManager;
import com.cnsj.neptunglasses.utils.CrashHandler;
import com.cnsj.neptunglasses.utils.VolumeManager;
import com.cnsj.neptunglasses.utils.WifiUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.serenegiant.usb.common.OffsetUtils;
import com.tencent.bugly.crashreport.CrashReport;

public class CuiNiaoApp extends Application {
    public static Context mAppContext;
    public static TextSpeechManager textSpeechManager;
    public static SharedPreferences sharedPreferences;
    public static WifiUtils mWifiUtils;
    public static VolumeManager mVolumeManager;
    public static boolean isOcrInit = false;
    public static boolean isYellowMode = false;//主题是黑白模式还是黑黄模式

    @Override
    public void onCreate() {
        super.onCreate();
        CrashReport.initCrashReport(getApplicationContext(), "7d293c3b6e", true);
        mAppContext = getApplicationContext();
//        //初始化opencv
//        OffsetUtils.init(mAppContext);
        sharedPreferences = getSharedPreferences(Constant.SP, Context.MODE_PRIVATE);
        mWifiUtils = new WifiUtils(this);
        mWifiUtils.OpenWifi();
        mVolumeManager = new VolumeManager(this);
//        OcrApp.setContext(mAppContext);
        initAccessToken();
        CrashHandler.getInstance().init(this);
        textSpeechManager = new TextSpeechManager(mAppContext);
        float xPoint = 1.0f, yPoint = 0.5625f;
//        float xPoint = 0.889f, yPoint = 1.0f;
//        float xPoint = 1.0f, yPoint = 1.0f;
        OffsetUtils.init(mAppContext, xPoint, yPoint);
        initThemeMode();
    }

    /**
     * 初始化主题颜色  默认为黑白模式
     */
    private void initThemeMode() {
        String itemKey = "theme_mode";
        CuiNiaoApp.isYellowMode = false;
        String json = CuiNiaoApp.sharedPreferences.getString(itemKey, null);
        if (json != null) {
            MenuEyent menuEyent = new Gson().fromJson(json, new TypeToken<MenuEyent>() {
            }.getType());
            CuiNiaoApp.isYellowMode = "开".equals(menuEyent.getItemCount());
        }
    }


    /**
     * 以license文件方式初始化
     */
    public static void initAccessToken() {
        OCR.getInstance(mAppContext).initAccessToken(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken accessToken) {
                String token = accessToken.getAccessToken();
                Log.d("TAG", "licence方式获取token成功");
                isOcrInit = true;
            }

            @Override
            public void onError(OCRError error) {
                error.printStackTrace();
                //alertText("licence方式获取token失败", error.getMessage());
                Log.e("TAG", "licence方式获取token失败" + error.getMessage());
                isOcrInit = false;
            }
        }, mAppContext);
    }


}
