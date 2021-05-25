//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.serenegiant.usb.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Environment;
import android.util.Log;

import com.jiangdg.usbcamera.utils.MathUtils;
import com.serenegiant.glutils.GLHelper;
import com.serenegiant.glutils.IDrawer2D;
import com.serenegiant.glutils.IDrawer2dES2;
import com.serenegiant.glutils.ITexture;
import com.serenegiant.glutils.TextureOffscreen;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES32.GL_CLAMP_TO_BORDER;

public class GLDrawer2D1 implements IDrawer2dES2 {

    private static final float[] VERTICES = new float[]{
            1.0F, 0.5F,
            0.0F, 0.5F,
            1.0F, -0.5F,
            1.0F, -0.5F,
            0.0F, 0.5F,
            0.0f,-0.5f,
            0.0f,0.5f,
            -1.0f,0.5f,
            0.0f,-0.5f,
            0.0f,-0.5f,
            -1.0f,0.5f,
            -1.0f,-0.5f
    };
    private static final float[] TEXCOORD = new float[]{
            1.0F, 1.0F,
            0.0F, 1.0F,
            1.0F, 0.0F,
            1.0F, 0.0F,
            0.0f,1.0f,
            0.0f,0.0f,
            1.0F, 1.0F,
            0.0F, 1.0F,
            1.0F, 0.0F,
            1.0F, 0.0F,
            0.0f,1.0f,
            0.0f,0.0f
    };

    private FloatBuffer pTexCoord;
    private FloatBuffer vertices;
    private final int mTexTarget;
    private int hProgram;
    int maPositionLoc;
    int maTextureCoordLoc;
    int muMVPMatrixLoc;
    int muTexMatrixLoc;
    int uQuatMatrix;
    private final float[] mMvpMatrix;
    int width = 1920, height = 1080, mEdgeMode = 1;
    Context mContext;
    private int[] texFBO = {0}, texDraw = {0}, texCamera = {0};
    private int[] FBO = {0};
    private OpenCVView openCVView;


    private SingleEye leftEye, rightEye;
    private boolean isDoubleEyes;

