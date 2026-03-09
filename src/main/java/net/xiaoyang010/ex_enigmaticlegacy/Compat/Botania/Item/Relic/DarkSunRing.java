package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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
import net.xiaoyang010.ex_enigmaticlegacy.api.INoEMCItem;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.item.IRelic;
import vazkii.botania.common.item.relic.RelicImpl;
import vazkii.botania.xplat.IXplatAbstractions;

import javax.annotation.Nullable;
import java.util.List;

public class DarkSunRing extends Item implements ICurioItem, INoEMCItem {

    public DarkSunRing(Properties properties) {
        super(properties);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @org.jetbrains.annotations.Nullable CompoundTag nbt) {
        return new RelicCapProvider(stack);
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
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.dark_sun_ring.lore1")
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.dark_sun_ring.lore2_1")
                    .append(" " + 100)
                    .append(new TranslatableComponent("item.ex_enigmaticlegacy.dark_sun_ring.lore2_2"))
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.dark_sun_ring.lore3")
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(new TextComponent(""));

            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.dark_sun_ring.lore4_1")
                    .append(" " + (int)(0.2F * 100.0F))
                    .append(new TranslatableComponent("item.ex_enigmaticlegacy.dark_sun_ring.lore4_2"))
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.dark_sun_ring.lore5")
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(new TextComponent(""));

            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.dark_sun_ring.lore6")
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.dark_sun_ring.lore7")
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(new TextComponent(""));

            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.dark_sun_ring.lore8")
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.dark_sun_ring.lore9")
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(new TextComponent(""));
        } else {
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.shift_tooltip")
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide && entity instanceof Player player) {
            var relic = IXplatAbstractions.INSTANCE.findRelic(stack);
            if (relic != null) {
                relic.tickBinding(player);
            }
        }
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();

        if (entity instanceof Player player) {
            var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
            if (relicCap.isPresent()) {
                IRelic relic = relicCap.orElse(null);
                if (relic != null && !relic.isRightPlayer(player)) {
                    return;
                }
            }
        }

        if (entity.isOnFire()) {
            entity.clearFire();
        }
    }

    @Override
    public boolean canEquip(SlotContext context, ItemStack stack) {
        if (context.entity() instanceof Player player) {
            var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
            if (relicCap.isPresent()) {
                IRelic relic = relicCap.orElse(null);
                if (relic != null && !relic.isRightPlayer(player)) {
                    return false;
                }
            }
        }

        return CuriosApi.getCuriosHelper().findEquippedCurio(this, context.entity()).isEmpty()
                && context.identifier().equals("ring");
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        if (slotContext.entity() instanceof Player player) {
            var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
            if (relicCap.isPresent()) {
                IRelic relic = relicCap.orElse(null);
                if (relic != null && !relic.isRightPlayer(player)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        if (slotContext.entity() instanceof Player player) {
            var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
            if (relicCap.isPresent()) {
                IRelic relic = relicCap.orElse(null);
                if (relic != null && !relic.isRightPlayer(player)) {
                    return;
                }
            }
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
    }
}