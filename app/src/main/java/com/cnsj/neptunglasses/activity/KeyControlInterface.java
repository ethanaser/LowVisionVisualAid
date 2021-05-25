package com.cnsj.neptunglasses.activity;

/**
 * 按键控制接口
 */
public interface KeyControlInterface {

    /**
     * 缩放
     *
     * @param scale
     */
    void setScale(int scale);

    int getScale();

    /**
     * 快速放大，快速缩小
     *
     * @param fastScaleTag
     */
    void setFastScale(int fastScaleTag);

    int getFastScaleTag();

    /**
     * 中心放大
     *
     * @param centerScaleTag
     */
    void setCenterScale(int centerScaleTag);

    int getCenterScaleTag();

    /**
     * 定格
     *
     * @param freezeMode
     */
    void setFreezeMode(int freezeMode);

    int getFreezeMode();

    /**
     * 颜色模式
     *
     * @param userMode
     */
    void setUserMode(int userMode);

    int getUserMode();

}
