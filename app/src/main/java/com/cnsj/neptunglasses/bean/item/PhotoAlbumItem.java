package com.cnsj.neptunglasses.bean.item;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;

import com.cnsj.neptunglasses.R;
import com.cnsj.neptunglasses.app.CuiNiaoApp;
import com.cnsj.neptunglasses.bean.BaseItem;
import com.cnsj.neptunglasses.bean.MenuEyent;
import com.cnsj.neptunglasses.constant.What;
import com.cnsj.neptunglasses.utils.CommonFileUtils;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.List;


/**
 * Created by Zph on 2020/9/11.
 */
public class PhotoAlbumItem extends BaseItem {

    public PhotoAlbumItem(Activity activity) {
        super(activity);
    }

    @Override
    public int getLogoImage() {
        return CuiNiaoApp.isYellowMode ? R.mipmap.photo_album_b_y : R.mipmap.photo_album_b;
    }

    private String displayText;

    @Override
    public String getDisplayText() {
        Log.d("getDisplayText", "getDisplayText: " + displayText);
        return displayText;
    }

    @Override
    public int getBigLogo() {
        return CuiNiaoApp.isYellowMode ? R.mipmap.photo_album_b_y : R.mipmap.photo_album_b;
    }

    @Override
    public int getSmallLogo() {
        return CuiNiaoApp.isYellowMode ? R.mipmap.photo_album_s_y : R.mipmap.photo_album_s;
    }

    BitmapFactory.Options options;

    @Override
    public void init() {
        menuEyent = new MenuEyent();
        menuEyent.setItemName("相册");
        menuEyent.setItemCount(">");
        options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
    }

