package com.cnsj.neptunglasses.view.gl;

import android.graphics.SurfaceTexture;
import android.util.Size;

import java.nio.ByteBuffer;

/**
 * 相机相关操作接口
 */
public interface CameraInterface {
    /**
     * 返回底层SurfaceTexture用于显示图像数据
     *
     * @return
     */
    SurfaceTexture getSurfaceTexture();

    /**
     * 获取摄像头数据 同时接收两个摄像头的数据
     *
     * @param frame
     */

    void onPreviewByteBuffer(ByteBuffer frame);
    /**
     * 打开相机后操作
     */
    void openCamera();

    /**
     * 开启预览后操作
     */
    void startPreview();

    /**
     * 停止预览后操作
     */
    void stopPreview();

    /**
     * 关闭相机后操作
     */
    void closeCamera();

    /**
     * 释放相机后操作
     */
    void release();

    /**
     * 获取指定的预览的相机分辨率
     *
     * @return
     */
    Size getDefaultSize();
}
