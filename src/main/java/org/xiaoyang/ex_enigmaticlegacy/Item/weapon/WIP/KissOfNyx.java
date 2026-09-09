package org.xiaoyang.ex_enigmaticlegacy.Item.weapon.WIP;

import net.minecraft.world.item.SwordItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import java.util.List;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.vine.VineEffects;
import org.xiaoyang.ex_enigmaticlegacy.api.EXEAPI;

public class KissOfNyx extends SwordItem {
    public KissOfNyx() {
        super(EXEAPI.MIRACLE_ITEM_TIER, 99, -2.4F, new Properties());

    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer && !player.getCooldowns().isOnCooldown(this)) {
            if (VineEffects.showAtLook(serverPlayer, VineEffects.ROOT_TICKS + VineEffects.FADE_TICKS) != null)
                player.getCooldowns().addCooldown(this, 24);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ex_enigmaticlegacy.kiss_of_nyx.vines").withStyle(net.minecraft.ChatFormatting.DARK_GREEN));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
