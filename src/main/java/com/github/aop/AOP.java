package com.github.aop;

import com.github.aop.listener.EventListener;
import com.github.aop.listener.EventListenerHandlers;
import com.github.aop.weave.IWeaveManager;
import com.github.aop.weave.WeaveManager;
import org.omg.CORBA.ObjectHelper;

public class AOP {

    public static final EventListenerHandlers listenerHandlers = EventListenerHandlers.getSingleton();

    private Class targetClass;
    private String targetMethodName;
    private EventListener eventListener;

    public AOP(Builder builder) {
        this.targetClass = builder.targetClass;
        this.targetMethodName = builder.targetMethodName;
        this.eventListener = builder.eventListener;
    }

    private IWeaveManager weaveManager = WeaveManager.getSingleton();

    //只能反射调用
    public Class createReflectClass() throws Throwable{
       Class clazz =  weaveManager.weave(targetClass, targetMethodName, eventListener);
       return clazz;
    }

    public Class createCgFactory() throws Throwable {
        Class clazz = weaveManager.weaveSubClass(targetClass, targetMethodName, eventListener);
        return clazz;
    }

    public Object createIntance() throws Throwable {
        Class clazz = weaveManager.weaveSubClass(targetClass, targetMethodName, eventListener);
        return clazz.newInstance();
    }

    public static class Builder {
        private Class targetClass;
        private String targetMethodName;
        private EventListener eventListener;

        public Builder targetClass(Class targetClass) {
            this.targetClass = targetClass;
            return this;
        }

        public Builder targetMethodName(String targetMethodName) {
            this.targetMethodName = targetMethodName;
            return this;
        }

        public Builder eventListener(EventListener eventListener) {
            this.eventListener = eventListener;
            return this;
        }

        public AOP build() {
            return new AOP(this);
        }

    }
}
