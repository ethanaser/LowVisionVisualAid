package com.cnsj.neptunglasses.utils;

import android.content.Context;
import android.os.PowerManager;
import android.os.SystemClock;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 系统工具类
 * android:name="android.permission.DEVICE_POWER"
 */
public class SightaidUtil {
    /**
     * 关闭屏幕 ，其实是使系统休眠
     */
    public static void goToSleep(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        try {
            powerManager.getClass().getMethod("goToSleep", new Class[]{long.class}).invoke(powerManager, SystemClock.uptimeMillis());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    /**
     * 息屏
     */
    public static void sleep() {
        String sleep = "input keyevent 223";
        execRootCmd(sleep);

    }

    /**
     * 唤醒
     */
    public static void wakeup() {
        String wakeup = "input keyevent 224";
        execRootCmd(wakeup);
    }


    /**
     * 执行命令并且输出结果
     */
    public static String execRootCmd(String cmd) {
        String result = "";
        DataOutputStream dos = null;
        DataInputStream dis = null;
        Context context;
        try {
            Process p = Runtime.getRuntime().exec("/system/bin/sh");// 经过Root处理的android系统即有su命令
//            p=Runtime.getRuntime().exec("/system/xbin/su");
            dos = new DataOutputStream(p.getOutputStream());
            dis = new DataInputStream(p.getInputStream());
            dos.writeBytes(cmd + "\n");
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            String line = null;
            while ((line = dis.readLine()) != null) {
                result += line;
            }
            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * 唤醒屏幕
     *
     * @param context
     */
    public static void wakeUp(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        try {
            powerManager.getClass().getMethod("wakeUp", new Class[]{long.class}).invoke(powerManager, SystemClock.uptimeMillis());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }


    public static int shutdown() {
        int r = 0;
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", "reboot -p"});
            r = process.waitFor();
            System.out.println("r:" + r);
        } catch (IOException e) {
            e.printStackTrace();
            r = -1;
        } catch (InterruptedException e) {
            e.printStackTrace();
            r = -1;
        }
        return r;
    }


    /**
     * 乘法计算
     *
     * @return
     */
    public float floatMultiply(float value1, float value2) {
        BigDecimal b1 = new BigDecimal(Float.toString(value1));
        BigDecimal b2 = new BigDecimal(Float.toString(value2));
        return b1.multiply(b2).floatValue();
    }


    private static String nums[] = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};

    private static String pos_units[] = {"", "十", "百", "千"};

    private static String weight_units[] = {"", "万", "亿"};

    public static String versionToChinese(String content) {
        if (content == null || "".equals(content))
            return null;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < content.length(); i++) {
            int value = Integer.parseInt(String.valueOf(content.charAt(i)));
            sb.append(nums[value]);
        }
        return sb.toString();
    }

    /**
     * 数字转汉字【新】
     *
     * @param num
     * @return
     */
    public static String numberToChinese(int num) {
        if (num == 0) {
            return "零";
        }

        int weigth = 0;//节权位
        String chinese = "";
        String chinese_section = "";
        boolean setZero = false;//下一小节是否需要零，第一次没有上一小节所以为false
        while (num > 0) {
            int section = num % 10000;//得到最后面的小节
            if (setZero) {//判断上一小节的千位是否为零，是就设置零
                chinese = nums[0] + chinese;
            }
            chinese_section = sectionTrans(section);
            if (section != 0) {//判断是都加节权位
                chinese_section = chinese_section + weight_units[weigth];
            }
            chinese = chinese_section + chinese;
            chinese_section = "";
            setZero = (section < 1000) && (section > 0);
            num = num / 10000;
            weigth++;
        }
        if ((chinese.length() == 2 || (chinese.length() == 3)) && chinese.contains("一十")) {
            chinese = chinese.substring(1, chinese.length());
        }
        if (chinese.indexOf("一十") == 0) {
            chinese = chinese.replaceFirst("一十", "十");
        }

        return chinese;
    }

    /**
     * 将每段数字转汉子
     *
     * @param section
     * @return
     */
    public static String sectionTrans(int section) {
        StringBuilder section_chinese = new StringBuilder();
        int pos = 0;//小节内部权位的计数器
        boolean zero = true;//小节内部的置零判断，每一个小节只能有一个零。
        while (section > 0) {
            int v = section % 10;//得到最后一个数
            if (v == 0) {
                if (!zero) {
                    zero = true;//需要补零的操作，确保对连续多个零只是输出一个
                    section_chinese.insert(0, nums[0]);
                }
            } else {
                zero = false;//有非零数字就把置零打开
                section_chinese.insert(0, pos_units[pos]);
                section_chinese.insert(0, nums[v]);
            }
            pos++;
            section = section / 10;
        }

        return section_chinese.toString();
    }

    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    public static int dpToPx(Context context,float dpValue) {//dp转换为px
        float scale=context.getResources().getDisplayMetrics().density;//获得当前屏幕密度
        return (int)(dpValue*scale+0.5f);
    }

}
