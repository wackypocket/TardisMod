package tardis.common.integration.waila;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInterModComms;

/**
 * Safe wrapper for WAILA integration that doesn't crash when WAILA is not present
 */
public class WailaIntegration {

    private static final String WAILA_MOD_ID = "Waila";
    private static boolean initialized = false;
    private static boolean wailaAvailable = false;

    public static void initialize() {
        if (initialized) return;
        initialized = true;

        wailaAvailable = Loader.isModLoaded(WAILA_MOD_ID);
        if (wailaAvailable) {
            io.darkcraft.darkcore.mod.logging.TMLLog.alwaysInfo("Waila is installed. Initializing Integration.");
        } else {
            io.darkcraft.darkcore.mod.logging.TMLLog.alwaysInfo("Waila is not installed. Skipping Integration.");
        }

        if (wailaAvailable) {
            try {
                // Try to load a WAILA class to make sure it's really available
                Class.forName("mcp.mobius.waila.api.IWailaDataAccessor");

                // Send the registration message to WAILA
                io.darkcraft.darkcore.mod.logging.TMLLog.info("", "Sending message to WAILA");
                FMLInterModComms
                    .sendMessage("Waila", "register", "tardis.common.integration.waila.WailaCallback.wailaRegister");
            } catch (Exception e) {
                io.darkcraft.darkcore.mod.logging.TMLLog
                    .alwaysInfo("Failed to initialize WAILA integration: " + e.getMessage());
                wailaAvailable = false;
            }
        }
    }

    public static boolean isWailaAvailable() {
        return wailaAvailable;
    }
}
