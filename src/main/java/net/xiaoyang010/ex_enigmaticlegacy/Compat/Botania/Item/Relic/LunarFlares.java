package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model.Vector3;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.others.EntityLunarFlare;
import net.xiaoyang010.ex_enigmaticlegacy.Event.RelicsEventHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEntities;
import net.xiaoyang010.ex_enigmaticlegacy.api.INoEMCItem;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.item.IRelic;
import vazkii.botania.common.item.relic.RelicImpl;
import vazkii.botania.xplat.IXplatAbstractions;

import javax.annotation.Nullable;
import java.util.List;

public class LunarFlares extends Item implements INoEMCItem {

    public static final float DAMAGE_DIRECT = 100.0F;    // 直击伤害
    public static final float DAMAGE_IMPACT = 75.0F;     // 爆炸伤害
    public static final int USAGE_DURATION = 72000;      // 使用持续时间
    public static final int FIRE_INTERVAL = 2;           // 发射间隔（ticks）
    public static final int SOUND_INTERVAL = 4;          // 音效间隔（ticks）
    public static final float DETECTION_RANGE = 128.0F;  // 检测范围

    public LunarFlares(Properties properties) {
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
            tooltip.add(new TranslatableComponent("item.lunar_flares.desc1"));
            tooltip.add(new TranslatableComponent(""));
            tooltip.add(new TranslatableComponent("item.lunar_flares.desc2"));
            tooltip.add(new TranslatableComponent("item.lunar_flares.damage_impact", (int)DAMAGE_IMPACT));
            tooltip.add(new TranslatableComponent("item.lunar_flares.desc3"));
            tooltip.add(new TranslatableComponent(""));
            tooltip.add(new TranslatableComponent("item.lunar_flares.damage_direct", (int)DAMAGE_DIRECT));
        } else {
            tooltip.add(new TranslatableComponent("item.FRShiftTooltip.lore"));
        }
        tooltip.add(new TranslatableComponent(""));
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

    public void spawnLunarFlare(Level world, Player player, BlockHitResult hitResult) {
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof LunarFlares) {
            var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
            if (relicCap.isPresent()) {
                IRelic relic = relicCap.orElse(null);
                if (relic != null && !relic.isRightPlayer(player)) {
                    return;
                }
            }
        }

        if (hitResult != null && !world.isClientSide && hitResult.getType() == HitResult.Type.BLOCK) {
            EntityLunarFlare flare = new EntityLunarFlare(
                    ModEntities.LUNAR_FLARE.get(),
                    world,
                    player,
                    hitResult.getBlockPos().getX(),
                    hitResult.getBlockPos().getY(),
                    hitResult.getBlockPos().getZ()
            );

            double targetX = hitResult.getBlockPos().getX() + 0.5;
            double targetY = hitResult.getBlockPos().getY();
            double targetZ = hitResult.getBlockPos().getZ() + 0.5;

            double spawnX = targetX + (Math.random() - 0.5) * 12.0;
            double spawnY = targetY + 24 + (Math.random() - 0.5) * 12.0;
            double spawnZ = targetZ + (Math.random() - 0.5) * 12.0;

            flare.setPos(spawnX, spawnY, spawnZ);

            Vector3 targetPos = new Vector3(targetX, targetY, targetZ);
            Vector3 spawnPos = new Vector3(spawnX, spawnY, spawnZ);
            Vector3 direction = targetPos.copy().sub(spawnPos).normalize().multiply(0.4);

            flare.setDeltaMovement(direction.x, direction.y, direction.z);

            world.addFreshEntity(flare);
        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return USAGE_DURATION;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
        if (relicCap.isPresent()) {
            IRelic relic = relicCap.orElse(null);
            if (relic != null && !relic.isRightPlayer(player)) {
                return InteractionResultHolder.fail(stack);
            }
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (!(entity instanceof Player player)) return;

        var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
        if (relicCap.isPresent()) {
            IRelic relic = relicCap.orElse(null);
            if (relic != null && !relic.isRightPlayer(player)) {
                player.stopUsingItem();
                return;
            }
        }

        int usedDuration = this.getUseDuration(stack) - remainingUseDuration;

        if (usedDuration != this.getUseDuration(stack) && usedDuration % FIRE_INTERVAL == 0 && !level.isClientSide) {
            BlockHitResult hitResult = RelicsEventHandler.getPointedBlock(player, level, DETECTION_RANGE);

            if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
                this.spawnLunarFlare(level, player, hitResult);

                if (usedDuration % SOUND_INTERVAL == 0) {
                    level.playSound(null,
                            player.getX(), player.getY(), player.getZ(),
                            SoundEvents.FIREWORK_ROCKET_LAUNCH,
                            SoundSource.PLAYERS,
                            2.0F,
                            1.0F + (float)Math.random() * 0.5F
                    );
                }
            }
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        super.releaseUsing(stack, level, entity, timeLeft);
    }
}