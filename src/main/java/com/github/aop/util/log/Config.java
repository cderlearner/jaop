package com.github.aop.util.log;

public class Config {

    public static class Logging {

        public static String FILE_NAME = "easy.log";

        /**
         * Log files directory. Default is blank string, means, use "system.out" to output logs.
         *
         * @see {@link WriterFactory#getLogWriter()}
         */
        public static String DIR = "E:\\ljx";

        /**
         * The max size of log file. If the size is bigger than this, archive the current file, and write into a new
         * file.
         */
        public static int MAX_FILE_SIZE = 300 * 1024 * 1024;


        public static LogLevel LEVEL = LogLevel.DEBUG;
    }

}
