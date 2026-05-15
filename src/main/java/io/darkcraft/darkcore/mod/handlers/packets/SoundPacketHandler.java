package io.darkcraft.darkcore.mod.handlers.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import io.darkcraft.darkcore.mod.DarkcoreMod;
import io.darkcraft.darkcore.mod.helpers.ServerHelper;
import io.darkcraft.darkcore.mod.helpers.WorldHelper;
import io.darkcraft.darkcore.mod.interfaces.IDataPacketHandler;

public class SoundPacketHandler implements IDataPacketHandler {

    public static final String disc = "core.sound";

    @Override
    public void handleData(NBTTagCompound data) {
        if (ServerHelper.isServer()) return;
        if ((data != null) && io.darkcraft.darkcore.mod.nbt.NBTUtils.getBoolean(data, "stop", false)) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc == null || mc.thePlayer == null || mc.getSoundHandler() == null) return;
            int dim = io.darkcraft.darkcore.mod.nbt.NBTUtils.getInt(data, "world", mc.thePlayer.dimension);
            if (mc.thePlayer.dimension != dim) return;
            double radius = io.darkcraft.darkcore.mod.nbt.NBTUtils.getDouble(data, "radius", 48);
            if (io.darkcraft.darkcore.mod.nbt.NBTUtils.hasCompound(data, "x")) {
                int x = io.darkcraft.darkcore.mod.nbt.NBTUtils.getInt(data, "x", 0);
                int y = io.darkcraft.darkcore.mod.nbt.NBTUtils.getInt(data, "y", 0);
                int z = io.darkcraft.darkcore.mod.nbt.NBTUtils.getInt(data, "z", 0);
                double dx = mc.thePlayer.posX - (x + 0.5);
                double dy = mc.thePlayer.posY - (y + 0.5);
                double dz = mc.thePlayer.posZ - (z + 0.5);
                if (((dx * dx) + (dy * dy) + (dz * dz)) > (radius * radius)) return;
            }
            mc.getSoundHandler()
                .stopSounds();
            return;
        }
        if ((data != null) && io.darkcraft.darkcore.mod.nbt.NBTUtils.hasCompound(data, "sound")) {
            String sound = io.darkcraft.darkcore.mod.nbt.NBTUtils.getString(data, "sound", "");
            if (DarkcoreMod.bannedSounds.contains(sound)) return;
            int dim = io.darkcraft.darkcore.mod.nbt.NBTUtils.getInt(data, "world", 0);

            float vol = io.darkcraft.darkcore.mod.nbt.NBTUtils.getFloat(data, "vol", 1f);
            float speed = 1f;
            if (io.darkcraft.darkcore.mod.nbt.NBTUtils.hasCompound(data, "spe"))
                speed = io.darkcraft.darkcore.mod.nbt.NBTUtils.getFloat(data, "spe", 1f);
            World w = WorldHelper.getWorld(dim);
            if (w != null) {
                if (io.darkcraft.darkcore.mod.nbt.NBTUtils.hasCompound(data, "x")) {
                    int x = io.darkcraft.darkcore.mod.nbt.NBTUtils.getInt(data, "x", 0);
                    int y = io.darkcraft.darkcore.mod.nbt.NBTUtils.getInt(data, "y", 0);
                    int z = io.darkcraft.darkcore.mod.nbt.NBTUtils.getInt(data, "z", 0);
                    w.playSound(x, y, z, sound, vol, speed, true);
                } else {
                    Minecraft.getMinecraft().thePlayer.playSound(sound, vol, speed);
                }
            }
        }
    }
}
