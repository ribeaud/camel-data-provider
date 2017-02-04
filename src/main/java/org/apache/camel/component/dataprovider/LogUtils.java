package org.apache.camel.component.dataprovider;

import org.slf4j.Logger;

import java.util.function.Supplier;

/**
 * Wrapper around {@link Logger} to make the use of <b>Java 8</b> <i>lamba</i> functions possible.
 *
 * @author <a href="mailto:christian.ribeaud@novartis.com">Christian Ribeaud</a>
 */
public class LogUtils {

    private LogUtils() {
        // Can NOT be instantiated
    }

    public static void trace(Logger logger, Supplier<String> message) {
        assert logger != null : "Unspecified logger";
        assert message != null : "Unspecified message supplier";
        if (logger.isTraceEnabled()) {
            logger.trace(message.get());
        }
    }

    public static void debug(Logger logger, Supplier<String> message) {
        assert logger != null : "Unspecified logger";
        assert message != null : "Unspecified message supplier";
        if (logger.isDebugEnabled()) {
            logger.debug(message.get());
        }
    }

    public static void info(Logger logger, Supplier<String> message) {
        assert logger != null : "Unspecified logger";
        assert message != null : "Unspecified message supplier";
        if (logger.isInfoEnabled()) {
            logger.info(message.get());
        }
    }

    public static void info(Logger logger, Supplier<String> message, Throwable th) {
        assert logger != null : "Unspecified logger";
        assert message != null : "Unspecified message supplier";
        if (logger.isInfoEnabled()) {
            logger.info(message.get(), th);
        }
    }

    public static void warn(Logger logger, Supplier<String> message) {
        assert logger != null : "Unspecified logger";
        assert message != null : "Unspecified message supplier";
        if (logger.isWarnEnabled()) {
            logger.warn(message.get());
        }
    }

    public static void warn(Logger logger, Supplier<String> message, Throwable th) {
        assert logger != null : "Unspecified logger";
        assert message != null : "Unspecified message supplier";
        if (logger.isWarnEnabled()) {
            logger.warn(message.get(), th);
        }
    }

    public static void error(Logger logger, Supplier<String> message) {
        assert logger != null : "Unspecified logger";
        assert message != null : "Unspecified message supplier";
        if (logger.isErrorEnabled()) {
            logger.error(message.get());
        }
    }

    public static void error(Logger logger, Supplier<String> message, Throwable th) {
        assert logger != null : "Unspecified logger";
        assert message != null : "Unspecified message supplier";
        if (logger.isErrorEnabled()) {
            logger.error(message.get(), th);
        }
    }
}
