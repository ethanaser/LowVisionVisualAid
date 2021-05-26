package com.serenegiant.usb.common;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * 增加图像畸变
 */
public class OffsetUtils {

    private static float[] leftOffsetVertex, rightOffsetVertex, textures;//增加畸变后的左右眼的Vertex顶点数据
    private static final String OFFSET_FILE = Environment.getExternalStorageDirectory() + "/offset.json";
    private static Context mContext;
    public static float amdParams = 1.0f;
    public static final int constSize = 20;

    public static void init(Context context) {
        mContext = context;
//        String json = getExternalJson(OFFSET_FILE);
//        if (json == null) {
//            json = getNormalJson("offset.json");
//        }
//        OffsetBean offsetBean = new Gson().fromJson(json, OffsetBean.class);
//        leftOffsetVertex = initOffset(offsetBean, true);
//        rightOffsetVertex = initOffset(offsetBean, false);
//        textures = initTexture1(33);
//        String json = getExternalJson(OFFSET_FILE);
//        if (json == null) {
//            json = getNormalJson("offset.json");
//        }
//        OffsetBean offsetBean = new Gson().fromJson(json, OffsetBean.class);
        leftOffsetVertex = initOffset(null, true);
        rightOffsetVertex = initOffset(null, false);
        textures = initTexture1(constSize);
    }

    private static float xPointValue = 1.0f, yPointValue = 0.5625f;

    public static void init(Context context, float xPoint, float yPoint) {
        mContext = context;
        xPointValue = xPoint;
        yPointValue = yPoint;
//        String json = getExternalJson(OFFSET_FILE);
//        if (json == null) {
//            json = getNormalJson("offset.json");
//        }
//        OffsetBean offsetBean = new Gson().fromJson(json, OffsetBean.class);
//        leftOffsetVertex = initOffset(offsetBean, true);
//        rightOffsetVertex = initOffset(offsetBean, false);
//        String json = getExternalJson(OFFSET_FILE);
//        if (json == null) {
//            json = getNormalJson("offset.json");
//        }
//        OffsetBean offsetBean = new Gson().fromJson(json, OffsetBean.class);
        leftOffsetVertex = initOffset(null, true);
        rightOffsetVertex = initOffset(null, false);
        textures = initTexture1(constSize);
    }


    /**
     * 初始化纹理坐标
     *
     * @param size
     * @return
     */
    private static float[] initTexture1(int size) {
        float[] result = new float[size * size * 4];
//        float offset = 0.125f;
        float offset = 0.0f;
        float spanx = (1.0f - 2 * offset) / (size - 1);
        float spany = 1.0f / (size - 1);
        int k = 0;
        for (int j = 0; j < size - 1; j++) {
            for (int i = 0; i < size; i++) {
                result[k++] = (spanx * i + offset);
                result[k++] = 1.0f - spany * j;
                result[k++] = (spanx * i + offset);
                result[k++] = 1.0f - spany * (j + 1);
            }
        }
        return result;
    }

    public static float[] initOffset(OffsetBean offsetBean, boolean b) {
        List<List<Double>> xx, yy;
        int size = constSize;
        float[] result = new float[size * size * 4];
//        if (b) {
//            xx = offsetBean.getLeftx();
//            yy = offsetBean.getLefty();
//        } else {
//            xx = offsetBean.getRightx();
//            yy = offsetBean.getRighty();
//        }
//        float[][] xResult = initX(xx);
//        float[][] yResult = initY(yy);
//        int k = 0;
//        for (int i = 0; i < size - 1; i++) {
//            for (int j = 0; j < size; j++) {
//                result[k++] = xResult[i][j];
//                result[k++] = yResult[i][j];
//                result[k++] = xResult[i + 1][j];
//                result[k++] = yResult[i + 1][j];
//            }
//        }
        float R0, pillowScale = 0.01f;
//        float R0, pillowScale = 0.0f;
        float[][] xResult = initX(null);
        float[][] yResult = initY(null);
        float[][] X0 = new float[constSize][constSize];
        float[][] Y0 = new float[constSize][constSize];
        for (int i = 0; i < constSize; i++) {
            for (int j = 0; j < constSize; j++) {
                X0[i][j] = xResult[i][j];
                Y0[i][j] = yResult[i][j];
                R0 = X0[i][j] * X0[i][j] * 4 * 4 + Y0[i][j] * Y0[i][j] * 3 * 3;//这里乘以系数是实际边长的比例 2880:2160=4:3
                X0[i][j] = X0[i][j] * (1.0f - pillowScale * R0);
                Y0[i][j] = Y0[i][j] * (1.0f - pillowScale * R0);
            }
        }
        int k = 0;
        for (int i = 0; i < size - 1; i++) {
            for (int j = 0; j < size; j++) {
                result[k++] = X0[i][j];
                result[k++] = Y0[i][j];
                result[k++] = X0[i + 1][j];
                result[k++] = Y0[i + 1][j];
            }
        }
        return result;
    }

