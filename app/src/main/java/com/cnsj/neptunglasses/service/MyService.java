package com.cnsj.neptunglasses.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.cnsj.neptunglasses.app.CuiNiaoApp;
import com.cnsj.neptunglasses.sensor.NeptungSensor;

public class MyService extends Service {
    public NeptungSensor sensor;

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return new MyBinder();
    }


    public class MyBinder extends Binder {
        public MyBinder() {

        }

        /**
         * 注册传感器
         */
        public void registerSensor() {
            sensor = NeptungSensor.getInstance(CuiNiaoApp.mAppContext);
            sensor.setOnSensorChangeListener(onSensorChangeListener);
        }

        NeptungSensor.OnSensorChangeListener onSensorChangeListener;

        public void setSensorChangeListener(NeptungSensor.OnSensorChangeListener onSensorChangeListener) {
            this.onSensorChangeListener = onSensorChangeListener;
        }

        public void setBrightness(int brightness) {
            if (sensor != null)
                sensor.setBrightness(brightness);
        }

        public void destorySensor() {
            if (sensor != null) {
                sensor.onDestory();
            }
        }

    }
}
