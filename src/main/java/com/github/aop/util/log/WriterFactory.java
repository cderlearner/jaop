package com.github.aop.util.log;

import com.github.aop.util.log.api.*;
import com.github.aop.util.log.writer.*;

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
