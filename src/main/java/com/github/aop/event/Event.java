package com.github.aop.event;


public class Event {

    public volatile int eventId = -1;

    public volatile TypeStatus defaultStatus = TypeStatus.INIT;

    /**
     * 事件类型
     */
    public Type type;

    /**
     * 构造调用事件
     *
     * @param type 事件类型
     */
    protected Event(Type type) {
        this.type = type;
    }

    public Event() {

    }

    /**
     * 事件枚举类型
     */
    public enum Type {

        /**
         * 调用:BEFORE
         */
        BEFORE,

        /**
         * 调用:RETURN
         */
        RETURN,

    }

    public enum TypeStatus {
        INIT,

        USING,

        CAN_USE,
    }

}
