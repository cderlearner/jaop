package com.github.aop.util.log.api;

public interface Logger {
    void info(String format);

    void info(String format, Object... arguments);

    void warn(String format, Object... arguments);

    void warn(Throwable e, String format, Object... arguments);

    void error(String format, Throwable e);

    void error(Throwable e, String format, Object... arguments);

    boolean isDebugEnabled();

    boolean isInfoEnabled();

    boolean isWarnEnabled();

    boolean isErrorEnabled();

    void debug(String format);

    void debug(String format, Object... arguments);

    void error(String format);
}
