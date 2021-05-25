package com.cnsj.neptunglasses.view.gl;

/**
 * 特殊的参数设定(眼部情况相关参数)
 */
public interface SpecialSettingsInterface {

    /**
     * 管状视野
     *
     * @param scale
     */
    void setDoubleEyeScale(float scale);

    float getDoubleEyeScale();

    /**
     * 瞳距
     *
     * @param distance
     */
    void setIpd(int distance);

    /**
     * 左眼缩放
     *
     * @param scale
     */
    void setLeftSubScale(float scale);

    float getLeftScale();

    /**
     * 右眼缩放
     *
     * @param scale
     */
    void setRightSubScale(float scale);

    float getRightScale();

    /**
     * 左眼偏移
     *
     * @param x
     * @param y
     */
    void setLeftEyeOffset(int x, int y);

    String getLeftEyeOffset();

    /**
     * 右眼偏移
     *
     * @param x
     * @param y
     */
    void setRightEyeOffset(int x, int y);

    String getRightEyeOffset();

    /**
     * 重置瞳距
     */
    void resetIpd();

    /**
     * 重置所有参数
     */
    void resetEyesSettings();

    /**
     * UI随动 的监听
     *
     * @param onEyesChangeListener
     */
    void setOnEyesChangeListener(OnEyesChangeListener onEyesChangeListener);
}
