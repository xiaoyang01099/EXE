package org.xiaoyang.ex_enigmaticlegacy.api.shader.slash;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public final class AudiencePolicy {
    private static final double MIN_WORLD_BROADCAST_RANGE = 32.0;
    private static final double WORLD_BROADCAST_PADDING = 16.0;

    private AudiencePolicy() {
    }

    public static boolean allowsScreenEffects(UUID ownerId, UUID viewerId) {
        return ownerId != null && ownerId.equals(viewerId);
    }

    public static boolean usesSpatialAudio(boolean allowsScreenEffects) {
        return !allowsScreenEffects;
    }

    public static float worldVisibility(boolean allowsScreenEffects, float domainVisibility) {
        return allowsScreenEffects ? Mth.clamp(domainVisibility, 0.0f, 1.0f) : 1.0f;
    }

    public static double worldEffectBroadcastRange(float slashLength, float domainRadius) {
        return Math.max(
                MIN_WORLD_BROADCAST_RANGE,
                Math.max(0.0f, slashLength) * 0.5 + Math.max(0.0f, domainRadius) + WORLD_BROADCAST_PADDING
        );
    }

    public static boolean isWithinWorldEffectBroadcast(Vec3 viewerPosition, Vec3 effectPosition, double broadcastRange) {
        if (viewerPosition == null || effectPosition == null || broadcastRange <= 0.0) return false;
        return viewerPosition.distanceToSqr(effectPosition) <= broadcastRange * broadcastRange;
    }
}
