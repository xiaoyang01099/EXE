package org.xiaoyang.ex_enigmaticlegacy.Entity.others.sword;

import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.system.ParticleEmitTask;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

// BattoSlashParticleEffects 集中提交拔刀斩出场阶段的 GPU 粒子。
public class BattoSlashParticleEffects {
    public static final int SAMPLE_COUNT = 56; // 沿半圆弧采样的发射点数量，当前密度翻倍。
    public static final double PARTICLE_ARC_RADIUS = 25.0D; // 根据实际渲染记录拟合出的粒子半圆半径。
    public static final double[] ROW_FORWARD_OFFSETS = {-1.4D, 0.0D, 1.4D}; // 半圆弧前后加两行后的整体前后偏移。
    public static final double FRONT_OFFSET = 2.2D; // 与拔刀斩渲染一致的前方偏移。
    public static final double HEIGHT_OFFSET = -0.5D; // 与拔刀斩渲染一致的高度偏移。

    // 在拔刀斩第一次渲染时提交一组贴合刀光范围的短生命周期粒子。
    public static void emitAppearanceParticles(BattoSlashEntity entity, float partialTick) {
        if (Exe.POST == null || entity == null) return;
        if (entity.clientAppearanceParticlesEmitted) return;
        if (entity.getProgress(partialTick) > 0.18F) return;
        entity.clientAppearanceParticlesEmitted = true;

        // 使用视觉种子派生粒子随机数，让同一次拔刀斩的粒子分布稳定。
        RandomSource random = RandomSource.create((long) entity.getVisualSeed() * 97L + 0x71A7B477L);
        Vec3 forward = entity.getForward();
        Vec3 side = entity.getTiltedSide();
        Vec3 up = entity.getTiltedUp();
        Vec3 center = entity.position().add(forward.scale(FRONT_OFFSET)).add(0.0D, HEIGHT_OFFSET, 0.0D);

        // 沿倾斜后的半圆弧均匀铺三行点，增加前后厚度和整体粒子数量。
        for (double rowOffset : ROW_FORWARD_OFFSETS) {
            for (int i = 0; i < SAMPLE_COUNT; i++) {
                double t = SAMPLE_COUNT <= 1 ? 0.0D : i / (double) (SAMPLE_COUNT - 1);
                double angle = Math.PI * t;
                double sideAmount = Math.cos(angle) * PARTICLE_ARC_RADIUS;
                double forwardAmount = Math.sin(angle) * PARTICLE_ARC_RADIUS + rowOffset;
                double upJitter = (random.nextDouble() - 0.5D) * 0.15D;
                Vec3 worldPos = center
                        .add(side.scale(sideAmount))
                        .add(forward.scale(forwardAmount));
//                        .add(up.scale(upJitter));
                emitOneBurst(worldPos, side, forward, random);
            }
        }
    }

    // 在指定采样点提交一次短促向上的粒子 burst。
    public static void emitOneBurst(Vec3 worldPos, Vec3 side, Vec3 forward, RandomSource random) {
        float randomSide = (random.nextFloat() - 0.5F) * 0.36F;
        float randomForward = (random.nextFloat() - 0.5F) * 0.36F;
        Vec3 direction = new Vec3(0.0D, 1.0D, 0.0D)
                .add(side.scale(randomSide))
                .add(forward.scale(randomForward));

        Exe.POST.addParticle(new ParticleEmitTask()
                .position(worldPos)
                .direction((float) direction.x, (float) direction.y, (float) direction.z)
                .speed(0.65F + random.nextFloat() * 0.85F)
                .spread(0.88F)
                .life(5.0F)
                .gravity(0.18F)
                .size(
                        0.055F + random.nextFloat() * 0.055F,
                        0.055F + random.nextFloat() * 0.055F,
                        random.nextFloat() * 6.283185F
                )
                .color(0xAA55FF, 0.92F)
                .endColor(0x2F7DFF, 0.0F)
                .randomShape(random)
                .motion(ParticleEmitTask.MOTION_BALLISTIC)
                .burst(15 + random.nextInt(5))
                .duration(0.2F));
    }
}
