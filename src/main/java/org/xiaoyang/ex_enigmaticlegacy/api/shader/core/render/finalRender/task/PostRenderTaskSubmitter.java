package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.task;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.PostProcessing;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.bloomQueue.CoinLightningQueue;

public class PostRenderTaskSubmitter {
    public final PostProcessing postProcessing; // 后处理统一 task 提交入口。

    public PostRenderTaskSubmitter(PostProcessing postProcessing) {
        this.postProcessing = postProcessing;
    }

    public void addLightningStartToEnd(Vec3 start, Vec3 end, float lifetime, float width, long seed,
                                       float coreR, float coreG, float coreB,
                                       float bloomR, float bloomG, float bloomB) {
        if (postProcessing == null) return;
        postProcessing.submit(LightningTask.startToEnd(start, end, lifetime, width, seed,
                coreR, coreG, coreB, bloomR, bloomG, bloomB));
    }

    public void addLightningPath(Vec3 start, Vec3 end, float growTime, float holdTime, float fadeTime,
                                 float width, long seed,
                                 float coreR, float coreG, float coreB,
                                 float bloomR, float bloomG, float bloomB) {
        if (postProcessing == null) return;
        postProcessing.submit(LightningTask.path(start, end, growTime, holdTime, fadeTime, width, seed,
                coreR, coreG, coreB, bloomR, bloomG, bloomB));
    }

    public void addLightningPath(Vec3 start, Vec3 end, float growTime, float holdTime, float fadeTime,
                                 float width, long seed,
                                 float coreR, float coreG, float coreB,
                                 float bloomR, float bloomG, float bloomB, float jitterScale) {
        if (postProcessing == null) return;
        postProcessing.submit(LightningTask.path(start, end, growTime, holdTime, fadeTime, width, seed,
                coreR, coreG, coreB, bloomR, bloomG, bloomB, jitterScale));
    }

    public void addLightningPath(Vec3 start, Vec3 end, float growTime, float holdTime, float fadeTime,
                                 float width, long seed,
                                 float coreR, float coreG, float coreB,
                                 float bloomR, float bloomG, float bloomB, float jitterScale, int terminalBounceCount) {
        if (postProcessing == null) return;
        postProcessing.submit(LightningTask.path(start, end, growTime, holdTime, fadeTime, width, seed,
                coreR, coreG, coreB, bloomR, bloomG, bloomB, jitterScale, terminalBounceCount));
    }

    public void addLightningPath(Vec3 start, Vec3 end, float growTime, float holdTime, float fadeTime,
                                 float width, long seed,
                                 float coreR, float coreG, float coreB,
                                 float bloomR, float bloomG, float bloomB, float jitterScale, int terminalBounceCount,
                                 float noiseIndex, float noiseStrength) {
        if (postProcessing == null) return;
        postProcessing.submit(LightningTask.path(start, end, growTime, holdTime, fadeTime, width, seed,
                coreR, coreG, coreB, bloomR, bloomG, bloomB, jitterScale, terminalBounceCount, noiseIndex, noiseStrength));
    }

    public void addLightningPath(Vec3 start, Vec3 end, float growTime, float holdTime, float fadeTime,
                                 float width, long seed,
                                 float coreR, float coreG, float coreB,
                                 float bloomR, float bloomG, float bloomB, float jitterScale, int terminalBounceCount,
                                 float noiseIndex, float noiseStrength, float startDelay) {
        if (postProcessing == null) return;
        postProcessing.submit(LightningTask.path(start, end, growTime, holdTime, fadeTime, width, seed,
                coreR, coreG, coreB, bloomR, bloomG, bloomB, jitterScale, terminalBounceCount,
                noiseIndex, noiseStrength, startDelay));
    }

    public void addPersistentLightningStartToEnd(Vec3 start, Vec3 end, float lifetime, float width, long seed,
                                                 float coreR, float coreG, float coreB,
                                                 float bloomR, float bloomG, float bloomB) {
        if (postProcessing == null) return;
        float safeLifetime = Math.max(CoinLightningQueue.MIN_TIME * 3.0F, lifetime);
        postProcessing.submit(LightningTask.burst(start, end, safeLifetime * 0.15F, safeLifetime * 0.65F,
                safeLifetime * 0.20F, width, seed, coreR, coreG, coreB, bloomR, bloomG, bloomB));
    }

