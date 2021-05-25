package com.cnsj.neptunglasses;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.cnsj.neptunglasses.activity.ThinGlassesActivity;
import com.cnsj.neptunglasses.activity.YUVModeActivity;


/**
 * Created by Zph on 2020/8/14.
 *
 */
public class WifiStateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            Log.i("MainActivity", "CONNECTIVITY_ACTION");
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            Log.i("MainActivity", "网络状态改变:" + wifi.isConnected());
            ((YUVModeActivity)context).checkWifiConnect(wifi.isConnected());
        }
    }
}
