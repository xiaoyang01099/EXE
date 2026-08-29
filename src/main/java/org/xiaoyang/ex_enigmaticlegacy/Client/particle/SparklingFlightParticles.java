package org.xiaoyang.ex_enigmaticlegacy.Client.particle;

import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.system.ParticleEmitTask;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

// SparklingFlightParticles 沿闪闪果实加速飞行历史路径发射金黄色 GPU 粒子。
public class SparklingFlightParticles {
    public static final int[] GOLD_COLORS = {0xFFF59D, 0xFFD740, 0xFFC107, 0xFFB300}; // Ctrl 飞行拖尾专用金黄色调色板。
    public static final RandomSource RANDOM = RandomSource.create(); // 客户端粒子尺寸、颜色和扰动随机源。
    public static final int MIN_BURST_COUNT = 2; // 单个历史采样点最少粒子数量。
    public static final int RANDOM_BURST_COUNT = 3; // 单个历史采样点额外随机粒子数量范围，实际为两到四粒。

    // 在单个历史位置提交一小批沿飞行反方向扩散的 GPU 粒子。
    public static void emit(Vec3 position, Vec3 backwardDirection) {
        if (Exe.POST == null || position == null) return;
        Vec3 safeBackward = backwardDirection == null || backwardDirection.lengthSqr() < 1.0E-8D
                ? new Vec3(0.0D, 0.0D, -1.0D)
                : backwardDirection.normalize();
        Vec3 jitteredPosition = position.add(
                (RANDOM.nextDouble() - 0.5D) * 0.18D,
                (RANDOM.nextDouble() - 0.5D) * 0.22D,
                (RANDOM.nextDouble() - 0.5D) * 0.18D);
        int startColor = GOLD_COLORS[RANDOM.nextInt(GOLD_COLORS.length)];
        int endColor = GOLD_COLORS[RANDOM.nextInt(GOLD_COLORS.length)];
        float size = 0.10F + RANDOM.nextFloat() * 0.12F;
        int burstCount = MIN_BURST_COUNT + RANDOM.nextInt(RANDOM_BURST_COUNT);

        // 使用短寿命、零重力和轻微反向速度形成不会遮住主体的发光碎屑轨迹。
        Exe.POST.addParticle(new ParticleEmitTask()
                .position(jitteredPosition)
                .direction((float) safeBackward.x, (float) safeBackward.y, (float) safeBackward.z)
                .speed(0.10F + RANDOM.nextFloat() * 0.20F)
                .spread(0.14F + RANDOM.nextFloat() * 0.12F)
                .life(2.90F + RANDOM.nextFloat() * 1.70F)
                .gravity(0.0F)
                .size(size, size, RANDOM.nextFloat() * 360.0F)
                .color(startColor, 0.92F)
                .endColor(endColor, 0.0F)
                .randomShape(RANDOM)
                .motion(ParticleEmitTask.MOTION_BALLISTIC)
                .rate(0)
                .duration(0.0F)
                .burst(burstCount));
    }
}
