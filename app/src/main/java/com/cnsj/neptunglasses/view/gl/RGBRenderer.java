package com.cnsj.neptunglasses.view.gl;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLES32;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;

import com.cnsj.neptunglasses.constant.What;
import com.jiangdg.usbcamera.utils.MathUtils;
import com.serenegiant.usb.common.OffsetUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_FLOAT;

/**
 * Opengl 绘制RGB565数据
 */
public class RGBRenderer implements GLSurfaceView.Renderer {
    private Activity context;
    private GLSurfaceView glSurfaceView;
    private float screenWidth, screenHeight;//屏幕宽度和屏幕高度，使用时屏幕宽度需要除以2
    private FastZoomScale leftFastScale, rightFastScale;
    private CenterScale leftCenterScale, rightCenterScale;

    public RGBRenderer(Context context, GLSurfaceView glSurfaceView) {
        this.context = (Activity) context;
        this.glSurfaceView = glSurfaceView;
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
    }

    /**
     * 相机分辨率
     */
    private final static int DEFAULT_WIDTH = 1928, DEFAUTL_HEIGHT = 1088;
    private int width = DEFAULT_WIDTH, height = DEFAUTL_HEIGHT;
    private final static int screenPiexlsWidth = 2400, screenPiexlsHeight = 1200;
    private float xPoint = 1.0f, yPoint = 0.5625f;
    //    private float xPoint = 0.889f, yPoint = 1.0f;
    private float VERTICES_COORS[] =
            {
                    -xPoint, yPoint,
                    -xPoint, -yPoint,
                    xPoint, yPoint,
                    xPoint, -yPoint
            };

    private float TEXTURE_COORS[] =
            {
                    0, 0,
                    0, 1,
                    1, 0,
                    1, 1
            };

    //两色模式 默认 黑白模式
    private float[] colorValue = {
            1.0f, -1.0f,//R
            1.0f, -1.0f,//G
            1.0f, -1.0f//B
    };
    //描边模式 默认黑白模式
    private float[] edgeValue = {
            1.0f, -1.0f,//R
            1.0f, -1.0f,//G
            1.0f, -1.0f//B
    };
    private int program;
    private int positionLocation, texcoordLocation, mvpLocation, quatLoaction, textureLocation;// uTextureLocation, vTextureLocation;
    //      颜色模式            对比度         饱和度     亮度          分辨率     两色模式权重数组 偏移数组 给出最终求和时的加权因子(为调整亮度) //给出卷积内核中各个元素对应像素相对于待处理像素的纹理坐标偏移量 除数
    int mmUserMode, mmSaturation, mmContrast, mmBrightness, piexlSize, aryWeight, aryOffset, scaleFactor, offsetBase, colorLocation, edgeLocation;//两色模式各种颜色切换  描边模式各种颜色切换
    private int[] textureId = {0}, uTextureId = {0}, vTextureId = {0}, texStab = {0};
    private FloatBuffer vertexLeftBuffer, vertexRightBuffer, texcoordBuffer;
    //    private boolean isReady;
    long lastMills;

    private void initProgram() {
        program = ShaderUtils.loadShader(YUVShaderCode.VERTEXCODE1, YUVShaderCode.FRAGMENTCODE_RGB);
//        isReady = false;
        lastMills = System.currentTimeMillis();
        //初始化顶点坐标 纹理坐标
        createFloatBuffers();
        Log.d("TAG", "onDrawFrame: 初始化顶点和纹理坐标:" + (System.currentTimeMillis() - lastMills));
        //绑定GLSL的位置
        initLocation();
        width = DEFAULT_WIDTH;
        height = DEFAUTL_HEIGHT;
        //初始化纹理ID
        createTextures();
        if (isDoubleEyes) {
            leftFastScale = new FastZoomScale(screenPiexlsWidth / 2, screenPiexlsHeight, 0);
            rightFastScale = new FastZoomScale(screenPiexlsWidth / 2, screenPiexlsHeight, screenPiexlsWidth / 2);
        } else {
            leftFastScale = new FastZoomScale(screenPiexlsWidth, screenPiexlsHeight, 0);
        }
        if (isDoubleEyes) {
            leftCenterScale = new CenterScale(program, screenPiexlsWidth / 2, screenPiexlsHeight, 0);
            rightCenterScale = new CenterScale(program, screenPiexlsWidth / 2, screenPiexlsHeight, screenPiexlsWidth / 2);
        } else {
            leftCenterScale = new CenterScale(program, screenPiexlsWidth, screenPiexlsHeight, 0);
        }
    }

