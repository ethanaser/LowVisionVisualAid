package com.cnsj.neptunglasses.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import org.zeromq.ZMQ;

/**
 * zmq服务端
 */
public class ZMQService extends Service {
    public ZMQService() {
    }

    ZMQ.Socket publisher;
    StringBuffer sb;

    @Override
    public void onCreate() {
        super.onCreate();
        ZMQ.Context context = ZMQ.context(1);
        publisher = context.socket(ZMQ.PUB);
        publisher.bind("tcp://*:5556");
        sb = new StringBuffer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (publisher != null)
            publisher.close();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new ZMQBinder();
    }


    public class ZMQBinder extends Binder {
        public ZMQBinder() {
        }

        public void quat(float[] quat) {
//            for (int i = 0; i < quat.length; i++) {
//                Log.d("TAG", "quat: "+quat[i]);
//            }
            if (publisher != null) {
                if (sb != null) {
                    sb.setLength(0);
                }
                sb.append(quat[0]);
                sb.append(" ");
                sb.append(quat[1]);
                sb.append(" ");
                sb.append(quat[2]);
                sb.append(" ");
                sb.append(quat[3]);
                publisher.send(sb.toString(), 0);
            }
        }
    }
}
