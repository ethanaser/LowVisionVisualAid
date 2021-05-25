package com.cnsj.neptunglasses.bean.item;


import android.app.Activity;

import com.cnsj.neptunglasses.R;

import java.util.Arrays;
import java.util.List;

import com.cnsj.neptunglasses.activity.ThinGlassesActivity;
import com.cnsj.neptunglasses.app.CuiNiaoApp;
import com.cnsj.neptunglasses.bean.BaseItem;
import com.cnsj.neptunglasses.bean.MenuEyent;
import com.cnsj.neptunglasses.bean.item.seconditem.AntiShakeSetItem;
import com.cnsj.neptunglasses.bean.item.seconditem.ColorsModeSetItem;
import com.cnsj.neptunglasses.bean.item.seconditem.ContrastSetItem;
import com.cnsj.neptunglasses.bean.item.seconditem.EdgeModeSetItem;
import com.cnsj.neptunglasses.bean.item.seconditem.FontSizeSetItem;
import com.cnsj.neptunglasses.bean.item.seconditem.BrightnessSetItem;
import com.cnsj.neptunglasses.bean.item.seconditem.SaturationSetItem;
import com.cnsj.neptunglasses.view.CustomMenuPopupThree;

/**
 * 图像设置
 */
public class DisplaySetItem extends BaseItem {

    private List<BaseItem> nextItems;

    public DisplaySetItem(Activity activity) {
        super(activity);
    }

    @Override
    public void init() {
        menuEyent = new MenuEyent();
        menuEyent.setItemName("图像设置");
        menuEyent.setItemCount(">");
        //new BrightnessSetItem(activity),
        nextItems = Arrays.asList( new SaturationSetItem(activity), new ContrastSetItem(activity)
                , new ColorsModeSetItem(activity), new EdgeModeSetItem(activity),
                new AntiShakeSetItem(activity),
                new FontSizeSetItem(activity));
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
        return CuiNiaoApp.isYellowMode ? R.mipmap.display_set_b_y : R.mipmap.display_set_b;
    }

    @Override
    public int getSmallLogo() {
        return CuiNiaoApp.isYellowMode ? R.mipmap.display_set_s_y : R.mipmap.display_set_s;
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
        CuiNiaoApp.textSpeechManager.speakNow("图像设置");
    }

    @Override
    public void update() {

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
