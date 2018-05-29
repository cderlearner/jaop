package com.github.aop.event;

public abstract class InvokeEvent extends Event{

    protected InvokeEvent(Type type) {
        super(type);
    }
}
