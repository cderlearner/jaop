package com.github.aop.event;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 事件缓存
 */
public class EventBuffer {

    private RingBuffer ringBuffer = new RingBuffer(1024);

    public EventBuffer() {
    }

    public Event borrowBeforeEvent(final ClassLoader javaClassLoader,
                             final String javaClassName,
                             final String javaMethodName,
                             final String javaMethodDesc,
                             final Object target,
                             final Object[] argumentArray) {
        Event event = ringBuffer.borrowEvent();
        if(event instanceof BeforeEvent) {
            BeforeEvent beforeEvent = (BeforeEvent) event;
            beforeEvent.javaClassLoader = javaClassLoader;
            beforeEvent.javaClassName = javaClassName;
            beforeEvent.javaMethodDesc = javaMethodDesc;
            beforeEvent.target= target;
            beforeEvent.argumentArray = argumentArray;
            return event;
        } else {
            BeforeEvent beforeEvent = new BeforeEvent(
                    javaClassLoader,
                    javaClassName,
                    javaMethodName,
                    javaMethodDesc,
                    target,
                    argumentArray
            );
            //更新状态
            beforeEvent.updateState(event.eventId, event.typeStatus);
            //覆盖缓存
            ringBuffer.coverEvent(beforeEvent);
            return beforeEvent;
        }
    }

    public Event borrowReturnEvent(final Object object) {
        Event event = ringBuffer.borrowEvent();
        if(event instanceof ReturnEvent) {
            ReturnEvent returnEvent = (ReturnEvent) event;
            returnEvent.object = object;
            return event;
        } else {
            ReturnEvent returnEvent = new ReturnEvent(object);

            returnEvent.updateState(event.eventId, event.typeStatus);

            ringBuffer.coverEvent(returnEvent);
            return returnEvent;
        }
    }

    public void returnEvent(Event event) {
        ringBuffer.returnEvent(event);
    }

    public static class RingBuffer {

        private Event[] events;
        private AtomicInteger sequecer = new AtomicInteger();

        public RingBuffer(int bufferSize) {
            if(!is2XValue(bufferSize)) {
                throw new IllegalArgumentException("bufferSize必须是2的n次方");
            }
            events = new Event[bufferSize];
        }

        public Event borrowEvent() {
            int next = sequecer.getAndIncrement();
            int index = next & (events.length - 1);

            Event event = events[index];

            if(event == null) {
                event = new Event();
                events[index] = event;
            }

            if(event.typeStatus.equals(Event.TypeStatus.USING)) {
                throw new IllegalStateException("什么情况，轮了一圈还在使用？");
            }

            updateState(event, Event.TypeStatus.USING, next);
            return event;
        }

        public void returnEvent(Event event) {
            if(event.eventId == -1) {
                throw new IllegalStateException("你在干啥");
            }
            updateState(event, Event.TypeStatus.CAN_USE, event.eventId);
        }

        private void updateState(Event event, Event.TypeStatus typeStatus, int eventId) {
            event.typeStatus = typeStatus;
            event.eventId = eventId;
        }

        public void coverEvent(Event event) {
            events[event.eventId] = event;
        }

        private boolean is2XValue(int value) {
            return value > 0 && (value & (value - 1)) == 0;
        }

    }
}
