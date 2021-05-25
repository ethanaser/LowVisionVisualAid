package com.cnsj.neptunglasses.bean;

import android.app.Activity;
import android.view.KeyEvent;

import com.cnsj.neptunglasses.activity.YUVModeActivity;
import com.lxj.xpopup.XPopup;

import java.util.List;

import com.cnsj.neptunglasses.activity.ThinGlassesActivity;
import com.cnsj.neptunglasses.app.CuiNiaoApp;
import com.cnsj.neptunglasses.view.CustomMenuPopupThree;

/**
 * 菜单基类 会实现部分通用方法
 */
public abstract class BaseItem {
    protected MenuEyent menuEyent;//设定menu的内容
    public YUVModeActivity activity;


    public BaseItem() {
        init();
    }

    public BaseItem(Activity activity) {
        this.activity = (YUVModeActivity) activity;
        init();
    }

    /**
     * 获取左侧显示的logo图标
     *
     * @return
     */
    public abstract int getLogoImage();

    /**
     * 获取右侧显示的图标内容
     *
     * @return
     */
    public int getDisplayImages() {
        return -1;
    }

    public String getDisplayText() {
        return null;
    }


    /**
     * 初始化
     */
    public abstract void init();

    /**
     * 加载设置
     */
    public abstract void load();

    /**
     * 更新当前列表配置
     */
    public abstract void reset(int tag);

    public abstract boolean toJson();

    public abstract MenuEyent fromJson();

    /**
     * onKeyDown事件
     *
     * @param keyCode
     */
    public abstract void onKeyDown(int keyCode);

    public void onKeyDown(int keyCode, KeyEvent event) {

    }

    public abstract void speak();

    public abstract void update();

    public abstract void finish();

    public void onKeyUp(int keyCode) {
    }

    protected CustomMenuPopupThree menuPopup;

    /**
     * 跳转至三级菜单 子类可根据实际情况自行选择是否使用该方法
     *
     * @param menuPopup
     */
    public boolean intentToMenu2(CustomMenuPopupThree menuPopup) {
        this.menuPopup = menuPopup;
        this.menuPopup.setBaseItem(this);
        new XPopup.Builder(CuiNiaoApp.mAppContext)
                .hasStatusBarShadow(false)
                .hasStatusBar(false)
                .asCustom(menuPopup)
                .show();
        return true;
    }

    /**
     * 返回二级菜单列表
     *
     * @return
     */
    public abstract List<BaseItem> getNextMenu();

    /**
     * 返回菜单级别
     *
     * @return
     */
    public abstract int getCurrentMenuLevel();


    /**
     * 获取选中状态的logo
     *
     * @return
     */
    public int getBigLogo() {
        return 0;
    }

    /**
     * 获取未选中状态的logo
     *
     * @return
     */
    public int getSmallLogo() {
        return 0;
    }

    public boolean isImage() {
        return false;
    }

    public int getItemCountImage(boolean isSelected) {
        return 0;
    }


    public String getMenuName() {
        return menuEyent.getItemName();
    }

    public String getMenuCount() {
        return menuEyent.getItemCount();
    }

    public void setMenuName(String menuName) {
        menuEyent.setItemName(menuName);
    }

    public void setMenuCount(String itemCount) {
        menuEyent.setItemCount(itemCount);
    }


}
