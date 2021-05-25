package com.serenegiant.usb.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.opengl.GLES20;
import android.opengl.GLES32;
import android.opengl.Matrix;
import android.util.DisplayMetrics;
import android.util.Log;

import com.jiangdg.usbcamera.utils.MathUtils;
import com.serenegiant.glutils.GLHelper;
import com.serenegiant.usb.common.OffsetUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glDrawArrays;

/**
 * 绘制单眼数据
 */
public class SingleEye {
    private Activity context;
    private int hProgram;
    private int contsize = 33;
    private FloatBuffer vertex;
    private FloatBuffer pTexCoord;
    private int width, height, xOffset;
    int maPositionLoc;
    int maTextureCoordLoc;
    int muMVPMatrixLoc;
    int mmUserMode, mmSaturation, mmContrast, mmBrightness, piexlSize, aryWeight, aryOffset, scaleFactor, offsetBase;
    int muTexMatrixLoc;
    int uQuatMatrix;
    private float scale = 1.0f;
    private final float[] mMvpMatrix;
    private boolean isDoubleEyes;
    private float xtValue = 2f, ytValue = 0.667f * 2;
    private float screenWidth, screenHeight;//屏幕宽度和屏幕高度，使用时屏幕宽度需要除以2
    private boolean isLeft;
    private float baseDoubleEyesScale = 1.0f;
    private float baseSingleEyeScale = 1.0f;
    private CenterScale centerScale;//中心放大
    private FastZoomScale fastZoomScale;

