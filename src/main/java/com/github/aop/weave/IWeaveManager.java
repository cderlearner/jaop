package com.github.aop.weave;

import com.github.aop.filter.Filter;
import com.github.aop.listener.EventListener;

import java.util.List;

public interface IWeaveManager {

    /**
     * 转换目标类，只能反射调用，不能newInstance赋值给目标类声明类型
     * @param targetClass
     * @param targetMethodName
     * @param eventListener
     * @return
     * @throws Throwable
     */
    Class weave(Class targetClass, String targetMethodName,
                EventListener eventListener) throws Throwable;


    Class weave(Class targetClass, List<String> targetMethodNames,
                EventListener eventListener) throws Throwable;


    Class weave(Class targetClass, Filter filter, String targetMethodName,
                EventListener eventListener) throws Throwable;


    /**
     * 生产目标类的子类
     * 不支持增强final修饰的类
     * @param targetClass
     * @param targetMethodName
     * @param eventListener
     * @return
     * @throws Throwable
     */
    Class weaveExten(Class targetClass, String targetMethodName,
                     EventListener eventListener) throws Throwable;

}
