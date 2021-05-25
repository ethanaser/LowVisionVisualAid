package com.cnsj.neptunglasses.bean.item;


import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

import com.cnsj.neptunglasses.R;
import com.cnsj.neptunglasses.activity.ThinGlassesActivity;
import com.cnsj.neptunglasses.app.CuiNiaoApp;
import com.cnsj.neptunglasses.bean.BaseItem;
import com.cnsj.neptunglasses.bean.MenuEyent;
import com.cnsj.neptunglasses.view.CustomMenuPopupThree;

/**
 * 说明书
 */
public class ManualItem extends BaseItem {

    private List<BaseItem> nextItems;

    public ManualItem(Activity activity) {
        super(activity);
    }

    @Override
    public int getLogoImage() {
        return 0;
    }

    @Override
    public int getBigLogo() {
        return CuiNiaoApp.isYellowMode ? R.mipmap.manual_set_b_y : R.mipmap.manual_set_b;
    }

    @Override
    public int getSmallLogo() {
        return CuiNiaoApp.isYellowMode ? R.mipmap.manual_set_s_y : R.mipmap.manual_set_s;
    }


    @Override
    public void init() {
        menuEyent = new MenuEyent();
        menuEyent.setItemName("说明书");
        menuEyent.setItemCount(">");
        nextItems = new ArrayList<>();
        String[] manuals = {"01 注意事项", "02 产品清单", "03 助视器及遥控器图示", "04 佩戴助视器",
                "05 连接遥控器", "06 充电", "07 功能说明", "08 常见问题", "09 产品参数", "10 售后服务"};
        for (int i = 0; i < manuals.length; i++) {
            ManualChildItem manualChildItem = new ManualChildItem(activity, manuals[i], i + 1);
            nextItems.add(manualChildItem);
        }
    }

    @Override
    public void load() {
        for (BaseItem baseItem : nextItems) {
            baseItem.load();
        }
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
        CuiNiaoApp.textSpeechManager.speakNow("说明书");
    }

    @Override
    public void update() {
//        for (BaseItem baseItem:nextItems){
//            baseItem.update();
//        }
    }

    @Override
    public void finish() {

    }

    /**
     * 临时将数据置空。暂无说明书
     *
     * @return
     */
    @Override
    public List<BaseItem> getNextMenu() {
        return nextItems;
    }

    @Override
    public int getCurrentMenuLevel() {
        return 1;
    }

    /**
     * 临时调整成false 暂无说明书
     *
     * @param menuPopup
     * @return
     */
    @Override
    public boolean intentToMenu2(CustomMenuPopupThree menuPopup) {
        return false;
    }
}
