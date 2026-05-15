package tardis.common.blocks;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

import cpw.mods.fml.common.registry.GameRegistry;
import io.darkcraft.darkcore.mod.abstracts.AbstractBlock;
import tardis.Configs;
import tardis.TardisMod;
import tardis.common.tileents.extensions.CraftingComponentType;

public class CompressedBlock extends AbstractBlock {

    public CompressedBlock() {
        super(TardisMod.modName);
        setCreativeTab(TardisMod.cTab);
    }

    @Override
    public void initData() {
        setBlockName("Block");
        setSubNames("Dalekanium", "Chronosteel");
        setLightLevel(Configs.lightBlocks ? 1 : 0);
        setHardness(100.0F);
        setHarvestLevel("pickaxe", 3, 0); // Dalekanium (meta 0) requires diamond
        setHarvestLevel("pickaxe", 3, 1); // Chronosteel (meta 1) requires diamond
    }

    @Override
    public void initRecipes() {
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                new ItemStack(this, 1, 0),
                false,
                "ddd",
                "ddd",
                "ddd",
                'd',
                CraftingComponentType.DALEKANIUM.getIS(1)));
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                new ItemStack(this, 1, 1),
                false,
                "ccc",
                "ccc",
                "ccc",
                'c',
                CraftingComponentType.CHRONOSTEEL.getIS(1)));
    }

}
