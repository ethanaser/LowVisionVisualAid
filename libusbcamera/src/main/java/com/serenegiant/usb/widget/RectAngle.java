package com.serenegiant.usb.widget;

import android.opengl.GLES20;
import android.opengl.GLES32;
import android.opengl.Matrix;
import android.util.Log;

import com.jiangdg.usbcamera.utils.MathUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * 快速放大，快速缩小
 */
public class RectAngle {

    private FloatBuffer leftVertex, rightVertex;
    private float[] leftFloats, rightFloats;
    private int hProgram;
    private final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    // the matrix must be included as a modifier of gl_Position
                    // Note that the uMVPMatrix factor *must be first* in order
                    // for the matrix multiplication product to be correct.
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}";
    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";
    private int vertexShader;
    private int fragmentShader;
    float color[] = {1f, 0.0f, 0.0f, 1.0f};
    private int vPMatrixHandle;
    private int positionHandle;
    private int colorHandle;
    float[] mMvpMatrix;
    private boolean isDoubleEyes;
    int muTexMatrixLoc;

    public RectAngle(boolean isDoubleEyes) {
        this.isDoubleEyes = isDoubleEyes;
        vertexShader = loadShader(GLES32.GL_VERTEX_SHADER,
                vertexShaderCode);
        fragmentShader = loadShader(GLES32.GL_FRAGMENT_SHADER,
                fragmentShaderCode);
        hProgram = GLES32.glCreateProgram();
        GLES32.glAttachShader(hProgram, vertexShader);
        GLES32.glAttachShader(hProgram, fragmentShader);
        GLES32.glLinkProgram(hProgram);
        positionHandle = GLES32.glGetAttribLocation(hProgram, "vPosition");
        colorHandle = GLES32.glGetUniformLocation(hProgram, "vColor");
        vPMatrixHandle = GLES32.glGetUniformLocation(hProgram, "uMVPMatrix");
        this.muTexMatrixLoc = GLES20.glGetUniformLocation(this.hProgram, "uTexMatrix");
        mMvpMatrix = new float[16];
        Matrix.setIdentityM(this.mMvpMatrix, 0);
//        Matrix.rotateM(mMvpMatrix, 0, 180, 1.0f, 0.0f, 0.0f);
//        initVertexFloats();
//        initVertexBuffers();
    }


    public void draw(float[] mvpMatrix,float[] tex_matrix) {
        initVertexFloats();
        initVertexBuffers();
        GLES32.glUseProgram(hProgram);
        GLES32.glUniform4fv(colorHandle, 1, color, 0);
        GLES20.glUniformMatrix4fv(this.muTexMatrixLoc, 1, false, tex_matrix, 0);
        GLES32.glUniformMatrix4fv(vPMatrixHandle, 1, false, mMvpMatrix, 0);
        GLES32.glLineWidth(5.0f);
        if (isDoubleEyes){
            GLES32.glEnableVertexAttribArray(positionHandle);
            GLES32.glVertexAttribPointer(positionHandle, 2,
                    GLES32.GL_FLOAT, false,
                    0, leftVertex);
            GLES32.glDrawArrays(GLES32.GL_LINES, 0, leftFloats.length / 2);
        }
        GLES32.glEnableVertexAttribArray(positionHandle);
        GLES32.glVertexAttribPointer(positionHandle, 2,
                GLES32.GL_FLOAT, false,
                0, rightVertex);
        GLES32.glDrawArrays(GLES32.GL_LINES, 0, rightFloats.length / 2);
    }

    private int loadShader(int type, String shaderCode) {
        int shader = GLES32.glCreateShader(type);
        GLES32.glShaderSource(shader, shaderCode);
        GLES32.glCompileShader(shader);
        return shader;
    }

    private float scale = 1.0f, leftSubScale = 1.0f, rightSubScale = 1.0f;
    private float singleLeftX = 0.0f, singleLeftY = 0.0f, singleRightX = 0.0f, singleRightY = 0.0f;
    private float ipdOffset = 0.0f;

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
//        initVertexFloats();
//        initVertexBuffers();
    }

    public float getScale() {
        return this.scale;
    }

    public void setShrinkScale(float scale) {
        this.scale = scale;
        setLeftX(this.singleLeftX);
    }

    public void setLeftX(float singleLeftX) {
        this.singleLeftX = singleLeftX;
        setLeftY(this.singleLeftY);
    }

    public void setLeftY(float singleLeftY) {
        this.singleLeftY = singleLeftY;
        setRightX(this.singleRightX);
    }

    public void setRightX(float singleRightX) {
        this.singleRightX = singleRightX;
        setRightY(singleRightY);
    }

    public void setRightY(float singleRightY) {
        this.singleRightY = singleRightY;
        setIpd(this.ipdOffset);
    }

    public void setIpd(float ipdOffset) {
        this.ipdOffset = ipdOffset;

    }


    private int width = 2400, height = 1200;

    /**
     * 加载左右眼的数组
     */
    private void initVertexFloats() {
        if (isDoubleEyes) {
            float leftCneterX = MathUtils.floatSub(ipdOffset, singleLeftX);
            Log.d("TAG", "initVertexFloats: "+leftCneterX);
            leftCneterX = MathUtils.floatSub(-0.5f, leftCneterX);
            float leftCenterY = singleLeftY;
            float rightCenterX = MathUtils.floatAdd(ipdOffset, singleRightX);
            rightCenterX = MathUtils.floatAdd(0.5f, rightCenterX);
            float rightCenterY = singleRightY;
            float ll = 0.5f / (this.scale * 2);
            float lx1 = leftCneterX - ll * 1.33f;
            float lx2 = leftCneterX + ll * 1.33f;
            float ly1 = leftCenterY - ll * (width / height);
            float ly2 = leftCenterY + ll * (width / height);
            leftFloats = new float[]{lx1 + 0.003f, ly1,
                    lx1 + 0.003f, ly2,
                    (lx1 + 0.006f), ly1,
                    (lx1 + 0.006f), ly2,
                    lx1 + 0.001f, ly2,
                    lx2 - 0.001f, ly2,
                    lx1 + 0.001f, ly2 + 0.003f,
                    lx2 - 0.001f, ly2 + 0.003f,
                    lx2 - 0.003f, ly2,
                    lx2 - 0.003f, ly1,
                    (lx2 - 0.006f), ly1,
                    (lx2 - 0.006f), ly2,
                    lx2 - 0.001f, ly1,
                    lx1 + 0.001f, ly1,
                    lx2 - 0.001f, ly1 - 0.003f,
                    lx1 + 0.001f, ly1 - 0.003f};
            float rx1 = rightCenterX - ll * 1.33f;
            float rx2 = rightCenterX + ll * 1.33f;
            float ry1 = rightCenterY - ll * (width / height);
            float ry2 = rightCenterY + ll * (width / height);
            rightFloats = new float[]{rx1 + 0.003f, ry1,
                    rx1 + 0.003f, ry2,
                    (rx1 + 0.006f), ry1,
                    (rx1 + 0.006f), ry2,
                    rx1 + 0.001f, ry2,
                    rx2 - 0.001f, ry2,
                    rx1 + 0.001f, ry2 + 0.003f,
                    rx2 - 0.001f, ry2 + 0.003f,
                    rx2 - 0.003f, ry2,
                    rx2 - 0.003f, ry1,
                    (rx2 - 0.006f), ry1,
                    (rx2 - 0.006f), ry2,
                    rx2 - 0.001f, ry1,
                    rx1 + 0.001f, ry1,
                    rx2 - 0.001f, ry1 - 0.003f,
                    rx1 + 0.001f, ry1 - 0.003f};
        } else {
            float rightCenterX = MathUtils.floatAdd(ipdOffset, singleRightX);
//            rightCenterX = MathUtils.floatAdd(0.5f, rightCenterX);
            float rightCenterY = singleRightY;
            float ll = 1.0f / (this.scale * 2);
            float rx1 = rightCenterX - ll * 1.33f;
            float rx2 = rightCenterX + ll * 1.33f;
            float ry1 = rightCenterY - ll * (width / height);
            float ry2 = rightCenterY + ll * (width / height);
            rightFloats = new float[]{rx1 + 0.003f, ry1,
                    rx1 + 0.003f, ry2,
                    (rx1 + 0.006f), ry1,
                    (rx1 + 0.006f), ry2,
                    rx1 + 0.001f, ry2,
                    rx2 - 0.001f, ry2,
                    rx1 + 0.001f, ry2 + 0.003f,
                    rx2 - 0.001f, ry2 + 0.003f,
                    rx2 - 0.003f, ry2,
                    rx2 - 0.003f, ry1,
                    (rx2 - 0.006f), ry1,
                    (rx2 - 0.006f), ry2,
                    rx2 - 0.001f, ry1,
                    rx1 + 0.001f, ry1,
                    rx2 - 0.001f, ry1 - 0.003f,
                    rx1 + 0.001f, ry1 - 0.003f};
        }

    }

    /**
     * 加载左右眼的buffer
     */
    private void initVertexBuffers() {
        if (isDoubleEyes) {
            ByteBuffer bb = ByteBuffer.allocateDirect(
                    leftFloats.length * 4);
            bb.order(ByteOrder.nativeOrder());
            leftVertex = bb.asFloatBuffer();
            leftVertex.put(leftFloats);
            leftVertex.position(0);
        }
        ByteBuffer bb1 = ByteBuffer.allocateDirect(
                rightFloats.length * 4);
        bb1.order(ByteOrder.nativeOrder());
        rightVertex = bb1.asFloatBuffer();
        rightVertex.put(rightFloats);
        rightVertex.position(0);
    }

}
