package org.xiaoyang.ex_enigmaticlegacy.Util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModEffects;
import org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket.SparklingFruitOutlineS2CPacket;
import org.xiaoyang.ex_enigmaticlegacy.Network.NetworkHandler;

public class SparklingOutlineSync {
    public static final int CLOSE_DURATION_TICKS = 0;

    public static void sendActive(LivingEntity entity) {
        if (entity == null || entity.level().isClientSide()) return;
        int durationTicks = getRemainingDuration(entity);
        if (durationTicks <= 0) return;
        NetworkHandler.sendToTrackingEntityAndSelf(new SparklingFruitOutlineS2CPacket(entity.getId(), true, durationTicks), entity);
    }

    public static void sendInactive(LivingEntity entity) {
        if (entity == null || entity.level().isClientSide()) return;
        NetworkHandler.sendToTrackingEntityAndSelf(new SparklingFruitOutlineS2CPacket(entity.getId(), false, CLOSE_DURATION_TICKS), entity);
    }

    public static void sendActiveToPlayer(LivingEntity entity, ServerPlayer player) {
        if (entity == null || player == null || entity.level().isClientSide()) return;
        int durationTicks = getRemainingDuration(entity);
        if (durationTicks <= 0) return;
        NetworkHandler.sendToPlayer(new SparklingFruitOutlineS2CPacket(entity.getId(), true, durationTicks), player);
    }

    public static int getRemainingDuration(LivingEntity entity) {
        if (entity == null) return 0;
        MobEffectInstance instance = entity.getEffect(ModEffects.SPARKLING_EFFECT.get());
        return instance == null ? 0 : instance.getDuration();
    }
}
