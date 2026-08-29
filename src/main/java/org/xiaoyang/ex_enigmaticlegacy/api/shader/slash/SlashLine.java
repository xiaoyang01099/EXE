package org.xiaoyang.ex_enigmaticlegacy.api.shader.slash;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class SlashLine {
    private static final int DEFAULT_OUTER_COLOR = SlashCofig.WorldSlash.OUTER_COLOR_PRIMARY;
    private static final int DEFAULT_CORE_COLOR = SlashCofig.WorldSlash.CORE_COLOR_ALT;
    private final Vec3 center;
    private final Vec3 direction;
    private final Vec3 normal;
    private final float length;
    private final float width;
    private final int outerColor;
    private final int coreColor;
    private final int startTick;
    private final int revealTicks;
    private final boolean sweepReversed;
    private final boolean finalStrike;
    private boolean sweepStartSoundPlayed;
    private boolean completionEffectsSpawned;

    public SlashLine(Vec3 center, Vec3 direction, Vec3 normal, float length, float width, int startTick, int revealTicks) {
        this(center, direction, normal, length, width, DEFAULT_OUTER_COLOR, DEFAULT_CORE_COLOR, startTick, revealTicks);
    }

    public SlashLine(Vec3 center, Vec3 direction, Vec3 normal, float length, float width, int outerColor, int coreColor, int startTick, int revealTicks) {
        this(center, direction, normal, length, width, outerColor, coreColor, startTick, revealTicks, false);
    }

    public SlashLine(Vec3 center, Vec3 direction, Vec3 normal, float length, float width, int outerColor, int coreColor, int startTick, int revealTicks, boolean sweepReversed) {
        this(center, direction, normal, length, width, outerColor, coreColor, startTick, revealTicks, sweepReversed, false);
    }

    public SlashLine(Vec3 center, Vec3 direction, Vec3 normal, float length, float width, int outerColor, int coreColor, int startTick, int revealTicks, boolean sweepReversed, boolean finalStrike) {
        this.center = center;
        this.direction = safeNormalize(direction, new Vec3(1.0, 0.0, 0.0));
        this.normal = safeNormalize(normal, new Vec3(0.0, 1.0, 0.0));
        this.length = Math.max(0.1f, length);
        this.width = Math.max(0.02f, width);
        this.outerColor = outerColor;
        this.coreColor = coreColor;
        this.startTick = Math.max(0, startTick);
        this.revealTicks = Math.max(1, revealTicks);
        this.sweepReversed = sweepReversed;
        this.finalStrike = finalStrike;
    }

    public Vec3 center() {
        return center;
    }

    public Vec3 direction() {
        return direction;
    }

    public Vec3 normal() {
        return normal;
    }

    public float length() {
        return length;
    }

    public float width() {
        return width;
    }

    public int outerColor() {
        return outerColor;
    }

    public int coreColor() {
        return coreColor;
    }

    public int startTick() {
        return startTick;
    }

    public int revealTicks() {
        return revealTicks;
    }

    public boolean isFinalStrike() {
        return finalStrike;
    }

    public boolean isFullyRevealed(float age) {
        return revealProgress(age) >= 1.0f;
    }

    public boolean consumeCompletionEffect(float age) {
        if (completionEffectsSpawned || !isFullyRevealed(age)) return false;
        completionEffectsSpawned = true;
        return true;
    }

    public boolean consumeSweepStartSound(float age) {
        int sweepStartTick = startTick + SlashCofig.WorldSlash.PRE_CRACK_TICKS;
        if (sweepStartSoundPlayed || age < sweepStartTick) return false;
        sweepStartSoundPlayed = true;
        return true;
    }

    public float revealProgress(float age) {
        float raw = (age - startTick) / revealTicks;
        return smooth(Mth.clamp(raw, 0.0f, 1.0f));
    }

    public float preCrackProgress(float age) {
        float ticks = Math.max(1.0f, SlashCofig.WorldSlash.PRE_CRACK_TICKS);
        return smooth(Mth.clamp(phaseAge(age) / ticks, 0.0f, 1.0f));
    }

    public float sweepProgress(float age) {
        return Geometry.sweepProgress(age, startTick);
    }

    public float settleProgress(float age) {
        float ticks = Math.max(1.0f, SlashCofig.WorldSlash.SETTLE_TICKS);
        float raw = (phaseAge(age) - SlashCofig.WorldSlash.PRE_CRACK_TICKS - SlashCofig.WorldSlash.SWEEP_TICKS) / ticks;
        return smooth(Mth.clamp(raw, 0.0f, 1.0f));
    }

    public Vec3 start() {
        return center.subtract(direction.scale(length * 0.5f));
    }

    public Vec3 end() {
        return center.add(direction.scale(length * 0.5f));
    }

    public Vec3 sweptStart(float age) {
        if (sweepReversed) return end().lerp(start(), sweepProgress(age));
        return start();
    }

    public Vec3 sweptEnd(float age) {
        if (sweepReversed) return end();
        return start().lerp(end(), sweepProgress(age));
    }

    public Vec3 sweepHead(float age) {
        return sweepReversed ? sweptStart(age) : sweptEnd(age);
    }

    public Vec3 sweepHeadTrail(float age, float lengthFraction) {
        float progress = sweepProgress(age);
        float trail = Mth.clamp(lengthFraction, 0.0f, 1.0f);
        if (sweepReversed) {
            float t = Mth.clamp(1.0f - progress + trail, 0.0f, 1.0f);
            return start().lerp(end(), t);
        }
        float t = Mth.clamp(progress - trail, 0.0f, 1.0f);
        return start().lerp(end(), t);
    }

    private float phaseAge(float age) {
        return Math.max(0.0f, age - startTick);
    }

    private static float smooth(float x) {
        return x * x * (3.0f - 2.0f * x);
    }

    private static Vec3 safeNormalize(Vec3 value, Vec3 fallback) {
        if (value.lengthSqr() < 1.0e-8) return fallback;
        return value.normalize();
    }
}
