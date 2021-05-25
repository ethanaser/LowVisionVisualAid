package com.cnsj.neptunglasses.bean.item.seconditem;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.KeyEvent;


import com.google.gson.reflect.TypeToken;

import java.util.List;

import com.cnsj.neptunglasses.R;
import com.cnsj.neptunglasses.app.CuiNiaoApp;
import com.cnsj.neptunglasses.bean.BaseItem;
import com.cnsj.neptunglasses.bean.MenuEyent;

/**
 * 瞳距 2级菜单
 */
public class EyeSpaceSetItem extends BaseItem {


    private static final String TAG = "EyeSpaceSetItem";

    public EyeSpaceSetItem(Activity activity) {
        super(activity);
    }

    private int mIPD = 62;

    @Override
    public void init() {
        menuEyent = fromJson();
        if (menuEyent == null) {
            menuEyent = new MenuEyent();
            menuEyent.setItemName("瞳距");
            menuEyent.setItemCount("62mm");
        }
        int distance = Integer.parseInt(menuEyent.getItemCount().substring(0, 2));
        Log.e(TAG, "distance " + distance);
        activity.setIpd(distance, 1);
    }

    @Override
    public int getCurrentMenuLevel() {
        return 2;
    }

    @Override
    public int getLogoImage() {
        return CuiNiaoApp.isYellowMode ? R.mipmap.eye_space_y : R.mipmap.eye_space;
    }

    private String displayText = "";

    @Override
    public String getDisplayText() {
        return displayText;
    }

    @Override
    public void load() {
        mIPD = Integer.parseInt(getMenuCount().trim().substring(0, getMenuCount().length() - 2));
        int offset = 62 - mIPD;
        float eye_translation = offset * 0.0078f * 2400 / 2;
        //        activity.setEyeTranslation(eye_translation);
        Log.e(TAG, "eye_translation" + eye_translation);
    }

    @Override
    public void reset(int tag) {
        mIPD = 62;
        menuEyent.setItemCount(mIPD + "mm");
        toJson();
    }

    @Override
    public boolean toJson() {
        String itemKey = "eye";
        String json = activity.getGson().toJson(menuEyent);
        SharedPreferences.Editor editor = CuiNiaoApp.sharedPreferences.edit();
        editor.putString(itemKey, json);
        editor.commit();
        return false;
    }

    @Override
    public MenuEyent fromJson() {
        String itemKey = "eye";
        String json = CuiNiaoApp.sharedPreferences.getString(itemKey, null);
        if (json != null) {
            MenuEyent menuEyent = activity.getGson().fromJson(json, new TypeToken<MenuEyent>() {
            }.getType());
            activity.setIpd(Integer.parseInt(menuEyent.getItemCount().trim().substring(0, menuEyent.getItemCount().length() - 2)), 0);
            return menuEyent;
        }
        return null;
    }

    @Override
    public void onKeyDown(int keyCode) {
        if (this.menuPopup == null) return;
        switch (keyCode) {
            case -1:
                isReset = false;
                displayText = getMenuCount();
                break;
            case KeyEvent.KEYCODE_D:
                if (mIPD <= 50) {
                    return;
                }
                mIPD -= 1;
                activity.setIpd(mIPD, -1);
                menuEyent.setItemCount(mIPD + "mm");
                CuiNiaoApp.textSpeechManager.speakNow("瞳距" + mIPD + "毫米");
                displayText = getMenuCount();
                this.menuPopup.updateDisplay(getLogoImage(), getDisplayText(), getDisplayImages());
                toJson();
                isReset = false;
                break;
            case KeyEvent.KEYCODE_E://-> right
                if (mIPD >= 75) {
                    return;
                }
                mIPD += 1;
                activity.setIpd(mIPD, 1);
                menuEyent.setItemCount(mIPD + "mm");
                CuiNiaoApp.textSpeechManager.speakNow("瞳距" + mIPD + "毫米");
                displayText = getMenuCount();
                this.menuPopup.updateDisplay(getLogoImage(), getDisplayText(), getDisplayImages());
                toJson();
                isReset = false;
                break;
            case KeyEvent.KEYCODE_C:// 下方向键
                if (!isReset) {
                    isReset = true;
                    displayText = "再次按下将重置瞳距";
                    CuiNiaoApp.textSpeechManager.speakNow(displayText);
                    this.menuPopup.updateDisplay(getLogoImage(), getDisplayText(), getDisplayImages());
                } else {
                    isReset = false;
                    if (activity.resetIPD() == 1) {
                        this.menuPopup.resetIPD();
                        activity.resetIPD();
                        reset(0);
                        CuiNiaoApp.textSpeechManager.speakNow("重置瞳距成功");
                        displayText = getMenuCount();
                        this.menuPopup.updateDisplay(getLogoImage(), getDisplayText(), getDisplayImages());
                    } else {
                        CuiNiaoApp.textSpeechManager.speakNow("重置瞳距失败");
                        displayText = "重置瞳距失败";
                        this.menuPopup.updateDisplay(getLogoImage(), getDisplayText(), getDisplayImages());
                    }
                }
                break;
        }
    }

    private boolean isReset = false;

    @Override
    public void speak() {
        CuiNiaoApp.textSpeechManager.speakNow("瞳距");
    }

    @Override
    public void update() {

    }

    @Override
    public void finish() {
        this.menuPopup = null;
        isReset = false;
    }

    @Override
    public List<BaseItem> getNextMenu() {
        return null;
    }
}
