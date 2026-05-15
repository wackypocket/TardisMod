package tardis.common.tileents.extensions.upgrades;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import tardis.api.TardisUpgradeMode;
import tardis.common.TMRegistry;
import tardis.common.dimension.TardisDataStore;
import tardis.common.dimension.damage.TardisDamageType;

public class SecretUpgrade extends AbstractUpgrade {

    private static final ResourceLocation tex = new ResourceLocation(
        "tardismod",
        "textures/models/upgrades/secret.png");

    @Override
    public boolean isValid(AbstractUpgrade[] currentUpgrades) {
        return true;
    }

    @Override
    public ItemStack getIS() {
        return new ItemStack(TMRegistry.secretUpgradeItem, 1);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setString("id", "secret");
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {}

    @Override
    public String[] getExtraInfo() {
        return new String[] { "Secret upgrade: grants massive bonuses to all upgrade modes" };
    }

    @Override
    public int getUpgradeEffect(TardisUpgradeMode mode, TardisDataStore ds) {
        return 10000;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ResourceLocation getTexture() {
        return tex;
    }

    @Override
    public int takeDamage(TardisDamageType dam, int amount) {
        return 0;
    }

}
