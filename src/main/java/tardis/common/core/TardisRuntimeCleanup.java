package tardis.common.core;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.common.FMLCommonHandler;
import io.darkcraft.darkcore.mod.handlers.packets.WorldDataStoreHandler;
import tardis.Configs;
import tardis.TardisMod;
import tardis.common.core.helpers.Helper;

public class TardisRuntimeCleanup {

    private TardisRuntimeCleanup() {}

    private static void unregisterRuntimeStores() {
        try {
            if (TardisMod.plReg != null) {
                MinecraftForge.EVENT_BUS.unregister(TardisMod.plReg);
                FMLCommonHandler.instance()
                    .bus()
                    .unregister(TardisMod.plReg);
            }
            if (TardisMod.dimReg != null) {
                MinecraftForge.EVENT_BUS.unregister(TardisMod.dimReg);
                FMLCommonHandler.instance()
                    .bus()
                    .unregister(TardisMod.dimReg);
            }
        } catch (Exception e) {}
        TardisMod.plReg = null;
        TardisMod.dimReg = null;
        WorldDataStoreHandler.clear();
        Helper.datastoreMap.clear();
        Helper.ssnDatastoreMap.clear();
    }

    private static void unregisterTardisDimensions() {
        try {
            if (TardisMod.dimReg != null) {
                Set<Integer> knownDims = TardisDimensionRegistry.getDims();
                ArrayList<Integer> dims = new ArrayList<Integer>(knownDims);
                for (Integer d : dims) TardisMod.dimReg.unregisterDim(d);
            }
        } catch (Exception e) {}

        try {
            Field f = DimensionManager.class.getDeclaredField("dimensions");
            f.setAccessible(true);
            Object o = f.get(null);
            if (o instanceof Hashtable) {
                @SuppressWarnings("unchecked")
                Hashtable<Integer, Integer> table = (Hashtable<Integer, Integer>) o;
                ArrayList<Integer> dims = new ArrayList<Integer>(table.keySet());
                for (Integer dim : dims) {
                    if (dim == null) continue;
                    try {
                        if (DimensionManager.getProviderType(dim) == Configs.providerID)
                            DimensionManager.unregisterDimension(dim);
                    } catch (Exception e) {}
                }
            }
        } catch (Exception e) {}
    }

    public static void clearRuntimeState() {
        unregisterTardisDimensions();
        unregisterRuntimeStores();
    }

    public static void clearRuntimeCachesOnly() {
        unregisterRuntimeStores();
    }
}
