package com.github.aop.weave;

public interface Transformer {

    byte[] toByteCodeArray(ClassLoader classLoader, byte[] srcByteArray) throws Throwable;
}
