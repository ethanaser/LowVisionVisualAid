package com.cnsj.neptunglasses.mvc;

import java.nio.ByteBuffer;

/**
 * 图像配置参数
 */
public interface ImageConfiguration {
    /**
     * 配置图像尺寸
     *
     * @param width
     * @param height
     */
    void size(int width, int height);

    /**
     * 配置数据源
     *
     * @param yuv
     */
    void data(byte[] yuv);

    /**
     * 配置数据源
     *
     * @param frame
     */
    void buffer(ByteBuffer frame);

    /**
     * 放大参数
     *
     * @param scale
     */
    void paramsScale(float scale);
}