    @Override
    public void load() {
        hasPressEnter = false;
        canDelete = false;
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


    private List<String> photos;
    private Bitmap bitmap;
    private int photoIndex;
    private boolean hasPressEnter;
    private boolean canDelete;
    private float x;
    private float y;

    @Override
    public void onKeyDown(int keyCode) {
        if (this.menuPopup == null) return;
        switch (keyCode) {
//            case -1:
//                hasPressEnter = false;
//                activity.setFirstPosition(3);
//                activity.setSecondPosition(-1);
//                activity.setThirdPosition(-1);
//                activity.remove(this.menuPopup);
//                this.menuPopup.dismiss();
//                activity.setMenuPopup(null, null);
//                finish();
//                activity.startUnityPhoto();
//                break;
            case -1:

                //退出定格
                if (activity.freezemode == 1) {
                    activity.freezemode = 0;
                    activity.yuvGLSurfaceView.quitFreezeMode();
                    activity.handler.sendEmptyMessage(What.PROMPT_GONE);
                }
                //        //退出中心放大
                if (activity.centerScaleTag == 1) {
                    activity.centerScaleTag = 0;
                    activity.yuvGLSurfaceView.setCenterScaleTag(0);
                }
//        //退出快速放大
                if (activity.fastScaleTag == 1) {
                    activity.fastScaleTag = 0;
                    activity.yuvGLSurfaceView.quitFastScale();
                }
                hasPressEnter = false;
                canDelete = false;
                StringBuffer sb = new StringBuffer();
                sb.append(Environment.getExternalStorageDirectory().getAbsolutePath());
                sb.append("/");
                sb.append("DCIM/freeze_image");
                long mm = System.currentTimeMillis();
                photos = CommonFileUtils.Companion.getFreezeImageList(sb.toString());
                Log.d("photo", "onKeyEvent: " + (System.currentTimeMillis() - mm));
                sb.setLength(0);
                sb = null;
                photoIndex = 0;
                Log.d("photo", "onKeyEvent: " + photos.size());
                if (photos != null && photos.size() > 0) {
                    Log.d("photo", "onKeyDown: " + photos.get(photoIndex));
                    bitmap = BitmapFactory.decodeFile(photos.get(photoIndex), options);
                    Log.d("photo", "onKeyDown: " + (bitmap == null));
//                    Matrix m = new Matrix();
//                    m.setScale(-1, -1);
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), null, true);
//                    activity.mUVCCameraView.setPhotoView(bitmap);
                    activity.yuvGLSurfaceView.setPhotoView(bitmap);
                    displayText = (photoIndex + 1) + "/" + photos.size();
                    this.menuPopup.showPrompt(getDisplayText(), getLogoImage());
                    CuiNiaoApp.textSpeechManager.speakNow("第" + (photoIndex + 1) + "张");
                } else {
                    displayText = "无照片";
                    this.menuPopup.showPrompt("无照片", getLogoImage());
                    this.menuPopup.showBlack();
                    CuiNiaoApp.textSpeechManager.speakNow("无照片");
                }
                break;
            case KeyEvent.KEYCODE_D:
                if (hasPressEnter && canDelete) return;
                if (photos == null || photos.size() <= 0) return;
                photoIndex--;
                if (photoIndex < 0) {
                    photoIndex = photos.size() - 1;
                }
                Log.d("photo", "onKeyEvent: " + photoIndex);
                bitmap = BitmapFactory.decodeFile(photos.get(photoIndex), options);
//                Matrix m = new Matrix();
//                m.setScale(-1, -1);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), null, true);
//                activity.mUVCCameraView.setPhotoView(bitmap);
                activity.yuvGLSurfaceView.setPhotoView(bitmap);
                displayText = (photoIndex + 1) + "/" + photos.size();
                this.menuPopup.showPrompt(getDisplayText(), getLogoImage());
                CuiNiaoApp.textSpeechManager.speakNow("第" + (photoIndex + 1) + "张");

                break;
            case KeyEvent.KEYCODE_E:
                if (hasPressEnter && canDelete) return;
                if (photos == null || photos.size() <= 0) return;
                photoIndex++;
                if (photoIndex >= photos.size()) {
                    photoIndex = 0;
                }
                Log.d("photo", "onKeyEvent: " + photoIndex);
                bitmap = BitmapFactory.decodeFile(photos.get(photoIndex), options);
//                m = new Matrix();
//                m.setScale(-1, -1);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), null, true);
//                activity.mUVCCameraView.setPhotoView(bitmap);
                activity.yuvGLSurfaceView.setPhotoView(bitmap);
                displayText = (photoIndex + 1) + "/" + photos.size();
                this.menuPopup.showPrompt(getDisplayText(), getLogoImage());
                CuiNiaoApp.textSpeechManager.speakNow("第" + (photoIndex + 1) + "张");
                break;
            case KeyEvent.KEYCODE_A:
                break;
            case KeyEvent.KEYCODE_B://++
                if (activity.mUserMode >= 5) {
                    activity.mUserMode = -1;
                }
                activity.mUserMode++;
                activity.setCameraModel(activity.mUserMode, true);
                break;
            case KeyEvent.KEYCODE_C://--
                activity.resetCenterPosition();
                CuiNiaoApp.textSpeechManager.speakNow("图像归正");
                break;
            case KeyEvent.KEYCODE_F://确认键删除图片
                if (photos.size() <= 0) {
                    displayText = "无照片";
                    CuiNiaoApp.textSpeechManager.speakNow(displayText);
                    this.menuPopup.showPrompt(getDisplayText(), getLogoImage());
                    return;
                }
                canDelete = true;
                if (hasPressEnter) {
                    canDelete = false;
                    //delete
                    hasPressEnter = false;
                }
                break;
            case KeyEvent.KEYCODE_H://返回键取消删除
                if (hasPressEnter) {
                    hasPressEnter = false;
                    this.menuPopup.dismissOSD();
                    displayText = "取消删除";
                    CuiNiaoApp.textSpeechManager.speakNow(displayText);
                    this.menuPopup.showPrompt(getDisplayText(), getLogoImage());
                } else {
                    this.menuPopup.dismiss();
                    CuiNiaoApp.textSpeechManager.speakNow("退出" + getMenuName());
                    activity.remove(this.menuPopup);
                    finish();
                }
                break;
            case KeyEvent.KEYCODE_I:
                this.menuPopup.dismiss();
                CuiNiaoApp.textSpeechManager.speakNow("退出" + getMenuName());
                activity.remove(this.menuPopup);
                finish();
                break;
        }
    }

    @Override
    public void onKeyDown(int keyCode, KeyEvent event) {
        super.onKeyDown(keyCode, event);
        switch (keyCode) {
            case KeyEvent.KEYCODE_ENTER:
                activity.onKeyDown(keyCode, event);
                Bitmap bitmap = BitmapFactory.decodeFile(photos.get(photoIndex), options);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), null, true);
                ByteBuffer byteBuffer = ByteBuffer.allocate(bitmap.getByteCount());
                bitmap.copyPixelsToBuffer(byteBuffer);
                activity.onFrameListener.onByteBufferFrame(byteBuffer, bitmap.getWidth(), bitmap.getHeight(), What.ARGB8888, activity.getScale());
                break;
        }
    }

    public void onKeyUp(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_F:
                if (photos.size() <= 0) return;
                if (!hasPressEnter && canDelete) {
                    hasPressEnter = true;
                    displayText = "确认删除/返回撤销";
                    CuiNiaoApp.textSpeechManager.speakNow("按确认删除,按返回取消");
                    this.menuPopup.showDeleteAction();
                }
                if (!hasPressEnter && !canDelete) {
                    this.menuPopup.dismissOSD();
                    String path = photos.get(photoIndex);
                    photos.remove(photoIndex);
                    new File(path).delete();
                    if (photos.size() <= 0) {
                        this.menuPopup.showBlack();
                        //没有照片了
                        displayText = "无照片";
                        CuiNiaoApp.textSpeechManager.speakNow(displayText);
                        this.menuPopup.showPrompt(getDisplayText(), getLogoImage());
                        return;
                    }
                    if (photoIndex == photos.size() && photos.size() != 0) {
                        photoIndex -= 1;
                    }
                    bitmap = BitmapFactory.decodeFile(photos.get(photoIndex), options);
//                    Matrix m = new Matrix();
//                    m.setScale(-1, -1);
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), null, true);
//                    activity.mUVCCameraView.setPhotoView(bitmap);
                    activity.yuvGLSurfaceView.setPhotoView(bitmap);
                    displayText = "删除图片";
                    CuiNiaoApp.textSpeechManager.speakNow(displayText);
                    this.menuPopup.showPrompt(getDisplayText(), getLogoImage());
                }
                break;
        }
    }

    @Override
    public void speak() {
        if (this.menuPopup == null)
            CuiNiaoApp.textSpeechManager.speakNow("相册");
    }

    @Override
    public void update() {

    }

    @Override
    public void finish() {
        this.menuPopup = null;
        hasPressEnter = false;
        canDelete = false;
        activity.yuvGLSurfaceView.quitPhotoView();
    }

    @Override
    public List<BaseItem> getNextMenu() {
        return null;
    }

    @Override
    public int getCurrentMenuLevel() {
        return 1;
    }
}
