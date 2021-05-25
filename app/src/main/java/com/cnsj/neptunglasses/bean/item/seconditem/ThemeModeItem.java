package com.cnsj.neptunglasses.bean.item.seconditem;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.KeyEvent;

import com.cnsj.neptunglasses.R;
import com.cnsj.neptunglasses.app.Constant;
import com.cnsj.neptunglasses.app.CuiNiaoApp;
import com.cnsj.neptunglasses.bean.BaseItem;
import com.cnsj.neptunglasses.bean.MenuEyent;
import com.cnsj.neptunglasses.view.CustomMenuPopupThree;
import com.google.gson.reflect.TypeToken;

import java.util.List;

/**
 * 主题模式选择，黑白模式，黑黄模式
 */
public class ThemeModeItem extends BaseItem {


    public ThemeModeItem(Activity activity) {
        super(activity);
    }

    @Override
    public int getLogoImage() {
        return 0;
    }

    @Override
    public void init() {
        CuiNiaoApp.isYellowMode = false;
        menuEyent = fromJson();
        if (menuEyent == null) {
            menuEyent = new MenuEyent();
            menuEyent.setItemName("黑黄模式");
            menuEyent.setItemCount("关");
        }
    }

    @Override
    public void load() {

    }

    @Override
    public void reset(int tag) {
        if (tag == Constant.ALL) {
            CuiNiaoApp.isYellowMode=false;
            menuEyent.setItemCount("关");
            toJson();
        }
    }

    @Override
    public boolean toJson() {
        String itemKey = "theme_mode";
        String json = activity.getGson().toJson(menuEyent);
        SharedPreferences.Editor editor = CuiNiaoApp.sharedPreferences.edit();
        editor.putString(itemKey, json);
        editor.commit();
        return false;
    }

    @Override
    public MenuEyent fromJson() {
        String itemKey = "theme_mode";
        String json = CuiNiaoApp.sharedPreferences.getString(itemKey, null);
        if (json != null) {
            MenuEyent menuEyent = activity.getGson().fromJson(json, new TypeToken<MenuEyent>() {
            }.getType());
            CuiNiaoApp.isYellowMode = "开".equals(menuEyent.getItemCount());
            return menuEyent;
        }
        return null;
    }

    @Override
    public void onKeyDown(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_H:
            case KeyEvent.KEYCODE_I:
                break;
            case KeyEvent.KEYCODE_E:
            case KeyEvent.KEYCODE_D:
            case KeyEvent.KEYCODE_F:
                Log.d("TAG", "onKeyDown: switchmode");
                CuiNiaoApp.isYellowMode = !CuiNiaoApp.isYellowMode;
                if (CuiNiaoApp.isYellowMode) {
                    CuiNiaoApp.textSpeechManager.speakNow("开启黑黄模式");
                    menuEyent.setItemCount("开");
//                    this.menuPopup.updateDisplay(0,"开",0);
                } else {
                    CuiNiaoApp.textSpeechManager.speakNow("关闭黑黄模式");
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
    public int getCurrentMenuLevel() {
        return 2;
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
            if (CuiNiaoApp.isYellowMode) {
                return R.mipmap.switch_open_y;
            } else {
                return R.mipmap.switch_close;
            }
        } else {
            if (CuiNiaoApp.isYellowMode)
                return R.mipmap.switch_no_select_open_y;
            else
                return R.mipmap.switch_no_selected_close;
        }

    }
}
