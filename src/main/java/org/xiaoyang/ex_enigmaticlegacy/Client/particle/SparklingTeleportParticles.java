package org.xiaoyang.ex_enigmaticlegacy.Client.particle;

import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.system.ParticleEmitTask;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

// SparklingTeleportParticles 在客户端发射闪闪果实瞬移残影 GPU 粒子。
public class SparklingTeleportParticles {
    public static final int[] COLORS = {0xFFF176, 0xFFD54F, 0xFFB300, 0xFF7043, 0xFFFFFF}; // 闪闪果实瞬移粒子候选颜色。
    public static final int BURST_POINTS = 8; // 单次瞬移的 burst 点位数量。
    public static final int MIN_BURST_COUNT = 15; // 单个点位最少粒子数量。
    public static final int MAX_BURST_COUNT = 30; // 单个点位最多粒子数量。
    public static final int MIN_TRAIL_POINTS = 3; // 路径拖尾最少发射器数量。
    public static final int MAX_TRAIL_POINTS = 8; // 路径拖尾最大发射器数量。

    // 在原位置按玩家身高发射多个 GPU 粒子 burst。
    public static void spawn(Vec3 origin, float height, float width) {
        spawn(origin, origin, height, width);
    }

    // 在原位置发射残影，并从新位置往旧位置补一段 GPU 粒子拖尾。
    public static void spawn(Vec3 origin, Vec3 target, float height, float width) {
        if (Exe.POST == null || origin == null) return;
        RandomSource random = RandomSource.create();
        spawnOriginBurst(origin, height, width, random);
//        spawnPathTrail(origin, target, random);
    }

    // 沿玩家高度生成几个点位，每个点位批量爆发，避免逐粒子提交。
    public static void spawnOriginBurst(Vec3 origin, float height, float width, RandomSource random) {
        float safeHeight = Math.max(height, 1.0F);
        float radius = Math.max(width * 0.65F, 0.35F);

        for (int i = 0; i < BURST_POINTS; i++) {
            float heightRatio = BURST_POINTS == 1 ? 0.5F : (float) i / (float) (BURST_POINTS - 1);
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double radial = radius * (0.25D + random.nextDouble() * 0.75D);
            Vec3 point = origin.add(Math.cos(angle) * radial, safeHeight * heightRatio, Math.sin(angle) * radial);
            emitBurst(point, random, i);
        }
    }

    // 从新位置往旧位置沿路径放置短持续发射器，形成魔法弓式拖尾。
    public static void spawnPathTrail(Vec3 origin, Vec3 target, RandomSource random) {
        if (origin == null || target == null) return;
        Vec3 travel = origin.subtract(target);
        double distance = travel.length();
        if (distance < 0.25D) return;

        Vec3 direction = travel.normalize();
        int pointCount = clamp((int) Math.ceil(distance / 2.25D), MIN_TRAIL_POINTS, MAX_TRAIL_POINTS);
        for (int i = 0; i < pointCount; i++) {
            float ratio = pointCount == 1 ? 0.0F : (float) i / (float) (pointCount - 1);
            Vec3 point = target.lerp(origin, ratio);
            point = point.add(random.nextGaussian() * 0.08D, random.nextDouble() * 0.45D, random.nextGaussian() * 0.08D);
            emitTrailEmitter(point, direction, random);
        }
    }

    // 在单个点位提交一个 GPU 粒子 burst。
    public static void emitBurst(Vec3 point, RandomSource random, int index) {
        int startColor = COLORS[random.nextInt(COLORS.length)];
        int endColor = COLORS[random.nextInt(COLORS.length)];
        int burstCount = MIN_BURST_COUNT + random.nextInt(MAX_BURST_COUNT - MIN_BURST_COUNT + 1);
        float size = 0.15F + random.nextFloat() * 0.06F;
        float life = 3.25F + random.nextFloat() * 0.30F;
        float spread = 0.1F + random.nextFloat() * 0.34F;
        float speed = 0.05F + random.nextFloat() * 0.45F;

        Exe.POST.addParticle(new ParticleEmitTask()
                .position(point)
                .direction(0.0F, 1.0F, 0.0F)
                .speed(speed)
                .spread(spread)
                .life(life)
                .gravity(0.1F)
                .size(size, size, random.nextFloat() * 360.0F)
                .color(startColor, 0.95F)
                .endColor(endColor, 0.0F)
                .randomShape(random)
                .motion(index % 2 == 0 ? ParticleEmitTask.MOTION_BALLISTIC : ParticleEmitTask.MOTION_RADIAL_DIFFUSION)
                .radialDiffusion(0.12F + random.nextFloat() * 0.10F, 0.04F, 0.06F)
                .rate(0)
                .duration(0.0F)
                .burst(burstCount));
    }

    // 在路径点位提交一个短持续拖尾发射器。
    public static void emitTrailEmitter(Vec3 point, Vec3 direction, RandomSource random) {
        Vec3 jitteredDirection = jitterDirection(direction, random);
        int startColor = COLORS[random.nextInt(COLORS.length)];
        int endColor = COLORS[random.nextInt(COLORS.length)];
        int burstCount = 6 + random.nextInt(7);
        int rate = 15 + random.nextInt(7);
        float size = 0.08F + random.nextFloat() * 0.08F;
        float life = 1.90F + random.nextFloat() * 0.90F;
        float speed = 1.8F + random.nextFloat() * 0.30F;
        float spread = 0.10F + random.nextFloat() * 0.04F;
        float duration = 1.5F + random.nextFloat() * 0.40F;

        Exe.POST.addParticle(new ParticleEmitTask()
                .position(point)
                .direction((float) jitteredDirection.x, (float) jitteredDirection.y, (float) jitteredDirection.z)
                .speed(speed)
                .spread(spread)
                .life(life)
                .gravity(0.0F)
                .size(size, size, random.nextFloat() * 360.0F)
                .color(startColor, 0.86F)
                .endColor(endColor, 0.20F)
                .randomShape(random)
                .motion(ParticleEmitTask.MOTION_BALLISTIC)
                .rate(rate)
                .duration(duration)
                .burst(burstCount));
    }

    // 给拖尾方向加入轻微扰动，避免持续发射器完全重叠成一条硬线。
    public static Vec3 jitterDirection(Vec3 direction, RandomSource random) {
        Vec3 safeDirection = direction == null || direction.lengthSqr() < 1.0E-6D ? new Vec3(0.0D, 1.0D, 0.0D) : direction;
        Vec3 jitter = new Vec3(
                (random.nextDouble() - 0.5D) * 0.08D,
                (random.nextDouble() - 0.5D) * 0.04D,
                (random.nextDouble() - 0.5D) * 0.08D);
        return safeDirection.add(jitter).normalize();
    }

    // 限制整数范围。
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
