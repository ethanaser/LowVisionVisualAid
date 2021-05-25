package com.cnsj.neptunglasses.bean.item.seconditem;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.KeyEvent;


import com.google.gson.reflect.TypeToken;
import com.jiangdg.usbcamera.UVCCameraHelper;

import java.util.List;

import com.cnsj.neptunglasses.R;
import com.cnsj.neptunglasses.activity.ThinGlassesActivity;
import com.cnsj.neptunglasses.app.Constant;
import com.cnsj.neptunglasses.app.CuiNiaoApp;
import com.cnsj.neptunglasses.bean.BaseItem;
import com.cnsj.neptunglasses.bean.MenuEyent;

/**
 * 饱和度设定
 */
public class SaturationSetItem extends BaseItem {

    private int[] images = {R.mipmap.stride_one, R.mipmap.stride_two, R.mipmap.stride_three,
            R.mipmap.stride_four, R.mipmap.stride_five};
    private int[] images_y = {R.mipmap.stride_one_y, R.mipmap.stride_two_y, R.mipmap.stride_three_y,
            R.mipmap.stride_four_y, R.mipmap.stride_five_y};
    private int mSaturation;

    public SaturationSetItem(Activity activity) {
        super(activity);
    }

    @Override
    public void init() {
        mSaturation = 1;
        menuEyent = fromJson();
        if (menuEyent == null) {
            menuEyent = new MenuEyent();
            menuEyent.setItemName("饱和度");
            menuEyent.setItemCount("" + mSaturation);
        }
        setSaturation(mSaturation);
    }

    @Override
    public int getCurrentMenuLevel() {
        return 2;
    }


    @Override
    public int getLogoImage() {
        return CuiNiaoApp.isYellowMode ? R.mipmap.saturation_y : R.mipmap.saturation;
    }

    @Override
    public int getDisplayImages() {
        return CuiNiaoApp.isYellowMode ? images_y[mSaturation - 1] : images[mSaturation - 1];
    }

    @Override
    public void load() {
    }

    @Override
    public void reset(int tag) {
        if (tag == Constant.ALL) {
            mSaturation = 1;
            menuEyent.setItemCount(Integer.toString(mSaturation));
            setSaturation(mSaturation);
            toJson();
        }
    }

    @Override
    public boolean toJson() {
        String itemKey = "saturation";
        String json = activity.getGson().toJson(menuEyent);
        SharedPreferences.Editor editor = CuiNiaoApp.sharedPreferences.edit();
        editor.putString(itemKey, json);
        editor.commit();
        return false;
    }

    @Override
    public MenuEyent fromJson() {
        String itemKey = "saturation";
        String json = CuiNiaoApp.sharedPreferences.getString(itemKey, null);
        if (json != null) {
            MenuEyent menuEyent = activity.getGson().fromJson(json, new TypeToken<MenuEyent>() {
            }.getType());
            mSaturation = Integer.parseInt(menuEyent.getItemCount());
            return menuEyent;
        }
        return null;
    }

    private void setSaturation(int mSaturation) {
        Log.d("TAG", "setValue: 设定饱和度" + mSaturation);
        activity.setSaturation(mSaturation);
        this.mSaturation = activity.getSaturation();
    }

    @Override
    public void onKeyDown(int keyCode) {
        if (this.menuPopup == null) return;
        switch (keyCode) {
            case KeyEvent.KEYCODE_D:
                this.mSaturation = activity.getSaturation();
                setSaturation(--mSaturation);
                menuEyent.setItemCount(Integer.toString(mSaturation));
                CuiNiaoApp.textSpeechManager.speakNow("饱和度" + mSaturation);
                this.menuPopup.updateDisplay(getLogoImage(), getDisplayText(), getDisplayImages());
                toJson();
                break;
            case KeyEvent.KEYCODE_E:
                this.mSaturation = activity.getSaturation();
                setSaturation(++mSaturation);
                menuEyent.setItemCount(Integer.toString(mSaturation));
                CuiNiaoApp.textSpeechManager.speakNow("饱和度" + mSaturation);
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
        toJson();
        this.menuPopup = null;
    }

    @Override
    public List<BaseItem> getNextMenu() {
        return null;
    }
}
