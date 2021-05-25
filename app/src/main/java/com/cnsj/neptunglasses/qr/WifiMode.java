package com.cnsj.neptunglasses.qr;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.gson.Gson;

/**
 * 解析WIFI的二维码
 */
public class WifiMode {

    private Gson gson;

    public WifiMode(Gson gson) {
        this.gson = gson;
    }

    public String getWifiResult(Bitmap bitmap) {
        String result = null;
        String jsonResult = QrCodeUtils.Companion.parseQRCodeResult(bitmap);
        Log.d("TAG", "getWifiResult: json:" + jsonResult);
        //{"command":"wifi","length":3,"arg0":"123123","arg1":"12345678","arg2":"3"}
        if (jsonResult == null) return null;
        if (!jsonResult.equals(QrCodeUtils.NO_DATA)) {//识别到正常数据
            WifiResult wifiResult = gson.fromJson(jsonResult, WifiResult.class);
            if (wifiResult != null && wifiResult.arg0 != null && !wifiResult.arg0.trim().equals("") && wifiResult.arg1 != null && !wifiResult.arg1.trim().equals("")) {
                StringBuffer sb = new StringBuffer();
                sb.append(wifiResult.arg0);
                sb.append(",");
                sb.append(wifiResult.arg1);
                return sb.toString();
            }
        }
        return result;
    }

    class WifiResult {
        private String command;
        private Integer length;
        private String arg0;
        private String arg1;
        private String arg2;

        public String getCommand() {
            return command;
        }

        public void setCommand(String command) {
            this.command = command;
        }

        public Integer getLength() {
            return length;
        }

        public void setLength(Integer length) {
            this.length = length;
        }

        public String getArg0() {
            return arg0;
        }

        public void setArg0(String arg0) {
            this.arg0 = arg0;
        }

        public String getArg1() {
            return arg1;
        }

        public void setArg1(String arg1) {
            this.arg1 = arg1;
        }

        public String getArg2() {
            return arg2;
        }

        public void setArg2(String arg2) {
            this.arg2 = arg2;
        }
    }


}
