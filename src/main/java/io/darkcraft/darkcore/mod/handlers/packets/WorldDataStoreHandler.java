package io.darkcraft.darkcore.mod.handlers.packets;

import java.util.HashMap;

import net.minecraft.nbt.NBTTagCompound;

import io.darkcraft.darkcore.mod.abstracts.AbstractWorldDataStore;
import io.darkcraft.darkcore.mod.datastore.Pair;
import io.darkcraft.darkcore.mod.helpers.ServerHelper;
import io.darkcraft.darkcore.mod.interfaces.IDataPacketHandler;

public class WorldDataStoreHandler implements IDataPacketHandler {

    public static final String disc = "core.awds";

    @SuppressWarnings("unchecked")
    private static HashMap<Pair<Integer, String>, AbstractWorldDataStore> map = new HashMap();

    public static void clear() {
        map.clear();
    }

    public static boolean register(AbstractWorldDataStore store) {
        if (store == null) return false;
        Pair<Integer, String> key = new Pair<Integer, String>(store.getDimension(), store.getName());
        if (map.containsKey(key)) return false;
        map.put(key, store);
        return true;
    }

    public static AbstractWorldDataStore get(Integer dim, String name) {
        Pair<Integer, String> key = new Pair<Integer, String>(dim, name);
        return map.get(key);
    }

    @Override
    public void handleData(NBTTagCompound data) {
        if ((data != null) && io.darkcraft.darkcore.mod.nbt.NBTUtils.hasCompound(data, "AWDSdim")
            && io.darkcraft.darkcore.mod.nbt.NBTUtils.hasCompound(data, "AWDSname")) {
            int dim = io.darkcraft.darkcore.mod.nbt.NBTUtils.getInt(data, "AWDSdim", 0);
            String name = io.darkcraft.darkcore.mod.nbt.NBTUtils.getString(data, "AWDSname", null);
            // System.out.print("RP:"+dim+":"+name);
            AbstractWorldDataStore awds = get(dim, name);
            if (awds != null) {
                // System.out.println("#NN");
                if (ServerHelper.isClient()) {

                    if (!ServerHelper.isIntegratedClient()) awds.readFromNBT(data);
                } else awds.sendUpdate();
            } else io.darkcraft.darkcore.mod.logging.TMLLog.debug("WDH", "#N");
        }
    }
}
