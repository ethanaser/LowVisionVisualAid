package com.cnsj.neptunglasses.bean.item.seconditem;


import android.app.Activity;

import java.util.Arrays;
import java.util.List;

import com.cnsj.neptunglasses.R;
import com.cnsj.neptunglasses.activity.ThinGlassesActivity;
import com.cnsj.neptunglasses.app.CuiNiaoApp;
import com.cnsj.neptunglasses.bean.BaseItem;
import com.cnsj.neptunglasses.bean.MenuEyent;
import com.cnsj.neptunglasses.view.CustomMenuPopupThree;

/**
 * 视野调节
 */
public class EyeCenterSetItem extends BaseItem {

    private List<BaseItem> nextItems;

    public EyeCenterSetItem(Activity activity) {
        super(activity);
    }

    @Override
    public void init() {
        menuEyent = new MenuEyent();
        menuEyent.setItemName("视位调节");
        menuEyent.setItemCount("");
        nextItems = Arrays.asList(new LeftEyeCenterSetItem(activity), new RightEyeCenterSetItem(activity));
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
        CuiNiaoApp.textSpeechManager.speakNow("视野调节");
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


    @Override
    public boolean isImage() {
        return true;
    }

    @Override
    public int getItemCountImage(boolean isSelected) {
        if (isSelected) {
            return CuiNiaoApp.isYellowMode ? R.mipmap.right_y : R.mipmap.right;
        }
        return 0;
    }
}
