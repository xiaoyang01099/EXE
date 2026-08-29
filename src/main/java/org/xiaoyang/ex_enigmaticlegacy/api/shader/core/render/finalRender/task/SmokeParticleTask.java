package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.task;

import net.minecraft.world.phys.Vec3;

public class SmokeParticleTask implements PostRenderTask {
    public enum EffectType {
        RING,
        SINGLE,
        CLOUD,
        CLOUD_RING,
        HEAVENLY_THUNDER_CLOUD_RING
    }

    public final EffectType effectType;
    public final Vec3 center;
    public final Vec3 normal;
    public final long seed;
    public final float lifeTime;
    public final float ringRotation;

    public SmokeParticleTask(EffectType effectType, Vec3 center, Vec3 normal, long seed, float lifeTime, float ringRotation) {
        this.effectType = effectType;
        this.center = center;
        this.normal = normal;
        this.seed = seed;
        this.lifeTime = lifeTime;
        this.ringRotation = ringRotation;
    }

    public PostRenderQueueType queueType() {
        return PostRenderQueueType.SMOKE_PARTICLE;
    }

    public static SmokeParticleTask ring(Vec3 center, Vec3 normal, long seed) {
        return new SmokeParticleTask(EffectType.RING, center, normal, seed, 0.0F, 0.0F);
    }

    public static SmokeParticleTask single(Vec3 center, long seed) {
        return new SmokeParticleTask(EffectType.SINGLE, center, null, seed, 0.0F, 0.0F);
    }

    public static SmokeParticleTask cloud(Vec3 center, long seed) {
        return new SmokeParticleTask(EffectType.CLOUD, center, null, seed, 0.0F, 0.0F);
    }

    public static SmokeParticleTask cloudRing(Vec3 center, long seed) {
        return cloudRing(center, seed, 0.0F);
    }

    public static SmokeParticleTask cloudRing(Vec3 center, long seed, float ringRotation) {
        return new SmokeParticleTask(EffectType.CLOUD_RING, center, null, seed, 0.0F, ringRotation);
    }

    public static SmokeParticleTask heavenlyThunderCloudRing(Vec3 center, long seed, float lifeTime) {
        return heavenlyThunderCloudRing(center, seed, lifeTime, 0.0F);
    }

    public static SmokeParticleTask heavenlyThunderCloudRing(Vec3 center, long seed, float lifeTime, float ringRotation) {
        return new SmokeParticleTask(EffectType.HEAVENLY_THUNDER_CLOUD_RING, center, null, seed, lifeTime, ringRotation);
    }
}
