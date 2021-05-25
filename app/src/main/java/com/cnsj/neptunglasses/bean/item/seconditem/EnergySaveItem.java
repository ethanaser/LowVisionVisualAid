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
import com.cnsj.neptunglasses.view.CustomMenuPopupThree;

/**
 * 节能开关
 */
public class EnergySaveItem extends BaseItem {

    private boolean isSave;

    public EnergySaveItem(Activity activity) {
        super(activity);
    }

    @Override
    public void init() {
        isSave = false;
        menuEyent = fromJson();
        if (menuEyent == null) {
            menuEyent = new MenuEyent();
            menuEyent.setItemName("节能");
            menuEyent.setItemCount("关");
        }
    }

    @Override
    public int getCurrentMenuLevel() {
        return 2;
    }

    @Override
    public int getLogoImage() {
        return 0;
    }


    @Override
    public void load() {

    }

    @Override
    public void reset(int tag) {
        if (tag == Constant.ALL) {
            isSave = false;
            menuEyent.setItemCount("关");
            toJson();
        }
    }

    @Override
    public boolean toJson() {
        String itemKey = "save_energy";
        String json = activity.getGson().toJson(menuEyent);
        SharedPreferences.Editor editor = CuiNiaoApp.sharedPreferences.edit();
        editor.putString(itemKey, json);
        editor.commit();
        return false;
    }

    @Override
    public MenuEyent fromJson() {
        String itemKey = "save_energy";
        String json = CuiNiaoApp.sharedPreferences.getString(itemKey, null);
        if (json != null) {
            MenuEyent menuEyent = activity.getGson().fromJson(json, new TypeToken<MenuEyent>() {
            }.getType());
            isSave = "开".equals(menuEyent.getItemCount());
            return menuEyent;
        }
        return null;
    }

    @Override
    public void onKeyDown(int keyCode) {
//        if (this.menuPopup == null) return;
        switch (keyCode) {
            case KeyEvent.KEYCODE_H:
            case KeyEvent.KEYCODE_I:
                break;
            case KeyEvent.KEYCODE_E:
            case KeyEvent.KEYCODE_D:
            case KeyEvent.KEYCODE_F:
                Log.d("TAG", "onKeyDown: switchmode");
                isSave = !isSave;
                if (isSave) {
                    CuiNiaoApp.textSpeechManager.speakNow("开启节能");
                    menuEyent.setItemCount("开");
//                    this.menuPopup.updateDisplay(0,"开",0);
                } else {
                    CuiNiaoApp.textSpeechManager.speakNow("关闭节能");
                    menuEyent.setItemCount("关");
//                    this.menuPopup.updateDisplay(0,"关",0);
                }
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

    @Override
    public boolean intentToMenu2(CustomMenuPopupThree menuPopup) {
        return false;
    }

    @Override
    public boolean isImage() {
        return true;
    }

    @Override
    public int getItemCountImage(boolean isSelected) {
        if (isSelected) {
            if (isSave) {
                return CuiNiaoApp.isYellowMode ? R.mipmap.switch_open_y : R.mipmap.switch_open;
            } else {
                return CuiNiaoApp.isYellowMode ? R.mipmap.switch_close_y : R.mipmap.switch_close;
            }
        } else {
            if (isSave)
                return CuiNiaoApp.isYellowMode ? R.mipmap.switch_no_select_open_y : R.mipmap.switch_no_select_open;
            else
                return CuiNiaoApp.isYellowMode ? R.mipmap.switch_no_selected_close_y : R.mipmap.switch_no_selected_close;
        }

    }
}
