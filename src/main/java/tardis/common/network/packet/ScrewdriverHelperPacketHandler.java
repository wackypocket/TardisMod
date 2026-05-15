package tardis.common.network.packet;

import net.minecraft.nbt.NBTTagCompound;

import io.darkcraft.darkcore.mod.helpers.ServerHelper;
import io.darkcraft.darkcore.mod.interfaces.IDataPacketHandler;
import tardis.common.core.helpers.ScrewdriverHelperFactory;

public class ScrewdriverHelperPacketHandler implements IDataPacketHandler {

    @Override
    public void handleData(NBTTagCompound data) {
        if (!ServerHelper.isClient()) return;
        if (data == null) return;
        if (!io.darkcraft.darkcore.mod.nbt.NBTUtils.hasCompound(data, "uuid")) return;
        ScrewdriverHelperFactory.get(data);
    }

}
