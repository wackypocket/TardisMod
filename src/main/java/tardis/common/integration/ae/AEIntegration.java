package tardis.common.integration.ae;

import net.minecraft.item.ItemStack;

import appeng.api.AEApi;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Optional;

/**
 * Safe wrapper for Applied Energistics 2 integration that doesn't crash when AE2 is not present
 */
public class AEIntegration {

    private static final String AE2_MOD_ID = "appliedenergistics2";
    private static boolean initialized = false;
    private static boolean ae2Available = false;
    private static Class<?> iaePowerStorageClass = null;

    public static void initialize() {
        if (initialized) return;
        initialized = true;

        ae2Available = Loader.isModLoaded(AE2_MOD_ID);
        if (ae2Available) {
            try {
                iaePowerStorageClass = Class.forName("appeng.api.implementations.items.IAEItemPowerStorage");
            } catch (Exception e) {
                ae2Available = false;
                iaePowerStorageClass = null;
            }
        }
    }

    public static boolean isAE2Available() {
        return ae2Available;
    }

    /**
     * Safely check if an item is an AE2 electric item
     */
    public static boolean isItemElectric(ItemStack stack) {
        if (!ae2Available || stack == null || iaePowerStorageClass == null) {
            return false;
        }

        try {
            return iaePowerStorageClass.isInstance(stack.getItem());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Safely create a grid node for the given IGridBlock
     * Uses AE2 API directly instead of reflection
     */
    @Optional.Method(modid = AE2_MOD_ID)
    public static IGridNode createGridNode(IGridBlock gridBlock) {
        if (!ae2Available) {
            return null;
        }

        try {
            return AEApi.instance()
                .createGridNode(gridBlock);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Safely create a grid connection between two nodes
     * Uses AE2 API directly instead of reflection
     */
    @Optional.Method(modid = AE2_MOD_ID)
    public static IGridConnection createGridConnection(IGridNode nodeA, IGridNode nodeB) {
        if (!ae2Available) {
            return null;
        }

        try {
            return AEApi.instance()
                .createGridConnection(nodeA, nodeB);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Safely get the Certus Quartz Crystal ItemStack
     * Returns null if AE2 is not available or if there's an error
     */
    public static ItemStack getCertusQuartzCrystal() {
        if (!ae2Available) {
            return null;
        }

        try {
            // Use AE2 API directly for getting materials
            Object materials = AEApi.instance()
                .materials();
            if (materials == null) {
                return null;
            }

            // Use reflection only for accessing materials (not in public API)
            java.lang.reflect.Field crystalField = materials.getClass()
                .getField("materialCertusQuartzCrystal");
            Object crystal = crystalField.get(materials);
            if (crystal == null) {
                return null;
            }

            // Use the direct stack() method
            java.lang.reflect.Method stackMethod = crystal.getClass()
                .getMethod("stack", int.class);
            stackMethod.setAccessible(true);
            Object stack = stackMethod.invoke(crystal, 1);

            if (stack instanceof ItemStack) {
                return (ItemStack) stack;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static final String MOD_ID = AE2_MOD_ID;
}
