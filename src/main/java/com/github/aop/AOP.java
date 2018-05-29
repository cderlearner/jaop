package com.github.aop;

import com.github.aop.listener.EventListener;
import com.github.aop.weave.IWeaveManager;
import com.github.aop.weave.WeaveManager;

public class AOP {

    private Class targetClass;
    private String targetMethodName;
    private EventListener eventListener;

    public AOP(Builder builder) {
        this.targetClass = builder.targetClass;
        this.targetMethodName = builder.targetMethodName;
        this.eventListener = builder.eventListener;
    }

    private IWeaveManager weaveManager = WeaveManager.getSingleton();

    public Object create() throws Throwable{
       Class clazz =  weaveManager.weaveExten(targetClass, targetMethodName, eventListener);
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
