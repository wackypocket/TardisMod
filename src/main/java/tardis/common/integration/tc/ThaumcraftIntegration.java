package tardis.common.integration.tc;

import cpw.mods.fml.common.Loader;

/**
 * Safe wrapper for Thaumcraft integration that doesn't crash when Thaumcraft is not present
 */
public class ThaumcraftIntegration {

    private static final String THAUMCRAFT_MOD_ID = "Thaumcraft";
    private static boolean initialized = false;
    private static boolean thaumcraftAvailable = false;

    public static void initialize() {
        if (initialized) return;
        initialized = true;

        thaumcraftAvailable = Loader.isModLoaded(THAUMCRAFT_MOD_ID);
        if (thaumcraftAvailable) {
            io.darkcraft.darkcore.mod.logging.TMLLog.alwaysInfo("Thaumcraft is installed. Initializing Integration.");
        } else {
            io.darkcraft.darkcore.mod.logging.TMLLog.alwaysInfo("Thaumcraft is not installed. Skipping Integration.");
        }

        if (thaumcraftAvailable) {
            try {
                // Try to load a Thaumcraft class to make sure it's really available
                Class.forName("thaumcraft.api.aspects.Aspect");
                io.darkcraft.darkcore.mod.logging.TMLLog.info("", "Thaumcraft integration initialized successfully.");
            } catch (Exception e) {
                io.darkcraft.darkcore.mod.logging.TMLLog
                    .alwaysInfo("Failed to initialize Thaumcraft integration: " + e.getMessage());
                thaumcraftAvailable = false;
            }
        }
    }

    public static boolean isThaumcraftAvailable() {
        return thaumcraftAvailable;
    }

    public static final String MOD_ID = THAUMCRAFT_MOD_ID;
}
