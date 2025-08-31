package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.common.helper.ItemNBTHelper;

import java.util.List;

public class AntigravityCharm extends Item {
    public static final String ACTIVE_KEY = "isActive";

    public AntigravityCharm(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        boolean aBoolean = ItemNBTHelper.getBoolean(pStack, ACTIVE_KEY, false);
        pTooltipComponents.add(new TextComponent("开启：" + aBoolean));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!world.isClientSide && player.isCrouching()) {
            boolean currentState = ItemNBTHelper.getBoolean(stack, ACTIVE_KEY, false);
            ItemNBTHelper.setBoolean(stack, ACTIVE_KEY, !currentState);
        }

        return InteractionResultHolder.success(stack);
    }
}