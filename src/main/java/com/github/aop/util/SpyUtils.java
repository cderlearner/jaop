package com.github.aop.util;

import com.github.aop.weave.Spy;
import com.github.aop.listener.EventListenerHandlers;

/**
 * Spy类操作工具类
 */
public class SpyUtils {

    private static final Initializer isSpyInit = new Initializer();

    /**
     * 初始化Spy类
     *
     * @throws Throwable 初始化失败
     */
    public synchronized static void init() throws Throwable {

        if (isSpyInit.isInitialized()) {
            return;
        }

        isSpyInit.initProcess(new Initializer.Processor() {
            @Override
            public void process() throws Throwable {
                Spy.init(
                        ReflectUtils.unCaughtGetClassDeclaredJavaMethod(EventListenerHandlers.class, "onBefore",
                                int.class,
                                String.class,
                                String.class,
                                String.class,
                                Object.class,
                                Object[].class
                        ),
                        ReflectUtils.unCaughtGetClassDeclaredJavaMethod(EventListenerHandlers.class, "onReturn",
                                int.class,
                                Object.class
                        )
                );
            }
        });

    }

}
