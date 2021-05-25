package com.cnsj.neptunglasses.view.gl;

/**
 * 眼部变化情况的回调  UI随动功能，UI要跟随眼部调节的参数同步
 */
public interface OnEyesChangeListener {

    void onIpdChange(float ipdOffset);

    void onLeftEyeOffsetChange(float leftX, float leftY);

    void onRightEyeOffsetChange(float rightX, float rightY);

    void onLeftScaleChange(float leftScale);

    void onRightScaleChange(float rightScale);

}