    /**
     * 初始化Y值
     *
     * @param yy
     * @return
     */
    private static float[][] initY(List<List<Double>> yy) {
        int size = constSize;
        float[][] result = new float[size][size];
//        float ymin = -1f, ymax = 1f;
        float ymin = -yPointValue, ymax = yPointValue;
//        float ymin = -0.75f, ymax = 0.75f;
//        float ymin = -0.667f, ymax = 0.667f;
        float spany = (ymax - ymin) / (size - 1);
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
//                result[j][i] = (float) (ymin + j * spany + (yy.get(j).get(i) * amdParams / (size / 2.0f)));
                result[j][i] = (float) (ymin + j * spany);
            }
        }
        return result;
    }

    /**
     * 初始化X值
     *
     * @param xx
     * @return
     */
    private static float[][] initX(List<List<Double>> xx) {
        int size = constSize;
        float[][] result = new float[size][size];
        //16/9===0.889  4/3===0.667
//        float xmin = -0.889f, xmax = 0.889f;
        float xmin = -xPointValue, xmax = xPointValue;
//        float xmin = -0.75f, xmax = 0.75f;
        float spanx = (xmax - xmin) / (size - 1);
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
//                result[i][j] = (float) (xmin + j * spanx + (xx.get(i).get(j) * amdParams / size));
                result[i][j] = (float) (xmin + j * spanx);
//                Log.d("TAG", "initX: "+result[i][j]);
            }
        }
        return result;
    }

    /**
     * 获取左眼的顶点数据
     *
     * @return
     */
    public static float[] getLeftOffsetVertex() {
        return leftOffsetVertex;
    }

    public static float[] getSingleOffsetVertex() {
        return leftOffsetVertex;
    }


    private float[] initVertex1(int size) {
        float[] result = new float[size * size * 4];
        //0.889=16/9 0.667=4/3// -1
        float xmin = -1f, xmax = 1f, ymin = -1, ymax = 1;
        float spanx = (xmax - xmin) / (size - 1);
        float spany = (ymax - ymin) / (size - 1);
        int k = 0;
        for (int j = 0; j < size - 1; j++) {
            for (int i = 0; i < size; i++) {
                result[k++] = spanx * i + xmin;
                result[k++] = 1 - (spany * j);
                result[k++] = spanx * i + xmin;
                result[k++] = 1 - (spany * (j + 1));
            }
        }
        return result;
    }

    /**
     * 获取右眼的顶点数据
     *
     * @return
     */
    public static float[] getRightOffsetVertex() {
        return rightOffsetVertex;
    }


    public static float[] getTextures() {
        return textures;
    }

    public static String getNormalJson(String fileName) {
        try {
            InputStreamReader inputReader = new InputStreamReader(mContext.getResources().getAssets().open(fileName));
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line = "";
            String Result = "";
            while ((line = bufReader.readLine()) != null)
                Result += line;
            return Result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String getExternalJson(String path) {
        StringBuffer stringBuffer = new StringBuffer();
        File filename = new File(path);
        if (!filename.exists()) {
//            filename = new File(OFFSET_FILE);
            return null;
        }
        InputStreamReader reader;
        try {
            reader = new InputStreamReader(new FileInputStream(filename));
            BufferedReader br = new BufferedReader(reader);
            String temp = "";
            while ((temp = br.readLine()) != null) {
                stringBuffer.append(temp);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return stringBuffer.toString();
    }

}
