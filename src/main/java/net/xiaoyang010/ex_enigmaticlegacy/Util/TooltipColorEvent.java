package net.xiaoyang010.ex_enigmaticlegacy.Util;


import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;
import vazkii.botania.api.mana.IManaItem;

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

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent e) {
        if (e.getItemStack().getItem() instanceof IManaItem manaItem) {
            e.getToolTip().add(new TranslatableComponent("Mana: %d/%d",
                    manaItem.getMana(),
                    manaItem.getMaxMana()));
        }
    }

    @SubscribeEvent
    public static void registerModels(RegisterClientReloadListenersEvent evt) {
        ModelLoaderRegistry.registerLoader(new ResourceLocation("ex_enigmaticlegacy", "obj"),
                new OBJLoader());

        ForgeRegistries.ITEMS.forEach(item -> {
            if (item instanceof IModelRegister modelReg) {
                modelReg.registerModels();
            }
        });
    }
}