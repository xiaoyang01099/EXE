package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.xiaoyang010.ex_enigmaticlegacy.Event.RelicsEventHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;
import net.xiaoyang010.ex_enigmaticlegacy.api.IFE.INoEMCItem;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.item.IRelic;
import vazkii.botania.common.helper.ItemNBTHelper;
import vazkii.botania.common.item.relic.RelicImpl;
import vazkii.botania.xplat.IXplatAbstractions;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.List;

public class FateTome extends Item implements INoEMCItem {
    public static final int FATE_TOME_COOLDOWN_MIN = 30;
    public static final int FATE_TOME_COOLDOWN_MAX = 90;

    public FateTome(Properties properties) {
        super(properties);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @org.jetbrains.annotations.Nullable CompoundTag nbt) {
        return new FateTome.RelicCapProvider(stack);
    }

    private static class RelicCapProvider implements ICapabilityProvider {
        private final LazyOptional<IRelic> relic;

        public RelicCapProvider(ItemStack stack) {
            this.relic = LazyOptional.of(() -> new RelicImpl(stack, null));
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @org.jetbrains.annotations.Nullable Direction direction) {
            if (capability == BotaniaForgeCapabilities.RELIC) {
                return relic.cast();
            }
            return LazyOptional.empty();
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        RelicImpl.addDefaultTooltip(stack, tooltip);

        if (Screen.hasShiftDown()) {
            tooltip.add(new TranslatableComponent("item.ItemFateTome1.lore").withStyle(ChatFormatting.GRAY));
            tooltip.add(new TranslatableComponent("item.ItemFateTome2.lore").withStyle(ChatFormatting.GRAY));
            tooltip.add(new TranslatableComponent("item.ItemFateTome3.lore").withStyle(ChatFormatting.GRAY));
            tooltip.add(new TranslatableComponent("item.FREmpty.lore"));

            if (FATE_TOME_COOLDOWN_MAX != 0) {
                tooltip.add(new TranslatableComponent("item.ItemFateTome5_1.lore")
                        .append(" " + FATE_TOME_COOLDOWN_MIN + "-" + FATE_TOME_COOLDOWN_MAX + " ")
                        .append(new TranslatableComponent("item.ItemFateTome5_2.lore"))
                        .withStyle(ChatFormatting.GRAY));
                tooltip.add(new TranslatableComponent("item.FREmpty.lore"));
            }

            tooltip.add(new TranslatableComponent("item.ItemFateTome6.lore").withStyle(ChatFormatting.GRAY));
            tooltip.add(new TranslatableComponent("item.ItemFateTome7.lore").withStyle(ChatFormatting.GRAY));
            tooltip.add(new TranslatableComponent("item.FREmpty.lore"));
            tooltip.add(new TranslatableComponent("item.ItemFateTome8.lore").withStyle(ChatFormatting.YELLOW));
            tooltip.add(new TranslatableComponent("item.ItemFateTome9.lore").withStyle(ChatFormatting.YELLOW));
        } else {
            tooltip.add(new TranslatableComponent("item.FRShiftTooltip.lore").withStyle(ChatFormatting.GRAY));
        }

        if (stack.hasTag() && ItemNBTHelper.verifyExistance(stack, "IFateCooldown") && ItemNBTHelper.getInt(stack, "IFateCooldown", 0) > 0) {
            tooltip.add(new TranslatableComponent("item.FREmpty.lore"));
            int randomCode = (int)(Math.random() * 15.0 + 1.0);
            tooltip.add(new TranslatableComponent("item.FRCode" + randomCode + ".lore")
                    .append(new TranslatableComponent("item.ItemFateTomeCooldown.lore"))
                    .append(" " + (new BigDecimal((double)ItemNBTHelper.getInt(stack, "IFateCooldown", 0) / 20.0)).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue() + " ")
                    .append(new TranslatableComponent("item.FRSeconds.lore"))
                    .withStyle(ChatFormatting.RED));
        }

        tooltip.add(new TranslatableComponent("item.FREmpty.lore"));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && entity instanceof Player player) {
            var relic = IXplatAbstractions.INSTANCE.findRelic(stack);
            if (relic != null) {
                relic.tickBinding(player);
            }

            var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
            if (relicCap.isPresent()) {
                IRelic relicInstance = relicCap.orElse(null);
                if (relicInstance != null && !relicInstance.isRightPlayer(player)) {
                    return;
                }
            }

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

//    @Override
//    public boolean isFoil(ItemStack stack) {
//        return stack.hasTag() && ItemNBTHelper.verifyExistance(stack, "IFateCooldown")
//                && ItemNBTHelper.getInt(stack, "IFateCooldown", 0) == 0;
//    }
}