    /**
     * viewport的大小 ---->使用的是是像素值
     * Matrix.translateM translate ----->使用的是归一化坐标（-1，1）
     *
     * @param context
     * @param width
     * @param height
     */
    public SingleEye(Context context, int width, int height, int xOffset, boolean isDoubleEyes) {
        this.context = (Activity) context;
        this.width = width;
        this.height = height;
        this.xOffset = xOffset;
        this.isDoubleEyes = isDoubleEyes;
        this.isLeft = xOffset <= 0;
        mMvpMatrix = new float[16];
        Matrix.setIdentityM(this.mMvpMatrix, 0);
//        Matrix.translateM(this.mMvpMatrix, 0, 1f, 0f, 0.0f);
//        Matrix.scaleM(this.mMvpMatrix, 0, 2f, 2f, 1f);
        this.hProgram = GLHelper.loadShader(shadercode.OriginVSS, shadercode.OriginFSS1);
//        float[] texcoord = OffsetUtils.getTextures();
        float[] texcoord = TEXCOORD;
        this.pTexCoord = ByteBuffer.allocateDirect(texcoord.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.pTexCoord.put(texcoord);
        this.pTexCoord.flip();
        GLES20.glLinkProgram(this.hProgram);
        this.maPositionLoc = GLES20.glGetAttribLocation(this.hProgram, "aPosition");
        this.maTextureCoordLoc = GLES20.glGetAttribLocation(this.hProgram, "aTextureCoord");
        this.muMVPMatrixLoc = GLES20.glGetUniformLocation(this.hProgram, "uMVPMatrix");
//        this.muTexMatrixLoc = GLES20.glGetUniformLocation(this.hProgram, "uTexMatrix");
//        this.uQuatMatrix = GLES20.glGetUniformLocation(this.hProgram, "uQuatMatrix");
//        this.mmUserMode = GLES20.glGetUniformLocation(this.hProgram, "mUserMode");
//        this.mmSaturation = GLES20.glGetUniformLocation(this.hProgram, "mSaturation");
//        this.mmContrast = GLES20.glGetUniformLocation(this.hProgram, "mContrast");
//        this.mmBrightness = GLES20.glGetUniformLocation(this.hProgram, "mBrightness");
//        this.piexlSize = GLES20.glGetUniformLocation(this.hProgram, "piexlSize");
//        this.aryWeight = GLES20.glGetUniformLocation(this.hProgram, "aryWeight");
//        this.aryOffset = GLES20.glGetUniformLocation(this.hProgram, "aryOffset");
//        this.scaleFactor = GLES20.glGetUniformLocation(this.hProgram, "scaleFactor");
//        this.offsetBase = GLES20.glGetUniformLocation(this.hProgram, "offsetBase");
        setTextureScaleAndOffset();
        setVertices();
        DisplayMetrics dm = new DisplayMetrics();
        this.context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        Point point = new Point();
        this.context.getWindowManager().getDefaultDisplay().getRealSize(point);
        float x = point.x / dm.xdpi;//dm.xdpi是屏幕x方向的真实密度值，比上面的densityDpi真实，x方向屏幕英寸值。
        float y = point.y / dm.ydpi;//dm.xdpi是屏幕y方向的真实密度值，比上面的densityDpi真实，y方向屏幕英寸值。
        Log.d("TAG", "onCreatedpidpi: " + dm.xdpi + " " + dm.ydpi + " " + x + " " + y);
        float mm = 25.4f;//每英寸包含的毫米数
        screenWidth = MathUtils.floatMultiply(y, mm);
        screenHeight = MathUtils.floatMultiply(x, mm);
        centerScale = new CenterScale(this.hProgram, this.width, this.height, this.xOffset);
        fastZoomScale = new FastZoomScale(this.width, this.height, this.xOffset);
    }

    // Blur权重数组 通过修改该数组来变更过滤算法
    private float[] weights = {
            0f, -1f, 0f,
            -1f, 4f, -1f,
            0f, -1f, 0f
    };
    // 横向Blur偏移数组 固定值
    private float[] offsets = {
            -1f, -1f,
            0f, -1f,
            1f, -1f,
            -1f, 0f,
            0f, 0f,
            1f, 0f,
            -1f, 1f,
            0f, 1f,
            1f, 1f
    };

    public void draw(int texId, float[] tex_matrix, float[] quatMatrix) {
        GLES20.glViewport(xOffset, 0, width, height);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glUseProgram(hProgram);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GL_TEXTURE_2D, texId);
//        GLES20.glUniformMatrix4fv(this.uQuatMatrix, 1, false, quatMatrix, 0);
//        GLES20.glUniformMatrix4fv(this.muTexMatrixLoc, 1, false, tex_matrix, 0);
//        GLES20.glUniform1i(this.mmUserMode, mUserMode);
//        GLES20.glUniform1f(this.mmSaturation, saturationValue);
//        GLES20.glUniform1f(this.mmContrast, contrastValue);
//        GLES20.glUniform1f(this.mmBrightness, brightnessValue);
//        GLES20.glUniform2f(this.piexlSize, 2400, 1200);
//        GLES20.glUniform1fv(this.aryWeight, 9, weights, 0);//float
//        GLES20.glUniform2fv(this.aryOffset, 9, offsets, 0);//vec2
//        GLES20.glUniform1f(this.scaleFactor, 2.0f);//这个值用于调节检测到边缘线的深度
//        GLES20.glUniform1f(this.offsetBase, 512.0f);////这个值用于调节偏移颜色 不能为0
        GLES20.glUniformMatrix4fv(this.muMVPMatrixLoc, 1, false, mMvpMatrix, 0);
        GLES32.glEnableVertexAttribArray(maTextureCoordLoc);
        GLES20.glVertexAttribPointer(this.maTextureCoordLoc, 2, GL_FLOAT, false, 8, this.pTexCoord);
        GLES32.glEnableVertexAttribArray(maPositionLoc);
        GLES20.glVertexAttribPointer(this.maPositionLoc, 2, GL_FLOAT, false, 8, this.vertex);
//        for (int i = 0; i < contsize; i++) {
//            glDrawArrays(GLES20.GL_TRIANGLE_STRIP, i * contsize * 2, contsize * 2);
//        }
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        if (centerScaleTag == 1) {
            if (centerScale != null) {
                centerScale.draw(texId, quatMatrix, tex_matrix);
            }
        }
        if (fastScaleTag == 1) {
            if (fastZoomScale != null) {
                fastZoomScale.draw();
            }
        }
    }


    /**
     * 中心放大 0 关闭  1 开启
     */
    private int centerScaleTag = 0;

    public int getCenterScaleTag() {
        return centerScaleTag;
    }

    public void setCenterScaleTag(int centerScaleTag) {
        this.centerScaleTag = centerScaleTag;
        if (this.centerScaleTag == 1) {
            setScale(this.scale);
        }
    }


    /**
     * 快速放大 0，快速缩小 1
     */
    private int fastScaleTag = 0;
    private float fastScale = 1.0f;

    public int getFastScaleTag() {
        return fastScaleTag;
    }

    /**
     * 快速放大 0，快速缩小 1
     */
    public void setFastScaleTag(int fastScaleTag) {
        this.fastScaleTag = fastScaleTag;
        if (this.fastScaleTag == 1) {
            setScale(this.scale);
        } else {
            setScale(this.fastScale);
        }
    }


    //设置显示模式，
    //0 全彩 5 灰度 6 灰度反色 7,8,9,10 描边模式4种颜色 11 伪彩色 1 2 3 4 两色模式
    private int mUserMode = 0, singleX = 0, singleY = 0, currentIpd = 62;
    private float singleScale = 1.0f;
    private int saturation, contrast, brightness;//饱和度，对比度，亮度
    private float saturationValue = 1.0f, contrastValue = 0.0f, brightnessValue = 0.0f;//饱和度，对比度，亮度

