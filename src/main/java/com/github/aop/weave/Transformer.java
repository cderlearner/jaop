package com.github.aop.weave;

import com.github.aop.exception.AOPException;

public interface Transformer {

    byte[] toByteCodeArray(ClassLoader classLoader, byte[] srcByteArray) throws AOPException;
}
