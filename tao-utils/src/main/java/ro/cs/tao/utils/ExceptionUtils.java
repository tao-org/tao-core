package ro.cs.tao.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class providing helper methods for handling and formatting exceptions.
 * This class includes methods to generate log messages and stack traces from
 * exceptions based on configurable criteria.
 *
 * The methods in this class are designed to either return a detailed stack trace
 * or a summarized format depending on the specified logging level or stack frame
 * limit.
 *
 * This is a utility class and is not designed to be instantiated.
 */
public class ExceptionUtils {

    /**
     * Generates a log message from the provided exception with an optional limit on the number of stack frames included.
     * This method either returns a detailed stack trace or a summarized format based on a configurable property.
     *
     * @param t The exception from which the log message or stack trace will be generated. Must not be null.
     * @param stackFrameCount The number of stack frames to include in the log message. If 0 or a negative value is provided, no stack frames are included.
     * @return A string representation of the exception and its partial or full stack trace based on the input parameters and configuration.
     */
    public static String getExceptionLoggingMessage(Throwable t, int stackFrameCount) {
        //String logExceptionValue = System.getProperty("log.exception.stack.trace");
        String logExceptionValue = "false";
        if (Boolean.parseBoolean(logExceptionValue)) {
            return org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(t);
        } else {
            StringBuilder buffer = new StringBuilder();
            buffer.append(t.getClass().getName())
                  .append(": ")
                  .append(t.getMessage());
            if (stackFrameCount > 0) {
                StackTraceElement[] stackTrace = t.getStackTrace();
                for (int i = 0; i < stackFrameCount && i < stackTrace.length; i++) {
                    buffer.append("\n\tat ")
                            .append(stackTrace[i].toString());
                }
            }
            return buffer.toString();
        }
    }

    /**
     * Generates a stack trace string for the provided exception based on the logger's configuration.
     * If the logger or its parent is configured at a fine logging level or lower, the full stack trace
     * is returned. Otherwise, a summarized version with only the first stack trace element and the
     * exception message is returned.
     *
     * @param logger The logger whose logging level determines the verbosity of the stack trace. Must not be null.
     * @param t The throwable from which the stack trace will be derived. Must not be null.
     * @return A string representation of the stack trace based on the logger's logging level.
     */
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
