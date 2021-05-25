package com.cnsj.neptunglasses.bean.item.seconditem;


import android.app.Activity;

import java.util.List;

import com.cnsj.neptunglasses.R;
import com.cnsj.neptunglasses.activity.ThinGlassesActivity;
import com.cnsj.neptunglasses.app.CuiNiaoApp;
import com.cnsj.neptunglasses.bean.BaseItem;
import com.cnsj.neptunglasses.bean.MenuEyent;

/**
 * 重置瞳距
 */
public class ResetEyeSetItem extends BaseItem {

    public ResetEyeSetItem(Activity activity) {
        super(activity);
    }

    @Override
    public void init() {
        menuEyent = new MenuEyent();
        menuEyent.setItemName("重置瞳距");
        menuEyent.setItemCount(">");
    }

    @Override
    public int getCurrentMenuLevel() {
        return 2;
    }

    private boolean success;

    @Override
    public int getLogoImage() {
        if (success)
            return CuiNiaoApp.isYellowMode ? R.mipmap.success_y : R.mipmap.success;
        else
            return CuiNiaoApp.isYellowMode ? R.mipmap.failure_y : R.mipmap.failure;
    }

    @Override
    public String getDisplayText() {
        if (success) {
            return "重置瞳距成功";
        } else {
            return "重置瞳距失败";
        }
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

    @Override
    public void onKeyDown(int keyCode) {
        if (this.menuPopup == null) return;
        switch (keyCode) {
            case -1:
                if (activity.resetIPD() == 1) {
                    this.menuPopup.resetIPD();
                    activity.resetIPD();
                    CuiNiaoApp.textSpeechManager.speakNow("重置瞳距成功");
                    success = true;
                    this.menuPopup.updateDisplay(getLogoImage(), getDisplayText(), getDisplayImages());
                } else {
                    CuiNiaoApp.textSpeechManager.speakNow("重置瞳距失败");
                    success = false;
                    this.menuPopup.updateDisplay(getLogoImage(), getDisplayText(), getDisplayImages());
                }
                break;
        }

    }

    @Override
    public void speak() {
        CuiNiaoApp.textSpeechManager.speakNow("重置瞳距");
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
    public boolean isImage() {
        return true;
    }

    @Override
    public int getItemCountImage(boolean isSelected) {
        if (isSelected) {
            return CuiNiaoApp.isYellowMode ? R.mipmap.right_y : R.mipmap.right;
        }
        return 0;
    }

}
