package com.cnsj.neptunglasses.view.gl;

import android.opengl.GLES20;
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
public class CenterScale {

    private int hProgram;
    private FloatBuffer vertex, texture;
    private int size = 40;
    private float scale = 1.0f, singleScale = 1.0f;
    private float singleX = 0.0f, singleY = 0.0f;
    private float ipd = 0.0f;
    private int width, height, xOffset;
    int maPositionLoc;
    int maTextureCoordLoc;
    int muMVPMatrixLoc;
    //    int mmUserMode;
//    int muTexMatrixLoc;
//    int uQuatMatrix;
    private boolean isLeft;
    private float[] mvpMatrix = new float[16];

    /**
     * 中心放大
     *
     * @param hProgram
     * @param width    viewport 宽度
     * @param height   高度
     * @param xOffset  X方向偏移量
     */
    public CenterScale(int hProgram, int width, int height, int xOffset) {
        this.hProgram = hProgram;
        this.width = width;
        this.height = height;
        this.xOffset = xOffset;
        isLeft = xOffset <= 0;
        this.maPositionLoc = GLES20.glGetAttribLocation(this.hProgram, "position");
        this.maTextureCoordLoc = GLES20.glGetAttribLocation(this.hProgram, "texcoord");
        this.muMVPMatrixLoc = GLES20.glGetUniformLocation(this.hProgram, "MVP");
//        this.mmUserMode = GLES20.glGetUniformLocation(this.hProgram, "mUserMode");
//        this.muTexMatrixLoc = GLES20.glGetUniformLocation(this.hProgram, "uTexMatrix");
//        this.uQuatMatrix = GLES20.glGetUniformLocation(this.hProgram, "uQuatMatrix");
        GLES20.glEnableVertexAttribArray(maPositionLoc);
        GLES20.glEnableVertexAttribArray(maTextureCoordLoc);
        GLES20.glLinkProgram(this.hProgram);
        initBuffers();
//        Matrix.scaleM(this.mvpMatrix, 0, 0.8f, 0.8f, 0.0f);
    }

    /**
     * 初始化顶点的buffer
     */
    private void initBuffers() {
        Matrix.setIdentityM(this.mvpMatrix, 0);
//        System.arraycopy(mMvpMatrix, 0, this.mvpMatrix, 0, this.mvpMatrix.length);
        Matrix.rotateM(this.mvpMatrix, 0, 180.0f, 0.0f, 1.0f, 0.0f);
        if (this.scale<2){
            Matrix.rotateM(this.mvpMatrix, 0, 180.0f, 0.0f, 0.0f, 1.0f);
        }
        float currentScale = MathUtils.floatMultiply(this.scale, this.singleScale);
        float baseScale = MathUtils.floatMultiply(0.35f, this.singleScale);
//        if (baseScale > 0.45f) {
//            baseScale = 0.45f;
//        }
        float[] vertexFloats;
        float[] texcoords;
        if (isLeft) {
            float offsetX = MathUtils.floatAdd(ipd, singleX);
            vertexFloats = grantPosMode1(baseScale, size, -offsetX, singleY);
        } else {
            float offsetX = MathUtils.floatAdd(-ipd, singleX);
            vertexFloats = grantPosMode1(baseScale, size, -offsetX, singleY);
        }
        float textureRadius = MathUtils.floatDiv(baseScale / 3, currentScale, 5);
        texcoords = grantTextureMode1(textureRadius, 1.0f, size, 0.5f, 0.5f);
        vertex = createFloatBuffer(vertexFloats.length * 4);
        vertex.put(vertexFloats);
        vertex.position(0);
        texture = createFloatBuffer(texcoords.length * 4);
        texture.put(texcoords);
        texture.position(0);
    }


    public void draw(int tex, float[] quatMatrix, float[] tex_matrix) {
        GLES20.glViewport(xOffset, 0, width, height);
        GLES20.glUniformMatrix4fv(this.muMVPMatrixLoc, 1, false, this.mvpMatrix, 0);
        GLES20.glVertexAttribPointer(this.maTextureCoordLoc, 2, GL_FLOAT, false, 8, this.texture);
        GLES20.glVertexAttribPointer(this.maPositionLoc, 2, GL_FLOAT, false, 8, this.vertex);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, size);
    }


    /**
     * 更新缩放偏移参数
     *
     * @param scale
     * @param singleScale
     * @param singleX
     * @param singleY
     * @param ipd
     */
    public void updateParams(float scale, float singleScale, float singleX, float singleY, float ipd) {
        this.scale = scale;
        this.singleScale = singleScale;
        this.singleX = singleX;
        this.singleY = singleY;
        this.ipd = ipd;
        initBuffers();
    }


    /**
     * 获取vertex坐标
     *
     * @param radius
     * @param size
     * @param x
     * @param y
     * @return
     */
    private float[] grantPosMode1(float radius, int size, float x, float y) {
        float[] result = new float[size * 2 + 2];
        int k = 0;
        double span = 3.14 * 2 / size;
        for (int i = 0; i < size; i++) {
//            result[k++] = 1.0f - (float) (x + radius * Math.cos(span * i));
            result[k++] = (float) (x + radius * Math.cos(span * i));
            result[k++] = (float) (y + radius * 1.0f * Math.sin(span * i));
        }
        result[k++] = result[0];
        result[k++] = result[1];
        return result;
    }

    /**
     * 获取texture坐标
     *
     * @param radius
     * @param offset
     * @param size
     * @param x
     * @param y
     * @return
     */
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


    private FloatBuffer createFloatBuffer(int length) {
        ByteBuffer bb = ByteBuffer.allocateDirect(length);
        bb.order(ByteOrder.nativeOrder());
        return bb.asFloatBuffer();
    }


}
