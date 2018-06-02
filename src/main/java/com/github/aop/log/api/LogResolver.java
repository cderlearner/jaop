package com.github.aop.log.api;

public interface LogResolver {

    Logger getLogger(Class<?> clazz);
}
