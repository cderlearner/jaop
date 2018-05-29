package com.github.aop.weave;

import java.lang.reflect.Method;

public class Spy {

    private static volatile Method ON_BEFORE_METHOD;
    private static volatile Method ON_RETURN_METHOD;

    /**
     * @param ON_BEFORE_METHOD ON_BEFORE 回调
     * @param ON_RETURN_METHOD ON_RETURN 回调
     */
    public static void init(final Method ON_BEFORE_METHOD,
                            final Method ON_RETURN_METHOD) {
        Spy.ON_BEFORE_METHOD = ON_BEFORE_METHOD;
        Spy.ON_RETURN_METHOD = ON_RETURN_METHOD;
    }

    public static void spyMethodOnBefore(final Object[] argumentArray,
                                        final int listenerId,
                                        final String javaClassName,
                                        final String javaMethodName,
                                        final String javaMethodDesc,
                                        final Object target) {
        try {
            ON_BEFORE_METHOD.invoke(null, listenerId,
                    javaClassName, javaMethodName, javaMethodDesc, target, argumentArray);

        }catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    public static void spyMethodOnReturn(final Object object,
                                        final int listenerId) {
        try {
            ON_RETURN_METHOD.invoke(null, listenerId, object);

        }catch (Throwable ex) {
            ex.printStackTrace();
        }
    }



}
