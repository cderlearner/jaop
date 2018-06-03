package com.github.aop.exception;

public class AOPException extends Throwable{

    public AOPException(String message) {
        super(message);
    }

    public AOPException(Throwable throwable) {
        super(throwable);
    }
}
