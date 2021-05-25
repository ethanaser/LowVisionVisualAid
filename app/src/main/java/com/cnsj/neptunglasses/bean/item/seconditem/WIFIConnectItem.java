package com.cnsj.neptunglasses.bean.item.seconditem;


import android.app.Activity;
import android.util.Log;
import android.view.KeyEvent;

import java.util.List;

import com.cnsj.neptunglasses.R;
import com.cnsj.neptunglasses.activity.ThinGlassesActivity;
import com.cnsj.neptunglasses.app.CuiNiaoApp;
import com.cnsj.neptunglasses.bean.BaseItem;
import com.cnsj.neptunglasses.bean.MenuEyent;

/**
 * 连接wifi
 */
public class WIFIConnectItem extends BaseItem {

    public WIFIConnectItem(Activity activity) {
        super(activity);
    }

    @Override
    public void init() {
        menuEyent = new MenuEyent();
        menuEyent.setItemName("WIFI连接");
        Log.d("ThinGlassesActivity", "init: " + CuiNiaoApp.mWifiUtils.isWifiConnect(activity));
        Log.d("ThinGlassesActivity", "init: " + CuiNiaoApp.mWifiUtils.getSSID());
        if (CuiNiaoApp.mWifiUtils.isWifiConnect(activity)) {
            if (CuiNiaoApp.mWifiUtils.getSSID() != null)
                menuEyent.setItemCount(CuiNiaoApp.mWifiUtils.getSSID());
            else
                menuEyent.setItemCount("已连接");
        } else {
            menuEyent.setItemCount("未连接");
        }
    }

    @Override
    public int getCurrentMenuLevel() {
        return 2;
    }

    @Override
    public int getLogoImage() {
        return CuiNiaoApp.isYellowMode ? R.mipmap.scan_y : R.mipmap.scan;
    }

    @Override
    public String getDisplayText() {
        return "扫码中...";
    }

    @Override
    public void load() {
        CuiNiaoApp.mWifiUtils.updateWifiInfo();
        if (CuiNiaoApp.mWifiUtils.isWifiConnect(activity)) {
            if (CuiNiaoApp.mWifiUtils.getSSID() != null)
                menuEyent.setItemCount(CuiNiaoApp.mWifiUtils.getSSID());
            else
                menuEyent.setItemCount("已连接");
        } else {
            menuEyent.setItemCount("未连接");
        }
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

    @Override
    public void onKeyDown(int keyCode) {
        switch (keyCode) {
            case -1:
                CuiNiaoApp.textSpeechManager.speakNow("Wifi扫码中");
                this.menuPopup.updateDisplay(getLogoImage(), getDisplayText(), getDisplayImages());
                activity.qrScanStart();
                break;
            case KeyEvent.KEYCODE_H:
            case KeyEvent.KEYCODE_I:
            case KeyEvent.KEYCODE_BACK:
                break;
        }
    }

    @Override
    public void speak() {
        CuiNiaoApp.textSpeechManager.speakNow("Wifi连接" + menuEyent.getItemCount());
    }

    @Override
    public void update() {
        if (activity.isWifiConnected()) {//wifi 连接成功
            Log.d("Wifi", "Wifiupdate: getSSID " + CuiNiaoApp.mWifiUtils.getSSID());
            menuEyent.setItemCount(activity.getFailReason());
            CuiNiaoApp.textSpeechManager.speakNow("Wifi连接成功");
//            activity.initNetWorkStatus();
        } else {//wifi连接失败
            menuEyent.setItemCount("未连接");
        }
        this.menuPopup = null;
    }

    @Override
    public void finish() {
        this.menuPopup = null;
        activity.cancelQR();
    }

    @Override
    public List<BaseItem> getNextMenu() {
        return null;
    }
}
