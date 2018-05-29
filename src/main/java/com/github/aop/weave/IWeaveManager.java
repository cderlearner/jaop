package com.github.aop.weave;

import com.github.aop.filter.Filter;
import com.github.aop.listener.EventListener;

import java.util.List;

public interface IWeaveManager {

    Class weave(Class targetClass, String targetMethodName,
                EventListener eventListener) throws Throwable;


    Class weave(Class targetClass, List<String> targetMethodNames,
                EventListener eventListener) throws Throwable;


    Class weave(Class targetClass, Filter filter, String targetMethodName,
                EventListener eventListener) throws Throwable;


    Class weaveExten(Class targetClass, String targetMethodName,
                     EventListener eventListener) throws Throwable;

}
