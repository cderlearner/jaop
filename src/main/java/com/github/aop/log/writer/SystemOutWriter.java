package com.github.aop.log.writer;

import com.github.aop.log.api.*;
import java.io.PrintStream;

public enum SystemOutWriter implements IWriter {
    INSTANCE;
    @Override
    public void write(String message) {
        PrintStream out = System.out;
        out.println(message);
    }
}
