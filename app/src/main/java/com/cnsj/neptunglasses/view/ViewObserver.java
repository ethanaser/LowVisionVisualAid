package com.cnsj.neptunglasses.view;

/**
 * Created by Zph on 2020/7/29.
 */
public interface ViewObserver {
    void changeView();

    void notifyWifiConnected();

    void popupDismiss();

    void changeContent(String text);
}
