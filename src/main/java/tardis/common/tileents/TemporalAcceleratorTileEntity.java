package tardis.common.tileents;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import io.darkcraft.darkcore.mod.abstracts.AbstractTileEntity;
import io.darkcraft.darkcore.mod.helpers.MathHelper;
import tardis.Configs;
import tardis.common.TMRegistry;
import tardis.common.blocks.TemporalAcceleratorBlock;
import tardis.common.core.helpers.Helper;

public class TemporalAcceleratorTileEntity extends AbstractTileEntity {

    @Override
    public void tick() {
        World w = coords.getWorldObj();
        int x = coords.x, y = coords.y, z = coords.z;

        if (!Helper.isTardisWorld(w)) return;
        if (w.isAirBlock(x, y + 1, z)) {
            return;
        }

        Block b = w.getBlock(x, y + 1, z);
        if (b == null) return;

        if ((tt % getNewTickRate(b.tickRate(w))) == 0) {
            CoreTileEntity core = Helper.getTardisCore(w);
            if ((core != null)) {
                if (w instanceof WorldServer) {

                    if (b instanceof TemporalAcceleratorBlock) return;
                    if (TMRegistry.unbreakableBlocks.contains(b)) return;
                    b.updateTick(w, x, y + 1, z, rand);

                    if (!b.hasTileEntity(b.getDamageValue(w, x, y + 1, z))) return;
                    TileEntity te = w.getTileEntity(x, y + 1, z);
                    if (te == null) return;
                    if (te == this) return;
                    if (te.isInvalid()) return;
                    te.updateEntity();

                }
            }
        }
    }

    public int getNewTickRate(int old) {
        int newRate = MathHelper.ceil((old * Configs.tempAccTickMult));
        return Math.max(1, newRate);
    }

}
