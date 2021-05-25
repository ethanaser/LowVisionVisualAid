package com.cnsj.neptunglasses.bean.item;


import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.KeyEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.cnsj.neptunglasses.R;
import com.cnsj.neptunglasses.app.Constant;
import com.cnsj.neptunglasses.app.CuiNiaoApp;
import com.cnsj.neptunglasses.bean.BaseItem;
import com.cnsj.neptunglasses.bean.MenuEyent;
import com.cnsj.neptunglasses.constant.What;

/**
 * 说明书子布局
 */
public class ManualChildItem extends BaseItem {

    private String menuName;
    private int position;
    private List<String> imagePaths;
    private List<String> txtPaths;
    private AssetManager assetManager;

    BitmapFactory.Options options;

    public ManualChildItem(Activity activity, String menuName, int position) {
        super(activity);
        this.menuName = menuName;
        this.position = position;
        menuEyent = new MenuEyent();
        menuEyent.setItemName(this.menuName);
        menuEyent.setItemCount("");
        assetManager = activity.getAssets();
        imagePaths = new ArrayList<>();
        txtPaths = new ArrayList<>();
        try {
            String[] files = null;
            files = assetManager.list(Integer.toString(position));
            for (int i = 0; i < files.length; i++) {
                if (files[i].endsWith(".png") || files[i].endsWith(".jpg")) {
                    imagePaths.add(files[i]);
                } else {
                    txtPaths.add(files[i]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
    }


    @Override
    public int getLogoImage() {
        return 0;
    }

    @Override
    public void init() {

    }

    @Override
    public void load() {

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

    private Bitmap bitmap;
    private int index;
    private float x;
    private float y;

    @Override
    public void onKeyDown(int keyCode) {
        if (this.menuPopup == null) return;
        switch (keyCode) {
            case -1:
                if (activity.freezemode == 1) {
                    activity.freezemode = 0;
                    activity.yuvGLSurfaceView.quitFreezeMode();
                    activity.handler.sendEmptyMessage(What.PROMPT_GONE);
                }
                if (activity.centerScaleTag == 1) {
                    activity.centerScaleTag = 0;
                    activity.yuvGLSurfaceView.setCenterScaleTag(0);
                }
                if (activity.fastScaleTag == 1) {
                    activity.fastScaleTag = 0;
                    activity.yuvGLSurfaceView.quitFastScale();
                }
                index = 0;
                showManual(index);
                break;
            case KeyEvent.KEYCODE_E://++
                if (index >= imagePaths.size() - 1) {
                    return;
                }
                index++;
                showManual(index);
                speak(index);
                break;
            case KeyEvent.KEYCODE_D://--
                if (index <= 0) {
                    return;
                }
                index--;
                showManual(index);
                speak(index);
                break;
            case KeyEvent.KEYCODE_B://++

                break;
            case KeyEvent.KEYCODE_C://--
                activity.yuvGLSurfaceView.resetCenterPosition();
                CuiNiaoApp.textSpeechManager.speakNow("图像归正");
                break;
            case KeyEvent.KEYCODE_K://缩小

                break;
        }
    }

    /**
     * 显示说明书
     *
     * @param index
     */
    private void showManual(int index) {
        try {
            InputStream is = CuiNiaoApp.isYellowMode ? assetManager.open((position + 100) + "/" + imagePaths.get(index))
                    : assetManager.open(position + "/" + imagePaths.get(index));
            bitmap = BitmapFactory.decodeStream(is, null, options);
//            Matrix m = new Matrix();
//            m.setScale(1, -1);
//            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
            this.menuPopup.updateSurface(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取内容
     *
     * @param i
     */
    public void speak(int i) {
        String content = getFromAssets(position + "/" + txtPaths.get(i));
        if (content != null)
            CuiNiaoApp.textSpeechManager.speakNow(content, Constant.MANUAL_LEVEL);
    }

    @Override
    public void speak() {
        CuiNiaoApp.textSpeechManager.speakNow(getMenuName().substring(3));
    }

    @Override
    public void update() {
    }

    @Override
    public void finish() {
        if (this.menuPopup != null)
            this.menuPopup.exitPhotoSurface();
        this.menuPopup = null;
        CuiNiaoApp.textSpeechManager.shutDown(Constant.MANUAL_LEVEL);
        x = 0.0f;
        y = 0.0f;
    }

    @Override
    public List<BaseItem> getNextMenu() {
        return null;
    }

    @Override
    public int getCurrentMenuLevel() {
        return 2;
    }


    public String getFromAssets(String fileName) {
        try {
            InputStreamReader inputReader = new InputStreamReader(assetManager.open(fileName));
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line = "";
            String Result = "";
            while ((line = bufReader.readLine()) != null)
                Result += line;
            return Result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public boolean isImage() {
        return true;
    }

    @Override
    public int getItemCountImage(boolean isSelected) {
        if (isSelected) {
            return CuiNiaoApp.isYellowMode ? R.mipmap.right_y : R.mipmap.right;
        }
        return 0;
    }
}
