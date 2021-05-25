/*
 *  UVCCamera
 *  library and sample to access to UVC web camera on non-rooted Android device
 *
 * Copyright (c) 2014-2017 saki t_saki@serenegiant.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *  All files in the folder are under this Apache License, Version 2.0.
 *  Files in the libjpeg-turbo, libusb, libuvc, rapidjson folder
 *  may have a different license, see the respective files.
 */

package com.serenegiant.usb.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import com.jiangdg.usbcamera.utils.MathUtils;
import com.serenegiant.glutils.EGLBase;
import com.serenegiant.usb.widget.GLDrawer2D1;
//import com.serenegiant.glutils.GLDrawer2D;
import com.serenegiant.glutils.es1.GLHelper;
import com.serenegiant.usb.encoder.IVideoEncoder;
import com.serenegiant.usb.encoder.MediaEncoder;
import com.serenegiant.usb.encoder.MediaVideoEncoder;
import com.serenegiant.utils.FpsCounter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

/**
 * change the view size with keeping the specified aspect ratio.
 * if you set this view with in a FrameLayout and set property "android:layout_gravity="center",
 * you can show this view in the center of screen and keep the aspect ratio of content
 * XXX it is better that can set the aspect ratio as xml property
 */
public class UVCCameraTextureView extends AspectRatioTextureView    // API >= 14
        implements TextureView.SurfaceTextureListener, CameraViewInterface {

    private static final boolean DEBUG = true;    // TODO set false on release
    private static final String TAG = "UVCCameraTextureView";

    private boolean mHasSurface;
    private RenderHandler mRenderHandler;
    private final Object mCaptureSync = new Object();
    private Bitmap mTempBitmap;
    private boolean mReqesutCaptureStillImage;
    private Callback mCallback;
    // Camera分辨率宽度

    Context mContext;

    /**
     * for calculation of frame rate
     */
    private final FpsCounter mFpsCounter = new FpsCounter();

    public UVCCameraTextureView(final Context context) {
        this(context, null, 0);
    }

    public UVCCameraTextureView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UVCCameraTextureView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
        setSurfaceTextureListener(this);
    }
    @Override
    public void onResume() {
        if (DEBUG) Log.v(TAG, "onResume:");
        if (mHasSurface) {
            mRenderHandler = RenderHandler.createHandler(this.mContext, mFpsCounter, super.getSurfaceTexture(), getWidth(), getHeight());
        }
    }

    @Override
    public void onPause() {
        if (DEBUG) Log.v(TAG, "onPause:");
        if (mRenderHandler != null) {
            mRenderHandler.release();
            mRenderHandler = null;
        }
        if (mTempBitmap != null) {
            mTempBitmap.recycle();
            mTempBitmap = null;
        }
    }


    @Override
    public void onSurfaceTextureAvailable(final SurfaceTexture surface, final int width, final int height) {
        if (DEBUG) Log.i(TAG, "onSurfaceTextureAvailable:" + surface);
        if (mRenderHandler == null) {
            mRenderHandler = RenderHandler.createHandler(mContext, mFpsCounter, surface, width, height);
        } else {
            mRenderHandler.resize(width, height);
        }
        mHasSurface = true;
        if (mCallback != null) {
            mCallback.onSurfaceCreated(this, getSurface());
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(final SurfaceTexture surface, final int width, final int height) {
        if (DEBUG) Log.i(TAG, "onSurfaceTextureSizeChanged:" + surface);
        if (mRenderHandler != null) {
            mRenderHandler.resize(width, height);
        }
        if (mCallback != null) {
            mCallback.onSurfaceChanged(this, getSurface(), width, height);
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(final SurfaceTexture surface) {
        if (DEBUG) Log.i(TAG, "onSurfaceTextureDestroyed:" + surface);
        if (mRenderHandler != null) {
            mRenderHandler.release();
            mRenderHandler = null;
        }
        mHasSurface = false;
        if (mCallback != null) {
            mCallback.onSurfaceDestroy(this, getSurface());
        }
        if (mPreviewSurface != null) {
            mPreviewSurface.release();
            mPreviewSurface = null;
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(final SurfaceTexture surface) {
        synchronized (mCaptureSync) {
            if (mReqesutCaptureStillImage) {
                mReqesutCaptureStillImage = false;
                if (mTempBitmap == null)
                    mTempBitmap = getBitmap();
                else
                    getBitmap(mTempBitmap);
                mCaptureSync.notifyAll();
            }
        }
        Log.d(TAG, "onSurfaceTextureUpdated: 更新图像数据");
//        if (mRenderHandler != null && mRenderHandler.mThread != null)
//            mRenderHandler.mThread.onDrawFrame();
    }

    @Override
    public boolean hasSurface() {
        return mHasSurface;
    }

    /**
     * capture preview image as a bitmap
     * this method blocks current thread until bitmap is ready
     * if you call this method at almost same time from different thread,
     * the returned bitmap will be changed while you are processing the bitmap
     * (because we return same instance of bitmap on each call for memory saving)
     * if you need to call this method from multiple thread,
     * you should change this method(copy and return)
     */
    @Override
    public Bitmap captureStillImage(int width, int height) {
        synchronized (mCaptureSync) {
            mReqesutCaptureStillImage = true;
            try {
                mCaptureSync.wait();
            } catch (final InterruptedException e) {
            }
            return mTempBitmap;
        }
    }

    @Override
    public SurfaceTexture getSurfaceTexture() {
        return mRenderHandler != null ? mRenderHandler.getPreviewTexture() : null;
    }

    private Surface mPreviewSurface;

    @Override
    public Surface getSurface() {
        if (DEBUG) Log.v(TAG, "getSurface:hasSurface=" + mHasSurface);
        if (mPreviewSurface == null) {
            final SurfaceTexture st = getSurfaceTexture();
            if (st != null) {
                mPreviewSurface = new Surface(st);
            }
        }
        return mPreviewSurface;
    }

    @Override
    public void setVideoEncoder(final IVideoEncoder encoder) {
//        if (mRenderHandler != null)
//            mRenderHandler.setVideoEncoder(encoder);
    }

    @Override
    public void setCallback(final Callback callback) {
        mCallback = callback;
    }

    public void resetFps() {
        mFpsCounter.reset();
    }

    /**
     * update frame rate of image processing
     */
    public void updateFps() {
        mFpsCounter.update();
    }

    /**
     * get current frame rate of image processing
     *
     * @return
     */
    public float getFps() {
        return mFpsCounter.getFps();
    }

    /**
     * get total frame rate from start
     *
     * @return
     */
    public float getTotalFps() {
        return mFpsCounter.getTotalFps();
    }

    public void setStabOn(boolean isStab) {
        mRenderHandler.setStabOn(isStab);
    }

    public void setOnDrawFrameListener(GLDrawer2D1.OnDrawFrameListener onDrawFrameListener) {
        if (mRenderHandler != null)
            mRenderHandler.setOnDrawFrameListener(onDrawFrameListener);
    }


    /**
     * 进入定格模式
     */
    public void setFreezeMode() {
        if (mRenderHandler != null)
            mRenderHandler.setFreezeMode();
    }

    /**
     * 退出定格模式
     */
    public void quitFreezeMode() {
        mRenderHandler.quitFreezeMode();
    }

    /**
     * 设置图片显示 width=2952  height 1944
     *
     * @param bitmap
     */
    public void setPhotoView(Bitmap bitmap) {
        mRenderHandler.setPhotoView(bitmap);
    }

    /**
     * 退出图片显示
     */
    public void exitPhotoView() {
        mRenderHandler.exitPhotoView();
    }


    public int getSaturation() {
        return mRenderHandler.getSaturation();
    }

    public void setSaturation(int saturation) {
        mRenderHandler.setSaturation(saturation);
    }

    public int getContrast() {
        return mRenderHandler.getContrast();
    }

    public void setContrast(int contrast) {
        mRenderHandler.setContrast(contrast);
    }

    public int getBrightness() {
        return mRenderHandler.getBrightness();
    }

    public void setBrightness(int brightness) {
        mRenderHandler.setBrightness(brightness);
    }

    //设置显示模式
    public void setUserMode(int userMode) {
        mRenderHandler.setUserMode(userMode);
    }


    public int getMUserMode() {
        return mRenderHandler.getMUserMode();
    }

    public void resetIpd() {
        if (mRenderHandler != null)
            mRenderHandler.resetIpd();
    }

    public void resetAll() {
        if (mRenderHandler != null)
            mRenderHandler.resetAll();
    }

    public void setScale(float scale) {
        mRenderHandler.setScale(scale);
    }

    public float getScale() {
        return mRenderHandler.getScale();
    }

    public void onSensorChange(float[] quat) {
        if (mRenderHandler != null) {
            mRenderHandler.onSensorChange(quat);
        }
    }

    /**
     * 通知activity参数改变的监听
     */
    public interface ChangeStateListener {
        void changeTranslationState(int type, int offset);

        void changeScaleState(int type, float offset);
    }

    ChangeStateListener changeStateListener;

    public void setChangeStateListener(ChangeStateListener changeStateListener) {
        this.changeStateListener = changeStateListener;
    }

    /**
     * 设定瞳距
     *
     * @param offset
     */
    public void setIpd(int offset) {
        if (mRenderHandler != null) {
            mRenderHandler.setIpd(offset);
        }
    }

    /**
     * 获取当前瞳距
     *
     * @return
     */
    public int getIpd() {
        return mRenderHandler.getIpd();
    }


    public void initParams(int scale, float leftScale, float rightScale, int leftX, int leftY, int rightX, int rightY, int currentIpd) {
        if (mRenderHandler != null)
            mRenderHandler.initParams(scale, leftScale, rightScale, leftX, leftY, rightX, rightY, currentIpd);
    }

    public void setImu(float x, float y) {
        if (mRenderHandler != null)
            mRenderHandler.setImu(x, y);
    }

    public void setQuickScaleTag(int quickScaleTag) {
        if (mRenderHandler != null)
            mRenderHandler.setQuickScaleTag(quickScaleTag);
    }


    public int getQuickScaleTag() {
        return mRenderHandler.getQuickScaleTag();
    }


    public void setQuickShrink() {
        mRenderHandler.setQuickShrink();
    }

    public void quitQuickShrink() {
        mRenderHandler.quitQuickShrink();
    }

    public int getQuickShrink() {
        return mRenderHandler.getQuickShrink();
    }

    public void setOnEyesChangeListener(OnEyesChangeListener onEyesChangeListener) {
        if (mRenderHandler != null)
            mRenderHandler.setOnEyesChangeListener(onEyesChangeListener);
    }

    public void setLeftSubScale(float leftSubScale) {
        if (mRenderHandler != null)
            mRenderHandler.setLeftSubScale(leftSubScale);
    }

    public float getLeftScale() {
        return mRenderHandler.getLeftScale();
    }

    public void setRightSubScale(float rightSubScale) {
        if (mRenderHandler != null)
            mRenderHandler.setRightSubScale(rightSubScale);
    }

    public float getRightScale() {
        return mRenderHandler.getRightScale();
    }

    /**
     * 设定管状视野
     *
     * @param scale
     */
    public void setDoubleEyeScale(float scale) {
        mRenderHandler.setDoubleEyeScale(scale);
    }

    public float getDoubleEyeScale() {
        return mRenderHandler.getDoubleEyeScale();
    }

    public void setLeftEyeOffset(int leftX, int leftY) {
        if (mRenderHandler != null)
            mRenderHandler.setLeftEyeOffset(leftX, leftY);
    }

    public String getLeftEyeOffset() {
        return mRenderHandler.getLeftEyeOffset();
    }

    public void setRightEyeOffset(int rightX, int rightY) {
        if (mRenderHandler != null)
            mRenderHandler.setRightEyeOffset(rightX, rightY);
    }

    public String getRightEyeOffset() {
        return mRenderHandler.getRightEyeOffset();
    }

    /**
     * render camera frames on this view on a private thread
     *
     * @author saki
     */
    private static final class RenderHandler extends Handler
            implements SurfaceTexture.OnFrameAvailableListener {

        private static final int MSG_REQUEST_RENDER = 1;
        //        private static final int MSG_SET_ENCODER = 2;
        private static final int MSG_CREATE_SURFACE = 3;
        private static final int MSG_RESIZE = 4;
        private static final int MSG_TERMINATE = 9;

        private RenderThread mThread;
        private boolean mIsActive = true;
        private final FpsCounter mFpsCounter;

        public static final RenderHandler createHandler(Context context, final FpsCounter counter,
                                                        final SurfaceTexture surface, final int width, final int height) {

            final RenderThread thread = new RenderThread(context, counter, surface, width, height);
            thread.start();
            return thread.getHandler();
        }

        private RenderHandler(final FpsCounter counter, final RenderThread thread) {
            mThread = thread;
            mFpsCounter = counter;
        }

//        public final void setVideoEncoder(final IVideoEncoder encoder) {
//            if (DEBUG) Log.v(TAG, "setVideoEncoder:");
//            if (mIsActive)
//                sendMessage(obtainMessage(MSG_SET_ENCODER, encoder));
//        }

        public final SurfaceTexture getPreviewTexture() {
            if (DEBUG) Log.v(TAG, "getPreviewTexture:");
            if (mIsActive) {
                synchronized (mThread.mSync) {
                    sendEmptyMessage(MSG_CREATE_SURFACE);
                    try {
                        mThread.mSync.wait();
                    } catch (final InterruptedException e) {
                    }
                    return mThread.mPreviewSurface;
                }
            } else {
                return null;
            }
        }

        public void resize(final int width, final int height) {
            if (DEBUG) Log.v(TAG, "resize:");
            if (mIsActive) {
                synchronized (mThread.mSync) {
                    sendMessage(obtainMessage(MSG_RESIZE, width, height));
                    try {
                        mThread.mSync.wait();
                    } catch (final InterruptedException e) {
                    }
                }
            }
        }

        public final void release() {
            if (DEBUG) Log.v(TAG, "release:");
            if (mIsActive) {
                mIsActive = false;
                removeMessages(MSG_REQUEST_RENDER);
//                removeMessages(MSG_SET_ENCODER);
                sendEmptyMessage(MSG_TERMINATE);
            }
        }

        @Override
        public final void onFrameAvailable(final SurfaceTexture surfaceTexture) {
            Log.d(TAG, "onFrameAvailable: luoyang:" + System.currentTimeMillis());
            if (mIsActive) {
                mFpsCounter.count();
                sendEmptyMessage(MSG_REQUEST_RENDER);
//                if (mThread == null) return;
//                mThread.onDrawFrame();
            }
        }

        @Override
        public final void handleMessage(final Message msg) {
            if (mThread == null) return;
            switch (msg.what) {
                case MSG_REQUEST_RENDER:
                    mThread.onDrawFrame();
                    break;
//                case MSG_SET_ENCODER:
//                    mThread.setEncoder((MediaEncoder) msg.obj);
//                    break;
                case MSG_CREATE_SURFACE:
                    mThread.updatePreviewSurface();
                    break;
                case MSG_RESIZE:
                    mThread.resize(msg.arg1, msg.arg2);
                    break;
                case MSG_TERMINATE:
                    Looper.myLooper().quit();
                    mThread = null;
                    break;
                default:
                    super.handleMessage(msg);
            }
        }

        public void setStabOn(boolean isStab) {
            mThread.setStabOn(isStab);
        }

        public void setOnDrawFrameListener(GLDrawer2D1.OnDrawFrameListener onDrawFrameListener) {
            if (mThread != null)
                mThread.setOnDrawFrameListener(onDrawFrameListener);
        }

        /**
         * 进入定格模式
         */
        public void setFreezeMode() {
            if (mThread != null)
                mThread.setFreezeMode();
        }

        /**
         * 退出定格模式
         */
        public void quitFreezeMode() {
            mThread.quitFreezeMode();
        }

        /**
         * 设置图片显示 width=2952  height 1944
         *
         * @param bitmap
         */
        public void setPhotoView(Bitmap bitmap) {
            mThread.setPhotoView(bitmap);
        }

        /**
         * 退出图片显示
         */
        public void exitPhotoView() {
            mThread.exitPhotoView();
        }

        public int getSaturation() {
            return mThread.getSaturation();
        }

        public void setSaturation(int saturation) {
            mThread.setSaturation(saturation);
        }

        public int getContrast() {
            return mThread.getContrast();
        }

        public void setContrast(int contrast) {
            mThread.setContrast(contrast);
        }

        public int getBrightness() {
            return mThread.getBrightness();
        }

        public void setBrightness(int brightness) {
            mThread.setBrightness(brightness);
        }

        //设置显示模式
        public void setUserMode(int userMode) {
            mThread.setUserMode(userMode);
        }

        public int getMUserMode() {
            return mThread.getMUserMode();
        }


        public void resetIpd() {
            if (mThread != null)
                mThread.resetIpd();
        }

        public void resetAll() {
            if (mThread != null)
                mThread.resetAll();
        }

        public void setScale(float scale) {
            mThread.setScale(scale);
        }

        public float getScale() {
            return mThread.getScale();
        }

        /**
         * 设定瞳距
         *
         * @param offset
         */
        public void setIpd(int offset) {
            if (mThread != null) {
                mThread.setIpd(offset);
            }
        }

        /**
         * 获取当前瞳距
         *
         * @return
         */
        public int getIpd() {
            return mThread.getIpd();
        }


        public void initParams(int scale, float leftScale, float rightScale, int leftX, int leftY, int rightX, int rightY, int currentIpd) {
            if (mThread != null)
                mThread.initParams(scale, leftScale, rightScale, leftX, leftY, rightX, rightY, currentIpd);
        }

        public void setImu(float x, float y) {
            if (mThread != null)
                mThread.setImu(x, y);
        }

        public void setQuickScaleTag(int quickScaleTag) {
            if (mThread != null)
                mThread.setQuickScaleTag(quickScaleTag);
        }


        public int getQuickScaleTag() {
            return mThread.getQuickScaleTag();
        }


        public void setQuickShrink() {
            mThread.setQuickShrink();
        }

        public void quitQuickShrink() {
            mThread.quitQuickShrink();
        }

        public int getQuickShrink() {
            return mThread.getQuickShrink();
        }

        public void setOnEyesChangeListener(OnEyesChangeListener onEyesChangeListener) {
            if (mThread != null)
                mThread.setOnEyesChangeListener(onEyesChangeListener);
        }

        public void setLeftSubScale(float leftSubScale) {
            if (mThread != null)
                mThread.setLeftSubScale(leftSubScale);
        }

        public float getLeftScale() {
            return mThread.getLeftScale();
        }

        public void setRightSubScale(float rightSubScale) {
            if (mThread != null)
                mThread.setRightSubScale(rightSubScale);
        }

        public float getRightScale() {
            return mThread.getRightScale();
        }


        /**
         * 设定管状视野
         *
         * @param scale
         */
        public void setDoubleEyeScale(float scale) {
            mThread.setDoubleEyeScale(scale);
        }

        public float getDoubleEyeScale() {
            return mThread.getDoubleEyeScale();
        }

        public void setLeftEyeOffset(int leftX, int leftY) {
            if (mThread != null)
                mThread.setLeftEyeOffset(leftX, leftY);
        }

        public String getLeftEyeOffset() {
            return mThread.getLeftEyeOffset();
        }

        public void setRightEyeOffset(int rightX, int rightY) {
            if (mThread != null)
                mThread.setRightEyeOffset(rightX, rightY);
        }

        public String getRightEyeOffset() {
            return mThread.getRightEyeOffset();
        }

        public void onSensorChange(float[] quat) {
            if (mThread != null) {
                mThread.onSensorChange(quat);
            }
        }


        private static final class RenderThread extends Thread {
            private final Object mSync = new Object();
            private final SurfaceTexture mSurface;
            private RenderHandler mHandler;
            private EGLBase mEgl;
            /**
             * IEglSurface instance related to this TextureView
             */
            private EGLBase.IEglSurface mEglSurface;
            private GLDrawer2D1 mDrawer;
            private int mTexId = -1;
            /**
             * SurfaceTexture instance to receive video images
             */
            private SurfaceTexture mPreviewSurface;
            private final float[] mStMatrix = new float[16];
            //            private MediaEncoder mEncoder;
            private int mViewWidth, mViewHeight;
            private final FpsCounter mFpsCounter;
            Context mContext;

            /**
             * constructor
             *
             * @param surface: drawing surface came from TexureView
             */
            public RenderThread(Context context, final FpsCounter fpsCounter, final SurfaceTexture surface, final int width, final int height) {
                mFpsCounter = fpsCounter;
                mSurface = surface;
                mViewWidth = width;
                mViewHeight = height;
                mContext = context;
                setName("RenderThread");
            }

            public final RenderHandler getHandler() {
                if (DEBUG) Log.v(TAG, "RenderThread#getHandler:");
                synchronized (mSync) {
                    // create rendering thread
                    if (mHandler == null)
                        try {
                            mSync.wait();
                        } catch (final InterruptedException e) {
                        }
                }
                return mHandler;
            }

            public void resize(final int width, final int height) {
                if (((width > 0) && (width != mViewWidth)) || ((height > 0) && (height != mViewHeight))) {
                    mViewWidth = width;
                    mViewHeight = height;
                    updatePreviewSurface();
                } else {
                    synchronized (mSync) {
                        mSync.notifyAll();
                    }
                }
            }

            public final void updatePreviewSurface() {
                if (DEBUG) Log.i(TAG, "RenderThread#updatePreviewSurface:");
                synchronized (mSync) {
                    if (mPreviewSurface != null) {
                        if (DEBUG) Log.d(TAG, "updatePreviewSurface:release mPreviewSurface");
                        mPreviewSurface.setOnFrameAvailableListener(null);
                        mPreviewSurface.release();
                        mPreviewSurface = null;
                    }
//                    mEglSurface.makeCurrent();
                    if (mTexId >= 0) {
                        mDrawer.deleteTex(mTexId);
                    }
                    // create texture and SurfaceTexture for input from camera
                    mTexId = mDrawer.initTex();
                    if (DEBUG) Log.v(TAG, "updatePreviewSurface:tex_id=" + mTexId);
                    mPreviewSurface = new SurfaceTexture(mTexId);
                    mPreviewSurface.setDefaultBufferSize(mViewWidth, mViewHeight);
                    mPreviewSurface.setOnFrameAvailableListener(mHandler);
                    // notify to caller thread that previewSurface is ready
                    mSync.notifyAll();
                }
            }


//            public final void setEncoder(final MediaEncoder encoder) {
//                if (DEBUG) Log.v(TAG, "RenderThread#setEncoder:encoder=" + encoder);
//                if (encoder != null && (encoder instanceof MediaVideoEncoder)) {
//                    ((MediaVideoEncoder) encoder).setEglContext(mEglSurface.getContext(), mTexId);
//                }
//                mEncoder = encoder;
//            }


            /*
             * Now you can get frame data as ByteBuffer(as YUV/RGB565/RGBX/NV21 pixel format) using IFrameCallback interface
             * with UVCCamera#setFrameCallback instead of using following code samples.
             */
/*			// for part1
 			private static final int BUF_NUM = 1;
			private static final int BUF_STRIDE = 640 * 480;
			private static final int BUF_SIZE = BUF_STRIDE * BUF_NUM;
			int cnt = 0;
			int offset = 0;
			final int pixels[] = new int[BUF_SIZE];
			final IntBuffer buffer = IntBuffer.wrap(pixels); */
/*			// for part2
			private ByteBuffer buf = ByteBuffer.allocateDirect(640 * 480 * 4);
 */
            private int i;

            private long currentMills;
            /**
             * draw a frame (and request to draw for video capturing if it is necessary)
             */
            public final void onDrawFrame() {
                Log.d(TAG, "onDrawFrame1: luoyang:" + i++);
                Log.d(TAG, "onDrawFrame1: luoyang:" + System.currentTimeMillis());
                currentMills=System.currentTimeMillis();
                mEglSurface.makeCurrent();
                // update texture(came from camera)
                Log.d(TAG, "onDrawFrame2: times0:" + (System.currentTimeMillis()-currentMills));
                mPreviewSurface.updateTexImage();

                // get texture matrix
//                mPreviewSurface.getTransformMatrix(mStMatrix);
                // notify video encoder if it exist
//                if (mEncoder != null) {
//                    // notify to capturing thread that the camera frame is available.
//                    if (mEncoder instanceof MediaVideoEncoder)
//                        ((MediaVideoEncoder) mEncoder).frameAvailableSoon(mStMatrix);
//                    else
//                        mEncoder.frameAvailableSoon();
//                }
                // draw to preview screen
                Log.d(TAG, "onDrawFrame2: times 1:" + (System.currentTimeMillis()-currentMills));
                mDrawer.draw(mTexId, mStMatrix, 0);
                Log.d(TAG, "onDrawFrame2: times: 2" + (System.currentTimeMillis()-currentMills));
                mEglSurface.swap();
                Log.d(TAG, "onDrawFrame2: times: 3     " + (System.currentTimeMillis()-currentMills));
                Log.d(TAG, "onDrawFrame2: luoyang:" + System.currentTimeMillis());
/*				// sample code to read pixels into Buffer and save as a Bitmap (part1)
				buffer.position(offset);
				GLES20.glReadPixels(0, 0, 640, 480, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);
				if (++cnt == 100) { // save as a Bitmap, only once on this sample code
					// if you save every frame as a Bitmap, app will crash by Out of Memory exception...
					Log.i(TAG, "Capture image using glReadPixels:offset=" + offset);
					final Bitmap bitmap = createBitmap(pixels,offset,  640, 480);
					final File outputFile = MediaMuxerWrapper.getCaptureFile(Environment.DIRECTORY_DCIM, ".png");
					try {
						final BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(outputFile));
						try {
							try {
								bitmap.compress(CompressFormat.PNG, 100, os);
								os.flush();
								bitmap.recycle();
							} catch (IOException e) {
							}
						} finally {
							os.close();
						}
					} catch (FileNotFoundException e) {
					} catch (IOException e) {
					}
				}
				offset = (offset + BUF_STRIDE) % BUF_SIZE;
*/
/*				// sample code to read pixels into Buffer and save as a Bitmap (part2)
		        buf.order(ByteOrder.LITTLE_ENDIAN);	// it is enough to call this only once.
		        GLES20.glReadPixels(0, 0, 640, 480, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buf);
		        buf.rewind();
				if (++cnt == 100) {	// save as a Bitmap, only once on this sample code
					// if you save every frame as a Bitmap, app will crash by Out of Memory exception...
					final File outputFile = MediaMuxerWrapper.getCaptureFile(Environment.DIRECTORY_DCIM, ".png");
			        BufferedOutputStream os = null;
					try {
				        try {
				            os = new BufferedOutputStream(new FileOutputStream(outputFile));
				            Bitmap bmp = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888);
				            bmp.copyPixelsFromBuffer(buf);
				            bmp.compress(Bitmap.CompressFormat.PNG, 90, os);
				            bmp.recycle();
				        } finally {
				            if (os != null) os.close();
				        }
					} catch (FileNotFoundException e) {
					} catch (IOException e) {
					}
				}
*/
            }

/*			// sample code to read pixels into IntBuffer and save as a Bitmap (part1)
			private static Bitmap createBitmap(final int[] pixels, final int offset, final int width, final int height) {
				final Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
				paint.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix(new float[] {
						0, 0, 1, 0, 0,
						0, 1, 0, 0, 0,
						1, 0, 0, 0, 0,
						0, 0, 0, 1, 0
					})));

				final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
				final Canvas canvas = new Canvas(bitmap);

				final Matrix matrix = new Matrix();
				matrix.postScale(1.0f, -1.0f);
				matrix.postTranslate(0, height);
				canvas.concat(matrix);

				canvas.drawBitmap(pixels, offset, width, 0, 0, width, height, false, paint);

				return bitmap;
			} */

            @Override
            public final void run() {
                Log.d(TAG, getName() + " started");
                init();
                Looper.prepare();
                synchronized (mSync) {
                    mHandler = new RenderHandler(mFpsCounter, this);
                    mSync.notify();
                }

                Looper.loop();

                Log.d(TAG, getName() + " finishing");
                release();
                synchronized (mSync) {
                    mHandler = null;
                    mSync.notify();
                }
            }

            private final void init() {
                if (DEBUG) Log.v(TAG, "RenderThread#init:");
                // create EGLContext for this thread
                mEgl = EGLBase.createFrom(null, false, false);
                mEglSurface = mEgl.createFromSurface(mSurface);
                mEglSurface.makeCurrent();
                // create drawing object
                mDrawer = new GLDrawer2D1(true, this.mContext);
            }

            private final void release() {
                if (DEBUG) Log.v(TAG, "RenderThread#release:");
                if (mDrawer != null) {
                    mDrawer.release();
                    mDrawer = null;
                }
                if (mPreviewSurface != null) {
                    mPreviewSurface.release();
                    mPreviewSurface = null;
                }
                if (mTexId >= 0) {
                    GLHelper.deleteTex(mTexId);
                    mTexId = -1;
                }
                if (mEglSurface != null) {
                    mEglSurface.release();
                    mEglSurface = null;
                }
                if (mEgl != null) {
                    mEgl.release();
                    mEgl = null;
                }
            }

            public void setStabOn(boolean isStab) {
                mDrawer.setStabOn(isStab);
            }

            public void setOnDrawFrameListener(GLDrawer2D1.OnDrawFrameListener onDrawFrameListener) {
                if (mDrawer != null)
                    mDrawer.setOnDrawFrameListener(onDrawFrameListener);
            }

            /**
             * 进入定格模式
             */
            public void setFreezeMode() {
                if (mDrawer != null)
                    mDrawer.setFreezeMode();
            }

            /**
             * 退出定格模式
             */
            public void quitFreezeMode() {
                mDrawer.quitFreezeMode();
            }

            /**
             * 设置图片显示 width=2952  height 1944
             *
             * @param bitmap
             */
            public void setPhotoView(Bitmap bitmap) {
                mDrawer.setPhotoView(bitmap);
            }

            /**
             * 退出图片显示
             */
            public void exitPhotoView() {
                mDrawer.exitPhotoView();
            }

            public int getSaturation() {
                return mDrawer.getSaturation();
            }

            public void setSaturation(int saturation) {
                mDrawer.setSaturation(saturation);
            }

            public int getContrast() {
                return mDrawer.getContrast();
            }

            public void setContrast(int contrast) {
                mDrawer.setContrast(contrast);
            }

            public int getBrightness() {
                return mDrawer.getBrightness();
            }

            public void setBrightness(int brightness) {
                mDrawer.setBrightness(brightness);
            }

            //设置显示模式
            public void setUserMode(int userMode) {
                mDrawer.setUserMode(userMode);
            }

            public int getMUserMode() {
                return mDrawer.getMUserMode();
            }

            public void setScale(float scale) {
                if (mDrawer != null)
                    mDrawer.setScale(scale);
            }

            public void resetIpd() {
                if (mDrawer != null)
                    mDrawer.resetIpd();
            }

            public void resetAll() {
                if (mDrawer != null)
                    mDrawer.resetAll();
            }

            public float getScale() {
                return mDrawer.getScale();
            }

            public void setIpd(int offset) {
                if (mDrawer != null) {
                    mDrawer.setIpd(offset);
                }
            }

            public int getIpd() {
                return mDrawer.getIpd();
            }

            public void initParams(int scale, float leftScale, float rightScale, int leftX, int leftY, int rightX, int rightY, int currentIpd) {
                if (mDrawer != null)
                    mDrawer.initParams(scale, leftScale, rightScale, leftX, leftY, rightX, rightY, currentIpd);
            }

            public void setImu(float x, float y) {
                if (mDrawer != null)
                    mDrawer.setImu(x, y);
            }

            public void setQuickScaleTag(int quickScaleTag) {
                if (mDrawer != null)
                    mDrawer.setQuickScaleTag(quickScaleTag);
            }

            public int getQuickScaleTag() {
                return mDrawer.getQuickScaleTag();
            }

            public void setQuickShrink() {
                mDrawer.setQuickShrink();
            }

            public void quitQuickShrink() {
                mDrawer.quitQuickShrink();
            }

            public int getQuickShrink() {
                return mDrawer.getQuickShrink();
            }

            public void setOnEyesChangeListener(OnEyesChangeListener onEyesChangeListener) {
                if (mDrawer != null)
                    mDrawer.setOnEyesChangeListener(onEyesChangeListener);
            }

            public void setLeftSubScale(float leftSubScale) {
                if (mDrawer != null)
                    mDrawer.setLeftSubScale(leftSubScale);
            }

            public float getLeftScale() {
                return mDrawer.getLeftScale();
            }

            public void setRightSubScale(float rightSubScale) {
                if (mDrawer != null)
                    mDrawer.setRightSubScale(rightSubScale);
            }

            public float getRightScale() {
                return mDrawer.getRightScale();
            }

            /**
             * 设定管状视野
             *
             * @param scale
             */
            public void setDoubleEyeScale(float scale) {
                mDrawer.setDoubleEyeScale(scale);
            }

            public float getDoubleEyeScale() {
                return mDrawer.getDoubleEyeScale();
            }

            public void setLeftEyeOffset(int leftX, int leftY) {
                if (mDrawer != null)
                    mDrawer.setLeftEyeOffset(leftX, leftY);
            }

            public String getLeftEyeOffset() {
                return mDrawer.getLeftEyeOffset();
            }

            public void setRightEyeOffset(int rightX, int rightY) {
                if (mDrawer != null)
                    mDrawer.setRightEyeOffset(rightX, rightY);
            }

            public String getRightEyeOffset() {
                return mDrawer.getRightEyeOffset();
            }

            public void onSensorChange(float[] quat) {
                if (mDrawer != null) {
                    mDrawer.onSensorChange(quat);
                }
            }
        }

    }
}