    public int getSaturation() {
        return saturation;
    }

    /**
     * 饱和度范围1-5  1 1.4 1.8 2.2 2.6
     *
     * @param saturation
     */
    public void setSaturation(int saturation) {
        if (saturation <= 1) {
            saturation = 1;
        } else if (saturation >= 5) {
            saturation = 5;
        }
        this.saturation = saturation;
//        saturationValue = 1.0f + (this.saturation - 1) * 0.4f;
        saturationValue = MathUtils.floatAdd(1.0f, MathUtils.floatMultiply(0.4f, (this.saturation - 1)));
    }

    public int getContrast() {
        return contrast;
    }

    /**
     * 对比度范围1-5 0 0.2 0.4 0.6 0.8
     *
     * @param contrast
     */
    public void setContrast(int contrast) {
        if (contrast <= 1) {
            contrast = 1;
        } else if (contrast >= 5) {
            contrast = 5;
        }
        this.contrast = contrast;
        contrastValue = MathUtils.floatMultiply(0.2f, (this.contrast - 1));
    }

    public int getBrightness() {
        return brightness;
    }

    /**
     * 亮度范围1-5 0 0.2 0.4 0.6 0.8
     *
     * @param brightness
     */
    public void setBrightness(int brightness) {
        if (brightness <= 1) {
            brightness = 1;
        } else if (brightness >= 5) {
            brightness = 5;
        }
        this.brightness = brightness;
        brightnessValue = MathUtils.floatMultiply(0.2f, (this.brightness - 1));
    }

    public void setUserMode(int userMode) {
        mUserMode = userMode;
    }

    public int getMUserMode() {
        return mUserMode;
    }


    float[] ver1;//1 right 先画 2 left后画

    private static final float[] VERTICES = new float[]{
            1.0F, 0.667F,
            -1.0F, 0.667F,
            1.0F, -0.667F,
            -1.0F, -0.667F
    };
    private static final float[] TEXCOORD = new float[]{
            1.0F, 1.0F,
            0.0F, 1.0F,
            1.0F, 0.0F,
            0.0F, 0.0F
    };

    /**
     * 设定偏移和缩放
     */
    public void setTextureScaleAndOffset() {
        ver1 = new float[contsize * contsize * 4];
//        float[] result = OffsetUtils.getLeftOffsetVertex();
        float[] result = VERTICES;
        if (isDoubleEyes) {
            float baseScale = MathUtils.floatMultiply(this.scale, baseDoubleEyesScale);
            float singleScale = MathUtils.floatMultiply(baseScale, this.singleScale);
            for (int i = 0; i < result.length; i++) {
                ver1[i] = result[i] * singleScale;
            }
        } else {
            float baseScale = MathUtils.floatMultiply(this.scale, baseSingleEyeScale);
            float singleScale = MathUtils.floatMultiply(baseScale, this.singleScale);
            for (int i = 0; i < result.length; i++) {
                ver1[i] = result[i] * singleScale;
            }
        }

    }

