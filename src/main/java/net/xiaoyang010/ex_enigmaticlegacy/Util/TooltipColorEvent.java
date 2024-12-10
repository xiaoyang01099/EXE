package net.xiaoyang010.ex_enigmaticlegacy.Util;


import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;

import java.util.Random;

@Mod.EventBusSubscriber(modid = "ex_enigmaticlegacy", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class TooltipColorEvent {

    @SubscribeEvent
    public static void onRenderTooltipColor(RenderTooltipEvent.Color event) {
        if (event.getItemStack().getItem().equals(ModItems.PRISMATICRADIANCEBLOCK.get())) {
            Random random = new Random();
            int color = random.nextInt() | 0xFF000000;

            event.setBorderStart(color);
            event.setBorderEnd(color);
            event.setBackgroundStart(color);
            event.setBackgroundEnd(color);
        }

        if (event.getItemStack().getItem().equals(ModItems.PRISMATICRADIANCEINGOT.get())) {
            Random random = new Random();
            int color = random.nextInt() | 0xFF000000;

            event.setBorderStart(color);
            event.setBorderEnd(color);
            event.setBackgroundStart(color);
            event.setBackgroundEnd(color);
        }
    }
}