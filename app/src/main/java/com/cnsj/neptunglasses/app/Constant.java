package com.cnsj.neptunglasses.app;

import com.cnsj.neptunglasses.R;

public class Constant {

    public static final int REQUEST_CAMERA_PERMISSION = 1;
    public static final String SP = "shared_p";
    public static final String SP_KEY_USER_CHOOSED = "sp_key_user_choosed";
    public static final String SP_KEY_POWER_SAVE = "sp_key_power_save";
    public static final String SP_KEY_SHOW_HINT = "sp_key_show_hint";
    public static final String SP_KEY_ACCOUNT_NAME = "sp_key_account_name";
    public static final String SP_KEY_ACCOUNT_PASS = "sp_key_account_pass";
    public static final String SP_KEY_VERSION = "sp_key_version";
    public static final int NATIVE_NEED_LENGTH = 25;
    public static final String UPDATE_URL = "http://server.cuiniaoshijue.com:9001/update/checkVersion";
    public static final String DOWNLOAD_URL = "http://server.cuiniaoshijue.com:9001/update/apk/";
    public static final String SNAP_DOWNLOAD_URL = "http://121.89.218.101:8080/download/";
    //    public static final String SNAP_DOWNLOAD_URL = "http://121.89.218.101:9001/update/apk/";
    public static final String CHECK_VERSION = "https://www.cuiniaoshijue.com/portum/download";
    public static final String NEW_UPDATE_URL = "http://121.89.223.217:10086/updatequery/";
    public static final String APK_NAME = "cnsj.apk";
    public static final String APK_MD5_TXT = "cnsj.txt";
    public static final String APK_NAME1 = "cnsj1.apk";
    public static final String APK_MD5_TXT1 = "cnsj1.txt";
    public static final int NO_MENU_SELECTED = -5;
    public static final String APP_UPDATE_KEY = "cuiniaovr-s839";
    public final static String live_url = "rtmp://81437.livepush.myqcloud.com/live/mafanwei?txSecret=5085c5803118c499745a919f41d7b130&txTime=5E63C4FF";
    public final static String pull_url = "rtmp://1234-81437.r.qlivecloud.com/live/mafanwei";
    public static String labelText = "beta";
    public static final float NORMAL_TEXT_SIZE = 1.45f;
    public static final int IPD = 0;
    public static final int ALL = 1;
    public static final int OCR_LEVEL = 10;
    public static final int BULETOOTH_LEVEL = 11;
    public static final int NORMAL_LEVEL = 3;
    public static final int MANUAL_LEVEL = 4;

    public enum options {
        left_x_offset,
        left_y_offset,
        right_x_offset,
        right_y_offset,
        instantZoomMode,
        userMode,
        userScale,
        userSaturation,
        userContrast,
        leftSubZoomRate,
        rightSubZoomRate,
        filter,
        binaryRatio,
        edgeWidth,
        quickViewOffsetX,
        quickViewOffsetY,
        left_eye_pos_x,
        right_eye_pos_x,
        imu_x,
        imu_y,
    }

    public enum ColorMode {
        mode_color("全彩", R.mipmap.fullcolor),//全彩
        mode_double_color("两色", R.mipmap.doublecolor),//两色两色_白黑
        mode_gray("灰度", R.mipmap.graycolor),//灰度
        mode_reversegray("反色", R.mipmap.reversegray),//反色
        mode_edge("描边", R.mipmap.edgecolor),//描边_白黑
        mode_fakecolor("伪彩色", R.mipmap.fakecolor);//伪彩色
        private String colorName;
        private int imageId;

        private ColorMode(String colorName, int imageId) {
            this.colorName = colorName;
            this.imageId = imageId;
        }


        public String getColorName() {
            return colorName;
        }

        public int getImageId() {
            return imageId;
        }

        public boolean equals(String color) {
            if (color.equals(getColorName())) {
                return true;
            }
            return false;
        }
    }
}
