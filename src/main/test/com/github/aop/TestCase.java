package com.github.aop;

import com.github.aop.event.BeforeEvent;
import com.github.aop.event.Event;
import com.github.aop.event.ReturnEvent;
import org.junit.Ignore;
import org.junit.Test;
import com.github.aop.listener.*;

@Ignore
public class TestCase {

    @Ignore
    @Test
    public void testComputer() throws Throwable{

        AOP aop = new AOP.Builder().
                targetClass(Computer.class)
                .targetMethodName("sum")
                .eventListener(new EventListener() {
                    @Override
                    public void onEvent(Event event) throws Throwable {
                        if(event instanceof BeforeEvent) {
                            final BeforeEvent beforeEvent = (BeforeEvent) event;
                            System.err.println("hello 我是before事件");

                            final int[] numberArray = (int[]) beforeEvent.argumentArray[0];

                            numberArray[0] = 100;
                            numberArray[1] = 101;

                            System.err.println("hello 我修改了参数值");

                        }else if(event instanceof ReturnEvent) {

                            System.err.println("hello 我是return事件");
                        }
                    }
                })
                .build();

        Computer computer = (Computer) aop.create();

        System.err.println(computer.sum(new int[]{1,1}));

    }
}
