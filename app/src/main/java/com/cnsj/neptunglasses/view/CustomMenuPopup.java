package com.cnsj.neptunglasses.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.BatteryManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.cnsj.neptunglasses.activity.YUVModeActivity;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.lxj.xpopup.impl.FullScreenPopupView;

import java.util.ArrayList;
import java.util.List;

import com.cnsj.neptunglasses.R;
import com.cnsj.neptunglasses.app.CuiNiaoApp;
import com.cnsj.neptunglasses.bean.BaseItem;

/**
 * 一级菜单
 */
public class CustomMenuPopup extends FullScreenPopupView {
    private Context mContext;

    private View layoutLeft, layoutRight, viewLieft, viewRight;
    private ListView listLeft, listRight;//mlistViewLeft, mlistViewRight,
    private ImageView wifiImageLeft, wifiImageRight, batteryImageLeft, batteryImageRight;
    private TextView batteryTextLeft, batteryTextRight;
    //    private LayoutInflater inflater;
//    private LinearLayout bodyView;
    private MylistAdapter t;
    private YUVModeActivity mActivity;
    private String screenSwitch = "关";
    private List<BaseItem> firstList = new ArrayList<>();
    private int selectedPosition;
    private SimpleAdapter simpleAdapter;

    public CustomMenuPopup(@NonNull Context context, Activity activity) {
        super(context);
        this.mContext = context;
        this.mActivity = (YUVModeActivity) activity;
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.menu_first_list;
    }

    @Override
    protected void onShow() {
        super.onShow();
        mActivity.coverUpPreview();
    }

    @Override
    protected void onDismiss() {
        super.onDismiss();
    }


