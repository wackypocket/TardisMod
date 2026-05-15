package io.darkcraft.darkcore.mod.proxy;

import net.minecraft.world.World;

import io.darkcraft.darkcore.mod.abstracts.AbstractBlockContainer;
import io.darkcraft.darkcore.mod.abstracts.AbstractItem;
import io.darkcraft.darkcore.mod.helpers.WorldHelper;

public class CommonProxy {

    public void init() {

    }

    public World getWorld(int id) {
        return WorldHelper.getWorldServer(id);
    }

    public void postInit() {}

    public void register(AbstractBlockContainer b) {}

    public void register(AbstractItem i) {}
}
