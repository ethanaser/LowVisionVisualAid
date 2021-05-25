package com.cnsj.neptunglasses.utils;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * Created by Zph on 2020/8/9.
 */
public class ApkParser {
    private MessageDigest messagedigest = null;
    private char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public ApkParser() {
        try {
            messagedigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException nsaex) {
            System.err.println(ApkParser.class.getName()
                    + "初始化失败，MessageDigest不支持MD5Util。");
            nsaex.printStackTrace();
        }
    }

    /**
     * 读文件
     *
     * @param file
     * @return
     */
    public String readFile(File file) {
        String readLine = null;
        StringBuffer sb = new StringBuffer();
        try {
            FileInputStream fis = new FileInputStream(file);
            //字符输入流  转换为字符流? 转换流
            //缓冲流 相当于村粗空间
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));

            while ((readLine = br.readLine()) != null) {
                sb.append(readLine + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * 生成文件的md5校验值
     *
     * @param file
     * @return
     * @throws IOException
     */
    public String getFileMD5String(File file) throws IOException {
        InputStream fis;
        fis = new FileInputStream(file);
        byte[] buffer = new byte[1024];
        int numRead = 0;
        while ((numRead = fis.read(buffer)) > 0) {
            messagedigest.update(buffer, 0, numRead);
        }
        fis.close();
        return bufferToHex(messagedigest.digest());
    }

    private String bufferToHex(byte bytes[]) {
        return bufferToHex(bytes, 0, bytes.length);
    }

    private String bufferToHex(byte bytes[], int m, int n) {
        StringBuffer stringbuffer = new StringBuffer(2 * n);
        int k = m + n;
        for (int l = m; l < k; l++) {
            appendHexPair(bytes[l], stringbuffer);
        }
        return stringbuffer.toString();
    }

    private void appendHexPair(byte bt, StringBuffer stringbuffer) {
        char c0 = hexDigits[(bt & 0xf0) >> 4]; // 取字节中高 4 位的数字转换, >>> 为逻辑右移，将符号位一起右移,此处未发现两种符号有何不同
        char c1 = hexDigits[bt & 0xf]; // 取字节中低 4 位的数字转换
        stringbuffer.append(c0);
        stringbuffer.append(c1);
    }


    public void install(String path, Context context) {
        Intent intent = new Intent();
        ComponentName cn = new ComponentName("com.emdoor.vr.sdk", "com.emdoor.vr.sdk.MainActivity");
        try {
            intent.setComponent(cn);
            Bundle bundle = new Bundle();
            bundle.putString("command", "install");
            bundle.putString("path", path);
            intent.putExtra("com.emdoor.vr.sdk", bundle);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.d("TAG", "install: 没有找到指定的Activity");
        }
    }


}
