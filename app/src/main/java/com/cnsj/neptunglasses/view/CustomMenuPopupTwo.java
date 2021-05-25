package com.cnsj.neptunglasses.view;

import android.content.Context;
import android.graphics.Color;
import android.text.TextPaint;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cnsj.neptunglasses.activity.YUVModeActivity;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.lxj.xpopup.impl.FullScreenPopupView;

import java.util.ArrayList;
import java.util.List;

import com.cnsj.neptunglasses.R;
import com.cnsj.neptunglasses.app.CuiNiaoApp;
import com.cnsj.neptunglasses.bean.BaseItem;

public
/**
 * 二级菜单
 */
class CustomMenuPopupTwo extends FullScreenPopupView {
    private YUVModeActivity activity;
    private View menuLayoutLeft, menuLayoutRight, menuLeft, menuRight;
    //    private TextView textLeft, textRight;
    private ListView listLeft, listRight;
    //imageCenterLeft, imageCenterRight,
//    private ImageView previousItemImageLeft, nextItemImageLeft, previousItemImageRight, nextItemImageRight;
    private int selectPostion;
    private MyListAdapter adapter;
    private List<BaseItem> lists;
    private FullScreenPopupView menuPopup;
//    private Typeface ttf;
//    private Typeface ttfRegular;

    public CustomMenuPopupTwo(Context context, YUVModeActivity activity) {
        super(context);
        this.activity = activity;
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.menu_second_list;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        menuLayoutLeft = findViewById(R.id.menu_second_layout_left);
        menuLayoutRight = findViewById(R.id.menu_second_layout_right);
        menuLeft = findViewById(R.id.menu_second_left);
        menuRight = findViewById(R.id.menu_second_right);
//        textLeft = menuLeft.findViewById(R.id.second_list_text);
//        textRight = menuRight.findViewById(R.id.second_list_text);
        listLeft = menuLeft.findViewById(R.id.second_list_list);
        listRight = menuRight.findViewById(R.id.second_list_list);
//        imageCenterLeft = menuLeft.findViewById(R.id.second_list_center_image);
//        imageCenterRight = menuRight.findViewById(R.id.second_list_center_image);
//        previousItemImageLeft = menuLeft.findViewById(R.id.second_list_up_image);
//        nextItemImageLeft = menuLeft.findViewById(R.id.second_list_down_image);
//        previousItemImageRight = menuRight.findViewById(R.id.second_list_up_image);
//        nextItemImageRight = menuRight.findViewById(R.id.second_list_down_image);
//        menuLayoutLeft.setVisibility(INVISIBLE);
//        menuLayoutRight.setVisibility(INVISIBLE);
        initData();
        float horizantal = activity.getPopupTranslationOffset()[0];
        float xLeft = activity.getPopupTranslationOffset()[1];
        float yLeft = activity.getPopupTranslationOffset()[2];
        float xRight = activity.getPopupTranslationOffset()[3];
        float yRight = activity.getPopupTranslationOffset()[4];
        Log.e("two", "xLeft " + xLeft + "yLeft " + yLeft + "xRight " + xRight + "yRight " + yRight);
        menuLayoutLeft.setTranslationX(-horizantal + xLeft);
        menuLayoutLeft.setTranslationY(-yLeft);
        menuLayoutRight.setTranslationX(horizantal + xRight);
        menuLayoutRight.setTranslationY(-yRight);
        int textScale = activity.getPopupTextScaleOffset()[0];
        float scale = 1 + (textScale - 1) * 0.2f;
        float leftScale = activity.getPopupScaleOffset()[0];
        float rightScale = activity.getPopupScaleOffset()[1];
        menuLayoutLeft.setScaleX(leftScale * scale);
        menuLayoutLeft.setScaleY(leftScale * scale);
        menuLayoutRight.setScaleX(rightScale * scale);
        menuLayoutRight.setScaleY(rightScale * scale);
        menuLayoutLeft.setVisibility(VISIBLE);
        menuLayoutRight.setVisibility(VISIBLE);

    }

    /**
     * 初始化数据
     */
    private void initData() {
        lists = new ArrayList<>();
        selectPostion = 0;
        if (activity.getThirdPosition() != -1) {
            lists.addAll(activity.getBaseItemList().get(activity.getFirstPosition()).getNextMenu().get(activity.getSecondPosition()).getNextMenu());
            selectPostion = activity.getThirdPosition() + 1;
        } else {
            Log.d("initData", "initData: " + activity.getBaseItemList().size());
            Log.d("initData", "initData: " + activity.getFirstPosition());
            lists.addAll(activity.getBaseItemList().get(activity.getFirstPosition()).getNextMenu());
            selectPostion = activity.getSecondPosition() + 1;
        }
        Log.d("initData", "initData: " + lists.size());
        lists.add(0, null);
        lists.add(null);
        if (selectPostion <= 1) {
            selectPostion = 1;
        }
        adapter = new MyListAdapter(activity, lists);
//        adapter.addAll(lists);
//        adapter.addAll(lists);
        listLeft.setAdapter(adapter);
        listRight.setAdapter(adapter);
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
        listLeft.setSelection(selectPostion);
        listRight.setSelection(selectPostion);
//        showUpAndDown();
        menuPopup = new CustomMenuPopupThree(activity, this.activity);
    }