    private void createFloatBuffers() {
        float[] leftResult;
        if (isDoubleEyes) {
            leftResult = OffsetUtils.getLeftOffsetVertex();
        } else {
            leftResult = OffsetUtils.getSingleOffsetVertex();
        }
//            vertexLeftBuffer = createFloatBuffer(VERTICES_COORS.length * 4);
        vertexLeftBuffer = createFloatBuffer(leftResult.length * 4);
        vertexLeftBuffer.put(leftResult);
        vertexLeftBuffer.position(0);
        if (isDoubleEyes) {
            float[] rightResult = OffsetUtils.getRightOffsetVertex();
//        vertexRightBuffer = createFloatBuffer(VERTICES_COORS.length * 4);
            vertexRightBuffer = createFloatBuffer(rightResult.length * 4);
            vertexRightBuffer.put(rightResult);
            vertexRightBuffer.position(0);
        }
        float[] texcoord = OffsetUtils.getTextures();
//            texcoordBuffer = createFloatBuffer(TEXTURE_COORS.length * 4);
        texcoordBuffer = createFloatBuffer(texcoord.length * 4);
        texcoordBuffer.put(texcoord);
        texcoordBuffer.position(0);
    }

    private FloatBuffer createFloatBuffer(int length) {
        ByteBuffer bb = ByteBuffer.allocateDirect(length);
        bb.order(ByteOrder.nativeOrder());
        return bb.asFloatBuffer();
    }

    private float[] mvpMatrixLeft = new float[16];
    private float[] mvpMatrixRight = new float[16];
    private float[] quatMatrix = new float[16];

    private void initLocation() {
        positionLocation = GLES20.glGetAttribLocation(program, "position");
        texcoordLocation = GLES20.glGetAttribLocation(program, "texcoord");
        mvpLocation = GLES20.glGetUniformLocation(program, "MVP");
        quatLoaction = GLES20.glGetUniformLocation(program, "quatMatrix");
        textureLocation = GLES20.glGetUniformLocation(program, "s_texture");
        Matrix.setIdentityM(mvpMatrixLeft, 0);
//            Matrix.rotateM(mvpMatrix, 0, 180, 0.0f, 0.0f, 1.0f);
        Matrix.setIdentityM(mvpMatrixRight, 0);
//            Matrix.rotateM(mvpMatrix1, 0, 180, 0.0f, 0.0f, 1.0f);
        this.mmUserMode = GLES20.glGetUniformLocation(this.program, "mUserMode");
        this.mmSaturation = GLES20.glGetUniformLocation(this.program, "mSaturation");
        this.mmContrast = GLES20.glGetUniformLocation(this.program, "mContrast");
        this.mmBrightness = GLES20.glGetUniformLocation(this.program, "mBrightness");
        this.piexlSize = GLES20.glGetUniformLocation(this.program, "piexlSize");
        this.aryWeight = GLES20.glGetUniformLocation(this.program, "aryWeight");
        this.aryOffset = GLES20.glGetUniformLocation(this.program, "aryOffset");
        this.scaleFactor = GLES20.glGetUniformLocation(this.program, "scaleFactor");
        this.offsetBase = GLES20.glGetUniformLocation(this.program, "offsetBase");
        this.colorLocation = GLES20.glGetUniformLocation(this.program, "colorValue");
        this.edgeLocation = GLES20.glGetUniformLocation(this.program, "edgeValue");

    }

    int rgbWidth, rgbHeight, uWidth, uHeight;

    private void createTextures() {
        rgbWidth = width;
        rgbHeight = height;
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glGenTextures(1, textureId, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGB, rgbWidth, rgbHeight, 0, GLES20.GL_RGB, GLES20.GL_UNSIGNED_SHORT_5_6_5,
                null);
//        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGB, rgbWidth, rgbHeight, 0, GLES20.GL_RGB, GLES20.GL_UNSIGNED_INT,
//                null);
//        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, rgbWidth, rgbHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_INT,
//                null);
    }

