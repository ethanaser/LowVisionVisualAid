package com.cnsj.neptunglasses.manager;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.cnsj.neptunglasses.constant.What;
import com.cnsj.neptunglasses.view.gl.CameraInterface;
import com.serenegiant.usb.IFrameCallback;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;

import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;


/**
 * UVCCamera 相机类
 */
public class CNSJCamera {
    private Context context;
    private USBMonitor.UsbControlBlock controlBlock;//USB摄像头相关硬件信息
    private int width = 1920, height = 1080;//相机分辨率
    private int format = UVCCamera.FRAME_FORMAT_MJPEG;//默认的图像格式
    private int minFps = 1, maxFps = 61;//最高帧率支持到60帧
    private float mBandwidthFactor = UVCCamera.DEFAULT_BANDWIDTH;//带宽设定0.0f-1.0f
    //    private float mBandwidthFactor = 0.5f;
    private CameraInterface cameraView;
    private CameraThread cameraThread;
    private UVCCamera uvcCamera;
    private boolean isOpen = false;
    private boolean isPreviewing = false;
    private String message;
    private Handler mainHandler;//将消息发送回主线程的handler
    private volatile boolean isRun = true;


    public CNSJCamera(Context context, Handler handler) {
        this.context = context;
        this.mainHandler = handler;
    }

    /**
     * 判断初始化状态
     *
     * @return
     */
    public boolean isInit() {
        return cameraThread != null && isRun;
    }

    /**
     * 初始化操作USB相机线程
     */
    public void init() {
        cameraThread = new CameraThread();
        cameraThread.start();
        isRun = true;
        isOpen = false;
        isPreviewing = false;
    }


    /**
     * 设置返回到主线程的消息通知
     *
     * @param message
     * @param successTag
     */
    public void sendMainMessage(String message, int successTag) {
        this.message = message;
        Message success = Message.obtain();
        success.what = successTag;
        success.arg1 = controlBlock.getVenderId();
        success.arg2 = controlBlock.getProductId();
        success.obj = this.message;
        mainHandler.sendMessage(success);
    }


    /**
     * 发送相机操作的消息
     *
     * @param action
     */
    public void sendCameraMessage(int action) {
//        if (cameraHandler != null) {
//            cameraHandler.sendEmptyMessage(action);
//        }
        if (actionQueue != null) {
            isRun = true;
            actionQueue.offer(action);
        }
    }

    public USBMonitor.UsbControlBlock getControlBlock() {
        return controlBlock;
    }

    /**
     * 打开相机
     */
    public void openCamera() {
//        cameraThread.openCamera();
        sendCameraMessage(CameraThread.OPEN_ACTION);
        if (cameraView != null) {
            cameraView.openCamera();
        }
    }

    /**
     * 关闭相机
     */
    public void closeCamera() {
        if (isPreviewing) stopPreview();
        sendCameraMessage(CameraThread.CLOSE_ACTION);
//        cameraThread.closeCamera();
        if (cameraView != null)
            cameraView.closeCamera();
        isOpen = false;
    }

    public SurfaceTexture cameraSurfaceTexture;

    public void startPreview(CameraInterface cameraView) {
        if (this.cameraView != cameraView) {
            this.cameraView = cameraView;
            cameraSurfaceTexture = this.cameraView.getSurfaceTexture();
        }
        sendCameraMessage(CameraThread.START_PREVIEW_ACTION);
//        synchronized (cameraThread){

//            cameraThread.startPreview();
        if (cameraView != null)
            this.cameraView.startPreview();
//        }

    }

    public void stopPreview() {
//        synchronized (cameraThread){
        if (isPreviewing) {
            sendCameraMessage(CameraThread.STOP_PREVICEW_ACTION);
            cameraThread.stopPreview();
            if (cameraView != null)
                cameraView.stopPreview();
        }
//        }

    }

    /**
     * 释放掉相机操作
     */
    public void release() {
        if (isOpen) closeCamera();
        sendCameraMessage(CameraThread.RELASE_ACTION);
//        cameraThread.release();
        if (cameraView != null)
            cameraView.release();
    }

