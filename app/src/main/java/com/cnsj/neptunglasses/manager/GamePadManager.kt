package com.em3.vrhiddemos.manager


import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.IntentFilter
import android.util.Log
import com.em3.vrhiddemos.receiver.HidReceiver
import com.em3.vrhiddemos.tools.ShowHintTimer
import com.cnsj.neptunglasses.app.CuiNiaoApp
import com.cnsj.neptunglasses.receiver.BluetoothStateListenerReceiver
import com.cnsj.neptunglasses.utils.ClsUtils


/**
 * Created by Zph on 2020/7/02.
 * 蓝牙管理类
 */
open class GamePadManager(private val gamePadConnectStateListener: GamePadConnectStateListener) : BluetoothStateListenerReceiver.StateChangedListener {
    private var mBluetoothAdapter: BluetoothAdapter
    private var mGamePad: BluetoothDevice? = null
    private var lastState = 2
    private var isRunning = false
    private var hasReminded = true;
    private val timer = ShowHintTimer(60000, 2000, ::onTimeFinish)
    private var isStartDiscovry: Boolean = false;

    init {
        val manager = CuiNiaoApp.mAppContext.getSystemService(Application.BLUETOOTH_SERVICE) as (BluetoothManager)
        mBluetoothAdapter = manager.adapter
        registerBluetooth()
        registerStateListener()
        getBoundedDevice()
        isRunning = true;
    }

    interface GamePadConnectStateListener {
        fun onGamePadConnectStateChanged(state: Int)
        fun onBluetoothConnected(isConnect: Boolean): Boolean;
    }

    override fun onStateChanged(blueState: Int) {
        if (blueState == BluetoothAdapter.STATE_ON) {
            if (isStartDiscovry) return;
            mBluetoothAdapter.cancelDiscovery()
            removePaired()
            mBluetoothAdapter.startDiscovery()
            isStartDiscovry = true;
        } else if (blueState == BluetoothAdapter.STATE_OFF) {
            mBluetoothAdapter.enable()
        }
    }

    override fun onBluetoothConnected(isConnect: Boolean) {
        if (!isConnect) {
            if (!hasReminded) {
                hasReminded = gamePadConnectStateListener.onBluetoothConnected(isConnect);
            }
            isRunning = true;
        }
    }


    private fun onTimeFinish() {
        if (isRunning && lastState != 2) {
            gamePadConnectStateListener.onGamePadConnectStateChanged(-1)
            isRunning = false
        }
    }

    fun isRunning(): Boolean {
        return isRunning
    }

    private fun registerBluetooth() {
        val intentFilter = IntentFilter()
        intentFilter.addAction("android.bluetooth.device.action.PAIRING_REQUEST")
        intentFilter.addAction("android.bluetooth.device.action.FOUND")
        intentFilter.addAction("android.bluetooth.input.profile.action.CONNECTION_STATE_CHANGED")
        CuiNiaoApp.mAppContext.registerReceiver(HidReceiver(mBluetoothAdapter, ::onConnectStateChanged), intentFilter)
    }

    private fun registerStateListener() {
        val filter = IntentFilter()
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        CuiNiaoApp.mAppContext.registerReceiver(BluetoothStateListenerReceiver(this), filter)
    }

    private fun onConnectStateChanged(state: Int) {
        Log.i("bluetooth", "蓝牙连接状态：老状态$lastState 新$state")
        if (state == 0) {
            if (lastState == 1) {
                // gamePadConnectStateListener.onGamePadConnectStateChanged(-1)
            } else if (lastState != 0) {
                gamePadConnectStateListener.onGamePadConnectStateChanged(state)
            }
        } else {
            gamePadConnectStateListener.onGamePadConnectStateChanged(state)
        }
        if (state == 2) {
            isRunning = false
            hasReminded = false;
            timer.cancel();
        }
        lastState = state
    }

    fun reStart() {
        getBoundedDevice()
        isRunning = true
        hasReminded = true;
        mGamePad?.let {
            unPair2(it)
        }
        mGamePad = null
        mBluetoothAdapter.cancelDiscovery()
        if (mBluetoothAdapter.isEnabled) {
            mBluetoothAdapter.disable()
        } else {
            mBluetoothAdapter.enable()
        }
        isStartDiscovry = false;
        lastState = 0
        HidReceiver.i = 0;
        timer.cancel()
        timer.start()
    }

    private fun unPair2(device: BluetoothDevice) {
        try {
            val bool = ClsUtils.removeBond(device.javaClass, device)

            Log.i("bluetooth", "profileProxy删除配对的结果：$bool+" + device.name)
        } catch (e: Exception) {

            Log.i("bluetooth", "profileProxy删除配对报错 ${e.message}")
        }

    }

    private fun removePaired() {
        val list = mBluetoothAdapter.bondedDevices
        for (item in list) {
            if (HidReceiver.isOurDevice(item)) {
                unPair2(item)
            }
        }
    }

    public fun getBoundedDevice(): Boolean {
        val list = mBluetoothAdapter.bondedDevices
        mGamePad = null;
        Log.i("bluetooth", "蓝牙现在配对数：" + list.size)
        for (item in list) {

            //Log.i("zphs", "item::::" + item.name)
            if (HidReceiver.isOurDevice(item)) {
                mGamePad = item
                if (!mBluetoothAdapter.isEnabled)
                    mBluetoothAdapter.enable()
                break
            }
        }
        if (mGamePad != null) return true;
        else return false;
    }

}