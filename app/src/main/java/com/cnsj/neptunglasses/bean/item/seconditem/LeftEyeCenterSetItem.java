package com.cnsj.neptunglasses.bean.item.seconditem;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.KeyEvent;


import com.google.gson.reflect.TypeToken;

import java.util.List;

import com.cnsj.neptunglasses.R;
import com.cnsj.neptunglasses.activity.ThinGlassesActivity;
import com.cnsj.neptunglasses.app.Constant;
import com.cnsj.neptunglasses.app.CuiNiaoApp;
import com.cnsj.neptunglasses.bean.BaseItem;
import com.cnsj.neptunglasses.bean.MenuEyent;

/**
 * 左侧视野位置调整
 */
public class LeftEyeCenterSetItem extends BaseItem {

    private int mLeftEyeCenter[];

    public LeftEyeCenterSetItem(Activity activity) {
        super(activity);
    }

    @Override
    public void init() {
        mLeftEyeCenter = new int[2];
        menuEyent = fromJson();
        if (menuEyent == null) {
            menuEyent = new MenuEyent();
            menuEyent.setItemName("左眼视觉位置");
            menuEyent.setItemCount("0,0");
        }
        activity.setLeftEyeCenter(mLeftEyeCenter[0], mLeftEyeCenter[1], true);
        activity.setLeftEyeCenter(mLeftEyeCenter[0], mLeftEyeCenter[1], false);
    }

    @Override
    public int getCurrentMenuLevel() {
        return 3;
    }

    @Override
    public int getLogoImage() {
        return CuiNiaoApp.isYellowMode ? R.mipmap.eye_position_y : R.mipmap.eye_position;
    }

    @Override
    public String getDisplayText() {
        return getMenuCount();
    }

    @Override
    public void load() {
        String[] ceterOffset = menuEyent.getItemCount().trim().split(",");
        Log.d("popup", menuEyent.getItemCount());
        mLeftEyeCenter[0] = Integer.parseInt(ceterOffset[0].trim());
        mLeftEyeCenter[1] = Integer.parseInt(ceterOffset[1].trim());
        float leftXOffset = mLeftEyeCenter[0] * 0.0078f * 3840 / 2;
        float leftYOffset = -mLeftEyeCenter[1] * 0.015f * 1992 / 2;
        //activity.setLeftEyeCenter(leftXOffset, leftYOffset);
    }

    @Override
    public void reset(int tag) {
        if (tag == Constant.ALL) {
            mLeftEyeCenter = null;
            mLeftEyeCenter = new int[2];
            menuEyent.setItemCount("0,0");
            activity.setLeftEyeCenter(mLeftEyeCenter[0], mLeftEyeCenter[1], true);
            activity.setLeftEyeCenter(mLeftEyeCenter[0], mLeftEyeCenter[1], false);
            toJson();
        }
    }

    @Override
    public boolean toJson() {
        String itemKey = "left_eye_center";
        String json = activity.getGson().toJson(menuEyent);
        SharedPreferences.Editor editor = CuiNiaoApp.sharedPreferences.edit();
        editor.putString(itemKey, json);
        editor.commit();
        return false;
    }

    @Override
    public MenuEyent fromJson() {
        String itemKey = "left_eye_center";
        String json = CuiNiaoApp.sharedPreferences.getString(itemKey, null);
        if (json != null) {
            MenuEyent menuEyent = activity.getGson().fromJson(json, new TypeToken<MenuEyent>() {
            }.getType());
//            activity.getmMyGLSurfaceView().setIPD(Integer.parseInt(menuEyent.getItemCount().trim()));
            String[] ceterOffset = menuEyent.getItemCount().trim().split(",");
            mLeftEyeCenter[0] = Integer.parseInt(ceterOffset[0].trim());
            mLeftEyeCenter[1] = Integer.parseInt(ceterOffset[1].trim());
            return menuEyent;
        }
        return null;
    }

    @Override
    public void onKeyDown(int keyCode) {
        if (this.menuPopup == null) return;
        switch (keyCode) {
            case KeyEvent.KEYCODE_D:
                activity.setLeftEyeCenter(mLeftEyeCenter[0] - 1, mLeftEyeCenter[1], true);
                mLeftEyeCenter = activity.getLeftEyeCenter();
                menuEyent.setItemCount(mLeftEyeCenter[0] + "," + mLeftEyeCenter[1]);
                CuiNiaoApp.textSpeechManager.speakNow("左眼视觉位置" + menuEyent.getItemCount());
                this.menuPopup.updateDisplay(getLogoImage(), getDisplayText(), getDisplayImages());
                toJson();
                break;
            case KeyEvent.KEYCODE_E:
                activity.setLeftEyeCenter(mLeftEyeCenter[0] + 1, mLeftEyeCenter[1], true);
                mLeftEyeCenter = activity.getLeftEyeCenter();
                menuEyent.setItemCount(mLeftEyeCenter[0] + "," + mLeftEyeCenter[1]);
                CuiNiaoApp.textSpeechManager.speakNow("左眼视觉位置" + menuEyent.getItemCount());
                this.menuPopup.updateDisplay(getLogoImage(), getDisplayText(), getDisplayImages());
                toJson();
                break;
            case KeyEvent.KEYCODE_B:
                activity.setLeftEyeCenter(mLeftEyeCenter[0], mLeftEyeCenter[1] + 1, true);
                mLeftEyeCenter = activity.getLeftEyeCenter();
                menuEyent.setItemCount(mLeftEyeCenter[0] + "," + mLeftEyeCenter[1]);
                CuiNiaoApp.textSpeechManager.speakNow("左眼视觉位置" + menuEyent.getItemCount());
                this.menuPopup.updateDisplay(getLogoImage(), getDisplayText(), getDisplayImages());
                toJson();
                break;
            case KeyEvent.KEYCODE_C:
                activity.setLeftEyeCenter(mLeftEyeCenter[0], mLeftEyeCenter[1] - 1, true);
                mLeftEyeCenter = activity.getLeftEyeCenter();
                menuEyent.setItemCount(mLeftEyeCenter[0] + "," + mLeftEyeCenter[1]);
                CuiNiaoApp.textSpeechManager.speakNow("左眼视觉位置" + menuEyent.getItemCount());
                this.menuPopup.updateDisplay(getLogoImage(), getDisplayText(), getDisplayImages());
                toJson();
                break;
        }
    }

    @Override
    public void speak() {
        CuiNiaoApp.textSpeechManager.speakNow("左眼视野");
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
