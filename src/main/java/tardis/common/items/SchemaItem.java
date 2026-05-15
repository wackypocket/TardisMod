package tardis.common.items;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import io.darkcraft.darkcore.mod.abstracts.AbstractItem;
import tardis.TardisMod;

public class SchemaItem extends AbstractItem {

    public SchemaItem() {
        super(TardisMod.modName);
        setMaxStackSize(1);
        setUnlocalizedName("Schematic");
    }

    @Override
    public void addInformation(ItemStack is, EntityPlayer player, List infoList, boolean par4) {
        if (is != null) {
            NBTTagCompound isTag = is.stackTagCompound;
            String name = io.darkcraft.darkcore.mod.nbt.NBTUtils.getString(isTag, "schemaName", null);
            if (name != null) infoList.add("Schematic file:" + name);
            else infoList.add("Schematic file: --NONE--");
        }
    }

    @Override
    public void initRecipes() {
        // TODO Auto-generated method stub

    }

}