    /**
     * 更新y u v 到纹理
     */
    private void updateTextures() {
        rgbBuffer.position(0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGB, rgbWidth,
                rgbHeight, 0,
                GLES20.GL_RGB, GLES20.GL_UNSIGNED_SHORT_5_6_5, rgbBuffer);
//        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGB, rgbWidth,
//                rgbHeight, 0,
//                GLES20.GL_RGB, GLES20.GL_UNSIGNED_INT, rgbBuffer);
//        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, rgbWidth, rgbHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_INT,
//                rgbBuffer);

    }

    /**
     * 更新y u v 到纹理
     */
    private void updateTexturesForPhoto() {
        rgbBuffer.position(0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, rgbWidth,
                rgbHeight, 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, rgbBuffer);
//        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGB, bitmap, 0);
//        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGB, rgbWidth,
//                rgbHeight, 0,
//                GLES20.GL_RGB, GLES20.GL_UNSIGNED_INT, rgbBuffer);
//        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, rgbWidth, rgbHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_INT,
//                rgbBuffer);

    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        initProgram();
    }


    private float[] mProjectMatrix = new float[16];
    private float[] mViewMatrix = new float[16];

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
//设置透视投影
        Matrix.frustumM(mProjectMatrix, 0, -1, 1, -1, 1, 18, 20);
    }

    boolean beginCountTime = false;
    double lastSecond;
    double curSecond;
    int frameNumber = 0;
    int curCountNumber;
    double[] countFrames = new double[60];
    double useTime;

    private void FrameListener1() {
        if (!beginCountTime) {
            frameNumber = 0;
            beginCountTime = !beginCountTime;
            curSecond = System.currentTimeMillis();
        }
        if (beginCountTime) {
            lastSecond = System.currentTimeMillis();
            if (lastSecond - curSecond < 1000)
                frameNumber++;
            else {
                Log.d("frameTest", frameNumber + "帧");
                beginCountTime = false;
                if (countFrames.length - 1 > curCountNumber) {
                    curCountNumber++;
                } else {
                    double temp = 0;
                    for (int i = 0; i < countFrames.length; i++) {
                        temp += countFrames[i];
                    }
                    Log.d("frameTest", "avgFrame :" + temp / countFrames.length + "帧");
                    curCountNumber = 0;
                }
                countFrames[curCountNumber] = frameNumber;
            }
        }


    }

    private long currentMills;

    @Override
    public void onDrawFrame(GL10 gl) {
        synchronized (this) {
//            Log.d("TAG", "onDrawFrame: startDtaw");
            if (isNoData) {
                GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
                return;
            } else {
                GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            }
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
//            if (!isReady) return;
            if (freezeMode != 1 && isStop) return;
            if (rgbBuffer == null) return;
            GLES20.glUseProgram(program);
//            FrameListener1();
//            currentMills = System.currentTimeMillis();
//            GLES20.glViewport(0, 0, 1200, 1200);

//            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
//            GLES20.glDisable(GLES20.GL_CULL_FACE);
            // enable face culling feature
//        GLES20.glEnable(GLES20.GL_CULL_FACE);
            // specify which faces to not draw
//        GLES20.glCullFace(GLES20.GL_FRONT);
            if (isPhotoView) {
                updateTexturesForPhoto();
            } else {
                updateTextures();
            }
            GLES20.glUniform1i(textureLocation, 0);
//            GLES20.glUniform1i(uTextureLocation, 1);
//            GLES20.glUniform1i(vTextureLocation, 2);

//            GLES20.glEnableVertexAttribArray(positionLocation);
//            GLES20.glVertexAttribPointer(positionLocation, 2, GL_FLOAT, false, 8, vertexLeftBuffer);
            GLES20.glUniform1i(this.mmUserMode, userModeValue);
            GLES20.glUniform1f(this.mmSaturation, saturationValue);
            GLES20.glUniform1f(this.mmContrast, contrastValue);
            GLES20.glUniform1f(this.mmBrightness, brightnessValue);
            GLES20.glUniform2f(this.piexlSize, screenPiexlsWidth, screenPiexlsHeight);
            GLES20.glUniform1fv(this.aryWeight, 9, weights, 0);//float
            GLES20.glUniform2fv(this.aryOffset, 9, offsets, 0);//vec2
            GLES20.glUniform1f(this.scaleFactor, 3.0f);//这个值用于调节检测到边缘线的深度
            GLES20.glUniform1f(this.offsetBase, 256.0f);////这个值用于调节偏移颜色 不能为0
            GLES20.glUniform2fv(this.colorLocation, 3, colorValue, 0);//vec2 设置两色模式的颜色值
            GLES20.glUniform2fv(this.edgeLocation, 3, edgeValue, 0);//vec2 设置描边模式的颜色值
            Matrix.setIdentityM(quatMatrix, 0);
            if ((isPhotoView || freezeMode == 1) && quats != null) {
                if (isResetCenter) {
                    isResetCenter = false;
                    quatsX = quats[1];
                    quatsY = quats[2];
                    quatsZ = quats[3];
                }
                //设置相机位置 eye（相机位置） 要和 center（观察中心） 位置相同，图像才不会变形 相机Z方向偏移要在投影变换的near和far之间才能看到图像
                Matrix.setLookAtM(mViewMatrix, 0, (quatsY - quats[2]) * 2f, (-quatsX + quats[1]) * 2f, 18f, (quatsY - quats[2]) * 2f, (-quatsX + quats[1]) * 2f, 0f, 0f, 1f, 0.0f);
                //计算变换矩阵
                Matrix.multiplyMM(this.quatMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);
            }
            GLES20.glUniformMatrix4fv(this.quatLoaction, 1, false, this.quatMatrix, 0);
            GLES20.glVertexAttribPointer(texcoordLocation, 2, GL_FLOAT, false, 8, texcoordBuffer);
            GLES20.glEnableVertexAttribArray(texcoordLocation);
            if (isDoubleEyes) {
                GLES20.glViewport(0, 0, screenPiexlsWidth / 2, screenPiexlsHeight);
            } else {
                GLES20.glViewport(0, 0, screenPiexlsWidth, screenPiexlsHeight);
            }
            GLES20.glEnableVertexAttribArray(positionLocation);
            GLES20.glVertexAttribPointer(positionLocation, 2, GL_FLOAT, false, 8, vertexLeftBuffer);
            GLES20.glUniformMatrix4fv(this.mvpLocation, 1, false, this.mvpMatrixLeft, 0);
//            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
            for (int i = 0; i < contsize; i++) {
                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, i * contsize * 2, contsize * 2);
            }
            if (isDoubleEyes) {
                GLES20.glViewport(screenPiexlsWidth / 2, 0, screenPiexlsWidth / 2, screenPiexlsHeight);
                GLES20.glEnableVertexAttribArray(positionLocation);
                GLES20.glVertexAttribPointer(positionLocation, 2, GL_FLOAT, false, 8, vertexRightBuffer);
                GLES20.glUniformMatrix4fv(this.mvpLocation, 1, false, this.mvpMatrixRight, 0);
                for (int i = 0; i < contsize; i++) {
                    GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, i * contsize * 2, contsize * 2);
                }
            }
