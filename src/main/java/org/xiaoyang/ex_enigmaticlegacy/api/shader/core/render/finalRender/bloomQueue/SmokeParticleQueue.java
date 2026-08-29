package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.bloomQueue;

import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.xiaoyang.ex_enigmaticlegacy.Util.MathUtil;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.shader.SmokeParticleShader;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class SmokeParticleQueue {
    public static final int SMOKE_TEXTURE_COLUMNS = 8; // smoke.png 序列帧列数。
    public static final int SMOKE_TEXTURE_ROWS = 8; // smoke.png 序列帧行数。
    public static final int SMOKE_TEXTURE_PLAYABLE_FRAMES = 44; // 跳过更多末尾强消散帧后的循环帧数。
    public static final float TEST_SMOKE_RING_DURATION = 5.0F; // 测试烟雾环持续时间。
    public static final float TEST_SMOKE_SINGLE_DURATION = 6.0F; // 单个测试烟雾粒子持续时间。
    public static final float TEST_SMOKE_CLOUD_DURATION = 8.0F; // 测试云团烟雾持续时间。
    public static final float INNER_BLOOM_SCALE = 0.45F; // 内层白蓝烟雾 bloom 强度。
    public static final float MIDDLE_BLOOM_SCALE = 0.25F; // 中层蓝色烟雾 bloom 强度。
    public static final float OUTER_BLOOM_SCALE = 0.04F; // 外围灰烟 bloom 强度。
    public static final long SMOKE_RING_RANDOM_SALT = 0x5A10C0DEL; // 烟雾环随机扰动盐值。
    public static final float HEAVENLY_LIGHT_INNER_RADIUS = 6.0F; // 天雷云环内部光源保持高亮的半径。
    public static final float HEAVENLY_LIGHT_OUTER_RADIUS = 52.0F; // 天雷云环内部光源完全衰减的半径。
    public static final int HEAVENLY_SPIRAL_ARM_COUNT = 3; // 天雷云使用三条螺旋臂，兼顾旋涡辨识度和整体密度。
    public static final float HEAVENLY_SPIRAL_TURNS = 1.35F; // 每条云臂从内到外旋转的圈数。
    public static final float HEAVENLY_SPIRAL_INNER_RADIUS = 5.0F; // 螺旋云中心空洞半径。
    public static final float HEAVENLY_SPIRAL_OUTER_RADIUS = 58.0F; // 螺旋云主体最外半径。
    public static final float HEAVENLY_SPIRAL_ORBIT_SPEED = 0.40F; // 螺旋云整体旋转角速度。
    public static final float HEAVENLY_SPIRAL_HUB_INNER_RADIUS = 0.8F; // 中心旋涡最小半径。
    public static final float HEAVENLY_SPIRAL_HUB_OUTER_RADIUS = 8.5F; // 中心旋涡外半径，和主云臂起点重叠。
    public static final float HEAVENLY_SPIRAL_HUB_TURNS = 1.8F; // 中心短螺旋从内到外旋转圈数。
    public static final float HEAVENLY_SPIRAL_ATTACH_RADIUS = 4.5F; // 主螺旋连接段起始半径。
    public static final float HEAVENLY_SPIRAL_CONNECT_END_T = 0.18F; // 主螺旋连接段结束进度。
    public static final float HEAVENLY_SPIRAL_CONNECT_CURVE = 0.35F; // 主螺旋连接段沿中心旋涡切线弯曲角度。
    public final List<SmokeParticleData> pendingParticles = new ArrayList<>();
    public final List<SmokeParticleData> activeParticles = new ArrayList<>();
    public final SmokeParticleInstancedRenderer renderer = new SmokeParticleInstancedRenderer();

    public void addRing(Vec3 center, Vec3 normal, long seed) {
        if (center == null) return;
        Vec3 safeNormal = safeNormalize(horizontalNormal(normal), new Vec3(0.0D, 0.0D, 1.0D));
        Vec3 ringRight = safeNormalize(new Vec3(safeNormal.z, 0.0D, -safeNormal.x), new Vec3(1.0D, 0.0D, 0.0D));
        Vec3 ringUp = new Vec3(0.0D, 1.0D, 0.0D);
        RandomSource random = RandomSource.create(seed ^ SMOKE_RING_RANDOM_SALT);
        addRingBand(center, ringRight, ringUp, random, 28, 1.45F, 0.18F, 0.75F,
                1.25F, 0.92F, 0.96F, 1.0F, 0.88F, 0.95F, 1.0F, INNER_BLOOM_SCALE);
        addRingBand(center, ringRight, ringUp, random, 46, 1.95F, 0.25F, 1.05F,
                0.82F, 0.28F, 0.72F, 1.0F, 0.20F, 0.82F, 1.0F, MIDDLE_BLOOM_SCALE);
        addRingBand(center, ringRight, ringUp, random, 72, 2.75F, 0.42F, 1.45F,
                0.42F, 0.64F, 0.68F, 0.74F, 0.40F, 0.56F, 0.78F, OUTER_BLOOM_SCALE);
    }

    public void addSingleParticle(Vec3 center, long seed) {
        if (center == null) return;
        RandomSource random = RandomSource.create(seed ^ SMOKE_RING_RANDOM_SALT);
        float spawnTime = MathUtil.getClientTime(0.0F);
        pendingParticles.add(new SmokeParticleData(center, 1.45F,
                0.72F, 0.82F, 0.86F, 0.78F,
                0.32F, 0.52F, 0.70F, 0.08F,
                spawnTime, TEST_SMOKE_SINGLE_DURATION,
                random.nextFloat() * SMOKE_TEXTURE_PLAYABLE_FRAMES, 14.0F,
                random.nextFloat() * (float) (Math.PI * 2.0D), 0.18F,
                random.nextFloat()));
    }

    public void addCloud(Vec3 center, long seed) {
        if (center == null) return;
        RandomSource random = RandomSource.create(seed ^ SMOKE_RING_RANDOM_SALT ^ 0xC10D5EEDL);
        float spawnTime = MathUtil.getClientTime(0.0F);
        addCloudLayer(center, random, spawnTime, 42, 0.95F, 0.35F, 1.35F, 1.85F,
                1F, 1F, 1F, 1F);
    }

    public void addCloudRing(Vec3 center, long seed, float ringRotation) {
        if (center == null) return;
        RandomSource random = RandomSource.create(seed ^ SMOKE_RING_RANDOM_SALT ^ 0xC10D51A6L);
        float spawnTime = MathUtil.getClientTime(0.0F);
        addCloudRingLayer(center, random, spawnTime, 128,
                1.05F, 3.10F, 0.32F,
                1.15F, 2.45F, ringRotation);
    }

    public void addHeavenlyThunderCloudRing(Vec3 center, long seed, float lifeTime, float ringRotation) {
        if (center == null) return;
        RandomSource random = RandomSource.create(seed ^ SMOKE_RING_RANDOM_SALT ^ 0x71A7C10DL);
        float spawnTime = MathUtil.getClientTime(0.0F);
        float safeLifeTime = Math.max(0.1F, lifeTime);
        addHeavenlySpiralHubLayer(center, random, spawnTime, safeLifeTime, 220, ringRotation);
        addHeavenlySpiralBaseLayer(center, random, spawnTime, safeLifeTime, 300, ringRotation);
        addHeavenlySpiralBridgeLayer(center, random, spawnTime, safeLifeTime, 600, ringRotation);
        addHeavenlySpiralContinuousLayer(center, random, spawnTime, safeLifeTime, 1320, ringRotation);
        addHeavenlySpiralEdgeLayer(center, random, spawnTime, safeLifeTime, 360, ringRotation);
    }

    public void addHeavenlySpiralHubLayer(Vec3 center, RandomSource random, float spawnTime, float lifeTime,
                                          int count, float ringRotation) {
        int pointsPerArm = Math.max(1, (count + HEAVENLY_SPIRAL_ARM_COUNT - 1) / HEAVENLY_SPIRAL_ARM_COUNT);
        for (int i = 0; i < count; i++) {
            int armIndex = i % HEAVENLY_SPIRAL_ARM_COUNT;
            int pointIndex = i / HEAVENLY_SPIRAL_ARM_COUNT;
            float hubT = Mth.clamp((pointIndex + random.nextFloat()) / pointsPerArm, 0.0F, 1.0F);
            float armSeparation = smoothRange(hubT, 0.12F, 0.92F);
            double armOffset = armIndex * Math.PI * 2.0D / HEAVENLY_SPIRAL_ARM_COUNT * armSeparation;
            double attachAngle = ringRotation + armOffset - HEAVENLY_SPIRAL_CONNECT_CURVE;
            double angle = attachAngle
                    - (1.0F - hubT) * HEAVENLY_SPIRAL_HUB_TURNS * Math.PI * 2.0D
                    + (random.nextDouble() * 2.0D - 1.0D) * Mth.lerp(hubT, 0.20F, 0.10F);
            float baseRadius = Mth.lerp(hubT, HEAVENLY_SPIRAL_HUB_INNER_RADIUS, HEAVENLY_SPIRAL_HUB_OUTER_RADIUS);
            float radius = Math.max(0.2F, baseRadius + (random.nextFloat() * 2.0F - 1.0F) * Mth.lerp(hubT, 0.9F, 2.1F));
            double y = (random.nextDouble() * 2.0D - 1.0D) * Mth.lerp(hubT, 0.8F, 2.6F);
            float baseColorT = Mth.lerp(hubT, 0.05F, 0.28F);
            float colorT = heavenlyCloudColorT(baseColorT, armIndex, hubT, (float) ringRotation,
                    (random.nextFloat() - 0.5F) * 0.025F);
            Vector3f visibleColor = sampleHeavenlyCloudColor(colorT);
            Vector3f bloomColor = sampleHeavenlyBloomColor(colorT);
            float alpha = Mth.clamp(Mth.lerp(hubT, 0.20F, 0.44F)
                    * (0.84F + random.nextFloat() * 0.24F), 0.14F, 0.50F);
            float size = Mth.lerp(random.nextFloat(), 5.5F, 11.5F);
            float bloomScale = Mth.lerp(hubT, 0.11F, 0.04F);
            float orbitSpeed = HEAVENLY_SPIRAL_ORBIT_SPEED * (0.96F + random.nextFloat() * 0.08F);
            float radialSpeed = random.nextFloat() * 0.03F;
            float verticalSpeed = (random.nextFloat() - 0.5F) * 0.05F;
            float internalLight = calculateHeavenlyInternalLight(radius);

            pendingParticles.add(new SmokeParticleData(
                    center.add(Math.cos(angle) * radius, y, Math.sin(angle) * radius),
                    size, visibleColor.x(), visibleColor.y(), visibleColor.z(), alpha,
                    bloomColor.x(), bloomColor.y(), bloomColor.z(), bloomScale,
                    spawnTime, lifeTime,
                    random.nextFloat() * SMOKE_TEXTURE_PLAYABLE_FRAMES, 5.0F + random.nextFloat() * 3.0F,
                    random.nextFloat() * (float) (Math.PI * 2.0D),
                    (0.12F + random.nextFloat() * 0.18F) * (random.nextBoolean() ? 1.0F : -1.0F),
                    random.nextFloat(), internalLight,
                    Vec3.ZERO, center, radius, (float) angle, orbitSpeed, radialSpeed, verticalSpeed));
        }
    }

    public void addHeavenlySpiralBridgeLayer(Vec3 center, RandomSource random, float spawnTime, float lifeTime,
                                             int count, float ringRotation) {
        int pointsPerArm = Math.max(1, (count + HEAVENLY_SPIRAL_ARM_COUNT - 1) / HEAVENLY_SPIRAL_ARM_COUNT);
        float[] armSeedPhases = new float[HEAVENLY_SPIRAL_ARM_COUNT];
        for (int armIndex = 0; armIndex < HEAVENLY_SPIRAL_ARM_COUNT; armIndex++) {
            armSeedPhases[armIndex] = random.nextFloat() * (float) (Math.PI * 2.0D);
        }

        for (int i = 0; i < count; i++) {
            int armIndex = i % HEAVENLY_SPIRAL_ARM_COUNT;
            int pointIndex = i / HEAVENLY_SPIRAL_ARM_COUNT;
            float baseT = pointIndex / (float) Math.max(pointsPerArm - 1, 1);
            float spiralT = Mth.clamp(baseT + (random.nextFloat() - 0.5F) * 0.35F / pointsPerArm,
                    0.0F, 1.0F);
            float densityWave = heavenlyDensityWave(armIndex, spiralT, armSeedPhases[armIndex]);
            float baseRadius = heavenlyConnectedSpiralRadius(spiralT);
            float radialSpread = Mth.lerp(spiralT, 1.5F, 4.5F) * Mth.lerp(densityWave, 0.82F, 1.12F);
            float radius = Math.max(HEAVENLY_SPIRAL_HUB_INNER_RADIUS,
                    baseRadius + (random.nextFloat() * 2.0F - 1.0F) * radialSpread);
            double angle = heavenlyConnectedSpiralAngle(armIndex, spiralT, ringRotation)
                    + (random.nextDouble() * 2.0D - 1.0D) * Mth.lerp(spiralT, 0.025F, 0.065F);
            double y = (random.nextDouble() * 2.0D - 1.0D)
                    * Mth.lerp(spiralT, 0.8F, 2.8F) * Mth.lerp(densityWave, 0.88F, 1.12F);
            Vec3 pos = center.add(Math.cos(angle) * radius, y, Math.sin(angle) * radius);

            float globalRadiusT = heavenlyGlobalRadiusT(radius);
            float colorT = heavenlyCloudColorT(globalRadiusT, armIndex, spiralT, armSeedPhases[armIndex],
                    (random.nextFloat() - 0.5F) * 0.036F);
            Vector3f visibleColor = sampleHeavenlyCloudColor(colorT);
            Vector3f bloomColor = sampleHeavenlyBloomColor(colorT);
            float startFade = Mth.lerp(smoothRange(spiralT, 0.0F, 0.08F), 0.58F, 1.0F);
            float outerFade = 1.0F - smoothRange(spiralT, 0.82F, 1.0F) * 0.42F;
            float alpha = Mth.clamp((0.18F + densityWave * 0.20F) * startFade * outerFade
                    * (0.90F + random.nextFloat() * 0.20F), 0.08F, 0.42F);
            float size = Mth.lerp(random.nextFloat(), 14.0F, 23.0F) * Mth.lerp(densityWave, 0.92F, 1.08F);
            float bloomScale = Mth.lerp(globalRadiusT, 0.10F, 0.018F);
            float orbitSpeed = HEAVENLY_SPIRAL_ORBIT_SPEED * (0.97F + random.nextFloat() * 0.06F);
            float radialSpeed = Mth.lerp(spiralT, 0.01F, 0.16F);
            float verticalSpeed = (random.nextFloat() - 0.5F) * Mth.lerp(spiralT, 0.04F, 0.14F);
            float internalLight = calculateHeavenlyInternalLight(radius);

            pendingParticles.add(new SmokeParticleData(pos, size,
                    visibleColor.x(), visibleColor.y(), visibleColor.z(), alpha,
                    bloomColor.x(), bloomColor.y(), bloomColor.z(), bloomScale,
                    spawnTime, lifeTime,
                    random.nextFloat() * SMOKE_TEXTURE_PLAYABLE_FRAMES, 5.5F + random.nextFloat() * 2.5F,
                    random.nextFloat() * (float) (Math.PI * 2.0D),
                    (0.10F + random.nextFloat() * 0.14F) * (random.nextBoolean() ? 1.0F : -1.0F),
                    random.nextFloat(), internalLight,
                    Vec3.ZERO, center, radius, (float) angle, orbitSpeed, radialSpeed, verticalSpeed));
        }
    }

    public void addHeavenlySpiralContinuousLayer(Vec3 center, RandomSource random, float spawnTime, float lifeTime,
                                                 int count, float ringRotation) {
        int pointsPerArm = Math.max(1, (count + HEAVENLY_SPIRAL_ARM_COUNT - 1) / HEAVENLY_SPIRAL_ARM_COUNT);
        float[] armSeedPhases = new float[HEAVENLY_SPIRAL_ARM_COUNT];
        float[] armRotations = new float[HEAVENLY_SPIRAL_ARM_COUNT];
        float[] armFrameOffsets = new float[HEAVENLY_SPIRAL_ARM_COUNT];
        for (int armIndex = 0; armIndex < HEAVENLY_SPIRAL_ARM_COUNT; armIndex++) {
            armSeedPhases[armIndex] = random.nextFloat() * (float) (Math.PI * 2.0D);
            armRotations[armIndex] = random.nextFloat() * (float) (Math.PI * 2.0D);
            armFrameOffsets[armIndex] = random.nextFloat() * SMOKE_TEXTURE_PLAYABLE_FRAMES;
        }

        for (int i = 0; i < count; i++) {
            int armIndex = i % HEAVENLY_SPIRAL_ARM_COUNT;
            int pointIndex = i / HEAVENLY_SPIRAL_ARM_COUNT;
            float baseT = pointIndex / (float) Math.max(pointsPerArm - 1, 1);
            float spiralT = Mth.clamp(baseT + (random.nextFloat() - 0.5F) * 0.90F / pointsPerArm,
                    0.0F, 1.0F);
            float densityWave = heavenlyDensityWave(armIndex, spiralT, armSeedPhases[armIndex]);
            Vec3 localPoint = heavenlySpiralLocalPoint(armIndex, spiralT, ringRotation);
            Vec3 nextLocalPoint = heavenlySpiralLocalPoint(armIndex, Math.min(spiralT + 0.002F, 1.0F), ringRotation);
            Vec3 tangent = safeNormalize(nextLocalPoint.subtract(localPoint), new Vec3(1.0D, 0.0D, 0.0D));
            Vec3 radial = safeNormalize(new Vec3(localPoint.x, 0.0D, localPoint.z), new Vec3(1.0D, 0.0D, 0.0D));

            float tangentSpread = Mth.lerp(spiralT, 1.5F, 5.0F) * Mth.lerp(densityWave, 0.85F, 1.16F);
            float radialSpread = Mth.lerp(spiralT, 2.0F, 6.0F) * Mth.lerp(densityWave, 0.84F, 1.18F);
            float verticalSpread = Mth.lerp(spiralT, 1.0F, 4.0F) * Mth.lerp(densityWave, 0.86F, 1.14F);
            double tangentOffset = (random.nextDouble() * 2.0D - 1.0D) * tangentSpread;
            double radialOffset = (random.nextDouble() * 2.0D - 1.0D) * radialSpread;
            double verticalOffset = (random.nextDouble() * 2.0D - 1.0D) * verticalSpread;
            Vec3 pos = center.add(localPoint)
                    .add(tangent.scale(tangentOffset))
                    .add(radial.scale(radialOffset))
                    .add(0.0D, verticalOffset, 0.0D);

            float radius = (float) Math.sqrt(localPoint.x * localPoint.x + localPoint.z * localPoint.z) + (float) radialOffset;
            radius = Mth.clamp(radius, HEAVENLY_SPIRAL_INNER_RADIUS, HEAVENLY_SPIRAL_OUTER_RADIUS + 3.0F);
            float globalRadiusT = heavenlyGlobalRadiusT(radius);
            float colorT = heavenlyCloudColorT(globalRadiusT, armIndex, spiralT, armSeedPhases[armIndex],
                    (random.nextFloat() - 0.5F) * 0.032F);
            Vector3f visibleColor = sampleHeavenlyCloudColor(colorT);
            Vector3f bloomColor = sampleHeavenlyBloomColor(colorT);
            float startFade = Mth.lerp(smoothRange(spiralT, 0.0F, 0.08F), 0.52F, 1.0F);
            float outerFade = 1.0F - smoothRange(spiralT, 0.80F, 1.0F) * 0.52F;
            float alpha = Mth.clamp((0.26F + densityWave * 0.32F) * startFade * outerFade
                    * (0.90F + random.nextFloat() * 0.20F), 0.16F, 0.68F);
            float sizeCenter = Mth.lerp(spiralT, 10.0F, 19.0F) * Mth.lerp(densityWave, 0.90F, 1.10F);
            float size = sizeCenter * (0.84F + random.nextFloat() * 0.32F);
            float bloomScale = Mth.lerp(globalRadiusT, 0.22F, 0.035F);
            float orbitSpeed = HEAVENLY_SPIRAL_ORBIT_SPEED * (0.97F + random.nextFloat() * 0.06F);
            float radialSpeed = Mth.lerp(spiralT, 0.01F, 0.20F);
            float verticalSpeed = (random.nextFloat() - 0.5F) * Mth.lerp(spiralT, 0.05F, 0.16F);
            float internalLight = calculateHeavenlyInternalLight(radius);
            int frameSegment = pointIndex / 15;
            float frameStart = (armFrameOffsets[armIndex] + frameSegment * 3.7F
                    + (random.nextFloat() - 0.5F) * 2.0F) % SMOKE_TEXTURE_PLAYABLE_FRAMES;
            if (frameStart < 0.0F) frameStart += SMOKE_TEXTURE_PLAYABLE_FRAMES;
            float rotation = armRotations[armIndex] + spiralT * 2.5F
                    + (random.nextFloat() - 0.5F) * 1.4F;
            float angle = (float) Math.atan2(localPoint.z, localPoint.x);

            pendingParticles.add(new SmokeParticleData(pos, size,
                    visibleColor.x(), visibleColor.y(), visibleColor.z(), alpha,
                    bloomColor.x(), bloomColor.y(), bloomColor.z(), bloomScale,
                    spawnTime, lifeTime,
                    frameStart, 6.0F + random.nextFloat() * 3.0F,
                    rotation,
                    (0.16F + random.nextFloat() * 0.22F) * (random.nextBoolean() ? 1.0F : -1.0F),
                    random.nextFloat(), internalLight,
                    Vec3.ZERO, center, radius, angle, orbitSpeed, radialSpeed, verticalSpeed));
        }
    }

    public Vec3 heavenlySpiralLocalPoint(int armIndex, float spiralT, float ringRotation) {
        float radius = heavenlyConnectedSpiralRadius(spiralT);
        double angle = heavenlyConnectedSpiralAngle(armIndex, spiralT, ringRotation);
        return new Vec3(Math.cos(angle) * radius, 0.0D, Math.sin(angle) * radius);
    }

    public float heavenlyConnectedSpiralRadius(float spiralT) {
        float normalRadius = Mth.lerp(spiralT, HEAVENLY_SPIRAL_INNER_RADIUS, HEAVENLY_SPIRAL_OUTER_RADIUS);
        float connectT = smoothRange(spiralT, 0.0F, HEAVENLY_SPIRAL_CONNECT_END_T);
        return Mth.lerp(connectT, HEAVENLY_SPIRAL_ATTACH_RADIUS, normalRadius);
    }

    public double heavenlyConnectedSpiralAngle(int armIndex, float spiralT, float ringRotation) {
        float connectT = smoothRange(spiralT, 0.0F, HEAVENLY_SPIRAL_CONNECT_END_T);
        return heavenlySpiralAngle(armIndex, spiralT, ringRotation)
                - (1.0F - connectT) * HEAVENLY_SPIRAL_CONNECT_CURVE;
    }

    public float heavenlyDensityWave(int armIndex, float spiralT, float seedPhase) {
        float waveA = 0.5F + 0.5F * Mth.sin(spiralT * 17.0F + armIndex * 1.7F + seedPhase);
        float waveB = 0.5F + 0.5F * Mth.sin(spiralT * 31.0F + armIndex * 2.3F + seedPhase * 1.6F);
        return Mth.clamp(waveA * 0.72F + waveB * 0.28F, 0.0F, 1.0F);
    }

    public void addHeavenlySpiralBaseLayer(Vec3 center, RandomSource random, float spawnTime, float lifeTime,
                                           int count, float ringRotation) {
        int pointsPerArm = Math.max(1, (count + HEAVENLY_SPIRAL_ARM_COUNT - 1) / HEAVENLY_SPIRAL_ARM_COUNT);
        for (int i = 0; i < count; i++) {
            int armIndex = i % HEAVENLY_SPIRAL_ARM_COUNT;
            int pointIndex = i / HEAVENLY_SPIRAL_ARM_COUNT;
            float spiralT = Mth.clamp((pointIndex + random.nextFloat()) / pointsPerArm, 0.0F, 1.0F);
            float armWidth = Mth.lerp(spiralT, 2.5F, 8.5F);
            float baseRadius = heavenlyConnectedSpiralRadius(spiralT);
            float radius = Math.max(HEAVENLY_SPIRAL_INNER_RADIUS,
                    baseRadius + (random.nextFloat() * 2.0F - 1.0F) * armWidth);
            double angle = heavenlyConnectedSpiralAngle(armIndex, spiralT, ringRotation)
                    + (random.nextDouble() * 2.0D - 1.0D) * Mth.lerp(spiralT, 0.04F, 0.13F);
            double y = (random.nextDouble() * 2.0D - 1.0D) * Mth.lerp(spiralT, 1.4F, 4.2F);
            Vec3 pos = center.add(Math.cos(angle) * radius, y, Math.sin(angle) * radius);

            float globalRadiusT = heavenlyGlobalRadiusT(radius);
            float colorT = heavenlyCloudColorT(globalRadiusT, armIndex, spiralT, (float) ringRotation,
                    (random.nextFloat() - 0.5F) * 0.065F);
            Vector3f visibleColor = sampleHeavenlyCloudColor(colorT);
            Vector3f bloomColor = sampleHeavenlyBloomColor(colorT);
            float middleWeight = 1.0F - Math.abs(spiralT * 2.0F - 1.0F);
            float alpha = Mth.clamp((0.16F + middleWeight * 0.16F) * (0.82F + random.nextFloat() * 0.24F),
                    0.10F, 0.36F);
            float size = Mth.lerp(random.nextFloat(), 21.0F, 34.0F);
            float bloomScale = Mth.lerp(globalRadiusT, 0.15F, 0.025F);
            float orbitSpeed = HEAVENLY_SPIRAL_ORBIT_SPEED * (0.95F + random.nextFloat() * 0.10F);
            float radialSpeed = Mth.lerp(spiralT, 0.01F, 0.14F);
            float verticalSpeed = (random.nextFloat() - 0.5F) * Mth.lerp(spiralT, 0.06F, 0.16F);
            float internalLight = calculateHeavenlyInternalLight(radius);

            pendingParticles.add(new SmokeParticleData(pos, size,
                    visibleColor.x(), visibleColor.y(), visibleColor.z(), alpha,
                    bloomColor.x(), bloomColor.y(), bloomColor.z(), bloomScale,
                    spawnTime, lifeTime,
                    random.nextFloat() * SMOKE_TEXTURE_PLAYABLE_FRAMES, 5.0F + random.nextFloat() * 3.0F,
                    random.nextFloat() * (float) (Math.PI * 2.0D),
                    (0.10F + random.nextFloat() * 0.16F) * (random.nextBoolean() ? 1.0F : -1.0F),
                    random.nextFloat(), internalLight,
                    Vec3.ZERO, center, radius, (float) angle, orbitSpeed, radialSpeed, verticalSpeed));
        }
    }

    public void addHeavenlySpiralClusterLayer(Vec3 center, RandomSource random, float spawnTime, float lifeTime,
                                              int count, int clusterCount, float ringRotation) {
        int safeClusterCount = Math.max(HEAVENLY_SPIRAL_ARM_COUNT, clusterCount);
        int clustersPerArm = Math.max(2,
                (safeClusterCount + HEAVENLY_SPIRAL_ARM_COUNT - 1) / HEAVENLY_SPIRAL_ARM_COUNT);
        int remaining = count;

        for (int cluster = 0; cluster < safeClusterCount; cluster++) {
            int armIndex = cluster % HEAVENLY_SPIRAL_ARM_COUNT;
            int clusterIndexInArm = cluster / HEAVENLY_SPIRAL_ARM_COUNT;
            float baseT = clusterIndexInArm / (float) Math.max(clustersPerArm - 1, 1);
            float spiralT = Mth.clamp(baseT + (random.nextFloat() - 0.5F) * 0.035F, 0.0F, 1.0F);
            float clusterRadius = Mth.lerp(spiralT, HEAVENLY_SPIRAL_INNER_RADIUS, HEAVENLY_SPIRAL_OUTER_RADIUS);
            double clusterAngle = heavenlySpiralAngle(armIndex, spiralT, ringRotation)
                    + (random.nextDouble() * 2.0D - 1.0D) * 0.055D;
            float middleWeight = 1.0F - Math.abs(spiralT * 2.0F - 1.0F);
            int clustersLeft = safeClusterCount - cluster;
            int averageSize = Math.max(1, remaining / Math.max(clustersLeft, 1));
            int minimumRemaining = Math.max(clustersLeft - 1, 0) * 8;
            int maximumClusterSize = Math.max(8, remaining - minimumRemaining);
            int clusterSize = cluster == safeClusterCount - 1
                    ? remaining
                    : Mth.clamp(averageSize + (int) (middleWeight * 8.0F) + random.nextInt(9) - 4,
                    8, maximumClusterSize);
            remaining -= clusterSize;

            float angleSpread = Mth.lerp(spiralT, 0.055F, 0.15F);
            float radiusSpread = Mth.lerp(spiralT, 2.2F, 7.5F);
            float clusterColorOffset = (random.nextFloat() - 0.5F) * 0.12F;
            for (int i = 0; i < clusterSize; i++) {
                double angle = clusterAngle + (random.nextDouble() * 2.0D - 1.0D) * angleSpread;
                float radius = Mth.clamp(
                        clusterRadius + (random.nextFloat() * 2.0F - 1.0F) * radiusSpread,
                        HEAVENLY_SPIRAL_INNER_RADIUS, HEAVENLY_SPIRAL_OUTER_RADIUS + 2.0F);
                float globalRadiusT = heavenlyGlobalRadiusT(radius);
                float colorT = Mth.clamp(globalRadiusT + clusterColorOffset
                        + (random.nextFloat() - 0.5F) * 0.05F, 0.0F, 1.0F);
                Vector3f visibleColor = sampleHeavenlyCloudColor(colorT);
                Vector3f bloomColor = sampleHeavenlyBloomColor(colorT);

                float startFade = smoothRange(spiralT, 0.0F, 0.10F);
                float outerFade = 1.0F - smoothRange(spiralT, 0.78F, 1.0F) * 0.55F;
                float alpha = Mth.clamp((0.38F + middleWeight * 0.36F)
                        * startFade * outerFade * (0.82F + random.nextFloat() * 0.24F), 0.08F, 0.82F);
                float sizeCenter = Mth.lerp(spiralT, 9.0F, 19.0F);
                float size = sizeCenter * (0.72F + random.nextFloat() * 0.58F);
                float bloomScale = Mth.lerp(globalRadiusT, 0.30F, 0.04F);
                float orbitSpeed = HEAVENLY_SPIRAL_ORBIT_SPEED * (0.94F + random.nextFloat() * 0.12F);
                float radialSpeed = Mth.lerp(spiralT, 0.01F, 0.22F);
                float verticalSpeed = (random.nextFloat() - 0.5F) * Mth.lerp(spiralT, 0.06F, 0.18F);
                float internalLight = calculateHeavenlyInternalLight(radius);

                pendingParticles.add(new SmokeParticleData(
                        center.add(Math.cos(angle) * radius,
                                (random.nextDouble() * 2.0D - 1.0D) * Mth.lerp(spiralT, 1.0F, 4.5F),
                                Math.sin(angle) * radius),
                        size, visibleColor.x(), visibleColor.y(), visibleColor.z(), alpha,
                        bloomColor.x(), bloomColor.y(), bloomColor.z(), bloomScale,
                        spawnTime, lifeTime,
                        random.nextFloat() * SMOKE_TEXTURE_PLAYABLE_FRAMES, 6.0F + random.nextFloat() * 4.0F,
                        random.nextFloat() * (float) (Math.PI * 2.0D),
                        (0.18F + random.nextFloat() * 0.26F) * (random.nextBoolean() ? 1.0F : -1.0F),
                        random.nextFloat(), internalLight,
                        Vec3.ZERO, center, radius, (float) angle, orbitSpeed, radialSpeed, verticalSpeed));
            }
        }
    }

    public void addHeavenlySpiralEdgeLayer(Vec3 center, RandomSource random, float spawnTime, float lifeTime,
                                           int count, float ringRotation) {
        int pointsPerArm = Math.max(1, (count + HEAVENLY_SPIRAL_ARM_COUNT - 1) / HEAVENLY_SPIRAL_ARM_COUNT);
        for (int i = 0; i < count; i++) {
            int armIndex = i % HEAVENLY_SPIRAL_ARM_COUNT;
            int pointIndex = i / HEAVENLY_SPIRAL_ARM_COUNT;
            float pointT = Mth.clamp((pointIndex + random.nextFloat()) / pointsPerArm, 0.0F, 1.0F);
            float spiralT = Mth.lerp(pointT, 0.55F, 1.05F);
            float baseRadius = Mth.lerp(spiralT, HEAVENLY_SPIRAL_INNER_RADIUS, HEAVENLY_SPIRAL_OUTER_RADIUS);
            float armWidth = Mth.lerp(pointT, 6.0F, 12.0F);
            float radius = Math.max(HEAVENLY_SPIRAL_INNER_RADIUS,
                    baseRadius + (random.nextFloat() * 2.0F - 1.0F) * armWidth);
            double angle = heavenlySpiralAngle(armIndex, spiralT, ringRotation)
                    + (random.nextDouble() * 2.0D - 1.0D) * Mth.lerp(pointT, 0.10F, 0.24F);
            float globalRadiusT = heavenlyGlobalRadiusT(radius);
            float colorT = heavenlyCloudColorT(globalRadiusT, armIndex, spiralT, (float) ringRotation,
                    (random.nextFloat() - 0.5F) * 0.085F);
            Vector3f visibleColor = sampleHeavenlyCloudColor(colorT);
            Vector3f bloomColor = sampleHeavenlyBloomColor(colorT);
            float endFade = 1.0F - smoothRange(pointT, 0.72F, 1.0F) * 0.72F;
            float alpha = Mth.clamp((0.15F + random.nextFloat() * 0.22F) * endFade, 0.04F, 0.36F);
            float size = Mth.lerp(random.nextFloat(), 7.0F, 16.0F);
            float orbitSpeed = HEAVENLY_SPIRAL_ORBIT_SPEED * (0.92F + random.nextFloat() * 0.16F);
            float radialSpeed = Mth.lerp(pointT, 0.16F, 0.34F);
            float verticalSpeed = (random.nextFloat() - 0.5F) * Mth.lerp(pointT, 0.14F, 0.28F);
            float internalLight = calculateHeavenlyInternalLight(radius);

            pendingParticles.add(new SmokeParticleData(
                    center.add(Math.cos(angle) * radius,
                            (random.nextDouble() * 2.0D - 1.0D) * Mth.lerp(pointT, 2.2F, 5.2F),
                            Math.sin(angle) * radius),
                    size, visibleColor.x(), visibleColor.y(), visibleColor.z(), alpha,
                    bloomColor.x(), bloomColor.y(), bloomColor.z(), Mth.lerp(globalRadiusT, 0.08F, 0.01F),
                    spawnTime, lifeTime,
                    random.nextFloat() * SMOKE_TEXTURE_PLAYABLE_FRAMES, 6.0F + random.nextFloat() * 5.0F,
                    random.nextFloat() * (float) (Math.PI * 2.0D),
                    (0.20F + random.nextFloat() * 0.34F) * (random.nextBoolean() ? 1.0F : -1.0F),
                    random.nextFloat(), internalLight,
                    Vec3.ZERO, center, radius, (float) angle, orbitSpeed, radialSpeed, verticalSpeed));
        }
    }

    public double heavenlySpiralAngle(int armIndex, float spiralT, float ringRotation) {
        double armOffset = armIndex * Math.PI * 2.0D / HEAVENLY_SPIRAL_ARM_COUNT;
        return ringRotation + armOffset + spiralT * HEAVENLY_SPIRAL_TURNS * Math.PI * 2.0D;
    }

    public float heavenlyCloudColorT(float globalRadiusT, int armIndex, float spiralT,
                                      float colorPhase, float particleNoise) {
        float armOffset = (armIndex - (HEAVENLY_SPIRAL_ARM_COUNT - 1) * 0.5F) * 0.03F;
        float colorWave = Mth.sin(spiralT * (float) Math.PI * 4.0F
                + armIndex * 1.7F + colorPhase) * 0.035F;
        return Mth.clamp(globalRadiusT + armOffset + colorWave + particleNoise, 0.0F, 1.0F);
    }

    public float heavenlyGlobalRadiusT(float radius) {
        float radiusRange = Math.max(HEAVENLY_SPIRAL_OUTER_RADIUS - HEAVENLY_SPIRAL_INNER_RADIUS, 0.001F);
        return Mth.clamp((radius - HEAVENLY_SPIRAL_INNER_RADIUS) / radiusRange, 0.0F, 1.0F);
    }

    public float smoothRange(float value, float start, float end) {
        float range = Math.max(end - start, 0.001F);
        float t = Mth.clamp((value - start) / range, 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    public Vector3f sampleHeavenlyCloudColor(float radiusT) {
        float t = Mth.clamp(radiusT, 0.0F, 1.0F);
        if (t < 0.08F) {
            return lerpHeavenlyColor(0.88F, 0.97F, 1.00F,
                    0.54F, 0.90F, 1.00F, smoothRange(t, 0.00F, 0.08F));
        }
        if (t < 0.24F) {
            return lerpHeavenlyColor(0.54F, 0.90F, 1.00F,
                    0.32F, 0.68F, 1.00F, smoothRange(t, 0.08F, 0.24F));
        }
        if (t < 0.46F) {
            return lerpHeavenlyColor(0.32F, 0.68F, 1.00F,
                    0.36F, 0.38F, 0.92F, smoothRange(t, 0.24F, 0.46F));
        }
        if (t < 0.68F) {
            return lerpHeavenlyColor(0.36F, 0.38F, 0.92F,
                    0.15F, 0.25F, 0.68F, smoothRange(t, 0.46F, 0.68F));
        }
        if (t < 0.86F) {
            return lerpHeavenlyColor(0.15F, 0.25F, 0.68F,
                    0.34F, 0.45F, 0.62F, smoothRange(t, 0.68F, 0.86F));
        }
        return lerpHeavenlyColor(0.34F, 0.45F, 0.62F,
                0.58F, 0.66F, 0.74F, smoothRange(t, 0.86F, 1.00F));
    }

    public Vector3f sampleHeavenlyBloomColor(float radiusT) {
        float t = Mth.clamp(radiusT, 0.0F, 1.0F);
        if (t < 0.22F) {
            return lerpHeavenlyColor(0.66F, 0.92F, 1.00F,
                    0.32F, 0.66F, 1.00F, smoothRange(t, 0.00F, 0.22F));
        }
        if (t < 0.52F) {
            return lerpHeavenlyColor(0.32F, 0.66F, 1.00F,
                    0.26F, 0.34F, 0.92F, smoothRange(t, 0.22F, 0.52F));
        }
        return lerpHeavenlyColor(0.26F, 0.34F, 0.92F,
                0.10F, 0.18F, 0.36F, smoothRange(t, 0.52F, 1.00F));
    }

    public Vector3f lerpHeavenlyColor(float startR, float startG, float startB,
                                      float endR, float endG, float endB, float t) {
        return new Vector3f(
                Mth.lerp(t, startR, endR),
                Mth.lerp(t, startG, endG),
                Mth.lerp(t, startB, endB)
        );
    }

    public void addCloudLayer(Vec3 center, RandomSource random, float spawnTime, int count,
                              float radius, float height, float minSize, float maxSize,
                              float colorR, float colorG, float colorB, float alphaBase) {
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double radial = Math.sqrt(random.nextDouble()) * radius;
            double x = Math.cos(angle) * radial;
            double z = Math.sin(angle) * radial * (0.72D + random.nextDouble() * 0.38D);
            double y = (random.nextDouble() * 2.0D - 1.0D) * height + Math.max(0.0D, 1.0D - radial / Math.max(radius, 0.001D)) * height * 0.45D;
            Vec3 pos = center.add(x, y, z);
            float colorNoise = 0.96F + random.nextFloat() * 0.08F;
            float alpha = Mth.clamp(alphaBase * (0.78F + random.nextFloat() * 0.28F), 0.0F, 1.0F);
            float size = Mth.lerp(random.nextFloat(), minSize, maxSize);
            pendingParticles.add(new SmokeParticleData(pos, size,
                    Mth.clamp(colorR * colorNoise, 0.0F, 1.15F),
                    Mth.clamp(colorG * colorNoise, 0.0F, 1.15F),
                    Mth.clamp(colorB * colorNoise, 0.0F, 1.15F),
                    alpha,
                    1.0F, 1.0F, 1.0F, 0.1F,
                    spawnTime, TEST_SMOKE_CLOUD_DURATION,
                    random.nextFloat() * SMOKE_TEXTURE_PLAYABLE_FRAMES, 8.0F + random.nextFloat() * 5.0F,
                    random.nextFloat() * (float) (Math.PI * 2.0D), (random.nextFloat() - 0.5F) * 0.14F,
                    random.nextFloat()));
        }
    }

    public void addCloudRingLayer(Vec3 center, RandomSource random, float spawnTime, int count,
                                  float innerRadius, float outerRadius, float heightNoise,
                                  float minSize, float maxSize, float ringRotation) {
        float radiusRange = Math.max(outerRadius - innerRadius, 0.001F);
        for (int i = 0; i < count; i++) {
            double angle = ringRotation + Math.PI * 2.0D * (i + random.nextDouble() * 0.85D) / count;
            float radialT = (float) Math.sqrt(random.nextFloat());
            float radius = Mth.lerp(radialT, innerRadius, outerRadius);
            float gradientT = Mth.clamp((radius - innerRadius) / radiusRange, 0.0F, 1.0F);
            float radiusNoise = (random.nextFloat() - 0.5F) * 0.18F;
            double x = Math.cos(angle) * (radius + radiusNoise);
            double z = Math.sin(angle) * (radius + radiusNoise);
            double y = (random.nextDouble() * 2.0D - 1.0D) * heightNoise;
            Vec3 pos = center.add(x, y, z);
            float colorNoise = 0.98F + random.nextFloat() * 0.04F;
            float colorR = Mth.clamp(Mth.lerp(gradientT, 1.0F, 0.46F) * colorNoise, 0.0F, 1.08F);
            float colorG = Mth.clamp(Mth.lerp(gradientT, 1.0F, 0.72F) * colorNoise, 0.0F, 1.08F);
            float colorB = Mth.clamp(colorNoise, 0.0F, 1.08F);
            float alpha = Mth.clamp(Mth.lerp(gradientT, 0.90F, 0.78F) * (0.86F + random.nextFloat() * 0.20F), 0.0F, 1.0F);
            float size = Mth.lerp(random.nextFloat(), minSize, maxSize);
            float bloomScale = Mth.lerp(gradientT, 0.04F, 0.12F);
            float rotationSpeed = 0.18F + random.nextFloat() * 0.28F;
            Vec3 orbitCenter = center;
            float orbitRadius = Math.max(0.0F, radius + radiusNoise);
            float orbitAngle = (float) angle;
            float orbitSpeed = Mth.lerp(gradientT, 0.42F, 0.26F) * (0.92F + random.nextFloat() * 0.16F); // 内圈略快、外圈略慢。
            float radialSpeed = Mth.lerp(gradientT, 0.01F, 0.05F);
            float verticalSpeed = (random.nextFloat() - 0.5F) * 0.03F;
            pendingParticles.add(new SmokeParticleData(pos, size,
                    colorR, colorG, colorB, alpha,
                    Mth.lerp(gradientT, 1.0F, 0.35F), Mth.lerp(gradientT, 1.0F, 0.70F), 1.0F, bloomScale,
                    spawnTime, TEST_SMOKE_CLOUD_DURATION,
                    random.nextFloat() * SMOKE_TEXTURE_PLAYABLE_FRAMES, 8.0F + random.nextFloat() * 5.0F,
                    random.nextFloat() * (float) (Math.PI * 2.0D), rotationSpeed,
                    random.nextFloat(),
                    Vec3.ZERO, orbitCenter, orbitRadius, orbitAngle, orbitSpeed, radialSpeed, verticalSpeed));
        }
    }

    public void addHeavenlyCloudRingLayer(Vec3 center, RandomSource random, float spawnTime, float lifeTime,
                                          int count, float innerRadius, float outerRadius, float heightNoise,
                                          float minSize, float maxSize,
                                          float visibleR, float visibleG, float visibleB,
                                          float bloomR, float bloomG, float bloomB,
                                          float minBloomScale, float maxBloomScale, float ringRotation) {
        float radiusRange = Math.max(outerRadius - innerRadius, 0.001F);
        for (int i = 0; i < count; i++) {
            double angle = ringRotation + Math.PI * 2.0D * (i + random.nextDouble() * 0.95D) / count;
            float radialT = (float) Math.sqrt(random.nextFloat());
            float radius = Mth.lerp(radialT, innerRadius, outerRadius);
            float gradientT = Mth.clamp((radius - innerRadius) / radiusRange, 0.0F, 1.0F);
            float radiusNoise = (random.nextFloat() - 0.5F) * radiusRange * 0.16F;
            double x = Math.cos(angle) * (radius + radiusNoise);
            double z = Math.sin(angle) * (radius + radiusNoise);
            double y = (random.nextDouble() * 2.0D - 1.0D) * heightNoise;
            Vec3 pos = center.add(x, y, z);
            float colorNoise = 0.96F + random.nextFloat() * 0.08F;
            float alpha = Mth.clamp(Mth.lerp(gradientT, 0.86F, 0.42F) * (0.82F + random.nextFloat() * 0.22F), 0.0F, 1.0F);
            float size = Mth.lerp(random.nextFloat(), minSize, maxSize);
            float bloomScale = Mth.lerp(gradientT, maxBloomScale, minBloomScale);
            float rotationSpeed = (0.22F + random.nextFloat() * 0.30F) * (random.nextBoolean() ? 1.0F : -1.0F);
            float orbitRadius = Math.max(0.0F, radius + radiusNoise);
            float orbitAngle = (float) angle;
            float direction = gradientT > 0.68F && random.nextFloat() < 0.18F ? -1.0F : 1.0F;
            float orbitSpeed = direction * Mth.lerp(gradientT, 0.45F, 0.22F) * (0.92F + random.nextFloat() * 0.24F);
            float radialSpeed = Mth.lerp(gradientT, 0.02F, 0.24F);
            float verticalSpeed = (random.nextFloat() - 0.5F) * 0.14F;
            float internalLight = calculateHeavenlyInternalLight(orbitRadius);
            pendingParticles.add(new SmokeParticleData(pos, size,
                    Mth.clamp(visibleR * colorNoise, 0.0F, 1.15F),
                    Mth.clamp(visibleG * colorNoise, 0.0F, 1.15F),
                    Mth.clamp(visibleB * colorNoise, 0.0F, 1.15F),
                    alpha,
                    bloomR, bloomG, bloomB, bloomScale,
                    spawnTime, lifeTime,
                    random.nextFloat() * SMOKE_TEXTURE_PLAYABLE_FRAMES, 7.0F + random.nextFloat() * 4.0F,
                    random.nextFloat() * (float) (Math.PI * 2.0D), rotationSpeed,
                    random.nextFloat(), internalLight,
                    Vec3.ZERO, center, orbitRadius, orbitAngle, orbitSpeed, radialSpeed, verticalSpeed));
        }
    }

    public void addHeavenlyCloudBaseLayer(Vec3 center, RandomSource random, float spawnTime, float lifeTime,
                                          int count, float innerRadius, float outerRadius, float heightNoise,
                                          float minSize, float maxSize,
                                          float visibleR, float visibleG, float visibleB,
                                          float bloomR, float bloomG, float bloomB,
                                          float minBloomScale, float maxBloomScale, float ringRotation) {
        float radiusRange = Math.max(outerRadius - innerRadius, 0.001F);
        for (int i = 0; i < count; i++) {
            double angle = ringRotation + Math.PI * 2.0D * (i + random.nextDouble()) / count;
            float radialT = (float) Math.sqrt(random.nextFloat());
            float radius = Mth.lerp(radialT, innerRadius, outerRadius);
            float gradientT = Mth.clamp((radius - innerRadius) / radiusRange, 0.0F, 1.0F);
            float radiusNoise = (random.nextFloat() - 0.5F) * radiusRange * 0.22F;
            double x = Math.cos(angle) * (radius + radiusNoise);
            double z = Math.sin(angle) * (radius + radiusNoise);
            double y = (random.nextDouble() * 2.0D - 1.0D) * heightNoise;
            Vec3 pos = center.add(x, y, z);
            float colorNoise = 0.92F + random.nextFloat() * 0.12F;
            float alpha = Mth.clamp(Mth.lerp(gradientT, 0.34F, 0.22F) * (0.78F + random.nextFloat() * 0.24F), 0.0F, 0.42F);
            float size = Mth.lerp(random.nextFloat(), minSize, maxSize);
            float bloomScale = Mth.lerp(gradientT, maxBloomScale, minBloomScale);
            float rotationSpeed = (0.10F + random.nextFloat() * 0.16F) * (random.nextBoolean() ? 1.0F : -1.0F);
            float orbitRadius = Math.max(0.0F, radius + radiusNoise);
            float orbitAngle = (float) angle;
            float orbitSpeed = Mth.lerp(gradientT, 0.22F, 0.12F) * (0.88F + random.nextFloat() * 0.18F);
            float radialSpeed = Mth.lerp(gradientT, 0.02F, 0.12F);
            float verticalSpeed = (random.nextFloat() - 0.5F) * 0.08F;
            float internalLight = calculateHeavenlyInternalLight(orbitRadius);
            pendingParticles.add(new SmokeParticleData(pos, size,
                    Mth.clamp(visibleR * colorNoise, 0.0F, 1.15F),
                    Mth.clamp(visibleG * colorNoise, 0.0F, 1.15F),
                    Mth.clamp(visibleB * colorNoise, 0.0F, 1.15F),
                    alpha,
                    bloomR, bloomG, bloomB, bloomScale,
                    spawnTime, lifeTime,
                    random.nextFloat() * SMOKE_TEXTURE_PLAYABLE_FRAMES, 5.0F + random.nextFloat() * 3.0F,
                    random.nextFloat() * (float) (Math.PI * 2.0D), rotationSpeed,
                    random.nextFloat(), internalLight,
                    Vec3.ZERO, center, orbitRadius, orbitAngle, orbitSpeed, radialSpeed, verticalSpeed));
        }
    }

    public void addHeavenlyCloudClusterLayer(Vec3 center, RandomSource random, float spawnTime, float lifeTime,
                                             int count, float innerRadius, float outerRadius, float heightNoise,
                                             float minSize, float maxSize,
                                             float visibleR, float visibleG, float visibleB,
                                             float bloomR, float bloomG, float bloomB,
                                             float minBloomScale, float maxBloomScale, float ringRotation) {
        int clusterCount = Mth.clamp(count / 28, 16, 32);
        int remaining = count;
        float radiusRange = Math.max(outerRadius - innerRadius, 0.001F);
        for (int cluster = 0; cluster < clusterCount; cluster++) {
            int clustersLeft = clusterCount - cluster;
            int clusterSize = cluster == clusterCount - 1 ? remaining : Math.max(8, remaining / clustersLeft + random.nextInt(15) - 7);
            remaining -= clusterSize;
            double clusterAngle = ringRotation + Math.PI * 2.0D * (cluster + random.nextDouble() * 0.65D) / clusterCount;
            float clusterRadius = Mth.lerp((float) Math.sqrt(random.nextFloat()), innerRadius, outerRadius);
            float angleSpread = Mth.lerp(random.nextFloat(), 0.08F, 0.24F);
            float radiusSpread = Mth.lerp(random.nextFloat(), radiusRange * 0.06F, radiusRange * 0.18F);
            for (int i = 0; i < clusterSize; i++) {
                double angle = clusterAngle + (random.nextDouble() * 2.0D - 1.0D) * angleSpread;
                float radius = Mth.clamp(clusterRadius + (random.nextFloat() * 2.0F - 1.0F) * radiusSpread, innerRadius, outerRadius);
                addHeavenlyClusterParticle(center, random, spawnTime, lifeTime, radiusRange, innerRadius, heightNoise,
                        minSize, maxSize, visibleR, visibleG, visibleB, bloomR, bloomG, bloomB,
                        minBloomScale, maxBloomScale, angle, radius);
            }
        }
    }

    public void addHeavenlyClusterParticle(Vec3 center, RandomSource random, float spawnTime, float lifeTime,
                                           float radiusRange, float innerRadius, float heightNoise,
                                           float minSize, float maxSize,
                                           float visibleR, float visibleG, float visibleB,
                                           float bloomR, float bloomG, float bloomB,
                                           float minBloomScale, float maxBloomScale,
                                           double angle, float radius) {
        float gradientT = Mth.clamp((radius - innerRadius) / radiusRange, 0.0F, 1.0F);
        double x = Math.cos(angle) * radius;
        double z = Math.sin(angle) * radius;
        double y = (random.nextDouble() * 2.0D - 1.0D) * heightNoise;
        Vec3 pos = center.add(x, y, z);
        float colorNoise = 0.94F + random.nextFloat() * 0.12F;
        float alpha = Mth.clamp(Mth.lerp(gradientT, 0.78F, 0.36F) * (0.76F + random.nextFloat() * 0.28F), 0.0F, 1.0F);
        float size = Mth.lerp(random.nextFloat(), minSize, maxSize);
        float bloomScale = Mth.lerp(gradientT, maxBloomScale, minBloomScale);
        float rotationSpeed = (0.18F + random.nextFloat() * 0.26F) * (random.nextBoolean() ? 1.0F : -1.0F);
        float orbitAngle = (float) angle;
        float direction = gradientT > 0.68F && random.nextFloat() < 0.22F ? -1.0F : 1.0F;
        float orbitSpeed = direction * Mth.lerp(gradientT, 0.40F, 0.20F) * (0.86F + random.nextFloat() * 0.28F);
        float radialSpeed = Mth.lerp(gradientT, 0.02F, 0.28F);
        float verticalSpeed = (random.nextFloat() - 0.5F) * 0.16F;
        float internalLight = calculateHeavenlyInternalLight(radius);
        pendingParticles.add(new SmokeParticleData(pos, size,
                Mth.clamp(visibleR * colorNoise, 0.0F, 1.15F),
                Mth.clamp(visibleG * colorNoise, 0.0F, 1.15F),
                Mth.clamp(visibleB * colorNoise, 0.0F, 1.15F),
                alpha,
                bloomR, bloomG, bloomB, bloomScale,
                spawnTime, lifeTime,
                random.nextFloat() * SMOKE_TEXTURE_PLAYABLE_FRAMES, 6.0F + random.nextFloat() * 4.0F,
                random.nextFloat() * (float) (Math.PI * 2.0D), rotationSpeed,
                random.nextFloat(), internalLight,
                Vec3.ZERO, center, radius, orbitAngle, orbitSpeed, radialSpeed, verticalSpeed));
    }

    public static float calculateHeavenlyInternalLight(float radius) {
        float radiusRange = Math.max(HEAVENLY_LIGHT_OUTER_RADIUS - HEAVENLY_LIGHT_INNER_RADIUS, 0.001F);
        float distanceT = Mth.clamp((radius - HEAVENLY_LIGHT_INNER_RADIUS) / radiusRange, 0.0F, 1.0F);
        float inverseDistance = 1.0F - distanceT;
        return inverseDistance * inverseDistance;
    }

    public void addRingBand(Vec3 center, Vec3 ringRight, Vec3 ringUp, RandomSource random, int count,
                            float radius, float radiusNoise, float size, float angularSpeed,
                            float colorR, float colorG, float colorB,
                            float bloomR, float bloomG, float bloomB, float bloomScale) {
        float spawnTime = MathUtil.getClientTime(0.0F);
        for (int i = 0; i < count; i++) {
            double angle = Math.PI * 2.0D * (i + random.nextDouble() * 0.65D) / count;
            double jitterRadius = radius + (random.nextFloat() - 0.5F) * radiusNoise * 2.0F;
            double heightJitter = (random.nextFloat() - 0.5F) * radiusNoise * 0.55F;
            Vec3 pos = center
                    .add(ringRight.scale(Math.cos(angle) * jitterRadius))
                    .add(ringUp.scale(Math.sin(angle) * jitterRadius + heightJitter));
            float alpha = Mth.clamp(0.46F + random.nextFloat() * 0.36F, 0.0F, 1.0F);
            float particleSize = size * (0.72F + random.nextFloat() * 0.62F);
            float direction = random.nextFloat() < 0.12F ? -1.0F : 1.0F;
            pendingParticles.add(new SmokeParticleData(pos, particleSize, colorR, colorG, colorB, alpha,
                    bloomR, bloomG, bloomB, bloomScale, spawnTime, TEST_SMOKE_RING_DURATION,
                    random.nextFloat() * SMOKE_TEXTURE_PLAYABLE_FRAMES, 18.0F + random.nextFloat() * 9.0F,
                    random.nextFloat() * (float) (Math.PI * 2.0D), direction * angularSpeed * (0.35F + random.nextFloat() * 0.35F),
                    random.nextFloat()));
        }
    }

    public void render(Camera camera, float partialTick, Matrix4f viewMatrix, int sceneDepthTextureId, int screenWidth, int screenHeight) {
        if (!SmokeParticleShader.isLoaded()) return;
        if (!activatePending()) return;

        float time = MathUtil.getClientTime(partialTick);
        removeExpired(time);
        if (activeParticles.isEmpty()) return;
        sortBackToFront(camera, time);
        renderer.render(activeParticles, time, camera, viewMatrix, sceneDepthTextureId, screenWidth, screenHeight);
    }

    public boolean activatePending() {
        if (!pendingParticles.isEmpty()) {
            activeParticles.addAll(pendingParticles);
            pendingParticles.clear();
        }
        return !activeParticles.isEmpty();
    }

    public void removeExpired(float time) {
        Iterator<SmokeParticleData> iterator = activeParticles.iterator();
        while (iterator.hasNext()) {
            SmokeParticleData particle = iterator.next();
            if (time - particle.spawnTime > particle.lifeTime) {
                iterator.remove();
            }
        }
    }

    public void sortBackToFront(Camera camera, float time) {
        Vec3 cameraPos = camera.getPosition();
        activeParticles.sort(Comparator.comparingDouble((SmokeParticleData particle) -> particle.currentPosition(time).distanceToSqr(cameraPos)).reversed());
    }

    public boolean hasActive() {
        return !pendingParticles.isEmpty() || !activeParticles.isEmpty();
    }

    public void clear() {
        pendingParticles.clear();
        activeParticles.clear();
    }

    public void cleanUp() {
        clear();
        renderer.cleanup();
    }

    public static Vec3 horizontalNormal(Vec3 normal) {
        if (normal == null) return new Vec3(0.0D, 0.0D, 1.0D);
        return new Vec3(normal.x, 0.0D, normal.z);
    }

    public static Vec3 safeNormalize(Vec3 vector, Vec3 fallback) {
        if (vector == null || vector.lengthSqr() < 1.0E-8D) return fallback;
        return vector.normalize();
    }

    public static class SmokeParticleData {
        public final Vec3 position; // 粒子中心位置。
        public final float size; // 粒子尺寸。
        public final float colorR; // 可见颜色 R。
        public final float colorG; // 可见颜色 G。
        public final float colorB; // 可见颜色 B。
        public final float alpha; // 可见透明度。
        public final float bloomR; // bloom 颜色 R。
        public final float bloomG; // bloom 颜色 G。
        public final float bloomB; // bloom 颜色 B。
        public final float bloomScale; // bloom 强度。
        public final float spawnTime; // 生成时间。
        public final float lifeTime; // 生命周期。
        public final float frameOffset; // 序列帧偏移。
        public final float frameRate; // 序列帧播放速度。
        public final float rotation; // 初始旋转。
        public final float rotationSpeed; // 旋转速度。
        public final float randomValue; // 随机扰动值。
        public final float internalLight; // 内部光源照亮强度，负数表示不启用假体积光照。
        public final Vec3 velocity; // 线性速度，单位为格/秒。
        public final Vec3 orbitCenter; // 环绕中心，为 null 时表示不启用环绕。
        public final float orbitRadius; // 初始环绕半径。
        public final float orbitAngle; // 初始环绕角度，单位弧度。
        public final float orbitSpeed; // 环绕角速度，单位弧度/秒。
        public final float radialSpeed; // 半径变化速度，正数向外扩散，负数向内收缩。
        public final float verticalSpeed; // 额外 Y 轴漂移速度。

        public SmokeParticleData(Vec3 position, float size, float colorR, float colorG, float colorB, float alpha,
                                 float bloomR, float bloomG, float bloomB, float bloomScale, float spawnTime,
                                 float lifeTime, float frameOffset, float frameRate, float rotation,
                                 float rotationSpeed, float randomValue) {
            this(position, size, colorR, colorG, colorB, alpha,
                    bloomR, bloomG, bloomB, bloomScale, spawnTime,
                    lifeTime, frameOffset, frameRate, rotation,
                    rotationSpeed, randomValue, -1.0F,
                    Vec3.ZERO, null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        }

        public SmokeParticleData(Vec3 position, float size, float colorR, float colorG, float colorB, float alpha,
                                 float bloomR, float bloomG, float bloomB, float bloomScale, float spawnTime,
                                 float lifeTime, float frameOffset, float frameRate, float rotation,
                                 float rotationSpeed, float randomValue, Vec3 velocity, Vec3 orbitCenter,
                                 float orbitRadius, float orbitAngle, float orbitSpeed,
                                 float radialSpeed, float verticalSpeed) {
            this(position, size, colorR, colorG, colorB, alpha,
                    bloomR, bloomG, bloomB, bloomScale, spawnTime,
                    lifeTime, frameOffset, frameRate, rotation,
                    rotationSpeed, randomValue, -1.0F,
                    velocity, orbitCenter, orbitRadius, orbitAngle, orbitSpeed, radialSpeed, verticalSpeed);
        }

        public SmokeParticleData(Vec3 position, float size, float colorR, float colorG, float colorB, float alpha,
                                 float bloomR, float bloomG, float bloomB, float bloomScale, float spawnTime,
                                 float lifeTime, float frameOffset, float frameRate, float rotation,
                                 float rotationSpeed, float randomValue, float internalLight,
                                 Vec3 velocity, Vec3 orbitCenter, float orbitRadius, float orbitAngle,
                                 float orbitSpeed, float radialSpeed, float verticalSpeed) {
            this.position = position;
            this.size = size;
            this.colorR = colorR;
            this.colorG = colorG;
            this.colorB = colorB;
            this.alpha = alpha;
            this.bloomR = bloomR;
            this.bloomG = bloomG;
            this.bloomB = bloomB;
            this.bloomScale = bloomScale;
            this.spawnTime = spawnTime;
            this.lifeTime = lifeTime;
            this.frameOffset = frameOffset;
            this.frameRate = frameRate;
            this.rotation = rotation;
            this.rotationSpeed = rotationSpeed;
            this.randomValue = randomValue;
            this.internalLight = internalLight;
            this.velocity = velocity == null ? Vec3.ZERO : velocity;
            this.orbitCenter = orbitCenter;
            this.orbitRadius = orbitRadius;
            this.orbitAngle = orbitAngle;
            this.orbitSpeed = orbitSpeed;
            this.radialSpeed = radialSpeed;
            this.verticalSpeed = verticalSpeed;
        }

        public Vec3 currentPosition(float time) {
            float age = Math.max(0.0F, time - spawnTime);
            Vec3 current = position.add(velocity.scale(age)).add(0.0D, verticalSpeed * age, 0.0D);
            if (orbitCenter == null || Math.abs(orbitSpeed) <= 1.0E-5F) return current;
            float radius = Math.max(0.0F, orbitRadius + radialSpeed * age);
            double angle = orbitAngle + orbitSpeed * age;
            return orbitCenter.add(Math.cos(angle) * radius, current.y - orbitCenter.y, Math.sin(angle) * radius);
        }

        public boolean writeInstance(FloatBuffer buffer, float time) {
            float age = time - spawnTime;
            if (age < 0.0F || age > lifeTime) return false;
            Vec3 currentPosition = currentPosition(time);
            buffer.put((float) currentPosition.x).put((float) currentPosition.y).put((float) currentPosition.z).put(size);
            buffer.put(colorR).put(colorG).put(colorB).put(alpha);
            buffer.put(bloomR).put(bloomG).put(bloomB).put(bloomScale);
            buffer.put(spawnTime).put(lifeTime).put(frameOffset).put(frameRate);
            buffer.put(rotation).put(rotationSpeed).put(randomValue).put(internalLight);
            return true;
        }
    }
}
