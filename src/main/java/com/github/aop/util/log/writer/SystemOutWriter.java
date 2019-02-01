package com.github.aop.util.log.writer;

import com.github.aop.util.log.api.*;
import java.io.PrintStream;

public enum SystemOutWriter implements IWriter {
    INSTANCE;
    @Override
    public void write(String message) {
        PrintStream out = System.out;
        out.println(message);
    }
}
