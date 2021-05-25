package com.cnsj.neptunglasses.constant;

/**
 * 管理handler的message的TAG
 */
public class What {


    /**
     * 通知菜单UI变化的TAG
     */
    public static final int VIEW_CHANGE = 1;
    public static final int WIFI_CONNECT = 3;
    public static final int POPUP_DISMISS = 4;
    public static final int CHANGE_CONTENT = 5;


    /**
     * 控制底部提示信息消失的 tag
     */
    public static final int PROMPT_GONE = 1;
    /**
     * 双击放大和单机放大时 区分的tag
     */
    public static final int DOUBLE_CLICK_INTERCEPT = 2;
    /**
     * 遥控器设备连接状态的tag
     */
    public static final int DEVICE_DISCONNECT = 3;
    /**
     * 佩戴设备状态的tag
     */
    public static final int WEAR_OFF = 4;

    /**
     * 图像识别后的tag
     */
    public static final int OCR_SUCCESS = 100;
    public static final int OCR_FAULIRE = 101;
    public static final int WIFI_SUCCES = 102;
    public static final int WIFI_FAULIRE = 103;
    public static final int WIFI_TIMEOUT = 104;
    public static final int SAVE_BITMAP_SUCCESS = 105;
    public static final int SAVE_BITMAP_FAULIRE = 106;


    /**
     * usb摄像头操作的tag
     */
    public static final int OPEN_SUCCESS = 201;
    public static final int OPEN_FAULIRE = 202;
    public static final int PREVIEW_SUCCES = 203;
    public static final int PREVIEW_FAULIRE = 204;
    public static final int STOP_SUCCES = 205;
    public static final int STOP_FAULIRE = 206;
    public static final int CLOSE_SUCCESS = 207;
    public static final int CLOSE_FAULIRE = 208;
    public static final int RGB565 = 0;
    public static final int ARGB8888 = 1;
}
