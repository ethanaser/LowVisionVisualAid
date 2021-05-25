package com.cnsj.neptunglasses.bean.item;


import android.app.Activity;
import android.view.KeyEvent;

import java.util.List;

import com.cnsj.neptunglasses.R;
import com.cnsj.neptunglasses.activity.ThinGlassesActivity;
import com.cnsj.neptunglasses.app.CuiNiaoApp;
import com.cnsj.neptunglasses.bean.BaseItem;
import com.cnsj.neptunglasses.bean.MenuEyent;

/**
 * 重置设置
 */
public class ResetSetItem extends BaseItem {

    public ResetSetItem(Activity activity) {
        super(activity);
    }

    @Override
    public void init() {
        menuEyent = new MenuEyent();
        menuEyent.setItemName("重置设置");
        menuEyent.setItemCount(">");
    }

    @Override
    public int getCurrentMenuLevel() {
        return 1;
    }


    private boolean success;

    @Override
    public int getLogoImage() {
        if (this.menuPopup == null) {
            return CuiNiaoApp.isYellowMode ? R.mipmap.photo_album_s_y : R.mipmap.photo_album_s;
        }
        if (success)
            return CuiNiaoApp.isYellowMode ? R.mipmap.success_y : R.mipmap.success;
        else
            return CuiNiaoApp.isYellowMode ? R.mipmap.update_remind_y : R.mipmap.update_remind;
    }


    @Override
    public int getBigLogo() {
        return CuiNiaoApp.isYellowMode ? R.mipmap.reset_set_b_y : R.mipmap.reset_set_b;
    }

    @Override
    public int getSmallLogo() {
        return CuiNiaoApp.isYellowMode ? R.mipmap.reset_set_s_y : R.mipmap.reset_set_s;
    }

    @Override
    public String getDisplayText() {
        return displayText;
    }

    @Override
    public void load() {

    }

    @Override
    public void reset(int tag) {

    }

    @Override
    public boolean toJson() {
        return false;
    }

    @Override
    public MenuEyent fromJson() {
        return null;
    }

    private String displayText;

    @Override
    public void onKeyDown(int keyCode) {
        if (this.menuPopup == null) return;
        switch (keyCode) {
            case -1:
                success = false;
                displayText = "按确认键重置设置";
                this.menuPopup.updateDisplay(getLogoImage(), getDisplayText(), getDisplayImages());
            case KeyEvent.KEYCODE_F:
                activity.resetALL();
                CuiNiaoApp.textSpeechManager.speakNow("重置设置成功");
                displayText = "重置设置成功";
                success = true;
                this.menuPopup.updateDisplay(getLogoImage(), getDisplayText(), getDisplayImages());
                break;
        }
    }

    @Override
    public void speak() {

    }

    @Override
    public void update() {
        CuiNiaoApp.textSpeechManager.speakNow("重置设置");
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
