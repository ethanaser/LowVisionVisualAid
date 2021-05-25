package com.cnsj.neptunglasses.bean.item.seconditem;

import android.app.Activity;
import android.content.SharedPreferences;
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
 * 右眼视野位置调整
 */
public class RightEyeCenterSetItem extends BaseItem {

    private int mRightEyeCenter[];

    public RightEyeCenterSetItem(Activity activity) {
        super(activity);
    }

    @Override
    public void init() {
        mRightEyeCenter = new int[2];
        menuEyent = fromJson();
        if (menuEyent == null) {
            menuEyent = new MenuEyent();
            menuEyent.setItemName("右眼视觉位置");
            menuEyent.setItemCount("0,0");
        }
        activity.setRightEyeCenter(mRightEyeCenter[0], mRightEyeCenter[1], true);
        activity.setRightEyeCenter(mRightEyeCenter[0], mRightEyeCenter[1], false);
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
        String[] ceterOffset = getMenuCount().trim().split(",");
        mRightEyeCenter[0] = Integer.parseInt(ceterOffset[0].trim());
        mRightEyeCenter[1] = Integer.parseInt(ceterOffset[1].trim());
        float rightXOffset = mRightEyeCenter[0] * 0.0078f * 3840 / 2;
        float rightOffset = -mRightEyeCenter[1] * 0.015f * 1992 / 2;
//        activity.setRightEyeCenter(rightXOffset, rightOffset);
    }

    @Override
    public void reset(int tag) {
        if (tag == Constant.ALL) {
            mRightEyeCenter = null;
            mRightEyeCenter = new int[2];
            activity.setRightEyeCenter(mRightEyeCenter[0], mRightEyeCenter[1], true);
            activity.setRightEyeCenter(mRightEyeCenter[0], mRightEyeCenter[1], false);
            menuEyent.setItemCount("0,0");
            toJson();
        }
    }

    @Override
    public boolean toJson() {
        String itemKey = "right_eye_center";
        String json = activity.getGson().toJson(menuEyent);
        SharedPreferences.Editor editor = CuiNiaoApp.sharedPreferences.edit();
        editor.putString(itemKey, json);
        editor.commit();
        return false;
    }

    @Override
    public MenuEyent fromJson() {
        String itemKey = "right_eye_center";
        String json = CuiNiaoApp.sharedPreferences.getString(itemKey, null);
        if (json != null) {
            MenuEyent menuEyent = activity.getGson().fromJson(json, new TypeToken<MenuEyent>() {
            }.getType());
            String[] ceterOffset = menuEyent.getItemCount().trim().split(",");
            mRightEyeCenter[0] = Integer.parseInt(ceterOffset[0].trim());
            mRightEyeCenter[1] = Integer.parseInt(ceterOffset[1].trim());
            return menuEyent;
        }
        return null;
    }

    @Override
    public void onKeyDown(int keyCode) {
        if (this.menuPopup == null) return;
        switch (keyCode) {
            case KeyEvent.KEYCODE_D:
                activity.setRightEyeCenter(mRightEyeCenter[0] - 1, mRightEyeCenter[1], true);
                mRightEyeCenter = activity.getRightEyeCenter();
                menuEyent.setItemCount(mRightEyeCenter[0] + "," + mRightEyeCenter[1]);
                CuiNiaoApp.textSpeechManager.speakNow("右眼视觉位置" + menuEyent.getItemCount());
                this.menuPopup.updateDisplay(getLogoImage(), getDisplayText(), getDisplayImages());
                toJson();
                break;
            case KeyEvent.KEYCODE_E:
                activity.setRightEyeCenter(mRightEyeCenter[0] + 1, mRightEyeCenter[1], true);
                mRightEyeCenter = activity.getRightEyeCenter();
                menuEyent.setItemCount(mRightEyeCenter[0] + "," + mRightEyeCenter[1]);
                CuiNiaoApp.textSpeechManager.speakNow("右眼视觉位置" + menuEyent.getItemCount());
                this.menuPopup.updateDisplay(getLogoImage(), getDisplayText(), getDisplayImages());
                toJson();
                break;
            case KeyEvent.KEYCODE_B:
                activity.setRightEyeCenter(mRightEyeCenter[0], mRightEyeCenter[1] + 1, false);
                mRightEyeCenter = activity.getRightEyeCenter();
                menuEyent.setItemCount(mRightEyeCenter[0] + "," + mRightEyeCenter[1]);
                CuiNiaoApp.textSpeechManager.speakNow("右眼视觉位置" + menuEyent.getItemCount());
                this.menuPopup.updateDisplay(getLogoImage(), getDisplayText(), getDisplayImages());
                toJson();
                break;
            case KeyEvent.KEYCODE_C:
                activity.setRightEyeCenter(mRightEyeCenter[0], mRightEyeCenter[1] - 1, false);
                mRightEyeCenter = activity.getRightEyeCenter();
                menuEyent.setItemCount(mRightEyeCenter[0] + "," + mRightEyeCenter[1]);
                CuiNiaoApp.textSpeechManager.speakNow("右眼视觉位置" + menuEyent.getItemCount());
                this.menuPopup.updateDisplay(getLogoImage(), getDisplayText(), getDisplayImages());
                toJson();
                break;
        }

    }

    @Override
    public void speak() {
        CuiNiaoApp.textSpeechManager.speakNow("右眼视觉位置");
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
