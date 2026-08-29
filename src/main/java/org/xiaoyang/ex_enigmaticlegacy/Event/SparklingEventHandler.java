package org.xiaoyang.ex_enigmaticlegacy.Event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.xiaoyang.ex_enigmaticlegacy.Client.particle.state.SparklingFlightState;
import org.xiaoyang.ex_enigmaticlegacy.Util.SparklingOutlineSync;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModEffects;
import org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket.SparklingBoostC2SPacket;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
@Mod.EventBusSubscriber(modid = Exe.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SparklingEventHandler {
    public static final Set<UUID> SYNCED_EFFECT_ENTITIES = new HashSet<>();

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        LivingEntity entity = event.getEntity();
        if (!hasSparklingFruitEffect(entity)) return;

        DamageSource source = event.getSource();
        if (source.is(DamageTypes.GENERIC_KILL)) {
            return;
        }

        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLivingKnockBack(LivingKnockBackEvent event) {
        if (hasSparklingFruitEffect(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        if (!hasSparklingFruitEffect(entity)) {
            if (entity instanceof Player player) {
                SparklingFlightState.restorePlayer(player);
            }
            syncOutlineRemoval(entity);
            return;
        }

        if (entity.isInWaterOrBubble()) {
            entity.removeEffect(ModEffects.SPARKLING_EFFECT.get());
            SparklingOutlineSync.sendInactive(entity);
            SYNCED_EFFECT_ENTITIES.remove(entity.getUUID());
            if (entity instanceof Player player) {
                SparklingFlightState.restorePlayer(player);
                entity.removeEffect(MobEffects.MOVEMENT_SPEED);
                entity.removeEffect(MobEffects.JUMP);
            }
            return;
        }

        syncOutlineState(entity);

        if (entity.isOnFire()) {
            entity.clearFire();
        }
        if (entity.hasEffect(MobEffects.DARKNESS)) {
            entity.removeEffect(MobEffects.DARKNESS);
        }
        if (entity instanceof Player player) {
            SparklingFlightState.maintainFlight(player);
            SparklingFlightState.tickBoost(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        SparklingFlightState.restorePlayer(event.getEntity());
        SparklingBoostC2SPacket.clearCooldown(event.getEntity());
        SYNCED_EFFECT_ENTITIES.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onPlayerStartTracking(PlayerEvent.StartTracking event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        Entity target = event.getTarget();
        if (target instanceof LivingEntity livingEntity && hasSparklingFruitEffect(livingEntity)) {
            SparklingOutlineSync.sendActiveToPlayer(livingEntity, serverPlayer);
        }
        if (target instanceof Player targetPlayer && SparklingFlightState.isBoosting(targetPlayer)) {
            SparklingFlightState.sendBoostStateToPlayer(targetPlayer, serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        SparklingFlightState.stopBoostForDimensionChange(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        SparklingFlightState.restoreClonedPlayer(event.getOriginal(), event.getEntity());
    }

    public static void syncOutlineRemoval(LivingEntity entity) {
        if (entity == null || entity.level().isClientSide()) return;
        if (SYNCED_EFFECT_ENTITIES.remove(entity.getUUID())) {
            SparklingOutlineSync.sendInactive(entity);
        }
    }

    public static void syncOutlineRefresh(LivingEntity entity) {
        if (entity == null || entity.level().isClientSide()) return;
        SYNCED_EFFECT_ENTITIES.add(entity.getUUID());
        SparklingOutlineSync.sendActive(entity);
    }

    public static void syncOutlineState(LivingEntity entity) {
        if (entity == null || entity.level().isClientSide()) return;
        if (SYNCED_EFFECT_ENTITIES.add(entity.getUUID())) {
            SparklingOutlineSync.sendActive(entity);
        }
    }

    public static boolean hasSparklingFruitEffect(LivingEntity entity) {
        return entity != null && entity.hasEffect(ModEffects.SPARKLING_EFFECT.get());
    }
}
