package com.cnsj.neptunglasses.bean.item.seconditem;


import android.app.Activity;
import android.os.BatteryManager;
import android.util.Log;

import java.util.List;

import com.cnsj.neptunglasses.activity.ThinGlassesActivity;
import com.cnsj.neptunglasses.app.CuiNiaoApp;
import com.cnsj.neptunglasses.bean.BaseItem;
import com.cnsj.neptunglasses.bean.MenuEyent;
import com.cnsj.neptunglasses.utils.SightaidUtil;
import com.cnsj.neptunglasses.view.CustomMenuPopupThree;

/**
 * 电池电量显示
 */
public class BatteryItem extends BaseItem {

    private int mBatteryValue;

    public BatteryItem(Activity activity) {
        super(activity);
    }

    @Override
    public void init() {
        mBatteryValue = activity.getBatteryValue();
        menuEyent = new MenuEyent();
        menuEyent.setItemName("电量");
        menuEyent.setItemCount(String.valueOf(mBatteryValue) + "%");
    }

    @Override
    public int getCurrentMenuLevel() {
        return 2;
    }

    @Override
    public int getLogoImage() {
        return 0;
    }

    @Override
    public int getDisplayImages() {
        return super.getDisplayImages();
    }

    @Override
    public void load() {
        menuEyent.setItemCount(String.valueOf(activity.getBatteryValue()) + "%");
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
        String value = getMenuCount().substring(0, getMenuCount().length() - 1);
        value = SightaidUtil.numberToChinese(Integer.parseInt(value));
        Log.d("TAG", "speak: " + value);
        CuiNiaoApp.textSpeechManager.speakNow("当前电量为百分之" + value);
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
    public boolean intentToMenu2(CustomMenuPopupThree menuPopup) {
        return false;
    }
}
