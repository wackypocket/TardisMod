package tardis.common.integration.waila;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import mcp.mobius.waila.api.IWailaDataAccessor;

public class WailaArtronProvider extends AbstractWailaProvider {

    @Override
    public String[] extraInfo(IWailaDataAccessor accessor, int control) {
        TileEntity te = accessor.getTileEntity();
        if (te instanceof tardis.common.tileents.BatteryTileEntity) {
            tardis.common.tileents.BatteryTileEntity battery = (tardis.common.tileents.BatteryTileEntity) te;
            String[] data = new String[3];
            data[0] = "Energy: " + battery.getArtronEnergy();
            data[1] = "Max Energy: " + battery.getMaxArtronEnergy();

            NBTTagCompound nbt = accessor.getNBTData();
            int m = io.darkcraft.darkcore.mod.nbt.NBTUtils.getInt(nbt, "m", 0);
            switch (m) {
                case 1:
                    data[2] = "Mode: Uncoordinated Flight";
                    break;
                case 2:
                    data[2] = "Mode: Coordinated Flight";
                    break;
                default:
                    data[2] = "Mode: Landed";
                    break;
            }
            return data;
        }

        // Fallback to NBT if tile entity is not available
        NBTTagCompound nbt = accessor.getNBTData();
        if (nbt == null) return null;
        String[] data = new String[3];
        if (io.darkcraft.darkcore.mod.nbt.NBTUtils.hasCompound(nbt, "ae")) {
            data[0] = "Energy: " + io.darkcraft.darkcore.mod.nbt.NBTUtils.getInt(nbt, "ae", 0);
        } else {
            data[0] = "Unknown energy";
        }
        if (io.darkcraft.darkcore.mod.nbt.NBTUtils.hasCompound(nbt, "maxAE")) {
            data[1] = "Max Energy: " + io.darkcraft.darkcore.mod.nbt.NBTUtils.getInt(nbt, "maxAE", 0);
        } else {
            data[1] = "Max Energy: Unknown";
        }
        int m = io.darkcraft.darkcore.mod.nbt.NBTUtils.getInt(nbt, "m", 0);
        switch (m) {
            case 1:
                data[2] = "Mode: Uncoordinated Flight";
                break;
            case 2:
                data[2] = "Mode: Coordinated Flight";
                break;
            default:
                data[2] = "Mode: Landed";
                break;
        }
        return data;
    }

    @Override
    public int getControlHit(IWailaDataAccessor accessor) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, int x,
        int y, int z) {
        return null;
    }
}
