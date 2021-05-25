package com.cnsj.neptunglasses.mvc;


import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Environment;
import android.os.Handler;

import com.cnsj.neptunglasses.constant.What;
import com.cnsj.neptunglasses.utils.CommonFileUtils;
import com.cnsj.neptunglasses.utils.ThreadManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 保存图片
 */
public class BitmapController implements Controller, ImageConfiguration, Callback {

    private SaveBitmapRunnable saveBitmapRunnable;

    public BitmapController() {
        saveBitmapRunnable = new SaveBitmapRunnable();
    }

    private Handler handler;
    private int width, height;
    private byte[] yuv;
    private float scale;
    private ByteBuffer frame;

    @Override
    public void execute() {
        ThreadManager.getInstance().execute(saveBitmapRunnable);
    }

    @Override
    public void setCallback(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void size(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void data(byte[] yuv) {
        this.yuv = yuv;
    }

    @Override
    public void buffer(ByteBuffer frame) {
        this.frame = frame;
    }

    @Override
    public void paramsScale(float scale) {
        this.scale = scale;
    }

    class SaveBitmapRunnable implements Runnable {

        @Override
        public void run() {
            if (frame == null) return;
//            Bitmap bitmap = YuvUtils.convertToBitmap(yuv, width, height);
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
//            ByteBuffer bufffer = ByteBuffer.wrap(yuv);
//            //bufffer.position(0);
//            bufffer.rewind();
//            bufffer.position(0);
            bitmap.copyPixelsFromBuffer(frame);
            if (scale >= 2) {//图像需要旋转
                Matrix m = new Matrix();
                m.setScale(-1, -1);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, m, false);
            }
            saveBitmapByGL(bitmap);
        }

        public SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

        /**
         * 根据时间戳保存图片
         *
         * @param bitmap
         */
        private void saveBitmapByGL(Bitmap bitmap) {
            StringBuffer sb = new StringBuffer();
            sb.append(Environment.getExternalStorageDirectory().getAbsolutePath());
            sb.append("/");
            sb.append("DCIM/freeze_image");//目前将定格图片保存在这个目录中。
            File dir = new File(sb.toString());
            if (!dir.exists()) {
                dir.mkdir();
            }
            int size = CommonFileUtils.Companion.getFreezeImageSize(sb.toString());
            if (size >= 100) {
                handler.sendEmptyMessage(What.SAVE_BITMAP_FAULIRE);
                return;
            }
            sb.append("/");
            sb.append("cnsj-");
            sb.append(simpleDateFormat.format(new Date()));
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
                handler.sendEmptyMessage(What.SAVE_BITMAP_SUCCESS);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
