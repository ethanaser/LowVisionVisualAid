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
 * 对比度设定
 */
public class ContrastSetItem extends BaseItem {

    private int mContrast;
    private int[] images = {R.mipmap.stride_one, R.mipmap.stride_two, R.mipmap.stride_three,
            R.mipmap.stride_four, R.mipmap.stride_five};
    private int[] images_y = {R.mipmap.stride_one_y, R.mipmap.stride_two_y, R.mipmap.stride_three_y,
            R.mipmap.stride_four_y, R.mipmap.stride_five_y};

    public ContrastSetItem(Activity activity) {
        super(activity);
    }

    @Override
    public void init() {
        mContrast = 1;
        menuEyent = fromJson();
        if (menuEyent == null) {
            menuEyent = new MenuEyent();
            menuEyent.setItemName("对比度");
            menuEyent.setItemCount("" + mContrast);
        }
        setContrast(mContrast);
    }

    @Override
    public int getCurrentMenuLevel() {
        return 2;
    }

    @Override
    public int getLogoImage() {
        return CuiNiaoApp.isYellowMode ? R.mipmap.contrast_y : R.mipmap.contrast;
    }

    @Override
    public int getDisplayImages() {
        return CuiNiaoApp.isYellowMode ? images_y[mContrast - 1] : images[mContrast - 1];
    }

    @Override
    public void load() {

    }

    @Override
    public void reset(int tag) {
        if (tag == Constant.ALL) {
            mContrast = 1;
            menuEyent.setItemCount(Integer.toString(mContrast));
            setContrast(mContrast);
            toJson();
        }
    }

    @Override
    public boolean toJson() {
        String itemKey = "contrast";
        String json = activity.getGson().toJson(menuEyent);
        SharedPreferences.Editor editor = CuiNiaoApp.sharedPreferences.edit();
        editor.putString(itemKey, json);
        editor.commit();
        return false;
    }

    @Override
    public MenuEyent fromJson() {
        String itemKey = "contrast";
        String json = CuiNiaoApp.sharedPreferences.getString(itemKey, null);
        if (json != null) {
            MenuEyent menuEyent = activity.getGson().fromJson(json, new TypeToken<MenuEyent>() {
            }.getType());
            mContrast = Integer.parseInt(menuEyent.getItemCount());
            return menuEyent;
        }
        return null;
    }

    /**
     * 设定对比度
     *
     * @param mContrast
     */

    private void setContrast(int mContrast) {
        Log.d("TAG", "setValue mContrast: 设定对比度" + mContrast);
        activity.setContrast(mContrast);
        this.mContrast = activity.getContrast();
    }

    @Override
    public void onKeyDown(int keyCode) {
        if (this.menuPopup == null) return;
        switch (keyCode) {
            case KeyEvent.KEYCODE_D:
                this.mContrast = activity.getContrast();
                mContrast--;
                setContrast(mContrast);
                menuEyent.setItemCount(Integer.toString(mContrast));
                CuiNiaoApp.textSpeechManager.speakNow("对比度" + mContrast);
                this.menuPopup.updateDisplay(getLogoImage(), getDisplayText(), getDisplayImages());
                toJson();
                break;
            case KeyEvent.KEYCODE_E:
                this.mContrast = activity.getContrast();
                mContrast++;
                setContrast(mContrast);
                menuEyent.setItemCount(Integer.toString(mContrast));
                CuiNiaoApp.textSpeechManager.speakNow("对比度" + mContrast);
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

}
