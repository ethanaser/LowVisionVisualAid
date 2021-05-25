package com.cnsj.neptunglasses.sensor;

import android.content.Context;
import android.hardware.GeomagneticField;
import android.hardware.SensorManager;
import android.util.Log;

import com.xgvr.glasses.HidDataInfoX1S;
import com.xgvr.glasses.TcpServerThread;
import com.xgvr.glasses.XgvrErrorCode;
import com.xgvr.glasses.XgvrMoudle;

/**
 * 获取眼镜的传感器数据
 */
public class NeptungSensor {
    private Context context;
    private XgvrMoudle xgvrMoudle;
    private static NeptungSensor instance;
    private int initStatus;// XgvrErrorCode.XGVR_NO_ERROR  初始化状态
    private NeptungThread neptungThread;

//    TcpServerThread tcpServerThread;

    private NeptungSensor(Context context) {
        this.context = context;
        xgvrMoudle = XgvrMoudle.getInstance();
//        xgvrMoudle.xgvr_hmd_set_state_listener(new XgvrMoudle.HmdStateListener() {
//            @Override
//            public void connect(int i) {
//                Log.d("TAG", "connect: iiiiiiiiiiii:"+i);
//            }
//
//            @Override
//            public void disonnect() {
//
//            }
//        });
        initStatus = -1;
        neptungThread = new NeptungThread();
        neptungThread.start();
//        tcpServerThread = new TcpServerThread();//向眼镜发送数据的线程
//        tcpServerThread.start();
    }

    public static NeptungSensor getInstance(Context context) {
        if (instance == null) {
            synchronized (NeptungSensor.class) {
                if (instance == null) {
                    instance = new NeptungSensor(context);
                }
            }
        }
        return instance;
    }

    boolean isUpdateBrightness = false;
    private int brightness;

    public void setBrightness(int brightness) {
        isUpdateBrightness = true;
        this.brightness = brightness;
    }

//    public int getBrightness(){
//
//    }

    float[] values;//[0-3] quat四元数 w x y z

