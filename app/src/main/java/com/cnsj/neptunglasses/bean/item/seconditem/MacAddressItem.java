package com.cnsj.neptunglasses.bean.item.seconditem;


import android.app.Activity;

import java.util.List;

import com.cnsj.neptunglasses.R;
import com.cnsj.neptunglasses.activity.ThinGlassesActivity;
import com.cnsj.neptunglasses.app.CuiNiaoApp;
import com.cnsj.neptunglasses.bean.BaseItem;
import com.cnsj.neptunglasses.bean.MenuEyent;

public class MacAddressItem extends BaseItem {

    public MacAddressItem(Activity activity) {
        super(activity);
    }

    @Override
    public int getLogoImage() {
        return CuiNiaoApp.isYellowMode ? R.mipmap.mac_y : R.mipmap.mac;
    }

    @Override
    public void init() {
        menuEyent = new MenuEyent();
        menuEyent.setItemName("机器码");
        menuEyent.setItemCount(getFormatMac());
    }

    @Override
    public String getDisplayText() {
        return getMenuCount();
    }

    private String getFormatMac() {
        String mac = CuiNiaoApp.mWifiUtils.getNewMac();
        if (mac == null) return "";
        return mac.replace(":", "");
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

    @Override
    public void onKeyDown(int keyCode) {

    }

    @Override
    public void speak() {
        CuiNiaoApp.textSpeechManager.speakNow("机器码");
    }

    @Override
    public void update() {

    }

    @Override
    public void finish() {

    }

    @Override
    public List<BaseItem> getNextMenu() {
        return null;
    }

    @Override
    public int getCurrentMenuLevel() {
        return 2;
    }
}
