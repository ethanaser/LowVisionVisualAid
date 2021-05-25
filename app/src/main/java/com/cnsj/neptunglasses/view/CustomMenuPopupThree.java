package com.cnsj.neptunglasses.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;


import com.cnsj.neptunglasses.R;
import com.cnsj.neptunglasses.activity.YUVModeActivity;
import com.cnsj.neptunglasses.bean.item.ResetSetItem;
import com.jiangdg.usbcamera.utils.MathUtils;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.lxj.xpopup.impl.FullScreenPopupView;

import com.cnsj.neptunglasses.app.CuiNiaoApp;
import com.cnsj.neptunglasses.bean.BaseItem;
import com.cnsj.neptunglasses.bean.item.ManualChildItem;
import com.cnsj.neptunglasses.bean.item.PhotoAlbumItem;
import com.cnsj.neptunglasses.bean.item.UpdateItem;
import com.cnsj.neptunglasses.utils.SightaidUtil;
import com.cnsj.neptunglasses.utils.VolumeManager;


/**
 * Created by Zph on 2020/7/14.
 */
public class CustomMenuPopupThree extends FullScreenPopupView implements ViewObserver {
    private Context mContext;
    private FullScreenPopupView menuPopup;
    private BasePopupView popupView;
    private YUVModeActivity mActivity;
    private BaseItem baseItem;
    private View msg_left, msg_right, layout_left, layout_right, popupThreeLayout;//image_layout_left, image_layout_right, ;
    private ImageView logo_left, logo_right, image_left, image_right;// image_font_left, image_font_right;
    private TextView text_left, text_right;// text_font_left, text_font_right;
    private boolean isHomeCall;
    private int textSize = 25;

    public CustomMenuPopupThree(@NonNull Context context, Activity activity) {
        super(context);
        this.mContext = context;
        this.mActivity = (YUVModeActivity) activity;
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.menu_set_param;
    }

    @Override
    protected void onShow() {
        super.onShow();
        changeView();
        if (baseItem != null) {
//            if (baseItem instanceof FontSizeSetItem) {
//                if (!noRemind) {
//                    image_layout_left.setVisibility(VISIBLE);
//                    image_layout_right.setVisibility(VISIBLE);
//                    updateDisplay(baseItem.getLogoImage(), baseItem.getDisplayText(), baseItem.getDisplayImages());
//                }
//            } else {
            if (!noRemind) {
                layout_left.setVisibility(VISIBLE);
                layout_right.setVisibility(VISIBLE);
                updateDisplay(baseItem.getLogoImage(), baseItem.getDisplayText(), baseItem.getDisplayImages());
            }
//            }

        }


    }


    @Override
    protected void onDismiss() {
        super.onDismiss();
        if (isHomeCall) return;
        if (mActivity.getSecondPosition() == -1) {
            menuPopup = new CustomMenuPopup(mContext, mActivity);
            popupView = new XPopup.Builder(mContext)
                    .hasStatusBarShadow(false)
                    .isDestroyOnDismiss(true)
                    .hasStatusBar(false)
                    .asCustom(menuPopup)
                    .show();
        } else {
            menuPopup = new CustomMenuPopupTwo(mContext, mActivity);
            popupView = new XPopup.Builder(mContext)
                    .hasStatusBarShadow(false)
                    .isDestroyOnDismiss(true)
                    .hasStatusBar(false)
                    .asCustom(menuPopup)
                    .show();
        }
        mActivity.setMenuPopup(menuPopup, popupView);
        //CuiNiaoApp.textSpeechManager.speakNow("退出"+formatSelect(CustomMenuPopup.p1));
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }


