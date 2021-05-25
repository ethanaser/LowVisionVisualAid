package com.cnsj.neptunglasses.utils;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


/**
 * Created by Zph on 2020/7/02.
 * 蓝牙配对类
 */
public class BlueConnectThread implements Runnable {
    private Handler handler;
    private BluetoothDevice device;
    private BluetoothAdapter mAdapter;
    private Context context;
    private static BlueConnectThread blueConnectThread;

    public static BlueConnectThread getInstance(Handler handler, BluetoothAdapter adapter, Context context) {
        if (blueConnectThread == null) {
            synchronized (BlueConnectThread.class) {
                if (blueConnectThread == null) {
                    blueConnectThread = new BlueConnectThread(handler, adapter, context);
                }
            }
        }
        return blueConnectThread;
    }

    boolean profileProxy;

    private BlueConnectThread(Handler handler, BluetoothAdapter adapter, Context context) {
        this.handler = handler;
//        this.device = device;
        this.mAdapter = adapter;
        this.context = context;
        profileProxy = mAdapter.getProfileProxy(context, connect, 4);
        Log.d("TAG", "profileProxy: 初始化时状态；" + profileProxy);
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    @Override
    public void run() {
        //进行HID蓝牙设备的配对，配对成功之后才进行下边的操作
        if (pair(device)) {
            try {
                Log.d("TAG", "profileProxy: 配对成功，开始连接");
                //进行HID蓝牙设备的连接操作，并返回连接结果，这里边的第3个参数就是代表连接HID设备，
//                boolean profileProxy = mAdapter.getProfileProxy(context, connect, 4);
                Thread.sleep(3000);
                if (proxys != null) {
                    try {
                        if (device != null) {
                            //得到BluetoothInputDevice然后反射connect连接设备
                            @SuppressLint("PrivateApi") Method method = proxys.getClass().getDeclaredMethod("connect",
                                    new Class[]{BluetoothDevice.class});
                            method.invoke(proxys, device);
                        }
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }else{
                    mAdapter.getProfileProxy(context, connect, 4);
                    Thread.sleep(3000);
                }
                Log.d("TAG", "profileProxy: 开始连接" + profileProxy);
                Message message = handler.obtainMessage(2);
                //通过handler将连接结果和连接的代理返回主线程中。这个BluetoothProfile对象在后边还有用，所以要返回主线程中
                if (profileProxy) {
                    message.arg1 = 1;
                } else {
                    message.arg1 = 2;
                }
                message.obj = proxys;
                handler.sendMessage(message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 配对
     *
     * @param bluetoothDevice
     */
    public boolean pair(BluetoothDevice bluetoothDevice) {
        device = bluetoothDevice;
        Method createBondMethod;
        try {
            createBondMethod = BluetoothDevice.class.getMethod("createBond");
            createBondMethod.invoke(device);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取当前设备设备是否支持INPUT_DEVICE设备（HID设备）
     */
    public static int getInputDeviceHiddenConstant() {
        Class<BluetoothProfile> clazz = BluetoothProfile.class;
        for (Field f : clazz.getFields()) {
            int mod = f.getModifiers();
            if (Modifier.isStatic(mod) && Modifier.isPublic(mod)
                    && Modifier.isFinal(mod)) {
                try {
                    if (f.getName().equals("INPUT_DEVICE")) {
                        return f.getInt(null);
                    }
                } catch (Exception e) {
                }
            }
        }
        return -1;
    }

    private BluetoothProfile proxys;
    /**
     * 查看BluetoothInputDevice源码，connect(BluetoothDevice device)该方法可以连接HID设备，但是查看BluetoothInputDevice这个类
     * 是隐藏类，无法直接使用，必须先通过BluetoothProfile.ServiceListener回调得到BluetoothInputDevice，然后再反射connect方法连接
     */
    private BluetoothProfile.ServiceListener connect = new BluetoothProfile.ServiceListener() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            //BluetoothProfile proxy这个已经是BluetoothInputDevice类型了
            Log.d("TAG", "profileProxy: " + (proxys == proxy));
            proxys = proxy;
            try {
                if (device != null) {
                    //得到BluetoothInputDevice然后反射connect连接设备
                    @SuppressLint("PrivateApi") Method method = proxys.getClass().getDeclaredMethod("connect",
                            new Class[]{BluetoothDevice.class});
                    method.invoke(proxys, device);
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(int profile) {
        }
    };
}
