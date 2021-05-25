package com.cnsj.neptunglasses.view.gl;

import android.graphics.Bitmap;

/**
 * 通用的参数设定
 */
public interface CustomSettingsInterface {

    /**
     * 定格
     */
    void setFreezeMode();

    void quitFreezeMode();

    /**
     * 缩放
     *
     * @param scale
     */
    void setScale(float scale);

    float getScale();

    /**
     * 设置颜色模式
     *
     * @param userMode
     */
    void setUserMode(int userMode);

    int getUserMode();


    /**
     * 饱和度
     *
     * @return
     */
    int getSaturation();

    /**
     * 饱和度范围1-5  1 1.4 1.8 2.2 2.6
     *
     * @param saturation
     */
    void setSaturation(int saturation);

    /**
     * 对比度
     *
     * @return
     */
    int getContrast();

    /**
     * 对比度范围1-5 0 0.2 0.4 0.6 0.8
     *
     * @param contrast
     */
    void setContrast(int contrast);

    /**
     * 亮度
     *
     * @return
     */
    int getBrightness();

    /**
     * 亮度范围1-5 0 0.2 0.4 0.6 0.8
     *
     * @param brightness
     */
    void setBrightness(int brightness);

    /**
     * 中心放大
     *
     * @param i
     */
    void setCenterScaleTag(int i);

    int getCenterScaleTag();


    /**
     * 快速放大，快速缩小
     */
    void quitFastScale();

    int getFastScaleTag();

    void setFastScaleTag();


    /**
     * 防抖
     *
     * @param isOpen
     */
    void setStabOn(boolean isOpen);

    /**
     * 照片显示
     *
     * @param bitmap
     */
    void setPhotoView(Bitmap bitmap);

    void quitPhotoView();

    /**
     * 重置3dof的位置
     */
    void resetCenterPosition();

    /**
     * 重置通用参数
     */
    void resetCustomSettings();
}
