package com.em3.vrhiddemos.receiver

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.cnsj.neptunglasses.utils.BlueConnectThread


/**
 * Created by Zph on 2020/7/02.
 * HIB蓝牙监听类
 */
class HidReceiver(private val adapter: BluetoothAdapter, private val onConnectStateChanged: (state: Int) -> Unit) : BroadcastReceiver() {

    companion object {
        public var i: Int = 0;
        private lateinit var blueConnectThread: BlueConnectThread;
        private val handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                if (msg.what == 2) {

                }
            }
        }
        private val GamePadNameRules = listOf<String>("CNSJ")//"翠鸟视觉"
        fun isOurDevice(device: BluetoothDevice): Boolean {
            return isOurDeviceWithName(device) || isOurDeviceWithMac(device)
        }

        private fun isOurDeviceWithName(device: BluetoothDevice): Boolean {
            if (device.name != null) {
                for (i in GamePadNameRules.indices) {
                    if (device.name.contains(GamePadNameRules[i])) {
                        return true
                    }
                }
            }
            return false
        }

        private fun isOurDeviceWithMac(device: BluetoothDevice): Boolean {
            if (device.address.contains("5F:5F:5B:A1:4C:44")) {
                return true
            }
            return false
        }
    }


    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        var btDevice: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        // XLog.d("device==" + btDevice?.name + "   " + btDevice?.address + "  " + action)

        if (btDevice == null || !isOurDevice(btDevice)) {
            return
        }
        if (BluetoothDevice.ACTION_FOUND == action) {
            if (i > 0) return
            Log.d("TAG", "profileProxy: 连接次数" + i)
            i++;
            adapter.cancelDiscovery()
            blueConnectThread = BlueConnectThread.getInstance(handler, adapter, context);
            blueConnectThread.device = btDevice;
            Log.d("TAG", "profileProxy: 线程启动，开始连接")
//            Thread(blueConnectThread).start();
            blueConnectThread.run();
//            BlueConnectThread(btDevice, adapter, context).run()
        } else if (action == "android.bluetooth.input.profile.action.CONNECTION_STATE_CHANGED") {
            //  XLog.d("android.bluetooth.input.profile.action.CONNECTION_STATE_CHANGED")
            val connectState = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothProfile.STATE_CONNECTED)
            Log.d("TAG", "profileProxy: 遥控器状态$connectState")
            if (connectState == 2) i = 0;
            onConnectStateChanged(connectState)
            if (connectState == BluetoothProfile.STATE_DISCONNECTED) {
                /* XLog.d("BluetoothProfile.STATE_DISCONNECTED")
                 adapter.cancelDiscovery()
                 adapter.startDiscovery()*/
                /* if (btDevice.bondState == BluetoothDevice.BOND_BONDED) {
                     ClsUtils.removeBond(btDevice.javaClass, btDevice)
                     XLog.d("BluetoothProfile.STATE_DISCONNECTED==removeBond")
                 }*/
            }
        }
    }

}