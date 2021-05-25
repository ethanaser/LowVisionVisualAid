package com.cnsj.neptunglasses.view;

/**
 * Created by Zph on 2020/7/29.
 */
public interface ViewObservable<T> {
    void add(T t);
    void notifyViewChange(int i);
    void remove(T t);
}
