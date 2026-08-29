package org.xiaoyang.ex_enigmaticlegacy.api.shader.slash;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class DomainRange {
    private DomainRange() {}

    public static boolean containsHorizontal(Vec3 position, Vec3 center, float radius) {
        if (position == null || center == null || radius <= 0.0f) return false;

        return horizontalDistanceSqr(position, center) < (double) radius * radius;
    }

    public static double horizontalDistanceSqr(Vec3 position, Vec3 center) {
        if (position == null || center == null) return Double.POSITIVE_INFINITY;

        double deltaX = position.x - center.x;
        double deltaZ = position.z - center.z;
        return deltaX * deltaX + deltaZ * deltaZ;
    }

    public static float horizontalDistance(Vec3 position, Vec3 center) {
        return (float) Math.sqrt(horizontalDistanceSqr(position, center));
    }

    public static float visibility(Vec3 position, Vec3 center, float radius, float edgeFadeFraction) {
        if (position == null || center == null) return 0.0f;

        double deltaX = position.x - center.x;
        double deltaZ = position.z - center.z;
        return visibilityAtDistance((float) Math.sqrt(deltaX * deltaX + deltaZ * deltaZ), radius, edgeFadeFraction);
    }

    public static float visibilityAtDistance(float horizontalDistance, float radius, float edgeFadeFraction) {
        if (radius <= 0.001f) return 0.0f;

        float safeDistance = Math.max(0.0f, horizontalDistance);
        if (safeDistance >= radius) return 0.0f;

        float fadeWidth = radius * Mth.clamp(edgeFadeFraction, 0.0f, 1.0f);
        if (fadeWidth <= 0.001f) return 1.0f;

        float fullVisibilityRadius = radius - fadeWidth;
        if (safeDistance <= fullVisibilityRadius) return 1.0f;

        float visibility = Mth.clamp((radius - safeDistance) / fadeWidth, 0.0f, 1.0f);
        return smooth(visibility);
    }

    public static float entrance(float age, float introTicks) {
        float duration = Math.max(0.001f, introTicks);
        return smooth(Mth.clamp(age / duration, 0.0f, 1.0f));
    }

    public static float activeRadius(float targetRadius, float age, float introTicks) {
        return Math.max(0.0f, targetRadius) * entrance(age, introTicks);
    }

    public static float blendMultiplier(float effectMultiplier, float visibility) {
        return Mth.lerp(Mth.clamp(visibility, 0.0f, 1.0f), 1.0f, Mth.clamp(effectMultiplier, 0.01f, 1.0f));
    }

    private static float smooth(float value) {
        return value * value * (3.0f - 2.0f * value);
    }
}
