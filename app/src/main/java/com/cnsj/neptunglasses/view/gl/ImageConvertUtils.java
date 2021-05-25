package com.cnsj.neptunglasses.view.gl;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 格式转换工具类
 */
public class ImageConvertUtils {


    /**
     * ARGB8888 TO RGB565
     *
     * @return
     */
    public static Bitmap convertARGB888Torgb565(Bitmap bitmap) {
        if (bitmap == null)
            return null;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels8888 = new int[width * height];
        int[] pixels565 = new int[width * height];
        bitmap.getPixels(pixels8888, 0, width, 0, 0, width, height);
        int RGB888_RED = 0x00ff0000;
        int RGB888_GREEN = 0x0000ff00;
        int RGB888_BLUE = 0x000000ff;
        for (int i = 0; i < pixels8888.length; i++) {
            char red = (char) (pixels8888[i] >> 27);
            char green = (char) (pixels8888[i] >> 18);
            char blue = (char) (pixels8888[i] >> 11);
//            int red = (Color.red(pixels8888[i]) >> 3) & 0X1F;
//            int green = (Color.green(pixels8888[i]) >> 2) & 0X3F;
//            int blue = (Color.blue(pixels8888[i]) >> 3) >> 0X1F;
            pixels565[i] = (red << 11) + (green << 5) + (blue << 0);
        }
        bitmap.recycle();
        Bitmap newBitmap = Bitmap.createBitmap(pixels565, width, height, Bitmap.Config.RGB_565);
        return newBitmap;
    }


    /*
     * 获取位图的YUV数据
     */
    public static byte[] getYUVByBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int size = width * height;
        int pixels[] = new int[size];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        // byte[] data = convertColorToByte(pixels);
        byte[] data = rgb2YCbCr420(pixels, width, height);
        return data;
    }

    /*
     * 获取位图的RGB数据
     */
    public static byte[] getRGBByBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int size = width * height;
        int pixels[] = new int[size];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        byte[] data = convertColorToByte(pixels);
        return data;
    }

    /*
     * 像素数组转化为RGB数组
     */
    public static byte[] convertColorToByte(int color[]) {
        if (color == null) {
            return null;
        }
        byte[] data = new byte[color.length * 3];
        for (int i = 0; i < color.length; i++) {
            data[i * 3] = (byte) (color[i] >> 16 & 0xff);
            data[i * 3 + 1] = (byte) (color[i] >> 8 & 0xff);
            data[i * 3 + 2] = (byte) (color[i] & 0xff);
        }
        return data;

    }

    public static byte[] rgb2YCbCr420(int[] pixels, int width, int height) {
        int len = width * height;
        // yuv格式数组大小，y亮度占len长度，u,v各占len/4长度。
        byte[] yuv = new byte[len * 3 / 2];
        int y, u, v;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                // 屏蔽ARGB的透明度值
                int rgb = pixels[i * width + j] & 0x00FFFFFF;
                // 像素的颜色顺序为bgr，移位运算。
                int r = rgb & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = (rgb >> 16) & 0xFF;
                // 套用公式
                y = ((66 * r + 129 * g + 25 * b + 128) >> 8) + 16;
                u = ((-38 * r - 74 * g + 112 * b + 128) >> 8) + 128;
                v = ((112 * r - 94 * g - 18 * b + 128) >> 8) + 128;
                // rgb2yuv
                // y = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                // u = (int) (-0.147 * r - 0.289 * g + 0.437 * b);
                // v = (int) (0.615 * r - 0.515 * g - 0.1 * b);
                // RGB转换YCbCr
                // y = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                // u = (int) (-0.1687 * r - 0.3313 * g + 0.5 * b + 128);
                // if (u > 255)
                // u = 255;
                // v = (int) (0.5 * r - 0.4187 * g - 0.0813 * b + 128);
                // if (v > 255)
                // v = 255;
                // 调整
                y = y < 16 ? 16 : (y > 255 ? 255 : y);
                u = u < 0 ? 0 : (u > 255 ? 255 : u);
                v = v < 0 ? 0 : (v > 255 ? 255 : v);
                // 赋值
                yuv[i * width + j] = (byte) y;
                yuv[len + (i >> 1) * width + (j & ~1) + 0] = (byte) u;
                yuv[len + +(i >> 1) * width + (j & ~1) + 1] = (byte) v;
            }
        }
        return yuv;
    }


    public static void decodeYUV420SP(byte[] rgbBuf, byte[] yuv420sp,
                                      int width, int height) {
        final int frameSize = width * height;
        if (rgbBuf == null)

            throw new NullPointerException("buffer 'rgbBuf' is null");
        if (rgbBuf.length < frameSize * 3)

            throw new IllegalArgumentException("buffer 'rgbBuf' size "
                    + rgbBuf.length + " < minimum " + frameSize * 3);

        if (yuv420sp == null)

            throw new NullPointerException("buffer 'yuv420sp' is null");

        if (yuv420sp.length < frameSize * 3 / 2)

            throw new IllegalArgumentException("buffer 'yuv420sp' size "
                    + yuv420sp.length + " < minimum " + frameSize * 3 / 2);
        int i = 0, y = 0;
        int uvp = 0, u = 0, v = 0;
        int y1192 = 0, r = 0, g = 0, b = 0;
        for (int j = 0, yp = 0; j < height; j++) {
            uvp = frameSize + (j >> 1) * width;
            u = 0;
            v = 0;
            for (i = 0; i < width; i++, yp++) {
                y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0)
                    y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }
                y1192 = 1192 * y;
                r = (y1192 + 1634 * v);
                g = (y1192 - 833 * v - 400 * u);
                b = (y1192 + 2066 * u);
                if (r < 0)
                    r = 0;
                else if (r > 262143)
                    r = 262143;
                if (g < 0)
                    g = 0;
                else if (g > 262143)
                    g = 262143;
                if (b < 0)
                    b = 0;
                else if (b > 262143)
                    b = 262143;
                rgbBuf[yp * 3] = (byte) (r >> 10);
                rgbBuf[yp * 3 + 1] = (byte) (g >> 10);
                rgbBuf[yp * 3 + 2] = (byte) (b >> 10);
            }
        }
    }


    /**
     * 将Yuv转换成Bitmap
     *
     * @param yuv
     * @return
     */
    public static Bitmap convertToBitmap(byte[] yuv, int width, int height) {
        Bitmap bmp = null;
        YuvImage image = new YuvImage(convertYuv420ToNv21(yuv, width, height), ImageFormat.NV21, width, height, null);
        if (image != null) {
            try {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                image.compressToJpeg(new Rect(0, 0, width, height), 100, stream);
                bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bmp;
    }


    /**
     * YUV 420转NV21后生成Bitmap
     *
     * @param yuv
     * @return
     */
    public static byte[] convertYuv420ToNv21(byte[] yuv, int width, int height) {
        byte[] nv21 = new byte[yuv.length];
        int ysize = width * height;
        int uvsize = width * height / 4;
        System.arraycopy(yuv, 0, nv21, 0, ysize);
        for (int i = 0; i < uvsize; i++) {
            byte u = yuv[ysize + i];
            byte v = yuv[ysize + uvsize + i];
            nv21[ysize + i * 2] = v;
            nv21[ysize + i * 2 + 1] = u;
        }
        return nv21;
    }
}
