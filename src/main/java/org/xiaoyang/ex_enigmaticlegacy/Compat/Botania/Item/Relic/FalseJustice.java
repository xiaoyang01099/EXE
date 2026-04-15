package org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item.Relic;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModDamageSources;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModItems;
import org.xiaoyang.ex_enigmaticlegacy.api.emc.INoEMCItem;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.item.Relic;
import vazkii.botania.common.item.relic.RelicImpl;
import vazkii.botania.xplat.XplatAbstractions;

import javax.annotation.Nullable;
import java.util.List;

public class FalseJustice extends Item implements INoEMCItem {

    public FalseJustice(Properties properties) {
        super(properties);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @org.jetbrains.annotations.Nullable CompoundTag nbt) {
        return new RelicCapProvider(stack);
    }

    private static class RelicCapProvider implements ICapabilityProvider {
        private final LazyOptional<Relic> relic;

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
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide && entity instanceof Player player) {
            var relic = XplatAbstractions.INSTANCE.findRelic(stack);
            if (relic != null) {
                relic.tickBinding(player);
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        RelicImpl.addDefaultTooltip(stack, tooltip);

        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.ex_enigmaticlegacy.false_justice.lore1")
                    .withStyle(ChatFormatting.DARK_PURPLE));
            tooltip.add(Component.translatable("item.ex_enigmaticlegacy.false_justice.lore2")
                    .withStyle(ChatFormatting.DARK_PURPLE));
            tooltip.add(Component.translatable("item.ex_enigmaticlegacy.false_justice.lore3")
                    .withStyle(ChatFormatting.DARK_PURPLE));
            tooltip.add(Component.nullToEmpty(""));
            tooltip.add(Component.translatable("item.ex_enigmaticlegacy.false_justice.warning")
                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        } else {
            tooltip.add(Component.translatable("item.ex_enigmaticlegacy.shift_tooltip")
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }

    private static boolean hasFalseJustice(Player player) {
        for (int slot = 0; slot < player.getInventory().items.size(); slot++) {
            ItemStack stackInSlot = player.getInventory().items.get(slot);
            if (!stackInSlot.isEmpty() && stackInSlot.getItem() == ModItems.FALSE_JUSTICE.get()) {
                var relicCap = stackInSlot.getCapability(BotaniaForgeCapabilities.RELIC);
                if (relicCap.isPresent()) {
                    Relic relic = relicCap.orElse(null);
                    if (relic != null && relic.isRightPlayer(player)) {
                        return true;
                    }
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isDamageAbsolute(DamageSource damageSource) {
        if (damageSource.is(ModDamageSources.ABSOLUTE) ||
                damageSource.is(ModDamageSources.TRUE_DAMAGE) ||
                damageSource.is(ModDamageSources.SOUL_DRAIN) ||
                damageSource.is(ModDamageSources.FATE) ||
                damageSource.is(ModDamageSources.OBLIVION) ||
                damageSource.is(ModDamageSources.PARADOX) ||
                damageSource.is(ModDamageSources.PARADOX_REFLECTION)) {
            return true;
        }
        return damageSource.is(DamageTypeTags.BYPASSES_ARMOR) &&
                damageSource.is(DamageTypeTags.BYPASSES_ENCHANTMENTS) &&
                damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player player && !event.isCanceled()) {
            if (hasFalseJustice(player) && !isDamageAbsolute(event.getSource())) {
                event.setCanceled(true);

                DamageSource trueDamageSource = event.getSource().getEntity() != null
                        ? ModDamageSources.trueDamage(player.level(), event.getSource().getEntity())
                        : ModDamageSources.trueDamage(player.level());

                player.hurt(trueDamageSource, event.getAmount() * 2.0F);
                return;
            }
        }

        if (event.getSource().getEntity() instanceof Player attacker && !event.isCanceled()) {
            if (hasFalseJustice(attacker) && !isDamageAbsolute(event.getSource())) {
                event.setCanceled(true);

                DamageSource trueDamageSource = ModDamageSources.trueDamage(attacker.level(), attacker);
                event.getEntity().hurt(trueDamageSource, event.getAmount() * 2.0F);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player && !event.isCanceled()) {
            if (hasFalseJustice(player) && !isDamageAbsolute(event.getSource())) {
                event.setCanceled(true);

                DamageSource trueDamageSource = event.getSource().getEntity() != null
                        ? ModDamageSources.trueDamage(player.level(), event.getSource().getEntity())
                        : ModDamageSources.trueDamage(player.level());

                player.hurt(trueDamageSource, event.getAmount() * 2.0F);
                return;
            }
        }

        if (event.getSource().getEntity() instanceof Player attacker && !event.isCanceled()) {
            if (hasFalseJustice(attacker) && !isDamageAbsolute(event.getSource())) {
                event.setCanceled(true);

                DamageSource trueDamageSource = ModDamageSources.trueDamage(attacker.level(), attacker);
                event.getEntity().hurt(trueDamageSource, event.getAmount() * 2.0F);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (hasFalseJustice(player) && !isDamageAbsolute(event.getSource())) {
                event.setCanceled(true);
                return;
            }
        }

        if (event.getSource().getEntity() instanceof Player attacker) {
            if (hasFalseJustice(attacker) && !isDamageAbsolute(event.getSource())) {
                event.setCanceled(true);
            }
        }
    }
}