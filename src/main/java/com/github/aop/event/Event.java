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

        /**
         * 调用:THROWS
         */
        THROWS,


        //
        // CALL事件系列是从GREYS中衍生过来的事件，它描述了一个方法内部，调用其他方法的过程。整个过程可以被描述成为三个阶段
        //
        // void test() {
        //     # CALL_BEFORE
        //     try {
        //         logger.info("TEST");
        //         # CALL_RETURN
        //     } catch(Throwable cause) {
        //         # CALL_THROWS
        //     }
        // }
        //




    }

    public enum TypeStatus {
        INIT,

        USING,

        CAN_USE,
    }

}
