package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.Event.KeybindHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Util.EComponent;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.List;

public class DiscordRing extends Item implements ICurioItem {

    public DiscordRing(Properties rarity) {
        super(rarity);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(EComponent.translatable("item.ex_enigmaticlegacy.discord_ring.lore1"));
            tooltip.add(EComponent.translatable("item.ex_enigmaticlegacy.discord_ring.lore2"));
            tooltip.add(EComponent.translatable("item.ex_enigmaticlegacy.discord_ring.lore3"));
            tooltip.add(EComponent.literal(""));
            tooltip.add(EComponent.translatable("item.ex_enigmaticlegacy.discord_ring.lore4")
                    .append(" ")
                    .append(EComponent.literal(KeybindHandler.getDiscordRingKeyName()).withStyle(ChatFormatting.GOLD)));
            tooltip.add(EComponent.literal(""));
            tooltip.add(EComponent.translatable("item.ex_enigmaticlegacy.curio.ring").withStyle(ChatFormatting.GOLD));
        } else {
            tooltip.add(EComponent.translatable("item.ex_enigmaticlegacy.shift_tooltip").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }
}
