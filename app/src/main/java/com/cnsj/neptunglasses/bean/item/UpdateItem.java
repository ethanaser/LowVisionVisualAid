package com.cnsj.neptunglasses.bean.item;


import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;

import com.cnsj.neptunglasses.BuildConfig;
import com.cnsj.neptunglasses.bean.http.UpdateMessageModel;
import com.cnsj.neptunglasses.bean.http.UpdateModel;
import com.google.gson.Gson;
import com.xuexiang.xupdate.UpdateManager;
import com.xuexiang.xupdate.XUpdate;
import com.xuexiang.xupdate.proxy.IUpdateHttpService;
import com.xuexiang.xupdate.service.OnFileDownloadListener;
import com.xuexiang.xupdate.utils.ApkInstallUtils;
import com.zhy.http.okhttp.OkHttpUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.cnsj.neptunglasses.R;
import com.cnsj.neptunglasses.activity.ThinGlassesActivity;
import com.cnsj.neptunglasses.app.Constant;
import com.cnsj.neptunglasses.app.CuiNiaoApp;
import com.cnsj.neptunglasses.bean.BaseItem;
import com.cnsj.neptunglasses.bean.MenuEyent;
import com.cnsj.neptunglasses.utils.ApkParser;
import com.cnsj.neptunglasses.utils.OKHttpUpdateHttpService;
import com.cnsj.neptunglasses.utils.ThreadManager;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

/**
 * 更新软件
 */
public class UpdateItem extends BaseItem {

    public UpdateItem(Activity activity) {
        super(activity);
    }

    @Override
    public void init() {
        menuEyent = new MenuEyent();
        menuEyent.setItemName("更新设置");
        menuEyent.setItemCount(">");
        gson = activity.getGson();
    }

    @Override
    public int getCurrentMenuLevel() {
        return 1;
    }

    @Override
    public int getLogoImage() {

        if (this.menuPopup == null) {
            return CuiNiaoApp.isYellowMode ? R.mipmap.photo_album_s_y : R.mipmap.photo_album_s;
        }
        return CuiNiaoApp.isYellowMode ? R.mipmap.update_remind_y : R.mipmap.update_remind;
    }

    @Override
    public int getBigLogo() {
        return CuiNiaoApp.isYellowMode ? R.mipmap.update_set_b_y : R.mipmap.update_set_b;
    }

    @Override
    public int getSmallLogo() {
        return CuiNiaoApp.isYellowMode ? R.mipmap.update_set_s_y : R.mipmap.update_set_s;
    }

    private String displayText;

    @Override
    public String getDisplayText() {
        return displayText;
    }

    @Override
    public void load() {
    }

    @Override
    public void reset(int tag) {

    }

    @Override
    public boolean toJson() {
        return false;
    }

    @Override
    public MenuEyent fromJson() {
        return null;
    }


    /**
     * 请求更新的实体
     */
    private class UpdateRequestModel {
        private Integer pid;
        private String version;
        private String mac_num;

        public Integer getPid() {
            return pid;
        }

        public void setPid(Integer pid) {
            this.pid = pid;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getMac_num() {
            return mac_num;
        }

        public void setMac_num(String mac_num) {
            this.mac_num = mac_num;
        }
    }

    private UpdateManager updateManager;
    private boolean startInstall;
    private ApkParser apkParser;
    private String txtMd5;
    private boolean startDownload;
    private Gson gson;

