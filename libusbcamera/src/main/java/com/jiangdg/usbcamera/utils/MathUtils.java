package com.jiangdg.usbcamera.utils;

import java.math.BigDecimal;


/**
 * float计算工具类.
 */
public class MathUtils {

    /**
     * 乘法计算
     *
     * @return
     */
    public static float floatMultiply(float value1, float value2) {
        BigDecimal b1 = new BigDecimal(Float.toString(value1));
        BigDecimal b2 = new BigDecimal(Float.toString(value2));
        return b1.multiply(b2).floatValue();
    }

    /**
     * 加法
     *
     * @param value1
     * @param value2
     * @return
     */
    public static float floatAdd(float value1, float value2) {
        BigDecimal b1 = new BigDecimal(Float.toString(value1));
        BigDecimal b2 = new BigDecimal(Float.toString(value2));
        return b1.add(b2).floatValue();
    }

    /**
     * 减法
     *
     * @param value1 被减数
     * @param value2 减数
     * @return
     */
    public static float floatSub(float value1, float value2) {
        BigDecimal b1 = new BigDecimal(Float.toString(value1));
        BigDecimal b2 = new BigDecimal(Float.toString(value2));
        return b1.subtract(b2).floatValue();
    }

    /**
     * 除法  BigDecimal.ROUND_HALF_UP:四舍五入，2.35保留1位，变成2.4
     *
     * @param value1
     * @param value2
     * @param round  保留位数
     * @return
     */
    public static float floatDiv(float value1, float value2, int round) {
        BigDecimal b1 = new BigDecimal(Float.toString(value1));
        BigDecimal b2 = new BigDecimal(Float.toString(value2));
        return b1.divide(b2, round, BigDecimal.ROUND_HALF_UP).floatValue();
    }


}
