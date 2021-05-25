package com.cnsj.neptunglasses.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapUtils {
    public static final String OCR_FILE_NAME = "/ocr_file_name.jpg";

    /**
     * @param
     * @return
     */
    public static boolean saveBitmap(Bitmap bmp) {
        //旋转
        Matrix matrix = new Matrix();
        matrix.postRotate(180);//旋转
        matrix.postScale(-1, 1);//镜像
        Bitmap bitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

        //等比剪裁
//        int w = bitmap.getWidth();
//        int h = bitmap.getHeight();
//        float cropWidth = w / scale;
//        float cropHeight = h / scale;
//        float startW = (w - cropWidth) / 2;
//        float startH = (h - cropHeight) / 2;
//        Bitmap bitmap1 = Bitmap.createBitmap(bitmap, (int) startW, (int) startH, (int) cropWidth, (int) cropHeight, null, false);

        File f = new File(Environment.getExternalStorageDirectory(), OCR_FILE_NAME);
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            //压缩
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
            out.flush();
            out.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    public static File saveBitmap(Bitmap bitmap, String fileName) {
        // 创建cnsj
        File appDir = new File(Environment.getExternalStorageDirectory(), "cnsj");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        File file = new File(appDir, fileName);
        if (file.exists()) {
            file.delete();
        }
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            // 格式为 JPEG，照相机拍出的图片为JPEG格式的，PNG格式的不能显示在相册中
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)) {
                out.flush();
                out.close();
            }
            return file;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static boolean saveBitmap(Context context, Bitmap bitmap, String bitName) {
        // 创建cnsj
        File appDir = new File(Environment.getExternalStorageDirectory(), "cnsj");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        File file = new File(appDir, bitName);
        if (file.exists()) {
            file.delete();
        }
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            // 格式为 JPEG，照相机拍出的图片为JPEG格式的，PNG格式的不能显示在相册中
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)) {
                out.flush();
                out.close();
                // 插入图库
                MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), bitName, null);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;

        }
        // 发送广播，通知刷新图库的显示
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(file.getAbsolutePath())));
        return true;
    }


    /**

     * @param bitmap      原图
     * @param edgeLength  希望得到的正方形部分的边长
     * @return  缩放截取正中部分后的位图。
     */
    public static Bitmap centerSquareScaleBitmap(Bitmap bitmap, int edgeLength)
    {
        if(null == bitmap || edgeLength <= 0)
        {
            return  null;
        }

        int widthOrg = bitmap.getWidth();
        int heightOrg = bitmap.getHeight();

        int x = (widthOrg-edgeLength)/2;
        int y = (heightOrg-edgeLength)/2;
        Bitmap result = Bitmap.createBitmap(bitmap, x, y, 100, 100, null, false);
        bitmap.recycle();
        return result;
    }

}
