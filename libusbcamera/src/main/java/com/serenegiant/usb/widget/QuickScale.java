package com.serenegiant.usb.widget;

import android.opengl.GLES20;
import android.opengl.GLES32;
import android.opengl.Matrix;

import com.jiangdg.usbcamera.utils.MathUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;

/**
 * 中心放大
 */
public class QuickScale {

    private int hProgram;
    private FloatBuffer leftVertex, rightVertex, leftTexture, rightTexture;
    private float[] leftFloat, rightFloat, leftCoords, rightCoords;
    private int size = 40;
    private float scale = 1.0f, leftSubScale = 1.0f, rightSubScale = 1.0f;
    private float singleLeftX = 0.0f, singleLeftY = 0.0f, singleRightX = 0.0f, singleRightY = 0.0f;
    private float ipdOffset = 0.0f;
    private boolean isDoubleEyes;

    public QuickScale(int hProgram, boolean isDoubleEyes) {
        this.isDoubleEyes = isDoubleEyes;
        this.hProgram = hProgram;
        mMvpMatrix = new float[16];
        Matrix.setIdentityM(this.mMvpMatrix, 0);
//        Matrix.rotateM(mMvpMatrix, 0, 180, 1.0f, 0.0f, 0.0f);
//        Matrix.rotateM(mMvpMatrix, 0, 180, 1.0f, 1.0f, 0.0f);
    }


    int maPositionLoc;
    int maTextureCoordLoc, muMVPMatrixLoc;
    float[] mMvpMatrix;
    int muTexMatrixLoc;

