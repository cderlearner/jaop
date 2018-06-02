package com.github.aop.log;

import com.github.aop.log.api.*;
import com.github.aop.log.writer.*;

public class WriterFactory {
    public static IWriter getLogWriter() {
        boolean findPackagePathAndDir = false;
        if (findPackagePathAndDir) {
            return FileWriter.get();
        } else {
            return SystemOutWriter.INSTANCE;
        }
    }
}
