package tardis.common.network.packet;

import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import io.darkcraft.darkcore.mod.helpers.ServerHelper;
import io.darkcraft.darkcore.mod.helpers.WorldHelper;
import io.darkcraft.darkcore.mod.interfaces.IDataPacketHandler;
import tardis.common.entities.particles.NanogeneParticleEntity;
import tardis.common.entities.particles.ParticleType;
import tardis.common.entities.particles.RestorationFieldParticleEntity;

public class ParticlePacketHandler implements IDataPacketHandler {

    private static Random rand = new Random();

    private double pO(boolean r) {
        if (r) return (rand.nextGaussian() / 3);
        return 0;
    }

    public void handleData(NBTTagCompound nbt) {
        if (nbt != null && ServerHelper.isClient()) {
            int dim = io.darkcraft.darkcore.mod.nbt.NBTUtils.getInt(nbt, "dim", 0);
            double x = io.darkcraft.darkcore.mod.nbt.NBTUtils.getDouble(nbt, "x", 0);
            double y = io.darkcraft.darkcore.mod.nbt.NBTUtils.getDouble(nbt, "y", 0);
            double z = io.darkcraft.darkcore.mod.nbt.NBTUtils.getDouble(nbt, "z", 0);
            ParticleType type = ParticleType.get(io.darkcraft.darkcore.mod.nbt.NBTUtils.getInt(nbt, "type", 0));
            int c = io.darkcraft.darkcore.mod.nbt.NBTUtils.getInt(nbt, "c", 1);
            c = Math.max(0, Math.min(c, 128));
            if (c == 0) return;
            boolean r = io.darkcraft.darkcore.mod.nbt.NBTUtils.getBoolean(nbt, "r", false);
            EntityFX[] fx = new EntityFX[c];
            World w = WorldHelper.getWorld(dim);
            if (w == null) return;
            for (int i = 0; i < c; i++) {
                if (type == ParticleType.NANOGENE)
                    fx[i] = new NanogeneParticleEntity(w, x + pO(r), y + pO(r), z + pO(r));

                if (type == ParticleType.RESTORATIONFIELD)
                    fx[i] = new RestorationFieldParticleEntity(w, x + pO(r), y + pO(r), z + pO(r));
            }

            for (EntityFX f : fx) if (f != null) Minecraft.getMinecraft().effectRenderer.addEffect(f);
        }
    }
}
