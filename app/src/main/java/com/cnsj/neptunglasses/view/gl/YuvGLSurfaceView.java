package com.cnsj.neptunglasses.view.gl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Size;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class YuvGLSurfaceView extends GLSurfaceView implements CameraInterface, CustomSettingsInterface, SpecialSettingsInterface {

    public YuvGLSurfaceView(Context context) {
        this(context, null);
    }

    public YuvGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private Context context;
//    YuvRenderer renderer;
    RGBRenderer renderer;
//    private int width = 1920, height = 1080;

    private void init(Context context) {
        this.context = context;
//        renderer = new YuvRenderer(this.context, this);
        renderer = new RGBRenderer(this.context, this);
        setEGLContextClientVersion(2);
        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }


    @Override
    public void onResume() {
        super.onResume();
        if (renderer != null) {
            renderer.updateImage(null, null, null);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (renderer != null) {
            renderer.updateImage(null, null, null);
        }
    }

    /**
     * 重置所有参数
     */
    public void resetAll() {
        resetIpd();
        resetCustomSettings();
        resetEyesSettings();
    }

    /**
     * 获取IMU数据
     *
     * @param quats
     */
    public void onQuaternion(float[] quats) {
        if (renderer != null)
            renderer.onQuaternion(quats);
    }


    @Override
    public void setFreezeMode() {
        if (renderer != null)
            renderer.setFreezeMode();
    }

    @Override
    public void quitFreezeMode() {
        if (renderer != null)
            renderer.quitFreezeMode();
    }

    @Override
    public void setScale(float scale) {
        if (renderer != null)
            renderer.setScale(scale);
    }

    @Override
    public float getScale() {
        if (renderer != null)
            return renderer.getScale();
        return 1.0f;
    }

    /**
     * 设置颜色模式
     *
     * @param userMode
     */
    @Override
    public void setUserMode(int userMode) {
        if (renderer != null)
            renderer.setUserMode(userMode);
    }

    @Override
    public int getUserMode() {
        if (renderer != null)
            return renderer.getUserMode();
        return 0;
    }

    /**
     * 设置两色模式的 颜色显示
     *
     * @param colorValue
     */
    public void setColorValue(float[] colorValue) {
        if (renderer != null)
            renderer.setColorValue(colorValue);
    }

    /**
     * 设置描边模式的颜色显示
     *
     * @param edgeValue
     */
    public void setEdgeValue(float[] edgeValue) {
        if (renderer != null)
            renderer.setEdgeValue(edgeValue);
    }


    @Override
    public int getSaturation() {
        if (renderer != null)
            return renderer.getSaturation();
        return 1;
    }

    /**
     * 饱和度范围1-5  1 1.4 1.8 2.2 2.6
     *
     * @param saturation
     */
    @Override
    public void setSaturation(int saturation) {
        if (renderer != null)
            renderer.setSaturation(saturation);
    }

    @Override
    public int getContrast() {
        if (renderer != null)
            return renderer.getContrast();
        return 1;
    }

    /**
     * 对比度范围1-5 0 0.2 0.4 0.6 0.8
     *
     * @param contrast
     */
    @Override
    public void setContrast(int contrast) {
        if (renderer != null)
            renderer.setContrast(contrast);
    }

    @Override
    public int getBrightness() {
        if (renderer != null)
            return renderer.getBrightness();
        return 1;
    }

    /**
     * 亮度范围1-5 0 0.2 0.4 0.6 0.8
     *
     * @param brightness
     */
    @Override
    public void setBrightness(int brightness) {
        if (renderer != null)
            renderer.setBrightness(brightness);
    }

    @Override
    public void setCenterScaleTag(int i) {
        if (renderer != null)
            renderer.setCenterScaleTag(i);
    }

    @Override
    public int getCenterScaleTag() {
        if (renderer != null)
            return renderer.getCenterScaleTag();
        return 0;
    }

    @Override
    public void quitFastScale() {
        if (renderer != null)
            renderer.setFastScaleTag(0);
    }

    @Override
    public int getFastScaleTag() {
        if (renderer != null)
            return renderer.getFastScaleTag();
        return 0;
    }

    @Override
    public void setFastScaleTag() {
        if (renderer != null)
            renderer.setFastScaleTag(1);
    }

    @Override
    public void setStabOn(boolean isOpen) {
    }

    @Override
    public void setPhotoView(Bitmap bitmap) {
        if (renderer != null)
            renderer.setPhotoView(bitmap);
    }

    @Override
    public void quitPhotoView() {
        if (renderer != null)
            renderer.quitPhotoView();
    }

    @Override
    public void resetCenterPosition() {
        if (renderer != null)
            renderer.resetCenterPosition();
    }

    @Override
    public void resetCustomSettings() {
        if (renderer != null)
            renderer.resetCustomSettings();
    }

    @Override
    public void setDoubleEyeScale(float scale) {
        if (renderer != null)
            renderer.setDoubleEyeScale(scale);
    }

    @Override
    public float getDoubleEyeScale() {
        if (renderer != null)
            return renderer.getDoubleEyeScale();
        return 1.0f;
    }

    @Override
    public void setIpd(int distance) {
        if (renderer != null)
            renderer.setIpd(distance);
    }

    @Override
    public void setLeftSubScale(float scale) {
        if (renderer != null)
            renderer.setLeftSubScale(scale);
    }

    @Override
    public float getLeftScale() {
        if (renderer != null)
            return renderer.getLeftScale();
        return 1.0f;
    }

    @Override
    public void setRightSubScale(float scale) {
        if (renderer != null)
            renderer.setRightSubScale(scale);
    }

    @Override
    public float getRightScale() {
        if (renderer != null)
            return renderer.getRightScale();
        return 1.0f;
    }

    @Override
    public void setLeftEyeOffset(int x, int y) {
        if (renderer != null)
            renderer.setLeftEyeOffset(x, y);
    }

    @Override
    public String getLeftEyeOffset() {
        if (renderer != null)
            return renderer.getLeftEyeOffset();
        return "0,0";
    }

    @Override
    public void setRightEyeOffset(int x, int y) {
        if (renderer != null)
            renderer.setRightEyeOffset(x, y);
    }

    @Override
    public String getRightEyeOffset() {
        if (renderer != null)
            return renderer.getRightEyeOffset();
        return "0,0";
    }

    @Override
    public void resetIpd() {
        if (renderer != null)
            renderer.resetIpd();
    }

    @Override
    public void resetEyesSettings() {
        if (renderer != null)
            renderer.resetEyesSettings();
    }

    @Override
    public void setOnEyesChangeListener(OnEyesChangeListener onEyesChangeListener) {
        if (renderer != null)
            renderer.setOnEyesChangeListener(onEyesChangeListener);
    }


    @Override
    public SurfaceTexture getSurfaceTexture() {
        if (renderer != null)
            return renderer.getSurfaceTexture();
        return null;
    }


    @Override
    public void openCamera() {
    }

    @Override
    public void startPreview() {
        if (renderer != null)
            renderer.startPreview();
    }

    @Override
    public void stopPreview() {
        if (renderer != null)
            renderer.stopPreview();
    }

//    private int i;



    @Override
    public void onPreviewByteBuffer(ByteBuffer frame) {
        if (renderer != null)
            renderer.onPreviewByteBuffer(frame);
    }


    /**
     * 根据时间戳保存图片
     *
     * @param bitmap
     */
    private void saveBitmap(Bitmap bitmap) {
        StringBuffer sb = new StringBuffer();
        sb.append(Environment.getExternalStorageDirectory().getAbsolutePath());
        sb.append("/");
        sb.append("DCIM/freeze_image");//目前将定格图片保存在这个目录中。
        File dir = new File(sb.toString());
        if (!dir.exists()) {
            dir.mkdir();
        }
        sb.append("/");
        sb.append("cnsj11111");
        sb.append(".jpg");
        File file = new File(sb.toString());
        if (file.exists()) {
            file.delete();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            //文件输出流
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            sb.setLength(0);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void closeCamera() {

    }


    @Override
    public void release() {
        if (renderer != null) {
//            renderer.onPreviewFrame(null);
//            renderer.onPreviewFrame1(null);
            renderer.onPreviewByteBuffer(null);
//            renderer.onPreviewByteBuffer1(null);
            quitPhotoView();
            quitFreezeMode();
            quitFastScale();
            setCenterScaleTag(0);
            setScale(1.0f);
        }
    }

    @Override
    public Size getDefaultSize() {
        if (renderer != null)
            return renderer.getDefaultSize();
        return null;
    }

    public void setOnYuvFrameListener(OnFrameListener onFrameListener) {
        if (renderer != null)
            renderer.setOnFrameListener(onFrameListener);
    }

    public void noDataTest() {
//        if (renderer!=null){
//            renderer.noDataTest();
//        }
    }
}
