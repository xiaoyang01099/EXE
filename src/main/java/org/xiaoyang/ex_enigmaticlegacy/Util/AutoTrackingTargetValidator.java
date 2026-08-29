package org.xiaoyang.ex_enigmaticlegacy.Util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.xiaoyang.ex_enigmaticlegacy.Config.MagicBowConfig;

// AutoTrackingTargetValidator 统一校验自动追踪客户端预览和服务端射击目标。
public class AutoTrackingTargetValidator {
    // 判断客户端是否可以把实体作为锁定目标。
    public static boolean isValidClientTarget(Player player, Entity entity) {
        if (!(entity instanceof LivingEntity living) || !living.isAlive()) return false;
        if (entity.getUUID().equals(player.getUUID())) return false;
        if (ClientWhitelistCache.isInWhitelist(entity.getType())) return false;
        return isInLockCone(player, living);
    }

    // 判断服务端是否接受客户端发送的射击目标。
    public static boolean isValidServerTarget(Player player, Entity entity) {
        if (!(entity instanceof LivingEntity living) || !living.isAlive()) return false;
        if (entity.getUUID().equals(player.getUUID())) return false;
        if (EntityUtil.isInDamageWhitelist(entity)) return false;
        return isInLockCone(player, living);
    }

    // 判断目标是否在自动追踪的距离、角度和视线范围内。
    public static boolean isInLockCone(Player player, LivingEntity target) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 targetCenter = target.getBoundingBox().getCenter();
        Vec3 toTarget = targetCenter.subtract(eyePos);
        double distanceSqr = toTarget.lengthSqr();
        double maxRange = MagicBowConfig.autoTrackingMaxLockRange();
        if (distanceSqr > maxRange * maxRange) return false;
        if (distanceSqr <= 1.0E-6D) return false;

        // 使用点积比较夹角，避免每个实体都计算反三角函数。
        Vec3 direction = toTarget.normalize();
        double cosMaxAngle = Math.cos(Math.toRadians(MagicBowConfig.autoTrackingMaxLockAngle()));
        if (direction.dot(player.getLookAngle()) < cosMaxAngle) return false;
        return !MagicBowConfig.autoTrackingRequireLineOfSight() || player.hasLineOfSight(target);
    }

    // 计算目标离玩家准心的角度评分，越小越靠近准心。
    public static double aimScore(Player player, LivingEntity target) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 toTarget = target.getBoundingBox().getCenter().subtract(eyePos);
        if (toTarget.lengthSqr() <= 1.0E-6D) return Double.MAX_VALUE;
        return 1.0D - toTarget.normalize().dot(player.getLookAngle());
    }
}
