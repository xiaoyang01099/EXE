package org.xiaoyang.ex_enigmaticlegacy.Font.Jade;

import net.minecraft.world.entity.item.ItemEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

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
        registration.registerEntityComponent(
                ItemEntityWaveNameProvider.INSTANCE,
                ItemEntity.class
        );

        registration.addTooltipCollectedCallback(
                ExEWailaTooltipEventHandler.INSTANCE
        );
    }
}