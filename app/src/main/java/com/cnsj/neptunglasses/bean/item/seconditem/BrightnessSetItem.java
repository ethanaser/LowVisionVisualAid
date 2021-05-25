package com.cnsj.neptunglasses.bean.item.seconditem;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;


import com.google.gson.reflect.TypeToken;

import java.util.List;

import com.cnsj.neptunglasses.R;
import com.cnsj.neptunglasses.activity.ThinGlassesActivity;
import com.cnsj.neptunglasses.app.Constant;
import com.cnsj.neptunglasses.app.CuiNiaoApp;
import com.cnsj.neptunglasses.bean.BaseItem;
import com.cnsj.neptunglasses.bean.MenuEyent;
import com.jiangdg.usbcamera.UVCCameraHelper;

/**
 * 亮度设定
 */
public class BrightnessSetItem extends BaseItem {

    private int brighnessvalue;
    private int[] images = {R.mipmap.stride_one, R.mipmap.stride_two, R.mipmap.stride_three,
            R.mipmap.stride_four, R.mipmap.stride_five};
    private int[] images_y = {R.mipmap.stride_one_y, R.mipmap.stride_two_y, R.mipmap.stride_three_y,
            R.mipmap.stride_four_y, R.mipmap.stride_five_y};

    public BrightnessSetItem(Activity activity) {
        super(activity);
    }

    @Override
    public void init() {
        brighnessvalue = 1;
        menuEyent = fromJson();
        if (menuEyent == null) {
            menuEyent = new MenuEyent();
            menuEyent.setItemName("亮度");
            menuEyent.setItemCount("" + brighnessvalue);
        }
        setLight(brighnessvalue);
    }

    @Override
    public int getCurrentMenuLevel() {
        return 2;
    }


    @Override
    public int getLogoImage() {
        return CuiNiaoApp.isYellowMode ? R.mipmap.light_y : R.mipmap.light;
    }

    @Override
    public int getDisplayImages() {
        return CuiNiaoApp.isYellowMode ? images_y[brighnessvalue - 1] : images[brighnessvalue - 1];
    }

    @Override
    public void load() {
    }

    @Override
    public void reset(int tag) {
        if (tag == Constant.ALL) {
            brighnessvalue = 1;
            menuEyent.setItemCount(Integer.toString(brighnessvalue));
            setLight(brighnessvalue);
            toJson();
        }
    }

    @Override
    public boolean toJson() {
        String itemKey = "light";
        String json = activity.getGson().toJson(menuEyent);
        SharedPreferences.Editor editor = CuiNiaoApp.sharedPreferences.edit();
        editor.putString(itemKey, json);
        editor.commit();
        return false;
    }

    @Override
    public MenuEyent fromJson() {
        String itemKey = "light";
        String json = CuiNiaoApp.sharedPreferences.getString(itemKey, null);
        if (json != null) {
            MenuEyent menuEyent = activity.getGson().fromJson(json, new TypeToken<MenuEyent>() {
            }.getType());
            brighnessvalue = Integer.parseInt(menuEyent.getItemCount());
            return menuEyent;
        }
        return null;
    }

    @Override
    public void onKeyDown(int keyCode) {
        if (this.menuPopup == null) return;
        switch (keyCode) {
            case KeyEvent.KEYCODE_D:
//                brighnessvalue=activity.getBrightness();
                if (brighnessvalue <= 1) {
                    brighnessvalue = 2;
                }
                setLight(--brighnessvalue);
                menuEyent.setItemCount(Integer.toString(brighnessvalue));
                CuiNiaoApp.textSpeechManager.speakNow("亮度" + brighnessvalue);
                Log.d("TAG", "onKeyDown: 亮度：" + brighnessvalue);
                this.menuPopup.updateDisplay(getLogoImage(), getDisplayText(), getDisplayImages());
                toJson();
                break;
            case KeyEvent.KEYCODE_E:
//                brighnessvalue=activity.getBrightness();
                if (brighnessvalue >= 5) {
                    brighnessvalue = 4;
                }
                setLight(++brighnessvalue);
                menuEyent.setItemCount(Integer.toString(brighnessvalue));
                CuiNiaoApp.textSpeechManager.speakNow("亮度" + brighnessvalue);
                Log.d("TAG", "onKeyDown: 亮度：" + brighnessvalue);
                this.menuPopup.updateDisplay(getLogoImage(), getDisplayText(), getDisplayImages());
                toJson();
                break;
        }
    }

    @Override
    public void speak() {
        CuiNiaoApp.textSpeechManager.speakNow(menuEyent.getItemName() + menuEyent.getItemCount());
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


    private void setLight(int brightness) {
        activity.setBrightness(brightness);
//        brighnessvalue=activity.getBrightness();
    }
}
