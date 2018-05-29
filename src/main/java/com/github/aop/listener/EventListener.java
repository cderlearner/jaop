package com.github.aop.listener;

import com.github.aop.event.Event;

/**
 * 事件监听器
 */
public interface EventListener {

    void onEvent(Event event) throws Throwable;
}
