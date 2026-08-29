package org.xiaoyang.ex_enigmaticlegacy.api.shader.slash;

import net.minecraft.util.Mth;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

public final class GroundImpactShockwave {
    private GroundImpactShockwave() {
    }

    public static float lineProgressAtGround(Vec3 lineStart, Vec3 lineEnd, double groundY) {
        if (lineStart == null || lineEnd == null) return 1.0f;

        double deltaY = lineEnd.y - lineStart.y;
        if (Math.abs(deltaY) <= 1.0e-8) return 1.0f;
        return Mth.clamp((float) ((groundY - lineStart.y) / deltaY), 0.0f, 1.0f);
    }

    public static float contactAge(Vec3 lineStart, Vec3 lineEnd, int startTick, double groundY) {
        float lineProgress = lineProgressAtGround(lineStart, lineEnd, groundY);
        float sweepTimeProgress = 1.0f - (float) Math.pow(1.0f - lineProgress, 0.2f);
        return startTick
                + SlashCofig.WorldSlash.PRE_CRACK_TICKS
                + sweepTimeProgress * Math.max(1.0f, SlashCofig.WorldSlash.SWEEP_TICKS);
    }

    public static float progress(float age, float contactAge, float durationTicks) {
        if (!Float.isFinite(contactAge)) return -1.0f;
        return Mth.clamp((age - contactAge) / Math.max(0.001f, durationTicks), 0.0f, 1.0f);
    }

    public static boolean isActive(float age, float contactAge, float durationTicks) {
        return Float.isFinite(contactAge)
                && age >= contactAge
                && age < contactAge + Math.max(0.001f, durationTicks);
    }

    public static float radius(float startRadius, float maximumRadius, float progress) {
        float t = Mth.clamp(progress, 0.0f, 1.0f);
        float inverse = 1.0f - t;
        float eased = 1.0f - inverse * inverse * inverse;
        return Mth.lerp(eased, Math.max(0.0f, startRadius), Math.max(startRadius, maximumRadius));
    }

    public static float alpha(float progress) {
        float inverse = 1.0f - Mth.clamp(progress, 0.0f, 1.0f);
        return inverse * inverse;
    }

    public static float maximumRadius(float domainRadius) {
        return Mth.clamp(
                domainRadius * SlashCofig.WorldSlash.FinalStrike.GroundImpact.MAX_RADIUS_SCALE,
                SlashCofig.WorldSlash.FinalStrike.GroundImpact.MIN_MAX_RADIUS,
                SlashCofig.WorldSlash.FinalStrike.GroundImpact.MAX_RADIUS
        );
    }

    public static float waveWidth(float progress) {
        return SlashCofig.WorldSlash.FinalStrike.GroundImpact.START_WIDTH
                * Mth.lerp(
                        Mth.clamp(progress, 0.0f, 1.0f),
                        1.0f,
                        SlashCofig.WorldSlash.FinalStrike.GroundImpact.END_WIDTH_SCALE
                );
    }

    public static boolean crossesTarget(
            float previousRadius,
            float currentRadius,
            float waveWidth,
            float targetDistance,
            float targetRadius
    ) {
        float safePrevious = Math.max(0.0f, previousRadius);
        float safeCurrent = Math.max(safePrevious, currentRadius);
        float collisionPadding = Math.max(0.0f, waveWidth) * 0.5f
                + Math.max(0.0f, targetRadius)
                + Math.max(0.0f, SlashCofig.WorldSlash.FinalStrike.GroundImpact.COLLISION_PADDING);
        float innerRadius = Math.max(0.0f, safePrevious - collisionPadding);
        float outerRadius = safeCurrent + collisionPadding;
        float distance = Math.max(0.0f, targetDistance);
        return distance >= innerRadius && distance <= outerRadius;
    }

    public static float damageHealthPoints(float distance, float maximumRadius) {
        float safeMaximumRadius = Math.max(0.001f, maximumRadius);
        float distanceFraction = Mth.clamp(distance / safeMaximumRadius, 0.0f, 1.0f);
        float hearts = Mth.lerp(
                distanceFraction,
                SlashCofig.WorldSlash.FinalStrike.GroundImpact.MAX_DAMAGE_HEARTS,
                SlashCofig.WorldSlash.FinalStrike.GroundImpact.MIN_DAMAGE_HEARTS
        );
        return hearts * 2.0f;
    }

    public static boolean affectsPlayerGameType(GameType gameType) {
        return gameType == GameType.SURVIVAL;
    }
}
