package com.github.aop.util.log.api;

public interface LogResolver {

    Logger getLogger(Class<?> clazz);
}
