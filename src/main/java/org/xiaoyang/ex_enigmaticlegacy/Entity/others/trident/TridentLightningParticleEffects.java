package org.xiaoyang.ex_enigmaticlegacy.Entity.others.trident;

import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.system.ParticleEmitTask;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

// TridentLightningParticleEffects 集中提交天雷战戟落点的 GPU 粒子爆发。
public class TridentLightningParticleEffects {
    public static final int BLUE_START_COLOR = 0xA5D8FF; // 蓝白电光起始颜色。
    public static final int BLUE_END_COLOR = 0x7C4DFF; // 蓝色电光结束颜色。
    public static final int RED_START_COLOR = 0x6666ff; // 引雷红色电光起始颜色。
    public static final int RED_END_COLOR = 0xA5D8FF; // 引雷红紫电光结束颜色。
    public static final int MAGIC_CIRCLE_BURST_COUNT = 500; // 法阵中心主雷爆发粒子数量，先复制战戟强化爆发参数。
    public static final float MAGIC_CIRCLE_BURST_SPEED = 3.18F; // 法阵中心主雷爆发粒子速度。
    public static final float MAGIC_CIRCLE_BURST_SPREAD = 2.45F; // 法阵中心主雷爆发粒子扩散范围。
    public static final float MAGIC_CIRCLE_BURST_LIFE = 2.95F; // 法阵中心主雷爆发粒子生命周期。
    public static final float MAGIC_CIRCLE_BURST_SIZE = 0.22F; // 法阵中心主雷爆发粒子尺寸。

    public TridentLightningParticleEffects() {}

    // 提交落点初始向上爆发粒子，不生成地面扩散粒子。
    public static void emitLandingBurst(Vec3 center, boolean enhanced, long seed) {
        RandomSource random = RandomSource.create(seed);
        int burst = enhanced ? 300 : 200;
        float speed = enhanced ? 2.18F : 1.58F;
        float spread = enhanced ? 2.45F : 2.0F;
        float life = enhanced ? 1.95F : 1.52F;
        float size = enhanced ? 0.12F : 0.09F;
        emitUpwardBurst(center, enhanced, random, burst, speed, spread, life, size);
    }

    // 提交引雷持续期间的小型向上爆发粒子。
    public static void emitSmallStormBurst(Vec3 center, long seed) {
        RandomSource random = RandomSource.create(seed);
        emitUpwardBurst(center, true, random, 300, 1.92F, 3.25F, 1.72F, 0.1F);
    }

    // 提交天雷法阵中心主雷专用爆发粒子，复制战戟强化爆发参数，后续可独立调参。
    public static void emitMagicCircleLandingBurst(Vec3 center, long seed) {
        RandomSource random = RandomSource.create(seed);
        emitUpwardBurst(center, true, random, MAGIC_CIRCLE_BURST_COUNT, MAGIC_CIRCLE_BURST_SPEED,
                MAGIC_CIRCLE_BURST_SPREAD, MAGIC_CIRCLE_BURST_LIFE, MAGIC_CIRCLE_BURST_SIZE);
    }

    // 使用 ParticleEmitTask 提交单次向上爆发，让随机形状粒子从落点向上弹出。
    public static void emitUpwardBurst(Vec3 center, boolean enhanced, RandomSource random, int burst, float speed, float spread, float life, float size) {
        if (Exe.POST == null) return;
        boolean red = enhanced && random.nextFloat() < 0.24F;
        int startColor = red ? RED_START_COLOR : BLUE_START_COLOR;
        int endColor = red ? RED_END_COLOR : BLUE_END_COLOR;


        // 在落点附近略微抬高，避免粒子出生在方块内部。
        Exe.POST.addParticle(new ParticleEmitTask()
                .position(center.add(0.0D, 0.14D, 0.0D))
                .direction(0.0F, 1.0F, 0.0F)
                .speed(speed)
                .spread(spread)
                .life(life)
                .gravity(0.26F)
                .size(size, size, random.nextFloat() * 6.283185F)
                .color(startColor, 1F)
                .endColor(endColor, 1F)
                .randomShape(random)
                .motion(ParticleEmitTask.MOTION_BALLISTIC)
                .burst(burst)
                .duration(0.03F));
    }
}
