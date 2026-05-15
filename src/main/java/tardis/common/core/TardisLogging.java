package tardis.common.core;

import io.darkcraft.darkcore.mod.DarkcoreMod;
import io.darkcraft.darkcore.mod.logging.TMLLog;
import tardis.Configs;

public final class TardisLogging {

    private TardisLogging() {}

    public static void configureLogging() {
        int p = Configs.priorityLevel.ordinal();
        TMLLog.Level lvl;
        if (p >= TardisOutput.Priority.OLDDEBUG.ordinal()) lvl = TMLLog.Level.DEBUG;
        else if (p >= TardisOutput.Priority.DEBUG.ordinal()) lvl = TMLLog.Level.DEBUG;
        else if (p >= TardisOutput.Priority.INFO.ordinal()) lvl = TMLLog.Level.INFO;
        else if (p >= TardisOutput.Priority.WARNING.ordinal()) lvl = TMLLog.Level.WARN;
        else if (p >= TardisOutput.Priority.ERROR.ordinal()) lvl = TMLLog.Level.ERROR;
        else lvl = TMLLog.Level.NONE;

        TMLLog.setLevel(lvl);
        DarkcoreMod.debugText = TMLLog.isDebugEnabled();
    }
}
