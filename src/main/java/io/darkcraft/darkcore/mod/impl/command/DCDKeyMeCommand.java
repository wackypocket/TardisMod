package io.darkcraft.darkcore.mod.impl.command;

import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.item.ItemStack;

import io.darkcraft.darkcore.mod.abstracts.AbstractCommandNew;
import io.darkcraft.darkcore.mod.helpers.ServerHelper;
import io.darkcraft.darkcore.mod.helpers.WorldHelper;
import io.darkcraft.darkcore.mod.impl.UniqueSwordItem;
import tardis.common.TMRegistry;

public class DCDKeyMeCommand extends AbstractCommandNew {

    @Override
    public String getCommandName() {
        return "keyme";
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
            ItemStack key = new ItemStack(TMRegistry.keyItem, 1);
            key.stackTagCompound = null;
            WorldHelper.giveItemStack(pl, key);
            sendString(sen, "Enjoy!");
        }
        return true;
    }

}