    /**
     * 显示或隐藏上下箭头
     */
//    private void showUpAndDown() {
//        nextItemImageLeft.setImageResource(CuiNiaoApp.isYellowMode ? R.mipmap.next_item_y : R.mipmap.next_item);
//        nextItemImageRight.setImageResource(CuiNiaoApp.isYellowMode ? R.mipmap.next_item_y : R.mipmap.next_item);
//        previousItemImageLeft.setImageResource(CuiNiaoApp.isYellowMode ? R.mipmap.previous_item_y : R.mipmap.previous_item);
//        previousItemImageRight.setImageResource(CuiNiaoApp.isYellowMode ? R.mipmap.previous_item_y : R.mipmap.previous_item);
//        Log.d("TAG", "showUpAndDown: position:" + selectPostion);
//        if (adapter.getCount() <= 4) {
//            nextItemImageLeft.setVisibility(GONE);
//            nextItemImageRight.setVisibility(GONE);
//            previousItemImageLeft.setVisibility(GONE);
//            previousItemImageRight.setVisibility(GONE);
//        } else {
//            if (selectPostion >= adapter.getCount() - 2) {
//                nextItemImageLeft.setVisibility(GONE);
//                nextItemImageRight.setVisibility(GONE);
//            } else {
//                nextItemImageLeft.setVisibility(VISIBLE);
//                nextItemImageRight.setVisibility(VISIBLE);
//            }
//            if (selectPostion <= 1) {
//                previousItemImageLeft.setVisibility(GONE);
//                previousItemImageRight.setVisibility(GONE);
//            } else {
//                previousItemImageLeft.setVisibility(VISIBLE);
//                previousItemImageRight.setVisibility(VISIBLE);
//            }
//        }
//    }


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


    @Override
    protected void onShow() {
        super.onShow();
        activity.coverUpPreview();

    }