    @Override
    protected void onCreate() {
        super.onCreate();
        selectedPosition = 0;
        initFirstList();
//        menuPopup = null;
        layoutLeft = findViewById(R.id.menu_first_layout_left);
        layoutRight = findViewById(R.id.menu_first_layout_right);
        viewLieft = findViewById(R.id.menu_first_left);
        viewRight = findViewById(R.id.menu_first_right);
        listLeft = viewLieft.findViewById(R.id.first_menu_listview);
        listRight = viewRight.findViewById(R.id.first_menu_listview);
        wifiImageLeft = viewLieft.findViewById(R.id.first_wifi_image);
        wifiImageRight = viewRight.findViewById(R.id.first_wifi_image);
        batteryImageLeft = viewLieft.findViewById(R.id.first_battery_image);
        batteryImageRight = viewRight.findViewById(R.id.first_battery_image);
        batteryTextLeft = viewLieft.findViewById(R.id.first_battery_text);
        batteryTextRight = viewRight.findViewById(R.id.first_battery_text);
        int mBatteryValue = mActivity.getBatteryValue();
        batteryImageLeft.setImageResource(mActivity.getBatteryImage(mBatteryValue));
        batteryImageRight.setImageResource(mActivity.getBatteryImage(mBatteryValue));
        batteryTextLeft.setText(mBatteryValue + "%");
        batteryTextRight.setText(mBatteryValue + "%");
        batteryTextLeft.setTextColor(CuiNiaoApp.isYellowMode ? mActivity.getResources().getColor(R.color.yellow_mode_selected) : mActivity.getResources().getColor(R.color.white));
        batteryTextRight.setTextColor(CuiNiaoApp.isYellowMode ? mActivity.getResources().getColor(R.color.yellow_mode_selected) : mActivity.getResources().getColor(R.color.white));
        //wifi连接状态
        wifiImageLeft.setImageResource(mActivity.getWifiStatus() ? (CuiNiaoApp.isYellowMode ? R.mipmap.wifi_success_y : R.mipmap.wifi_success) : (CuiNiaoApp.isYellowMode ? R.mipmap.wifi_failure_y : R.mipmap.wifi_failure));
        wifiImageRight.setImageResource(mActivity.getWifiStatus() ? (CuiNiaoApp.isYellowMode ? R.mipmap.wifi_success_y : R.mipmap.wifi_success) : (CuiNiaoApp.isYellowMode ? R.mipmap.wifi_failure_y : R.mipmap.wifi_failure));
        t = new MylistAdapter(mContext, firstList);
//        screenSwitch = Constant.Cheeses.get(10).getItemCount();
        if (screenSwitch == null) {
            screenSwitch = "关";
        }
        listLeft.setAdapter(t);
        listRight.setAdapter(t);
        listLeft.setSelection(selectedPosition);
        listRight.setSelection(selectedPosition);
        listLeft.setOverScrollMode(ListView.OVER_SCROLL_NEVER);
        listRight.setOverScrollMode(ListView.OVER_SCROLL_NEVER);
        listLeft.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                changeTextLocation(listLeft, position);
//                listLeft.smoothScrollToPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        listRight.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                changeTextLocation(listRight, position);
//                listLeft.smoothScrollToPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        float horizantal = mActivity.getPopupTranslationOffset()[0];
        float xLeft = mActivity.getPopupTranslationOffset()[1];
        float yLeft = mActivity.getPopupTranslationOffset()[2];
        float xRight = mActivity.getPopupTranslationOffset()[3];
        float yRight = mActivity.getPopupTranslationOffset()[4];
        layoutLeft.setTranslationX(-horizantal + xLeft);
        Log.e("one", "xLeft " + xLeft + "yLeft " + yLeft + "xRight " + xRight + "yRight " + yRight);
        layoutLeft.setTranslationY(-yLeft);
        layoutRight.setTranslationX(horizantal + xRight);
        layoutRight.setTranslationY(-yRight);
        int textScale = mActivity.getPopupTextScaleOffset()[0];
        float scale = 1 + (textScale - 1) * 0.2f;
        float leftScale = mActivity.getPopupScaleOffset()[0];
        float rightScale = mActivity.getPopupScaleOffset()[1];
        layoutLeft.setScaleX(leftScale * scale);
        layoutLeft.setScaleY(leftScale * scale);
        layoutRight.setScaleX(rightScale * scale);
        layoutRight.setScaleY(rightScale * scale);
        layoutLeft.setVisibility(VISIBLE);
        layoutRight.setVisibility(VISIBLE);
//        mlistViewLeft = (ListView) findViewById(R.id.menu_first_list_left);
//        mlistViewRight = (ListView) findViewById(R.id.menu_first_list_right);
//        mlistViewLeft.setAdapter(t);
//        mlistViewRight.setAdapter(t);
//        mlistViewLeft.setSelection(selectedPosition);
//        mlistViewRight.setSelection(selectedPosition);
//        mlistViewLeft.setOverScrollMode(ListView.OVER_SCROLL_NEVER);
//        mlistViewRight.setOverScrollMode(ListView.OVER_SCROLL_NEVER);
//        float horizantal = mActivity.getPopupTranslationOffset()[0];
//        float xLeft = mActivity.getPopupTranslationOffset()[1];
//        float yLeft = mActivity.getPopupTranslationOffset()[2];
//        float xRight = mActivity.getPopupTranslationOffset()[3];
//        float yRight = mActivity.getPopupTranslationOffset()[4];
//        mlistViewLeft.setTranslationX(-horizantal + xLeft);
//        Log.e("one", "xLeft " + xLeft + "yLeft " + yLeft + "xRight " + xRight + "yRight " + yRight);
//        mlistViewLeft.setTranslationY(-yLeft);
//        mlistViewRight.setTranslationX(horizantal + xRight);
//        mlistViewRight.setTranslationY(-yRight);
//        int textScale = mActivity.getPopupTextScaleOffset()[0];
//        float scale = 1 + (textScale - 1) * 0.2f;
//        float leftScale = mActivity.getPopupScaleOffset()[0];
//        float rightScale = mActivity.getPopupScaleOffset()[1];
//        mlistViewLeft.setScaleX(leftScale * scale);
//        mlistViewLeft.setScaleY(leftScale * scale);
//        mlistViewRight.setScaleX(rightScale * scale);
//        mlistViewRight.setScaleY(rightScale * scale);
//        mlistViewLeft.setVisibility(VISIBLE);
//        mlistViewRight.setVisibility(VISIBLE);
    }

    private void changeTextLocation(ListView listView, int ssPosition) {
        int x = 0;
        if (ssPosition <= (listView.getLastVisiblePosition() + listView.getFirstVisiblePosition()) / 2) {
            x = (ssPosition - getScrollViewMiddle(listView));
        } else {
            x = (ssPosition + getScrollViewMiddle(listView));
        }
        listView.smoothScrollToPosition(x);
    }

    /**
     * 返回listview的中间位置
     *
     * @return
     */
    private int getScrollViewMiddle(ListView listView) {
        int scrollViewMiddle = getScrollViewheight(listView) / 2;
        return scrollViewMiddle;
    }

    /**
     * @return
     */
    private int getScrollViewheight(ListView listView) {

        int scrllViewWidth = listView.getLastVisiblePosition() - listView.getFirstVisiblePosition();
        return scrllViewWidth;
    }

    /**
     * 初始化一级列表的显示
     */
    private void initFirstList() {
        firstList.clear();
        int first = mActivity.getFirstPosition();
        firstList.addAll(mActivity.getBaseItemList());
        selectedPosition = first;
    }

