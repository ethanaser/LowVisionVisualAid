package com.cnsj.neptunglasses.bean.item.seconditem;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.KeyEvent;


import com.cnsj.neptunglasses.app.Constant;
import com.google.gson.reflect.TypeToken;
import com.jiangdg.usbcamera.utils.MathUtils;

import java.text.DecimalFormat;
import java.util.List;

import com.cnsj.neptunglasses.R;
import com.cnsj.neptunglasses.activity.ThinGlassesActivity;
import com.cnsj.neptunglasses.app.CuiNiaoApp;
import com.cnsj.neptunglasses.bean.BaseItem;
import com.cnsj.neptunglasses.bean.MenuEyent;

/**
 * 左眼基础缩放
 */
public class LeftEyeScaleSetItem extends BaseItem {

    private float mLeftSubScale;

    public LeftEyeScaleSetItem(Activity activity) {
        super(activity);
    }

    @Override
    public void init() {
        mLeftSubScale = 1.0f;
        menuEyent = fromJson();
        if (menuEyent == null) {
            menuEyent = new MenuEyent();
            menuEyent.setItemName("左眼缩放");
            menuEyent.setItemCount("1.00X");
        }
    }

    @Override
    public int getCurrentMenuLevel() {
        return 3;
    }

    private boolean isAdd;

    @Override
    public int getLogoImage() {
        if (mLeftSubScale >= 1.0) {
            return CuiNiaoApp.isYellowMode ? R.mipmap.eye_scale_normal_y : R.mipmap.eye_scale_normal;
        } else if (isAdd) {
            return CuiNiaoApp.isYellowMode ? R.mipmap.eye_scale_enlarge_y : R.mipmap.eye_scale_enlarge;
        } else {
            return CuiNiaoApp.isYellowMode ? R.mipmap.eye_scale_shrink_y : R.mipmap.eye_scale_shrink;
        }
    }

    @Override
    public String getDisplayText() {
        return getMenuCount();
    }

    @Override
    public void load() {
        mLeftSubScale = Float.parseFloat(getMenuCount().substring(0, getMenuCount().length() - 1).trim());
        activity.setLeftScale(mLeftSubScale);
    }

    @Override
    public void reset(int tag) {
        if (tag == Constant.ALL) {
            mLeftSubScale = 1.0f;
            activity.setLeftEyeScale(mLeftSubScale);
            menuEyent.setItemCount("1.00X");
            toJson();
        }
    }

    @Override
    public boolean toJson() {
        String itemKey = "left_scale";
        String json = activity.getGson().toJson(menuEyent);
        SharedPreferences.Editor editor = CuiNiaoApp.sharedPreferences.edit();
        editor.putString(itemKey, json);
        editor.commit();
        return false;
    }

    @Override
    public MenuEyent fromJson() {
        String itemKey = "left_scale";
        String json = CuiNiaoApp.sharedPreferences.getString(itemKey, null);
        if (json != null) {
            MenuEyent menuEyent = activity.getGson().fromJson(json, new TypeToken<MenuEyent>() {
            }.getType());
            mLeftSubScale = Float.parseFloat(menuEyent.getItemCount().substring(0, menuEyent.getItemCount().length() - 1).trim());
            return menuEyent;
        }
        return null;
    }

    DecimalFormat decimalFormat = new DecimalFormat("0.00");

    @Override
    public void onKeyDown(int keyCode) {
        if (this.menuPopup == null) return;
        switch (keyCode) {
            case KeyEvent.KEYCODE_D:
                activity.setLeftEyeScale(MathUtils.floatSub(mLeftSubScale, 0.01f));
                mLeftSubScale = activity.getLeftEyeScale();
                String p = decimalFormat.format(mLeftSubScale);//format 返回的是字符串
                Log.d("popup", "onKeyEvent: " + p);
                menuEyent.setItemCount(p + "X");
                CuiNiaoApp.textSpeechManager.speakNow("左眼缩放" + p);
                isAdd = false;
                this.menuPopup.updateDisplay(getLogoImage(), getDisplayText(), getDisplayImages());
                toJson();
                break;
            case KeyEvent.KEYCODE_E://-> right
                activity.setLeftEyeScale(MathUtils.floatAdd(mLeftSubScale, 0.01f));
                mLeftSubScale = activity.getLeftEyeScale();
                p = decimalFormat.format(mLeftSubScale);//format 返回的是字符串
                Log.d("popup", "onKeyEvent: " + p);
                menuEyent.setItemCount(p + "X");
                CuiNiaoApp.textSpeechManager.speakNow("左眼缩放" + p);
                isAdd = true;
                this.menuPopup.updateDisplay(getLogoImage(), getDisplayText(), getDisplayImages());
                toJson();
                break;
        }
    }

    @Override
    public void speak() {
        CuiNiaoApp.textSpeechManager.speakNow("左眼缩放");
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
