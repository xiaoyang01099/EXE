package net.xiaoyang010.ex_enigmaticlegacy.Event;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;

@Mod.EventBusSubscriber(modid = "ex_enigmaticlegacy", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEventSubscriber {

    @SubscribeEvent
    public static void onTooltipEvent(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() == ModItems.PRISMATICRADIANCEINGOT.get()) {
            event.getToolTip().add(new TranslatableComponent("item.ex_enigmaticlegacy.prismaticradianceingot.desc").withStyle(ChatFormatting.GOLD));
        }
    }
}

