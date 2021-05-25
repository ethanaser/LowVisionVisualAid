package com.cnsj.neptunglasses.service;

import com.serenegiant.usb.USBMonitor;

public interface GlassesObserver {

    /**
     * 连接相机设备连接状态
     *
     * @param productId       设备ID
     * @param usbControlBlock
     */
    void onConnect(int productId, USBMonitor.UsbControlBlock usbControlBlock);

    /**
     * USB设备断开
     */
    void onDisconnect();

    /**
     * 四元数数据
     *
     * @param quat
     */
    void onQuatData(float[] quat);

    /**
     * 原始的四元数数据
     *
     * @param quats
     */
    void onQuaternion(float[] quats);

    /**
     * 获取佩戴传感器的数据
     *
     * @param isWear
     */
    void onWearStatus(boolean isWear);
}
