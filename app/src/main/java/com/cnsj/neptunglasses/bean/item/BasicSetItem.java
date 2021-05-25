package com.cnsj.neptunglasses.bean.item;


import android.app.Activity;

import com.cnsj.neptunglasses.R;

import java.util.Arrays;
import java.util.List;

import com.cnsj.neptunglasses.activity.ThinGlassesActivity;
import com.cnsj.neptunglasses.app.CuiNiaoApp;
import com.cnsj.neptunglasses.bean.BaseItem;
import com.cnsj.neptunglasses.bean.MenuEyent;
import com.cnsj.neptunglasses.bean.item.seconditem.BatteryItem;
import com.cnsj.neptunglasses.bean.item.seconditem.EnergySaveItem;
import com.cnsj.neptunglasses.bean.item.seconditem.MacAddressItem;
import com.cnsj.neptunglasses.bean.item.seconditem.ThemeModeItem;
import com.cnsj.neptunglasses.bean.item.seconditem.WIFIConnectItem;
import com.cnsj.neptunglasses.view.CustomMenuPopupThree;

/**
 * 基础设置
 */
public class BasicSetItem extends BaseItem {
    private List<BaseItem> nextItems;

    public BasicSetItem(Activity activity) {
        super(activity);
    }

    @Override
    public void init() {
        menuEyent = new MenuEyent();
        menuEyent.setItemName("基础设置");
        menuEyent.setItemCount(">");
        nextItems = Arrays.asList(new WIFIConnectItem(activity), //new EnergySaveItem(activity),
                new ThemeModeItem(activity), new MacAddressItem(activity));//new BatteryItem(activity),
        //, new ISOSwitchItem(activity)
    }

    @Override
    public int getCurrentMenuLevel() {
        return 1;
    }

    @Override
    public int getLogoImage() {
        return 0;
    }

    @Override
    public int getBigLogo() {
        return CuiNiaoApp.isYellowMode ? R.mipmap.basic_set_b_y : R.mipmap.basic_set_b;
    }

    @Override
    public int getSmallLogo() {
        return CuiNiaoApp.isYellowMode ? R.mipmap.basic_set_s_y : R.mipmap.basic_set_s;
    }

    @Override
    public int getDisplayImages() {
        return super.getDisplayImages();
    }

    @Override
    public void load() {
        for (BaseItem baseItem : nextItems) {
            baseItem.load();
        }
    }

    @Override
    public void reset(int tag) {
        for (BaseItem baseItem : nextItems) {
            baseItem.reset(tag);
        }
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
        CuiNiaoApp.textSpeechManager.speakNow("基础设置");
    }

    @Override
    public void update() {
        for (BaseItem baseItem : nextItems) {
            baseItem.update();
        }
    }

    @Override
    public void finish() {

    }

    @Override
    public List<BaseItem> getNextMenu() {
        return nextItems;
    }

    @Override
    public boolean intentToMenu2(CustomMenuPopupThree menuPopup) {
        return true;
    }
}
