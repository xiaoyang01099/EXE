package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
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
import vazkii.botania.common.helper.ItemNBTHelper;
import vazkii.botania.common.item.relic.RelicImpl;
import vazkii.botania.xplat.IXplatAbstractions;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class DeificAmulet extends Item implements ICurioItem, INoEMCItem {

    private static final boolean EFFECT_IMMUNITY_ENABLED = true;
    private static final boolean INVINCIBILITY_ENABLED = true;
    private static final boolean ONLY_NEGATE_DEBUFFS = false;
    private static final int INVINCIBILITY_COOLDOWN = 32;
    private static final int INVINCIBILITY_DURATION = 40;
    private static final int AIR_RESTORE_AMOUNT = 300;

    public DeificAmulet(Properties properties) {
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
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide && entity instanceof Player player) {
            var relic = IXplatAbstractions.INSTANCE.findRelic(stack);
            if (relic != null) {
                relic.tickBinding(player);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        RelicImpl.addDefaultTooltip(stack, tooltip);

        if (Screen.hasShiftDown()) {
            if (EFFECT_IMMUNITY_ENABLED) {
                if (ONLY_NEGATE_DEBUFFS) {
                    tooltip.add(new TranslatableComponent("item.ItemDeificAmulet1_alt.lore")
                            .withStyle(ChatFormatting.GOLD));
                } else {
                    tooltip.add(new TranslatableComponent("item.ItemDeificAmulet1.lore")
                            .withStyle(ChatFormatting.GOLD));
                }
            }
            tooltip.add(new TranslatableComponent("item.ItemDeificAmulet2.lore")
                    .withStyle(ChatFormatting.GOLD));
            if (INVINCIBILITY_ENABLED) {
                tooltip.add(new TranslatableComponent("item.ItemDeificAmulet3.lore")
                        .withStyle(ChatFormatting.GOLD));
            }
            tooltip.add(new TranslatableComponent(""));
            tooltip.add(new TranslatableComponent("item.ItemDeificAmulet4.lore")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(new TranslatableComponent(""));
        } else {
            tooltip.add(new TranslatableComponent("item.FRShiftTooltip.lore")
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
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

        Level level = entity.level;

        if (!level.isClientSide && entity instanceof Player player) {

            if (entity.getActiveEffects().size() > 0 && EFFECT_IMMUNITY_ENABLED) {
                if (ONLY_NEGATE_DEBUFFS) {
                    List<MobEffect> effectsToRemove = new ArrayList<>();
                    for (MobEffectInstance effect : entity.getActiveEffects()) {
                        MobEffect mobEffect = effect.getEffect();
                        if (!mobEffect.isBeneficial()) {
                            effectsToRemove.add(mobEffect);
                        }
                    }
                    for (MobEffect effect : effectsToRemove) {
                        entity.removeEffect(effect);
                    }
                } else {
                    entity.removeAllEffects();
                }
            }

            if (entity.isOnFire()) {
                entity.clearFire();
            }

            if (entity.getAirSupply() == 0) {
                entity.setAirSupply(AIR_RESTORE_AMOUNT);
            }

            if (INVINCIBILITY_ENABLED) {
                int cooldown = ItemNBTHelper.getInt(stack, "ICooldown", 0);

                if (cooldown == 0 && entity.invulnerableTime > 10) {
                    entity.invulnerableTime = INVINCIBILITY_DURATION;
                    ItemNBTHelper.setInt(stack, "ICooldown", INVINCIBILITY_COOLDOWN);
                }

                if (cooldown > 0) {
                    ItemNBTHelper.setInt(stack, "ICooldown", cooldown - 1);
                }
            }
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
                && context.identifier().equals("necklace");
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

    @Override
    public boolean canSync(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public List<Component> getAttributesTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(new TranslatableComponent("curios.tooltip.immunity")
                .withStyle(ChatFormatting.YELLOW));
        if (INVINCIBILITY_ENABLED) {
            tooltips.add(new TranslatableComponent("curios.tooltip.invincibility")
                    .withStyle(ChatFormatting.YELLOW));
        }
        return tooltips;
    }
}