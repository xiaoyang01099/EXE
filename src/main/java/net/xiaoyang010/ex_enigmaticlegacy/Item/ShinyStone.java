package net.xiaoyang010.ex_enigmaticlegacy.Item;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;

import javax.annotation.Nullable;
import java.util.List;

public class ShinyStone extends Item {
    private boolean isPermanentDay = false;

    public ShinyStone() {
        super(new Item.Properties()
                .tab(ModTabs.TAB_EXENIGMATICLEGACY_ITEM)
                .stacksTo(1)
                .fireResistant()
                .rarity(Rarity.EPIC));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {

        tooltip.add(new TranslatableComponent("item.daynight.controller"));

        super.appendHoverText(stack, world, tooltip, flag);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (!world.isClientSide) {

            isPermanentDay = !isPermanentDay;

            if (isPermanentDay) {
                // 启动永久白天
                world.getDayTime();
                world.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, world.getServer());
                player.displayClientMessage(new TextComponent("永日开始!"), true);
            } else {
                // 恢复正常昼夜循环
                world.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(true, world.getServer());
                player.displayClientMessage(new TextComponent("恢复昼夜循环!"), true);
            }
        }

        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), world.isClientSide());
    }
}
