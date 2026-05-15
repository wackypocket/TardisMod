package tardis.common.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;

import io.darkcraft.darkcore.mod.abstracts.AbstractWorldDataStore;
import io.darkcraft.darkcore.mod.helpers.PlayerHelper;
import io.darkcraft.darkcore.mod.helpers.ServerHelper;
import io.darkcraft.darkcore.mod.nbt.NBTUtils;
import tardis.TardisMod;
import tardis.common.core.helpers.Helper;
import tardis.common.dimension.TardisDataStore;
import tardis.common.tileents.CoreTileEntity;

public class TardisOwnershipRegistry extends AbstractWorldDataStore {

    // Primary mapping: dimension -> owner UUID
    public HashMap<Integer, UUID> ownedDimMapping = new HashMap<Integer, UUID>();
    // Fallback mapping for legacy entries that couldn't be resolved to UUIDs yet
    public HashMap<Integer, String> ownedDimNameFallback = new HashMap<Integer, String>();

    public TardisOwnershipRegistry() {
        super("TModPReg");
    }

    public TardisOwnershipRegistry(String s) {
        super("TModPReg");
    }

    public static void loadAll() {
        if (TardisMod.plReg == null) TardisMod.plReg = new TardisOwnershipRegistry();
        TardisMod.plReg.load();
    }

    public static void saveAll() {
        if (ServerHelper.isServer()) TardisMod.plReg.save();
    }

