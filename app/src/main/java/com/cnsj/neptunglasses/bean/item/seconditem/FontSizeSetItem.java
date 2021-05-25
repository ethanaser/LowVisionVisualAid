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
 * 字号设定
 */
public class FontSizeSetItem extends BaseItem {

    private int textScale;

    public FontSizeSetItem(Activity activity) {
        super(activity);
    }

    @Override
    public void init() {
        textScale = 3;
        menuEyent = fromJson();
        if (menuEyent == null) {
            menuEyent = new MenuEyent();
            menuEyent.setItemName("文字字号");
            menuEyent.setItemCount("" + textScale);
            setTextScale(textScale);
        }
    }

    @Override
    public int getCurrentMenuLevel() {
        return 2;
    }

    @Override
    public int getLogoImage() {
        return CuiNiaoApp.isYellowMode ? R.mipmap.font_size_y : R.mipmap.font_size;
    }

    @Override
    public String getDisplayText() {
        return "文字字号" + getMenuCount();
    }

//    @Override
//    public int getDisplayImages() {
//        return fontSizeImages[textScale - 1];
//    }

    /**
     * 设定字号
     */
    private void setTextScale(int textScale) {
        activity.setPopupTextScaleOffset(textScale);
    }

    @Override
    public void load() {
        textScale = Integer.parseInt(getMenuCount().trim());
        setTextScale(textScale);
    }

    @Override
    public void reset(int tag) {
        if (tag == Constant.ALL) {
            textScale = 3;
            menuEyent.setItemCount(Integer.toString(textScale));
            setTextScale(textScale);
            toJson();
        }
    }

    @Override
    public boolean toJson() {
        String itemKey = "font_size";
        String json = activity.getGson().toJson(menuEyent);
        SharedPreferences.Editor editor = CuiNiaoApp.sharedPreferences.edit();
        editor.putString(itemKey, json);
        editor.commit();
        return false;
    }

    @Override
    public MenuEyent fromJson() {
        String itemKey = "font_size";
        String json = CuiNiaoApp.sharedPreferences.getString(itemKey, null);
        if (json != null) {
            MenuEyent menuEyent = activity.getGson().fromJson(json, new TypeToken<MenuEyent>() {
            }.getType());
            textScale = Integer.parseInt(menuEyent.getItemCount().trim());
            setTextScale(textScale);
            return menuEyent;
        }
        return null;
    }

    @Override
    public void onKeyDown(int keyCode) {
        if (this.menuPopup == null) return;
        switch (keyCode) {
            case KeyEvent.KEYCODE_D://-> left
                textScale = Integer.parseInt(getMenuCount());
                if (textScale <= 1) return;
                textScale--;
                setTextScale(textScale);
                menuEyent.setItemCount(String.valueOf(textScale));
                CuiNiaoApp.textSpeechManager.speakNow("文字字号" + textScale);
                this.menuPopup.updateDisplay(getLogoImage(), getDisplayText(), getDisplayImages());
                this.menuPopup.changeView();
                toJson();
                break;
            case KeyEvent.KEYCODE_E://-> right
                textScale = Integer.parseInt(getMenuCount());
                if (textScale >= 5) return;
                textScale++;
                setTextScale(textScale);
                menuEyent.setItemCount(String.valueOf(textScale));
                CuiNiaoApp.textSpeechManager.speakNow("文字字号" + textScale);
                this.menuPopup.updateDisplay(getLogoImage(), getDisplayText(), getDisplayImages());
                this.menuPopup.changeView();
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
