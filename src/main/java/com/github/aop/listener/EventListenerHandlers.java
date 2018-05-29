package com.github.aop.listener;

import com.github.aop.event.BeforeEvent;
import com.github.aop.event.Event;
import com.github.aop.util.EventBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 事件处理
 **/
public class EventListenerHandlers {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final AtomicInteger sequencer = new AtomicInteger(1000);

    // 全局处理器ID:处理器映射集合
    private final Map<Integer/*LISTENER_ID*/, EventListener> globalEventListenerMap
            = new ConcurrentHashMap<Integer, EventListener>();

    private final EventBuffer eventPool = new EventBuffer();

    public int createAndActiveListenerId(EventListener eventListener) {
        int listenerId = sequencer.getAndIncrement();
        active(listenerId, eventListener);
        return listenerId;
    }

    /**
     * 注册事件处理器
     *
     * @param listenerId     事件监听器ID
     * @param listener       事件监听器
     */
    private void active(final int listenerId,
                       final EventListener listener) {
        globalEventListenerMap.put(listenerId, listener);
        logger.info("active listener success. listener-id={};listener={};", listenerId, listener);
    }

    /**
     * 取消事件处理器
     *
     * @param listenerId 事件处理器ID
     */
    public void frozen(int listenerId) {
        final EventListener listener = globalEventListenerMap.remove(listenerId);
        if (null == listener) {
            return;
        }

        logger.info("frozen listener success. listener-id={};listener={};", listenerId, listener);
    }

    /**
     * 调用出发事件处理&调用执行流程控制
     *
     * @param listenerId 处理器ID
     * @param event      调用事件
     * @param listener       事件处理器
     * @return 处理返回结果
     * @throws Throwable 当出现未知异常时,且事件处理器为中断流程事件时抛出
     */
    private void handleEvent(final int listenerId,
                                final Event event,
                                final EventListener listener) throws Throwable {

        try {

            // 调用事件处理
            listener.onEvent(event);
            if (logger.isDebugEnabled()) {
                logger.debug("listener onEvent success, listener-id={};type={}",
                        listenerId, event.type
                );
            }

        }
        catch (Throwable throwable) {
            logger.error("", throwable);

        }
    }


    private void handleOnBefore(final int listenerId,
                                   final String javaClassName,
                                   final String javaMethodName,
                                   final String javaMethodDesc,
                                   final Object target,
                                   final Object[] argumentArray) throws Throwable {

        // 获取事件处理器
        final EventListener listener = globalEventListenerMap.get(listenerId);

        // 如果尚未注册,则直接返回,不做任何处理
        if (null == listener) {
            logger.debug("listener was not active yet, ignore this process. id={};", listenerId);
            return;
        }


        final BeforeEvent event = (BeforeEvent) eventPool.borrowBeforeEvent(
                Thread.currentThread().getContextClassLoader(),
                javaClassName,
                javaMethodName,
                javaMethodDesc,
                target,
                argumentArray
        );
        try {
            handleEvent(listenerId, event, listener);
        } finally {
            eventPool.returnEvent(event);
        }
    }

    private void handleOnEnd(final int listenerId,
                                final Object object,
                                final boolean isReturn) throws Throwable {

        final EventListener listener = globalEventListenerMap.get(listenerId);


//        final Event event = isReturn
//                ? eventPool.borrowReturnEvent(object)
//                : eventPool.borrowThrowsEvent(object);

        final Event event = eventPool.borrowReturnEvent(object);

        try {
            handleEvent(listenerId, event, listener);
        } finally {
            eventPool.returnEvent(event);
        }

    }


    // ----------------------------------- 从这里开始就是提供给Spy的static方法 -----------------------------------

    private EventListenerHandlers(){}

    private static final EventListenerHandlers singleton = new EventListenerHandlers();

    public static EventListenerHandlers getSingleton() {
        return singleton;
    }


    public static void onBefore(final int listenerId,
                                  final String javaClassName,
                                  final String javaMethodName,
                                  final String javaMethodDesc,
                                  final Object target,
                                  final Object[] argumentArray) throws Throwable {
        singleton.handleOnBefore(
                listenerId,
                javaClassName,
                javaMethodName,
                javaMethodDesc,
                target,
                argumentArray
        );
    }

    public static void onReturn(final int listenerId, final Object object) throws Throwable {
        singleton.handleOnEnd(listenerId, object, true);
    }

    public static void onThrows(final int listenerId,
                                  final Throwable throwable) throws Throwable {
        singleton.handleOnEnd(listenerId, throwable, false);
    }


}