    public boolean addPlayer(String username, int dimension) {
        if ((username == null) || (dimension == 0)) return false;
        TardisOutput.print("TPlReg", "Mapping dim " + dimension + " to " + username);
        if (hasTardis(username)) return false;
        try {
            UUID uuid = PlayerHelper.getUUID(username);
            if (uuid != null) {
                ownedDimMapping.put(dimension, uuid);
            } else {
                ownedDimNameFallback.put(dimension, username);
            }
            markDirty();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean removePlayer(String username) {
        if (username == null) return false;
        UUID uuid = PlayerHelper.getUUID(username);
        if (uuid != null) return ownedDimMapping.values()
            .remove(uuid);
        // fallback: remove by name
        return ownedDimNameFallback.values()
            .remove(username);
    }

    public Integer getDimension(String username) {
        if (username == null) return null;
        try {
            UUID uuid = PlayerHelper.getUUID(username);
            if (uuid != null) {
                for (Integer i : ownedDimMapping.keySet()) {
                    UUID u = ownedDimMapping.get(i);
                    if ((u != null) && u.equals(uuid)) return i;
                }
            }
            // fallback: check legacy name map
            for (Integer i : ownedDimNameFallback.keySet()) {
                String name = ownedDimNameFallback.get(i);
                if ((name != null) && name.equals(username)) {
                    // try to migrate this entry to UUID if possible
                    UUID newU = PlayerHelper.getUUID(username);
                    if (newU != null) {
                        ownedDimMapping.put(i, newU);
                        ownedDimNameFallback.remove(i);
                        markDirty();
                        return i;
                    }
                    return i;
                }
            }
        } catch (Exception e) {}
        return null;
    }

    public Integer getDimension(EntityPlayer player) {
        if (player == null) return null;
        UUID uuid = PlayerHelper.getUUID(player);
        if (uuid != null) {
            for (Integer i : ownedDimMapping.keySet()) {
                UUID u = ownedDimMapping.get(i);
                if ((u != null) && u.equals(uuid)) return i;
            }
        }
        // fallback to name-based lookup and attempt to migrate
        return getDimension(ServerHelper.getUsername(player));
    }

    public CoreTileEntity getCore(EntityPlayer player) {
        Integer dimID = getDimension(player);
        if (dimID != null) {
            return Helper.getTardisCore(dimID);
        }
        return null;
    }

    public TardisDataStore getDataStore(EntityPlayer player) {
        Integer dimID = getDimension(player);
        if (dimID != null) {
            return Helper.getDataStore(dimID);
        }
        return null;
    }

    public EntityPlayerMP getPlayer(int dimension) {
        if (ownedDimMapping.containsKey(dimension)) {
            UUID u = ownedDimMapping.get(dimension);
            if (u != null) {
                String name = PlayerHelper.getUsername(u);
                if (name != null) return ServerHelper.getPlayer(name);
            }
        }
        if (ownedDimNameFallback.containsKey(dimension))
            return ServerHelper.getPlayer(ownedDimNameFallback.get(dimension));
        return null;
    }

    public String getPlayerName(int dimension) {
        if (ownedDimMapping.containsKey(dimension)) {
            UUID u = ownedDimMapping.get(dimension);
            if (u != null) return PlayerHelper.getUsername(u);
        }
        if (ownedDimNameFallback.containsKey(dimension)) return ownedDimNameFallback.get(dimension);
        return null;
    }

    public boolean hasTardis(String username) {
        if (username == null) return false;
        UUID uuid = PlayerHelper.getUUID(username);
        if (uuid != null) {
            Collection<UUID> values = ownedDimMapping.values();
            if (values != null) return values.contains(uuid);
        }
        Collection<String> nameValues = ownedDimNameFallback.values();
        if (nameValues != null) return nameValues.contains(username);
        return false;
    }

    public void chatMapping(ICommandSender comsen) {
        comsen.addChatMessage(new ChatComponentText("Dimension mapping:"));
        for (Integer i : ownedDimMapping.keySet()) {
            UUID owner = ownedDimMapping.get(i);
            String name = PlayerHelper.getUsername(owner);
            if (name == null) name = (owner == null) ? "null" : owner.toString();
            if (i != null) comsen.addChatMessage(new ChatComponentText(name + "->" + i));
        }
        for (Integer i : ownedDimNameFallback.keySet()) {
            String owner = ownedDimNameFallback.get(i);
            if (i != null) comsen.addChatMessage(new ChatComponentText(owner + "->" + i));
        }
    }

    public List<String> getPlayersWithHash() {
        List<String> strings = new ArrayList<String>(ownedDimMapping.size() + ownedDimNameFallback.size());
        for (UUID u : ownedDimMapping.values()) {
            String name = PlayerHelper.getUsername(u);
            if (name != null) strings.add("#" + name);
            else strings.add("#" + u.toString());
        }
        for (String s : ownedDimNameFallback.values()) strings.add("#" + s);
        return strings;
    }

    @Override
    public int getDimension() {
        return 0;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        int i = 0;
        TardisOutput.print("TPlReg", "Reading from NBT");
        ownedDimMapping.clear();
        ownedDimNameFallback.clear();
        while (io.darkcraft.darkcore.mod.nbt.NBTUtils.hasCompound(nbt, "store" + i)) {
            NBTTagCompound tag = NBTUtils.getCompoundOrEmpty(nbt, "store" + i);
            String un = io.darkcraft.darkcore.mod.nbt.NBTUtils.getString(tag, "username", null);
            String us = io.darkcraft.darkcore.mod.nbt.NBTUtils.getString(tag, "uuid", null);
            Integer dim = io.darkcraft.darkcore.mod.nbt.NBTUtils.getInt(tag, "dimension", 0);
            TardisOutput.print("TPlReg", "NBT Load: Mapping " + un + "|" + us + "->" + dim);
            try {
                if (us != null && !us.isEmpty()) {
                    UUID uuid = UUID.fromString(us);
                    ownedDimMapping.put(dim, uuid);
                } else if (un != null && !un.isEmpty()) {
                    UUID uuid = PlayerHelper.getUUID(un);
                    if (uuid != null) ownedDimMapping.put(dim, uuid);
                    else ownedDimNameFallback.put(dim, un);
                }
            } catch (Exception e) {
                if (un != null) ownedDimNameFallback.put(dim, un);
            }
            i++;
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        TardisOutput.print("TPlReg", "Saving to NBT");
        int i = 0;
        Set<Integer> dims = ownedDimMapping.keySet();
        for (Integer j : dims) {
            NBTTagCompound tag = new NBTTagCompound();
            UUID u = ownedDimMapping.get(j);
            if (u != null) {
                String name = PlayerHelper.getUsername(u);
                if (name != null) tag.setString("username", name);
                tag.setString("uuid", u.toString());
                tag.setInteger("dimension", j);
                nbt.setTag("store" + i, tag);
                i++;
            }
        }
        // also write fallback name-based entries
        for (Integer j : ownedDimNameFallback.keySet()) {
            NBTTagCompound tag = new NBTTagCompound();
            String s = ownedDimNameFallback.get(j);
            if (s != null) {
                tag.setString("username", s);
                tag.setInteger("dimension", j);
                nbt.setTag("store" + i, tag);
                i++;
            }
        }
    }

}