    @Override
    public void onKeyDown(int keyCode) {
        if (this.menuPopup == null) return;
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (startDownload || startInstall) {
                    CuiNiaoApp.textSpeechManager.speakNow("中断下载更新");
                } else {
                    CuiNiaoApp.textSpeechManager.speakNow("退出更新设置");
                }
                break;
            case KeyEvent.KEYCODE_H:
            case KeyEvent.KEYCODE_I:
                if (startInstall) {
                    CuiNiaoApp.textSpeechManager.speakNow("安装过程中禁止退出");
                    return;
                }
                if (startDownload) {
                    CuiNiaoApp.textSpeechManager.speakNow("下载过程中禁止退出");
                    return;
                }
                this.menuPopup.dismiss();
                activity.remove(this.menuPopup);
                finish();
                CuiNiaoApp.textSpeechManager.speakNow("退出" + getMenuName());
                break;
            case KeyEvent.KEYCODE_F:
                if (!CuiNiaoApp.mWifiUtils.isWifiConnect(activity)) {
                    CuiNiaoApp.textSpeechManager.speakNow("当前未连接wifi，无法更新");
                    return;
                }
                if (startDownload || startInstall) {
                    CuiNiaoApp.textSpeechManager.speakNow("正在更新中");
                    return;
                }
//                if (startInstall) {
//                    CuiNiaoApp.textSpeechManager.speakNow("正在更新中");
//                    return;
//                }
                CuiNiaoApp.textSpeechManager.speakNow("开始更新");
                startDownload = true;
                startInstall = false;
                apkParser = new ApkParser();
                txtMd5 = null;
//                initXLog();
                initOKHttpUtils();
                initXUpdate();
                updateManager = XUpdate.newBuild(activity)
                        .apkCacheDir(Environment.getExternalStorageDirectory().getAbsolutePath()) //设置下载缓存的根目录
                        .build();
//                String txtName = Constant.APK_MD5_TXT;
//                String apkName = Constant.APK_NAME;
//                if (getLocalVersionName(activity).contains("alpha")) {
//                    txtName = Constant.APK_MD5_TXT1;
//                    apkName = Constant.APK_NAME1;
//                }
                // iswhiteuser 0否1是 isdeveloper 0否1是 pid=3 翠鸟视觉
//                String finalApkName = apkName;

//                "version":"005",       版本输入说明：V0.0.5 就输入"005"
//                "mobile":"13688888847",
//                "pid":1

                String mac = CuiNiaoApp.mWifiUtils.getNewMac().replace(":", "");
                String version = getLocalVersionName(activity);
                version = version.replace(".", "");
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                UpdateRequestModel updateRequestModel = new UpdateRequestModel();
                updateRequestModel.setPid(6);
                updateRequestModel.setMac_num(mac);
                updateRequestModel.setVersion(version);
                String json = gson.toJson(updateRequestModel);
                RequestBody requestBody = RequestBody.create(JSON, json);
                Log.d("TAG", "onSuccess: json:" + json);
                okHttpUpdateHttpService.asyncPost(Constant.CHECK_VERSION, requestBody, new IUpdateHttpService.Callback() {
                    //                okHttpUpdateHttpService.asyncPost(Constant.CHECK_VERSION, downloadParams, new IUpdateHttpService.Callback() {
                    @Override
                    public void onSuccess(String result) {
                        Log.d("TAG", "onSuccess: " + result);
                        if (result != null) {
                            UpdateModel updateModel = gson.fromJson(result, UpdateModel.class);
                            if (updateModel != null) {
                                if (updateModel.getErrno() == 0) {
                                    UpdateMessageModel messageModel = updateModel.getMessage();
                                    if (messageModel == null) {
                                        startDownload = false;
                                        if (UpdateItem.this.menuPopup != null) {
                                            CuiNiaoApp.textSpeechManager.speakNow("版本校验失败");
                                            displayText = "版本校验失败";
                                            UpdateItem.this.menuPopup.updateDisplayAsyn(getLogoImage(), getDisplayText(), getDisplayImages());
                                        }
                                        return;
                                    }
                                    txtMd5 = messageModel.getApkmd5();
                                    String downUrl = messageModel.getApkurl();
                                    updateManager.download(downUrl, new OnFileDownloadListener() {   //设置下载的地址和下载的监听
                                        @Override
                                        public void onStart() {
                                            Log.d("XUpdate", "onStart: 下载开始");
                                        }

                                        @Override
                                        public void onProgress(float progress, long total) {
                                            Log.d("XUpdate", "onProgress: 下载进度" + Math.round(progress * 100));
                                            if (UpdateItem.this.menuPopup != null) {
                                                displayText = Math.round(progress * 100) + "%";
                                                UpdateItem.this.menuPopup.updateDisplayAsyn(getLogoImage(), getDisplayText(), getDisplayImages());
                                            }
                                        }

                                        @Override
                                        public boolean onCompleted(File file) {
                                            Log.d("XUpdate", "onCompleted: 下载完成:" + file.getAbsolutePath());
                                            CuiNiaoApp.textSpeechManager.speakNow("下载完成，等待安装", Constant.OCR_LEVEL);


                                            String apkMD5s = null;
                                            try {
                                                apkMD5s = apkParser.getFileMD5String(file);
                                                Log.d("XUpdate", "apkMD5s:" + apkMD5s);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            startDownload = false;
                                            if (txtMd5 != null && apkMD5s != null && txtMd5.equals(apkMD5s)) {
                                                startInstall = true;
                                                //md5校验通过,执行apk安装
                                                Log.d("XUpdate", "version:执行apk安装");
//                            activity.getmMyGLSurfaceView().waitForUpdate();
                                                ThreadManager.getInstance().execute(new Runnable() {
                                                    @Override
                                                    public void run() {
//                                                            ApkInstallUtils.install(activity.getApplicationContext(), file);
                                                        apkParser.install(file.getPath(), activity);
                                                    }
                                                });
                                            } else {
                                                CuiNiaoApp.textSpeechManager.speakNow("下载文件校验未通过");
                                                if (UpdateItem.this.menuPopup != null) {
                                                    displayText = "文件校验失败";
                                                    UpdateItem.this.menuPopup.updateDisplayAsyn(getLogoImage(), getDisplayText(), getDisplayImages());
                                                }
                                            }
                                            return false;
                                        }

                                        @Override
                                        public void onError(Throwable throwable) {
                                            CuiNiaoApp.textSpeechManager.speakNow("文件下载失败");
                                            startDownload = false;
                                            Log.d("XUpdate", "onError: 下载错误" + throwable.getLocalizedMessage());
                                            if (UpdateItem.this.menuPopup != null) {
                                                displayText = "下载失败";
                                                CuiNiaoApp.textSpeechManager.speakNow(displayText);
                                                UpdateItem.this.menuPopup.updateDisplayAsyn(getLogoImage(), getDisplayText(), getDisplayImages());
                                            }
                                            updateManager.cancelDownload();
                                        }
                                    });
                                } else {
                                    startDownload = false;
                                    displayText = updateModel.getErrmsg();
                                    CuiNiaoApp.textSpeechManager.speakNow(displayText);
                                    UpdateItem.this.menuPopup.updateDisplayAsyn(getLogoImage(), getDisplayText(), getDisplayImages());
                                }
                            } else {
                                startDownload = false;
                                if (UpdateItem.this.menuPopup != null) {
                                    displayText = "版本校验失败";
                                    CuiNiaoApp.textSpeechManager.speakNow(displayText);
                                    UpdateItem.this.menuPopup.updateDisplayAsyn(getLogoImage(), getDisplayText(), getDisplayImages());
                                }
                            }
                        } else {
                            startDownload = false;
                            if (UpdateItem.this.menuPopup != null) {
                                displayText = "版本校验失败";
                                CuiNiaoApp.textSpeechManager.speakNow(displayText);
                                UpdateItem.this.menuPopup.updateDisplayAsyn(getLogoImage(), getDisplayText(), getDisplayImages());
                            }
                        }


                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.d("TAG", "onSuccess: error: " + throwable.getLocalizedMessage());
                        startDownload = false;
                        CuiNiaoApp.textSpeechManager.speakNow("版本校验失败");
                        if (UpdateItem.this.menuPopup != null) {
                            displayText = throwable.getLocalizedMessage();
                            UpdateItem.this.menuPopup.updateDisplayAsyn(getLogoImage(), getDisplayText(), getDisplayImages());
                        }
                    }
                });

//                updateManager.download(Constant.SNAP_DOWNLOAD_URL + txtName, new OnFileDownloadListener() {
//                    @Override
//                    public void onStart() {
//                        Log.d("XUpdate", "txt-onCompleted: 下载开始:");
//                    }
//
//                    @Override
//                    public void onProgress(float progress, long total) {
//
//                    }
//
//                    @Override
//                    public boolean onCompleted(File file) {
//                        String content = apkParser.readFile(file).trim();
//                        if (!TextUtils.isEmpty(content)) {
//                            Log.d("XUpdate", "onCompleted: " + content);
//                            int version = getLocalVersion(activity);
//                            Log.d("XUpdate", "version:" + version);
//                            String[] vm = content.split("\n");
//                            int serverVersion = Integer.parseInt(vm[0].trim());
//                            if (version >= serverVersion) {
//                                startDownload = false;
//                                updateManager.cancelDownload();
//                                CuiNiaoApp.textSpeechManager.speakNow("当前版本为最新版本");
//                                if (UpdateItem.this.menuPopup != null) {
//                                    displayText = "当前版本为最新版本";
//                                    UpdateItem.this.menuPopup.updateDisplay(getLogoImage(), getDisplayText(), getDisplayImages());
//                                }
//
//                                return false;
//                            }
//                            txtMd5 = vm[1].trim();
//                            Log.d("XUpdate", "txt-onCompleted: 下载完成:" + txtMd5);
//                            updateManager.download(Constant.SNAP_DOWNLOAD_URL + finalApkName, new OnFileDownloadListener() {   //设置下载的地址和下载的监听
//                                @Override
//                                public void onStart() {
//                                    Log.d("XUpdate", "onStart: 下载开始");
//                                }
//
//                                @Override
//                                public void onProgress(float progress, long total) {
//                                    Log.d("XUpdate", "onProgress: 下载进度" + Math.round(progress * 100));
//                                    if (UpdateItem.this.menuPopup != null) {
//                                        displayText = Math.round(progress * 100) + "%";
//                                        UpdateItem.this.menuPopup.updateDisplay(getLogoImage(), getDisplayText(), getDisplayImages());
//                                    }
//                                }
//
//                                @Override
//                                public boolean onCompleted(File file) {
//                                    Log.d("XUpdate", "onCompleted: 下载完成:" + file.getAbsolutePath());
//                                    CuiNiaoApp.textSpeechManager.speakNow("下载完成，等待安装", Constant.OCR_LEVEL);
//
//
//                                    String apkMD5s = null;
//                                    try {
//                                        apkMD5s = apkParser.getFileMD5String(file);
//                                        Log.d("XUpdate", "apkMD5s:" + apkMD5s);
//                                    } catch (IOException e) {
//                                        e.printStackTrace();
//                                    }
//                                    startDownload = false;
//                                    if (txtMd5 != null && apkMD5s != null && txtMd5.equals(apkMD5s)) {
//                                        startInstall = true;
//                                        //md5校验通过,执行apk安装
//                                        Log.d("XUpdate", "version:执行apk安装");
////                            activity.getmMyGLSurfaceView().waitForUpdate();
//                                        ThreadManager.getInstance().execute(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                try {
//                                                    ApkInstallUtils.install(activity.getApplicationContext(), file);
//                                                } catch (IOException e) {
//                                                    e.printStackTrace();
//                                                }
//                                            }
//                                        });
//                                    } else {
//                                        CuiNiaoApp.textSpeechManager.speakNow("下载文件校验未通过");
//                                        if (UpdateItem.this.menuPopup != null) {
//                                            displayText = "文件校验失败";
//                                            UpdateItem.this.menuPopup.updateDisplay(getLogoImage(), getDisplayText(), getDisplayImages());
//                                        }
//                                    }
//                                    return false;
//                                }
//
//                                @Override
//                                public void onError(Throwable throwable) {
//                                    CuiNiaoApp.textSpeechManager.speakNow("文件下载失败");
//                                    startDownload = false;
//                                    Log.d("XUpdate", "onError: 下载错误" + throwable.getLocalizedMessage());
//                                    if (UpdateItem.this.menuPopup != null) {
//                                        displayText = "下载失败";
//                                        UpdateItem.this.menuPopup.updateDisplay(getLogoImage(), getDisplayText(), getDisplayImages());
//                                    }
//                                    updateManager.cancelDownload();
//                                }
//                            });
//                        } else {
//                            startDownload = false;
//                            updateManager.cancelDownload();
//                            CuiNiaoApp.textSpeechManager.speakNow("版本校验异常，停止更新");
//                            if (UpdateItem.this.menuPopup != null) {
//                                displayText = "版本校验异常";
//                                UpdateItem.this.menuPopup.updateDisplay(getLogoImage(), getDisplayText(), getDisplayImages());
//                            }
//                        }
//
//                        return false;
//                    }
//
//                    @Override
//                    public void onError(Throwable throwable) {
//                        startDownload = false;
//                        updateManager.cancelDownload();
//                        CuiNiaoApp.textSpeechManager.speakNow("版本校验异常，停止更新");
//                        if (UpdateItem.this.menuPopup != null) {
//                            displayText = "版本校验异常";
//                            UpdateItem.this.menuPopup.updateDisplay(getLogoImage(), getDisplayText(), getDisplayImages());
//                        }
//
//                    }
//                });

                break;
            case -1:
                displayText = "版本" + getLocalVersionName(activity) + "\n是否更新?";
                UpdateItem.this.menuPopup.updateDisplay(getLogoImage(), getDisplayText(), getDisplayImages());
                startDownload = false;
                break;
        }
    }

