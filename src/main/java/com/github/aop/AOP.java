package com.github.aop;

import com.github.aop.exception.AOPException;
import com.github.aop.listener.EventListener;
import com.github.aop.listener.EventListenerHandlers;
import com.github.aop.util.Assert;
import com.github.aop.weave.IWeaveManager;
import com.github.aop.weave.WeaveManager;

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
    public Class createReflectClass() throws AOPException {
        Assert.notNull(targetClass, "目标类不能为空");
        Assert.notNull(eventListener, "aop监听器不能为空");
        Assert.notBlank(targetMethodName, "目标方法不能为空");
        return weaveManager.weave(targetClass, targetMethodName, eventListener);
    }

    public Class createCgFactory() throws Throwable {
        Assert.notNull(targetClass, "目标类不能为空");
        Assert.notNull(eventListener, "aop监听器不能为空");
        Assert.notBlank(targetMethodName, "目标方法不能为空");
        return weaveManager.weaveSubClass(targetClass, targetMethodName, eventListener);
    }

    public Object createIntance() throws Throwable {
        Assert.notNull(targetClass, "目标类不能为空");
        Assert.notNull(eventListener, "aop监听器不能为空");
        Assert.notBlank(targetMethodName, "目标方法不能为空");
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
