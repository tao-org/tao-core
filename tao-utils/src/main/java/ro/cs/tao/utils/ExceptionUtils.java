package ro.cs.tao.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ExceptionUtils {
    public static String getStackTrace(Logger logger, Throwable t) {
        Level logLevel = logger.getLevel();
        if (logLevel == null && logger.getParent() != null) {
            logLevel = logger.getParent().getLevel();
        }
        if (logLevel == null) {
            logLevel = Level.FINE;
        }
        if (logLevel.intValue() <= Level.FINE.intValue()) {
            return org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(t);
        } else {
            final StringBuilder buffer = new StringBuilder();
            buffer.append(t.getMessage()).append("\n");
            final StackTraceElement[] stackTrace = t.getStackTrace();
            for (int i = 0; i < 1; i++) {
                buffer.append("\tat ").append(stackTrace[i].toString()).append("\n");
            }
            return buffer.toString();
        }
    }
}
