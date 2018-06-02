package com.github.aop.log;

import com.github.aop.log.api.*;

public class EasyLogResolver implements LogResolver {
    @Override
    public Logger getLogger(Class<?> clazz) {
        return new EasyLogger(clazz);
    }
}
