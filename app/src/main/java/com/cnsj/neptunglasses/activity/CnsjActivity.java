package com.cnsj.neptunglasses.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.TextView;

import com.cnsj.neptunglasses.R;
import com.cnsj.neptunglasses.view.gl.YuvGLSurfaceView;
import com.cnsj.neptunglasses.manager.CNSJCamera;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.widget.UVCCameraTextureView;

public class CnsjActivity extends AppCompatActivity{

    private YuvGLSurfaceView yuvGLSurfaceView;
    private USBMonitor usbMonitor;
    private int width = 1920, height = 1080;
//    private TextView textMills;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cnsj);
        yuvGLSurfaceView = findViewById(R.id.cnsj_camera_view);
        usbMonitor = new USBMonitor(getApplicationContext(), onDeviceConnectListener);
        startPermission();
//        textMills = findViewById(R.id.text_mills);
    }

    private CNSJCamera camera5801, camera5802, currentCamera;//5801 是长焦镜头，5802是短焦镜头
    USBMonitor.OnDeviceConnectListener onDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        @Override
        public void onAttach(UsbDevice device) {
//            if (device.getVendorId() == 0x0bda && device.getProductId() == 0x5801) {
        }

        @Override
        public void onDettach(UsbDevice device) {
            if (camera5801 != null) {
                if (camera5801.isOpen()) {
                    camera5801.closeCamera();
                }
                camera5801 = null;
            }
            if (camera5802 != null) {
                if (camera5802.isOpen()) {
                    camera5802.closeCamera();
                }
                camera5802 = null;
            }
            currentCamera = null;
            yuvGLSurfaceView.setScale(scaleFloats[0]);
            yuvGLSurfaceView.quitFreezeMode();
        }

        @Override
        public void onConnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock, boolean createNew) {
//            if (device.getVendorId() == 0x0bda && device.getProductId() == 0x5801) {
            if (device.getVendorId() == 0x0bda && device.getProductId() == 0x5875
                    || device.getVendorId() == 0x0bda && device.getProductId() == 0x5801
                    || device.getVendorId() == 0x1bcf && device.getProductId() == 0x28c4
            ) {
                if (camera5801 == null) return;
                camera5801.setControlBlock(ctrlBlock);
                if (currentCamera == camera5801) {
                    if (!currentCamera.isOpen()) {
                        currentCamera.openCamera();
                        currentCamera.startPreview(yuvGLSurfaceView);
                    }
                }
            }
            if (device.getVendorId() == 0x0bda && device.getProductId() == 0x5802) {
                if (camera5802 == null) return;
                camera5802.setControlBlock(ctrlBlock);
                if (currentCamera == camera5802) {
                    if (!currentCamera.isOpen()) {
                        currentCamera.openCamera();
                        currentCamera.startPreview(yuvGLSurfaceView);
                    }
                }
            }
        }

        @Override
        public void onDisconnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock) {

        }

        @Override
        public void onCancel(UsbDevice device) {

        }
    };


    private int freezemode = 0;
    private float[] scaleFloats = {1.0f, 1.5f, 2.0f, 3.0f, 5.0f, 10.0f, 20.0f, 25.0f};
    private int scale;
    private int mUserMode = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getRepeatCount() == 0) {
            event.startTracking();
            switch (keyCode) {
                case KeyEvent.KEYCODE_A:
                    if (freezemode == 0) {
                        freezemode = 1;
                        yuvGLSurfaceView.setFreezeMode();
                    } else {
                        freezemode = 0;
                        yuvGLSurfaceView.quitFreezeMode();
                    }
                    break;
                case KeyEvent.KEYCODE_J:
                case KeyEvent.KEYCODE_VOLUME_UP:
                    if (scale >= scaleFloats.length - 1) {
                        scale = scaleFloats.length - 2;
                    }
                    scale++;
                    if (scale == 2) {
//                        currentMills=System.currentTimeMillis();
                        yuvGLSurfaceView.setFreezeMode();
                        currentCamera.closeCamera();
                        currentCamera = camera5801;
                        currentCamera.openCamera();
                        currentCamera.startPreview(yuvGLSurfaceView);
                        yuvGLSurfaceView.quitFreezeMode();
//                        textMills.setText("摄像头切换时长:"+(System.currentTimeMillis()-currentMills)+"ms");
                    }
                    yuvGLSurfaceView.setScale(scaleFloats[scale]);

                    return true;
                case KeyEvent.KEYCODE_K:
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    if (scale <= 0) {
                        scale = 1;
                    }
                    scale--;
                    if (scale == 1) {
//                        currentMills=System.currentTimeMillis();
                        yuvGLSurfaceView.setFreezeMode();
                        currentCamera.closeCamera();
                        currentCamera = camera5802;
                        currentCamera.openCamera();
                        currentCamera.startPreview(yuvGLSurfaceView);
                        yuvGLSurfaceView.quitFreezeMode();
//                        textMills.setText("摄像头切换时长:"+(System.currentTimeMillis()-currentMills)+"ms");
                    }
                    yuvGLSurfaceView.setScale(scaleFloats[scale]);
                    return true;
                case KeyEvent.KEYCODE_D:
                    if (mUserMode <= 0) {
                        mUserMode = 10;
                    }
                    mUserMode--;
                    yuvGLSurfaceView.setUserMode(mUserMode);
                    return true;
                case KeyEvent.KEYCODE_E:
                    if (mUserMode >= 11) {
                        mUserMode = -1;
                    }
                    mUserMode++;
                    yuvGLSurfaceView.setUserMode(mUserMode);
                    return true;
                case KeyEvent.KEYCODE_B:
//                    if (constant<=1){
//                        constant=2;
//                    }
//                    constant--;
//                    yuvGLSurfaceView.setContrast(constant);
//                    if (saturation<=1){
//                        saturation=2;
//                    }
//                    saturation--;
//                    yuvGLSurfaceView.setSaturation(saturation);
                    return true;
                case KeyEvent.KEYCODE_C:
//                    if (constant>=5){
//                        constant=4;
//                    }
//                    constant++;
//                    yuvGLSurfaceView.setContrast(constant);
//                    if (saturation>=5){
//                        saturation=4;
//                    }
//                    saturation++;
//                    yuvGLSurfaceView.setSaturation(saturation);
                    return true;
            }
        } else {

        }
        return super.onKeyDown(keyCode, event);
    }
    private int saturation,constant,brightness;


    @Override
    protected void onStart() {
        super.onStart();
        if (usbMonitor != null) {
            usbMonitor.register();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (usbMonitor != null) {
            usbMonitor.unregister();
        }
    }

    private int MY_PERMISSIONS_REQUEST_CAMERA = 0;

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
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE}, MY_PERMISSIONS_REQUEST_CAMERA);
            }

        }
    }

    private int k = 0;
    private long currentMills;
    private int x = 0, z = 0;
    int ysize = width * height;
    int uvsize = width * height / 4;
    byte[] y = new byte[ysize];
    byte[] u = new byte[uvsize];
    byte[] v = new byte[uvsize];



    private void saveBitmap(byte[] y, byte[] u, byte[] v) {
        int[] pixels = new int[y.length];
        int alpha = 0xff << 24;
        Bitmap result = Bitmap.createBitmap(1920, 1080, Bitmap.Config.RGB_565);
        for (int j = 0; j < y.length; j++) {
            int grey = alpha | ((y[j] << 16) & 0x00FF0000) | ((y[j] << 8) & 0x0000FF00) | (y[j] & 0x000000FF);
            pixels[j] = grey;
        }
        result.setPixels(pixels, 0, 1920, 0, 0, 1920, 1080);

    }
}