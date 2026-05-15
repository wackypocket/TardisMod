package io.darkcraft.darkcore.mod.nbt;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

public final class NBTUtils {

    private NBTUtils() {}

    public static NBTTagCompound getCompoundIfPresent(NBTTagCompound parent, String key) {
        if ((parent == null) || (key == null)) return null;
        return parent.hasKey(key) ? parent.getCompoundTag(key) : null;
    }

    public static String getString(NBTTagCompound parent, String key, String def) {
        if ((parent == null) || (key == null)) return def;
        return parent.hasKey(key) ? parent.getString(key) : def;
    }

    public static int getInt(NBTTagCompound parent, String key, int def) {
        if ((parent == null) || (key == null)) return def;
        return parent.hasKey(key) ? parent.getInteger(key) : def;
    }

    public static double getDouble(NBTTagCompound parent, String key, double def) {
        if ((parent == null) || (key == null)) return def;
        return parent.hasKey(key) ? parent.getDouble(key) : def;
    }

    public static float getFloat(NBTTagCompound parent, String key, float def) {
        if ((parent == null) || (key == null)) return def;
        return parent.hasKey(key) ? parent.getFloat(key) : def;
    }

    public static boolean getBoolean(NBTTagCompound parent, String key, boolean def) {
        if ((parent == null) || (key == null)) return def;
        return parent.hasKey(key) ? parent.getBoolean(key) : def;
    }

    public static byte getByte(NBTTagCompound parent, String key, byte def) {
        if ((parent == null) || (key == null)) return def;
        return parent.hasKey(key) ? parent.getByte(key) : def;
    }

    public static long getLong(NBTTagCompound parent, String key, long def) {
        if ((parent == null) || (key == null)) return def;
        return parent.hasKey(key) ? parent.getLong(key) : def;
    }

    public static byte[] getByteArray(NBTTagCompound parent, String key) {
        if ((parent == null) || (key == null)) return new byte[0];
        return parent.hasKey(key) ? parent.getByteArray(key) : new byte[0];
    }

    public static int[] getIntArray(NBTTagCompound parent, String key) {
        if ((parent == null) || (key == null)) return new int[0];
        return parent.hasKey(key) ? parent.getIntArray(key) : new int[0];
    }

    public static NBTBase getTag(NBTTagCompound parent, String key) {
        if ((parent == null) || (key == null)) return null;
        return parent.hasKey(key) ? parent.getTag(key) : null;
    }

    public static boolean hasCompound(NBTTagCompound parent, String key) {
        if ((parent == null) || (key == null)) return false;
        return parent.hasKey(key);
    }

    public static NBTTagCompound getCompoundOrEmpty(NBTTagCompound parent, String key) {
        if ((parent == null) || (key == null)) return new NBTTagCompound();
        return parent.hasKey(key) ? parent.getCompoundTag(key) : new NBTTagCompound();
    }
}
