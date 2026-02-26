package net.fataled.wynnstacks.client.Utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerUtils {

    private static Logger LOGGER;

    public static void initLogger() {
        LOGGER = LoggerFactory.getLogger("WynnStacks");
    }

    public static void info(String message) {
        LOGGER.info(message);
    }

    public static void warn(String message) {
        LOGGER.warn(message);
    }

    public static void error(String message) {
        LOGGER.error(message);
    }

}
