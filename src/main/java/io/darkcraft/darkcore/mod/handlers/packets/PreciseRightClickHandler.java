package io.darkcraft.darkcore.mod.handlers.packets;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import io.darkcraft.darkcore.mod.DarkcoreMod;
import io.darkcraft.darkcore.mod.helpers.PlayerHelper;
import io.darkcraft.darkcore.mod.helpers.ServerHelper;
import io.darkcraft.darkcore.mod.helpers.WorldHelper;
import io.darkcraft.darkcore.mod.interfaces.IActivatablePrecise;
import io.darkcraft.darkcore.mod.interfaces.IDataPacketHandler;
import io.darkcraft.darkcore.mod.network.DataPacket;

public class PreciseRightClickHandler implements IDataPacketHandler {

    public static final String disc = "core.preciseright";

    public static void handle(World w, int x, int y, int z, EntityPlayer pl, int s, float i, float j, float k) {
        NBTTagCompound nbt = new NBTTagCompound();
        if (ServerHelper.isClient()) {
            nbt.setInteger("w", WorldHelper.getWorldID(w));
            nbt.setInteger("x", x);
            nbt.setInteger("y", y);
            nbt.setInteger("z", z);
            nbt.setInteger("s", s);
            nbt.setFloat("i", i);
            nbt.setFloat("j", j);
            nbt.setFloat("k", k);
            nbt.setString("pl", PlayerHelper.getUsername(pl));
            DataPacket dp = new DataPacket(nbt, disc);
            DarkcoreMod.networkChannel.sendToServer(dp);
        }
    }

    @Override
    public void handleData(NBTTagCompound data) {
        if (ServerHelper.isServer()) {
            if (data == null) return;
            World w = WorldHelper.getWorld(io.darkcraft.darkcore.mod.nbt.NBTUtils.getInt(data, "w", 0));
            EntityPlayer pl = PlayerHelper
                .getPlayer(io.darkcraft.darkcore.mod.nbt.NBTUtils.getString(data, "pl", null));
            if ((w == null) || (pl == null)) return;
            int x = io.darkcraft.darkcore.mod.nbt.NBTUtils.getInt(data, "x", 0);
            int y = io.darkcraft.darkcore.mod.nbt.NBTUtils.getInt(data, "y", 0);
            int z = io.darkcraft.darkcore.mod.nbt.NBTUtils.getInt(data, "z", 0);
            int s = io.darkcraft.darkcore.mod.nbt.NBTUtils.getInt(data, "s", 0);
            float i = io.darkcraft.darkcore.mod.nbt.NBTUtils.getFloat(data, "i", 0f);
            float j = io.darkcraft.darkcore.mod.nbt.NBTUtils.getFloat(data, "j", 0f);
            float k = io.darkcraft.darkcore.mod.nbt.NBTUtils.getFloat(data, "k", 0f);
            Block b = w.getBlock(x, y, z);
            TileEntity te = w.getTileEntity(x, y, z);
            if (!((b instanceof IActivatablePrecise) || (te instanceof IActivatablePrecise))) return;
            if (b instanceof IActivatablePrecise) ((IActivatablePrecise) b)
                .activate(pl, s, x + Math.max(i, 0.9999f), y + Math.max(j, 0.9999f), z + Math.max(k, 0.9999f));
            if (te instanceof IActivatablePrecise) ((IActivatablePrecise) te).activate(pl, s, i, j, k);
        }
    }

}