    @Override
    protected void onCreate() {
        super.onCreate();
        mActivity.add(this);
        isHomeCall = false;
        popupThreeLayout = findViewById(R.id.menu2_layout);
        layout_left = findViewById(R.id.menu_set_left);
        layout_right = findViewById(R.id.menu_set_right);
//        image_layout_left = findViewById(R.id.menu_set_image_left);
//        image_layout_right = findViewById(R.id.menu_set_image_right);
        msg_left = findViewById(R.id.menu_set_layout_left);
        msg_right = findViewById(R.id.menu_set_layout_right);
        logo_left = msg_left.findViewById(R.id.menu_msg_image);
        image_left = msg_left.findViewById(R.id.menu_msg_image2);
        text_left = msg_left.findViewById(R.id.menu_msg_text);
        logo_right = msg_right.findViewById(R.id.menu_msg_image);
        image_right = msg_right.findViewById(R.id.menu_msg_image2);
        text_right = msg_right.findViewById(R.id.menu_msg_text);
        if (baseItem instanceof ResetSetItem){
            text_left.setTextColor(mActivity.getResources().getColor(R.color.white));
            text_right.setTextColor(mActivity.getResources().getColor(R.color.white));
        }else{
            text_left.setTextColor(CuiNiaoApp.isYellowMode ? mActivity.getResources().getColor(R.color.yellow_mode_selected) : mActivity.getResources().getColor(R.color.white));
            text_right.setTextColor(CuiNiaoApp.isYellowMode ? mActivity.getResources().getColor(R.color.yellow_mode_selected) : mActivity.getResources().getColor(R.color.white));
        }
//        image_font_left = image_layout_left.findViewById(R.id.menu_msg_long_image);
//        image_font_right = image_layout_right.findViewById(R.id.menu_msg_long_image);
//        text_font_left = image_layout_left.findViewById(R.id.menu_msg_long_text);
//        text_font_right = image_layout_right.findViewById(R.id.menu_msg_long_text);
        noRemind = false;
        if (baseItem instanceof ManualChildItem) {
            ((ManualChildItem) baseItem).speak(0);
        } else {
            baseItem.speak();
        }
        if (baseItem != null) {
            baseItem.onKeyDown(-1);
        }
        layout_left.setVisibility(INVISIBLE);
        layout_right.setVisibility(INVISIBLE);
//        image_layout_left.setVisibility(GONE);
//        image_layout_right.setVisibility(GONE);
    }

