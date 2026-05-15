package tardis.common.items;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.darkcraft.darkcore.mod.abstracts.AbstractItem;
import tardis.TardisMod;

public class SecretUpgradeItem extends AbstractItem {

    @SideOnly(Side.CLIENT)
    private IIcon icon;

    public SecretUpgradeItem() {
        super(TardisMod.modName);
        setUnlocalizedName("secretUpgrade");
        setTextureName(TardisMod.modName + ":" + "CraftingComponent.Upgrade");
        setCreativeTab(null);
    }

    @Override
    public void initRecipes() {
        // No recipe
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister ir) {
        icon = ir.registerIcon(TardisMod.modName + ":" + "CraftingComponent.Upgrade");
        hideFromNEI();
    }

    @SideOnly(Side.CLIENT)
    private void hideFromNEI() {
        if (!Loader.isModLoaded("NotEnoughItems")) return;
        try {
            Class<?> apiClass = Class.forName("codechicken.nei.api.API");
            java.lang.reflect.Method hideItem = apiClass.getMethod("hideItem", ItemStack.class);
            hideItem.invoke(null, new ItemStack(this, 1, 0));
        } catch (Exception ignored) {}
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIconFromDamage(int dmg) {
        return icon;
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected String getIconString() {
        return TardisMod.modName + ":" + "CraftingComponent.Upgrade";
    }

    @Override
    public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> list) {
        // Intentionally empty so this item does not enumerate into item panels.
    }

}
