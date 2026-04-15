package org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item.Relic;

import com.aizistral.enigmaticlegacy.api.items.ICursed;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.xiaoyang.ex_enigmaticlegacy.Event.RelicsEventHandler;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModItems;
import org.xiaoyang.ex_enigmaticlegacy.api.emc.INoEMCItem;
import vazkii.botania.common.helper.ItemNBTHelper;
import vazkii.botania.common.item.relic.RelicImpl;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.List;

public class FateTome extends Item implements INoEMCItem, ICursed {
    public static final int FATE_TOME_COOLDOWN_MIN = 30;
    public static final int FATE_TOME_COOLDOWN_MAX = 90;

    public FateTome(Properties properties) {
        super(properties);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        ItemLoreHelper.indicateCursedOnesOnly(tooltip);
        RelicImpl.addDefaultTooltip(stack, tooltip);

        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.ItemFateTome1.lore").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("item.ItemFateTome2.lore").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("item.ItemFateTome3.lore").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("item.FREmpty.lore"));

            if (FATE_TOME_COOLDOWN_MAX != 0) {
                tooltip.add(Component.translatable("item.ItemFateTome5_1.lore")
                        .append(" " + FATE_TOME_COOLDOWN_MIN + "-" + FATE_TOME_COOLDOWN_MAX + " ")
                        .append(Component.translatable("item.ItemFateTome5_2.lore"))
                        .withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.translatable("item.FREmpty.lore"));
            }

            tooltip.add(Component.translatable("item.ItemFateTome6.lore").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("item.ItemFateTome7.lore").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("item.FREmpty.lore"));
            tooltip.add(Component.translatable("item.ItemFateTome8.lore").withStyle(ChatFormatting.YELLOW));
            tooltip.add(Component.translatable("item.ItemFateTome9.lore").withStyle(ChatFormatting.YELLOW));
        } else {
            tooltip.add(Component.translatable("item.FRShiftTooltip.lore").withStyle(ChatFormatting.GRAY));
        }

        if (stack.hasTag() && ItemNBTHelper.verifyExistance(stack, "IFateCooldown") && ItemNBTHelper.getInt(stack, "IFateCooldown", 0) > 0) {
            tooltip.add(Component.translatable("item.FREmpty.lore"));
            int randomCode = (int)(Math.random() * 15.0 + 1.0);
            tooltip.add(Component.translatable("item.FRCode" + randomCode + ".lore")
                    .append(Component.translatable("item.ItemFateTomeCooldown.lore"))
                    .append(" " + (new BigDecimal((double)ItemNBTHelper.getInt(stack, "IFateCooldown", 0) / 20.0)).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue() + " ")
                    .append(Component.translatable("item.FRSeconds.lore"))
                    .withStyle(ChatFormatting.RED));
        }

        tooltip.add(Component.translatable("item.FREmpty.lore"));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && entity instanceof Player player) {
            if (!stack.hasTag()) {
                ItemNBTHelper.setInt(stack, "IFateID", (int)(Math.random() * Integer.MAX_VALUE));
                ItemNBTHelper.setInt(stack, "IFateCooldown", 0);
            } else if (ItemNBTHelper.verifyExistance(stack, "IFateCooldown") && ItemNBTHelper.getInt(stack, "IFateCooldown", 0) > 0) {
                ItemNBTHelper.setInt(stack, "IFateCooldown", ItemNBTHelper.getInt(stack, "IFateCooldown", 0) - 1);
                if (ItemNBTHelper.getInt(stack, "IFateCooldown", 0) == 0) {
                    RelicsEventHandler.sendNotification(player, 1);
                }
                player.containerMenu.broadcastChanges();
            }

            if (Math.random() <= 1.6E-5) {
                if (RelicsEventHandler.itemSearch(player, ModItems.FATE_TOME.get()).size() > 1) {
                    RelicsEventHandler.insanelyDisastrousConsequences(player);
                }
            }
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.hasTag() && ItemNBTHelper.verifyExistance(stack, "IFateCooldown")
                && ItemNBTHelper.getInt(stack, "IFateCooldown", 0) == 0;
    }
}