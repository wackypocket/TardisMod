package io.darkcraft.darkcore.mod.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TMLLog {

    public enum Level {
        NONE,
        ERROR,
        WARN,
        INFO,
        DEBUG
    }

    private static volatile Level level = Level.INFO;

    public static void setLevel(Level l) {
        if (l == null) return;
        level = l;
    }

    public static Level getLevel() {
        return level;
    }

    public static boolean isDebugEnabled() {
        return level.ordinal() >= Level.DEBUG.ordinal();
    }

    public static boolean isInfoEnabled() {
        return level.ordinal() >= Level.INFO.ordinal();
    }

    private static final Logger LOGGER = LogManager.getLogger("TML");

    public static void log(Level l, String tag, String message) {
        if (l == null) return;
        if (level == Level.NONE) return;
        if (l.ordinal() > level.ordinal()) return;
        String out = message;
        org.apache.logging.log4j.Level lvl = org.apache.logging.log4j.Level.INFO;
        switch (l) {
            case ERROR:
                lvl = org.apache.logging.log4j.Level.ERROR;
                break;
            case WARN:
                lvl = org.apache.logging.log4j.Level.WARN;
                break;
            case INFO:
                lvl = org.apache.logging.log4j.Level.INFO;
                break;
            case DEBUG:
                lvl = org.apache.logging.log4j.Level.DEBUG;
                break;
            default:
                lvl = org.apache.logging.log4j.Level.INFO;
                break;
        }
        LOGGER.log(lvl, out);
    }

    public static void debug(String tag, String msg) {
        log(Level.DEBUG, tag, msg);
    }

    public static void info(String tag, String msg) {
        log(Level.INFO, tag, msg);
    }

    public static void warn(String tag, String msg) {
        log(Level.WARN, tag, msg);
    }

    public static void error(String tag, String msg) {
        log(Level.ERROR, tag, msg);
    }

    /**
     * Log an info-level message regardless of the TMLLog level setting.
     */
    public static void alwaysInfo(String msg) {
        if (msg == null) return;
        LOGGER.info(msg);
    }
}
