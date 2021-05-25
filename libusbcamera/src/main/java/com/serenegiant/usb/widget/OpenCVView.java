package com.serenegiant.usb.widget;

/**
 * 利用OpenCV 绘制图像防抖模式 和两色模式
 */
public class OpenCVView {

    static {
        System.loadLibrary("native-lib");
    }

    public native void processFrame(int tex2, int w, int h, boolean isstab, int edgemode);

    public native void resetFrame();
}