    private int mUserScale = 0;
    private int textScale;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getRepeatCount() == 0) {
            event.startTracking();
            if (baseItem != null) {
                baseItem.onKeyDown(keyCode);
                baseItem.onKeyDown(keyCode, event);
            }
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    isHomeCall = true;
                    dismiss();
                    mActivity.displayPreview();
                    baseItem.finish();
                    mActivity.setFirstPosition(0);
                    mActivity.setSecondPosition(-1);
                    mActivity.setThirdPosition(-1);
                    mActivity.onKeyDown(keyCode, event);
                    break;
                case KeyEvent.KEYCODE_I:
                case KeyEvent.KEYCODE_H:
                    if (baseItem instanceof PhotoAlbumItem) {
                        return true;
                    }
                    if (baseItem instanceof UpdateItem) {
                        return true;
                    }
                    dismiss();
                    mActivity.coverUpPreview();
                    baseItem.finish();
                    CuiNiaoApp.textSpeechManager.speakNow("退出" + baseItem.getMenuName());
                    break;
                case KeyEvent.KEYCODE_J://放大
                    mActivity.setScale(false);
                    mUserScale = mActivity.getScale();
                    CuiNiaoApp.textSpeechManager.speakNow("放大" + mActivity.regexEnd(mActivity.scaleX[mUserScale]) + "倍");
                    return true;
                case KeyEvent.KEYCODE_K://缩小
                    mActivity.setScale(true);
                    mUserScale = mActivity.getScale();
                    CuiNiaoApp.textSpeechManager.speakNow("放大" + mActivity.regexEnd(mActivity.scaleX[mUserScale]) + "倍");
                    return true;
                case KeyEvent.KEYCODE_D:
                    break;
                case KeyEvent.KEYCODE_E:
                    break;
                case KeyEvent.KEYCODE_B:
                    break;
                case KeyEvent.KEYCODE_C:
                    break;
                case KeyEvent.KEYCODE_F:
                    Log.d("popup", "onKeyDown: " + (event.getAction()));
                    return true;
//            case KeyEvent.KEYCODE_C:
//                break;
                case KeyEvent.KEYCODE_VOLUME_UP://音量+ 放大
                case KeyEvent.KEYCODE_VOLUME_DOWN://音量- 缩小
                    //长按会触发系统音量增大减小
                    return true;
                case KeyEvent.KEYCODE_0:
                    Log.d("YUVModeActivity", "onKeyDown: 电源键啊");
                    isShutDown = false;
                    currentTimeMillis = System.currentTimeMillis();
                    return true;
                default:
                    mActivity.notifyError();
                    break;
            }
        } else {
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP://音量+ 放大
                case KeyEvent.KEYCODE_VOLUME_DOWN://音量- 缩小
                    //长按会触发系统音量增大减小
                    return true;
                case KeyEvent.KEYCODE_0:

                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private boolean isShutDown = false;
    private long currentTimeMillis = 0l;

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_F:
                if (baseItem != null)
                    baseItem.onKeyUp(keyCode);
                Log.d("popup", "onKeyUp: " + (event.getAction()));
                return true;
        }
        return super.onKeyUp(keyCode, event);
    }


    private String formatZoomRate(int scale) {//这种方式需要修改一下
        switch (scale) {
            case 1:
                return "1";
            case 2:
                return "1.5";
            case 3:
                return "2";
            case 4:
                return "3";
            case 5:
                return "5";
            case 6:
                return "10";
            case 7:
                return "20";
            case 8:
                return "25";
        }
        return null;
    }


    /**
     * 修改view的偏移以及缩放
     */
    @Override
    public void changeView() {
        float horizantal = ((YUVModeActivity) mContext).getPopupTranslationOffset()[0];
        float xLeft = ((YUVModeActivity) mContext).getPopupTranslationOffset()[1];
        float yLeft = ((YUVModeActivity) mContext).getPopupTranslationOffset()[2];
        float xRight = ((YUVModeActivity) mContext).getPopupTranslationOffset()[3];
        float yRight = ((YUVModeActivity) mContext).getPopupTranslationOffset()[4];
        Log.e("changeView", "xLeft " + xLeft + "yLeft " + yLeft + "xRight " + xRight + "yRight " + yRight);
        logo_left.setTranslationX(-horizantal + xLeft);
        text_left.setTranslationX(-horizantal + xLeft);
        layout_left.setTranslationY(-yLeft);
        logo_right.setTranslationX(horizantal + xRight);
        text_right.setTranslationX(horizantal + xRight);
        layout_right.setTranslationY(-yRight);
        textScale = ((YUVModeActivity) mContext).getPopupTextScaleOffset()[0];
        float scale = 1 + (textScale - 1) * 0.2f;
        float leftScale = ((YUVModeActivity) mContext).getPopupScaleOffset()[0];
        float rightScale = ((YUVModeActivity) mContext).getPopupScaleOffset()[1];
        leftScale = MathUtils.floatMultiply(leftScale, scale);
        rightScale = MathUtils.floatMultiply(rightScale, scale);
        Log.d("popup", "scale: " + leftScale + " " + rightScale);
        //view间的间距 同步放大间距
        float leftMargin = SightaidUtil.dpToPx(mActivity, 20.0f);
        leftMargin = MathUtils.floatMultiply(leftMargin, leftScale) - leftMargin;
        float rightMargin = SightaidUtil.dpToPx(mActivity, 20.0f);
        rightMargin = MathUtils.floatMultiply(rightMargin, rightScale) - rightMargin;
        text_left.setTextSize(MathUtils.floatMultiply(textSize, leftScale));
        text_right.setTextSize(MathUtils.floatMultiply(textSize, rightScale));
        logo_left.setScaleX(leftScale);
        logo_left.setScaleY(leftScale);
        image_left.setScaleX(leftScale);
        image_left.setScaleY(leftScale);
        logo_right.setScaleX(rightScale);
        logo_right.setScaleY(rightScale);
        image_right.setScaleX(rightScale);
        image_right.setScaleY(rightScale);
        Log.e("image_left", "horizantal " + horizantal + "xLeft " + xLeft + "leftMargin " + leftMargin);
        Log.e("image_left", "horizantal " + horizantal + "xRight " + xRight + "rightMargin " + rightMargin);
//        image_left.setTranslationX(horizantal + xLeft - leftMargin);
        image_left.setTranslationX(-horizantal + xLeft + leftMargin);
        image_right.setTranslationX(horizantal + xRight + rightMargin);
    }


    @Override
    public void notifyWifiConnected() {
        if (mActivity.isWifiConnected()) {
            dismiss();
            baseItem.update();
        } else {
            if (mActivity.getFailReason() != null && !mActivity.isWifiConnected())
                CuiNiaoApp.textSpeechManager.speakNow(mActivity.getFailReason());
            baseItem.update();
//            mActivity.qrScanStart();
        }
    }


    @Override
    public void changeContent(String text) {
        if (text_left != null) {
            text_left.setText(text);
            text_right.setText(text);
        }
    }


    @Override
    public void popupDismiss() {
        if (isShow()) {
            dismiss();
        }
        baseItem.finish();
        isHomeCall = true;
    }


    /**
     * 设定由哪个item跳转过来的
     *
     * @param baseItem
     */
    public void setBaseItem(BaseItem baseItem) {
        this.baseItem = baseItem;
    }


    /**
     * 更新视图
     *
     * @param logoImage
     * @param menuCount
     * @param contentImages
     */
    public void updateDisplay(int logoImage, String menuCount, int contentImages) {
        logo_left.setImageResource(logoImage);
        logo_right.setImageResource(logoImage);
        if (menuCount == null) {
            text_left.setVisibility(GONE);
            text_right.setVisibility(GONE);
            image_left.setImageResource(contentImages);
            image_right.setImageResource(contentImages);
        }
        if (contentImages == -1) {
            image_left.setVisibility(GONE);
            image_right.setVisibility(GONE);
            text_left.setText(menuCount);
            text_right.setText(menuCount);
        }
    }

    /**
     * 更新视图
     *
     * @param logoImage
     * @param menuCount
     * @param contentImages
     */
    public void updateDisplayAsyn(int logoImage, String menuCount, int contentImages) {
        logo_left.post(new Runnable() {
            @Override
            public void run() {
                logo_left.setImageResource(logoImage);
                logo_right.setImageResource(logoImage);
                if (menuCount == null) {
                    text_left.setVisibility(GONE);
                    text_right.setVisibility(GONE);
                    image_left.setImageResource(contentImages);
                    image_right.setImageResource(contentImages);
                }
                if (contentImages == -1) {
                    image_left.setVisibility(GONE);
                    image_right.setVisibility(GONE);
                    text_left.setText(menuCount);
                    text_right.setText(menuCount);
                }
            }
        });
    }


    public void resetIPD() {
        logo_left.setTranslationX(0f);
        logo_right.setTranslationX(0f);
        image_left.setTranslationX(0f);
        image_right.setTranslationX(0f);
        text_left.setTranslationX(0f);
        text_right.setTranslationX(0f);
    }

    public void resetALL() {

        logo_left.setTranslationX(0f);
        logo_right.setTranslationX(0f);
        image_left.setTranslationX(0f);
        image_right.setTranslationX(0f);
        text_left.setTranslationX(0f);
        text_right.setTranslationX(0f);
        layout_left.setTranslationY(0f);
        layout_right.setTranslationY(0f);
    }


    private boolean noRemind = false;

    /**
     * 显示说明书
     *
     * @param bitmap
     */
    public void updateSurface(Bitmap bitmap) {
        noRemind = true;
        layout_left.setVisibility(GONE);
        layout_right.setVisibility(GONE);
//        image_layout_left.setVisibility(GONE);
//        image_layout_right.setVisibility(GONE);
        mActivity.yuvGLSurfaceView.setPhotoView(bitmap);
    }

    public void exitPhotoSurface() {
        mActivity.yuvGLSurfaceView.quitPhotoView();
    }

    /**
     * 显示黑色屏幕
     */
    public void showBlack() {
        popupThreeLayout.setBackgroundColor(Color.BLACK);
        layout_left.setVisibility(VISIBLE);
        layout_right.setVisibility(VISIBLE);
        text_left.setText("无照片");
        text_right.setText("无照片");
    }

    /**
     * 提示图片删除
     */
    public void showDeleteAction() {
        layout_left.setVisibility(VISIBLE);
        layout_right.setVisibility(VISIBLE);
        updateDisplay(baseItem.getLogoImage(), baseItem.getDisplayText(), baseItem.getDisplayImages());
    }

    public void dismissOSD() {
        layout_left.setVisibility(GONE);
        layout_right.setVisibility(GONE);
    }

    public void showPrompt(String text, int logo) {
        Log.d("getDisplayText", "showPrompt: " + text);
        handler.removeCallbacksAndMessages(null);
        layout_left.setVisibility(VISIBLE);
        layout_right.setVisibility(VISIBLE);
        updateDisplay(logo, text, -1);
        handler.sendEmptyMessageDelayed(0, 1000);
    }


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    dismissOSD();
                    break;
            }
        }
    };
}
