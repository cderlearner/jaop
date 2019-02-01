package com.github.aop.util.log;

import com.github.aop.util.log.api.*;

public class EasyLogResolver implements LogResolver {
    @Override
    public Logger getLogger(Class<?> clazz) {
        return new EasyLogger(clazz);
    }
}
