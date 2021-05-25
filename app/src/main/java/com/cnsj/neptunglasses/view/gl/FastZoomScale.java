package com.cnsj.neptunglasses.view.gl;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.jiangdg.usbcamera.utils.MathUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * 快速放大 快速缩小
 */
public class FastZoomScale {

    private FloatBuffer vertexBuffer;
    private int mProgram;
    private int positionHandle;
    private int colorHandle;
    private int vPMatrixHandle;
    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 2;
    private int vertexShader;
    private int fragmentShader;
    private float color[] = {1f, 0.0f, 0.0f, 1.0f};
    private float xValue = 1.0f, yValue = 0.5625f;
    private float vertices[] = {
            -xValue, -yValue,
            xValue, -yValue,
            xValue, yValue,
            -xValue, yValue,
    };
    private int drawSize = 10;
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

    private int width, height, xOffset;
    private boolean isLeft;
    private float[] mvpMatrix;

    /**
     * 快速放大，快速缩小
     *
     * @param width
     * @param height
     * @param xOffset
     */
    public FastZoomScale(int width, int height, int xOffset) {
        this.width = width;
        this.height = height;
        this.xOffset = xOffset;
        this.isLeft = xOffset <= 0;
        ByteBuffer bb = ByteBuffer.allocateDirect(
                vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);
        vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);
        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        vPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        Log.d("TAG", "FastZoomScale: mProgram:" + mProgram);
        Log.d("TAG", "FastZoomScale: positionHandle:" + positionHandle);
        Log.d("TAG", "FastZoomScale: colorHandle:" + colorHandle);
        Log.d("TAG", "FastZoomScale: vPMatrixHandle:" + vPMatrixHandle);
        mvpMatrix = new float[16];
        Matrix.setIdentityM(mvpMatrix, 0);
    }

    public void draw() {
        GLES20.glViewport(xOffset, 0, width, height);
        GLES20.glUseProgram(mProgram);
        GLES20.glUniform4fv(colorHandle, 1, color, 0);
        GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix, 0);
        GLES20.glLineWidth(5.0f);
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, 2,
                GLES20.GL_FLOAT, false,
                0, vertexBuffer);
        GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, vertices.length / 2);
    }

    private float scale = 1.0f, singleScale = 1.0f, offsetX = 1.0f, offsetY = 1.0f, ipdOffset = 1.0f;

    /**
     * 更新放大参数
     *
     * @param singleScale
     * @param offsetX
     * @param offsetY
     * @param ipdOffset
     */
    public void updateParams(float scale, float singleScale, float offsetX, float offsetY, float ipdOffset) {
        this.scale = scale;
        this.singleScale = singleScale;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.ipdOffset = ipdOffset;
        float fastScale = MathUtils.floatDiv(1.0f, scale, 5);
        Matrix.setIdentityM(this.mvpMatrix, 0);
        Matrix.scaleM(this.mvpMatrix, 0, singleScale, singleScale, 1.0f);
        Matrix.scaleM(this.mvpMatrix, 0, fastScale, fastScale, 1.0f);
        if (isLeft) {
            Matrix.translateM(this.mvpMatrix, 0, -ipdOffset, 0.0f, 0.0f);
        } else {
            Matrix.translateM(this.mvpMatrix, 0, ipdOffset, 0.0f, 0.0f);
        }
        Matrix.translateM(this.mvpMatrix, 0, offsetX, offsetY, 0.0f);
    }


    private int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }
}
