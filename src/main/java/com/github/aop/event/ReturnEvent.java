package com.github.aop.event;


public class ReturnEvent extends InvokeEvent {

    /**
     * 调用返回值
     */
    public Object object;

    /**
     * 构造调用RETURN事件
     *
     * @param object 调用返回值(void方法返回值为null)
     */
    public ReturnEvent(final Object object) {
        super(Type.RETURN);
        this.object = object;
    }

}