    public CameraInterface getCameraView() {
        return cameraView;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public boolean isPreviewing() {
        return isPreviewing;
    }

    public void setControlBlock(USBMonitor.UsbControlBlock ctrlBlock) {
        this.controlBlock = ctrlBlock;
    }

    public void setDefaultSize(int width, int height) {
        this.width = width;
        this.height = height;
    }


    private ArrayBlockingQueue<Integer> actionQueue = new ArrayBlockingQueue<>(3);

    public class CameraThread extends Thread {
        public static final int RELASE_ACTION = 7;//释放
        private Object sync = new Object();//用于线程同步操作
        public final static int OPEN_ACTION = 1;//打开相机
        public final static int CLOSE_ACTION = 2;//关闭相机
        public final static int START_PREVIEW_ACTION = 3;//启动预览
        public final static int STOP_PREVICEW_ACTION = 4;//停止预览
        public final static int START_FOCUS_ACTION = 5;//启动自动对焦
        public final static int RESET_MODE_ACTION = 6;


        public CameraThread() {
//            if (cameraHandler == null) {
//                cameraHandler = new MyCameraHandler();
//                isRun = true;
//            }
        }

        @Override
        public void run() {
            super.run();
            while (isRun) {
                Log.d("actionQueue", "run: startaaaaaaa");
                int nextAction;
                try {
                    nextAction = actionQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    continue;
                }
                Log.d("actionQueue", "run: dododododododdoodododdodododo : " + nextAction);
                switch (nextAction) {
                    case CameraThread.OPEN_ACTION:
                        openCamera();
                        break;
                    case CameraThread.CLOSE_ACTION:
                        closeCamera();
                        break;
                    case CameraThread.START_PREVIEW_ACTION:
                        startPreview();
                        break;
                    case CameraThread.STOP_PREVICEW_ACTION:
                        stopPreview();
                        break;
                    case CameraThread.START_FOCUS_ACTION:
                        startAutoFocus();
                        break;
                    case CameraThread.RESET_MODE_ACTION:
                        resetParams();
                        break;
                    case CameraThread.RELASE_ACTION:
                        release();
                        break;
                }
            }
//            Looper.prepare();
//            if (cameraHandler == null) {
//                cameraHandler = new MyCameraHandler();
//                isRun = true;
//            }
//            Log.d("TAG", "run: dooooooooooooooooooooooooooooo");
//            Looper.loop();
//            if (cameraHandler != null) {
//                isRun = false;
//            }
        }

        /**
         * 打开相机
         */
        public void openCamera() {
            synchronized (sync) {
                Log.d("TAG", "openCamera: " + controlBlock.getProductId());
                if (isOpen) return;
                if (uvcCamera == null) {
                    uvcCamera = new UVCCamera();
                }
                try {
                    uvcCamera.open(getControlBlock());
                    isOpen = true;
//                    if (goToStartPreview) startPreview();
                    sendMainMessage("相机打开成功", What.OPEN_SUCCESS);
                } catch (Exception e) {
                    isOpen = false;
                    sendMainMessage(e.getLocalizedMessage(), What.OPEN_FAULIRE);
                }
            }
        }

        /**
         * 关闭相机
         */
        public void closeCamera() {
            synchronized (sync) {
                Log.d("TAG", "closeCamera: " + controlBlock.getProductId());
                if (uvcCamera == null) return;
                uvcCamera.destroy();
                uvcCamera = null;
                isPreviewing = false;
                isOpen = false;
                sendMainMessage("close camera succes", What.CLOSE_SUCCESS);
            }
        }

//        private boolean goToStartPreview;


        /**
         * 打开预览
         */
        public void startPreview() {
            Log.d("TAG", "startPreview: " + controlBlock.getProductId());
            Log.d("TAG", "startPreview: " + isOpen);
//            goToStartPreview = true;
            if (!isOpen) return;
            synchronized (sync) {
//                goToStartPreview = false;
                if (uvcCamera == null || isPreviewing) return;
                try {
                    uvcCamera.setPreviewSize(width, height, minFps, maxFps, format, mBandwidthFactor);
                } catch (Exception e) {
                    isPreviewing = false;
                    sendMainMessage(e.getLocalizedMessage(), What.PREVIEW_FAULIRE);
                    return;
                }
                Log.d("TAG", "onSurfaceCreated: surfacetexture:" + cameraSurfaceTexture);
                uvcCamera.setFrameCallback(mIFrameCallback, UVCCamera.PIXEL_FORMAT_RGB565);//rgb565
//                uvcCamera.setFrameCallback(mIFrameCallback, UVCCamera.PIXEL_FORMAT_RGBX);
//                uvcCamera.setFrameCallback(mIFrameCallback, UVCCamera.PIXEL_FORMAT_YUV420SP);//YUV420
                uvcCamera.setPreviewTexture(cameraSurfaceTexture);
//                uvcCamera.setPreviewDisplay(new Surface(cameraView.getSurfaceTexture()));
                uvcCamera.startPreview();
                uvcCamera.updateCameraParams();
//                setZoom(100);
//                setPan(100);
//                setTitl(80);
//                Log.d("TAG", "startPreview: zoom:" + getZoom());
                uvcCamera.resetBrightness();
                uvcCamera.resetSaturation();
                uvcCamera.resetContrast();
                uvcCamera.setAutoFocus(true);
                isPreviewing = true;
                sendMainMessage("start preview success", What.PREVIEW_SUCCES);
            }
        }

        /**
         * zoom=0~60
         * value:0-100
         * 设定方式为百分比设定
         *
         * @param value
         */
        public void setZoom(int value) {
            uvcCamera.setZoom(value);
        }

        public int getZoom() {
            return uvcCamera.getZoom();
        }

        /**
         * 当zoom为60时显示的3840*2160的图像，pan为X方向的偏移值
         * -2000 ~ 2000
         * -1000 ~ 1000有效值超过1000就无效了 step=3600 每变换一个值，变化一个step 默认最好为0
         * 设定值0~100 百分比设定
         *
         * @param panValue
         */
        public void setPan(int panValue) {
            if (panValue < 0) {
                panValue = 0;
            } else if (panValue > 100) {
                panValue = 100;
            }
            uvcCamera.setPan(panValue);
        }

        /**
         * 当zoom为60时显示的3840*2160的图像，titl为Y方向的偏移值
         * -900 ~ 900
         * -900 ~ 900有效值超过1000就无效了 step=3600 每变换一个值，变化一个step 默认最好为0
         * 设定值0~100 百分比设定
         *
         * @param titlValue
         */
        public void setTitl(int titlValue) {
            if (titlValue < 0) {
                titlValue = 0;
            } else if (titlValue > 100) {
                titlValue = 100;
            }
            uvcCamera.setTilt(titlValue);
        }


        IFrameCallback mIFrameCallback = new IFrameCallback() {
            byte[] yuv = new byte[width * height * 2];//RGB565数据

            @Override
            public void onFrame(ByteBuffer frame) {
//                int len = frame.capacity();
//                byte[] yuv = new byte[len];
                if (!isPreviewing) return;
                frame.get(yuv);
                //双摄同时打开，根据摄像头ID将yuv数据传递至不同接口
                if (cameraView != null) {
//                    if (getControlBlock() != null && getControlBlock().getProductId() == 0x5802)
                    cameraView.onPreviewByteBuffer(ByteBuffer.wrap(yuv));
//                    else
//                        cameraView.onPreviewByteBuffer1(ByteBuffer.wrap(yuv));
//                    Log.d("TAG", "luoyanggg: length:"+yuv.length);
//                    Log.d("TAG", "onFrame: 1920x1080:"+(1920*1080));
//                    mainHandler.removeMessages(200);
//                    mainHandler.sendEmptyMessageDelayed(200, 15000);
                }
            }
        };


        /**
         * 关闭预览
         */
        public void stopPreview() {
            synchronized (sync) {
                Log.d("TAG", "stopPreview: " + controlBlock.getProductId());
                if (uvcCamera == null || !isPreviewing) return;
                isPreviewing = false;
                uvcCamera.stopPreview();
                uvcCamera.setFrameCallback(null, 0);
                sendMainMessage("stop preview succes", What.STOP_SUCCES);
//                if (goToStartPreview) startPreview();
            }
        }

        /**
         * 开启自动对焦
         */
        public void startAutoFocus() {

        }

        /**
         * 重置图像饱和度等参数
         */
        public void resetParams() {

        }

        /**
         * 释放掉线程
         */
        public void release() {
            synchronized (sync) {
                isRun = false;
//                Looper.myLooper().quit();//退出handler消息
            }
        }
    }

}