    private int getLocalVersion(ThinGlassesActivity activity) {
        try {
            PackageInfo packageInfo = activity.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(activity.getApplicationContext().getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void initOKHttpUtils() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(20000L, TimeUnit.MILLISECONDS)
                .readTimeout(20000L, TimeUnit.MILLISECONDS)
                .build();
        OkHttpUtils.initClient(okHttpClient);
    }

//    private void initXLog() {
//        if ((activity.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
//            XLog.init(new LogConfiguration.Builder().logLevel(LogLevel.ALL).b().build());
//        } else {
//            XLog.init(LogLevel.NONE);
//            Constant.labelText = "";
//        }
//    }

    private OKHttpUpdateHttpService okHttpUpdateHttpService;

    private void initXUpdate() {
        okHttpUpdateHttpService = new OKHttpUpdateHttpService();
        XUpdate.get()
                .debug(BuildConfig.DEBUG)
                .isWifiOnly(true)                                               //默认设置只在wifi下检查版本更新
                .isGet(false)                                                  //默认设置使用get请求检查版本
//                .param("versionCode", UpdateUtils.getVersionCode(activity)) //设置默认公共请求参数
//                .param("appKey", Constant.APP_UPDATE_KEY)
//                .setApkCacheDir(Environment.getExternalStorageDirectory().toString())
                .supportSilentInstall(true)                                     //设置是否支持静默安装，默认是true
                .setIUpdateHttpService(okHttpUpdateHttpService)           //这个必须设置！实现网络请求功能。
                .init(activity.getApplication());
    }

    @Override
    public void speak() {
        CuiNiaoApp.textSpeechManager.speakNow("更新设置");
    }

    @Override
    public void update() {

    }

    @Override
    public void finish() {
        this.menuPopup = null;
        if (updateManager != null) {
            updateManager.cancelDownload();
        }
    }

    @Override
    public List<BaseItem> getNextMenu() {
        return null;
    }

    /**
     * 获取本地软件版本名称
     */
    public String getLocalVersionName(Context ctx) {
        String localVersion = "";
        try {
            PackageInfo packageInfo = ctx.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(ctx.getPackageName(), 0);
            localVersion = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return localVersion;
    }
}