    private int mUserScale = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        selectPostion = listLeft.getSelectedItemPosition();
        BaseItem baseItem;
        if (event.getRepeatCount() == 0) {
            event.startTracking();
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    baseItem = adapter.getItem(selectPostion);
                    if (baseItem == null) return true;
                    dismiss();
                    activity.displayPreview();
                    onDestroy();
//                CuiNiaoApp.textSpeechManager.speakNow("退出菜单");
                    activity.setFirstPosition(0);
                    activity.setSecondPosition(-1);
                    activity.setThirdPosition(-1);
                    baseItem.onKeyDown(keyCode);
//                    activity.setCurrentTimeMillis(System.currentTimeMillis());
                    activity.onKeyDown(keyCode, event);
                    activity.setMenuPopup(null, null);
                    break;
                case KeyEvent.KEYCODE_H:
                case KeyEvent.KEYCODE_I:
                case KeyEvent.KEYCODE_D:
                    baseItem = adapter.getItem(selectPostion);
                    if (baseItem == null) return true;
                    if (baseItem.getCurrentMenuLevel() == 2) {//二级菜单
                        dismiss();
                        onDestroy();
                        activity.setSecondPosition(-1);
                        activity.setThirdPosition(-1);
                        CuiNiaoApp.textSpeechManager.speakNow("返回上级菜单");
                        menuPopup = new CustomMenuPopup(activity, activity);
                        BasePopupView popupView = new XPopup.Builder(activity)
                                .isDestroyOnDismiss(true)
                                .hasStatusBarShadow(false)
                                .hasStatusBar(false)
                                .asCustom(menuPopup)
                                .show();
                        activity.setMenuPopup(menuPopup, popupView);
                    } else {//三级菜单
                        activity.setThirdPosition(-1);
                        selectPostion = activity.getSecondPosition() + 1;
                        if (itemList == null) {
                            itemList = new ArrayList<>();
                        } else {
                            itemList.clear();
                        }
                        if (activity.getBaseItemList().get(activity.getFirstPosition()).getNextMenu() != null)
                            itemList.addAll(activity.getBaseItemList().get(activity.getFirstPosition()).getNextMenu());
                        itemList.add(0, null);
                        itemList.add(null);
                        Log.d("TAG", "onKeyDown: itemList；" + itemList.size());
                        adapter.clearAll();
                        adapter.addAll(itemList);
                        if (selectPostion <= 1) {
                            selectPostion = 1;
                        } else if (selectPostion >= adapter.getCount() - 2) {
                            selectPostion = adapter.getCount() - 2;
                        }
                        adapter.notifyDataSetChanged();
                        listLeft.setSelection(selectPostion);
                        listRight.setSelection(selectPostion);
                        CuiNiaoApp.textSpeechManager.speakNow("返回上级菜单");
//                        textLeft.setText(itemName);
//                        textRight.setText(itemName);

                    }
                    baseItem.onKeyDown(keyCode);
                    return true;
                case KeyEvent.KEYCODE_C:
                    selectPostion++;
                    if (selectPostion <= 1) {
                        selectPostion = 1;
                    } else if (selectPostion >= adapter.getCount() - 2) {
                        selectPostion = adapter.getCount() - 2;
                    }
                    listLeft.setSelection(selectPostion);
                    listRight.setSelection(selectPostion);
                    baseItem = adapter.getItem(selectPostion);
                    if (baseItem == null) return true;
                    baseItem.speak();
                    adapter.notifyDataSetChanged();
                    return true;
                case KeyEvent.KEYCODE_B:
                    selectPostion--;
                    if (selectPostion <= 1) {
                        selectPostion = 1;
                    } else if (selectPostion >= adapter.getCount() - 2) {
                        selectPostion = adapter.getCount() - 2;
                    }
                    listLeft.setSelection(selectPostion);
                    listRight.setSelection(selectPostion);
                    baseItem = adapter.getItem(selectPostion);
                    if (baseItem == null) return true;
                    baseItem.speak();
                    adapter.notifyDataSetChanged();
                    return true;
//                case KeyEvent.KEYCODE_D:
//                case KeyEvent.KEYCODE_E:
//                    baseItem = t.getItem(selectPostion);
//                    baseItem.onKeyEvent(keyCode);
//                    t.notifyDataSetChanged();
//                    if (baseItem instanceof FontSizeSetItem) {
//                        setViewScale();
//                    }
//                    return true;
                case KeyEvent.KEYCODE_F://enter
                case KeyEvent.KEYCODE_E:
                    baseItem = adapter.getItem(selectPostion);
                    if (baseItem == null) return true;
                    if (baseItem.getCurrentMenuLevel() == 2) {
                        activity.setSecondPosition(selectPostion - 1);
                    } else {
                        activity.setThirdPosition(selectPostion - 1);
                    }
                    ((CustomMenuPopupThree) menuPopup).setBaseItem(baseItem);
                    baseItem.onKeyDown(keyCode);
                    boolean needIntent = baseItem.intentToMenu2((CustomMenuPopupThree) menuPopup);
                    if (itemList == null) {
                        itemList = new ArrayList<>();
                    } else {
                        itemList.clear();
                    }
                    if (baseItem.getNextMenu() != null)
                        itemList.addAll(baseItem.getNextMenu());
                    if (itemList != null && itemList.size() > 0) {//如果返回的二级菜单列表不为空就刷新列表
                        itemList.add(0, null);
                        itemList.add(null);
                        adapter.clearAll();
                        adapter.addAll(itemList);
                        adapter.notifyDataSetChanged();
                        selectPostion = 0;
                        if (selectPostion <= 1) {
                            selectPostion = 1;
                        } else if (selectPostion >= adapter.getCount() - 2) {
                            selectPostion = adapter.getCount() - 2;
                        }
                        listLeft.setSelection(selectPostion);
                        listRight.setSelection(selectPostion);
                        adapter.getItem(selectPostion).speak();
//                        activity.setItemName(baseItem.getMenuName());
//                        itemName = activity.getItemName();
//                        textLeft.setText(itemName);
//                        textRight.setText(itemName);
                        return true;
                    }
                    if (needIntent) {
                        dismiss();
                        activity.displayPreview();
                        activity.setMenuPopup(null, null);
                    } else {
                        baseItem.speak();
                        adapter.notifyDataSetChanged();
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
                    activity.notifyError();
                    break;
            }
        } else {
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP://音量+ 放大
                case KeyEvent.KEYCODE_VOLUME_DOWN://音量- 缩小
                    //长按会触发系统音量增大减小
                    return true;
                case KeyEvent.KEYCODE_0:
//                    if (System.currentTimeMillis() - currentTimeMillis > 2000 && !isShutDown) {
//                        isShutDown = true;
//                        //go to shutdown with animation
//                        Intent intent = new Intent(activity,ShutDownActivity.class);
//                        activity.startActivity(intent);
//                        activity.finish();
//                    }
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private List<BaseItem> itemList;
    private boolean isShutDown = false;
    private long currentTimeMillis = 0l;


    @Override
    protected void onDismiss() {
        super.onDismiss();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    class MyListAdapter extends BaseAdapter {
        LayoutInflater inflater;

        private List<BaseItem> list;

        public MyListAdapter(Context context, List<BaseItem> list) {
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
            MyListAdapter.ViewHolder viewHolder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.second_list_item, null);
                viewHolder = new MyListAdapter.ViewHolder();
                viewHolder.layoutView = convertView.findViewById(R.id.second_list_item_layout);
                viewHolder.itemName = (TextView) convertView.findViewById(R.id.second_list_item_text1);
//                viewHolder.itemCont = (TextView) convertView.findViewById(R.id.second_list_item_text2);
                viewHolder.itemImage = convertView.findViewById(R.id.second_list_item_image);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (MyListAdapter.ViewHolder) convertView.getTag();
            }
            if (list.get(i) == null) {
                viewHolder.itemName.setText(" ");
                return convertView;
            }
            if (list.get(i).getMenuCount() != null && !list.get(i).getMenuCount().trim().equals(""))
                viewHolder.itemName.setText(list.get(i).getMenuName() + "(" + list.get(i).getMenuCount() + ")");
            else
                viewHolder.itemName.setText(list.get(i).getMenuName());
            if (selectPostion == i) {
//                viewHolder.itemName.setTypeface();
//                viewHolder.itemCont.setTypeface(ttf);
                TextPaint tp = viewHolder.itemName.getPaint();
                tp.setFakeBoldText(true);
//                if (getList().get(i).isImage()) {
//                    viewHolder.itemCont.setVisibility(GONE);
//                    viewHolder.itemImage.setVisibility(VISIBLE);
                if (getList().get(i).getItemCountImage(true) != 0)
                    viewHolder.itemImage.setImageResource(getList().get(i).getItemCountImage(true));
                else
                    viewHolder.itemImage.setImageResource(CuiNiaoApp.isYellowMode ? R.mipmap.right_y : R.mipmap.right);
//                } else {
//                    viewHolder.itemImage.setVisibility(GONE);
//                    tp = viewHolder.itemCont.getPaint();
//                    tp.setFakeBoldText(true);
//                    viewHolder.itemCont.setVisibility(VISIBLE);
//                    viewHolder.itemCont.setText(getList().get(i).getMenuCount());
//                }
                viewHolder.layoutView.setBackgroundResource(CuiNiaoApp.isYellowMode ? R.drawable.second_list_item_selected_y : R.drawable.second_list_item_selected);
                viewHolder.itemName.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.second_list_text_list_select_size));
//                viewHolder.itemCont.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.second_list_text_list_select_size));
                viewHolder.itemName.setTextColor(CuiNiaoApp.isYellowMode ? activity.getResources().getColor(R.color.yellow_mode_selected) : activity.getResources().getColor(R.color.white));
//                viewHolder.itemCont.setTextColor(Color.parseColor("#FFFFFF"));
//                showUpAndDown();
            } else {
//                viewHolder.itemName.setTypeface(ttfRegular);
//                viewHolder.itemCont.setTypeface(ttfRegular);
                TextPaint tp = viewHolder.itemName.getPaint();
                tp.setFakeBoldText(false);
//                if (getList().get(i).isImage()) {
//                    viewHolder.itemCont.setVisibility(GONE);
//                    viewHolder.itemImage.setVisibility(VISIBLE);
                if (getList().get(i).getItemCountImage(true) != 0)
                    viewHolder.itemImage.setImageResource(getList().get(i).getItemCountImage(false));
                else
                    viewHolder.itemImage.setImageResource(0);
//                } else {
//                    viewHolder.itemImage.setVisibility(GONE);
//                    tp = viewHolder.itemCont.getPaint();
//                    tp.setFakeBoldText(true);
//                    viewHolder.itemCont.setVisibility(VISIBLE);
//                    viewHolder.itemCont.setText(getList().get(i).getMenuCount());
//                }
                viewHolder.layoutView.setBackgroundResource(R.drawable.second_list_item_normal);
                viewHolder.itemName.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.second_list_text_list_normal_size));
//                viewHolder.itemCont.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.second_list_text_list_normal_size));
//                viewHolder.itemName.setTextColor(Color.parseColor("#77FFFFFF"));
                viewHolder.itemName.setTextColor(CuiNiaoApp.isYellowMode ? activity.getResources().getColor(R.color.yellow_mode_unselected) : activity.getResources().getColor(R.color.unselected_white));
//                viewHolder.itemCont.setTextColor(Color.parseColor("#77FFFFFF"));
            }
            return convertView;
        }

        public List<BaseItem> getList() {
            return list;
        }

        private class ViewHolder {
            public TextView itemName;
            //            public TextView itemCont;
            public ImageView itemImage;
            public View layoutView;

        }

    }
}