    private void setVertices() {
        this.vertex = ByteBuffer.allocateDirect(ver1.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.vertex.put(ver1);
        this.vertex.position(0);
    }

    /**
     * 初始化单眼偏移缩放参数
     *
     * @param scale
     * @param singleScale
     * @param singleX
     * @param singleY
     * @param currentIpd
     */
    public void initParams(int scale, float singleScale, int singleX, int singleY, int currentIpd) {
        this.scale = scale;
        this.singleScale = singleScale;
        this.singleX = singleX;
        this.singleY = singleY;
        this.currentIpd = currentIpd;
        setScale(this.scale);
    }


    public void resetIpd() {
        setIpd(62);
    }

    public void resetAll() {
        currentIpd = 62;
        singleX = 0;
        singleY = 0;
        singleScale = 1.0f;
        setScale(this.scale);
    }

    public void setScale(float scale) {
        this.scale = scale;
        if (scale < 1) {
            this.scale = 1f;
        } else if (scale > 25) {
            this.scale = 25;
        }
        if (fastScaleTag == 1) {
            fastScale = scale;
            this.scale = 1.0f;
        }
        setSubScale(singleScale);
    }

    public float getScale() {
        if (fastScaleTag == 1) {
            return this.fastScale;
        }
        return this.scale;
    }

    /**
     * 设置单眼缩放
     *
     * @param eyeScale
     */
    public void setSubScale(float eyeScale) {
        this.singleScale = eyeScale;
        if (singleScale < 0.5f) {
            singleScale = 0.5f;
        } else if (singleScale >= 1.0f) {
            singleScale = 1.0f;
        }
        if (this.onEyesChangeListener != null && isLeft) {
            this.onEyesChangeListener.onLeftScaleChange(singleScale);
        } else if (this.onEyesChangeListener != null && !isLeft) {
            this.onEyesChangeListener.onRightScaleChange(singleScale);
        }
        setSingleEyeOffset(singleX, singleY);
    }


    public void setSingleEyeOffset(int singleX, int singleY) {
        this.singleX = singleX;
        this.singleY = singleY;
        if (this.singleX > 20) {
            this.singleX = 20;
        } else if (this.singleX < -20) {
            this.singleX = -20;
        }
        if (this.singleY > 20) {
            this.singleY = 20;
        } else if (this.singleY < -20) {
            this.singleY = -20;
        }
        setIpd(this.currentIpd);
    }

    public String getSingleEyeOffset() {
        return singleX + "," + singleY;
    }


    /**
     * 设置瞳距偏移
     *
     * @param ipd
     */
    public void setIpd(int ipd) {
        if (ipd > 75) {
            ipd = 75;
        } else if (ipd < 50) {
            ipd = 50;
        }
        currentIpd = ipd;
        int intOffset = currentIpd - 62;
        Matrix.setIdentityM(this.mMvpMatrix, 0);
        float baseScale = 1.0f;
        if (isDoubleEyes) {
            baseScale = MathUtils.floatMultiply(getScale(), baseDoubleEyesScale);
        } else {
            baseScale = MathUtils.floatMultiply(getScale(), baseSingleEyeScale);
        }
        float singleScale = 1.0f;
        float tempScale = 1.0f;
        if (fastScaleTag == 1) {
            if (isDoubleEyes) {
                tempScale = MathUtils.floatMultiply(1.0f, baseDoubleEyesScale);
            } else {
                tempScale = MathUtils.floatMultiply(1.0f, baseSingleEyeScale);
            }
            singleScale = MathUtils.floatMultiply(tempScale, this.singleScale);
        } else {
            singleScale = MathUtils.floatMultiply(baseScale, this.singleScale);
        }
        Log.d("TAG", "onIpdChange: 缩放比例：" + singleScale);
        if (getScale() >= 2) {
            Matrix.rotateM(this.mMvpMatrix, 0, 180.0f, 0.0f, 0.0f, 1.0f);
//            Matrix.translateM(this.mMvpMatrix, 0, 1.62f, 0.2f, 0.0f);
        }
        Matrix.rotateM(this.mMvpMatrix, 0, 180.0f, 0.0f, 1.0f, 0.0f);
        Matrix.scaleM(this.mMvpMatrix, 0, singleScale, singleScale, 1.0f);
        float translateIpd = MathUtils.floatDiv(intOffset, screenWidth, 5);
        Log.d("TAG", "onIpdChange: 基础尺寸：" + screenWidth);
        Log.d("TAG", "onIpdChange: 基础尺寸YYYY：" + screenHeight);
        Log.d("TAG", "onIpdChange: 瞳距平移：" + translateIpd);
        float translateX = MathUtils.floatDiv(singleX, screenWidth, 5);
        float translateY = MathUtils.floatDiv(singleY, screenHeight, 5);
        Log.d("TAG", "onIpdChange: X平移：" + translateX);
        Log.d("TAG", "onIpdChange: Y平移：" + translateY);
        if (isLeft) {
            if (this.onEyesChangeListener != null) {
                this.onEyesChangeListener.onIpdChange(translateIpd / xtValue);
                this.onEyesChangeListener.onLeftEyeOffsetChange(translateX / xtValue, translateY / (1.5f * ytValue));
            }
            Matrix.translateM(this.mMvpMatrix, 0, -translateIpd, 0.0f, 0.0f);
        } else {
            if (this.onEyesChangeListener != null) {
                this.onEyesChangeListener.onRightEyeOffsetChange(translateX / xtValue, translateY / (1.5f * ytValue));
            }
            Matrix.translateM(this.mMvpMatrix, 0, translateIpd, 0.0f, 0.0f);
        }
        Matrix.translateM(this.mMvpMatrix, 0, translateX, translateY, 0.0f);
        if (centerScale != null) {
            centerScale.updateParams(baseScale, this.singleScale, translateX, translateY, translateIpd);
        }
        if (fastScaleTag == 1) {
            if (fastZoomScale != null) {
                fastZoomScale.updateParams(baseScale, this.singleScale, translateX, translateY, translateIpd);
            }
        }

    }

    public int getIpd() {
        return currentIpd;
    }

    private OnEyesChangeListener onEyesChangeListener;

    public void setOnEyesChangeListener(OnEyesChangeListener onEyesChangeListener) {
        this.onEyesChangeListener = onEyesChangeListener;
    }


}
