package com.cnsj.neptunglasses.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;

import com.cnsj.neptunglasses.activity.YUVModeActivity;
import com.cnsj.neptunglasses.app.CuiNiaoApp;


/**
 * Created by Zph on 2020/7/30.
 */
public class BatteryReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        int level = 100;
        if (action != null && Intent.ACTION_BATTERY_CHANGED.equals(action)) {
            int status = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
            if (status == BatteryManager.BATTERY_STATUS_CHARGING) {//充电过程中不提示低电量
                Log.d("onReceive", "onReceive: 充电中");
                ((YUVModeActivity) context).setBatteryCharging(true);
            } else {
                ((YUVModeActivity) context).setBatteryCharging(false);
                level = intent.getIntExtra("level", 0);
                //更新电量显示
//            BaseItem baseItem = ((MainActivity) context).getBaseItemList().get(0);
//            baseItem.setMenuCount(Integer.toString(level) + "%");
                if (level == 20) {
                    CuiNiaoApp.textSpeechManager.speakNow("当前电量剩余百分之20,请及时充电");
                }
                if (level == 10) {
                    CuiNiaoApp.textSpeechManager.speakNow("当前电量剩余百分之10，请及时充电");
                }
                if (level == 5) {
                    CuiNiaoApp.textSpeechManager.speakNow("当前电量剩余百分之5，请及时充电");
                }
                if (level == 4) {
                    CuiNiaoApp.textSpeechManager.speakNow("当前电量剩余百分之4，请及时充电");
                }
                if (level == 3) {
                    CuiNiaoApp.textSpeechManager.speakNow("当前电量剩余百分之3，请及时充电");
                }
                if (level == 2) {
                    CuiNiaoApp.textSpeechManager.speakNow("当前电量剩余百分之2，请及时充电");
                }
                if (level == 1) {
                    CuiNiaoApp.textSpeechManager.speakNow("当前电量剩余百分之1，请及时充电");
                }
            }

        }
    }
}
