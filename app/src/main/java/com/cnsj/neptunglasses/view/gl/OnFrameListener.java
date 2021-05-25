package com.cnsj.neptunglasses.view.gl;

import java.nio.ByteBuffer;

/**
 * 获取相机的Yuv数据
 */
public interface OnFrameListener {

    void onYuvFrame(byte[] yuv,float scale);
    void onRGBFrame(byte[] rgb,float scale);
    void onByteBufferFrame(ByteBuffer frame,int width,int height,int type, float scale);
}
