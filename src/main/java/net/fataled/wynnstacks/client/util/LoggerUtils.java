package net.fataled.wynnstacks.client.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LoggerUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger("WynnStacks");

    private LoggerUtils() {}

    // Parameterized variants — SLF4J substitutes "{}" with each arg in order.
    // The formatting only runs if the level is enabled, so log calls in hot
    // paths cost ~one method call when the level is off.
    public static void info(String format, Object... args)  { LOGGER.info(format, args);  }
    public static void warn(String format, Object... args)  { LOGGER.warn(format, args);  }
    public static void error(String format, Object... args) { LOGGER.error(format, args); }

    // Explicit Throwable overloads — preserve the full stack trace.
    // (SLF4J would also detect a trailing Throwable inside the varargs form,
    //  but having the explicit overloads makes intent obvious at the call site.)
    public static void warn(String msg, Throwable t)  { LOGGER.warn(msg, t);  }
    public static void error(String msg, Throwable t) { LOGGER.error(msg, t); }
}
