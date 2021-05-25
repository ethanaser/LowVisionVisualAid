package com.serenegiant.usb.widget;

/**
 * 眼部变化情况的回调
 */
public interface OnEyesChangeListener {

    void onIpdChange(float ipdOffset);

    void onLeftEyeOffsetChange(float leftX, float leftY);

    void onRightEyeOffsetChange(float rightX, float rightY);

    void onLeftScaleChange(float leftScale);

    void onRightScaleChange(float rightScale);

}
