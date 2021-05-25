package com.cnsj.neptunglasses.bean.item.seconditem;


import android.app.Activity;
import android.content.SharedPreferences;
import android.view.KeyEvent;

import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import com.cnsj.neptunglasses.R;
import com.cnsj.neptunglasses.activity.ThinGlassesActivity;
import com.cnsj.neptunglasses.app.Constant;
import com.cnsj.neptunglasses.app.CuiNiaoApp;
import com.cnsj.neptunglasses.bean.BaseItem;
import com.cnsj.neptunglasses.bean.MenuEyent;

/**
 * 音量设定
 */
public class VolumeSetItem extends BaseItem {

    private int[] images = {R.mipmap.stride_zero, R.mipmap.stride_one, R.mipmap.stride_two, R.mipmap.stride_three,
            R.mipmap.stride_four, R.mipmap.stride_five};
    private int[] images_y = {R.mipmap.stride_zero_y, R.mipmap.stride_one_y, R.mipmap.stride_two_y, R.mipmap.stride_three_y,
            R.mipmap.stride_four_y, R.mipmap.stride_five_y};
    private int voice;

    public VolumeSetItem(Activity activity) {
        super(activity);
    }

    @Override
    public void init() {
        voice = 3;
        menuEyent = fromJson();
        if (menuEyent == null) {
            menuEyent = new MenuEyent();
            menuEyent.setItemName("音量调节");
            menuEyent.setItemCount("" + voice);
        }
        setNowVolume(voice);
    }

    @Override
    public int getCurrentMenuLevel() {
        return 2;
    }

    @Override
    public int getLogoImage() {
        if (Integer.parseInt(getNowVolume()) <= 0) {
            return CuiNiaoApp.isYellowMode ? R.mipmap.volume_none_y : R.mipmap.volume_none;
        }
        return CuiNiaoApp.isYellowMode ? R.mipmap.volume_y : R.mipmap.volume;
    }

    @NotNull
    private String getNowVolume() {
        return CuiNiaoApp.mVolumeManager.getNowVolume();
    }

    private void setNowVolume(int offset) {
        CuiNiaoApp.mVolumeManager.setVolumeFromOffset(offset, 0);
    }

    @Override
    public int getDisplayImages() {
        return CuiNiaoApp.isYellowMode ? images_y[voice] : images[voice];
    }

    @Override
    public void load() {
        voice = Integer.parseInt(menuEyent.getItemCount());
        setNowVolume(voice);
        menuEyent.setItemCount("" + voice);
    }

    @Override
    public void reset(int tag) {
        if (tag == Constant.ALL) {
            voice = 3;
            setNowVolume(voice);
            menuEyent.setItemCount("" + 3);
        }
    }

    @Override
    public boolean toJson() {
        String itemKey = "volume_set";
        String json = activity.getGson().toJson(menuEyent);
        SharedPreferences.Editor editor = CuiNiaoApp.sharedPreferences.edit();
        editor.putString(itemKey, json);
        editor.commit();
        return false;
    }

    @Override
    public MenuEyent fromJson() {
        String itemKey = "volume_set";
        String json = CuiNiaoApp.sharedPreferences.getString(itemKey, null);
        if (json != null) {
            MenuEyent menuEyent = activity.getGson().fromJson(json, new TypeToken<MenuEyent>() {
            }.getType());
            voice = Integer.parseInt(menuEyent.getItemCount());
            return menuEyent;
        }
        return null;
    }

    @Override
    public void onKeyDown(int keyCode) {
        if (this.menuPopup == null) return;
        switch (keyCode) {
            case KeyEvent.KEYCODE_D://left
                voice--;
                if (voice < 0) {
                    voice = 0;
                }
                setNowVolume(voice);
                menuEyent.setItemCount(Integer.toString(voice));
                CuiNiaoApp.textSpeechManager.speakNow("音量" + voice);
                this.menuPopup.updateDisplay(getLogoImage(), getDisplayText(), getDisplayImages());
                if (voice <= 0) {
                    activity.notifyError();
                }
                toJson();
                break;
            case KeyEvent.KEYCODE_E://right
                voice++;
                if (voice > 5) {
                    voice = 5;
                }
                setNowVolume(voice);
                menuEyent.setItemCount(Integer.toString(voice));
                CuiNiaoApp.textSpeechManager.speakNow("音量" + voice);
                this.menuPopup.updateDisplay(getLogoImage(), getDisplayText(), getDisplayImages());
                toJson();
                break;
        }
    }

    @Override
    public void speak() {
        CuiNiaoApp.textSpeechManager.speakNow("音量调节");
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
