package org.xiaoyang.ex_enigmaticlegacy.Client.particle;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

final class TrueDemonWeaponParticleVisuals {
    private static final float BASE_RED = 1.0F;
    private static final float BASE_GREEN = 45.0F / 255.0F;
    private static final float BASE_BLUE = 242.0F / 255.0F;
    private static final float PULSE_SPEED_MIN = 0.38F;
    private static final float PULSE_SPEED_RANDOM = 0.22F;
    private static final float SCALE_BASE = 0.62F;
    private static final float SCALE_PULSE = 0.50F;
    private static final float SCALE_FLICKER = 0.18F;

    static Profile create(RandomSource random, float sizeScale) {
        return create(random, sizeScale, 36, 21, 0.018F, 0.018F, 1.0F);
    }

    static Profile createCorrosion(RandomSource random, float sizeScale) {
        return create(random, sizeScale, 12, 9, 0.045F, 0.035F, 1.25F);
    }

    private static Profile create(RandomSource random, float sizeScale, int lifetimeBase, int lifetimeRandom,
                                  float sizeBase, float sizeRandom, float alphaScale) {
        int lifetime = Math.max(1, lifetimeBase) + random.nextInt(Math.max(1, lifetimeRandom));
        float baseSize = (sizeBase + random.nextFloat() * sizeRandom) * Mth.clamp(sizeScale, 0.10F, 2.0F);
        float pulsePhase = random.nextFloat() * Mth.TWO_PI;
        float pulseSpeed = PULSE_SPEED_MIN + random.nextFloat() * PULSE_SPEED_RANDOM;
        float flickerPhase = random.nextFloat() * Mth.TWO_PI;
        float colorBias = random.nextFloat();
        return new Profile(lifetime, baseSize, pulsePhase, pulseSpeed, flickerPhase, colorBias, alphaScale);
    }

    record Profile(int lifetime, float baseSize, float pulsePhase, float pulseSpeed,
                   float flickerPhase, float colorBias, float alphaScale) {
        Sample sample(float age) {
            float clampedAge = Mth.clamp(age, 0.0F, Math.max(1.0F, lifetime));
            float progress = clampedAge / Math.max(1.0F, lifetime);
            float pulse = 0.5F + 0.5F * Mth.sin(clampedAge * pulseSpeed + pulsePhase);
            float flicker = 0.5F + 0.5F * Mth.sin(clampedAge * 0.21F + flickerPhase);

            float sine = Mth.sin(Mth.PI * Mth.clamp(progress, 0.0F, 1.0F));
            float scaleEnvelope = sine * sine;
            float size = baseSize * scaleEnvelope * (SCALE_BASE + pulse * SCALE_PULSE + flicker * SCALE_FLICKER);

            float brightness = Mth.clamp(0.52F + pulse * 0.36F + flicker * 0.12F, 0.52F, 1.0F);
            float fadeIn = smootherstep(0.0F, 0.22F, progress);
            float fadeOut = 1.0F - smootherstep(0.52F, 1.0F, progress);
            float alpha = Mth.clamp(fadeIn * fadeOut * (0.26F + brightness * 0.46F) * alphaScale, 0.0F, 0.90F);

            float red = Mth.clamp(BASE_RED * (0.72F + brightness * 0.28F), 0.0F, 1.0F);
            float green = Mth.clamp(BASE_GREEN * (0.55F + pulse * 1.10F) + colorBias * 0.030F, 0.0F, 0.32F);
            float blue = Mth.clamp(BASE_BLUE * (0.70F + brightness * 0.36F) + colorBias * 0.060F, 0.0F, 1.0F);
            return new Sample(size, alpha, red, green, blue, brightness, pulse, flicker);
        }
    }

    record Sample(float size, float alpha, float red, float green, float blue,
                  float brightness, float pulse, float flicker) {
        boolean visible() {
            return size > 1.0e-5F && alpha > 1.0e-4F;
        }
    }

    private static float smootherstep(float edge0, float edge1, float value) {
        float t = Mth.clamp((value - edge0) / Math.max(1.0e-6F, edge1 - edge0), 0.0F, 1.0F);
        return t * t * t * (t * (t * 6.0F - 15.0F) + 10.0F);
    }

    private TrueDemonWeaponParticleVisuals() {
    }
}
