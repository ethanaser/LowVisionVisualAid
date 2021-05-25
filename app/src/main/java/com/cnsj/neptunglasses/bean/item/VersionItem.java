package com.cnsj.neptunglasses.bean.item;


import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.List;

import com.cnsj.neptunglasses.R;
import com.cnsj.neptunglasses.activity.ThinGlassesActivity;
import com.cnsj.neptunglasses.app.CuiNiaoApp;
import com.cnsj.neptunglasses.bean.BaseItem;
import com.cnsj.neptunglasses.bean.MenuEyent;
import com.cnsj.neptunglasses.utils.SightaidUtil;

/**
 * 版本信息
 */
public class VersionItem extends BaseItem {

    public VersionItem(Activity activity) {
        super(activity);
    }

    @Override
    public void init() {
        menuEyent = new MenuEyent();
        menuEyent.setItemName("版本号");
        menuEyent.setItemCount(getLocalVersionName(activity.getApplicationContext()));
    }

    @Override
    public int getCurrentMenuLevel() {
        return 1;
    }

    @Override
    public int getLogoImage() {
        return CuiNiaoApp.isYellowMode ? R.mipmap.version_set_b_y : R.mipmap.version_b;
    }


    @Override
    public int getBigLogo() {
        return CuiNiaoApp.isYellowMode ? R.mipmap.version_set_b_y : R.mipmap.version_b;
    }

    @Override
    public int getSmallLogo() {
        return CuiNiaoApp.isYellowMode ? R.mipmap.version_set_s_y : R.mipmap.version_s;
    }

    @Override
    public String getDisplayText() {
        return "当前版本:" + getMenuCount();
    }

    @Override
    public void load() {

    }

    @Override
    public void reset(int tag) {

    }

    @Override
    public boolean toJson() {
        return false;
    }

    @Override
    public MenuEyent fromJson() {
        return null;
    }

    @Override
    public void onKeyDown(int keyCode) {

    }

    @Override
    public void speak() {
        StringBuffer sb = new StringBuffer();
        sb.append("当前版本:");
        String[] versionText = getMenuCount().split("");
        if (versionText == null) return;
        for (int i = 0; i < versionText.length; i++) {
            if (null == versionText[i] || "".equals(versionText[i].trim())) {
                continue;
            }
            if (".".equals(versionText[i])) {
                sb.append("点");
            } else if (SightaidUtil.isNumeric(versionText[i])) {
                sb.append(SightaidUtil.numberToChinese(Integer.parseInt(versionText[i])));
            } else {
                sb.append(versionText[i]);
            }
        }
        Log.d("TAG", "speak: " + sb.toString());
        CuiNiaoApp.textSpeechManager.speakNow(sb.toString());
    }

    @Override
    public void update() {

    }

    @Override
    public void finish() {

    }

    @Override
    public List<BaseItem> getNextMenu() {
        return null;
    }


    /**
     * 获取本地软件版本名称
     */
    public String getLocalVersionName(Context ctx) {
        String localVersion = "";
        try {
            PackageInfo packageInfo = ctx.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(ctx.getPackageName(), 0);
            localVersion = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return localVersion;
    }
}