//            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
            if (fastScaleTag == 1 && getScale() > 1) {
                if (leftFastScale != null) {
                    leftFastScale.draw();
                }
                if (rightFastScale != null) {
                    rightFastScale.draw();
                }
            }
            if (centerScaleTag == 1) {
                if (leftCenterScale != null) {
                    leftCenterScale.draw(-1, mvpMatrixLeft, null);
                }
                if (rightCenterScale != null) {
                    rightCenterScale.draw(-1, mvpMatrixRight, null);
                }
            }
//            Log.d("TAG", "onDrawFrame: ++" + (System.currentTimeMillis() - currentMills));
        }
    }

    int contsize = OffsetUtils.constSize;

    // Blur权重数组 通过修改该数组来变更过滤算法
    private float[] weights = {
            0f, 1f, 0f,
            1f, -4f, 1f,
            0f, 1f, 0f
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


    private float scale = 1.0f;
    private int userModeValue = 0;
    private int freezeMode = 0;
    private int freezeStart = 0;
    private boolean isDoubleEyes = true;
    private float baseDoubleEyesScale = 1.0f;
    private float baseSingleEyeScale = 1.0f;
    private int singleLeftX = 0, singleLeftY = 0, singleRightX = 0, singleRightY = 0, currentIpd = 62;
    private float singleLeftScale = 1.0f, singleRightScale = 1.0f;
    private float doubleEyeScale = 1.0f;

    public void setFreezeMode() {
        Log.d("TAG", "setFreezeMode: 定格");
        this.freezeMode = 1;
        freezeStart = 0;
        isResetCenter = true;
    }

    public void quitFreezeMode() {
        this.freezeMode = 0;
        setScale(this.scale);
    }

    /**
     * 设置颜色模式
     *
     * @param userMode
     */
    public void setUserMode(int userMode) {
        this.userModeValue = userMode;
        if (isPhotoView) {
            this.glSurfaceView.requestRender();
        }
    }

    public int getUserMode() {
        return userModeValue;
    }


    /**
     * 设置两色模式的 颜色显示
     *
     * @param colorValue
     */
    public void setColorValue(float[] colorValue) {
        this.colorValue = colorValue;
    }

    /**
     * 设置描边模式的颜色显示
     *
     * @param edgeValue
     */
    public void setEdgeValue(float[] edgeValue) {
        this.edgeValue = edgeValue;
    }

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


    private boolean isStop = false;

    /**
     * 关闭预览
     */
    public void stopPreview() {
        isStop = true;
    }

    /**
     * 开启预览
     */
    public void startPreview() {
        isStop = false;
    }

    private ByteBuffer rgbBuffer, uBuffer, vBuffer, currentBuffer;
    private byte[] lastY, lastU, lastV;

    public void updateImage(byte[] rgb, byte[] u, byte[] v) {
//        FrameListener1();
        if (rgb != null) {
            rgbBuffer = ByteBuffer.wrap(rgb);
//            uBuffer = ByteBuffer.wrap(u);
//            vBuffer = ByteBuffer.wrap(v);
        }
        this.glSurfaceView.requestRender();
    }

    public void updateImage(ByteBuffer frame) {
        FrameListener1();
        if (frame != null) {
            rgbBuffer = frame;
//            uBuffer = ByteBuffer.wrap(u);
//            vBuffer = ByteBuffer.wrap(v);
        } else {
            rgbBuffer = null;
        }
        this.glSurfaceView.requestRender();
    }


    /**
     * 双眼同步缩放
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
        if (fastScaleTag == 1) {
            fastScale = scale;
            this.scale = 1.0f;
        }
        setDoubleEyeScale(doubleEyeScale);
    }

    public float getScale() {
        if (fastScaleTag == 1) {
            return this.fastScale;
        }
        return this.scale;
    }


    /**
     * 设定管状视野
     *
     * @param scale
     */
    public void setDoubleEyeScale(float scale) {
        if (scale < 0.50 || scale > 1.00) {
            return;
        }
        doubleEyeScale = scale;
        setLeftSubScale(singleLeftScale);
    }

    /**
     * @return
     */
    public float getDoubleEyeScale() {
        return doubleEyeScale;
    }

    /**
     * 计算最终的缩放值
     *
     * @param scale
     * @return
     */
    protected float getEyeScale(float scale) {
        float result = 1.0f;
        float scaleOffset = MathUtils.floatSub(scale, 0.5f);
        float doubleScaleOffset = MathUtils.floatSub(doubleEyeScale, 0.5f);
        result = MathUtils.floatMultiply(scaleOffset, doubleScaleOffset);
        result = MathUtils.floatMultiply(2.0f, result);
        result = MathUtils.floatAdd(0.5f, result);
        Log.d("TAG", "getEyeScale: " + result);
        return result;
    }


    /**
     * 左眼单眼缩放
     *
     * @param scale
     */
    public void setLeftSubScale(float scale) {
        if (scale < 0.5f || scale > 1.0f) {
            return;
        }
        this.singleLeftScale = scale;
        if (this.onEyesChangeListener != null) {
            this.onEyesChangeListener.onLeftScaleChange(this.singleLeftScale);
        }
        setRightSubScale(singleRightScale);
    }

    public float getLeftScale() {
        return singleLeftScale;
    }

    /**
     * 右眼单眼缩放
     *
     * @param scale
     */
    public void setRightSubScale(float scale) {
        if (scale < 0.5f || scale > 1.0f) {
            return;
        }
        this.singleRightScale = scale;
        if (this.onEyesChangeListener != null) {
            this.onEyesChangeListener.onRightScaleChange(this.singleRightScale);
        }
        setLeftEyeOffset(singleLeftX, singleLeftY);
    }

    public float getRightScale() {
        return singleRightScale;
    }


    /**
     * 左眼单眼偏移
     *
     * @param x
     * @param y
     */
    public void setLeftEyeOffset(int x, int y) {
        this.singleLeftX = x;
        this.singleLeftY = y;
        if (this.singleLeftX > 20) {
            this.singleLeftX = 20;
        } else if (this.singleLeftX < -20) {
            this.singleLeftX = -20;
        }
        if (this.singleLeftY > 20) {
            this.singleLeftY = 20;
        } else if (this.singleLeftY < -20) {
            this.singleLeftY = -20;
        }
        setRightEyeOffset(singleRightX, singleRightY);
    }

    public String getLeftEyeOffset() {
        return singleLeftX + "," + singleLeftY;
    }

    /**
     * 右眼单眼偏移
     *
     * @param x
     * @param y
     */
    public void setRightEyeOffset(int x, int y) {
        this.singleRightX = x;
        this.singleRightY = y;
        if (this.singleRightX > 20) {
            this.singleRightX = 20;
        } else if (this.singleRightX < -20) {
            this.singleRightX = -20;
        }
        if (this.singleRightY > 20) {
            this.singleRightY = 20;
        } else if (this.singleRightY < -20) {
            this.singleRightY = -20;
        }
        setIpd(this.currentIpd);
    }

    public String getRightEyeOffset() {
        return singleRightX + "," + singleRightY;
    }

    private float xtValue = 2f, ytValue = 0.667f * 2;

    /**
     * 设定瞳距
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
        float baseScale = 1.0f;
        if (isDoubleEyes) {
            baseScale = MathUtils.floatMultiply(getScale(), baseDoubleEyesScale);
        } else {
            baseScale = MathUtils.floatMultiply(getScale(), baseSingleEyeScale);
        }
        float leftScale = 1.0f;
        float rightScale = 1.0f;
        float tempScale = 1.0f;
        if (fastScaleTag == 1) {
            if (isDoubleEyes) {
                tempScale = MathUtils.floatMultiply(1.0f, baseDoubleEyesScale);
            } else {
                tempScale = MathUtils.floatMultiply(1.0f, baseSingleEyeScale);
            }
            leftScale = MathUtils.floatMultiply(tempScale, getEyeScale(this.singleLeftScale));
            rightScale = MathUtils.floatMultiply(tempScale, getEyeScale(this.singleRightScale));
        } else {
            leftScale = MathUtils.floatMultiply(baseScale, getEyeScale(this.singleLeftScale));
            rightScale = MathUtils.floatMultiply(baseScale, getEyeScale(this.singleRightScale));
        }
        Log.d("TAG", "onIpdChange: 缩放比例：left:" + leftScale);
        Log.d("TAG", "onIpdChange: 缩放比例：right:" + rightScale);
        float translateIpd = MathUtils.floatDiv(intOffset, screenWidth, 5);
        Log.d("TAG", "onIpdChange: 基础尺寸：" + screenWidth);
        Log.d("TAG", "onIpdChange: 基础尺寸YYYY：" + screenHeight);
        Log.d("TAG", "onIpdChange: 瞳距平移：" + translateIpd);
        float leftX = MathUtils.floatDiv(singleLeftX, screenWidth, 5);
        float leftY = MathUtils.floatDiv(singleLeftY, screenHeight, 5);
        float rightX = MathUtils.floatDiv(singleRightX, screenWidth, 5);
        float rightY = MathUtils.floatDiv(singleRightY, screenHeight, 5);
        float[] mvpMatrixLeft = new float[16];
        float[] mvpMatrixRight = new float[16];
        Matrix.setIdentityM(mvpMatrixLeft, 0);
        Matrix.setIdentityM(mvpMatrixRight, 0);
        if (!isPhotoView) {
            if (freezeMode == 1) {
                if (freezeScale >= 2) {
                    Matrix.rotateM(mvpMatrixLeft, 0, 180.0f, 0.0f, 0.0f, 1.0f);
                    Matrix.rotateM(mvpMatrixRight, 0, 180.0f, 0.0f, 0.0f, 1.0f);
                }
            } else {
                if (getScale() >= 2) {
                    Matrix.rotateM(mvpMatrixLeft, 0, 180.0f, 0.0f, 0.0f, 1.0f);
                    Matrix.rotateM(mvpMatrixRight, 0, 180.0f, 0.0f, 0.0f, 1.0f);
                }
            }
        }
        Matrix.scaleM(mvpMatrixLeft, 0, leftScale, leftScale, 1.0f);
        Matrix.scaleM(mvpMatrixRight, 0, rightScale, rightScale, 1.0f);
        if (onEyesChangeListener != null) {
            this.onEyesChangeListener.onIpdChange(translateIpd / xtValue);
            this.onEyesChangeListener.onLeftEyeOffsetChange(leftX / xtValue, leftY / (1.5f * ytValue));
            this.onEyesChangeListener.onRightEyeOffsetChange(rightX / xtValue, rightY / (1.5f * ytValue));
        }
        Matrix.translateM(mvpMatrixLeft, 0, -translateIpd, 0.0f, 0.0f);
        Matrix.translateM(mvpMatrixLeft, 0, leftX, leftY, 0.0f);
        Matrix.translateM(mvpMatrixRight, 0, translateIpd, 0.0f, 0.0f);
        Matrix.translateM(mvpMatrixRight, 0, rightX, rightY, 0.0f);
        if (fastScaleTag == 1) {
            if (leftFastScale != null) {
                leftFastScale.updateParams(baseScale, this.singleLeftScale, leftX, leftY, translateIpd);
            }
            if (rightFastScale != null) {
                rightFastScale.updateParams(baseScale, this.singleRightScale, rightX, rightY, translateIpd);
            }
        }
        if (leftCenterScale != null) {
            leftCenterScale.updateParams(baseScale, this.singleLeftScale, leftX, leftY, translateIpd);
        }
        if (rightCenterScale != null) {
            rightCenterScale.updateParams(baseScale, this.singleRightScale, rightX, rightY, translateIpd);
        }
        this.mvpMatrixLeft = mvpMatrixLeft;
        this.mvpMatrixRight = mvpMatrixRight;
        if (isPhotoView) {
            this.glSurfaceView.requestRender();
        }
    }


    /**
     * 重置瞳距
     */
    public void resetIpd() {
        setIpd(62);
    }


    /**
     * 眼部调节 的监听回调 ui随动
     */
    private OnEyesChangeListener onEyesChangeListener;

    public void setOnEyesChangeListener(OnEyesChangeListener onEyesChangeListener) {
        this.onEyesChangeListener = onEyesChangeListener;
    }


    /**
     * 设置中心放大
     *
     * @param centerTag
     */
    public void setCenterScaleTag(int centerTag) {
        this.centerScaleTag = centerTag;
        if (this.centerScaleTag == 1) {
            setScale(this.scale);
        }
    }

    public int getCenterScaleTag() {
        return centerScaleTag;
    }

    /**
     * 中心放大 0 关闭  1 开启
     */
    private int centerScaleTag = 0;
    /**
     * 快速放大 0，快速缩小 1
     */
    private int fastScaleTag = 0;
    private float fastScale = 1.0f;

    /**
     * 设置快速放大，快速缩小
     *
     * @param fastTag
     */
    public void setFastScaleTag(int fastTag) {
        this.fastScaleTag = fastTag;
        if (this.fastScaleTag == 1) {
            setScale(this.scale);
        } else {
            setScale(this.fastScale);
        }
    }

    public int getFastScaleTag() {
        return fastScaleTag;
    }


    private boolean isPhotoView = false;
    private boolean isResetCenter = false;
    private float photoScale;
    private boolean isLoaded = false;
    private ByteBuffer photoBuffer;
    private Bitmap bitmap;

    /**
     * 显示图片
     *
     * @param bitmap
     */
    public void setPhotoView(Bitmap bitmap) {
        isLoaded = false;
        isPhotoView = true;
        //相册模式下直接阻断摄像头图像显示
        blockDisplay(System.currentTimeMillis());
        width = bitmap.getWidth();
        height = bitmap.getHeight();
        createTextures();
        int bytes = bitmap.getByteCount();
        ByteBuffer buffer = ByteBuffer.allocate(bytes);
        bitmap.copyPixelsToBuffer(buffer); //Move the byte data to the buffer
//        byte[] data = buffer.array();
        isLoaded = true;
        photoScale = getScale();
        setScale(this.scale);
        updateImage(buffer);
        isResetCenter = true;
        bitmap.recycle();

    }

    /**
     * 退出相册模式
     */
    public void quitPhotoView() {
        width = DEFAULT_WIDTH;
        height = DEFAUTL_HEIGHT;
        isLoaded = false;
        createTextures();
        isPhotoView = false;
        //退出相册模式时延迟1s后在显示图像
        blockDisplay(30);
        setScale(this.scale);
    }


    int ysize = width * height;
    int uvsize = width * height / 4;
    //    byte[] y = new byte[ysize];
//    byte[] u = new byte[uvsize];
//    byte[] v = new byte[uvsize];
//    byte[] currentRGB;
    private float freezeScale = 1.0f;

    public void onPreviewByteBuffer(ByteBuffer frame) {
        isNoData = false;
//        if (getScale() >= 2) return;
        if (isPhotoView) return;
        blockingTimes--;
        if (blockingTimes > 0) {
//            if (isNoData)
//                updateImage(null);
            return;
        }
        if (frame != null) {
//            currentMills = System.currentTimeMillis();
//            System.arraycopy(yuv, 0, y, 0, ysize);
//            System.arraycopy(yuv, ysize, u, 0, uvsize);
//            System.arraycopy(yuv, ysize + uvsize, v, 0, uvsize);
//            updateImage(y, u, v);
            if (freezeMode == 1) {
                if (freezeStart == 0) {
                    freezeStart = 1;
//                    lastY = new byte[y.length];
//                    lastU = new byte[u.length];
//                    lastV = new byte[v.length];
//                    System.arraycopy(y, 0, lastY, 0, y.length);
//                    System.arraycopy(u, 0, lastU, 0, u.length);
//                    System.arraycopy(v, 0, lastV, 0, v.length);
                    byte[] data = new byte[frame.capacity()];
                    frame.get(data);
                    currentBuffer = ByteBuffer.wrap(data);
                    freezeScale = getScale();
                }
                updateImage(currentBuffer);
                if (this.onFrameListener != null) {
                    this.onFrameListener.onByteBufferFrame(currentBuffer, width, height, What.RGB565, freezeScale);
                }
            } else {
                currentBuffer = frame;
//                updateImage(rgb, null, null);
                updateImage(frame);
                if (this.onFrameListener != null) {
                    this.onFrameListener.onByteBufferFrame(currentBuffer, width, height, What.RGB565, getScale());
                }
            }

//            Log.d("TAG", "saveBitmap: +++++" + (System.currentTimeMillis() - currentMills));
        } else {
            updateImage(null);
        }
    }


    public SurfaceTexture getSurfaceTexture() {
        SurfaceTexture surfaceTexture = new SurfaceTexture(0);
        surfaceTexture.setDefaultBufferSize(DEFAULT_WIDTH, DEFAUTL_HEIGHT);
        return surfaceTexture;
    }

    public Size getDefaultSize() {
        Size size = new Size(DEFAULT_WIDTH, DEFAUTL_HEIGHT);
        return size;
    }

    private OnFrameListener onFrameListener;

    public void setOnFrameListener(OnFrameListener onFrameListener) {
        this.onFrameListener = onFrameListener;
    }

    public void resetCustomSettings() {


    }

    public void resetEyesSettings() {
        singleLeftX = 0;
        singleLeftY = 0;
        singleRightX = 0;
        singleRightY = 0;
        singleLeftScale = 1.0f;
        singleRightScale = 1.0f;
        setScale(this.scale);
    }

    public void resetCenterPosition() {
        isResetCenter = true;
    }

    float quats[];
    private float quatsX, quatsY, quatsZ;

    /**
     * 获取眼镜的四元数数据进行3dof展示
     * 原始四元数数据 w x y z
     *
     * @param quats
     */
    public void onQuaternion(float[] quats) {
        this.quats = quats;
        if (isPhotoView && isLoaded) {
            this.glSurfaceView.requestRender();
        }
    }

    boolean isNoData;

    /**
     * 阻断图像显示
     */
    public void blockDisplay() {
        isNoData = true;
        this.glSurfaceView.requestRender();
        blockDisplay(System.currentTimeMillis());
    }

    /**
     * 显示图像
     */
    public void display() {
        isNoData = false;
        blockDisplay(30);
    }


    public long blockingTimes = 0l;

    /**
     * 阻断图像显示 利用时间进行阻断，大概每秒30帧或60帧，阻断时间 几秒就乘以帧数
     *
     * @param times
     */
    public void blockDisplay(long times) {
        this.blockingTimes = times;
    }


}
