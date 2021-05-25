package com.cnsj.neptunglasses.bean.item.seconditem;

import android.app.Activity;
import android.content.SharedPreferences;
import android.view.KeyEvent;


import com.google.gson.reflect.TypeToken;

import java.util.List;

import com.cnsj.neptunglasses.R;
import com.cnsj.neptunglasses.app.Constant;
import com.cnsj.neptunglasses.app.CuiNiaoApp;
import com.cnsj.neptunglasses.bean.BaseItem;
import com.cnsj.neptunglasses.bean.MenuEyent;

/**
 * 描边模式设定
 */
public class EdgeModeSetItem extends BaseItem {

    //描边模式的颜色值
    private float[][] edgeValues;
    //对应上面的颜色值
    private String[] menuItemCounts;
    private int userMode = 0;

    public EdgeModeSetItem(Activity activity) {
        super(activity);
    }

    @Override
    public void init() {
        edgeValues = new float[][]{
                {//黑白
                        1.0f, -1.0f,//r=1.0-1.0r
                        1.0f, -1.0f,//g=1.0-1.0g
                        1.0f, -1.0f//b=1.0-1.0b
                },
                {//白黑
                        0.0f, 1.0f,//R
                        0.0f, 1.0f,//G
                        0.0f, 1.0f//B
                },
                {//蓝黄
                        1.0f, -1.0f,//R
                        1.0f, -1.0f,//G
                        0.0f, 1.0f//B
                },
                {//黄蓝
                        0.0f, 1.0f,//R
                        0.0f, 1.0f,//G
                        1.0f, -1.0f//B
                },
                {//黄黑
                        0.0f, 1.0f,//R
                        0.0f, 1.0f,//G
                        -1.0f, -1.0f//B
                },
                {//黑黄
                        1.0f, -1.0f,//R
                        1.0f, -1.0f,//G
                        -1.0f, -1.0f//B
                },
                {//绿黑
                        0.0f, -1.0f,//R
                        0.0f, 1.0f,//G
                        0.0f, -1.0f//B
                },
                {//黑绿
                        0.0f, -1.0f,//R
                        1.0f, -1.0f,//G
                        0.0f, -1.0f//B
                },
                {//白蓝
                        0.0f, 1.0f,//R
                        0.0f, 1.0f,//G
                        1.0f, 1.0f//B
                },
                {//蓝白
                        1.0f, -1.0f,//R
                        1.0f, -1.0f,//G
                        1.0f, 1.0f//B
                },
                {//红黑
                        0.0f, 1.0f,//R
                        0.0f, -1.0f,//G
                        0.0f, -1.0f//B
                },
                {//黑红
                        1.0f, -1.0f,//R
                        0.0f, -1.0f,//G
                        0.0f, -1.0f//B
                },
                {//白红
                        1.0f, 1.0f,//R
                        0.0f, 1.0f,//G
                        0.0f, 1.0f//B
                },
                {//红白
                        1.0f, 1.0f,//R
                        1.0f, -1.0f,//G
                        1.0f, -1.0f//B
                }
        };

        menuItemCounts = new String[]{"黑白", "白黑", "蓝黄", "黄蓝", "黄黑", "黑黄", "绿黑", "黑绿", "白蓝", "蓝白", "红黑", "黑红", "白红", "红白"};
        userMode = 0;
        menuEyent = fromJson();
        if (menuEyent == null) {
            menuEyent = new MenuEyent();
            menuEyent.setItemName("描边模式");
            menuEyent.setItemCount(menuItemCounts[userMode]);
        }
        activity.setEdgeColor(edgeValues[userMode]);
    }

    @Override
    public int getCurrentMenuLevel() {
        return 2;
    }

    @Override
    public int getLogoImage() {
        return R.mipmap.edgecolor;
    }

    @Override
    public String getDisplayText() {
        return "< " + menuItemCounts[userMode] + " >";
    }

//    private Constant.ColorMode getSelectColorMode(String color) {
//        for (int i = 0; i < edgeColors.length; i++) {
//            if (edgeColors[i].equals(color)) {
//                return edgeColors[i];
//            }
//        }
//        return Constant.ColorMode.mode_edge_black_white;
//    }

    private int getIndexOfEdgeColors(String color) {
        for (int i = 0; i < menuItemCounts.length; i++) {
            if (menuItemCounts[i].equals(color)) {
                return i;
            }
        }
        return 0;
    }


    @Override
    public void load() {
    }

    @Override
    public void reset(int tag) {
        userMode = 0;
        menuEyent.setItemCount(menuItemCounts[userMode]);
        activity.setEdgeColor(edgeValues[userMode]);
        toJson();
    }

    @Override
    public boolean toJson() {
        String itemKey = "edge_colors";
        String json = activity.getGson().toJson(menuEyent);
        SharedPreferences.Editor editor = CuiNiaoApp.sharedPreferences.edit();
        editor.putString(itemKey, json);
        editor.commit();
        return false;
    }

    @Override
    public MenuEyent fromJson() {
        String itemKey = "edge_colors";
        String json = CuiNiaoApp.sharedPreferences.getString(itemKey, null);
        if (json != null) {
            MenuEyent menuEyent = activity.getGson().fromJson(json, new TypeToken<MenuEyent>() {
            }.getType());
            userMode = getIndexOfEdgeColors(menuEyent.getItemCount());
            return menuEyent;
        }
        return null;
    }


    @Override
    public void onKeyDown(int keyCode) {
        if (this.menuPopup == null) return;
        switch (keyCode) {
            case -1:
                activity.setEdgeColor(edgeValues[userMode]);
                activity.setCameraModel(4, false);
                break;
            case KeyEvent.KEYCODE_E:
                userMode++;
                if (userMode > edgeValues.length - 1) {
                    userMode = 0;
                }
                activity.setEdgeColor(edgeValues[userMode]);
                activity.setCameraModel(4, false);
                menuEyent.setItemCount(menuItemCounts[userMode]);
                CuiNiaoApp.textSpeechManager.speakNow("描边模式色彩" + menuItemCounts[userMode]);
                this.menuPopup.updateDisplay(getLogoImage(), getDisplayText(), getDisplayImages());
                toJson();
                break;
            case KeyEvent.KEYCODE_D:
                userMode--;
                if (userMode < 0) {
                    userMode = edgeValues.length - 1;
                }
                activity.setEdgeColor(edgeValues[userMode]);
                activity.setCameraModel(4, false);
                menuEyent.setItemCount(menuItemCounts[userMode]);
                CuiNiaoApp.textSpeechManager.speakNow("描边模式色彩" + menuItemCounts[userMode]);
                this.menuPopup.updateDisplay(getLogoImage(), getDisplayText(), getDisplayImages());
                toJson();
                break;
        }
    }


    @Override
    public void speak() {
        CuiNiaoApp.textSpeechManager.speakNow("描边模式色彩" + menuEyent.getItemCount());
    }

    @Override
    public void update() {

    }

    @Override
    public void finish() {
        this.menuPopup = null;
        activity.resetColorMode();
    }

    @Override
    public List<BaseItem> getNextMenu() {
        return null;
    }

}