    /**
     * 获取传感器数据的线程
     */
    public class NeptungThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (true) {
                if (initStatus != XgvrErrorCode.XGVR_NO_ERROR) {
                    Log.d("TAG", "hidDataInfoX1S: 初始化");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    initStatus = xgvrMoudle.xgvr_hdm_init(context);
                    continue;
                }
                if (isUpdateBrightness) {
                    if (initStatus == XgvrErrorCode.XGVR_NO_ERROR) {
                        isUpdateBrightness = false;
                        int aaa = xgvrMoudle.writeData_x1s(brightness);
                        Log.d("TAG", "run: aaaaaa:" + aaa);
                    }

                }
                HidDataInfoX1S hidDataInfoX1S = xgvrMoudle.xgvr_hmd_x1s_data_get();
                if (hidDataInfoX1S == null) {
                    Log.d("TAG", "hidDataInfoX1S: hidDataInfoX1S获取失败");
                    initStatus = xgvrMoudle.xgvr_hdm_init(context);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                values = hidDataInfoX1S.quat;
                if (values == null) {
                    Log.d("TAG", "hidDataInfoX1S: 眼镜四元数获取失败");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
//                float[] rotationMatrix = new float[9];
//                getRotationMatrixFromVector(rotationMatrix, values);
//                int worldAxisForDeviceAxisX = 3;
//                int worldAxisForDeviceAxisY = 1 | 0x80;
//                float[] adjustedRotationMatrix = new float[9];
//                remapCoordinateSystem(rotationMatrix, worldAxisForDeviceAxisX,
//                        worldAxisForDeviceAxisY, adjustedRotationMatrix);
//                float[] orientation = new float[3];
//                getOrientation(adjustedRotationMatrix, orientation);
//
//                quat[0] = orientation[0] * -57;
//                quat[1] = orientation[1] * -57;
//                quat[2] = orientation[2] * -57;
//                for (int i = 0; i < values.length; i++) {
//                    Log.d("TAG", "value: " + values[i]);
//                }
//                quat[3] = values[3];
                if (onSensorChangeListener != null) {
                    onSensorChangeListener.onQuaternion(values);
//                    onSensorChangeListener.onSensorChange(quat);
                    onSensorChangeListener.onWearStatus(hidDataInfoX1S.reserve[0]);
                } else {
                    synchronized (this) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /**
     * Helper function to convert a rotation vector to a rotation matrix.
     * Given a rotation vector (presumably from a ROTATION_VECTOR sensor), returns a
     * 9  or 16 element rotation matrix in the array R.  R must have length 9 or 16.
     * If R.length == 9, the following matrix is returned:
     * <pre>
     *   /  R[ 0]   R[ 1]   R[ 2]   \
     *   |  R[ 3]   R[ 4]   R[ 5]   |
     *   \  R[ 6]   R[ 7]   R[ 8]   /
     * </pre>
     * If R.length == 16, the following matrix is returned:
     * <pre>
     *   /  R[ 0]   R[ 1]   R[ 2]   0  \
     *   |  R[ 4]   R[ 5]   R[ 6]   0  |
     *   |  R[ 8]   R[ 9]   R[10]   0  |
     *   \  0       0       0       1  /
     * </pre>
     *
     * @param rotationVector the rotation vector to convert
     * @param R              an array of floats in which to store the rotation matrix
     */
    public void getRotationMatrixFromVector(float[] R, float[] rotationVector) {

        float q0;
        float q1 = rotationVector[1];
        float q2 = rotationVector[2];
        float q3 = rotationVector[3];

//        if (rotationVector.length >= 4) {
        q0 = rotationVector[0];
//        } else {
//            q0 = 1 - q1 * q1 - q2 * q2 - q3 * q3;
//            q0 = (q0 > 0) ? (float) Math.sqrt(q0) : 0;
//        }

        float sq_q1 = 2 * q1 * q1;
        float sq_q2 = 2 * q2 * q2;
        float sq_q3 = 2 * q3 * q3;
        float q1_q2 = 2 * q1 * q2;
        float q3_q0 = 2 * q3 * q0;
        float q1_q3 = 2 * q1 * q3;
        float q2_q0 = 2 * q2 * q0;
        float q2_q3 = 2 * q2 * q3;
        float q1_q0 = 2 * q1 * q0;

        if (R.length == 9) {
            R[0] = 1 - sq_q2 - sq_q3;
            R[1] = q1_q2 - q3_q0;
            R[2] = q1_q3 + q2_q0;

            R[3] = q1_q2 + q3_q0;
            R[4] = 1 - sq_q1 - sq_q3;
            R[5] = q2_q3 - q1_q0;

            R[6] = q1_q3 - q2_q0;
            R[7] = q2_q3 + q1_q0;
            R[8] = 1 - sq_q1 - sq_q2;
        } else if (R.length == 16) {
            R[0] = 1 - sq_q2 - sq_q3;
            R[1] = q1_q2 - q3_q0;
            R[2] = q1_q3 + q2_q0;
            R[3] = 0.0f;

            R[4] = q1_q2 + q3_q0;
            R[5] = 1 - sq_q1 - sq_q3;
            R[6] = q2_q3 - q1_q0;
            R[7] = 0.0f;

            R[8] = q1_q3 - q2_q0;
            R[9] = q2_q3 + q1_q0;
            R[10] = 1 - sq_q1 - sq_q2;
            R[11] = 0.0f;

            R[12] = R[13] = R[14] = 0.0f;
            R[15] = 1.0f;
        }
    }

    /**
     * <p>
     * Rotates the supplied rotation matrix so it is expressed in a different
     * coordinate system. This is typically used when an application needs to
     * compute the three orientation angles of the device (see
     * {@link #getOrientation}) in a different coordinate system.
     * </p>
     *
     * <p>
     * When the rotation matrix is used for drawing (for instance with OpenGL
     * ES), it usually <b>doesn't need</b> to be transformed by this function,
     * unless the screen is physically rotated, in which case you can use
     * {@link android.view.Display#getRotation() Display.getRotation()} to
     * retrieve the current rotation of the screen. Note that because the user
     * is generally free to rotate their screen, you often should consider the
     * rotation in deciding the parameters to use here.
     * </p>
     *
     * <p>
     * <u>Examples:</u>
     * <p>
     *
     * <ul>
     * <li>Using the camera (Y axis along the camera's axis) for an augmented
     * reality application where the rotation angles are needed:</li>
     *
     * <p>
     * <ul>
     * <code>remapCoordinateSystem(inR, AXIS_X, AXIS_Z, outR);</code>
     * </ul>
     * </p>
     *
     * <li>Using the device as a mechanical compass when rotation is
     * {@link android.view.Surface#ROTATION_90 Surface.ROTATION_90}:</li>
     *
     * <p>
     * <ul>
     * <code>remapCoordinateSystem(inR, AXIS_Y, AXIS_MINUS_X, outR);</code>
     * </ul>
     * </p>
     * <p>
     * Beware of the above example. This call is needed only to account for a
     * rotation from its natural orientation when calculating the rotation
     * angles (see {@link #getOrientation}). If the rotation matrix is also used
     * for rendering, it may not need to be transformed, for instance if your
     * {@link android.app.Activity Activity} is running in landscape mode.
     * </ul>
     *
     * <p>
     * Since the resulting coordinate system is orthonormal, only two axes need
     * to be specified.
     *
     * @param inR  the rotation matrix to be transformed. Usually it is the matrix
     *             returned by {@link #getRotationMatrix}.
     * @param X    defines the axis of the new cooridinate system that coincide with the X axis of the
     *             original coordinate system.
     * @param Y    defines the axis of the new cooridinate system that coincide with the Y axis of the
     *             original coordinate system.
     * @param outR the transformed rotation matrix. inR and outR should not be the same
     *             array.
     * @return <code>true</code> on success. <code>false</code> if the input
     * parameters are incorrect, for instance if X and Y define the same
     * axis. Or if inR and outR don't have the same length.
     * @see #getRotationMatrix(float[], float[], float[], float[])
     */

    public boolean remapCoordinateSystem(float[] inR, int X, int Y, float[] outR) {
        if (inR == outR) {
            final float[] temp = sTempMatrix;
            synchronized (temp) {
                // we don't expect to have a lot of contention
                if (remapCoordinateSystemImpl(inR, X, Y, temp)) {
                    final int size = outR.length;
                    for (int i = 0; i < size; i++) {
                        outR[i] = temp[i];
                    }
                    return true;
                }
            }
        }
        return remapCoordinateSystemImpl(inR, X, Y, outR);
    }

    private static final float[] sTempMatrix = new float[16];

    private boolean remapCoordinateSystemImpl(float[] inR, int X, int Y, float[] outR) {
        /*
         * X and Y define a rotation matrix 'r':
         *
         *  (X==1)?((X&0x80)?-1:1):0    (X==2)?((X&0x80)?-1:1):0    (X==3)?((X&0x80)?-1:1):0
         *  (Y==1)?((Y&0x80)?-1:1):0    (Y==2)?((Y&0x80)?-1:1):0    (Y==3)?((X&0x80)?-1:1):0
         *                              r[0] ^ r[1]
         *
         * where the 3rd line is the vector product of the first 2 lines
         *
         */

        final int length = outR.length;
        if (inR.length != length) {
            return false;   // invalid parameter
        }
        if ((X & 0x7C) != 0 || (Y & 0x7C) != 0) {
            return false;   // invalid parameter
        }
        if (((X & 0x3) == 0) || ((Y & 0x3) == 0)) {
            return false;   // no axis specified
        }
        if ((X & 0x3) == (Y & 0x3)) {
            return false;   // same axis specified
        }

        // Z is "the other" axis, its sign is either +/- sign(X)*sign(Y)
        // this can be calculated by exclusive-or'ing X and Y; except for
        // the sign inversion (+/-) which is calculated below.
        int Z = X ^ Y;

        // extract the axis (remove the sign), offset in the range 0 to 2.
        final int x = (X & 0x3) - 1;
        final int y = (Y & 0x3) - 1;
        final int z = (Z & 0x3) - 1;

        // compute the sign of Z (whether it needs to be inverted)
        final int axis_y = (z + 1) % 3;
        final int axis_z = (z + 2) % 3;
        if (((x ^ axis_y) | (y ^ axis_z)) != 0) {
            Z ^= 0x80;
        }

        final boolean sx = (X >= 0x80);
        final boolean sy = (Y >= 0x80);
        final boolean sz = (Z >= 0x80);

        // Perform R * r, in avoiding actual muls and adds.
        final int rowLength = ((length == 16) ? 4 : 3);
        for (int j = 0; j < 3; j++) {
            final int offset = j * rowLength;
            for (int i = 0; i < 3; i++) {
                if (x == i) outR[offset + i] = sx ? -inR[offset + 0] : inR[offset + 0];
                if (y == i) outR[offset + i] = sy ? -inR[offset + 1] : inR[offset + 1];
                if (z == i) outR[offset + i] = sz ? -inR[offset + 2] : inR[offset + 2];
            }
        }
        if (length == 16) {
            outR[3] = outR[7] = outR[11] = outR[12] = outR[13] = outR[14] = 0;
            outR[15] = 1;
        }
        return true;
    }


    /**
     * Computes the device's orientation based on the rotation matrix.
     * <p>
     * When it returns, the array values are as follows:
     * <ul>
     * <li>values[0]: <i>Azimuth</i>, angle of rotation about the -z axis.
     *                This value represents the angle between the device's y
     *                axis and the magnetic north pole. When facing north, this
     *                angle is 0, when facing south, this angle is &pi;.
     *                Likewise, when facing east, this angle is &pi;/2, and
     *                when facing west, this angle is -&pi;/2. The range of
     *                values is -&pi; to &pi;.</li>
     * <li>values[1]: <i>Pitch</i>, angle of rotation about the x axis.
     *                This value represents the angle between a plane parallel
     *                to the device's screen and a plane parallel to the ground.
     *                Assuming that the bottom edge of the device faces the
     *                user and that the screen is face-up, tilting the top edge
     *                of the device toward the ground creates a positive pitch
     *                angle. The range of values is -&pi; to &pi;.</li>
     * <li>values[2]: <i>Roll</i>, angle of rotation about the y axis. This
     *                value represents the angle between a plane perpendicular
     *                to the device's screen and a plane perpendicular to the
     *                ground. Assuming that the bottom edge of the device faces
     *                the user and that the screen is face-up, tilting the left
     *                edge of the device toward the ground creates a positive
     *                roll angle. The range of values is -&pi;/2 to &pi;/2.</li>
     * </ul>
     * <p>
     * Applying these three rotations in the azimuth, pitch, roll order
     * transforms an identity matrix to the rotation matrix passed into this
     * method. Also, note that all three orientation angles are expressed in
     * <b>radians</b>.
     *
     * @param R      rotation matrix see {@link #getRotationMatrix}.
     * @param values an array of 3 floats to hold the result.
     * @return The array values passed as argument.
     * @see #getRotationMatrix(float[], float[], float[], float[])
     * @see GeomagneticField
     */
    public static float[] getOrientation(float[] R, float[] values) {
        /*
         * 4x4 (length=16) case:
         *   /  R[ 0]   R[ 1]   R[ 2]   0  \
         *   |  R[ 4]   R[ 5]   R[ 6]   0  |
         *   |  R[ 8]   R[ 9]   R[10]   0  |
         *   \      0       0       0   1  /
         *
         * 3x3 (length=9) case:
         *   /  R[ 0]   R[ 1]   R[ 2]  \
         *   |  R[ 3]   R[ 4]   R[ 5]  |
         *   \  R[ 6]   R[ 7]   R[ 8]  /
         *
         */
        if (R.length == 9) {
            values[0] = (float) Math.atan2(R[1], R[4]);
            values[1] = (float) Math.asin(-R[7]);
            values[2] = (float) Math.atan2(-R[6], R[8]);
        } else {
            values[0] = (float) Math.atan2(R[1], R[5]);
            values[1] = (float) Math.asin(-R[9]);
            values[2] = (float) Math.atan2(-R[8], R[10]);
        }

        return values;
    }

    private float[] quat = new float[3];
    private OnSensorChangeListener onSensorChangeListener;

    public void setOnSensorChangeListener(OnSensorChangeListener onSensorChangeListener) {
        this.onSensorChangeListener = onSensorChangeListener;
        synchronized (this) {
            try {
                notifyAll();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 监听传感器变化的监听
     */
    public interface OnSensorChangeListener {
        /**
         * 转换后的数据
         *
         * @param quat
         */
        void onSensorChange(float[] quat);

        /**
         * 原始的四元数数据
         *
         * @param values
         */
        void onQuaternion(float[] values);

        void onWearStatus(short value);
    }


    /**
     * 释放资源
     */
    public void onDestory() {
        if (xgvrMoudle.xgvr_hmd_is_present() == 0) {
            xgvrMoudle.xgvr_hmd_deinit();
        }
        onSensorChangeListener = null;
    }

    // 0xEB 90 03 06 LEN B CSL CSH
    private final static int DATA_EB = 0xEB;//包头1
    private final static int DATA_START2 = 0x90;//包头2
    private final static int DATA_DEVICE_TYPE = 0x03;//设备类型


    /**
     * 发送数据到眼镜
     *
     * @param type  数据类型  0  1
     * @param value 值 亮度时最大50 控制亮屏时 0x00 auto 0x01 on 0x02 off
     */
//    public void sendMessageToNeptung(int type, int value) {
//        if (tcpServerThread != null) {
//            int commondType = 0;
//            int DATA_CSL = value & 0xff;
//            int DATA_CSH = value >> 24 & 0xff;
//            int DATA_LENGTH = 0;
//            for (int i = 0; i < 32; i++) {
//                if ((value >> i & 0xffffffff) > 0) {
//                    continue;
//                } else {
//                    DATA_LENGTH = i;
//                    break;
//                }
//            }
//            byte[] data = new byte[8];
//            data[0] = (byte) DATA_EB;
//            data[1] = (byte) DATA_START2;
//            data[2] = DATA_DEVICE_TYPE;
//            switch (type) {
//                case 0:// 亮度  0X00-0X64;  厂商控制最高50 否则会出现问题
//                    commondType = 0x06;
//                    break;
//                case 1://控制亮屏设置 0X00 auto 0x01 on 0x02 off
//                    commondType = 0x25;
//                    break;
//            }
//            data[3] = (byte) commondType;
//            data[4] = (byte) DATA_LENGTH;
//            data[5] = (byte) value;
//            data[6] = (byte) DATA_CSL;
//            data[7] = (byte) DATA_CSH;
//            tcpServerThread.sendMessage(data);
//        }
//    }
}
