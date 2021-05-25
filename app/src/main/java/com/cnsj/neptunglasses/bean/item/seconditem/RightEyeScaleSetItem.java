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
 * 右眼基础缩放
 */
public class RightEyeScaleSetItem extends BaseItem {

    private float mRightSubScale;

    public RightEyeScaleSetItem(Activity activity) {
        super(activity);
    }

    @Override
    public void init() {
        mRightSubScale = 1.0f;
        menuEyent = fromJson();
        if (menuEyent == null) {
            menuEyent = new MenuEyent();
            menuEyent.setItemName("右眼缩放");
            menuEyent.setItemCount("1.00X");
        }
//        activity.setRightEyeScale(mRightSubScale);
    }

    @Override
    public int getCurrentMenuLevel() {
        return 3;
    }

    private boolean isAdd;

    @Override
    public int getLogoImage() {
        if (mRightSubScale >= 1.0) {
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
        mRightSubScale = Float.parseFloat(getMenuCount().substring(0, getMenuCount().length() - 1).trim());
        activity.setRightScale(mRightSubScale);
    }

    @Override
    public void reset(int tag) {
        if (tag == Constant.ALL) {
            mRightSubScale = 1.0f;
            activity.setRightEyeScale(mRightSubScale);
            menuEyent.setItemCount("1.00X");
            toJson();
        }
    }

    @Override
    public boolean toJson() {
        String itemKey = "right_scale";
        String json = activity.getGson().toJson(menuEyent);
        SharedPreferences.Editor editor = CuiNiaoApp.sharedPreferences.edit();
        editor.putString(itemKey, json);
        editor.commit();
        return false;
    }

    @Override
    public MenuEyent fromJson() {
        String itemKey = "right_scale";
        String json = CuiNiaoApp.sharedPreferences.getString(itemKey, null);
        if (json != null) {
            MenuEyent menuEyent = activity.getGson().fromJson(json, new TypeToken<MenuEyent>() {
            }.getType());
            mRightSubScale = Float.parseFloat(menuEyent.getItemCount().substring(0, menuEyent.getItemCount().length() - 1).trim());
            return menuEyent;
        }
        return null;
    }

    DecimalFormat decimalFormat = new DecimalFormat("0.00");

    @Override
    public void onKeyDown(int keyCode) {
        if (this.menuPopup == null) return;
        switch (keyCode) {
            case KeyEvent.KEYCODE_D://<- left direction
                activity.setRightEyeScale(MathUtils.floatSub(mRightSubScale, 0.01f));
                mRightSubScale = activity.getRightEyeScale();
                String p = decimalFormat.format(mRightSubScale);//format 返回的是字符串
                CuiNiaoApp.textSpeechManager.speakNow("右眼缩放" + p);
                menuEyent.setItemCount(p + "X");
                isAdd = false;
                this.menuPopup.updateDisplay(getLogoImage(), getDisplayText(), getDisplayImages());
                toJson();
                break;
            case KeyEvent.KEYCODE_E://<+ right direction
                activity.setRightEyeScale(MathUtils.floatAdd(mRightSubScale, 0.01f));
                mRightSubScale = activity.getRightEyeScale();
                p = decimalFormat.format(mRightSubScale);//format 返回的是字符串
                menuEyent.setItemCount(p + "X");
                CuiNiaoApp.textSpeechManager.speakNow("右眼缩放" + p);
                isAdd = true;
                this.menuPopup.updateDisplay(getLogoImage(), getDisplayText(), getDisplayImages());
                toJson();
                break;
        }
    }

    @Override
    public void speak() {
        CuiNiaoApp.textSpeechManager.speakNow("右眼缩放");
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
