package net.xiaoyang010.ex_enigmaticlegacy.Compat.Jade;

import mcp.mobius.waila.api.*;
import net.minecraft.world.entity.item.ItemEntity;

@WailaPlugin
public class ExEWailaPlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerEntityDataProvider(
                ItemEntityDataProvider.INSTANCE,
                ItemEntity.class
        );
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerComponentProvider(
                ItemEntityWaveNameProvider.INSTANCE,
                TooltipPosition.HEAD,
                ItemEntity.class
        );
        ExEWailaTooltipEventHandler.register();
    }
}