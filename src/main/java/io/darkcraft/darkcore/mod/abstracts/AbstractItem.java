package io.darkcraft.darkcore.mod.abstracts;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.darkcraft.darkcore.mod.DarkcoreMod;
import io.darkcraft.darkcore.mod.handlers.RecipeHandler;
import io.darkcraft.darkcore.mod.interfaces.IRecipeContainer;

public abstract class AbstractItem extends Item implements IRecipeContainer {

    private IIcon iconBuffer;
    private String unlocalizedFragment;
    private final String modName;
    public boolean registerIcons = true;

    private String[] subNames = null;
    private IIcon[] subIcons = null;

    public AbstractItem(String mod) {
        modName = mod;
        CreativeTabs tab = DarkcoreMod.getCreativeTab(mod);
        if (tab != null) setCreativeTab(tab);
        RecipeHandler.addRecipeContainer(this);
    }

    public AbstractItem register() {
        GameRegistry.registerItem(this, getUnlocalizedName());
        DarkcoreMod.proxy.register(this);
        return this;
    }

    @Override
    public abstract void initRecipes();

    public void setSubNames(String... _subNames) {
        subNames = _subNames;
        if ((subNames != null) && (subNames.length > 1)) setHasSubtypes(true);
    }

    @Override
    public Item setUnlocalizedName(String unlocal) {
        Item orig = super.setUnlocalizedName(unlocal);
        unlocalizedFragment = unlocal;
        return orig;
    }

    @Override
    public String getUnlocalizedName() {
        return "item." + modName + "." + unlocalizedFragment;
    }

    @Override
    public String getUnlocalizedName(ItemStack is) {
        if (subNames == null) return getUnlocalizedName();
        else {
            int damage = is.getItemDamage();
            if ((damage >= 0) && (damage < subNames.length)) return getUnlocalizedName() + "." + subNames[damage];
            else return getUnlocalizedName() + ".Malformed";
        }
    }

    public String[] getSubNamesForIcons() {
        return subNames;
    }

    public String[] getSubNamesForNEI() {
        return subNames;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister ir) {
        if (!registerIcons) return;
        String[] subNames = getSubNamesForIcons();
        if (DarkcoreMod.debugText)
            io.darkcraft.darkcore.mod.logging.TMLLog.debug("TAI", "Registering icon " + unlocalizedFragment);
        if (subNames != null) {
            subIcons = new IIcon[subNames.length];
            for (int i = 0; i < subNames.length; i++)
                subIcons[i] = ir.registerIcon(modName + ":" + unlocalizedFragment + "." + subNames[i]);
        } else iconBuffer = ir.registerIcon(modName + ":" + unlocalizedFragment);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIconFromDamage(int damage) {
        String[] subNames = getSubNamesForIcons();
        if (subNames == null) return iconBuffer;
        else if ((damage >= 0) && (damage < subNames.length)) return subIcons[damage];
        return iconBuffer;
    }

    @Override
    public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List<ItemStack> list) {
        String[] subNames = getSubNamesForNEI();
        if (subNames == null) list.add(new ItemStack(par1, 1, 0));
        else {
            for (int i = 0; i < subNames.length; i++) list.add(new ItemStack(par1, 1, i));
        }
    }

    public void addInfo(ItemStack is, EntityPlayer player, List<String> infoList) {}

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack is, EntityPlayer player, List<String> infoList, boolean par4) {
        super.addInformation(is, player, infoList, par4);
        addInfo(is, player, infoList);
    }

    @SideOnly(Side.CLIENT)
    public IItemRenderer getRenderer() {
        return null;
    }

}
