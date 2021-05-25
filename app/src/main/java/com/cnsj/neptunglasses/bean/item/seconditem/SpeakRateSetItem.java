package com.cnsj.neptunglasses.bean.item.seconditem;


import android.app.Activity;
import android.view.KeyEvent;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import com.cnsj.neptunglasses.R;
import com.cnsj.neptunglasses.activity.ThinGlassesActivity;
import com.cnsj.neptunglasses.app.Constant;
import com.cnsj.neptunglasses.app.CuiNiaoApp;
import com.cnsj.neptunglasses.bean.BaseItem;
import com.cnsj.neptunglasses.bean.MenuEyent;

/**
 * 语速设置
 */
public class SpeakRateSetItem extends BaseItem {


    public SpeakRateSetItem(Activity activity) {
        super(activity);
    }

    @Override
    public void init() {
        menuEyent = new MenuEyent();
        menuEyent.setItemName("语速调节");
        menuEyent.setItemCount(getSpeechSpeak() + "X");
    }

    @Override
    public int getCurrentMenuLevel() {
        return 2;
    }

    @Override
    public int getLogoImage() {
        return CuiNiaoApp.isYellowMode ? R.mipmap.speak_rate_y : R.mipmap.speak_rate;
    }

    @Override
    public String getDisplayText() {
        return getSpeechSpeak() + "X";
    }

    @NotNull
    private String getSpeechSpeak() {
        return CuiNiaoApp.textSpeechManager.getSpeechRate();
    }

    @Override
    public void load() {

    }

    @Override
    public void reset(int tag) {
        if (tag == Constant.ALL) {
            int speed = Integer.parseInt(getSpeechSpeak());
            setPeechRate(-speed);
            menuEyent.setItemCount(getSpeechSpeak() + "X");
        }
    }

    private void setPeechRate(int speed) {
        CuiNiaoApp.textSpeechManager.setSpeechRate(speed);
    }

    @Override
    public boolean toJson() {
        return false;
    }

    @Override
    public MenuEyent fromJson() {
        return null;
    }

    @Override
    public void onKeyDown(int keyCode) {
        if (this.menuPopup == null) return;
        int offset;
        switch (keyCode) {
            case KeyEvent.KEYCODE_D://left
                offset = -1;
                setPeechRate(offset);
                menuEyent.setItemCount(getSpeechSpeak() + "X");
                CuiNiaoApp.textSpeechManager.speakNow("语速" + getSpeechSpeak() + "倍");
                this.menuPopup.updateDisplay(getLogoImage(), getDisplayText(), getDisplayImages());
                break;
            case KeyEvent.KEYCODE_E://right
                offset = 1;
                setPeechRate(offset);
                menuEyent.setItemCount(getSpeechSpeak() + "X");
                CuiNiaoApp.textSpeechManager.speakNow("语速" + getSpeechSpeak() + "倍");
                this.menuPopup.updateDisplay(getLogoImage(), getDisplayText(), getDisplayImages());
                break;
        }
    }

    @Override
    public void speak() {
        CuiNiaoApp.textSpeechManager.speakNow("语速调节");
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
