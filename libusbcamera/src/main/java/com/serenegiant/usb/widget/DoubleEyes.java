package com.serenegiant.usb.widget;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLES32;
import android.opengl.Matrix;
import android.util.Log;

import com.jiangdg.usbcamera.utils.MathUtils;
import com.serenegiant.glutils.GLHelper;
import com.serenegiant.usb.common.OffsetUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glGetShaderInfoLog;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glViewport;

/**
 * 绘制双眼的数据
 */
public class DoubleEyes {

    private Context context;
    private int hProgram;
    private int contsize = 33;
    private FloatBuffer pVertex;
    private FloatBuffer pVertex2;
    private FloatBuffer pTexCoord;
    int maPositionLoc;
    int maTextureCoordLoc;
    int muMVPMatrixLoc;
    int mmUserMode;
    int muTexMatrixLoc;
    int uQuatMatrix;
    private float scale = 1.0f;
    private final float[] mMvpMatrix;
    private QuickScale quickScale;
    private RectAngle leftRectAngle;
    private boolean isDoubleEyes = true;

    public DoubleEyes(Context context, int hProgram) {
        this.context = context;
        this.hProgram = GLHelper.loadShader(shadercode.OriginVSS, shadercode.OriginFSS11);
        this.mMvpMatrix = new float[16];
        Matrix.setIdentityM(this.mMvpMatrix, 0);
//        Matrix.rotateM(this.mMvpMatrix, 0, 180, 0.0f, 0.0f, 1.0f);
//        Matrix.rotateM(this.mMvpMatrix, 0, 180, 0.0f, 1.0f, 0.0f);
        maPositionLoc = GLES20.glGetAttribLocation(this.hProgram, "aPosition");
        maTextureCoordLoc = GLES20.glGetAttribLocation(this.hProgram, "aTextureCoord");
        this.muMVPMatrixLoc = GLES20.glGetUniformLocation(this.hProgram, "uMVPMatrix");
        this.mmUserMode = GLES20.glGetUniformLocation(this.hProgram, "mUserMode");
        this.muTexMatrixLoc = GLES20.glGetUniformLocation(this.hProgram, "uTexMatrix");
        this.uQuatMatrix = GLES20.glGetUniformLocation(this.hProgram, "uQuatMatrix");
        float[] texcoord = initTexture1(contsize);
        this.pTexCoord = ByteBuffer.allocateDirect(texcoord.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.pTexCoord.put(texcoord);
        this.pTexCoord.flip();
        GLES20.glLinkProgram(this.hProgram);
        quickScale = new QuickScale(this.hProgram, isDoubleEyes);
        leftRectAngle = new RectAngle(isDoubleEyes);
//        leftRectAngle.initParams(this.scale, leftScale, rightScale, leftX, leftY, rightX, rightY, currentIpd);
        setScale(this.scale);
    }

    private int vertexShader;
    private int fragmentShader2D;

    private int loadShader(int type, String shaderCode) {
        int shader = glCreateShader(type);
        glShaderSource(shader, shaderCode);
        glCompileShader(shader);
        String log = glGetShaderInfoLog(shader);
        return shader;
    }

    /**
     * 绘制
     *
     * @param texId
     */
    public void draw(int texId, float[] tex_matrix, float[] quatMatrix) {
//        GLES20.glClearColor(0f, 0f, 0f, 1f);
//        glClear(GL_COLOR_BUFFER_BIT);
//        glViewport(0, 0, 2400, 1200);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glUseProgram(hProgram);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GL_TEXTURE_2D, texId);
//        maPositionLoc = GLES20.glGetAttribLocation(hProgram, "aPosition");
//        maTextureCoordLoc = GLES20.glGetAttribLocation(hProgram, "aTextureCoord");
        GLES20.glUniformMatrix4fv(this.uQuatMatrix, 1, false, quatMatrix, 0);
        GLES20.glUniformMatrix4fv(this.muTexMatrixLoc, 1, false, tex_matrix, 0);
        GLES20.glUniform1i(this.mmUserMode, mUserMode);
        GLES20.glUniformMatrix4fv(this.muMVPMatrixLoc, 1, false, mMvpMatrix, 0);
        GLES32.glEnableVertexAttribArray(maTextureCoordLoc);
        GLES20.glVertexAttribPointer(this.maTextureCoordLoc, 2, GL_FLOAT, false, 8, this.pTexCoord);
        GLES32.glEnableVertexAttribArray(maPositionLoc);
        GLES20.glVertexAttribPointer(this.maPositionLoc, 2, GL_FLOAT, false, 8, this.pVertex);
        for (int i = 0; i < contsize; i++) {
            glDrawArrays(GLES20.GL_TRIANGLE_STRIP, i * contsize * 2, contsize * 2);
        }
        if (isDoubleEyes) {
            GLES20.glVertexAttribPointer(this.maPositionLoc, 2, GL_FLOAT, false, 8, this.pVertex2);
            for (int i = 0; i < contsize; i++) {
                glDrawArrays(GLES20.GL_TRIANGLE_STRIP, i * contsize * 2, contsize * 2);
            }
        }
        if (quickScaleTag == 1) {
            quickScale.draw(texId, mMvpMatrix, tex_matrix);
        }
        if (quickShrink == 1) {
            leftRectAngle.draw(mMvpMatrix, tex_matrix);
        }
//        leftRectAngle.draw(mMvpMatrix);

    }


    /**
     * 中心放大 0 关闭  1 开启
     */
    private int quickScaleTag = 0;

    public int getQuickScaleTag() {
        return quickScaleTag;
    }

    public void setQuickScaleTag(int quickScaleTag) {
        this.quickScaleTag = quickScaleTag;
        if (this.quickScaleTag == 1) {
            quickScale.initParams(MathUtils.floatMultiply(this.scale, 0.5f), this.leftScale, this.rightScale, this.singleLeftX,
                    this.singleLeftY, this.singleRightX, this.singleRightY, this.currentIpdOffset);
        }
    }


    /**
     * 初始化纹理坐标
     *
     * @param size
     * @return
     */
    private float[] initTexture1(int size) {
        float[] result = new float[size * size * 4];
//        float offset = 0.125f;
        float offset = 0.0f;
        float spanx = (1.0f - 2 * offset) / (size - 1);
        float spany = 1.0f / (size - 1);
        int k = 0;
        for (int j = 0; j < size - 1; j++) {
            for (int i = 0; i < size; i++) {
                result[k++] = 1.0f - (spanx * i + offset);
                result[k++] = spany * j;
                result[k++] = 1.0f - (spanx * i + offset);
                result[k++] = spany * (j + 1);
            }
        }
        return result;
    }

    /**
     * 设置IMU纹理偏移
     *
     * @param x
     * @param y
     */
    public void setImu(float x, float y) {
        float[] textureResult = initTexture1(contsize);
        for (int i = 0; i < textureResult.length; i++) {
            if (i % 2 == 0) {
                textureResult[i] = textureResult[i] + x;
            } else {
                textureResult[i] = textureResult[i] + y;
            }
        }
        if (quickScale != null) {
            quickScale.setImu(x, y);
        }
        this.pTexCoord = ByteBuffer.allocateDirect(textureResult.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.pTexCoord.put(textureResult);
        this.pTexCoord.flip();
    }

    private float[] initVertex1(int size) {
        float[] result = new float[size * size * 4];
        //0.889=16/9 0.667=4/3
        float xmin = -0.889f, xmax = 0.889f, ymin = -1, ymax = 1;
        float spanx = (xmax - xmin) / (size - 1);
        float spany = (ymax - ymin) / (size - 1);
        int k = 0;
        for (int j = 0; j < size - 1; j++) {
            for (int i = 0; i < size; i++) {
                result[k++] = spanx * i + xmin;
                result[k++] = 1 - (spany * j);
                result[k++] = spanx * i + xmin;
                result[k++] = 1 - (spany * (j + 1));
            }
        }
        return result;
    }


    /**
     * 初始化参数
     *
     * @param scale
     * @param leftScale
     * @param rightScale
     * @param leftX
     * @param leftY
     * @param rightX
     * @param rightY
     * @param currentIpd
     */
    public void initParams(float scale, float leftScale, float rightScale, int leftX, int leftY, int rightX, int rightY, int currentIpd) {
        this.scale = scale;
        this.leftScale = leftScale;
        this.rightScale = rightScale;
        this.leftX = leftX;
        this.leftY = leftY;
        this.rightX = rightX;
        this.rightY = rightY;
        this.currentIpd = currentIpd;
        setScale(this.scale);
    }


    /**
     * 设定放大倍数
     *
     * @param scale
     */
    public void setScale(float scale) {
        this.scale = scale;
        if (scale < 1) {
            this.scale = 1f;
        } else if (scale > 25) {
            this.scale = 25;
        }
        Log.d("TAG", "setScale: ipd:" + currentIpd);
        setLeftSubScale(this.leftScale);
    }

    public float getScale() {
        if (quickShrink == 1) {
            return this.mShinkScale;
        } else {
            return this.scale;
        }
    }

    private OnEyesChangeListener onEyesChangeListener;

    public void setOnEyesChangeListener(OnEyesChangeListener onEyesChangeListener) {
        this.onEyesChangeListener = onEyesChangeListener;
        Log.d("TAG", "onIpdChange: 初始化");
    }

    /**
     * 左眼缩放
     *
     * @param leftSubScale
     */
    public void setLeftSubScale(float leftSubScale) {
        leftScale = leftSubScale;
        if (leftSubScale < 0.5f) {
            leftScale = 0.5f;
        } else if (leftSubScale >= 1.0f) {
            leftScale = 1.0f;
        }
        if (this.onEyesChangeListener != null) {
            this.onEyesChangeListener.onLeftScaleChange(leftScale);
        }
        setRightSubScale(this.rightScale);
    }

    public float getLeftScale() {
        return leftScale;
    }

    /**
     * 右眼缩放
     *
     * @param rightSubScale
     */
    public void setRightSubScale(float rightSubScale) {
        rightScale = rightSubScale;
        if (rightSubScale < 0.5f) {
            rightScale = 0.5f;
        } else if (rightSubScale >= 1.0f) {
            rightScale = 1.0f;
        }
        if (this.onEyesChangeListener != null) {
            this.onEyesChangeListener.onRightScaleChange(rightScale);
        }
        setLeftEyeOffset(this.leftX, this.leftY);
    }

    public float getRightScale() {
        return rightScale;
    }

    /**
     * 左眼偏移
     *
     * @param leftX
     * @param leftY
     */
    public void setLeftEyeOffset(int leftX, int leftY) {
        this.leftX = leftX;
        this.leftY = leftY;
        if (this.leftX > 20) {
            this.leftX = 20;
        } else if (this.leftX < -20) {
            this.leftX = -20;
        }
        if (this.leftY > 20) {
            this.leftY = 20;
        } else if (this.leftY < -20) {
            this.leftY = -20;
        }
        singleLeftX = MathUtils.floatMultiply(this.leftX, xDensity);
        singleLeftY = MathUtils.floatMultiply(this.leftY, yDensity);
        if (this.onEyesChangeListener != null) {
            this.onEyesChangeListener.onLeftEyeOffsetChange(singleLeftX, singleLeftY);
        }
        setRightEyeOffset(this.rightX, this.rightY);
    }

    public String getLeftEyeOffset() {
        return leftX + "," + leftY;
    }

    /**
     * 右眼偏移
     *
     * @param rightX
     * @param rightY
     */
    public void setRightEyeOffset(int rightX, int rightY) {
        this.rightX = rightX;
        this.rightY = rightY;
        if (this.rightX > 20) {
            this.rightX = 20;
        } else if (this.rightX < -20) {
            this.rightX = -20;
        }
        if (this.rightY > 20) {
            this.rightY = 20;
        } else if (this.rightY < -20) {
            this.rightY = -20;
        }
        singleRightX = MathUtils.floatMultiply(this.rightX, xDensity);
        singleRightY = MathUtils.floatMultiply(this.rightY, yDensity);
        if (this.onEyesChangeListener != null) {
            this.onEyesChangeListener.onRightEyeOffsetChange(singleRightX, singleRightY);
        }
        setIpd(this.currentIpd);
    }

    public String getRightEyeOffset() {
        return rightX + "," + rightY;
    }


    private int leftX = 0, leftY = 0, rightX = 0, rightY = 0;
    private float singleLeftX = 0.0f, singleLeftY = 0.0f, singleRightX = 0.0f, singleRightY = 0.0f;
    private float leftScale = 1.0f;
    private float rightScale = 1.0f;
    private float yDensity = 0.0108f;
    private float xDensity = 0.0069f;
    private int currentIpd = 62;
    private float currentIpdOffset = 0.0f;


    /**
     * 重置所有设置
     */
    public void resetAll() {
        currentIpd = 62;
        leftX = 0;
        leftY = 0;
        rightX = 0;
        rightY = 0;
        leftScale = 1.0f;
        rightScale = 1.0f;
        setScale(this.scale);
    }

    /**
     * 瞳距偏移
     *
     * @param offset
     */
    public void setIpd(int offset) {
        int ipd = offset;
        if (ipd > 75) {
            ipd = 75;
        } else if (ipd < 50) {
            ipd = 50;
        }
        int intOffset = ipd - 62;
        currentIpd = ipd;
        currentIpdOffset = xDensity * intOffset;
        if (this.onEyesChangeListener != null) {
            Log.d("TAG", "onIpdChange: 配置瞳距");
            this.onEyesChangeListener.onIpdChange(currentIpdOffset);
        } else {
            Log.d("TAG", "onIpdChange: 监听为NULL");
        }
        if (quickScaleTag == 1) {
            quickScale.initParams(MathUtils.floatMultiply(this.scale, 0.5f), this.leftScale, this.rightScale, this.singleLeftX,
                    this.singleLeftY, this.singleRightX, this.singleRightY, this.currentIpdOffset);
        }
        if (quickShrink == 1) {
//            if (this.scale == 1) {
//                leftRectAngle.initParams(this.mShinkScale, this.leftScale, this.rightScale,
//                        this.singleLeftX, this.singleLeftY, this.singleRightX, this.singleRightY, this.currentIpdOffset);
//            } else {
            leftRectAngle.initParams(this.scale, this.leftScale, this.rightScale,
                    this.singleLeftX, this.singleLeftY, this.singleRightX, this.singleRightY, this.currentIpdOffset);

//            }
            mShinkScale = this.scale;
            this.scale = 1;
        }
        setTextureScaleAndOffset();
        setVertices();
    }


    private float mShinkScale = 1.0f;
    private int quickShrink = 0;

    public void setQuickShrink() {
        mShinkScale = this.scale;
        setScale(1);
        quickShrink = 1;
        if (leftRectAngle != null) {
            leftRectAngle.initParams(this.scale, this.leftScale, this.rightScale,
                    this.singleLeftX, this.singleLeftY, this.singleRightX, this.singleRightY, this.currentIpdOffset);
        }
    }

    public void quitQuickShrink() {
        quickShrink = 0;
        setScale(mShinkScale);
        mShinkScale = 1.0f;
        if (leftRectAngle != null) {
            leftRectAngle.initParams(this.scale, this.leftScale, this.rightScale,
                    this.singleLeftX, this.singleLeftY, this.singleRightX, this.singleRightY, this.currentIpdOffset);
        }
    }

    public int getQuickShrink() {
        return quickShrink;
    }

    /**
     * 获取当前瞳距
     *
     * @return
     */
    public int getIpd() {
        return currentIpd;
    }

    /**
     * 重置瞳距
     */
    public void resetIpd() {
        setIpd(62);
    }


    float[] ver1, ver2;//1 right 先画 2 left后画

    /**
     * 设定偏移和缩放
     */
    public void setTextureScaleAndOffset() {
        ver1 = new float[contsize * contsize * 4];
        ver2 = new float[contsize * contsize * 4];
//        float[] texcoord = new float[CENTER_VERTICES.length];

        float R0, pillowScale = 0.0f;
        int k = 0;
//        float[] result = initVertex1(contsize);

        float[] rightResult = OffsetUtils.getRightOffsetVertex();
//        for (int i = 0; i < result.length; i++) {
//            result[i] = result[i] * baseScale;
//        }
        if (isDoubleEyes) {
            float[] leftResult = OffsetUtils.getLeftOffsetVertex();
            float baseScale = MathUtils.floatMultiply(this.scale, 0.4f);
            float leftScale = MathUtils.floatMultiply(baseScale, this.leftScale);
            float rightScale = MathUtils.floatMultiply(baseScale, this.rightScale);
            for (int i = 0; i < leftResult.length; i++) {
                if (i % 2 == 0) {
                    ver1[i] = rightResult[i] * rightScale + 0.5f + 0.1f;
                    ver2[i] = leftResult[i] * leftScale - 0.5f + 0.1f;
                    if (ver2[i] >= 0) {
                        ver2[i] = 0;
                    }
                    if (ver1[i] <= 0) {
                        ver1[i] = 0;
                    }
                    ver1[i] += currentIpdOffset;
                    ver1[i] += singleRightX;
                    ver2[i] -= currentIpdOffset;
                    ver2[i] += singleLeftX;
                } else {
                    ver1[i] = rightResult[i] * rightScale;
                    ver2[i] = leftResult[i] * leftScale;
                    ver1[i] += singleRightY;
                    ver2[i] += singleLeftY;
                }
            }
        } else {
            float baseScale = MathUtils.floatMultiply(this.scale, 1.0f);
            float rightScale = MathUtils.floatMultiply(baseScale, this.rightScale);
            for (int i = 0; i < rightResult.length; i++) {
                if (i % 2 == 0) {
                    ver1[i] = rightResult[i] * rightScale;
                    ver1[i] += currentIpdOffset;
                    ver1[i] += singleRightX;
                } else {
                    ver1[i] = rightResult[i] * rightScale;
                    ver1[i] += singleRightY;
                }
            }
        }

    }

    private void setVertices() {
        this.pVertex = ByteBuffer.allocateDirect(ver1.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.pVertex.put(ver1);
        this.pVertex.position(0);
        if (isDoubleEyes) {
            this.pVertex2 = ByteBuffer.allocateDirect(ver2.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            this.pVertex2.put(ver2);
            this.pVertex2.position(0);
        }
    }


    //设置显示模式，
    //0 全彩 5 灰度 6 灰度反色 7,8,9,10 描边模式4种颜色 11 伪彩色
    int mUserMode = 0;

    public void setUserMode(int userMode) {
        mUserMode = userMode;
    }

    public int getMUserMode() {
        return mUserMode;
    }
}

