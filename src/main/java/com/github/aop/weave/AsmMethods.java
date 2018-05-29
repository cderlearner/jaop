package com.github.aop.weave;

import com.github.aop.util.*;
import org.objectweb.asm.commons.Method;

public interface AsmMethods {

    class InnerHelper {
        private InnerHelper() {
        }

        public static Method getAsmMethod(final Class<?> clazz,
                                   final String methodName,
                                   final Class<?>... parameterClassArray) {
            return Method.getMethod(ReflectUtils.unCaughtGetClassDeclaredJavaMethod(clazz, methodName, parameterClassArray));
        }
    }

    Method ASM_METHOD_Spy$spyMethodOnBefore = InnerHelper.getAsmMethod(
            Spy.class,
            "spyMethodOnBefore",
            Object[].class,
            int.class,
            String.class,
            String.class,
            String.class,
            Object.class
    );

    Method ASM_METHOD_Spy$spyMethodOnReturn = InnerHelper.getAsmMethod(
            Spy.class,
            "spyMethodOnReturn",
            Object.class,
            int.class
    );


}
