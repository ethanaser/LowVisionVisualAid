package com.cnsj.neptunglasses.bean.item.seconditem;


import android.app.Activity;
import android.content.SharedPreferences;
import android.view.KeyEvent;

import com.google.gson.reflect.TypeToken;
import com.jiangdg.usbcamera.utils.MathUtils;

import java.text.DecimalFormat;
import java.util.List;

import com.cnsj.neptunglasses.R;
import com.cnsj.neptunglasses.activity.ThinGlassesActivity;
import com.cnsj.neptunglasses.app.Constant;
import com.cnsj.neptunglasses.app.CuiNiaoApp;
import com.cnsj.neptunglasses.bean.BaseItem;
import com.cnsj.neptunglasses.bean.MenuEyent;

/**
 * 管状视野  双眼缩放
 */
public class TubularVisionItem extends BaseItem {

    private float doubleScale;

    public TubularVisionItem(Activity activity) {
        super(activity);
    }

    @Override
    public int getLogoImage() {
        return CuiNiaoApp.isYellowMode ? R.mipmap.tubular_y : R.mipmap.tubular;
    }

    @Override
    public String getDisplayText() {
        return getMenuCount();
    }

    @Override
    public void init() {
        doubleScale = 1.0f;
        menuEyent = fromJson();
        if (menuEyent == null) {
            menuEyent = new MenuEyent();
            menuEyent.setItemName("管状视野");
            menuEyent.setItemCount("1.00X");
        }
    }

    @Override
    public void load() {
        doubleScale = Float.parseFloat(getMenuCount().substring(0, getMenuCount().length() - 1).trim());
//        activity.setRightScale(mRightSubScale);
    }

    @Override
    public void reset(int tag) {
        if (tag == Constant.ALL) {
            doubleScale = 1.0f;
            activity.setDoubleEyeScale(doubleScale);
            menuEyent.setItemCount("1.00X");
            toJson();
        }
    }

    @Override
    public boolean toJson() {
        String itemKey = "double_scale";
        String json = activity.getGson().toJson(menuEyent);
        SharedPreferences.Editor editor = CuiNiaoApp.sharedPreferences.edit();
        editor.putString(itemKey, json);
        editor.commit();
        return false;
    }

    DecimalFormat decimalFormat = new DecimalFormat("0.00");

    @Override
    public MenuEyent fromJson() {
        String itemKey = "double_scale";
        String json = CuiNiaoApp.sharedPreferences.getString(itemKey, null);
        if (json != null) {
            MenuEyent menuEyent = activity.getGson().fromJson(json, new TypeToken<MenuEyent>() {
            }.getType());
            doubleScale = Float.parseFloat(menuEyent.getItemCount().substring(0, menuEyent.getItemCount().length() - 1).trim());
            return menuEyent;
        }
        return null;
    }

    @Override
    public void onKeyDown(int keyCode) {
        if (this.menuPopup == null) return;
        switch (keyCode) {
            case KeyEvent.KEYCODE_D://<- left direction
                activity.setDoubleEyeScale(MathUtils.floatSub(doubleScale, 0.01f));
                doubleScale = activity.getDoubleEyeScale();
                String p = decimalFormat.format(doubleScale);//format 返回的是字符串
                CuiNiaoApp.textSpeechManager.speakNow("管状视野" + p);
                menuEyent.setItemCount(p + "X");
                this.menuPopup.updateDisplay(getLogoImage(), getDisplayText(), getDisplayImages());
                toJson();
                break;
            case KeyEvent.KEYCODE_E://<+ right direction
                activity.setDoubleEyeScale(MathUtils.floatAdd(doubleScale, 0.01f));
                doubleScale = activity.getDoubleEyeScale();
                p = decimalFormat.format(doubleScale);//format 返回的是字符串
                menuEyent.setItemCount(p + "X");
                CuiNiaoApp.textSpeechManager.speakNow("管状视野" + p);
                this.menuPopup.updateDisplay(getLogoImage(), getDisplayText(), getDisplayImages());
                toJson();
                break;
        }
    }

    @Override
    public void speak() {
        CuiNiaoApp.textSpeechManager.speakNow("管状视野");
    }

    @Override
    public void update() {

    }

    @Override
    public void finish() {
        this.menuPopup = null;
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
