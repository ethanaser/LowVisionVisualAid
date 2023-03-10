package com.cnsj.neptunglasses.activity;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.util.Size;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.PointerIcon;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cnsj.neptunglasses.R;
import com.cnsj.neptunglasses.app.Constant;
import com.cnsj.neptunglasses.app.CuiNiaoApp;
import com.cnsj.neptunglasses.bean.BaseItem;
import com.cnsj.neptunglasses.bean.item.BasicSetItem;
import com.cnsj.neptunglasses.bean.item.DisplaySetItem;
import com.cnsj.neptunglasses.bean.item.EyeSetItem;
import com.cnsj.neptunglasses.bean.item.ManualItem;
import com.cnsj.neptunglasses.bean.item.PhotoAlbumItem;
import com.cnsj.neptunglasses.bean.item.ResetSetItem;
import com.cnsj.neptunglasses.bean.item.UpdateItem;
import com.cnsj.neptunglasses.bean.item.VersionItem;
import com.cnsj.neptunglasses.bean.item.VoiceSetItem;
import com.cnsj.neptunglasses.constant.What;
import com.cnsj.neptunglasses.manager.CNSJCamera;
import com.cnsj.neptunglasses.mvc.BitmapController;
import com.cnsj.neptunglasses.mvc.OCRController;
import com.cnsj.neptunglasses.mvc.WifiController;
import com.cnsj.neptunglasses.receiver.BatteryReceiver;
import com.cnsj.neptunglasses.receiver.HomeWatcherReceiver;
import com.cnsj.neptunglasses.service.GlassesObserver;
import com.cnsj.neptunglasses.service.GlassesService;
import com.cnsj.neptunglasses.utils.SightaidUtil;
import com.cnsj.neptunglasses.utils.ThreadManager;
import com.cnsj.neptunglasses.view.CustomMenuPopup;
import com.cnsj.neptunglasses.view.ViewObservable;
import com.cnsj.neptunglasses.view.ViewObserver;
import com.cnsj.neptunglasses.view.gl.OnEyesChangeListener;
import com.cnsj.neptunglasses.view.gl.OnFrameListener;
import com.cnsj.neptunglasses.view.gl.YuvGLSurfaceView;
import com.em3.vrhiddemos.manager.GamePadManager;
import com.google.gson.Gson;
import com.jiangdg.usbcamera.utils.MathUtils;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.lxj.xpopup.impl.FullScreenPopupView;
import com.serenegiant.usb.USBMonitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class YUVModeActivity extends BaseAppCompatActivity implements GamePadManager.GamePadConnectStateListener, ViewObservable<ViewObserver> {


    public static final String TAG = "YUVModeActivity";
    private View left_bottom, right_bottom, left_layout_bottom, right_layout_bottom,
            coverView;//ocrLeft, ocrRight, left_top, right_top, left_layout_top, right_layout_top,
    private GamePadManager mGamePadManager;//??????MSG
    private int width = 0, height = 0;
    private CNSJCamera camera5801, camera5802;//5801 ??????????????????5802???????????????
    private Gson gson;
    public BaseItem[] firstMenu;//????????????
    private int[] recordPosition;//???????????????????????????????????????????????????
    private List<BaseItem> baseItemList;//??????????????????
    private FullScreenPopupView menuPopup;//????????????
    private BasePopupView popupView;//????????????
    private BatteryManager batteryManager;//????????????
    private BatteryReceiver batteryReceiver;//????????????????????????
    private HomeWatcherReceiver mHomeKeyReceiver;//Home???????????????
    public YuvGLSurfaceView yuvGLSurfaceView;//?????????????????????GLSurfaceview
    public GlassesService.MyBinder myBinder;//????????????????????????????????????Service
    private boolean isNetworkConnect;//????????????????????????
    private float[] popupTranslationOffset = new float[5];//???????????? ???????????? pos 0 ?????? 1 2???????????? 3 4 ????????????
    private float[] popupScaleOffset = {1.0f, 1.0f};//???????????????????????? pos 0 ?????? 1 ??????
    private Integer[] popupTextScaleOffset = {1};//??????????????????
    private Vector<ViewObserver> observerList;//?????????????????????????????????
    private String content;//????????????UI?????????????????????
    public int scale = 0;//????????????
    public float[] scaleX = new float[]{1.0f, 1.5f, 2.0f, 3.0f, 5.0f, 10.0f, 20.f, 25.f};
    private int currentIpd = 62;//??????????????????
    public int mUserMode = 0;//????????????????????????
    //0 ?????? 5 ?????? 6 ???????????? 7,8,9,10 ????????????4????????? 11 ?????????
    //??????????????????
    public Constant.ColorMode[] colorModes;
    private boolean isDecoding = true;//??????????????????????????????????????????????????????????????????WIFI??????????????????????????????
    private boolean isConnect;//WiFi????????????
    private String failReason;//WiFi???????????????????????????
    public int centerScaleTag = 0;//???????????? ????????????
    public int fastScaleTag = 0;//??????????????????????????? ????????????
    public int freezemode = 0;//?????? ????????????
    private boolean canStart;//????????????????????????????????????  ????????????+?????????????????????????????????5s ??????????????????????????????
    private long currentTimeMillis = 0l;//??????????????????
    private boolean isCharging;//??????????????????????????????????????????UI
    private boolean isRunningStop = false;//???????????????????????????????????????????????????????????????
    private long lastScaleCurrentTimeMillis;//??????????????????????????????????????????????????????
    WifiController wifiController;//WiFi???????????????
    private int ocrInitCount = 0;//OCR?????????????????????????????????????????????OCR??????????????????
    int ocrcount = 1;//??????????????????OCR????????????
    /**
     * ??????????????????
     */
    boolean isSaveBitmap = true;//????????????????????????
    private boolean isReading = false;//
    private TextView prompt1, prompt2, leftBattery, rightBattery;//??????????????? view
    private ImageView left_image_bottom, right_image_bottom;//, left_image_top, right_image_top,left_anti_top, right_anti_top, left_bluetooth_top, right_bluetooth_top, left_wifi_topright_wifi_top, left_battery_top, right_battery_top;
    private int textSize = 25;//????????????

    public Gson getGson() {
        return gson;
    }

    /**
     * ??????????????????
     *
     * @param scale
     */
    public void setDoubleEyeScale(float scale) {
        yuvGLSurfaceView.setDoubleEyeScale(scale);
    }

    public float getDoubleEyeScale() {
        return yuvGLSurfaceView.getDoubleEyeScale();
    }

    /**
     * ?????????????????????
     */
    private void initReceiver() {
        batteryReceiver = new BatteryReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, intentFilter);
        IntentFilter filter = new IntentFilter();
//        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
//        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
//        wifiStateReceiver = new WifiStateReceiver();
//        Intent intent = registerReceiver(wifiStateReceiver, filter);
        mHomeKeyReceiver = new HomeWatcherReceiver();
        IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mHomeKeyReceiver, homeFilter);
    }

    /**
     * ??????wifi???????????????
     *
     * @param connected
     */
    public void checkWifiConnect(boolean connected) {
//        if (connected) {
//            isConnect = true;
//            handler.removeMessages(What.WIFI_TIMEOUT);
//        } else {
//            isConnect = false;
//        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: create");
        super.onCreate(savedInstanceState);
        initReceiver();
        setContentView(R.layout.activity_yuv_mode);
//        getWindow().getDecorView().setPointerIcon(PointerIcon.load(getResources(), R.drawable.pointer_spot_touch_icon));
        left_bottom = findViewById(R.id.main_layout_left);
        right_bottom = findViewById(R.id.main_layout_right);
        left_layout_bottom = findViewById(R.id.main_left);
        right_layout_bottom = findViewById(R.id.main_right);
        prompt1 = left_bottom.findViewById(R.id.main_msg_text);
        prompt2 = right_bottom.findViewById(R.id.main_msg_text);
        left_image_bottom = left_bottom.findViewById(R.id.main_msg_image);
        right_image_bottom = right_bottom.findViewById(R.id.main_msg_image);
        left_layout_bottom.setVisibility(View.GONE);
        right_layout_bottom.setVisibility(View.GONE);
        yuvGLSurfaceView = findViewById(R.id.yuv_view);
        yuvGLSurfaceView.setOnEyesChangeListener(onEyesChangeListener);
        Size size = yuvGLSurfaceView.getDefaultSize();
        width = size.getWidth();
        height = size.getHeight();
        yuvGLSurfaceView.setOnYuvFrameListener(onFrameListener);
//        Log.d(TAG, "onCreate: w:" + width + "  height:" + height);
        coverView = findViewById(R.id.cover_view);
        gson = new Gson();
        mGamePadManager = new GamePadManager(this);
        batteryManager = (BatteryManager) (getApplicationContext().getSystemService(getApplicationContext().BATTERY_SERVICE));
        recordPosition = new int[3];
        setFirstPosition(0);
        setSecondPosition(-1);
        setThirdPosition(-1);
//        colorModes = new Constant.ColorMode[]{Constant.ColorMode.mode_color, doubleColor, Constant.ColorMode.mode_gray,
//                Constant.ColorMode.mode_reversegray, edgeColor, Constant.ColorMode.mode_fakecolor};
//        CuiNiaoApp.textSpeechManager.speakNow("???????????????");
        startPermission();
        initNetWorkStatus();
        initColorMode();
        initService();
    }


    private void initService() {
        Intent intent = new Intent(this, GlassesService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }


    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myBinder = (GlassesService.MyBinder) service;
            myBinder.addObserver(observer);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    //??????????????????????????? IMU ?????? USB????????????
    GlassesObserver observer = new GlassesObserver() {
        @Override
        public void onConnect(int productId, USBMonitor.UsbControlBlock usbControlBlock) {
            Log.d(TAG, "onConnect: cccccccccccccc");
            switch (productId) {
                case GlassesService.MyBinder.VR_770:
                    initList();
                    break;
                case GlassesService.MyBinder
                        .CONNECT_5801:
                    initList();
                    if (camera5801 == null) {
                        camera5801 = new CNSJCamera(getApplicationContext(), handler);
                        camera5801.setDefaultSize(width, height);
                        if (!camera5801.isInit()) {
                            camera5801.init();
                        }
                        camera5801.setControlBlock(usbControlBlock);
                        if (!camera5801.isOpen()) {
                            camera5801.openCamera();
//                    if (cameraTag == 0x5801) {
                            Log.d(TAG, "onConnect: ccccccccccc:" + 5801);
//                            camera5801.startPreview(yuvGLSurfaceView);
//                    }
                        }
                    }
                    break;
                case GlassesService.MyBinder
                        .CONNECT_5802:
                    initList();
                    if (camera5802 == null) {
                        camera5802 = new CNSJCamera(getApplicationContext(), handler);
                        camera5802.setDefaultSize(width, height);
                        if (!camera5802.isInit()) {
                            camera5802.init();
                        }
                        camera5802.setControlBlock(usbControlBlock);
                        if (!camera5802.isOpen()) {
                            camera5802.openCamera();
//                    if (cameraTag == 0x5802) {
                            Log.d(TAG, "onConnect: ccccccccccc:" + 5802);
                            yuvGLSurfaceView.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    camera5802.startPreview(yuvGLSurfaceView);
                                }
                            }, 500);
//                    }
                        }
                    }
                    break;
            }
        }

        @Override
        public void onDisconnect() {
//            Log.d(TAG, "onDettach: dddddddddd");
            if (camera5801 != null) {
                if (camera5801.isOpen()) {
                    camera5801.release();
                }
                camera5801 = null;
            }
            if (camera5802 != null) {
                if (camera5802.isOpen()) {
                    camera5802.release();
                }
                camera5802 = null;
            }
            yuvGLSurfaceView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (YUVModeActivity.this.menuPopup != null && YUVModeActivity.this.popupView != null) {
                        menuPopup.dismiss();
                        popupView.dismiss();
                        setMenuPopup(null, null);
                    }
                    setFirstPosition(0);
                    setSecondPosition(-1);
                    setThirdPosition(-1);
                    notifyViewChange(What.POPUP_DISMISS);
                    scale = 0;
                    yuvGLSurfaceView.setScale(scaleX[0]);
                    yuvGLSurfaceView.quitFreezeMode();
                    yuvGLSurfaceView.release();
                    displayPreview();
                }
            }, 30);

        }

        @Override
        public void onQuatData(float[] quat) {
//            if (yuvGLSurfaceView != null) {
//                yuvGLSurfaceView.onSensorChange(quat);
//            }
        }

        @Override
        public void onQuaternion(float[] quats) {
            if (yuvGLSurfaceView != null) {
                yuvGLSurfaceView.onQuaternion(quats);
            }
        }

        @Override
        public void onWearStatus(boolean isWear) {
            Log.d(TAG, "onWearStatus: ooooooooooooooooooon:" + isWear);
            if (!isWear) {
                //??????1??????????????????????????????????????????????????????????????????
                long delayTime = 1000 * 60 * 1;
//                handler.removeMessages(WEAR_ON);
//                handler.sendEmptyMessageDelayed(What.WEAR_OFF, delayTime);
            } else {
//                handler.removeMessages(What.WEAR_OFF);
//                handler.sendEmptyMessageDelayed(WEAR_ON, delayTime);
            }
        }
    };

    private void initList() {
        if (baseItemList == null) {
            baseItemList = new ArrayList<>();
            firstMenu = new BaseItem[]{new EyeSetItem(YUVModeActivity.this),
                    new DisplaySetItem(YUVModeActivity.this),
                    new VoiceSetItem(YUVModeActivity.this),
                    new PhotoAlbumItem(YUVModeActivity.this),
                    new BasicSetItem(YUVModeActivity.this),
                    new ResetSetItem(YUVModeActivity.this),
                    new UpdateItem(YUVModeActivity.this),
                    new ManualItem(YUVModeActivity.this),
                    new VersionItem(YUVModeActivity.this)};
            baseItemList.addAll(Arrays.asList(firstMenu));
        }
    }


    /**
     * ??????????????????????????????
     */
    private void startPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE}, 0);
            }

        }
    }


    public void setIpd(int distance, int direction) {
        yuvGLSurfaceView.setIpd(distance);
    }

    /**
     * ????????????????????????
     */
    OnEyesChangeListener onEyesChangeListener = new OnEyesChangeListener() {
        @Override
        public void onIpdChange(float ipdOffset) {
            float baseX = 2400 / 2;
            float ipd = MathUtils.floatMultiply(baseX, ipdOffset);//????????????????????????
            Log.d("TAG", "onIpdChange: offset???" + ipdOffset);
            Log.d("TAG", "onIpdChange: ?????????????????????" + ipd);
            popupTranslationOffset[0] = ipd;
            notifyViewChange(What.VIEW_CHANGE);
        }

        @Override
        public void onLeftEyeOffsetChange(float leftX, float leftY) {
            float baseX = 2400 / 2;
            float baseY = 1200;
            float leftXOffset = MathUtils.floatMultiply(baseX, leftX);//????????????????????????
            float leftYOffset = MathUtils.floatMultiply(baseY, leftY);//????????????????????????
            Log.d("TAG", "onIpdChange: leftXOffset " + leftXOffset);
            Log.d("TAG", "onIpdChange: leftYOffset " + leftYOffset);
            popupTranslationOffset[1] = leftXOffset;
            popupTranslationOffset[2] = leftYOffset;
            notifyViewChange(What.VIEW_CHANGE);
        }

        @Override
        public void onRightEyeOffsetChange(float rightX, float rightY) {
            float baseX = 2400 / 2;
            float baseY = 1200;
            float rightXOffset = MathUtils.floatMultiply(baseX, rightX);//????????????????????????
            float rightYOffset = MathUtils.floatMultiply(baseY, rightY);//????????????????????????
            Log.d("TAG", "onIpdChange: rightXOffset " + rightXOffset);
            Log.d("TAG", "onIpdChange: rightYOffset " + rightYOffset);
            popupTranslationOffset[3] = rightXOffset;
            popupTranslationOffset[4] = rightYOffset;
            notifyViewChange(What.VIEW_CHANGE);
        }

        @Override
        public void onLeftScaleChange(float leftScale) {
            //???????????????????????????
            Log.d("TAG", "onIpdChange: leftScale " + leftScale);
            popupScaleOffset[0] = leftScale;
            notifyViewChange(What.VIEW_CHANGE);
        }

        @Override
        public void onRightScaleChange(float rightScale) {
            //???????????????????????????
            Log.d("TAG", "onIpdChange: rightScale " + rightScale);
            popupScaleOffset[1] = rightScale;
            notifyViewChange(What.VIEW_CHANGE);
        }
    };

    /**
     * ????????????????????????
     */
    public void initNetWorkStatus() {
        isNetworkConnect = false;
        ThreadManager.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                while (!isNetworkConnect) {
                    Socket s = null;
                    try {
                        if (s == null) {
                            s = new Socket();
                        }
                        InetAddress host = InetAddress.getByName("www.baidu.com");//
                        s.connect(new InetSocketAddress(host, 80), 3000);//
                        isNetworkConnect = true;
                        s.close();
                    } catch (IOException e) {
                        isNetworkConnect = false;
                    }
                }
            }
        });
    }

    public void setLeftEyeScale(float scale) {
        yuvGLSurfaceView.setLeftSubScale(scale);
    }

    public float getLeftEyeScale() {
        return yuvGLSurfaceView.getLeftScale();
    }

    public void setLeftScale(float mLeftSubScale) {
        popupScaleOffset[0] = mLeftSubScale;
    }

    public void setRightScale(float mRightSubScale) {
        popupScaleOffset[1] = mRightSubScale;
    }

    public float getRightEyeScale() {
        return yuvGLSurfaceView.getRightScale();
    }

    public void setRightEyeScale(float scale) {
        yuvGLSurfaceView.setRightSubScale(scale);
    }

    public List<BaseItem> getBaseItemList() {
        return baseItemList;
    }


    public void setEyeTranslation(float eye_translation) {
        popupTranslationOffset[0] = eye_translation;
    }


    public int getSaturation() {
        return yuvGLSurfaceView.getSaturation();
    }

    public void setSaturation(int saturation) {
        yuvGLSurfaceView.setSaturation(saturation);
    }

    public int getContrast() {
        return yuvGLSurfaceView.getContrast();
    }


    public void setContrast(int contrast) {
        yuvGLSurfaceView.setContrast(contrast);
    }


    /**
     * 8-40 ???????????????
     * ????????????
     * @param brightness
     */
    public void setBrightness(int brightness) {
        brightness = brightness * 7;
        if (myBinder != null) {
            myBinder.setBrightness(brightness);
        }
    }


    public void setPopupTextScaleOffset(int popupTextScaleOffset) {
        this.popupTextScaleOffset[0] = popupTextScaleOffset;
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: onResume");
        yuvGLSurfaceView.onResume();
        displayPreview();
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart: onStart");
        super.onStart();
    }

    /**
     * ??????????????? ????????????????????? startPreview
     */
    private void openCamera() {
        if (camera5801 != null) {
            if (!camera5801.isOpen()) {
                camera5801.openCamera();
//                camera5801.startPreview(yuvGLSurfaceView);
            }
        }
        if (camera5802 != null) {
            if (!camera5802.isOpen()) {
                camera5802.openCamera();
                camera5802.startPreview(yuvGLSurfaceView);
            }
        }
    }


    /**
     * ???????????????????????????
     */
    private void closeCamera() {
        if (camera5801 != null) {
            if (camera5801.isOpen()) {
                camera5801.closeCamera();
            }
        }
        if (camera5802 != null) {
            if (camera5802.isOpen()) {
                camera5802.closeCamera();
            }
        }
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause: onPause");
        super.onPause();
        yuvGLSurfaceView.onPause();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop: onStop");
        super.onStop();
//        closeCamera();
        // ????????????USB????????????
//        if (usbMonitor != null) {
//            usbMonitor.unregister();
//        }
//        if (currentCamera != null) {
//            currentCamera.closeCamera();
//        }
    }


    private void stopReceiver() {
//        if (wifiStateReceiver != null) {
//            unregisterReceiver(wifiStateReceiver);
//            wifiStateReceiver = null;
//        }
        if (mHomeKeyReceiver != null) {
            unregisterReceiver(mHomeKeyReceiver);
            mHomeKeyReceiver = null;
        }
        if (batteryReceiver != null) {
            unregisterReceiver(batteryReceiver);
            batteryReceiver = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: onDestory");
        ocrInitCount = 0;
        if (myBinder != null) {
            myBinder.deleteObserver(observer);
        }
//        if (usbMonitor != null) {
//            usbMonitor.unregister();
//        }
        stopReceiver();
        // ??????uvc????????????
    }

    public float[] getPopupTranslationOffset() {
        return popupTranslationOffset;
    }


    public void notifyError() {
        //TODO
//        mMyGLSurfaceView.playNoOperateSound();
        CuiNiaoApp.mVolumeManager.playNoOperateSound();
    }


    public void setWifiStatus(boolean isConnect, String failReason) {
        this.isConnect = isConnect;
        this.failReason = failReason;
    }

    public String getFailReason() {
        return this.failReason;
    }


    /**
     * ????????????wifi?????????
     */
    public void qrScanStart() {
//        mCameraHelper.setOnPreviewFrameListener(mAbstractUVCCameraHandler);
        isDecoding = false;
        wifiController = null;
        content = "?????????...";
        notifyViewChange(What.CHANGE_CONTENT);
        handler.removeMessages(What.WIFI_TIMEOUT);
        handler.sendEmptyMessageDelayed(What.WIFI_TIMEOUT, 15000);
        ocrcount = 1;
        isSaveBitmap = true;
    }

    /**
     * ??????wifi??????
     */
    public void cancelQR() {
        isDecoding = true;
        if (wifiController != null) {
            wifiController.updateWifiStatus(true);
            wifiController = null;
        }
        handler.removeMessages(What.WIFI_TIMEOUT);
        handler.removeMessages(What.WIFI_SUCCES);
        handler.removeMessages(What.WIFI_FAULIRE);
    }

    public boolean isWifiConnected() {
        return isConnect;
    }

    //????????????
    private void showMenu() {
        if (baseItemList == null) {
            return;
        }
        for (BaseItem baseItem : baseItemList) {//??????????????????????????????????????????
            baseItem.load();
        }
        menuPopup = new CustomMenuPopup(this, YUVModeActivity.this);
        popupView = new XPopup.Builder(this)
                .isDestroyOnDismiss(true)
                .hasStatusBarShadow(false)
                .hasStatusBar(false)
                .asCustom(menuPopup)
                .show();
        setMenuPopup(menuPopup, popupView);
    }

    public void setMenuPopup(FullScreenPopupView menuPopup, BasePopupView popupView) {
        this.menuPopup = menuPopup;
        this.popupView = popupView;
    }


    public void setLeftEyeCenter(int x, int y, boolean isX) {
        yuvGLSurfaceView.setLeftEyeOffset(x, y);
    }

    public int[] getLeftEyeCenter() {
        String[] split = yuvGLSurfaceView.getLeftEyeOffset().split(",");
        int[] ints = new int[2];
        for (int i = 0; i < ints.length; i++) {
            ints[i] = Integer.parseInt(split[i]);
        }
        return ints;
    }


    public void setRightEyeCenter(int x, int y, boolean isX) {
        yuvGLSurfaceView.setRightEyeOffset(x, y);
    }

    public int[] getRightEyeCenter() {
        String[] split = yuvGLSurfaceView.getRightEyeOffset().split(",");
        int[] ints = new int[2];
        for (int i = 0; i < ints.length; i++) {
            ints[i] = Integer.parseInt(split[i]);
        }
        return ints;
    }


    /**
     * ????????????
     */
    public int resetIPD() {
        popupTranslationOffset[0] = 0f;
        yuvGLSurfaceView.resetIpd();
        for (BaseItem baseItem : baseItemList) {
            baseItem.reset(Constant.IPD);
        }
        return 1;
    }

    /**
     * z
     * ????????????
     */
    public void resetALL() {
        yuvGLSurfaceView.resetAll();
        popupTranslationOffset[0] = 0f;
        popupTranslationOffset[1] = 0f;
        popupTranslationOffset[2] = 0f;
        popupTranslationOffset[3] = 0f;
        popupTranslationOffset[4] = 0f;
        popupScaleOffset[0] = 1.0f;
        popupScaleOffset[1] = 1.0f;
        popupTextScaleOffset[0] = 3;
        for (BaseItem baseItem : baseItemList) {
            baseItem.reset(Constant.ALL);
        }
        //????????????
        if (freezemode == 1) {
            freezemode = 0;
            yuvGLSurfaceView.quitFreezeMode();
        }
//        //??????????????????
        if (centerScaleTag == 1) {
            centerScaleTag = 0;
            yuvGLSurfaceView.setCenterScaleTag(0);
        }
//        //??????????????????
        if (fastScaleTag == 1) {
            fastScaleTag = 0;
            yuvGLSurfaceView.quitFastScale();
        }
        //??????????????????
//        if (cameraTag == 0x5801) {
//            if (camera5801 != null)
//                camera5801.stopPreview();
//            cameraTag = 0x5802;
////            currentCamera.openCamera();
//            if (camera5802 != null)
//                camera5802.startPreview(yuvGLSurfaceView);
//        }
        if (scale > 1) {
            if (camera5801 != null) {
                if (camera5801.isOpen() && camera5801.isPreviewing()) {
                    isRunningStop = true;
                    camera5801.stopPreview();
                }
            }
        }
        resetScale();
        //?????????????????????
        mUserMode = 0;
        setCameraModel(mUserMode, true);
    }


    public void setFirstPosition(int first) {
        recordPosition[0] = first;
    }

    public void setSecondPosition(int second) {
        recordPosition[1] = second;
    }

    public void setThirdPosition(int third) {
        recordPosition[2] = third;
    }

    public int getFirstPosition() {
        return recordPosition[0];
    }

    public int getSecondPosition() {
        return recordPosition[1];
    }

    public int getThirdPosition() {
        return recordPosition[2];
    }


    public Integer[] getPopupTextScaleOffset() {
        return popupTextScaleOffset;
    }


    public float[] getPopupScaleOffset() {
        return popupScaleOffset;
    }

    public void setDoubleColor(float[] colorValue) {
        yuvGLSurfaceView.setColorValue(colorValue);
    }

    public void setEdgeColor(float[] edgeValue) {
        yuvGLSurfaceView.setEdgeValue(edgeValue);
    }


    public void resetColorMode() {
        setCameraModel(mUserMode, false);
    }

    /**
     * ?????????????????????
     */
    private void initColorMode() {
        colorModes = new Constant.ColorMode[]{Constant.ColorMode.mode_color, Constant.ColorMode.mode_double_color, Constant.ColorMode.mode_gray,
                Constant.ColorMode.mode_reversegray, Constant.ColorMode.mode_edge, Constant.ColorMode.mode_fakecolor};
    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (baseItemList == null) return true;
        if (isRunningStop) return true;
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (canStart) {
                    canStart = false;
                    mGamePadManager.reStart();
                } else {
                    handler.sendEmptyMessageDelayed(What.DOUBLE_CLICK_INTERCEPT, 0);
                }
                return true;
        }
        return super.onKeyUp(keyCode, event);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (baseItemList == null) return true;
        if (isRunningStop) return true;
        if (event.getRepeatCount() == 0) {
            event.startTracking();
            Log.d("TAG", "onKeyDown: " + keyCode);
            switch (keyCode) {
                case KeyEvent.KEYCODE_G:
                    if (centerScaleTag == 1 || freezemode == 1) {
                        notifyError();
                        return true;
                    }
                    Log.e(TAG, "quickScaleTag  before " + centerScaleTag);
                    fastScaleTag = yuvGLSurfaceView.getFastScaleTag();
                    if (fastScaleTag == 0) {
                        yuvGLSurfaceView.setFastScaleTag();
                        CuiNiaoApp.textSpeechManager.speakNow("????????????");
                        showPrompt("????????????", CuiNiaoApp.isYellowMode ? R.mipmap.quick_shrink_y : R.mipmap.quick_shrink);
                    } else {
                        yuvGLSurfaceView.quitFastScale();
                        CuiNiaoApp.textSpeechManager.speakNow("????????????");
                        showPrompt("????????????", CuiNiaoApp.isYellowMode ? R.mipmap.quick_enlarge_y : R.mipmap.quick_enlarge);
                    }
                    Log.e(TAG, "quickScaleTag " + fastScaleTag);
                    fastScaleTag = yuvGLSurfaceView.getFastScaleTag();
                    break;
//                case KeyEvent.KEYCODE_B:
//                    break;
                case KeyEvent.KEYCODE_ENTER:
                    //???????????????OCR????????????OCR
                    Log.e(TAG, "isReading " + CuiNiaoApp.textSpeechManager.isOCRReading());
                    if (!CuiNiaoApp.mWifiUtils.isWifiConnect(this)) {
                        CuiNiaoApp.textSpeechManager.speakNow("?????????wifi?????????????????????wifi");
                        return true;
                    }
                    if (!isNetworkConnect) {
                        CuiNiaoApp.textSpeechManager.speakNow("?????????????????????????????????????????????");
//                        initNetWorkStatus();
                        return true;
                    }
                    if (!CuiNiaoApp.isOcrInit && ocrInitCount < 5) {
                        CuiNiaoApp.textSpeechManager.speakNow("???????????????????????????????????????3????????????");
                        CuiNiaoApp.initAccessToken();
                        ocrInitCount++;
                    } else if (ocrInitCount >= 5) {
                        CuiNiaoApp.textSpeechManager.speakNow("??????????????????????????????????????????");
                        return true;
                    }
                    if (CuiNiaoApp.textSpeechManager.isPauseOCRReading()) {
                        CuiNiaoApp.textSpeechManager.restartReading();
                        return true;
                    } else {
                        if (CuiNiaoApp.textSpeechManager.isOCRReading()) {
                            CuiNiaoApp.textSpeechManager.pauseReading();
                            return true;
                        }
                    }
                    CuiNiaoApp.textSpeechManager.speakNow("????????????", Constant.OCR_LEVEL);
                    showPrompt("????????????", CuiNiaoApp.isYellowMode ? R.mipmap.ocr_y : R.mipmap.ocr);
//                    capturePhoto();
                    startOcr();
                    break;
                case KeyEvent.KEYCODE_H:
                    if (freezemode == 1) {
                        freezemode = 0;
                        yuvGLSurfaceView.quitFreezeMode();
                        Log.d("TAG", "onKeyDown: ????????????");
                        CuiNiaoApp.textSpeechManager.speakNow("????????????");
                        showPrompt("????????????", CuiNiaoApp.isYellowMode ? R.mipmap.freeze_y : R.mipmap.freeze);
                    }
//                    if (CuiNiaoApp.textSpeechManager.isOCRReading()) {
                    CuiNiaoApp.textSpeechManager.shutDown(Constant.OCR_LEVEL);
//                        subtitleEnd();
//                    }
                    return true;
                case KeyEvent.KEYCODE_I:
                    CuiNiaoApp.textSpeechManager.speakNow("????????????");
                    Log.d(TAG, "showMenu: ?????????????????????");
                    showMenu();
                    break;
                case KeyEvent.KEYCODE_VOLUME_UP:
                    canStart = false;
                    currentTimeMillis = System.currentTimeMillis();
                    return true;
                case KeyEvent.KEYCODE_J:
//                case KeyEvent.KEYCODE_B:
                    long lastCurrentTimeMillis = currentTimeMillis;
                    currentTimeMillis = System.currentTimeMillis();
                    if (currentTimeMillis - lastCurrentTimeMillis < 300) {
                        if (fastScaleTag == 1) {
                            notifyError();
                            return true;
                        }
                        handler.removeMessages(What.DOUBLE_CLICK_INTERCEPT);
                        //??????????????????
                        centerScaleTag = yuvGLSurfaceView.getCenterScaleTag();
                        if (centerScaleTag == 0 || freezemode == 1) {
                            centerScaleTag = 1;
                            CuiNiaoApp.textSpeechManager.speakNow("????????????");
                            showPrompt("????????????", CuiNiaoApp.isYellowMode ? R.mipmap.center_scale_y : R.mipmap.center_scale);
                        } else {
                            centerScaleTag = 0;
                            CuiNiaoApp.textSpeechManager.speakNow("??????????????????");
                            showPrompt("??????????????????", CuiNiaoApp.isYellowMode ? R.mipmap.center_scale_y : R.mipmap.center_scale);
                        }
                        Log.e(TAG, "quickScaleTag " + centerScaleTag);
                        yuvGLSurfaceView.setCenterScaleTag(centerScaleTag);
                    } else {
                        handler.sendEmptyMessageDelayed(What.DOUBLE_CLICK_INTERCEPT, 310);
                    }
                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                case KeyEvent.KEYCODE_K:
//                case KeyEvent.KEYCODE_C:
                    //??????----
                    setScale(true);
                    return true;
                case KeyEvent.KEYCODE_D://---
                    //0 ?????? 5 ?????? 6 ???????????? 7,8,9,10 ????????????4????????? 11 ?????????
                    if (mUserMode <= 0) {
                        mUserMode = 6;
                    }
                    mUserMode--;
                    setCameraModel(mUserMode, true);
                    return true;
                case KeyEvent.KEYCODE_E://+++
                    if (mUserMode >= 5) {
                        mUserMode = -1;
                    }
                    mUserMode++;
                    setCameraModel(mUserMode, true);
                    return true;
                case KeyEvent.KEYCODE_BACK:
                    canStart = false;
                    currentTimeMillis = System.currentTimeMillis();
                    return true;
                case KeyEvent.KEYCODE_F:
//                    if (freezemode == 1) {
                    startSaveBitmap();
//                    }
                    return true;
                case KeyEvent.KEYCODE_A:
                    if (freezemode == 0) {
                        freezemode = 1;
                        yuvGLSurfaceView.setFreezeMode();
                        Log.d("TAG", "onKeyDown: ??????");
                        CuiNiaoApp.textSpeechManager.speakNow("??????");
                        //??????????????????
                        if (centerScaleTag == 1) {
                            centerScaleTag = 0;
                            yuvGLSurfaceView.setCenterScaleTag(centerScaleTag);
                        }
                        //??????????????????
                        if (fastScaleTag == 1) {
                            fastScaleTag = 0;
                            yuvGLSurfaceView.quitFastScale();
                        }
//                        showPrompt("??????", R.mipmap.freeze);
                        showPromptNoDissmiss("???OK???????????????", CuiNiaoApp.isYellowMode ? R.mipmap.freeze_y : R.mipmap.freeze);
                    } else {
                        freezemode = 0;
                        yuvGLSurfaceView.quitFreezeMode();
                        Log.d("TAG", "onKeyDown: ????????????");
                        CuiNiaoApp.textSpeechManager.speakNow("????????????");
                        showPrompt("????????????", CuiNiaoApp.isYellowMode ? R.mipmap.freeze_y : R.mipmap.freeze);
                    }
                    return true;
//                case KeyEvent.KEYCODE_B:
                case KeyEvent.KEYCODE_C:
                    resetCenterPosition();
                    CuiNiaoApp.textSpeechManager.speakNow("????????????");
                    return true;

            }
        } else {
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                    if (System.currentTimeMillis() - currentTimeMillis > 5000 && !canStart) {
                        canStart = true;
                        CuiNiaoApp.textSpeechManager.speakNow("??????????????????", Constant.BULETOOTH_LEVEL);
                        showPromptNoDissmiss("??????????????????", CuiNiaoApp.isYellowMode ? R.mipmap.conncet_y : R.mipmap.connect);
//                        Toast.makeText(this, "?????????????????????", Toast.LENGTH_SHORT).show();
                    }
                    return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * home?????????
     */
    public void callHomeKey() {
        Log.d(TAG, "callHomeKey: ?????????HOMEEEEEEEEEEEEEEEEEE");
        CuiNiaoApp.textSpeechManager.shutDownAll();
        CuiNiaoApp.textSpeechManager.reset();
        if (this.menuPopup != null && this.popupView != null) {
            menuPopup.dismiss();
            popupView.dismiss();
            setMenuPopup(null, null);
        }
        notifyViewChange(What.POPUP_DISMISS);
        //??????
        if (freezemode == 1) {
            freezemode = 0;
            yuvGLSurfaceView.quitFreezeMode();
        }
        //??????????????????
        if (centerScaleTag == 1) {
            centerScaleTag = 0;
            yuvGLSurfaceView.setCenterScaleTag(centerScaleTag);
        }
        //??????????????????
        if (fastScaleTag == 1) {
            fastScaleTag = 0;
            yuvGLSurfaceView.quitFastScale();
        }
//        if (cameraTag == 0x5801) {
//            if (camera5801 != null)
//                camera5801.stopPreview();
//            cameraTag = 0x5802;
//            if (camera5802 != null)
//                camera5802.startPreview(yuvGLSurfaceView);
//        }
        if (this.scale > 1) {
            if (camera5801 != null) {
                if (camera5801.isOpen() && camera5801.isPreviewing()) {
                    isRunningStop = true;
                    camera5801.stopPreview();
                }
            }
        }
        resetScale();
        //?????????????????????
        mUserMode = 0;
        setCameraModel(mUserMode, true);
        //??????????????????
        setFirstPosition(0);
        setSecondPosition(-1);
        setThirdPosition(-1);
//        showTopVeiw(-1);
        displayPreview();
        CuiNiaoApp.textSpeechManager.speakNow("???????????????");
    }


    /**
     * ????????????
     */
    private void startSaveBitmap() {
        isSaveBitmap = false;
        ocrcount = 1;
    }


    /**
     * ??????OCR??????
     */
    private void startOcr() {
        ocrcount = 0;
        isSaveBitmap = true;
    }


    public OnFrameListener onFrameListener = new OnFrameListener() {
        @Override
        public void onYuvFrame(byte[] yuv, float scale) {
//            if (yuv == null) return;
//            if (!isSaveBitmap) {
//                Log.d("TAG", "onDrawFrame: ??????????????????");
//                isSaveBitmap = true;
//                BitmapController bitmapController = new BitmapController();
//                bitmapController.data(yuv);
//                bitmapController.paramsScale(yuvGLSurfaceView.getScale());
//                bitmapController.size(width, height);
//                bitmapController.setCallback(handler);
//                bitmapController.execute();
//                return;
//            }
//            if (ocrcount == 0) {
//                ocrcount = 1;
//                OCRController ocrController = new OCRController();
//                ocrController.data(yuv);
//                ocrController.paramsScale(yuvGLSurfaceView.getScale());
//                ocrController.size(width, height);
//                ocrController.setCallback(handler);
//                ocrController.execute();
//            }
//            if (!isDecoding) {
//                if (wifiController == null) {
//                    wifiController = WifiController.getInstance();
//                    wifiController.setCallback(handler);
//                    wifiController.setGson(gson);
//                    wifiController.size(width, height);
//                    wifiController.updateWifiStatus(false);
//                }
//                wifiController.data(yuv);
//                wifiController.execute();
//            }

        }

        @Override
        public void onByteBufferFrame(ByteBuffer frame, int width, int height, int type, float scale) {
            if (frame == null) return;
            if (!isSaveBitmap) {
                Log.d("TAG", "onDrawFrame: ??????????????????");
                isSaveBitmap = true;
                BitmapController bitmapController = new BitmapController();
                bitmapController.buffer(frame);
                bitmapController.paramsScale(yuvGLSurfaceView.getScale());
                bitmapController.size(width, height);
                bitmapController.setCallback(handler);
                bitmapController.execute();
                return;
            }
            if (ocrcount == 0) {
                ocrcount = 1;
                OCRController ocrController = new OCRController();
                ocrController.buffer(frame);
                ocrController.paramsScale(yuvGLSurfaceView.getScale());
                ocrController.size(width, height);
                ocrController.type(type);
                ocrController.setCallback(handler);
                ocrController.execute();
            }
            if (!isDecoding) {
                if (wifiController == null) {
                    wifiController = WifiController.getInstance();
                    wifiController.setCallback(handler);
                    wifiController.setGson(gson);
                    wifiController.size(width, height);
                    wifiController.updateWifiStatus(false);
                }
                wifiController.buffer(frame);
                wifiController.execute();
            }
        }

        @Override
        public void onRGBFrame(byte[] rgb, float scale) {
//            if (rgb == null) return;
//            if (!isSaveBitmap) {
//                Log.d("TAG", "onDrawFrame: ??????????????????");
//                isSaveBitmap = true;
//                BitmapController bitmapController = new BitmapController();
//                bitmapController.data(rgb);
//                bitmapController.paramsScale(yuvGLSurfaceView.getScale());
//                bitmapController.size(width, height);
//                bitmapController.setCallback(handler);
//                bitmapController.execute();
//                return;
//            }
//            if (ocrcount == 0) {
//                ocrcount = 1;
//                OCRController ocrController = new OCRController();
//                ocrController.data(rgb);
//                ocrController.paramsScale(yuvGLSurfaceView.getScale());
//                ocrController.size(width, height);
//                ocrController.setCallback(handler);
//                ocrController.execute();
//            }
//            if (!isDecoding) {
//                if (wifiController == null) {
//                    wifiController = WifiController.getInstance();
//                    wifiController.setCallback(handler);
//                    wifiController.setGson(gson);
//                    wifiController.size(width, height);
//                    wifiController.updateWifiStatus(false);
//                }
//                wifiController.data(rgb);
//                wifiController.execute();
//            }
        }
    };


    public String regexEnd(float scale) {
        String scaleString = String.valueOf(scale);
        if (scaleString.endsWith(".0")) {
            return scaleString.substring(0, scaleString.length() - 2);
        }
        return scaleString;

    }

//    public int edgeModel = 7;

    public void setCameraModel(int model, boolean isShow) {
        yuvGLSurfaceView.setUserMode(colorModes[model].ordinal());
        if (!isShow) {
            return;
        }
        switch (colorModes[model].getImageId()) {
            case R.mipmap.fullcolor://??????
                showPrompt(colorModes[model].getColorName(), CuiNiaoApp.isYellowMode ? R.mipmap.fullcolor_y : colorModes[model].getImageId());
                break;
            case R.mipmap.doublecolor://??????
                showPrompt(colorModes[model].getColorName(), CuiNiaoApp.isYellowMode ? R.mipmap.doublecolor_y : colorModes[model].getImageId());
                break;
            case R.mipmap.graycolor://??????
                showPrompt(colorModes[model].getColorName(), CuiNiaoApp.isYellowMode ? R.mipmap.graycolor_y : colorModes[model].getImageId());
                break;
            case R.mipmap.reversegray://??????
                showPrompt(colorModes[model].getColorName(), CuiNiaoApp.isYellowMode ? R.mipmap.reversegray_y : colorModes[model].getImageId());
                break;
            case R.mipmap.edgecolor://??????
                showPrompt(colorModes[model].getColorName(), CuiNiaoApp.isYellowMode ? R.mipmap.edgecolor_y : colorModes[model].getImageId());
                break;
            case R.mipmap.fakecolor://?????????
                showPrompt(colorModes[model].getColorName(), CuiNiaoApp.isYellowMode ? R.mipmap.fakecolor_y : colorModes[model].getImageId());
                break;
        }
        CuiNiaoApp.textSpeechManager.speakNow(colorModes[model].getColorName());
    }


//    private void setUserMode(String itemName, int id) {
//        showPrompt(itemName, id);
//        CuiNiaoApp.textSpeechManager.speakNow(itemName);
//    }


    //    private QRScanMode qrScanMode;
    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case What.WIFI_SUCCES:
                    if (isDecoding) return;
                    isDecoding = true;
                    if (wifiController != null) {
                        wifiController.updateWifiStatus(true);
                        wifiController = null;
                    }
                    handler.removeMessages(What.WIFI_FAULIRE);
                    handler.removeMessages(What.WIFI_TIMEOUT);
                    setWifiStatus(true, CuiNiaoApp.mWifiUtils.getSSID());
                    notifyViewChange(What.WIFI_CONNECT);
                    break;
                case What.WIFI_FAULIRE:
                    if (isDecoding) return;
                    setWifiStatus(false, CuiNiaoApp.mWifiUtils.getFailReason(false));
                    notifyViewChange(What.WIFI_CONNECT);
                    break;
                case What.WIFI_TIMEOUT:
                    if (isDecoding) return;
                    setWifiStatus(false, "Wifi????????????");
                    notifyViewChange(What.WIFI_CONNECT);
                    handler.removeMessages(What.WIFI_TIMEOUT);
                    handler.sendEmptyMessageDelayed(What.WIFI_TIMEOUT, 15000);
                    break;
                case What.SAVE_BITMAP_SUCCESS:
                    CuiNiaoApp.textSpeechManager.speakNow("??????????????????");
                    showPrompt("??????????????????", CuiNiaoApp.isYellowMode ? R.mipmap.success_y : R.mipmap.success);
                    break;
                case What.SAVE_BITMAP_FAULIRE:
                    showPrompt("??????????????????????????????100???", CuiNiaoApp.isYellowMode ? R.mipmap.failure_y : R.mipmap.failure);
                    CuiNiaoApp.textSpeechManager.speakNow("??????????????????");
                    break;
                case What.PROMPT_GONE:
                    if (left_layout_bottom.getVisibility() == View.VISIBLE)
                        left_layout_bottom.setVisibility(View.GONE);
                    if (right_layout_bottom.getVisibility() == View.VISIBLE)
                        right_layout_bottom.setVisibility(View.GONE);
                    break;
                case What.DEVICE_DISCONNECT:
                    CuiNiaoApp.textSpeechManager.speakNow("?????????????????????");
                    break;
                case What.DOUBLE_CLICK_INTERCEPT:
                    //??????++++
                    setScale(false);
                    break;
                case What.WEAR_OFF:
                    //?????????????????????????????????
//                    observer.onDisconnect();

                    break;
                case What.OPEN_SUCCESS:
//                    camera5801.startPreview(mUVCCameraView);
                    Log.d(TAG, "handleMessage: openopenopen:" + msg.arg2);
                    break;
                case What.OPEN_FAULIRE:
                    break;
                case What.CLOSE_SUCCESS:
                    Log.d(TAG, "handleMessage: closeclose:" + msg.arg2);
                    break;
                case What.PREVIEW_SUCCES:
                    Log.d(TAG, "handleMessage: previewpreeeeeee:" + msg.arg2);
                    break;
                case What.PREVIEW_FAULIRE:
                    break;
                case What.STOP_SUCCES:
                    Log.d(TAG, "handleMessage: stopppppppp:" + msg.arg2);
                    if (msg.arg2 == 0x5802) {
                        if (camera5801 != null)
                            camera5801.startPreview(yuvGLSurfaceView);
                    } else {
                        if (camera5802 != null)
                            camera5802.startPreview(yuvGLSurfaceView);
                    }
                    isRunningStop = false;
                    break;
                case What.OCR_SUCCESS:
                    //OCR????????????
                    String ocrSuccessMessage = (String) msg.obj;
                    CuiNiaoApp.textSpeechManager.speakNow(ocrSuccessMessage, Constant.OCR_LEVEL);
                    break;
                case What.OCR_FAULIRE:
                    //OCR????????????
                    String ocrErrorMessage = (String) msg.obj;
                    CuiNiaoApp.textSpeechManager.speakNow(ocrErrorMessage, Constant.OCR_LEVEL);
                    showPrompt(ocrErrorMessage, CuiNiaoApp.isYellowMode ? R.mipmap.ocr_y : R.mipmap.ocr);
                    break;

            }
        }
    };


    /**
     * ????????????
     *
     * @param isDownScale ???????????????????????????
     */
    public void setScale(boolean isDownScale) {
        if (System.currentTimeMillis() - lastScaleCurrentTimeMillis < 500) {
            return;
        }
        if (isRunningStop)
            return;
        scale = getScale();
        if (isDownScale) {
            scale--;
        } else {
            scale++;
        }
        lastScaleCurrentTimeMillis = System.currentTimeMillis();
        if (scale < 0) {
            scale = 0;
        } else if (scale > scaleX.length - 1) {
            scale = scaleX.length - 1;
        }
//        this.scale = getScale();
        if (scaleX[scale] == 1.5f && isDownScale) {
            if (camera5801 != null) {
                isDownScale = false;
                isRunningStop = true;
                camera5801.stopPreview();

            }
        } else if (scaleX[scale] == 2.0f && !isDownScale) {
            if (camera5802 != null) {
                isDownScale = true;
                isRunningStop = true;
                camera5802.stopPreview();
            }
        }
        yuvGLSurfaceView.setScale(scaleX[scale]);
        this.scale = getScale();
        showPrompt(regexEnd(scaleX[scale]) + "X", CuiNiaoApp.isYellowMode ? R.mipmap.scale_y : R.mipmap.scale);
        CuiNiaoApp.textSpeechManager.speakNow("??????" + regexEnd(scaleX[scale]) + "???");
    }

    public void resetScale() {
        scale = 0;
        yuvGLSurfaceView.setScale(scaleX[scale]);
        this.scale = getScale();
    }


    public int getScale() {
        for (int i = 0; i < scaleX.length; i++) {
            if (scaleX[i] == yuvGLSurfaceView.getScale()) {
                this.scale = i;
                break;
            }
        }
        return this.scale;
    }


    /**
     * ??????????????????
     */
    public void showPrompt(String content, int imageResource) {
        if (isReading) return;
        handler.removeMessages(What.PROMPT_GONE);
        if (left_layout_bottom.getVisibility() == View.GONE)
            left_layout_bottom.setVisibility(View.VISIBLE);
        if (right_layout_bottom.getVisibility() == View.GONE)
            right_layout_bottom.setVisibility(View.VISIBLE);
        prompt1.setText(content);
        prompt2.setText(content);
        prompt1.setTextColor(CuiNiaoApp.isYellowMode ? getResources().getColor(R.color.yellow_mode_selected) : getResources().getColor(R.color.white));
        prompt2.setTextColor(CuiNiaoApp.isYellowMode ? getResources().getColor(R.color.yellow_mode_selected) : getResources().getColor(R.color.white));
        left_image_bottom.setImageResource(imageResource);
        right_image_bottom.setImageResource(imageResource);
        float horizantal = getPopupTranslationOffset()[0];
        float xLeft = getPopupTranslationOffset()[1];
        float yLeft = getPopupTranslationOffset()[2];
        float xRight = getPopupTranslationOffset()[3];
        float yRight = getPopupTranslationOffset()[4];
//        prompt1.setTranslationX(horizantal + xLeft);
//        prompt2.setTranslationX(-horizantal + xRight);
        Log.e("showPrompt", "horizantal" + horizantal + "xLeft" + xLeft + "xRight" + xRight);
        left_image_bottom.setTranslationX(-horizantal + xLeft);
        right_image_bottom.setTranslationX(horizantal + xRight);
        left_layout_bottom.setTranslationY(-yLeft);
        right_layout_bottom.setTranslationY(-yRight);
        int textScale = getPopupTextScaleOffset()[0];
        float scale = 1 + (textScale - 1) * 0.2f;
        float leftScale = getPopupScaleOffset()[0];
        float rightScale = getPopupScaleOffset()[1];
        float leftSize = MathUtils.floatMultiply(textSize, MathUtils.floatMultiply(scale, leftScale));
        float rightSize = MathUtils.floatMultiply(textSize, MathUtils.floatMultiply(scale, rightScale));
        prompt1.setTextSize(leftSize);
        prompt2.setTextSize(rightSize);
        left_image_bottom.setScaleX(MathUtils.floatMultiply(scale, leftScale));
        left_image_bottom.setScaleY(MathUtils.floatMultiply(scale, leftScale));
        right_image_bottom.setScaleX(MathUtils.floatMultiply(scale, rightScale));
        right_image_bottom.setScaleY(MathUtils.floatMultiply(scale, rightScale));
        //view???????????? ??????????????????
        float leftMargin = SightaidUtil.dpToPx(this, 39.0f);
        leftMargin = MathUtils.floatMultiply(leftMargin, MathUtils.floatMultiply(scale, leftScale)) - leftMargin;
        float rightMargin = SightaidUtil.dpToPx(this, 39.0f);
        rightMargin = MathUtils.floatMultiply(rightMargin, MathUtils.floatMultiply(scale, rightScale)) - rightMargin;
        prompt1.setTranslationX(-horizantal + xLeft + leftMargin);
        prompt2.setTranslationX(horizantal + xRight + rightMargin);
//        Log.e("showPrompt", "horizantal" + horizantal + "xLeft" + xLeft + "leftMargin" + leftMargin);
//        Log.e("showPrompt", "horizantal" + horizantal + "xRight" + xRight + "rightMargin" + rightMargin);
        handler.sendEmptyMessageDelayed(What.PROMPT_GONE, 1000);
    }


    public void setBatteryCharging(boolean isCharging) {
        this.isCharging = isCharging;
    }

    /**
     * ??????????????????
     *
     * @return
     */
    public int getBatteryImage(int mBatteryValue) {
        if (isCharging) return CuiNiaoApp.isYellowMode ? R.mipmap.battery_charging_y :
                R.mipmap.battery_charging;
        if (mBatteryValue > 75)
            return CuiNiaoApp.isYellowMode ? R.mipmap.battery_full_y : R.mipmap.battery_full;
        else if (mBatteryValue > 50)
            return CuiNiaoApp.isYellowMode ? R.mipmap.battery_seventy_five_y : R.mipmap.battery_seventy_five;
        else if (mBatteryValue > 25)
            return CuiNiaoApp.isYellowMode ? R.mipmap.battery_half_y : R.mipmap.battery_half;
        else
            return CuiNiaoApp.isYellowMode ? R.mipmap.battery_twenty_five_y : R.mipmap.battery_twenty_five;
    }

    /**
     * ??????????????????
     *
     * @return
     */
    public int getBatteryValue() {
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
    }


    public void showPromptNoDissmiss(String content, int imageResource) {
        if (left_layout_bottom.getVisibility() == View.GONE)
            left_layout_bottom.setVisibility(View.VISIBLE);
        if (right_layout_bottom.getVisibility() == View.GONE)
            right_layout_bottom.setVisibility(View.VISIBLE);
        prompt1.setText(content);
        prompt2.setText(content);
        prompt1.setTextColor(CuiNiaoApp.isYellowMode ? getResources().getColor(R.color.yellow_mode_selected) : getResources().getColor(R.color.white));
        prompt2.setTextColor(CuiNiaoApp.isYellowMode ? getResources().getColor(R.color.yellow_mode_selected) : getResources().getColor(R.color.white));
        left_image_bottom.setImageResource(imageResource);
        right_image_bottom.setImageResource(imageResource);
        float horizantal = getPopupTranslationOffset()[0];
        float xLeft = getPopupTranslationOffset()[1];
        float yLeft = getPopupTranslationOffset()[2];
        float xRight = getPopupTranslationOffset()[3];
        float yRight = getPopupTranslationOffset()[4];
        left_image_bottom.setTranslationX(horizantal + xLeft);
        right_image_bottom.setTranslationX(-horizantal + xRight);
        left_layout_bottom.setTranslationY(yLeft);
        right_layout_bottom.setTranslationY(yRight);
        int textScale = getPopupTextScaleOffset()[0];
        float scale = 1 + (textScale - 1) * 0.2f;
        float leftScale = getPopupScaleOffset()[0];
        float rightScale = getPopupScaleOffset()[1];
        float leftSize = MathUtils.floatMultiply(textSize, MathUtils.floatMultiply(scale, leftScale));
        float rightSize = MathUtils.floatMultiply(textSize, MathUtils.floatMultiply(scale, rightScale));
        prompt1.setTextSize(leftSize);
        prompt2.setTextSize(rightSize);
        left_image_bottom.setScaleX(MathUtils.floatMultiply(scale, leftScale));
        left_image_bottom.setScaleY(MathUtils.floatMultiply(scale, leftScale));
        right_image_bottom.setScaleX(MathUtils.floatMultiply(scale, rightScale));
        right_image_bottom.setScaleY(MathUtils.floatMultiply(scale, rightScale));
        //view???????????? ??????????????????
        float leftMargin = SightaidUtil.dpToPx(this, 39.0f);
        leftMargin = MathUtils.floatMultiply(leftMargin, leftScale) - leftMargin;
        float rightMargin = SightaidUtil.dpToPx(this, 39.0f);
        rightMargin = MathUtils.floatMultiply(rightMargin, rightScale) - rightMargin;
//        prompt1.setTranslationX(leftMargin);
//        prompt2.setTranslationX(rightMargin);
        prompt1.setTranslationX(horizantal + xLeft + leftMargin);
        prompt2.setTranslationX(-horizantal + xRight + rightMargin);
    }


    public boolean getWifiStatus() {
        if (CuiNiaoApp.mWifiUtils.isWifiConnect(this)) {
            return true;
        }
        return false;
    }


    @Override
    public void onGamePadConnectStateChanged(int state) {
        if (!mGamePadManager.isRunning()) {
            return;
        }
        Log.d("MainActivity", "onGamePadConnectStateChanged: state " + state);
        if (state == 2) {
            handler.removeMessages(What.DEVICE_DISCONNECT);
            CuiNiaoApp.textSpeechManager.speakNow("?????????????????????");
            showPrompt("?????????????????????", CuiNiaoApp.isYellowMode ? R.mipmap.success_y : R.mipmap.success);
        } else if (state == -1) {
//            Toast.makeText(this, "?????????????????????", Toast.LENGTH_SHORT).show();
            CuiNiaoApp.textSpeechManager.speakNow("?????????????????????");
            showPrompt("?????????????????????", CuiNiaoApp.isYellowMode ? R.mipmap.failure_y : R.mipmap.failure);
        }
    }

    @Override
    public boolean onBluetoothConnected(boolean isConnect) {
        if (!isConnect) {
//            Toast.makeText(this, "??????????????????", Toast.LENGTH_SHORT).show();
            handler.removeMessages(What.DEVICE_DISCONNECT);
            handler.sendEmptyMessageDelayed(What.DEVICE_DISCONNECT, 1500);
            return true;
        }
        return isConnect;
    }


    @Override
    public void add(ViewObserver viewObserver) {
        if (observerList == null) {
            observerList = new Vector<>();
        }
        if (observerList.contains(viewObserver)) return;
        observerList.add(viewObserver);
    }


    @Override
    public void notifyViewChange(int i) {
        if (observerList == null || observerList.size() <= 0) return;
        for (int j = 0; j < observerList.size(); j++) {
            ViewObserver viewObserver = observerList.get(j);
            if (viewObserver == null) return;
            switch (i) {
                case What.VIEW_CHANGE:
                    viewObserver.changeView();
                    break;
                case What.WIFI_CONNECT:
                    viewObserver.notifyWifiConnected();
                    break;
                case What.POPUP_DISMISS:
                    viewObserver.popupDismiss();
                    break;
                case What.CHANGE_CONTENT:
                    viewObserver.changeContent(content);
                    break;
            }
        }
    }

    @Override
    public void remove(ViewObserver viewObserver) {
        for (Iterator<ViewObserver> iterator = observerList.iterator(); iterator.hasNext(); ) {
            ViewObserver observer = iterator.next();
            if (observer == viewObserver) {
                Log.d("ThinGlassesActivity", "remove: ??????observer");
                iterator.remove();
                return;
            }
        }
        Log.d("ThinGlassesActivity", "remove: " + observerList.size());
    }


    /**
     * ?????????????????????????????????????????????????????????????????????
     */
    public void coverUpPreview() {
        if (coverView.getVisibility() == View.GONE || coverView.getVisibility() == View.INVISIBLE) {
            coverView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * ????????????????????????????????????
     */
    public void displayPreview() {
        if (coverView.getVisibility() == View.VISIBLE) {
            coverView.setVisibility(View.GONE);
        }
    }

    public void resetCenterPosition() {
        yuvGLSurfaceView.resetCenterPosition();
    }
}