    public GLDrawer2D1(boolean isOES, Context context) {
        this.mContext = context;
        this.mMvpMatrix = new float[16];
        this.mTexTarget = isOES ? '赥' : 3553;
//        initTexOES(texCamera);
        initFbo();
        this.pTexCoord = ByteBuffer.allocateDirect(TEXCOORD.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.pTexCoord.put(TEXCOORD);
        this.pTexCoord.flip();
//        float[] testVertex = initVertex1(33);
        this.vertices = ByteBuffer.allocateDirect(VERTICES.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.vertices.put(VERTICES);
        this.vertices.flip();
        Matrix.setIdentityM(this.mMvpMatrix, 0);
        Matrix.rotateM(this.mMvpMatrix,0,180,0.0f,1.0f,0.0f);
        resetShader();
        isDoubleEyes = true;
        leftEye = new SingleEye(mContext, 1200, 1200, 0, isDoubleEyes);
        rightEye = new SingleEye(mContext, 1200, 1200, 1200, isDoubleEyes);
        openCVView = new OpenCVView();
        GLES20.glUseProgram(this.hProgram);
    }

    public void resetShader() {
        this.release();
        if (this.isOES()) {
            this.hProgram = GLHelper.loadShader("#version 100\nuniform mat4 uMVPMatrix;" +
                    "\nuniform mat4 uTexMatrix;" +
                    "\nuniform mat4 uQuatMatrix;" +
                    "\nattribute highp vec4 aPosition;" +
                    "\nattribute highp vec4 aTextureCoord;" +
                    "\nvarying highp vec2 vTextureCoord;" +
                    "\nvoid main() {" +
                    "\n    gl_Position = uMVPMatrix * aPosition;" +
                    "\n vTextureCoord = (uQuatMatrix * aTextureCoord).xy;\n}\n", "#version 100\n#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nuniform samplerExternalOES sTexture;\nvarying highp vec2 vTextureCoord;\nvoid main() {\n  gl_FragColor = texture2D(sTexture, vTextureCoord);\n}");
        } else {
            this.hProgram = GLHelper.loadShader(shadercode.OriginVSS, shadercode.OriginFSS2);

        }
        this.init();
    }

    private void init() {
//        GLES20.glUseProgram(this.hProgram);
        this.maPositionLoc = GLES20.glGetAttribLocation(this.hProgram, "aPosition");
        this.maTextureCoordLoc = GLES20.glGetAttribLocation(this.hProgram, "aTextureCoord");
        this.muMVPMatrixLoc = GLES20.glGetUniformLocation(this.hProgram, "uMVPMatrix");
        this.muTexMatrixLoc = GLES20.glGetUniformLocation(this.hProgram, "uTexMatrix");
        this.uQuatMatrix = GLES20.glGetUniformLocation(this.hProgram, "uQuatMatrix");
//        this.mmUserMode = GLES20.glGetUniformLocation(this.hProgram, "mUserMode");
        GLES20.glLinkProgram(this.hProgram);
        GLES20.glUniformMatrix4fv(this.muMVPMatrixLoc, 1, false, this.mMvpMatrix, 0);
        Matrix.setIdentityM(this.quatMatrix, 0);
//        GLES20.glUniformMatrix4fv(this.muTexMatrixLoc, 1, false, this.mMvpMatrix, 0);
    }


    private int freezeMode = 0;
    private int freezeStart = 0;

    /**
     * 进入定格模式
     */
    public void setFreezeMode() {
        freezeMode = 1;
        freezeStart = 0;
        isResetPosition = true;
    }


    /**
     * 退出定格模式
     */
    public void quitFreezeMode() {
        freezeMode = 0;
    }

    private boolean isPhotoMode = false;
    private Bitmap bitmap;


    /**
     * 设置图片显示
     *
     * @param bitmap
     */
    public void setPhotoView(Bitmap bitmap) {
        isPhotoMode = bitmap != null;
        if (isPhotoMode) {
            this.bitmap = bitmap;
            isResetPosition = true;
        }
//        freezeMode = 1;
//        freezeStart = 0;
//        byte tmp[] = new byte[(bitmap.getWidth() * bitmap.getHeight() * 4)];
//        freezeImage = ByteBuffer.wrap(tmp);
//        freezeImage.position(0);
//        bitmap.copyPixelsToBuffer(freezeImage);//默认都是3264*2448
//        freezeImage.position(0);
//        isResetPosition = true;
    }

    /**
     * 退出图片显示
     */
    public void exitPhotoView() {
//        quitFreezeMode();
        if (isPhotoMode) {
            isPhotoMode = false;
            if (this.bitmap != null) {
                this.bitmap.recycle();
                this.bitmap = null;
            }
        }
    }

    public void release() {
        if (this.hProgram >= 0) {
            GLES20.glDeleteProgram(this.hProgram);
        }

        this.hProgram = -1;
    }

    public boolean isOES() {
        Log.d("TAG", "lylyisOES: " + this.mTexTarget);
        return this.mTexTarget == 36197;
    }

    public float[] getMvpMatrix() {
        return null;
    }

    public IDrawer2D setMvpMatrix(float[] matrix, int offset) {
        return this;
    }

    public void getMvpMatrix(float[] matrix, int offset) {
    }

    private void initFbo() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glGenTextures(1, texDraw, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texDraw[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glGenTextures(1, texFBO, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texFBO[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        //==========
        //纹理环绕
        //==========
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
        //==========
        //纹理过滤
        //==========
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        //==========
        //init FBO
        //==========
        GLES20.glGenFramebuffers(1, FBO, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, FBO[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, texFBO[0], 0);
        int FBOstatus = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (FBOstatus != GLES20.GL_FRAMEBUFFER_COMPLETE)
            Log.e("LOGTAG", "initFBO failed, status: " + FBOstatus);
    }


    private long currentMills;
    private void drawTex(int cameraTex, int fbo) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo);

        GLES20.glVertexAttribPointer(this.maPositionLoc, 2, GL_FLOAT, false, 8, this.vertices);
        GLES20.glVertexAttribPointer(this.maTextureCoordLoc, 2, GL_FLOAT, false, 8, this.pTexCoord);
        GLES20.glEnableVertexAttribArray(this.maPositionLoc);
        GLES20.glEnableVertexAttribArray(this.maTextureCoordLoc);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, cameraTex);
        currentMills=System.currentTimeMillis();
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 12);
        Log.d("TAG", "onDrawFrame2: times1: 0     " + (System.currentTimeMillis()-currentMills));
    }

    private ByteBuffer freezeImage;
    private float xReset = 0.0f, yReset = 0.0f;
    private boolean isResetPosition = false;
    private float[] quatMatrix = new float[16];
    boolean isStab = true;

    public synchronized void draw(int texId, float[] tex_matrix, int offset) {
        if (this.hProgram >= 0) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            GLES20.glClearColor(0f, 0f, 0f, 1f);

            Matrix.setIdentityM(this.quatMatrix, 0);
//            if (freezeMode == 1 || isPhotoMode) {
//                if (isResetPosition) {
//                    isResetPosition = false;
//                    xReset = quat[0];
//                    yReset = quat[1];
//                }
//                Matrix.translateM(this.quatMatrix, 0, (xReset - quat[0]) / 90.f, (yReset - quat[1]) / 90.f, 0.0f);
//            }
            GLES20.glUniformMatrix4fv(this.uQuatMatrix, 1, false, this.quatMatrix, 0);
//            if (tex_matrix != null) {
//                GLES20.glUniformMatrix4fv(this.muTexMatrixLoc, 1, false, tex_matrix, 0);
//            }
            GLES20.glUniformMatrix4fv(this.muMVPMatrixLoc, 1, false, this.mMvpMatrix, 0);
            drawTex(texId, 0);
            boolean isCV = false;
//            if (freezeMode == 1) {
//                if (freezeStart == 0) {
//                    freezeStart = 1;
//                    byte[] b = new byte[width * height * 4];
//                    freezeImage = ByteBuffer.wrap(b);
//                    freezeImage.position(0);
//                    GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, freezeImage);
//                }
//                freezeImage.position(0);
//                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texFBO[0]);
//                GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, width, height,
//                        GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, freezeImage);
////                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
//            }
//            if (isPhotoMode && this.bitmap != null) {
//                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texFBO[0]);
////                GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, width, height,
////                        GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, freezeImage);
//                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
//            }
//            if (onDrawFrameListener != null) {
//                ByteBuffer buffer = ByteBuffer.wrap(b);
//                buffer.position(0);
//                GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);
//                try {
//                    onDrawFrameListener.onDrawFrame(buffer, width, height);
//                } catch (NullPointerException e) {
//                    e.printStackTrace();
//                }
//            }
//            currnetMills=System.currentTimeMillis();
//            if (freezeMode != 1) {
//                if (isStab && leftEye.getScale() > 5) {
//                    isCV = true;
//                    openCVView.processFrame(texDraw[0], width, height, isStab, 0);
//                } else {
//                    isCV = false;
////                    openCVView.resetFrame();
//                }
//            }
//            Log.d("TAG", "drawwwwwwwwwwwww: "+(System.currentTimeMillis()-currnetMills));
//            leftEye.draw(texId, tex_matrix, quatMatrix);
//            rightEye.draw(texId, tex_matrix, quatMatrix);
//            if (isCV) {
//                leftEye.draw(texDraw[0], tex_matrix, quatMatrix);
//                rightEye.draw(texDraw[0], tex_matrix, quatMatrix);
//            }
//            else {
//                leftEye.draw(texFBO[0], tex_matrix, quatMatrix);
//                rightEye.draw(texFBO[0], tex_matrix, quatMatrix);
//            }

        }
    }

    /**
     * 设定防抖开关
     *
     * @param isStab
     */
    public void setStabOn(boolean isStab) {
        this.isStab = isStab;
    }

//    long currnetMills=0l;

    byte[] b = new byte[width * height * 4];
    private OnDrawFrameListener onDrawFrameListener;

    public void setOnDrawFrameListener(OnDrawFrameListener onDrawFrameListener) {
        this.onDrawFrameListener = onDrawFrameListener;
    }

    public interface OnDrawFrameListener {
        void onDrawFrame(ByteBuffer byteBuffer, int width, int height);
    }

    /**
     * 根据时间戳保存图片
     *
     * @param bitmap
     */
    private void saveBitmap(Bitmap bitmap) {
        StringBuffer sb = new StringBuffer();
        sb.append(Environment.getExternalStorageDirectory().getAbsolutePath());
        sb.append("/");
        sb.append("DCIM/freeze_image");//目前将定格图片保存在这个目录中。
        File dir = new File(sb.toString());
        if (!dir.exists()) {
            dir.mkdir();
        }
        sb.append("/");
        sb.append("cnsj");
        sb.append(".jpg");
        File file = new File(sb.toString());
        if (file.exists()) {
            file.delete();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            //文件输出流
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            sb.setLength(0);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void draw(ITexture texture) {
        this.draw(texture.getTexture(), texture.getTexMatrix(), 0);
    }

    public void draw(TextureOffscreen offscreen) {
        this.draw(offscreen.getTexture(), offscreen.getTexMatrix(), 0);
    }

    public int initTex() {
//        return texCamera[0];
        return GLHelper.initTex(this.mTexTarget, 9728);
    }

    public void deleteTex(int hTex) {
//        if (texCamera.length == 1) {
//            GLES20.glDeleteTextures(1, texCamera, 0);
//        }
        GLHelper.deleteTex(hTex);
    }


    public synchronized void updateShader(String vs, String fs) {
        this.release();
        this.hProgram = GLHelper.loadShader(vs, fs);
        this.init();
    }

    public void updateShader(String fs) {
        this.updateShader(shadercode.OriginVSS, fs);
    }


    public int glGetAttribLocation(String name) {
        GLES20.glUseProgram(this.hProgram);
        return GLES20.glGetAttribLocation(this.hProgram, name);
    }

    public int glGetUniformLocation(String name) {
        GLES20.glUseProgram(this.hProgram);
        return GLES20.glGetUniformLocation(this.hProgram, name);
    }

    public void glUseProgram() {
        GLES20.glUseProgram(this.hProgram);
    }


    public int getSaturation() {
        return leftEye.getSaturation();
    }

    /**
     * 饱和度范围1-5  1 1.4 1.8 2.2 2.6
     *
     * @param saturation
     */
    public void setSaturation(int saturation) {
        leftEye.setSaturation(saturation);
        rightEye.setSaturation(saturation);
    }

    public int getContrast() {
        return leftEye.getContrast();
    }

    /**
     * 对比度范围1-5 0 0.2 0.4 0.6 0.8
     *
     * @param contrast
     */
    public void setContrast(int contrast) {
        leftEye.setContrast(contrast);
        rightEye.setContrast(contrast);
    }

    public int getBrightness() {
        return leftEye.getBrightness();
    }

    /**
     * 亮度范围1-5 0 0.2 0.4 0.6 0.8
     *
     * @param brightness
     */
    public void setBrightness(int brightness) {
        leftEye.setBrightness(brightness);
        rightEye.setBrightness(brightness);
    }

    public void setUserMode(int userMode) {
        leftEye.setUserMode(userMode);
        rightEye.setUserMode(userMode);
    }

    public int getMUserMode() {
        return leftEye.getMUserMode();
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
    public void initParams(int scale,
                           float leftScale, float rightScale, int leftX, int leftY, int rightX, int rightY, int currentIpd) {
        leftEye.initParams(scale, leftScale, leftX, leftY, currentIpd);
        rightEye.initParams(scale, rightScale, rightX, rightY, currentIpd);
    }

    public void setImu(float x, float y) {

    }


    /**
     * 中心放大
     *
     * @param quickScaleTag
     */
    public void setQuickScaleTag(int quickScaleTag) {
        leftEye.setCenterScaleTag(quickScaleTag);
        rightEye.setCenterScaleTag(quickScaleTag);
    }


    public int getQuickScaleTag() {
        return leftEye.getCenterScaleTag();
    }

    public void setQuickShrink() {
        leftEye.setFastScaleTag(1);
        rightEye.setFastScaleTag(1);
    }

    public void quitQuickShrink() {
        leftEye.setFastScaleTag(0);
        rightEye.setFastScaleTag(0);
    }

    public int getQuickShrink() {
        return leftEye.getFastScaleTag();
    }


    public void resetIpd() {
        leftEye.resetIpd();
        rightEye.resetIpd();
    }

    public void resetAll() {
        leftEye.resetAll();
        rightEye.resetAll();
    }

    /**
     * 设定放大倍数
     *
     * @param scale
     */
    public void setScale(float scale) {
        leftEye.setScale(scale);
        rightEye.setScale(scale);
    }


    public void setOnEyesChangeListener(OnEyesChangeListener onEyesChangeListener) {
        leftEye.setOnEyesChangeListener(onEyesChangeListener);
        rightEye.setOnEyesChangeListener(onEyesChangeListener);

    }


    /**
     * 左眼偏移
     *
     * @param leftX
     * @param leftY
     */
    public void setLeftEyeOffset(int leftX, int leftY) {
        leftEye.setSingleEyeOffset(leftX, leftY);
    }

    public String getLeftEyeOffset() {
        return leftEye.getSingleEyeOffset();
    }

    /**
     * 右眼偏移
     *
     * @param rightX
     * @param rightY
     */
    public void setRightEyeOffset(int rightX, int rightY) {
        rightEye.setSingleEyeOffset(rightX, rightY);
    }

    public String getRightEyeOffset() {
        return rightEye.getSingleEyeOffset();
    }


    /**
     * 瞳距偏移
     *
     * @param offset
     */
    public void setIpd(int offset) {
        leftEye.setIpd(offset);
        rightEye.setIpd(offset);
    }

    /**
     * 获取当前瞳距
     *
     * @return
     */
    public int getIpd() {
        return leftEye.getIpd();
    }


    /**
     * 设定偏移和缩放
     */

    public float getScale() {
        return leftEye.getScale();
    }

    private float leftEyeScale = 1.0f;
    private float rightEyeScale = 1.0f;
    private float doubleEyeScale = 1.0f;


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
        setLeftSubScale(leftEyeScale);
        setRightSubScale(rightEyeScale);
    }

    public float getDoubleEyeScale() {
        return doubleEyeScale;
    }

    /**
     * 左眼缩放
     *
     * @param leftSubScale
     */
    public void setLeftSubScale(float leftSubScale) {
        if (leftSubScale < 0.50 || leftSubScale > 1.00) {
            return;
        }
        leftEyeScale = leftSubScale;
        leftEye.setSubScale(getEyeScale(leftSubScale));
    }

    public float getLeftScale() {
        return leftEyeScale;
    }

    /**
     * 右眼缩放
     *
     * @param rightSubScale
     */
    public void setRightSubScale(float rightSubScale) {
        if (rightSubScale < 0.50 || rightSubScale > 1.00) {
            return;
        }
        rightEyeScale = rightSubScale;
        rightEye.setSubScale(getEyeScale(rightSubScale));
    }

    public float getRightScale() {
        return rightEyeScale;
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


    private float[] quat = {0, 0, 0};

    /**
     * 获取传感器的四元数数据
     *
     * @param quat
     */
    public void onSensorChange(float[] quat) {
        this.quat = quat;
    }
}