    /**
     *
     */
    public void draw(int tex, float[] mMvpMatrix, float[] tex_matrix) {
        GLES20.glActiveTexture(GL_TEXTURE0);
        GLES20.glBindTexture(GL_TEXTURE_2D, tex);
        GLES20.glUseProgram(hProgram);
        maPositionLoc = GLES20.glGetAttribLocation(hProgram, "aPosition");
        maTextureCoordLoc = GLES20.glGetAttribLocation(hProgram, "aTextureCoord");
        this.muMVPMatrixLoc = GLES20.glGetUniformLocation(this.hProgram, "uMVPMatrix");
        this.muTexMatrixLoc = GLES20.glGetUniformLocation(this.hProgram, "uTexMatrix");
        GLES32.glEnableVertexAttribArray(maPositionLoc);
        GLES32.glEnableVertexAttribArray(maTextureCoordLoc);
//        Matrix.rotateM(mMvpMatrix, 0, 180, 0.0f, 0.0f, 1.0f);
//        Matrix.rotateM(mMvpMatrix, 0, 180, 0.0f, 1.0f, 0.0f);
        GLES20.glUniformMatrix4fv(this.muTexMatrixLoc, 1, false, tex_matrix, 0);
        GLES20.glUniformMatrix4fv(this.muMVPMatrixLoc, 1, false, this.mMvpMatrix, 0);
        GLES20.glVertexAttribPointer(this.maTextureCoordLoc, 2, GL_FLOAT, false, 8, this.rightTexture);
        GLES20.glVertexAttribPointer(this.maPositionLoc, 2, GL_FLOAT, false, 8, this.rightVertex);
        GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN, 0, size);
        if (isDoubleEyes) {
            GLES20.glVertexAttribPointer(this.maTextureCoordLoc, 2, GL_FLOAT, false, 8, this.leftTexture);
            GLES20.glVertexAttribPointer(this.maPositionLoc, 2, GL_FLOAT, false, 8, this.leftVertex);
            GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN, 0, size);
        }
    }


    private float[] grantPosMode1(float radius, int size, float x, float y) {
        float[] result = new float[size * 2 + 2];
        int k = 0;
        double span = 3.14 * 2 / size;
        for (int i = 0; i < size; i++) {
//            result[k++] = 1.0f - (float) (x + radius * Math.cos(span * i));
            result[k++] = (float) (x + radius * Math.cos(span * i));
            result[k++] = (float) (y + radius * 1.8 * Math.sin(span * i));
        }
        result[k++] = result[0];
        result[k++] = result[1];
        return result;
    }

    private float[] grantTextureMode1(float radius, float offset, int size, float x, float y) {
        float[] result = new float[size * 2 + 2];
        int k = 0;
        double span = Math.PI * 2 / size;
        for (int i = 0; i < size; i++) {
            result[k++] = (float) (x + radius * Math.cos(span * i));
            result[k++] = (float) (y + radius * offset * Math.sin(span * i));
        }
        result[k++] = result[0];
        result[k++] = result[1];
        return result;
    }


    /**
     * 初始化参数
     *
     * @param scale
     * @param leftSubScale
     * @param rightSubScale
     * @param singleLeftX
     * @param singleLeftY
     * @param singleRightX
     * @param singleRightY
     * @param ipdOffset
     */
    public void initParams(float scale, float leftSubScale,
                           float rightSubScale, float singleLeftX,
                           float singleLeftY, float singleRightX,
                           float singleRightY, float ipdOffset) {
        this.scale = scale;
        this.leftSubScale = leftSubScale;
        this.rightSubScale = rightSubScale;
        this.singleLeftX = singleLeftX;
        this.singleLeftY = singleLeftY;
        this.singleRightX = singleRightX;
        this.singleRightY = singleRightY;
        this.ipdOffset = ipdOffset;
        refreshParams();
        updateBuffers();
    }

    private void updateBuffers() {
        if (isDoubleEyes) {
            leftVertex = createFloatBuffer(leftFloat.length * 4);
            leftVertex.put(leftFloat);
            leftVertex.position(0);
            leftTexture = createFloatBuffer(leftCoords.length * 4);
            leftTexture.put(leftCoords);
            leftTexture.position(0);
        }
        rightVertex = createFloatBuffer(rightFloat.length * 4);
        rightVertex.put(rightFloat);
        rightVertex.position(0);
        rightTexture = createFloatBuffer(rightCoords.length * 4);
        rightTexture.put(rightCoords);
        rightTexture.position(0);
    }

    private FloatBuffer createFloatBuffer(int length) {
        ByteBuffer bb = ByteBuffer.allocateDirect(length);
        bb.order(ByteOrder.nativeOrder());
        return bb.asFloatBuffer();
    }

    /**
     * 更新尺寸参数
     */
    private void refreshParams() {
        if (isDoubleEyes) {
            float leftScale = MathUtils.floatMultiply(scale, leftSubScale);

            float leftRange = MathUtils.floatMultiply(leftScale, 0.2f);
            if (leftRange > 0.25f) {
                leftRange = 0.25f;
            }
            float leftXOffset = MathUtils.floatSub(ipdOffset, singleLeftX);
            float leftYoffset = singleLeftY;
            leftFloat = grantPosMode1(leftRange, size, MathUtils.floatSub(-0.5f, leftXOffset), leftYoffset);
            leftCoords = grantTextureMode1(leftRange / 2 * 1.0f / scale, 1.5f, size, 0.5f + imuX, 0.5f + imuY);
            float rightScale = MathUtils.floatMultiply(scale, rightSubScale);
            float rightRange = MathUtils.floatMultiply(rightScale, 0.2f);
            if (rightRange > 0.25f) {
                rightRange = 0.25f;
            }
            float rightXOffset = MathUtils.floatAdd(ipdOffset, singleRightX);
            float rightYOffset = singleRightY;
            rightFloat = grantPosMode1(rightRange, size, MathUtils.floatAdd(0.5f, rightXOffset), rightYOffset);
            rightCoords = grantTextureMode1(rightRange / 2 * 1.0f / scale, 1.5f, size, 0.5f + imuX, 0.5f + imuY);
        } else {
            float rightScale = MathUtils.floatMultiply(scale, rightSubScale);
            float rightRange = MathUtils.floatMultiply(rightScale, 0.4f);
            if (rightRange > 0.5f) {
                rightRange = 0.5f;
            }
            float rightXOffset = MathUtils.floatAdd(ipdOffset, singleRightX);
            float rightYOffset = singleRightY;
            rightFloat = grantPosMode1(rightRange, size, MathUtils.floatAdd(0.0f, rightXOffset), rightYOffset);
            rightCoords = grantTextureMode1(rightRange / 2 * 1.0f / scale, 0.75f, size, 0.5f + imuX, 0.5f + imuY);
        }


    }


    private float imuX = 0, imuY = 0;

    public void setImu(float x, float y) {
        imuX = -x;
        imuY = y;
    }


}
