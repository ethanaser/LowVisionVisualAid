package com.cnsj.neptunglasses.receiver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


/**
 * Created by Zph on 2020/7/02.
 * 蓝牙广播监听类
 */
public class BluetoothStateListenerReceiver extends BroadcastReceiver {

    private StateChangedListener stateChangedListener;

    public interface StateChangedListener {
        void onStateChanged(int blueState);

        void onBluetoothConnected(boolean isConnect);
    }

    public BluetoothStateListenerReceiver(StateChangedListener stateChangedListener) {
        this.stateChangedListener = stateChangedListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case BluetoothAdapter.ACTION_STATE_CHANGED:
                int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                stateChangedListener.onStateChanged(blueState);
                switch (blueState) {
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.i("bluetooth", "onReceive---------蓝牙正在打开中");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.i("bluetooth", "onReceive---------蓝牙已经打开");

                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.i("bluetooth", "onReceive---------蓝牙正在关闭中");
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        Log.i("bluetooth", "onReceive---------蓝牙已经关闭");
                        break;
                }
                break;
            case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                //设备断开了
                stateChangedListener.onBluetoothConnected(false);
                Log.d("bluetooth", "onReceive: 蓝牙设备断开了");
                break;
            case BluetoothDevice.ACTION_ACL_CONNECTED:
                //设备连接上了
                stateChangedListener.onBluetoothConnected(true);
                Log.d("bluetooth", "onReceive: 蓝牙设备连接上了");
                break;
        }

    }
}
