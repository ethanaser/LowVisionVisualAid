package com.cnsj.neptunglasses.service;


/**
 * 头显消息观察者，用于向订阅者发送头显的消息
 */
public interface GlassesObservable {

    /**
     * 添加订阅者
     *
     * @param observer
     */
    void addObserver(GlassesObserver observer);

    /**
     * 删除订阅者
     *
     * @param observer
     */
    void deleteObserver(GlassesObserver observer);

    /**
     * 根据tag类型更新消息
     *
     * @param tag
     * @param arg
     */
    void update(int tag, Object arg);

}
