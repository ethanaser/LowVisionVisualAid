package com.serenegiant.usb.common;

import java.util.List;

/**
 * offset.json的数据结构
 */
public class OffsetBean {
    private List<List<Double>> leftx;

    private List<List<Double>> lefty;

    private List<List<Double>> rightx;

    private List<List<Double>> righty;

    public void setLeftx(List<List<Double>> leftx) {
        this.leftx = leftx;
    }

    public List<List<Double>> getLeftx() {
        return this.leftx;
    }

    public void setLefty(List<List<Double>> lefty) {
        this.lefty = lefty;
    }

    public List<List<Double>> getLefty() {
        return this.lefty;
    }

    public void setRightx(List<List<Double>> rightx) {
        this.rightx = rightx;
    }

    public List<List<Double>> getRightx() {
        return this.rightx;
    }

    public void setRighty(List<List<Double>> righty) {
        this.righty = righty;
    }

    public List<List<Double>> getRighty() {
        return this.righty;
    }
}
