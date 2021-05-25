package com.cnsj.neptunglasses.bean.item;


import android.app.Activity;

import java.util.Arrays;
import java.util.List;

import com.cnsj.neptunglasses.R;
import com.cnsj.neptunglasses.activity.ThinGlassesActivity;
import com.cnsj.neptunglasses.app.CuiNiaoApp;
import com.cnsj.neptunglasses.bean.BaseItem;
import com.cnsj.neptunglasses.bean.MenuEyent;
import com.cnsj.neptunglasses.bean.item.seconditem.EyeCenterSetItem;
import com.cnsj.neptunglasses.bean.item.seconditem.EyeScaleSetItem;
import com.cnsj.neptunglasses.bean.item.seconditem.EyeSpaceSetItem;
import com.cnsj.neptunglasses.bean.item.seconditem.ResetEyeSetItem;
import com.cnsj.neptunglasses.bean.item.seconditem.TubularVisionItem;
import com.cnsj.neptunglasses.view.CustomMenuPopupThree;

/**
 * 眼部情况菜单项
 */
public class EyeSetItem extends BaseItem {

    private List<BaseItem> nextItems;

    public EyeSetItem(Activity activity) {
        super(activity);
    }

    @Override
    public void init() {
//        sharedKey="eye_set";
        menuEyent = new MenuEyent();
        menuEyent.setItemName("眼部情况");
        menuEyent.setItemCount(">");
        nextItems = Arrays.asList(new EyeSpaceSetItem(activity), new EyeCenterSetItem(activity),
                new EyeScaleSetItem(activity), new TubularVisionItem(activity));
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
        return CuiNiaoApp.isYellowMode ? R.mipmap.eye_set_b_y : R.mipmap.eye_set_b;
    }

    @Override
    public int getSmallLogo() {
        return CuiNiaoApp.isYellowMode ? R.mipmap.eye_set_s_y : R.mipmap.eye_set_s;
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
        CuiNiaoApp.textSpeechManager.speakNow("眼部情况");
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