    public void addLightningBurst(Vec3 start, Vec3 end, float growTime, float holdTime, float fadeTime,
                                  float width, long seed,
                                  float coreR, float coreG, float coreB,
                                  float bloomR, float bloomG, float bloomB) {
        if (postProcessing == null) return;
        postProcessing.submit(LightningTask.burst(start, end, growTime, holdTime, fadeTime, width, seed,
                coreR, coreG, coreB, bloomR, bloomG, bloomB));
    }

    public void addLightningRing(Vec3 center, Vec3 normal, float startRadius, float endRadius,
                                 float growTime, float holdTime, float fadeTime, float width, long seed,
                                 float coreR, float coreG, float coreB,
                                 float bloomR, float bloomG, float bloomB) {
        if (postProcessing == null) return;
        postProcessing.submit(LightningTask.ring(center, normal, startRadius, endRadius, growTime, holdTime,
                fadeTime, width, seed, coreR, coreG, coreB, bloomR, bloomG, bloomB));
    }

    public void addShockwave(Vec3 center, Vec3 normal, float startRadius, float endRadius,
                             float growTime, float holdTime, float fadeTime, float width, long seed, float alpha) {
        if (postProcessing == null) return;
        postProcessing.submit(ShockwaveTask.shockwave(center, normal, startRadius, endRadius,
                growTime, holdTime, fadeTime, width, seed, alpha));
    }

    public void addCircleShockwave(Vec3 center, Vec3 normal, float startRadius, float endRadius,
                                   float growTime, float holdTime, float fadeTime, float width, long seed, float alpha) {
        if (postProcessing == null) return;
        postProcessing.submit(ShockwaveTask.circleShockwave(center, normal, startRadius, endRadius,
                growTime, holdTime, fadeTime, width, seed, alpha));
    }

    public void addSmokeRing(Vec3 center, Vec3 normal, long seed) {
        if (postProcessing == null) return;
        postProcessing.submit(SmokeParticleTask.ring(center, normal, seed));
    }

    public void addSmokeParticle(Vec3 center, long seed) {
        if (postProcessing == null) return;
        postProcessing.submit(SmokeParticleTask.single(center, seed));
    }

    public void addSmokeCloud(Vec3 center, long seed) {
        if (postProcessing == null) return;
        postProcessing.submit(SmokeParticleTask.cloud(center, seed));
    }

    public void addSmokeCloudRing(Vec3 center, long seed) {
        if (postProcessing == null) return;
        postProcessing.submit(SmokeParticleTask.cloudRing(center, seed));
    }

    public void addSmokeCloudRing(Vec3 center, long seed, float ringRotation) {
        if (postProcessing == null) return;
        postProcessing.submit(SmokeParticleTask.cloudRing(center, seed, ringRotation));
    }

    public void addHeavenlyThunderCloudRing(Vec3 center, long seed, float lifeTime) {
        if (postProcessing == null) return;
        postProcessing.submit(SmokeParticleTask.heavenlyThunderCloudRing(center, seed, lifeTime));
    }

    public void addHeavenlyThunderCloudRing(Vec3 center, long seed, float lifeTime, float ringRotation) {
        if (postProcessing == null) return;
        postProcessing.submit(SmokeParticleTask.heavenlyThunderCloudRing(center, seed, lifeTime, ringRotation));
    }

    public void addChargingLightning(Player player, float chargeProgress, float partialTick, boolean colorful) {
        if (postProcessing == null) return;
        postProcessing.submit(LightningTask.charging(player, chargeProgress, partialTick, colorful));
    }

    public void addGoldenSpiralEffect(Vec3 center, long seed) {
        if (postProcessing == null) return;
        postProcessing.submit(GoldenSpiralEffectTask.create(center, seed));
    }
}
