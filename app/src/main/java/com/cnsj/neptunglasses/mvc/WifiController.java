package com.cnsj.neptunglasses.mvc;


import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import com.cnsj.neptunglasses.app.CuiNiaoApp;
import com.cnsj.neptunglasses.constant.What;
import com.cnsj.neptunglasses.qr.WifiMode;
import com.cnsj.neptunglasses.utils.ThreadManager;
import com.google.gson.Gson;

import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;

public class WifiController implements Controller, ImageConfiguration, Callback {


    private WifiRunnable wifiRunnable;
    private static WifiController instance;
    private ArrayBlockingQueue<ByteBuffer> yuvQueue;//

    private WifiController() {
        yuvQueue = new ArrayBlockingQueue<>(1);
    }

    public static WifiController getInstance() {
        if (instance == null) {
            synchronized (WifiController.class) {
                if (instance == null) {
                    instance = new WifiController();
                }
            }
        }
        return instance;
    }

    private Gson gson;
    private Handler handler;
    private int width, height;
    private byte[] yuv;
    private float scale;
    private boolean isRun;
    private ByteBuffer frame;

    @Override
    public void execute() {
        if (isRun) return;
        if (wifiRunnable == null) {
            wifiRunnable = new WifiRunnable();
            isRun = true;
            ThreadManager.getInstance().execute(wifiRunnable);
        }
    }

    @Override
    public void setCallback(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void size(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void data(byte[] yuv) {
        this.yuv = yuv;
//        if (yuvQueue != null) {
//            try {
//                yuvQueue.add(this.yuv);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
    }

    @Override
    public void buffer(ByteBuffer frame) {
        this.frame = frame;
        if (yuvQueue != null) {
            try {
                yuvQueue.add(this.frame);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void paramsScale(float scale) {
        this.scale = scale;
    }

    public void setGson(Gson gson) {
        this.gson = gson;
    }

    private boolean isFinish;

    /**
     * 用于结束或者取消WiFi解析
     *
     * @param isConnect
     */
    public void updateWifiStatus(boolean isConnect) {
        this.isFinish = isConnect;
        if (isConnect) {
            isRun = false;
            wifiRunnable = null;
        }
    }


    class WifiRunnable implements Runnable {

        WifiMode wifiMode;

        @Override
        public void run() {
            while (isRun) {
                if (isFinish) {
                    isRun = false;
                    return;
                }
//                byte[] yuv;
//                try {
//                    yuv = yuvQueue.take();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                    continue;
//                }
//                if (yuv == null) {
//                    continue;
//                }
                ByteBuffer frame;
                try {
                    frame = yuvQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    continue;
                }
                if (frame == null) {
                    continue;
                }
//                Bitmap bitmap = YuvUtils.convertToBitmap(yuv, width, height);
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
//                ByteBuffer bufffer = ByteBuffer.wrap(yuv);
//                //bufffer.position(0);
//                bufffer.rewind();
//                bufffer.position(0);
                bitmap.copyPixelsFromBuffer(frame);
                if (wifiMode == null) {
                    wifiMode = new WifiMode(WifiController.this.gson);
                }
                String result = wifiMode.getWifiResult(bitmap);
                Log.d("TAG", "run: result:" + result);
                if (result != null) {
                    String[] results = result.split(",");
                    if (results != null && results.length >= 2) {
                        String ssid = results[0];
                        String password = results[1];
                        Log.d("TAG", "run: sid:" + ssid + "     password:" + password);
                        if (ssid != null && password != null) {
                            CuiNiaoApp.mWifiUtils.OpenWifi();
                            String lastWifi = null;
                            if (CuiNiaoApp.mWifiUtils.isWifiConnect(CuiNiaoApp.mAppContext)) {
                                lastWifi = CuiNiaoApp.mWifiUtils.getSSID();
                            }
                            if (lastWifi != null && lastWifi.equals(ssid)) {//若当前连接wifi未目标wifi 直接返回成功
                                handler.sendEmptyMessage(What.WIFI_SUCCES);
                                isFinish = true;
                                isRun = false;
                                return;
                            } else {//否则需要重新连接wifi
                                CuiNiaoApp.mWifiUtils.connectNet(ssid, password, "WPA");
                                try {
                                    Thread.sleep(2500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                CuiNiaoApp.mWifiUtils.updateWifiInfo();
                                if (CuiNiaoApp.mWifiUtils.isWifiConnect(CuiNiaoApp.mAppContext)) {
                                    if (CuiNiaoApp.mWifiUtils.getSSID() != null && CuiNiaoApp.mWifiUtils.getSSID().equals(ssid)) {
                                        handler.sendEmptyMessage(What.WIFI_SUCCES);
                                        isFinish = true;
                                        isRun = false;
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
                handler.sendEmptyMessage(What.WIFI_FAULIRE);

            }
        }
    }


}
