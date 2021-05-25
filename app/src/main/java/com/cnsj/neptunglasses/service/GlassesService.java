package com.cnsj.neptunglasses.service;

import android.app.Service;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.cnsj.neptunglasses.app.CuiNiaoApp;
import com.cnsj.neptunglasses.sensor.NeptungSensor;
import com.serenegiant.usb.USBMonitor;

import java.util.Observer;
import java.util.Vector;

/**
 * 连接头显的服务
 * 用于监测USB连接和断开
 * 用于接收头显传感器数据
 */
public class GlassesService extends Service {
    public NeptungSensor sensor;
    public static final String TAG = GlassesService.class.getSimpleName();
    private USBMonitor.OnDeviceConnectListener onDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        @Override
        public void onAttach(UsbDevice device) {
            Log.d(TAG, "onAttachDev: " + device.getProductId() + " V:" + device.getVendorId() + " PATH:" + device.getDeviceName());
            usbMonitor.requestPermission(device);
        }

        @Override
        public void onDettach(UsbDevice device) {
            Log.d(TAG, "onDettach: dddddddddddddisconnect: " + device.getProductId() + " V:" + device.getVendorId() + " PATH:" + device.getDeviceName());
            if (binder != null) {
                binder.update(MyBinder.DIS_CONNECT, device);
            }
        }

        @Override
        public void onConnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock, boolean createNew) {
            if (device.getProductId() == 770) {
                if (binder != null) {
                    binder.registerSensor();
                    binder.update(MyBinder.VR_770, ctrlBlock);
                }

            }
            if (device.getVendorId() == 0x0bda && device.getProductId() == 0x5875
                    || device.getVendorId() == 0x0bda && device.getProductId() == 0x5801
                    || device.getVendorId() == 0x0bdb && device.getProductId() == 0x5801
                    || device.getVendorId() == 0x1bcf && device.getProductId() == 0x5801
                    || device.getVendorId() == 0x1bcf && device.getProductId() == 0x28c4
                    || device.getVendorId() == 1287 && device.getProductId() == 1409 && device.getDeviceName().equals("/dev/bus/usb/002/004")
                    || device.getVendorId() == 1287 && device.getProductId() == 1409 && device.getDeviceName().equals("/dev/bus/usb/001/004")
            ) {
                if (binder != null) {
                    binder.update(MyBinder.CONNECT_5801, ctrlBlock);
                }
            }
            if (device.getVendorId() == 0x0bda && device.getProductId() == 0x5802
                    || device.getVendorId() == 0x0bdb && device.getProductId() == 0x5802
                    || device.getVendorId() == 0x1bcf && device.getProductId() == 0x5802
                    || device.getVendorId() == 0x1bcf && device.getProductId() == 0x0215
                    || device.getVendorId() == 1287 && device.getProductId() == 1409 && device.getDeviceName().equals("/dev/bus/usb/002/003")
                    || device.getVendorId() == 1287 && device.getProductId() == 1409 && device.getDeviceName().equals("/dev/bus/usb/001/003")
            ) {
                if (binder != null) {
                    binder.update(MyBinder.CONNECT_5802, ctrlBlock);
                }
            }
        }

        @Override
        public void onDisconnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock) {

        }

        @Override
        public void onCancel(UsbDevice device) {

        }
    };

    public GlassesService() {
    }


    USBMonitor usbMonitor;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: serviceCreate");
        usbMonitor = new USBMonitor(getApplicationContext(), onDeviceConnectListener);
        usbMonitor.register();
    }

    private MyBinder binder;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        binder = new MyBinder();
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onCreate: serviceDestory");
        binder.destorySensor();
        if (usbMonitor != null) {
            usbMonitor.unregister();
        }
    }

    public class MyBinder extends Binder implements GlassesObservable {
        Vector<GlassesObserver> obs;

        public MyBinder() {
            obs = new Vector<>();
        }

        /**
         * 注册传感器
         */
        public void registerSensor() {
            sensor = NeptungSensor.getInstance(CuiNiaoApp.mAppContext);
            sensor.setOnSensorChangeListener(onSensorChangeListener);
        }

        NeptungSensor.OnSensorChangeListener onSensorChangeListener = new NeptungSensor.OnSensorChangeListener() {
            private short lastValue = -1;
            public static final short WEAR_ON = 1;
            public static final short WEAR_OFF = 0;
            short wearStatus = -1;

            @Override
            public void onSensorChange(float[] quat) {
                update(SENSOR_QUAT, quat);
            }

            @Override
            public void onQuaternion(float[] values) {
                update(SENSOR_QUATERNION, values);
            }

            @Override
            public void onWearStatus(short value) {
                if (lastValue != value) {
                    lastValue = value;
                    if (lastValue == WEAR_ON) {
                        update(MyBinder.SENSOR_WEAR, true);
                    } else {
                        update(MyBinder.SENSOR_WEAR, false);
                    }
                }
            }
        };

//        public void setSensorChangeListener(NeptungSensor.OnSensorChangeListener onSensorChangeListener) {
//            this.onSensorChangeListener = onSensorChangeListener;
//        }

        /**
         * 设定头显的亮度
         *
         * @param brightness
         */
        public void setBrightness(int brightness) {
            if (sensor != null)
                sensor.setBrightness(brightness);
        }

        /**
         * 释放掉眼镜的线程
         */
        public void destorySensor() {
            if (sensor != null) {
                sensor.onDestory();
            }
        }

        @Override
        public synchronized void addObserver(GlassesObserver observer) {
            if (observer == null)
                throw new NullPointerException();
            if (!obs.contains(observer)) {
                obs.addElement(observer);
            }
        }

        @Override
        public synchronized void deleteObserver(GlassesObserver observer) {
            obs.removeElement(observer);
        }

        public static final int CONNECT_5801 = 0x5801;//连接相机5801
        public static final int CONNECT_5802 = 0x5802;//连接相机5802
        public static final int VR_770 = 770;
        public static final int DIS_CONNECT = 0;
        public static final int SENSOR_QUAT = 1;
        public static final int SENSOR_QUATERNION = 2;
        public static final int SENSOR_WEAR = 3;


        @Override
        public synchronized void update(int tag, Object arg) {
            Object[] arrLocal;
            synchronized (this) {
                arrLocal = obs.toArray();
            }
            switch (tag) {
                case CONNECT_5801:
                case CONNECT_5802:
                case VR_770:
                    for (int i = arrLocal.length - 1; i >= 0; i--) {
                        ((GlassesObserver) arrLocal[i]).onConnect(tag, (USBMonitor.UsbControlBlock) arg);
                    }
                    break;

                case DIS_CONNECT:
                    for (int i = arrLocal.length - 1; i >= 0; i--) {
                        ((GlassesObserver) arrLocal[i]).onDisconnect();
                    }
                    break;
                case SENSOR_QUAT:
                    for (int i = arrLocal.length - 1; i >= 0; i--) {
                        ((GlassesObserver) arrLocal[i]).onQuatData((float[]) arg);
                    }
                    break;
                case SENSOR_QUATERNION:
                    for (int i = arrLocal.length - 1; i >= 0; i--) {
                        ((GlassesObserver) arrLocal[i]).onQuaternion((float[]) arg);
                    }
                    break;
                case SENSOR_WEAR:
                    for (int i = arrLocal.length - 1; i >= 0; i--) {
                        ((GlassesObserver) arrLocal[i]).onWearStatus((Boolean) arg);
                    }
                    break;
            }
//            for (int i = arrLocal.length-1; i>=0; i--)
//                ((Observer)arrLocal[i]).update(this, arg);
        }
    }
}
