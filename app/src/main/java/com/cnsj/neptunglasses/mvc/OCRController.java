package com.cnsj.neptunglasses.mvc;


import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.GeneralResult;
import com.baidu.ocr.sdk.model.WordSimple;
import com.cnsj.neptunglasses.constant.What;
import com.cnsj.neptunglasses.utils.ThreadManager;
import com.cnsj.sightaid.model.OCRMode;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * OCR识别
 */
public class OCRController implements Controller, ImageConfiguration, Callback {


    private OCRRunnable ocrRunnable;

    public OCRController() {
        ocrRunnable = new OCRRunnable();
    }

    private Handler handler;
    private int width, height;
    private byte[] yuv;
    private float scale;
    private ByteBuffer frame;

    @Override
    public void execute() {
        ThreadManager.getInstance().execute(ocrRunnable);
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

    private int type;

    /**
     * 获取图片格式
     *
     * @param type
     */
    public void type(int type) {
        this.type = type;
    }

    class OCRRunnable implements Runnable {

        @Override
        public void run() {
            if (frame == null) return;
//            Bitmap bitmap = YuvUtils.convertToBitmap(yuv, width, height);
            Bitmap bitmap;
            if (type == What.ARGB8888) {
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            } else {
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            }
//            ByteBuffer bufffer = ByteBuffer.wrap(yuv);
//            //bufffer.position(0);
//            bufffer.rewind();
//            bufffer.position(0);
            frame.rewind();
            bitmap.copyPixelsFromBuffer(frame);
            Matrix m = new Matrix();
            if (scale >= 2) {//图像需要旋转
                m.setScale(-1, -1);
            }
            int offsetx = 0, offsety = 0;
            if (scale == 1.0f) {
                m.preScale(0.5f, 0.5f);
            } else if (scale == 1.5f) {
                m.preScale(0.5f, 0.5f);
                offsetx = width / 6;
                offsety = height / 6;
                width = width * 2 / 3;
                height = height * 2 / 3;
            } else if (scale == 2f) {
                offsetx = width / 4;
                offsety = height / 4;
                width = width / 2;
                height = height / 2;
            } else if (scale == 3f) {
                offsetx = width / 3;
                offsety = height / 3;
                width = width / 3;
                height = height / 3;
            } else if (scale == 5f) {
                offsetx = width * 2 / 5;
                offsety = height * 2 / 5;
                width = width / 5;
                height = height / 5;
            } else if (scale == 10f) {
                offsetx = width * 9 / 20;
                offsety = height * 9 / 20;
                width = width / 10;
                height = height / 10;
            } else if (scale == 20f) {
                offsetx = width * 19 / 40;
                offsety = height * 19 / 40;
                width = width / 20;
                height = height / 20;
            } else if (scale == 25f) {
                offsetx = width * 12 / 25;
                offsety = height * 12 / 25;
                width = width / 25;
                height = height / 25;
            }
            bitmap = Bitmap.createBitmap(bitmap, offsetx, offsety, width, height, m, true);
            OCRMode.INSTANCE.start(bitmap, 1.0f, new OnResultListener<GeneralResult>() {
                @Override
                public void onResult(GeneralResult generalResult) {
                    OCRMode.INSTANCE.cancel();
                    Log.d("MainActivity", "onResult: ocrocr 解析成功");
                    List<WordSimple> wordSimples = (List<WordSimple>) generalResult.getWordList();
                    if (wordSimples == null) {
                        Log.d("MainActivity", "onResult: ocrocr wordSimples为空");
                        Message message = Message.obtain();
                        message.what = What.OCR_FAULIRE;
                        message.obj = "未识别到文字";
                        handler.sendMessage(message);
                        return;
                    }
                    StringBuffer sb = new StringBuffer();
                    for (WordSimple wordSimple : wordSimples) {
                        Log.d("MainActivity", "onResult: " + wordSimple);
                        sb.append(wordSimple.getWords());
                    }
                    if (sb.toString().trim().equals("") || TextUtils.isEmpty(sb.toString())) {
//                        CuiNiaoApp.textSpeechManager.speakNow("未识别到文字", Constant.OCR_LEVEL);
//                        showPrompt("未识别到文字", R.mipmap.ocr);
                        Message message = Message.obtain();
                        message.what = What.OCR_FAULIRE;
                        message.obj = "未识别到文字";
                        handler.sendMessage(message);
                    } else {
                        Message message = Message.obtain();
                        message.what = What.OCR_SUCCESS;
                        message.obj = sb.toString();
                        handler.sendMessage(message);
//                ocrLeft.setVisibility(View.VISIBLE);
//                ocrRight.setVisibility(View.VISIBLE);
//                String content = sb.toString();//.replaceAll("[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……& amp;*（）——+|{}【】‘；：”“’。，、？|-]", "");
//                textLeft.setScrollText(content, VRApp.textSpeechManager.getRates());
//                textRight.setScrollText(content, VRApp.textSpeechManager.getRates());
//                textLeft.setOnSubtitleEndListener(MainActivity.this::subtitleEnd);
//                isReading = true;
                    }

                }

                @Override
                public void onError(OCRError ocrError) {
                    Log.d("MainActivity", "onError: ocrocr 解析错误" + ocrError.getMessage());
                    OCRMode.INSTANCE.cancel();
//                    CuiNiaoApp.textSpeechManager.speakNow("识别失败", Constant.OCR_LEVEL);
//                    showPrompt("识别失败", R.mipmap.ocr);
                    Message message = Message.obtain();
                    message.what = What.OCR_FAULIRE;
                    message.obj = "识别失败";
                    handler.sendMessage(message);
                }
            });
        }
    }
}
