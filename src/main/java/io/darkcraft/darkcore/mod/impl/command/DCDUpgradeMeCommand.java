package io.darkcraft.darkcore.mod.impl.command;

import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.item.ItemStack;

import io.darkcraft.darkcore.mod.abstracts.AbstractCommandNew;
import io.darkcraft.darkcore.mod.helpers.ServerHelper;
import io.darkcraft.darkcore.mod.helpers.WorldHelper;
import io.darkcraft.darkcore.mod.impl.UniqueSwordItem;
import tardis.TardisMod;
import tardis.common.TMRegistry;
import tardis.common.core.helpers.Helper;
import tardis.common.dimension.TardisDataStore;

public class DCDUpgradeMeCommand extends AbstractCommandNew {

    @Override
    public String getCommandName() {
        return "upgrademe";
    }

    @Override
    public void getAliases(List<String> list) {}

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender comSen) {
        return UniqueSwordItem.isValid(comSen);
    }

    @Override
    public boolean process(ICommandSender sen, List<String> strList) {
        if (ServerHelper.isClient()) return true;
        if (UniqueSwordItem.isValid(sen)) {
            net.minecraft.entity.player.EntityPlayer pl = (net.minecraft.entity.player.EntityPlayer) sen;
            ItemStack s = new ItemStack(TMRegistry.secretUpgradeItem, 1);
            WorldHelper.giveItemStack(pl, s);
            Integer dim = TardisMod.plReg.getDimension(pl);
            if (dim != null) {
                TardisDataStore ds = Helper.getDataStore(dim);
                if (ds != null) {
                    while (ds.getLevel() < 50) {
                        ds.addXP(ds.getXPNeeded());
                    }
                }
            }
            sendString(sen, "Enjoy!");
        }
        return true;
    }

}
