package com.cnsj.neptunglasses.bean.item.seconditem;

import android.app.Activity;
import android.util.Log;
import android.view.KeyEvent;


import java.util.List;

import com.cnsj.neptunglasses.R;
import com.cnsj.neptunglasses.activity.ThinGlassesActivity;
import com.cnsj.neptunglasses.app.CuiNiaoApp;
import com.cnsj.neptunglasses.bean.BaseItem;
import com.cnsj.neptunglasses.bean.MenuEyent;
import com.cnsj.neptunglasses.view.CustomMenuPopupThree;

/**
 * 防抖开关
 */
public class AntiShakeSetItem extends BaseItem {

    private boolean isOpen;

    public AntiShakeSetItem(Activity activity) {
        super(activity);
    }

    @Override
    public int getLogoImage() {
        return 0;
    }

    @Override
    public int getDisplayImages() {
        return super.getDisplayImages();
    }

    @Override
    public void init() {
        isOpen = true;
        menuEyent = new MenuEyent();
        menuEyent.setItemName("防抖");
        menuEyent.setItemCount("开");
        setStabOn(isOpen);
    }

    @Override
    public int getCurrentMenuLevel() {
        return 2;
    }

    @Override
    public void load() {
        setStabOn(isOpen);
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
//        if (this.menuPopup == null) return;
        switch (keyCode) {
            case KeyEvent.KEYCODE_E:
            case KeyEvent.KEYCODE_D:
            case KeyEvent.KEYCODE_F:
                Log.d("TAG", "onKeyDown: switchmode");
                isOpen = !isOpen;
                setStabOn(isOpen);
                if (isOpen) {
                    CuiNiaoApp.textSpeechManager.speakNow("开启防抖");
                    menuEyent.setItemCount("开");
//                    this.menuPopup.updateDisplay(0,"开",0);
                } else {
                    CuiNiaoApp.textSpeechManager.speakNow("关闭防抖");
                    menuEyent.setItemCount("关");
//                    this.menuPopup.updateDisplay(0,"关",0);
                }
                break;
        }
    }

    private void setStabOn(boolean isOpen) {
//        activity.mUVCCameraView.setStabOn(isOpen);
        activity.yuvGLSurfaceView.setStabOn(isOpen);
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
            if (isOpen) {
                return CuiNiaoApp.isYellowMode ? R.mipmap.switch_open_y : R.mipmap.switch_open;
            } else {
                return CuiNiaoApp.isYellowMode ? R.mipmap.switch_close_y : R.mipmap.switch_close;
            }
        } else {
            if (isOpen)
                return CuiNiaoApp.isYellowMode ? R.mipmap.switch_no_select_open_y : R.mipmap.switch_no_select_open;
            else
                return CuiNiaoApp.isYellowMode ? R.mipmap.switch_no_selected_close_y : R.mipmap.switch_no_selected_close;
        }

    }
}
