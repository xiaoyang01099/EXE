package net.xiaoyang010.ex_enigmaticlegacy.Event;

import net.minecraft.Util;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.HolyRing;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;
import net.xiaoyang010.ex_enigmaticlegacy.Util.EComponent;
import net.xiaoyang010.ex_enigmaticlegacy.api.IModelRegister;
import vazkii.botania.api.mana.IManaItem;
import vazkii.botania.xplat.IXplatAbstractions;

import java.util.Random;

@Mod.EventBusSubscriber(modid = "ex_enigmaticlegacy", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class TooltipEvent {

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
    public static void onTooltip(RenderTooltipEvent.Color event) {
        float hue = (float) Util.getMillis() / 5000.0F % 1.0F;
        float c =0xff000000| Mth.hsvToRgb(hue, 2.0F, 1.5F);
        int cd= (int)0xff000000| Mth.hsvToRgb(hue, 2.0F, 0.1F);
        if (event.getItemStack().getItem() instanceof HolyRing) {
            event.setBorderEnd((int)c);
            event.setBorderStart((int)c/*Mth.hsvToRgb(hue, 2.0F, 1.5F)*/);
            event.setBackground(cd);
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

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onItemTooltip(ItemTooltipEvent event) {
        ItemStack itemStack = event.getItemStack();

        Item item = itemStack.getItem();

        IManaItem manaItem = null;

        if (item instanceof IManaItem directManaItem) {
            manaItem = directManaItem;
        } else {
            var foundManaItem = IXplatAbstractions.INSTANCE.findManaItem(itemStack);
            if (foundManaItem != null) {
                manaItem = foundManaItem;
            }
        }

        if (manaItem != null) {
            int currentMana = manaItem.getMana();
            int maxMana = manaItem.getMaxMana();
            TranslatableComponent manaComponent = EComponent.translatable(
                    "tooltip.ex_enigmaticlegacy.mana_info",
                    String.format("%,d", currentMana),
                    String.format("%,d", maxMana)
            );

            event.getToolTip().add(manaComponent);

            if (maxMana > 0) {
                double percentage = (double) currentMana / maxMana * 100;

                TranslatableComponent percentageComponent = EComponent.translatable(
                        "tooltip.ex_enigmaticlegacy.mana_percentage",
                        String.format("%.1f", percentage)
                );

                event.getToolTip().add(percentageComponent);
            }
        }
    }
}