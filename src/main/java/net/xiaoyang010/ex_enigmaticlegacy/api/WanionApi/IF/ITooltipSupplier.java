package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF;

import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.WInteraction;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

@FunctionalInterface
public interface ITooltipSupplier {
    List<Component> getTooltip(WInteraction interaction, Supplier<ItemStack> stackSupplier);
}