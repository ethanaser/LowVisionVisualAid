package com.cnsj.neptunglasses.view.gl;

import android.opengl.GLES20;

import com.serenegiant.usb.common.OffsetUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_FLOAT;

/**
 * 绘制单眼
 */
public class SingleEyeToYuv {

    private int program;
    private int width, height, xOffset;
    private boolean isLeft;

    /**
     * viewport的宽度，高度，和偏移位置
     *
     * @param program
     * @param width
     * @param height
     * @param xOffset
     * @param isLeft
     */
    public SingleEyeToYuv(int program, int width, int height, int xOffset, boolean isLeft) {
        this.program = program;
        this.width = width;
        this.height = height;
        this.xOffset = xOffset;
        this.isLeft = isLeft;
        initLocation();
        initBuffers();
    }

    private int positionLocation, texcoordLocation, mvpLocation;

    private void initLocation() {
        positionLocation = GLES20.glGetAttribLocation(program, "position");
        texcoordLocation = GLES20.glGetAttribLocation(program, "texcoord");
        mvpLocation = GLES20.glGetUniformLocation(program, "MVP");
    }

    private FloatBuffer vertexBuffer, texcoordBuffer;

    /**
     * 初始化顶点和纹理
     */
    public void initBuffers() {
        float[] vertex;
        if (isLeft) {
            vertex = OffsetUtils.getLeftOffsetVertex();
        } else {
            vertex = OffsetUtils.getRightOffsetVertex();
        }
        vertexBuffer = createFloatBuffer(vertex.length * 4);
        vertexBuffer.put(vertex);
        vertexBuffer.position(0);
        float[] texcoord = OffsetUtils.getTextures();
        texcoordBuffer = createFloatBuffer(texcoord.length * 4);
        texcoordBuffer.put(texcoord);
        texcoordBuffer.position(0);
    }

    private FloatBuffer createFloatBuffer(int length) {
        ByteBuffer bb = ByteBuffer.allocateDirect(length);
        bb.order(ByteOrder.nativeOrder());
        return bb.asFloatBuffer();
    }

    int contsize = 33;

    public void draw(int texId, float[] mvpMatrix) {
        GLES20.glVertexAttribPointer(texcoordLocation, 2, GL_FLOAT, false, 8, texcoordBuffer);
        GLES20.glEnableVertexAttribArray(texcoordLocation);
        GLES20.glViewport(xOffset, 0, width, height);
        GLES20.glEnableVertexAttribArray(positionLocation);
        GLES20.glVertexAttribPointer(positionLocation, 2, GL_FLOAT, false, 8, vertexBuffer);
        GLES20.glUniformMatrix4fv(this.mvpLocation, 1, false, mvpMatrix, 0);
//            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        for (int i = 0; i < contsize; i++) {
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, i * contsize * 2, contsize * 2);
        }
    }

}
