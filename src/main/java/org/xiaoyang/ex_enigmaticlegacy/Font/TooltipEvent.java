package org.xiaoyang.ex_enigmaticlegacy.Font;

import com.mojang.datafixers.util.Either;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModItems;
import org.xiaoyang.ex_enigmaticlegacy.api.exboapi.IWaveName;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.xplat.XplatAbstractions;

import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = "ex_enigmaticlegacy", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class TooltipEvent {

    @SubscribeEvent
    public static void onGatherTooltip(RenderTooltipEvent.GatherComponents event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        List<Either<FormattedText, TooltipComponent>> list = event.getTooltipElements();

        if (stack.getItem() instanceof IWaveName wni && !list.isEmpty()) {
            Either<FormattedText, TooltipComponent> first = list.get(0);
            first.ifLeft(text -> {
                String rawText = ChatFormatting.stripFormatting(text.getString());
                if (rawText != null && !rawText.isEmpty()) {
                    IWaveName.WaveStyle style = wni.getWaveStyle(stack);
                    list.set(0, Either.right(new WaveNameData(stack, style, rawText)));
                }
            });
        }

        for (int i = 1; i < list.size(); i++) {
            final int idx = i;
            list.get(i).ifLeft(text -> {
                IWaveName.WaveStyle waveStyle = StyleMarker.extractStyle(text);
                if (waveStyle == null) return;
                String rawText = ChatFormatting.stripFormatting(text.getString());
                if (rawText == null || rawText.isEmpty()) return;
                list.set(idx, Either.right(
                        new WaveNameData(stack, waveStyle, rawText)
                ));
            });
        }
    }

    @SubscribeEvent
    public static void onRenderTooltipColor(RenderTooltipEvent.Color event) {
//        if (event.getItemStack().getItem().equals(ModItems.PRISMATICRADIANCEBLOCK.get())) {
//            Random random = new Random();
//            int color = random.nextInt() | 0xFF000000;
//            event.setBorderStart(color);
//            event.setBorderEnd(color);
//            event.setBackgroundStart(color);
//            event.setBackgroundEnd(color);
//        }

        if (event.getItemStack().getItem().equals(ModItems.PRISMATICRADIANCEINGOT.get())) {
            Random random = new Random();
            int color = random.nextInt() | 0xFF000000;
            event.setBorderStart(color);
            event.setBorderEnd(color);
            event.setBackgroundStart(color);
            event.setBackgroundEnd(color);
        }
    }

//    @SubscribeEvent
//    public static void onTooltip(RenderTooltipEvent.Color event) {
//        float hue = (float) Util.getMillis() / 5000.0F % 1.0F;
//        int c = 0xFF000000 | Mth.hsvToRgb(hue, 2.0F, 1.5F);
//        int cd = 0xFF000000 | Mth.hsvToRgb(hue, 2.0F, 0.1F);
//        if (event.getItemStack().getItem() instanceof HolyRing) {
//            event.setBorderEnd(c);
//            event.setBorderStart(c);
//            event.setBackground(cd);
//        }
//    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack itemStack = event.getItemStack();
        Item item = itemStack.getItem();

        ManaItem manaItem = null;

        if (item instanceof ManaItem directManaItem) {
            manaItem = directManaItem;
        } else {
            var foundManaItem = XplatAbstractions.INSTANCE.findManaItem(itemStack);
            if (foundManaItem != null) {
                manaItem = foundManaItem;
            }
        }

        if (manaItem != null) {
            int currentMana = manaItem.getMana();
            int maxMana = manaItem.getMaxMana();
            Component manaComponent = Component.translatable(
                    "tooltip.ex_enigmaticlegacy.mana_info",
                    String.format("%,d", currentMana),
                    String.format("%,d", maxMana)
            );
            event.getToolTip().add(manaComponent);

            if (maxMana > 0) {
                double percentage = (double) currentMana / maxMana * 100;
                Component percentageComponent = Component.translatable(
                        "tooltip.ex_enigmaticlegacy.mana_percentage",
                        String.format("%.1f", percentage)
                );
                event.getToolTip().add(percentageComponent);
            }
        }
    }
}