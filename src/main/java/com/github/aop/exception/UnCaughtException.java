package com.github.aop.exception;

/**
 * 未捕获异常
 * 用来封装不希望抛出的异常
 */
public class UnCaughtException extends RuntimeException {

    public UnCaughtException(Throwable cause) {
        super(cause);
    }
}
