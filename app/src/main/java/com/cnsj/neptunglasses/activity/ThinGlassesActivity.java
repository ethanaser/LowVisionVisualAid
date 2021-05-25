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
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.usb.UsbDevice;
import android.net.ConnectivityManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.PointerIcon;
import android.view.Surface;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cnsj.neptunglasses.R;
import com.cnsj.neptunglasses.WifiStateReceiver;
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
import com.cnsj.neptunglasses.receiver.BatteryReceiver;
import com.cnsj.neptunglasses.receiver.HomeWatcherReceiver;
import com.cnsj.neptunglasses.sensor.NeptungSensor;
import com.cnsj.neptunglasses.service.GlassesService;
import com.cnsj.neptunglasses.service.ZMQService;
import com.cnsj.neptunglasses.utils.CommonFileUtils;
import com.cnsj.neptunglasses.utils.SightaidUtil;
import com.cnsj.neptunglasses.utils.ThreadManager;
import com.cnsj.neptunglasses.view.ViewObservable;
import com.cnsj.neptunglasses.view.CustomMenuPopup;
import com.cnsj.neptunglasses.view.ViewObserver;
import com.cnsj.sightaid.model.QRScanMode;
import com.em3.vrhiddemos.manager.GamePadManager;
import com.google.gson.Gson;
import com.jiangdg.usbcamera.UVCCameraHelper;
import com.jiangdg.usbcamera.utils.MathUtils;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.lxj.xpopup.impl.FullScreenPopupView;
import com.serenegiant.usb.common.AbstractUVCCameraHandler;
import com.serenegiant.usb.widget.CameraViewInterface;
import com.serenegiant.usb.widget.GLDrawer2D1;
import com.serenegiant.usb.widget.OnEyesChangeListener;
import com.serenegiant.usb.widget.UVCCameraTextureView;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class ThinGlassesActivity extends BaseAppCompatActivity implements GamePadManager.GamePadConnectStateListener, ViewObservable<ViewObserver>, QRScanMode.QRScanResultListener {


    public static final String TAG = "ThinGlassesActivity";
    private View left_bottom, right_bottom, left_layout_bottom, right_layout_bottom,
            left_top, right_top, left_layout_top, right_layout_top, coverView;//ocrLeft, ocrRight,
    public UVCCameraTextureView mUVCCameraView;
    private UVCCameraHelper mCameraHelper;
    //    private Button devicesButton;
    private String[] deviceName;
    private int index = 0;
    private GamePadManager mGamePadManager;//蓝牙MSG
    private int width = 1920, height = 1080;

    public void setLeftEyeCenter(float leftXOffset, float leftYOffset) {
        popupTranslationOffset[1] = leftXOffset;
        popupTranslationOffset[2] = leftYOffset;
    }

    public void setRightEyeCenter(float rightXOffset, float rightOffset) {
        popupTranslationOffset[3] = rightXOffset;
        popupTranslationOffset[4] = rightOffset;
    }

    private Gson gson;

    public Gson getGson() {
        return gson;
    }

    public BaseItem[] firstMenu;
    private float doubleEyeScale = 1.0f;

    /**
     * 设定管状视野
     *
     * @param scale
     */
    public void setDoubleEyeScale(float scale) {
        mUVCCameraView.setDoubleEyeScale(scale);
    }

    public float getDoubleEyeScale() {
        return mUVCCameraView.getDoubleEyeScale();
    }

    private BatteryReceiver batteryReceiver;

    /**
     * 初始化电量监听
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
        wifiStateReceiver = new WifiStateReceiver();
        Intent intent = registerReceiver(wifiStateReceiver, filter);
        mHomeKeyReceiver = new HomeWatcherReceiver();
        IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mHomeKeyReceiver, homeFilter);
    }

    private HomeWatcherReceiver mHomeKeyReceiver;
    private WifiStateReceiver wifiStateReceiver;
    private BatteryManager batteryManager;

    /**
     * 监测wifi的连接状态
     *
     * @param connected
     */
    public void checkWifiConnect(boolean connected) {
        if (connected) {
            isConnect = true;
            handler.removeMessages(QR_TIMEOUT);
        } else {
            isConnect = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initReceiver();
        setContentView(R.layout.activity_thin_glasses);
        getWindow().getDecorView().setPointerIcon(PointerIcon.load(getResources(), R.drawable.pointer_spot_touch_icon));
        left_bottom = findViewById(R.id.main_layout_left);
        right_bottom = findViewById(R.id.main_layout_right);
        left_layout_bottom = findViewById(R.id.main_left);
        right_layout_bottom = findViewById(R.id.main_right);
        prompt1 = left_bottom.findViewById(R.id.main_msg_text);
        prompt2 = right_bottom.findViewById(R.id.main_msg_text);
        left_layout_top = findViewById(R.id.main_msg_top_layout_left);
        right_layout_top = findViewById(R.id.main_msg_top_layout_right);
        left_top = findViewById(R.id.main_msg_top_left);
        right_top = findViewById(R.id.main_msg_top_right);
        left_image_top = left_top.findViewById(R.id.main_msg_top_image);
        left_anti_top = left_top.findViewById(R.id.main_msg_top_anti);
        left_bluetooth_top = left_top.findViewById(R.id.main_msg_top_bluetooth);
        left_wifi_top = left_top.findViewById(R.id.main_msg_top_wifi);
        left_battery_top = left_top.findViewById(R.id.main_msg_top_battery);
        right_image_top = right_top.findViewById(R.id.main_msg_top_image);
        right_anti_top = right_top.findViewById(R.id.main_msg_top_anti);
        right_bluetooth_top = right_top.findViewById(R.id.main_msg_top_bluetooth);
        right_wifi_top = right_top.findViewById(R.id.main_msg_top_wifi);
        right_battery_top = right_top.findViewById(R.id.main_msg_top_battery);
        leftBattery = left_top.findViewById(R.id.main_msg_top_battery_text);
        rightBattery = right_top.findViewById(R.id.main_msg_top_battery_text);
        left_layout_top.setVisibility(View.GONE);
        right_layout_top.setVisibility(View.GONE);
        left_image_bottom = left_bottom.findViewById(R.id.main_msg_image);
        right_image_bottom = right_bottom.findViewById(R.id.main_msg_image);
        left_layout_bottom.setVisibility(View.GONE);
        right_layout_bottom.setVisibility(View.GONE);
        coverView = findViewById(R.id.cover_view);
        displayPreview();
        gson = new Gson();
        mGamePadManager = new GamePadManager(this);
        mUVCCameraView = findViewById(R.id.thin_camera_view);
        mUVCCameraView.setCallback(mCameraViewInterface);
        mCameraHelper = UVCCameraHelper.getInstance();
        //设置默认预览大小 3264*2448有点卡 640*480--madgaze
        mCameraHelper.setDefaultPreviewSize(width, height);
        batteryManager = (BatteryManager) (getApplicationContext().getSystemService(getApplicationContext().BATTERY_SERVICE));

        //设置默认帧格式，defalut为UVCCameraHelper.Frame_FORMAT_MPEG
        //如果使用mpeg无法录制mp4，请尝试yuv
//        mCameraHelper.setDefaultFrameFormat (UVCCameraHelper.FRAME_FORMAT_YUYV);
        mCameraHelper.setDefaultFrameFormat(UVCCameraHelper.FRAME_FORMAT_MJPEG);
        mCameraHelper.initUSBMonitor(this, mUVCCameraView, mUVCCameraHelperListener);
//        mCameraHelper.setOnPreviewFrameListener(mAbstractUVCCameraHandler);
        recordPosition = new int[3];
        setFirstPosition(0);
        setSecondPosition(-1);
        setThirdPosition(-1);
//        colorModes = new Constant.ColorMode[]{Constant.ColorMode.mode_color, doubleColor, Constant.ColorMode.mode_gray,
//                Constant.ColorMode.mode_reversegray, edgeColor, Constant.ColorMode.mode_fakecolor};
//        CuiNiaoApp.textSpeechManager.speakNow("开机成功了");
        initService();
        startPermission();
        initNetWorkStatus();
        initColorMode();
        isShortDevice = true;
    }

    /**
     * 进入授权界面进行授权
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


    //网络状态是否正常
    private boolean isNetworkConnect;

    public void setDistance(int distance, int direction) {
        mUVCCameraView.setIpd(distance);
    }

    /**
     * 眼部情况变化监听
     */
    OnEyesChangeListener onEyesChangeListener = new OnEyesChangeListener() {
        @Override
        public void onIpdChange(float ipdOffset) {
            float baseX = 2400 / 2;
            float ipd = MathUtils.floatMultiply(baseX, ipdOffset);//瞳距移动的像素值
            Log.d("TAG", "onIpdChange: offset：" + ipdOffset);
            Log.d("TAG", "onIpdChange: 移动的像素值：" + ipd);
            popupTranslationOffset[0] = ipd;
            notifyViewChange(VIEW_CHANGE);
        }

        @Override
        public void onLeftEyeOffsetChange(float leftX, float leftY) {
            float baseX = 2400 / 2;
            float baseY = 1200;
            float leftXOffset = MathUtils.floatMultiply(baseX, leftX);//左眼移动的像素值
            float leftYOffset = MathUtils.floatMultiply(baseY, leftY);//左眼移动的像素值
            Log.d("TAG", "onIpdChange: leftXOffset " + leftXOffset);
            Log.d("TAG", "onIpdChange: leftYOffset " + leftYOffset);
            popupTranslationOffset[1] = leftXOffset;
            popupTranslationOffset[2] = leftYOffset;
            notifyViewChange(VIEW_CHANGE);
        }

        @Override
        public void onRightEyeOffsetChange(float rightX, float rightY) {
            float baseX = 2400 / 2;
            float baseY = 1200;
            float rightXOffset = MathUtils.floatMultiply(baseX, rightX);//右眼移动的像素值
            float rightYOffset = MathUtils.floatMultiply(baseY, rightY);//右眼移动的像素值
            Log.d("TAG", "onIpdChange: rightXOffset " + rightXOffset);
            Log.d("TAG", "onIpdChange: rightYOffset " + rightYOffset);
            popupTranslationOffset[3] = rightXOffset;
            popupTranslationOffset[4] = rightYOffset;
            notifyViewChange(VIEW_CHANGE);
        }

        @Override
        public void onLeftScaleChange(float leftScale) {
            //左眼的缩放，直接用
            Log.d("TAG", "onIpdChange: leftScale " + leftScale);
            popupScaleOffset[0] = leftScale;
            notifyViewChange(VIEW_CHANGE);
        }

        @Override
        public void onRightScaleChange(float rightScale) {
            //右眼的缩放，直接用
            Log.d("TAG", "onIpdChange: rightScale " + rightScale);
            popupScaleOffset[1] = rightScale;
            notifyViewChange(VIEW_CHANGE);
        }
    };

    /**
     * 眼部情况变化监听
     */
    public void initNetWorkStatus() {
        isNetworkConnect = false;
        ThreadManager.getInstance().execute(new Runnable() {
            @Override
            public void run() {
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
        });
    }

    public void setLeftEyeScale(float scale) {
        mUVCCameraView.setLeftSubScale(scale);
    }

    public float getLeftEyeScale() {
        return mUVCCameraView.getLeftScale();
    }

    public void setLeftScale(float mLeftSubScale) {
        popupScaleOffset[0] = mLeftSubScale;
    }

    public void setRightScale(float mRightSubScale) {
        popupScaleOffset[1] = mRightSubScale;
    }

    public float getRightEyeScale() {
        return mUVCCameraView.getRightScale();
    }

    public void setRightEyeScale(float scale) {
        mUVCCameraView.setRightSubScale(scale);
    }

    public List<BaseItem> getBaseItemList() {
        return baseItemList;
    }


    /**
     * mCallback是接口CameraViewInterface的一个对象。回调，它用于听创建或删除的surfaceView
     */
    CameraViewInterface.Callback mCameraViewInterface = new CameraViewInterface.Callback() {

        @Override
        public void onSurfaceCreated(CameraViewInterface view, Surface surface) {
            Log.e(TAG, "onSurfaceCreated " + mCameraHelper.isCameraOpened());
            if (mCameraHelper.isCameraOpened()) {
                mCameraHelper.startPreview(mUVCCameraView);
            }
        }

        @Override
        public void onSurfaceChanged(CameraViewInterface view, Surface surface, int width, int height) {

        }

        @Override
        public void onSurfaceDestroy(CameraViewInterface view, Surface surface) {
            if (mCameraHelper.isCameraOpened()) {
                mCameraHelper.stopPreview();
            }
        }
    };

    boolean isOpen = false;
    UsbDevice currentDevices, shortFocusDevices, longFocusDevices;//短焦广角，长焦
    private boolean isShortDevice = true;
    /**
     * mDevConnectListener是接口UVCCameraHelper的一个对象。OnMyDevConnectListener，它用于监听检测和连接USB设备。
     */
    UVCCameraHelper.OnMyDevConnectListener mUVCCameraHelperListener = new UVCCameraHelper.OnMyDevConnectListener() {

        @Override
        public void onAttachDev(UsbDevice device) {
            Log.e(TAG, "onAttachDev");
            List<UsbDevice> deviceLists = mCameraHelper.getUsbDeviceList();
            if (deviceLists == null) return;
            deviceName = null;
            deviceName = new String[deviceLists.size()];
            for (int i = 0; i < deviceLists.size(); i++) {
                deviceName[i] = deviceLists.get(i).getDeviceName();
                Log.d("TAG", "onAttachDev: " + deviceLists.get(i).getProductId() + " V:" + deviceLists.get(i).getVendorId() + " PATH:" + deviceLists.get(i).getDeviceName());
            }
            // 5802
            if (device.getVendorId() == 0x0bda && device.getProductId() == 0x5801
                    || device.getVendorId() == 0x1bcf && device.getProductId() == 0x28c4
                    || device.getVendorId() == 0x0bda && device.getProductId() == 0x5875
//                    ||device.getVendorId() == 0x1bcf && device.getProductId() == 0x28c4
            ) {
//                ToastUtils.showShort("摄像头已连接");
                shortFocusDevices = device;
            }
            if (device.getVendorId() == 0x0bda && device.getProductId() == 0x5802) {
                longFocusDevices = device;
            }
            if (isShortDevice) {
                currentDevices = shortFocusDevices;
            } else {
                currentDevices = longFocusDevices;
            }
            if (isOpen) return;
            if (currentDevices != null) {
                if (mCameraHelper.isCameraOpened()) {
                    mCameraHelper.stopPreview();
                }
                // request open permission
                if (mCameraHelper != null) {

                    Log.d("TAG", "onAttachDev: +startPreview:" + device.getDeviceName());
                    mCameraHelper.requestPermission(currentDevices);
                    isOpen = true;
                }
            }

        }

        @Override
        public void onDettachDev(UsbDevice device) {
            Log.e(TAG, "onDettachDev");
            // close camera
            mCameraHelper.closeCamera();
            currentDevices = null;
            shortFocusDevices = null;
            longFocusDevices = null;
            isOpen = false;
        }

        @Override
        public void onConnectDev(UsbDevice device, boolean isConnected) {
            Log.e(TAG, "onConnectDev:" + isConnected);
            if (!isConnected) {
//                ToastUtils.showShort("连接失败");
            } else {
//                ToastUtils.showShort("设备已连接");
                if (myBinder != null) {
                    myBinder.registerSensor();
                }
                mUVCCameraView.setOnEyesChangeListener(onEyesChangeListener);
                if (baseItemList == null) {
                    mUVCCameraView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            firstMenu = new BaseItem[]{new EyeSetItem(ThinGlassesActivity.this),
                                    new DisplaySetItem(ThinGlassesActivity.this),
                                    new VoiceSetItem(ThinGlassesActivity.this),
                                    new PhotoAlbumItem(ThinGlassesActivity.this),
                                    new BasicSetItem(ThinGlassesActivity.this),
                                    new ResetSetItem(ThinGlassesActivity.this),
                                    new UpdateItem(ThinGlassesActivity.this),
                                    new ManualItem(ThinGlassesActivity.this),
                                    new VersionItem(ThinGlassesActivity.this)};
                            baseItemList = new ArrayList<>();
                            baseItemList.addAll(Arrays.asList(firstMenu));
                        }
                    }, 510);
                }
            }
        }

        @Override
        public void onDisConnectDev(UsbDevice device) {
            Log.e(TAG, "onDisConnectDev");
            //ToastUtils.showShort("断开链接");
            isOpen = false;
        }
    };

    private GlassesService.MyBinder myBinder;
    private ZMQService.ZMQBinder zmqBinder;

    private void initService() {
        Intent intent = new Intent(this, GlassesService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
//        Intent intent1 = new Intent(this, ZMQService.class);
//        bindService(intent1, zmqConn, Context.BIND_AUTO_CREATE);
    }

    ServiceConnection zmqConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            zmqBinder = (ZMQService.ZMQBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

//    NeptungSensor.OnSensorChangeListener listener = new NeptungSensor.OnSensorChangeListener() {
//        @Override
//        public void onSensorChange(float[] quat) {
//            for (int i = 0; i < quat.length; i++) {
//            }
//            if (mUVCCameraView != null) {
//                mUVCCameraView.onSensorChange(quat);
//            }
//        }
//
//        @Override
//        public void onQuaternion(float[] values) {
//            if (zmqBinder != null) {
//                zmqBinder.quat(values);
//            }
//        }
//    };

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myBinder = (GlassesService.MyBinder) service;
//            myBinder.setSensorChangeListener(listener);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private float[] popupTranslationOffset = new float[5];//平移尺寸 pos 0 瞳距 1 2左侧位置 3 4 右侧位置

    public void setEyeTranslation(float eye_translation) {
        popupTranslationOffset[0] = eye_translation;
    }


    AbstractUVCCameraHandler.OnPreViewResultListener mAbstractUVCCameraHandler = new AbstractUVCCameraHandler.OnPreViewResultListener() {
        @Override
        public void onPreviewResult(byte[] nv21Yuv) {
//            float fps = mUVCCameraView.getFps();
//            Log.d(TAG, "onPreviewResult: fps:" + fps);
//            mUVCCameraView.updateFps();
            Bitmap bmp = null;
            YuvImage image = new YuvImage(nv21Yuv, ImageFormat.NV21, width, height, null);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compressToJpeg(new Rect(0, 0, width, height), 100, stream);
            bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
            //saveBitmap(bmp);
            Log.e(TAG, "mAbstractUVCCameraHandler " + bmp);
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (bmp != null && !isDecoding) {
                Message message = Message.obtain();
                message.what = SCAN_QRCODE;
                message.obj = bmp;
                handler.sendMessage(message);
            }
        }

    };


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //屏蔽触摸事件
        return true;
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent ev) {
        Log.d("TAG", "onTouchEvent: 触摸板事件");
        Log.d("TAG", "onTouchEvent: " + ev.getAction());
        return true;
    }


    public SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    /**
     * 根据时间戳保存图片
     *
     * @param bitmap
     */
    private void saveBitmapByGL(Bitmap bitmap) {
        StringBuffer sb = new StringBuffer();
        sb.append(Environment.getExternalStorageDirectory().getAbsolutePath());
        sb.append("/");
        sb.append("DCIM/freeze_image");//目前将定格图片保存在这个目录中。
        File dir = new File(sb.toString());
        if (!dir.exists()) {
            dir.mkdir();
        }
        int size = CommonFileUtils.Companion.getFreezeImageSize(sb.toString());
        if (size >= 100) {
            handler.sendEmptyMessage(SAVE_PHOTO_ERROR);
            return;
        }
        sb.append("/");
        sb.append("cnsj-");
        sb.append(simpleDateFormat.format(new Date()));
        sb.append(".jpg");
        File file = new File(sb.toString());
        if (file.exists()) {
            file.delete();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            //文件输出流
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            sb.setLength(0);
            handler.sendEmptyMessage(SAVE_PHOTO_SUCCESS);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 根据时间戳保存图片
     *
     * @param bitmap
     */
    private void saveBitmap(Bitmap bitmap) {
        StringBuffer sb = new StringBuffer();
        sb.append(Environment.getExternalStorageDirectory().getAbsolutePath());
        sb.append("/");
        sb.append("DCIM/freeze_image");//目前将定格图片保存在这个目录中。
        File dir = new File(sb.toString());
        if (!dir.exists()) {
            dir.mkdir();
        }
        sb.append("/");
        sb.append("cnsj");
        sb.append(".png");
        File file = new File(sb.toString());
        if (file.exists()) {
            file.delete();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            //文件输出流
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            sb.setLength(0);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public int getSaturation() {
        return mUVCCameraView.getSaturation();
    }

    public void setSaturation(int saturation) {
        mUVCCameraView.setSaturation(saturation);
    }

    public int getContrast() {
        return mUVCCameraView.getContrast();
    }

    public void setContrast(int contrast) {
        mUVCCameraView.setContrast(contrast);
    }

    public int getBrightness() {
        return mUVCCameraView.getBrightness();
    }

    public void setBrightness(int brightness) {
        mUVCCameraView.setBrightness(brightness);
    }


    private Integer[] popupTextScaleOffset = {1};

    public void setPopupTextScaleOffset(int popupTextScaleOffset) {
        this.popupTextScaleOffset[0] = popupTextScaleOffset;
    }


    @Override
    public void onResume() {
        super.onResume();
        if (mCameraHelper == null)
            return;
        boolean cameraOpened = mCameraHelper.isCameraOpened();
        Log.d("TAG", "onResume: cameraOpened" + cameraOpened);
        showTopVeiw(-1);
//        if (mTtsManager != null)
//            mTtsManager.resumeSpeaking();
    }

    @Override
    public void onStart() {
        super.onStart();

        // 注册USB事件广播
        if (mCameraHelper != null) {
            mCameraHelper.registerUSB();
        }
        if (currentDevices != null) {
            mCameraHelper.requestPermission(currentDevices);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
//        mUVCCameraView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        // 取消注册USB事件广播
        if (mCameraHelper != null) {
            mCameraHelper.unregisterUSB();
        }
        if (currentDevices != null) {
            mCameraHelper.stopPreview();
        }
    }


    private void stopReceiver() {
        if (wifiStateReceiver != null) {
            unregisterReceiver(wifiStateReceiver);
            wifiStateReceiver = null;
        }
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
        ocrInitCount = 0;
        if (myBinder != null) {
            unbindService(conn);
        }
        if (zmqBinder != null) {
            unbindService(zmqConn);
        }
        stopReceiver();
        // 释放uvc相机资源
        if (mCameraHelper != null) {
            mCameraHelper.release();
        }
    }

    public int scale = 0;
    private int currentIpd = 62;
    private int mUserMode = 0;
    private List<BaseItem> baseItemList;
    private FullScreenPopupView menuPopup;
    private BasePopupView popupView;


    public float[] getPopupTranslationOffset() {
        return popupTranslationOffset;
    }


    public void notifyError() {
        //TODO
//        mMyGLSurfaceView.playNoOperateSound();
        CuiNiaoApp.mVolumeManager.playNoOperateSound();
    }

    private boolean isConnect;
    private String failReason;

    public void setWifiStatus(boolean isConnect, String failReason) {
        this.isConnect = isConnect;
        this.failReason = failReason;
    }

    public String getFailReason() {
        return this.failReason;
    }

    private boolean isDecoding = false;

    /**
     * 开启扫描二维码
     */
    int ocrcount = 0;

    public void qrScanStart() {
        mCameraHelper.setOnPreviewFrameListener(mAbstractUVCCameraHandler);
        isDecoding = false;
        content = "扫码中...";
        notifyViewChange(CHANGE_CONTENT);
        handler.removeMessages(QR_TIMEOUT);
        handler.sendEmptyMessageDelayed(QR_TIMEOUT, 15000);
        ocrcount = 1;
        isSaveBitmap = true;
    }


    public boolean isWifiConnected() {
        return isConnect;
    }

    //显示菜单
    private void showMenu() {
        if (baseItemList == null) {
            return;
        }
        for (BaseItem baseItem : baseItemList) {//显示菜单式加载一下保存的配置
            baseItem.load();
        }
        menuPopup = new CustomMenuPopup(this, ThinGlassesActivity.this);
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
        mUVCCameraView.setLeftEyeOffset(x, y);
    }

    public int[] getLeftEyeCenter() {
        String[] split = mUVCCameraView.getLeftEyeOffset().split(",");
        int[] ints = new int[2];
        for (int i = 0; i < ints.length; i++) {
            ints[i] = Integer.parseInt(split[i]);
        }
        return ints;
    }


    public void setRightEyeCenter(int x, int y, boolean isX) {
        mUVCCameraView.setRightEyeOffset(x, y);
    }

    public int[] getRightEyeCenter() {
        String[] split = mUVCCameraView.getRightEyeOffset().split(",");
        int[] ints = new int[2];
        for (int i = 0; i < ints.length; i++) {
            ints[i] = Integer.parseInt(split[i]);
        }
        return ints;
    }

    private String itemName;

    public String getItemName() {
        return itemName;
    }

    /**
     * 重置瞳距
     */
    public int resetIPD() {
        popupTranslationOffset[0] = 0f;
        mUVCCameraView.resetIpd();
        for (BaseItem baseItem : baseItemList) {
            baseItem.reset(Constant.IPD);
        }
        return 1;
    }

    /**
     * z
     * 重置所有
     */
    public void resetALL() {
        mUVCCameraView.resetAll();
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
        //退出定格
        if (freezemode == 1) {
            freezemode = 0;
            mUVCCameraView.quitFreezeMode();
        }
//        //退出中心放大
        if (quickScaleTag == 1) {
            quickScaleTag = 0;
            mUVCCameraView.setQuickScaleTag(0);
        }
//        //退出快速放大
        if (quickShrink == 1) {
            quickShrink = 0;
            mUVCCameraView.quitQuickShrink();
        }
        scale = 0;
        setScale(scaleX[scale]);
        //恢复至全彩模式
        mUserMode = 0;
        setCameraModel(mUserMode);
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    private int[] recordPosition;

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

    /**
     * 获取二级菜单显示logo
     *
     * @return
     */
    public int getLogoImages() {
        return logoImages;
    }

    private int logoImages;

    public void setLogoImages(int logoImages) {
        this.logoImages = logoImages;
    }


    public Integer[] getPopupTextScaleOffset() {
        return popupTextScaleOffset;
    }

    private float[] popupScaleOffset = {1.0f, 1.0f};//缩放比例 pos 0 左侧 1 右侧

    public float[] getPopupScaleOffset() {
        return popupScaleOffset;
    }

    public void setDoubleColor(Constant.ColorMode doubleColor) {
        this.doubleColor = doubleColor;
        if (colorModes != null) {
            colorModes[1] = doubleColor;
        }
    }

    public Constant.ColorMode getDoubleColor() {
        return this.doubleColor;
    }

    public void setEdgeColor(Constant.ColorMode edgeColor) {
        this.edgeColor = edgeColor;
        if (colorModes != null) {
            colorModes[4] = edgeColor;
        }
    }

    public Constant.ColorMode getEdgeColor() {
        return this.edgeColor;
    }

    public void resetColorMode() {
        setCameraModel(colorModes[mUserMode].ordinal());
    }

    /**
     * 初始化显示模式
     */
    private void initColorMode() {
        colorModes = new Constant.ColorMode[]{Constant.ColorMode.mode_color, doubleColor, Constant.ColorMode.mode_gray,
                Constant.ColorMode.mode_reversegray, edgeColor, Constant.ColorMode.mode_fakecolor};
    }

    //0 全彩 5 灰度 6 灰度反色 7,8,9,10 描边模式4种颜色 11 伪彩色
    public float[] scaleX = new float[]{1.0f, 1.5f, 2.0f, 3.0f, 5.0f, 10.0f, 20.f, 25.f};
    //    public float[] scaleX = new float[]{1.0f, 1.5f, 2.0f, 3.0f, 5.0f, 10.0f};
    //设定颜色模式
    private Constant.ColorMode[] colorModes;
    private Constant.ColorMode doubleColor;
    private Constant.ColorMode edgeColor;
    public int quickScaleTag = 0;
    public int quickShrink = 0;

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (canStart) {
                    canStart = false;
                    mGamePadManager.reStart();
                } else {
                    handler.sendEmptyMessageDelayed(DOUBLE_CLICK_INTERCEPT, 0);
                }
                return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private int ocrInitCount = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (baseItemList == null) return true;
        if (event.getRepeatCount() == 0) {
            event.startTracking();
            Log.d("TAG", "onKeyDown: " + keyCode);
            switch (keyCode) {
                case KeyEvent.KEYCODE_G:
                    if (quickScaleTag == 1) {
                        notifyError();
                        return true;
                    }
                    Log.e(TAG, "quickScaleTag  before " + quickScaleTag);
                    quickShrink = mUVCCameraView.getQuickShrink();
                    if (quickShrink == 0) {
                        mUVCCameraView.setQuickShrink();
                        CuiNiaoApp.textSpeechManager.speakNow("快速缩小");
                        showPrompt("快速缩小", R.mipmap.quick_shrink);
                    } else {
                        mUVCCameraView.quitQuickShrink();
                        CuiNiaoApp.textSpeechManager.speakNow("快速放大");
                        showPrompt("快速放大", R.mipmap.quick_enlarge);
                    }
                    Log.e(TAG, "quickScaleTag " + quickShrink);
                    quickShrink = mUVCCameraView.getQuickShrink();
                    break;
//                case KeyEvent.KEYCODE_B:
//                    break;
                case KeyEvent.KEYCODE_ENTER:        //拍照
                    //如果正在读OCR直接打断OCR
                    Log.e(TAG, "isReading " + CuiNiaoApp.textSpeechManager.isOCRReading());
                    if (CuiNiaoApp.textSpeechManager.isOCRReading()) {
                        CuiNiaoApp.textSpeechManager.shutDown(Constant.OCR_LEVEL);
//                        subtitleEnd();
                        return true;
                    }
                    if (!CuiNiaoApp.mWifiUtils.isWifiConnect(this)) {
                        CuiNiaoApp.textSpeechManager.speakNow("检测到wifi未连接，请检查wifi");
                        return true;
                    }
                    if (!isNetworkConnect) {
                        CuiNiaoApp.textSpeechManager.speakNow("检测到网络异常，请检查网络状态");
                        initNetWorkStatus();
                        return true;
                    }
                    if (!CuiNiaoApp.isOcrInit && ocrInitCount < 5) {
                        CuiNiaoApp.textSpeechManager.speakNow("文字识别引擎初始化异常，请3秒后重试");
                        CuiNiaoApp.initAccessToken();
                        ocrInitCount++;
                    } else if (ocrInitCount >= 5) {
                        CuiNiaoApp.textSpeechManager.speakNow("文字识别引擎异常，请联系客服");
                        return true;
                    }
                    CuiNiaoApp.textSpeechManager.speakNow("正在解析", Constant.OCR_LEVEL);
                    showPrompt("正在解析", R.mipmap.ocr);
//                    capturePhoto();
                    startOcr();
                    break;
                case KeyEvent.KEYCODE_H:
                    if (freezemode == 1) {
                        freezemode = 0;
                        mUVCCameraView.quitFreezeMode();
                        Log.d("TAG", "onKeyDown: 退出定格");
                        CuiNiaoApp.textSpeechManager.speakNow("退出定格");
                        showPrompt("退出定格", R.mipmap.freeze);
                    }
                    if (CuiNiaoApp.textSpeechManager.isOCRReading()) {
                        CuiNiaoApp.textSpeechManager.shutDown(Constant.OCR_LEVEL);
//                        subtitleEnd();
                    }
                    return true;
                case KeyEvent.KEYCODE_I:
                    CuiNiaoApp.textSpeechManager.speakNow("显示菜单");
                    Log.d(TAG, "showMenu: 我要显示菜单啊");
                    showMenu();
                    break;
                case KeyEvent.KEYCODE_VOLUME_UP:
                    canStart = false;
                    currentTimeMillis = System.currentTimeMillis();
                    return true;
                case KeyEvent.KEYCODE_J:
                case KeyEvent.KEYCODE_B:
                    long lastCurrentTimeMillis = currentTimeMillis;
                    currentTimeMillis = System.currentTimeMillis();
                    if (currentTimeMillis - lastCurrentTimeMillis < 300) {
                        if (quickShrink == 1) {
                            notifyError();
                            return true;
                        }
                        handler.removeMessages(DOUBLE_CLICK_INTERCEPT);
                        //测试中心放大
                        quickScaleTag = mUVCCameraView.getQuickScaleTag();
                        if (quickScaleTag == 0) {
                            quickScaleTag = 1;
                            CuiNiaoApp.textSpeechManager.speakNow("中心放大");
                            showPrompt("中心放大", R.mipmap.center_scale);
                        } else {
                            quickScaleTag = 0;
                            CuiNiaoApp.textSpeechManager.speakNow("退出中心放大");
                            showPrompt("退出中心放大", R.mipmap.center_scale);
                        }
                        Log.e(TAG, "quickScaleTag " + quickScaleTag);
                        mUVCCameraView.setQuickScaleTag(quickScaleTag);
                    } else {
                        handler.sendEmptyMessageDelayed(DOUBLE_CLICK_INTERCEPT, 310);
                    }
                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                case KeyEvent.KEYCODE_K:
                case KeyEvent.KEYCODE_C:
                    //缩小----
                    if (scale > 0) {
                        scale -= 1;
                        CuiNiaoApp.textSpeechManager.speakNow("放大" + regexEnd(scaleX[scale]) + "倍");
                        setScale(scaleX[scale]);
                        if (mUVCCameraView.getScale() == 1.5f) {
                            currentDevices = shortFocusDevices;
                            isShortDevice = true;
                            mCameraHelper.closeCamera();
                            mCameraHelper.requestPermission(currentDevices);
                        }
                        showPrompt(regexEnd(scaleX[scale]) + "X", R.mipmap.scale);
                    } else {
                        CuiNiaoApp.textSpeechManager.speakNow("放大" + regexEnd(scaleX[0]) + "倍");
                        showPrompt(regexEnd(scaleX[0]) + "X", R.mipmap.scale);
                    }
                    return true;
                case KeyEvent.KEYCODE_D://---
                    //0 全彩 5 灰度 6 灰度反色 7,8,9,10 描边模式4种颜色 11 伪彩色
                    if (doubleColor == null || edgeColor == null) return true;
                    if (mUserMode <= 0) {
                        mUserMode = 6;
                    }
                    mUserMode--;
                    setCameraModel(colorModes[mUserMode].ordinal());
                    if (mUserMode == 0) {
                        setUserMode("全彩", R.mipmap.fullcolor);
                    } else if (mUserMode == 2) {
                        setUserMode("灰度", R.mipmap.graycolor);
                    } else if (mUserMode == 3) {
                        setUserMode("反色", R.mipmap.reversegray);
                    } else if (mUserMode == 5) {
                        setUserMode("伪彩色", R.mipmap.fakecolor);
                    } else if (mUserMode == 4) {
                        setUserMode("描边", R.mipmap.edgecolor);
                    } else if (mUserMode == 1) {
                        setUserMode("两色", R.mipmap.doublecolor);
                    }
                    return true;
                case KeyEvent.KEYCODE_E://+++
                    if (doubleColor == null || edgeColor == null) return true;
                    if (mUserMode >= 5) {
                        mUserMode = -1;
                    }
                    mUserMode++;
                    setCameraModel(colorModes[mUserMode].ordinal());
                    if (mUserMode == 0) {
                        setUserMode("全彩", R.mipmap.fullcolor);
                    } else if (mUserMode == 2) {
                        setUserMode("灰度", R.mipmap.graycolor);
                    } else if (mUserMode == 3) {
                        setUserMode("反色", R.mipmap.reversegray);
                    } else if (mUserMode == 5) {
                        setUserMode("伪彩色", R.mipmap.fakecolor);
                    } else if (mUserMode == 4) {
                        setUserMode("描边", R.mipmap.edgecolor);
                    } else if (mUserMode == 1) {
                        setUserMode("两色", R.mipmap.doublecolor);
                    }
                    return true;
                case KeyEvent.KEYCODE_BACK:
                    canStart = false;
                    currentTimeMillis = System.currentTimeMillis();
                    return true;
                case KeyEvent.KEYCODE_F:
//                    takePic=0;
//                    Intent intent = new Intent();
//                    ComponentName componentName = new ComponentName("com.em3.cnsj", "com.unity3d.player.UnityPlayerActivity");
//                    intent.setComponent(componentName);
//                    startActivityForResult(intent, 0);
//                    if (freezemode == 1) {
                    startSaveBitmap();
//                    }
                    return true;
                case KeyEvent.KEYCODE_A:
                    if (freezemode == 0) {
                        freezemode = 1;
                        mUVCCameraView.setFreezeMode();
                        Log.d("TAG", "onKeyDown: 定格");
                        CuiNiaoApp.textSpeechManager.speakNow("定格");
//                        showPrompt("定格", R.mipmap.freeze);
                        showPromptNoDissmiss("按OK键保存图片", R.mipmap.freeze);
                    } else {
                        freezemode = 0;
                        mUVCCameraView.quitFreezeMode();
                        Log.d("TAG", "onKeyDown: 退出定格");
                        CuiNiaoApp.textSpeechManager.speakNow("退出定格");
                        showPrompt("退出定格", R.mipmap.freeze);
                    }
                    return true;

            }
        } else {
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                    if (System.currentTimeMillis() - currentTimeMillis > 5000 && !canStart) {
                        canStart = true;
                        CuiNiaoApp.textSpeechManager.speakNow("遥控器寻找中");
                        showPromptNoDissmiss("遥控器寻找中", R.mipmap.connect);
//                        Toast.makeText(this, "开始连接遥控器", Toast.LENGTH_SHORT).show();
                    }
                    return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * home键回调
     */
    public void callHomeKey() {
        Log.d(TAG, "callHomeKey: 调用了HOMEEEEEEEEEEEEEEEEEE");
        CuiNiaoApp.textSpeechManager.shutDownAll();
        CuiNiaoApp.textSpeechManager.reset();
        CuiNiaoApp.textSpeechManager.speakNow("返回主界面");
        if (this.menuPopup != null && this.popupView != null) {
            menuPopup.dismiss();
            popupView.dismiss();
            setMenuPopup(null, null);
        }
        notifyViewChange(POPUP_DISMISS);
        //定格
        if (freezemode == 1) {
            freezemode = 0;
            mUVCCameraView.quitFreezeMode();
        }
        //退出中心放大
        if (quickScaleTag == 1) {
            quickScaleTag = 0;
            mUVCCameraView.setQuickScaleTag(quickScaleTag);
        }
        //退出快速放大
        if (quickShrink == 1) {
            quickShrink = 0;
            mUVCCameraView.quitQuickShrink();
        }
        this.scale = 0;
        mUVCCameraView.setScale(scaleX[this.scale]);
        //恢复至全彩模式
        mUserMode = 0;
        setCameraModel(mUserMode);
        //重置菜单位置
        setFirstPosition(0);
        setSecondPosition(-1);
        setThirdPosition(-1);
        showTopVeiw(-1);
    }

//    public void startUnityPhoto() {
//        Intent intent = new Intent();
//        ComponentName componentName = new ComponentName("com.em3.cnsj", "com.unity3d.player.UnityPlayerActivity");
//        intent.setComponent(componentName);
//        startActivityForResult(intent, 0);
//    }

    /**
     * 定格保存图像
     */
    boolean isSaveBitmap = false;

    /**
     * 保存图片
     */
    private void startSaveBitmap() {
        mUVCCameraView.setOnDrawFrameListener(onDrawFrameListener);
        isSaveBitmap = false;
        ocrcount = 1;
    }

    /**
     * 开始OCR识别
     */
    private void startOcr() {
        mUVCCameraView.setOnDrawFrameListener(onDrawFrameListener);
        ocrcount = 0;
        isSaveBitmap = true;
    }

    GLDrawer2D1.OnDrawFrameListener onDrawFrameListener = new GLDrawer2D1.OnDrawFrameListener() {
        @Override
        public void onDrawFrame(ByteBuffer byteBuffer, int width, int height) {
            if (byteBuffer == null) return;
            if (!isSaveBitmap) {
                Log.d("TAG", "onDrawFrame: 保存图片开始");
                isSaveBitmap = true;
                mUVCCameraView.setOnDrawFrameListener(null);
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                bitmap.copyPixelsFromBuffer(byteBuffer);
                Matrix m = new Matrix();
                m.setScale(-1, -1);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, m, true);
                saveBitmapByGL(bitmap);
                return;
            }
            if (ocrcount == 0) {
                ocrcount = 1;
                mUVCCameraView.setOnDrawFrameListener(null);
                Log.d("TAG", "onDrawFrame: ocrocr bitmap");
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                bitmap.copyPixelsFromBuffer(byteBuffer);
                Matrix m = new Matrix();
                m.setScale(-1, -1);
                int offsetx = 0, offsety = 0;
                if (mUVCCameraView.getScale() == 1.0f) {
                    m.preScale(0.5f, 0.5f);
                } else if (mUVCCameraView.getScale() == 1.5f) {
                    m.preScale(0.5f, 0.5f);
                    offsetx = width / 6;
                    offsety = height / 6;
                    width = width * 2 / 3;
                    height = height * 2 / 3;
                } else if (mUVCCameraView.getScale() == 2f) {
                    offsetx = width / 4;
                    offsety = height / 4;
                    width = width / 2;
                    height = height / 2;
                } else if (mUVCCameraView.getScale() == 3f) {
                    offsetx = width / 3;
                    offsety = height / 3;
                    width = width / 3;
                    height = height / 3;
                } else if (mUVCCameraView.getScale() == 5f) {
                    offsetx = width * 2 / 5;
                    offsety = height * 2 / 5;
                    width = width / 5;
                    height = height / 5;
                } else if (mUVCCameraView.getScale() == 10f) {
                    offsetx = width * 9 / 20;
                    offsety = height * 9 / 20;
                    width = width / 10;
                    height = height / 10;
                } else if (mUVCCameraView.getScale() == 20f) {
                    offsetx = width * 19 / 40;
                    offsety = height * 19 / 40;
                    width = width / 20;
                    height = height / 20;
                } else if (mUVCCameraView.getScale() == 25f) {
                    offsetx = width * 12 / 25;
                    offsety = height * 12 / 25;
                    width = width / 25;
                    height = height / 25;
                }
                bitmap = Bitmap.createBitmap(bitmap, offsetx, offsety, width, height, m, true);
                saveScaleBitmap(bitmap);
//                OCRManager.recGeneralBasic(getBaseContext(), filePath, new OCRManager.ServiceListener() {
//                    @Override
//                    public void onResult(String result) {
//                        Log.e(TAG, " OCR " + result);
//                        NormalOcrResult normalOcrResult = gson.fromJson(result, NormalOcrResult.class);
//                        if (normalOcrResult == null || normalOcrResult.getWords_result_num() < 1) {
//                            CuiNiaoApp.textSpeechManager.speakNow("未识别到文字", Constant.OCR_LEVEL);
//                            showPrompt("未识别到文字", R.mipmap.ocr);
//                            return;
//                        }
//                        StringBuffer stringBuffer = new StringBuffer();
//                        for (NormalOcrResult.WordsResultBean word :
//                                normalOcrResult.getWords_result()) {
//                            String words = word.getWords();
//                            stringBuffer.append(words);
//                        }
//                        CuiNiaoApp.textSpeechManager.speakNow(stringBuffer.toString(), Constant.OCR_LEVEL);
//                    }
//
//                    @Override
//                    public void onError(String errorString) {
//                        CuiNiaoApp.textSpeechManager.speakNow("未识别到文字", Constant.OCR_LEVEL);
//                        showPrompt("未识别到文字", R.mipmap.ocr);
//                    }
//                });
            }
        }
    };


    public String regexEnd(float scale) {
        String scaleString = String.valueOf(scale);
        if (scaleString.endsWith(".0")) {
            return scaleString.substring(0, scaleString.length() - 2);
        }
        return scaleString;

    }

    public int edgeModel = 7;

    public void setCameraModel(int model) {
        mUVCCameraView.setUserMode(model);
    }

    public void setEdgeModel(int model) {
        mUVCCameraView.setUserMode(model);
    }


    private void setUserMode(String itemName, int id) {
        showPrompt(itemName, id);
        CuiNiaoApp.textSpeechManager.speakNow(itemName);
    }

    private String filePath = Environment.getExternalStorageDirectory().getPath() + "/cnsj/ocr.jpg";

    private void saveScaleBitmap(Bitmap bitmap) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            //文件输出流
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private boolean isReading = false;
    private TextView prompt1, prompt2, leftBattery, rightBattery;
    private ImageView left_image_bottom, right_image_bottom, left_image_top, right_image_top,
            left_anti_top, right_anti_top, left_bluetooth_top, right_bluetooth_top, left_wifi_top,
            right_wifi_top, left_battery_top, right_battery_top;
    private int textSize = 25;
    private static final int PROMPT_GONE = 1;
    private static final int TOP_GONE = 7;
    private static final int DOUBLE_CLICK_INTERCEPT = 2;
    private static final int QR_TIMEOUT = 3;
    private static final int SCAN_QRCODE = 9;
    private static final int SAVE_PHOTO_ERROR = 6;
    private static final int SAVE_PHOTO_SUCCESS = 8;
    private QRScanMode qrScanMode;
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TOP_GONE:
                    if (left_layout_top.getVisibility() == View.VISIBLE)
                        left_layout_top.setVisibility(View.GONE);
                    if (right_layout_top.getVisibility() == View.VISIBLE)
                        right_layout_top.setVisibility(View.GONE);
                    break;
                case SCAN_QRCODE:
                    Bitmap bitmap = (Bitmap) msg.obj;
                    if (bitmap != null) {
                        if (!isDecoding) {
                            if (qrScanMode == null)
                                qrScanMode = new QRScanMode(ThinGlassesActivity.this);
                            qrScanMode.prepare(true);
                            if (qrScanMode.isPrepared()) {
                                qrScanMode.start(bitmap);
                            }
                        }
                    }
                    break;
                case WIFI_DELAY:
                    Log.d("MainActivity", "onWifiQRCode: " + isConnect);
                    if (isConnect) {
                        CuiNiaoApp.mWifiUtils.updateWifiInfo();
                        setWifiStatus(true, CuiNiaoApp.mWifiUtils.getSSID());
                    } else {
                        setWifiStatus(false, CuiNiaoApp.mWifiUtils.getFailReason(false));
                    }
                    notifyViewChange(WIFI_CONNECT);
                    break;
                case SAVE_PHOTO_SUCCESS:
                    CuiNiaoApp.textSpeechManager.speakNow("已保存至相册");
                    showPrompt("已保存至相册", R.mipmap.success);
                    break;
                case SAVE_PHOTO_ERROR:
                    showPrompt("保存图片失败数量超过100张", R.mipmap.failure);
                    CuiNiaoApp.textSpeechManager.speakNow("保存图片失败");
                    break;
                case QR_TIMEOUT:
                    cancelQR();
                    isDecoding = true;
                    setWifiStatus(false, "Wifi连接超时");
                    notifyViewChange(WIFI_CONNECT);
                    break;
                case PROMPT_GONE:
                    if (left_layout_bottom.getVisibility() == View.VISIBLE)
                        left_layout_bottom.setVisibility(View.GONE);
                    if (right_layout_bottom.getVisibility() == View.VISIBLE)
                        right_layout_bottom.setVisibility(View.GONE);
                    break;
                case DEVICE_DISCONNECT:
                    CuiNiaoApp.textSpeechManager.speakNow("遥控器已经断开");
                    break;
                case DOUBLE_CLICK_INTERCEPT:
                    //放大++++
                    if (scale < scaleX.length - 1) {
                        scale += 1;
                        CuiNiaoApp.textSpeechManager.speakNow("放大" + regexEnd(scaleX[scale]) + "倍");
                        setScale(scaleX[scale]);
                        if (mUVCCameraView.getScale() == 2.0f) {
                            currentDevices = longFocusDevices;
                            isShortDevice = false;
                            mCameraHelper.closeCamera();
                            mCameraHelper.requestPermission(currentDevices);
                        }
                        showPrompt(regexEnd(scaleX[scale]) + "X", R.mipmap.scale);
                    } else {
                        CuiNiaoApp.textSpeechManager.speakNow("放大" + regexEnd(scaleX[scaleX.length - 1]) + "倍");
                        showPrompt(regexEnd(scaleX[scaleX.length - 1]) + "X", R.mipmap.scale);
                    }
//                    canStart = false;
//                    currentTimeMillis = System.currentTimeMillis();
//                    //单击事件
//                    mUserScale = mMyGLSurfaceView.getScale();
//                    mMyGLSurfaceView.setScale(mUserScale + 1);
//                    mUserScale = mMyGLSurfaceView.getScale();
//                    CuiNiaoApp.textSpeechManager.speakNow(getString(R.string.magnifier, formatZoomRate(mUserScale)));
//                    showPrompt(formatZoomRate(mUserScale) + "X", R.mipmap.scale);
                    break;
            }
        }
    };

    public void setScale(float scale) {
        mUVCCameraView.setScale(scale);
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public int getScale() {
        return scale;
    }

    public void cancelQR() {
        if (qrScanMode != null) {
            qrScanMode.cancel();
            qrScanMode = null;
            handler.removeMessages(QR_TIMEOUT);
        }
    }


    /**
     * 显示内容提示
     */
    public void showPrompt(String content, int imageResource) {
        if (isReading) return;
        handler.removeMessages(PROMPT_GONE);
        if (left_layout_bottom.getVisibility() == View.GONE)
            left_layout_bottom.setVisibility(View.VISIBLE);
        if (right_layout_bottom.getVisibility() == View.GONE)
            right_layout_bottom.setVisibility(View.VISIBLE);
        prompt1.setText(content);
        prompt2.setText(content);
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
        //view间的间距 同步放大间距
        float leftMargin = SightaidUtil.dpToPx(this, 39.0f);
        leftMargin = MathUtils.floatMultiply(leftMargin, MathUtils.floatMultiply(scale, leftScale)) - leftMargin;
        float rightMargin = SightaidUtil.dpToPx(this, 39.0f);
        rightMargin = MathUtils.floatMultiply(rightMargin, MathUtils.floatMultiply(scale, rightScale)) - rightMargin;
        prompt1.setTranslationX(-horizantal + xLeft + leftMargin);
        prompt2.setTranslationX(horizantal + xRight + rightMargin);
        Log.e("showPrompt", "horizantal" + horizantal + "xLeft" + xLeft + "leftMargin" + leftMargin);
        Log.e("showPrompt", "horizantal" + horizantal + "xRight" + xRight + "rightMargin" + rightMargin);
        handler.sendEmptyMessageDelayed(PROMPT_GONE, 1000);
    }


    private boolean isCharging;

    public void setBatteryCharging(boolean isCharging) {
        this.isCharging = isCharging;
    }

    /**
     * 获取电池状态
     *
     * @return
     */
    private int getBatteryImage(int mBatteryValue) {
        if (isCharging) return R.mipmap.battery_charging;
        if (mBatteryValue > 75)
            return R.mipmap.battery_full;
        else if (mBatteryValue > 50)
            return R.mipmap.battery_seventy_five;
        else if (mBatteryValue > 25)
            return R.mipmap.battery_half;
        else
            return R.mipmap.battery_twenty_five;
    }


    public void showPromptNoDissmiss(String content, int imageResource) {
        if (left_layout_bottom.getVisibility() == View.GONE)
            left_layout_bottom.setVisibility(View.VISIBLE);
        if (right_layout_bottom.getVisibility() == View.GONE)
            right_layout_bottom.setVisibility(View.VISIBLE);
        prompt1.setText(content);
        prompt2.setText(content);
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
        //view间的间距 同步放大间距
        float leftMargin = SightaidUtil.dpToPx(this, 39.0f);
        leftMargin = MathUtils.floatMultiply(leftMargin, leftScale) - leftMargin;
        float rightMargin = SightaidUtil.dpToPx(this, 39.0f);
        rightMargin = MathUtils.floatMultiply(rightMargin, rightScale) - rightMargin;
//        prompt1.setTranslationX(leftMargin);
//        prompt2.setTranslationX(rightMargin);
        prompt1.setTranslationX(horizantal + xLeft + leftMargin);
        prompt2.setTranslationX(-horizantal + xRight + rightMargin);
    }

    private void showTopVeiw(int i) {
        handler.removeMessages(TOP_GONE);
        if (left_layout_top.getVisibility() == View.GONE)
            left_layout_top.setVisibility(View.VISIBLE);
        if (right_layout_top.getVisibility() == View.GONE)
            right_layout_top.setVisibility(View.VISIBLE);
        //左侧变化的图标
//        left_image_top.setImageResource(i);
//        right_image_top.setImageResource(i);
        //电量
        int mBatteryValue = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        left_battery_top.setImageResource(getBatteryImage(mBatteryValue));
        right_battery_top.setImageResource(getBatteryImage(mBatteryValue));
        leftBattery.setText(mBatteryValue + "%");
        rightBattery.setText(mBatteryValue + "%");
//        //防抖
//        if (!mMyGLSurfaceView.ismStabOn()) {
//            left_anti_top.setVisibility(View.GONE);
//            right_anti_top.setVisibility(View.GONE);
//        } else {
//            left_anti_top.setVisibility(View.VISIBLE);
//            right_anti_top.setVisibility(View.VISIBLE);
//        }
        //遥控器连接状态
        boolean bluetoothConnect = mGamePadManager.getBoundedDevice();
        left_bluetooth_top.setImageResource(bluetoothConnect ? R.mipmap.bluetooth_success : R.mipmap.bluetooth_failure);
        right_bluetooth_top.setImageResource(bluetoothConnect ? R.mipmap.bluetooth_success : R.mipmap.bluetooth_failure);
        //wifi连接状态
        left_wifi_top.setImageResource(getWifiStatus() ? R.mipmap.wifi_success : R.mipmap.wifi_failure);
        right_wifi_top.setImageResource(getWifiStatus() ? R.mipmap.wifi_success : R.mipmap.wifi_failure);
        float horizantal = getPopupTranslationOffset()[0];
        float xLeft = getPopupTranslationOffset()[1];
        float yLeft = getPopupTranslationOffset()[2];
        float xRight = getPopupTranslationOffset()[3];
        float yRight = getPopupTranslationOffset()[4];
        left_layout_top.setTranslationX(horizantal + xLeft);
        left_layout_top.setTranslationY(yLeft);
        right_layout_top.setTranslationX(-horizantal + xRight);
        right_layout_top.setTranslationY(yRight);
        int textScale = getPopupTextScaleOffset()[0];
        float scale = 0.6f + (textScale - 1) * 0.2f;
        float leftScale = getPopupScaleOffset()[0];
        float rightScale = getPopupScaleOffset()[1];
        left_layout_top.setScaleX(MathUtils.floatMultiply(scale, leftScale));
        left_layout_top.setScaleY(MathUtils.floatMultiply(scale, leftScale));
        right_layout_top.setScaleX(MathUtils.floatMultiply(scale, rightScale));
        right_layout_top.setScaleY(MathUtils.floatMultiply(scale, rightScale));
        handler.sendEmptyMessageDelayed(TOP_GONE, 1000);
    }

    public int freezemode = 0;
//    private int takePic = 1;

    private boolean canStart;

    private long currentTimeMillis = 0l;

    private boolean getWifiStatus() {
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
            handler.removeMessages(DEVICE_DISCONNECT);
            CuiNiaoApp.textSpeechManager.speakNow("遥控器连接成功");
            showPrompt("遥控器连接成功", R.mipmap.success);
        } else if (state == -1) {
//            Toast.makeText(this, "遥控器连接失败", Toast.LENGTH_SHORT).show();
            CuiNiaoApp.textSpeechManager.speakNow("遥控器连接失败");
            showPrompt("遥控器连接失败", R.mipmap.failure);
        }
    }

    @Override
    public boolean onBluetoothConnected(boolean isConnect) {
        if (!isConnect) {
//            Toast.makeText(this, "遥控器已断开", Toast.LENGTH_SHORT).show();
            handler.removeMessages(DEVICE_DISCONNECT);
            handler.sendEmptyMessageDelayed(DEVICE_DISCONNECT, 1500);
            return true;
        }
        return isConnect;
    }

    private Vector<ViewObserver> observerList;

    @Override
    public void add(ViewObserver viewObserver) {
        if (observerList == null) {
            observerList = new Vector<>();
        }
        if (observerList.contains(viewObserver)) return;
        observerList.add(viewObserver);
    }

    private static final int VIEW_CHANGE = 1;
    private static final int WIFI_CONNECT = 3;
    private static final int POPUP_DISMISS = 4;
    private static final int CHANGE_CONTENT = 5;
    private String content;

    @Override
    public void notifyViewChange(int i) {
        if (observerList == null || observerList.size() <= 0) return;
        for (int j = 0; j < observerList.size(); j++) {
            ViewObserver viewObserver = observerList.get(j);
            if (viewObserver == null) return;
            switch (i) {
                case VIEW_CHANGE:
                    viewObserver.changeView();
                    break;
                case WIFI_CONNECT:
                    viewObserver.notifyWifiConnected();
                    break;
                case POPUP_DISMISS:
                    viewObserver.popupDismiss();
                    break;
                case CHANGE_CONTENT:
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
                Log.d("ThinGlassesActivity", "remove: 移除observer");
                iterator.remove();
                return;
            }
        }
        Log.d("ThinGlassesActivity", "remove: " + observerList.size());
    }


    private static final int DEVICE_DISCONNECT = 10;
    private static final int WIFI_DELAY = 4;

    @Override
    public void onWifiQRCode(@NotNull String ssid, @NotNull String password) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isDecoding) return;
                Log.d("MainActivity", "onWifiQRCode: 解析|" + ssid + "|" + password + "|");
                cancelQR();
                handler.removeMessages(QR_TIMEOUT);
                isDecoding = true;
                if (ssid == null || password == null) {
                    CuiNiaoApp.textSpeechManager.speakNow("二维码解析不完整");
                    return;
                }
//                CuiNiaoApp.textSpeechManager.speakNow("wifi连接中");
                content = "连接中...";
                notifyViewChange(CHANGE_CONTENT);
                String lastWifi = null;
                isConnect = false;
                if (CuiNiaoApp.mWifiUtils.isWifiConnect(ThinGlassesActivity.this)) {
                    lastWifi = CuiNiaoApp.mWifiUtils.getSSID();
                }
                if (lastWifi != null && lastWifi.equals(ssid)) {//若当前连接wifi未目标wifi 直接返回成功
                    isConnect = true;
                    setWifiStatus(true, CuiNiaoApp.mWifiUtils.getSSID());
                    notifyViewChange(WIFI_CONNECT);
                } else {//否则需要重新连接wifi
                    CuiNiaoApp.mWifiUtils.connectNet(ssid, password, "WPA");
                    handler.sendEmptyMessageDelayed(WIFI_DELAY, 2500);
                }
            }
        });
    }

    @Override
    public void onDecodingError() {

    }


    /**
     * 隐藏图像
     */
    public void coverUpPreview() {
        if (coverView.getVisibility() == View.GONE || coverView.getVisibility() == View.INVISIBLE) {
            coverView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 显示图像
     */
    public void displayPreview() {
        if (coverView.getVisibility() == View.VISIBLE) {
            coverView.setVisibility(View.GONE);
        }
    }


}
