package tardis.common.items;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

import cpw.mods.fml.common.registry.GameRegistry;
import io.darkcraft.darkcore.mod.abstracts.AbstractItem;
import tardis.TardisMod;
import tardis.common.tileents.extensions.CraftingComponentType;

public class ManualItem extends AbstractItem {

    public ManualItem() {
        super(TardisMod.modName);
        setUnlocalizedName("ManualItem");
        setCreativeTab(TardisMod.cTab);
        registerIcons = false;
    }

    @Override
    public void initRecipes() {
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                new ItemStack(this, 1),
                false,
                " i ",
                "igi",
                " k ",
                'i',
                "ingotIron",
                'g',
                "blockGlass",
                'k',
                CraftingComponentType.CHRONOSTEEL.getIS(1)));
    }

}
