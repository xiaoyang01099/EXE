package net.xiaoyang010.ex_enigmaticlegacy.Compat.Projecte;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EMCWand extends Item {
    public EMCWand() {
        super(new Properties().stacksTo(1).rarity(Rarity.EPIC).tab(ModTabs.TAB_EXENIGMATICLEGACY_ITEM));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openGui(serverPlayer, new EMCWandMenuProvider(), buf -> {
                buf.writeBoolean(false);
            });
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<net.minecraft.network.chat.Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(new TranslatableComponent("tooltip.ex_enigmaticlegacy.emc_wand.desc1"));
        tooltip.add(new TranslatableComponent("tooltip.ex_enigmaticlegacy.emc_wand.desc2"));
    }
}