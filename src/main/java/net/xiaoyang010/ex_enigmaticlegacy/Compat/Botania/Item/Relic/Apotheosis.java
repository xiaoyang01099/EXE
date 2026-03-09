package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model.Vector3;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.others.EntityBabylonWeaponSS;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEntities;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.item.IRelic;
import vazkii.botania.common.item.relic.RelicImpl;
import vazkii.botania.xplat.IXplatAbstractions;

import javax.annotation.Nullable;
import java.util.List;

public class Apotheosis extends Item {

    public static final int USAGE_DURATION = 72000;
    public static final int SPAWN_INTERVAL = 2;

    public Apotheosis(Properties properties) {
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

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        RelicImpl.addDefaultTooltip(stack, tooltip);

        if (Screen.hasShiftDown()) {
            tooltip.add(new TranslatableComponent("item.apotheosis.desc1").withStyle(ChatFormatting.GOLD));
            tooltip.add(new TranslatableComponent(""));
            tooltip.add(new TranslatableComponent("item.apotheosis.desc2").withStyle(ChatFormatting.GRAY));
            tooltip.add(new TranslatableComponent("item.apotheosis.desc3").withStyle(ChatFormatting.GRAY));
            tooltip.add(new TranslatableComponent(""));
            tooltip.add(new TranslatableComponent("item.apotheosis.damage_direct").withStyle(ChatFormatting.RED));
            tooltip.add(new TranslatableComponent("item.apotheosis.damage_impact").withStyle(ChatFormatting.RED));
            tooltip.add(new TranslatableComponent("item.apotheosis.desc4").withStyle(ChatFormatting.YELLOW));
        } else {
            tooltip.add(new TranslatableComponent("item.FRShiftTooltip.lore").withStyle(ChatFormatting.GRAY));
        }
    }

    public void spawnBabylonWeapon(Player player) {
        if (!player.level.isClientSide) {
            Vector3 originalPos = Vector3.fromEntityCenter(player);
            EntityBabylonWeaponSS weapon = new EntityBabylonWeaponSS(ModEntities.BABYLON_WEAPON_SS.get(), player.level, player);

            double rawYaw = player.getYHeadRot();
            double yaw;
            if (rawYaw < 0.0F) {
                yaw = Math.abs(rawYaw);
            } else {
                yaw = 360.0F - rawYaw;
            }

            double x = 0.0;
            double z = 0.0;

            if (yaw >= 0.0 && yaw <= 90.0) {
                double m = (yaw - 0.0) / 90.0;
                z = 1.0 - m;
                x = m;
            } else if (yaw >= 90.0 && yaw <= 180.0) {
                double m = (yaw - 90.0) / 90.0;
                x = 1.0 - m;
                z = -m;
            } else if (yaw >= 180.0 && yaw <= 270.0) {
                double m = (yaw - 180.0) / 90.0;
                z = -(1.0 - m);
                x = -m;
            } else if (yaw >= 270.0 && yaw <= 360.0) {
                double m = (yaw - 270.0) / 90.0;
                x = -(1.0 - m);
                z = m;
            }

            double multV2 = yaw % 90.0;
            if (multV2 > 45.0) {
                multV2 = 45.0 - (multV2 - 45.0);
            }

            double multV3 = 1.0 + multV2 / 90.0;
            Vector3 lookVec = new Vector3(x * multV3, 0.0, z * multV3);
            Vector3 additive = lookVec.copy();

            for (int attempts = 0; attempts <= 100; attempts++) {
                Vector3 finalVec = lookVec.copy();

                double negative = Math.random() >= 0.5 ? -1.0 : 1.0;
                finalVec.rotate(Math.toRadians(80.0 * negative), new Vector3(0.0, 1.0, 0.0));

                finalVec.multiply(2.0 + Math.random() * 10.0);
                finalVec.add(additive.copy().multiply(Math.random()));
                finalVec.y += -0.5 + Math.random() * 8.0;
                finalVec.add(originalPos.copy());

                double range = 2.0;
                List<EntityBabylonWeaponSS> existingWeapons = player.level.getEntitiesOfClass(
                        EntityBabylonWeaponSS.class,
                        new AABB(
                                finalVec.x - range, finalVec.y - range, finalVec.z - range,
                                finalVec.x + range, finalVec.y + range, finalVec.z + range
                        )
                );

                if (existingWeapons.isEmpty()) {
                    lookVec = finalVec;
                    break;
                }
            }

            weapon.setPos(lookVec.x, lookVec.y, lookVec.z);
            weapon.setYRot(player.getYHeadRot());
            weapon.setVariety(player.level.random.nextInt(12));
            weapon.setDelay(0);
            weapon.setRotation(Mth.wrapDegrees(-player.getYHeadRot() + 180.0F));

            player.level.addFreshEntity(weapon);

            player.level.playSound(null, weapon.getX(), weapon.getY(), weapon.getZ(),
                    SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F,
                    1.0F + player.level.random.nextFloat() * 3.0F);
        }
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
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return USAGE_DURATION;
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

        if (usedDuration != this.getUseDuration(stack) &&
                usedDuration % SPAWN_INTERVAL == 0 &&
                !level.isClientSide) {

            this.spawnBabylonWeapon(player);
        }
    }
}