    private int mUserScale = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        selectedPosition = listLeft.getSelectedItemPosition();
        BaseItem baseItem;
        if (event.getRepeatCount() == 0) {
            event.startTracking();
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    baseItem = t.getItem(selectedPosition);
                    if (baseItem == null) return true;
                    dismiss();
                    mActivity.displayPreview();
                    onDestroy();
//                CuiNiaoApp.textSpeechManager.speakNow("退出菜单");
                    mActivity.setFirstPosition(0);
                    mActivity.setSecondPosition(-1);
                    mActivity.setThirdPosition(-1);
                    baseItem.onKeyDown(keyCode);
//                    mActivity.setCurrentTimeMillis(System.currentTimeMillis());
                    mActivity.onKeyDown(keyCode, event);
                    mActivity.setMenuPopup(null, null);
                    break;
                case KeyEvent.KEYCODE_H:
                case KeyEvent.KEYCODE_I:
                case KeyEvent.KEYCODE_D:
                    baseItem = t.getItem(selectedPosition);
                    if (baseItem == null) return true;
                    dismiss();
                    mActivity.displayPreview();
                    onDestroy();
                    CuiNiaoApp.textSpeechManager.speakNow("退出菜单");
                    mActivity.setFirstPosition(0);
                    mActivity.setSecondPosition(-1);
                    mActivity.setThirdPosition(-1);
                    mActivity.setMenuPopup(null, null);
                    baseItem.onKeyDown(keyCode);
                    return true;
                case KeyEvent.KEYCODE_C:
                    if (selectedPosition >= firstList.size() - 1) {
//                        selectedPosition = -1;
                        return true;
                    }
                    selectedPosition++;
                    listLeft.setSelection(selectedPosition);
                    listRight.setSelection(selectedPosition);
                    baseItem = t.getItem(selectedPosition);
                    if (baseItem == null) return true;
                    CuiNiaoApp.textSpeechManager.speakNow(baseItem.getMenuName());
                    t.notifyDataSetChanged();
                    return true;
                case KeyEvent.KEYCODE_B:
                    if (selectedPosition <= 0) {
//                        selectedPosition = firstList.size();
                        return true;
                    }
                    selectedPosition--;
                    listLeft.setSelection(selectedPosition);
                    listRight.setSelection(selectedPosition);
                    baseItem = t.getItem(selectedPosition);
                    if (baseItem == null) return true;
                    CuiNiaoApp.textSpeechManager.speakNow(baseItem.getMenuName());
                    t.notifyDataSetChanged();
                    return true;
//                case KeyEvent.KEYCODE_D:
//                case KeyEvent.KEYCODE_E:
//                    baseItem = t.getItem(selectedPosition);
//                    baseItem.onKeyEvent(keyCode);
//                    t.notifyDataSetChanged();
//                    if (baseItem instanceof FontSizeSetItem) {
//                        setViewScale();
//                    }
//                    return true;
                case KeyEvent.KEYCODE_F://enter
                case KeyEvent.KEYCODE_E:
                    baseItem = t.getItem(selectedPosition);
                    if (baseItem == null) return true;
                    mActivity.setFirstPosition(selectedPosition);
                    menuPopup = new CustomMenuPopupThree(mContext, this.mActivity);
                    ((CustomMenuPopupThree) menuPopup).setBaseItem(baseItem);
                    baseItem.onKeyDown(keyCode);
                    boolean needIntent = baseItem.intentToMenu2((CustomMenuPopupThree) menuPopup);
                    List<BaseItem> item2 = baseItem.getNextMenu();
                    if (item2 != null) {//如果返回的二级菜单列表不为空就跳转至二级菜单
                        menuPopup = new CustomMenuPopupTwo(mContext, this.mActivity);
                        mActivity.setFirstPosition(selectedPosition);
                        mActivity.setSecondPosition(0);
                        item2.get(0).speak();
                        BasePopupView basePopupView = new XPopup.Builder(CuiNiaoApp.mAppContext)
                                .hasStatusBarShadow(false)
                                .hasStatusBar(false)
                                .asCustom(menuPopup)
                                .show();
                        mActivity.setMenuPopup(menuPopup, basePopupView);
                        dismiss();
                        onDestroy();
                        return true;
                    }
                    if (needIntent) {
                        dismiss();
                        mActivity.displayPreview();
                        mActivity.setMenuPopup(null, null);
                    } else {
                        baseItem.speak();
                    }
                    return true;
//                case KeyEvent.KEYCODE_J://放大
//                    mUserScale = myGLSurfaceView.getScale();
//                    myGLSurfaceView.setScale(mUserScale + 1);
//                    mUserScale = myGLSurfaceView.getScale();
//                    CuiNiaoApp.textSpeechManager.speakNow("放大" + formatZoomRate(mUserScale) + "倍");
//                    return true;
//                case KeyEvent.KEYCODE_K:
//                    mUserScale = myGLSurfaceView.getScale();
//                    myGLSurfaceView.setScale(mUserScale - 1);
//                    mUserScale = myGLSurfaceView.getScale();
//                    CuiNiaoApp.textSpeechManager.speakNow("放大" + formatZoomRate(mUserScale) + "倍");
//                    return true;
                case KeyEvent.KEYCODE_VOLUME_UP://音量+ 放大
                case KeyEvent.KEYCODE_VOLUME_DOWN://音量- 缩小
                    //长按会触发系统音量增大减小
                    return true;
                case KeyEvent.KEYCODE_0:
                    Log.d("YUVModeActivity", "onKeyDown: 电源键啊");
                    isShutDown = false;
                    currentTimeMillis = System.currentTimeMillis();
                    return true;
                default:
                    mActivity.notifyError();
                    break;
            }
        } else {
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP://音量+ 放大
                case KeyEvent.KEYCODE_VOLUME_DOWN://音量- 缩小
                    //长按会触发系统音量增大减小
                    return true;
                case KeyEvent.KEYCODE_0:

                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private boolean isShutDown = false;
    private long currentTimeMillis = 0l;

    /**
     * 字号缩放
     */
    private void setViewScale() {
        int textScale = mActivity.getPopupTextScaleOffset()[0];
        float scale = 1 + (textScale - 1) * 0.2f;
        float leftScale = mActivity.getPopupScaleOffset()[0];
        float rightScale = mActivity.getPopupScaleOffset()[1];
        layoutLeft.setScaleX(leftScale * scale);
        layoutLeft.setScaleY(leftScale * scale);
        layoutRight.setScaleX(rightScale * scale);
        layoutRight.setScaleY(rightScale * scale);
    }

    private FullScreenPopupView menuPopup;

    class MylistAdapter extends BaseAdapter {
        LayoutInflater inflater;

        private List<BaseItem> list;

        public MylistAdapter(Context context, List<BaseItem> list) {
            this.list = list;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            if (list == null) {
                return 0;
            }
            return list.size();
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public BaseItem getItem(int i) {
            if (i < 0) {
                return null;
            }
            if (list == null) {
                return null;
            }
            return list.get(i);
        }

        public void clearAll() {
            if (list != null) {
                list.clear();
            }
        }

        public void addAll(List<BaseItem> items) {
            if (list != null) {
                list.addAll(items);
            }
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.first_list_item, null);
                viewHolder = new ViewHolder();
//                viewHolder.imageView = convertView.findViewById(R.id.first_list_image);
                viewHolder.showImage = convertView.findViewById(R.id.first_list_item_image_b);
                viewHolder.hideImage = convertView.findViewById(R.id.first_list_item_image_s);
                viewHolder.showText = convertView.findViewById(R.id.first_list_item_text_b);
                viewHolder.hideText = convertView.findViewById(R.id.first_list_item_text_s);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
//            viewHolder.itemName.setText(list.get(i).getMenuName());
//            viewHolder.itemCont.setText(list.get(i).getMenuCount());
//            viewHolder.imageView.setImageResource(list.get(i).getSmallLogo());
            if (selectedPosition == i) {
                viewHolder.showImage.setImageResource(list.get(i).getBigLogo());
                viewHolder.showText.setText(list.get(i).getMenuName());
//                viewHolder.showText.setTextColor(Color.parseColor("#FFFFFFFF"));
                viewHolder.showText.setTextColor(CuiNiaoApp.isYellowMode ? mActivity.getResources().getColor(R.color.yellow_mode_selected) : mActivity.getResources().getColor(R.color.white));
                viewHolder.hideImage.setImageResource(0);
                viewHolder.hideText.setText(null);
            } else {
                viewHolder.hideImage.setImageResource(list.get(i).getSmallLogo());
                viewHolder.hideText.setText(list.get(i).getMenuName());
//                viewHolder.hideText.setTextColor(Color.parseColor("#77FFFFFF"));
                viewHolder.hideText.setTextColor(CuiNiaoApp.isYellowMode ? mActivity.getResources().getColor(R.color.yellow_mode_unselected) : mActivity.getResources().getColor(R.color.unselected_white));
                viewHolder.showImage.setImageResource(0);
                viewHolder.showText.setText(null);
            }
            return convertView;
        }

        public List<BaseItem> getList() {
            return list;
        }

        private class ViewHolder {
            //            ImageView imageView;
            ImageView showImage, hideImage;
            TextView showText, hideText;
        }

    }

    private String getResourceString(int id) {
        return CuiNiaoApp.mAppContext.getResources().getString(id);
    }

    private String formatZoomRate(int scale) {
        switch (scale) {
            case 1:
                return "1";
            case 2:
                return "1.5";
            case 3:
                return "2";
            case 4:
                return "3";
            case 5:
                return "5";
            case 6:
                return "10";
            case 7:
                return "20";
            case 8:
                return "25";
        }
        return null;
    }
}
