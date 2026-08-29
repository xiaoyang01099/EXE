package org.xiaoyang.ex_enigmaticlegacy.api.shader.slash;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class Geometry {
    private static final float PROJECTIVE_GOLDEN_ANGLE = 1.1999816f;
    private static final Vec3 WORLD_UP = new Vec3(0.0, 1.0, 0.0);
    private static final Vec3 DEFAULT_FORWARD = new Vec3(0.0, 0.0, 1.0);

    private Geometry() {}

    public static Placement placement(int index, int count, float radius) {
        return placement(index, count, radius, DEFAULT_FORWARD);
    }

    public static Placement placement(int index, int count, float radius, Vec3 facing) {
        if (isFinalStrike(index, count)) {
            return finalStrikePlacement(radius, facing);
        }

        int pairIndex = index / 2;
        int pairCount = Math.max(1, (count + 1) / 2);
        boolean mirrored = (index & 1) != 0;
        int spatialIndex = spatialPairSlot(pairIndex, pairCount);
        float h0 = hash01(pairIndex * 97 + count * 31 + 11);
        float h2 = hash01(pairIndex * 173 + count * 43 + 89);
        float h3 = hash01(pairIndex * 211 + count * 59 + 7);

        Vec3 shell = sphericalFibonacciAxis(spatialIndex, pairCount);
        if (mirrored) shell = shell.scale(-1.0);

        float radialOrder = radialLayerProgress(pairIndex, pairCount);
        float shellRadius = radius * Mth.lerp(
                radialOrder,
                SlashCofig.WorldSlash.Placement.INNER_RADIUS_SCALE,
                SlashCofig.WorldSlash.Placement.OUTER_RADIUS_SCALE
        );
        Vec3 offset = shell.scale(shellRadius);

        Vec3 tangent = shell.cross(new Vec3(0.0, 1.0, 0.0));
        if (tangent.lengthSqr() < 1.0e-8) tangent = shell.cross(new Vec3(1.0, 0.0, 0.0));
        tangent = tangent.normalize();
        Vec3 bitangent = shell.cross(tangent).normalize();
        int directionFamilies = Math.max(1, SlashCofig.WorldSlash.Placement.DIRECTION_FAMILIES);
        int directionFamily = Math.floorMod(pairIndex, directionFamilies);
        int directionLayer = pairIndex / directionFamilies;
        float cutAngle = directionFamily * (Mth.PI / directionFamilies)
                + directionLayer * SlashCofig.WorldSlash.Placement.DIRECTION_DRIFT_RADIANS
                + (h2 - 0.5f) * SlashCofig.WorldSlash.Placement.DIRECTION_JITTER_RADIANS;
        float radialSkew = (h3 * 2.0f - 1.0f) * SlashCofig.WorldSlash.Placement.RADIAL_SKEW;
        Vec3 direction = tangent.scale(Mth.cos(cutAngle)).add(bitangent.scale(Mth.sin(cutAngle))).add(shell.scale(radialSkew));
        if (direction.lengthSqr() < 1.0e-8) direction = tangent;
        direction = direction.normalize();

        Vec3 normal = shell.subtract(direction.scale(shell.dot(direction)));
        if (normal.lengthSqr() < 1.0e-8) normal = stableNormal(direction);
        normal = normal.normalize();

        int accentInterval = Math.max(1, SlashCofig.WorldSlash.Placement.ACCENT_PAIR_INTERVAL);
        boolean accentPair = pairIndex % accentInterval == 0;
        float innerWeight = 1.0f - radialOrder;
        float lengthScale = Mth.lerp(h0, 0.52f, 0.78f)
                + innerWeight * SlashCofig.WorldSlash.Placement.INNER_LENGTH_BOOST;
        float width = Mth.lerp(h2, 0.16f, 0.22f)
                + innerWeight * SlashCofig.WorldSlash.Placement.INNER_WIDTH_BOOST;
        if (accentPair) {
            lengthScale += SlashCofig.WorldSlash.Placement.ACCENT_LENGTH_BOOST;
            width += SlashCofig.WorldSlash.Placement.ACCENT_WIDTH_BOOST;
        }
        if (mirrored) {
            lengthScale *= SlashCofig.WorldSlash.Placement.MIRROR_SECONDARY_LENGTH_SCALE;
            width *= SlashCofig.WorldSlash.Placement.MIRROR_SECONDARY_WIDTH_SCALE;
        }
        return new Placement(offset, direction, normal, lengthScale, width);
    }

    public static boolean isFinalStrike(int index, int count) {
        int safeCount = Math.max(1, count);
        return index == safeCount - 1;
    }

    public static int slashStartTick(int index, int count) {
        int safeIndex = Math.max(0, index);
        int baseTick = safeIndex * Math.max(1, SlashCofig.Audio.SLASH_INTERVAL_TICKS);
        return isFinalStrike(safeIndex, count)
                ? baseTick + Math.max(0, SlashCofig.WorldSlash.FinalStrike.WINDUP_DELAY_TICKS)
                : baseTick;
    }

    public static int spatialPairSlot(int pairIndex, int pairCount) {
        int safePairCount = Math.max(1, pairCount);
        if (safePairCount == 1) return 0;

        int step = Math.max(1, Math.round(safePairCount * SlashCofig.WorldSlash.Placement.SPATIAL_STEP_RATIO));
        while (greatestCommonDivisor(step, safePairCount) != 1) {
            step++;
        }
        return Math.floorMod(pairIndex * step, safePairCount);
    }

    public static Vec3 sphericalFibonacciAxis(int spatialIndex, int pairCount) {
        int safePairCount = Math.max(1, pairCount);
        int safeIndex = Math.floorMod(spatialIndex, safePairCount);
        float equalAreaY = 1.0f - (safeIndex + 0.5f) / safePairCount;
        float horizontal = Mth.sqrt(Math.max(0.0f, 1.0f - equalAreaY * equalAreaY));
        float angle = safeIndex * PROJECTIVE_GOLDEN_ANGLE + SlashCofig.WorldSlash.Placement.SPHERICAL_PHASE_RADIANS;
        return new Vec3(Mth.cos(angle) * horizontal, equalAreaY, Mth.sin(angle) * horizontal).normalize();
    }

    public static float radialLayerProgress(int pairIndex, int pairCount) {
        int safePairCount = Math.max(1, pairCount);
        if (safePairCount == 1) return 0.0f;

        float linear = Mth.clamp(pairIndex / (float) (safePairCount - 1), 0.0f, 1.0f);
        float power = Math.max(0.01f, SlashCofig.WorldSlash.Placement.RADIAL_CURVE_POWER);
        return (float) Math.pow(linear, power);
    }

    public static boolean sweepReversed(long seed, int index) {
        long value = seed ^ (0x9E3779B97F4A7C15L * (index + 1L));
        value ^= value >>> 33;
        value *= 0xFF51AFD7ED558CCDL;
        value ^= value >>> 33;
        return (value & 1L) == 0L;
    }

    public static boolean sweepReversed(long seed, int index, int count) {
        return !isFinalStrike(index, count) && sweepReversed(seed, index);
    }

    public static float sweepProgress(float effectAge, int startTick) {
        float ticks = Math.max(1.0f, SlashCofig.WorldSlash.SWEEP_TICKS);
        float raw = (effectAge - startTick - SlashCofig.WorldSlash.PRE_CRACK_TICKS) / ticks;
        float clamped = Mth.clamp(raw, 0.0f, 1.0f);
        float inverse = 1.0f - clamped;
        return 1.0f - inverse * inverse * inverse * inverse * inverse;
    }

    public static Segment sweptSegment(Vec3 center, Vec3 direction, float length, boolean reversed, float sweepProgress) {
        Vec3 safeDirection = direction.lengthSqr() < 1.0e-8 ? new Vec3(1.0, 0.0, 0.0) : direction.normalize();
        float safeLength = Math.max(0.1f, length);
        float progress = Mth.clamp(sweepProgress, 0.0f, 1.0f);
        Vec3 fullStart = center.subtract(safeDirection.scale(safeLength * 0.5f));
        Vec3 fullEnd = center.add(safeDirection.scale(safeLength * 0.5f));
        if (reversed) {
            return new Segment(fullEnd.lerp(fullStart, progress), fullEnd);
        }
        return new Segment(fullStart, fullStart.lerp(fullEnd, progress));
    }

    public static boolean touches(AABB bounds, Segment segment, float padding) {
        if (bounds == null || segment == null) return false;
        AABB expanded = bounds.inflate(Math.max(0.0f, padding));
        if (expanded.contains(segment.start()) || expanded.contains(segment.end())) return true;
        return expanded.clip(segment.start(), segment.end()).isPresent();
    }

    public static int slashSequenceDurationTicks(int slashCount) {
        int safeCount = Math.max(1, slashCount);
        return slashStartTick(safeCount - 1, safeCount) + SlashCofig.WorldSlash.LINE_REVEAL_TICKS;
    }

    private static Placement finalStrikePlacement(float radius, Vec3 facing) {
        Vec3 forward = horizontalDirection(facing);
        Vec3 right = forward.cross(WORLD_UP);
        if (right.lengthSqr() < 1.0e-8) right = new Vec3(1.0, 0.0, 0.0);
        right = right.normalize();

        Vec3 direction = right.scale(-SlashCofig.WorldSlash.FinalStrike.LEFT_COMPONENT)
                .add(WORLD_UP.scale(-SlashCofig.WorldSlash.FinalStrike.DOWN_COMPONENT))
                .add(forward.scale(SlashCofig.WorldSlash.FinalStrike.FORWARD_COMPONENT));
        if (direction.lengthSqr() < 1.0e-8) direction = right.scale(-1.0).add(WORLD_UP.scale(-1.0));
        direction = direction.normalize();

        Vec3 normal = forward.subtract(direction.scale(forward.dot(direction)));
        if (normal.lengthSqr() < 1.0e-8) normal = stableNormal(direction);
        normal = normal.normalize();

        float safeRadius = Math.max(0.0f, radius);
        Vec3 offset = WORLD_UP.scale(safeRadius * SlashCofig.WorldSlash.FinalStrike.CENTER_HEIGHT_RADIUS_SCALE)
                .add(forward.scale(safeRadius * SlashCofig.WorldSlash.FinalStrike.CENTER_FORWARD_RADIUS_SCALE));
        return new Placement(
                offset,
                direction,
                normal,
                SlashCofig.WorldSlash.FinalStrike.LENGTH_SCALE,
                SlashCofig.WorldSlash.FinalStrike.WIDTH
        );
    }

    private static Vec3 horizontalDirection(Vec3 facing) {
        if (facing == null) return DEFAULT_FORWARD;
        Vec3 horizontal = new Vec3(facing.x, 0.0, facing.z);
        return horizontal.lengthSqr() < 1.0e-8 ? DEFAULT_FORWARD : horizontal.normalize();
    }

    private static float hash01(int value) {
        int x = value;
        x ^= x << 13;
        x ^= x >>> 17;
        x ^= x << 5;
        return (x & 0x7FFFFFFF) / (float) 0x7FFFFFFF;
    }

    private static int greatestCommonDivisor(int a, int b) {
        int dividend = Math.abs(a);
        int divisor = Math.abs(b);
        while (divisor != 0) {
            int remainder = dividend % divisor;
            dividend = divisor;
            divisor = remainder;
        }
        return Math.max(1, dividend);
    }

    private static Vec3 stableNormal(Vec3 direction) {
        Vec3 raw = Math.abs(direction.y) > 0.88 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
        raw = raw.subtract(direction.scale(raw.dot(direction)));
        if (raw.lengthSqr() < 1.0e-8) raw = pickSide(direction);
        return raw.normalize();
    }

    private static Vec3 pickSide(Vec3 direction) {
        Vec3 up = Math.abs(direction.y) > 0.85 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
        Vec3 side = direction.cross(up);
        if (side.lengthSqr() < 1.0e-8) return new Vec3(1.0, 0.0, 0.0);
        return side.normalize();
    }

    public record Placement(Vec3 offset, Vec3 direction, Vec3 normal, float lengthScale, float width) {}

    public record Segment(Vec3 start, Vec3 end) {}
}
