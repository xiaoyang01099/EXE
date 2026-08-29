package org.xiaoyang.ex_enigmaticlegacy.Entity.others.bow;

import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.system.ParticleEmitTask;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

import java.util.HashMap;

// MagicBowParticleEffects 统一管理魔法弓客户端粒子效果，避免拖尾逻辑绑死在同步实体里。
public final class MagicBowParticleEffects {
    public static final int[] DEFAULT_GRADIENT_COLORS = {0xA5D8FF, 0x7C4DFF}; // 拖尾和扩散粒子的默认渐变颜色。
    public static final HashMap<Integer, int[]> GRADIENT_COLOR_MAP = new HashMap<>(); // 魔法弓粒子共用渐变色表。

    static {
        GRADIENT_COLOR_MAP.put(0, new int[] {0xA5D8FF, 0x7C4DFF});//蓝紫
        GRADIENT_COLOR_MAP.put(1, new int[] {0xFFD76A, 0x8A55FF});//黄紫
        GRADIENT_COLOR_MAP.put(2, new int[] {0xFFCC00, 0xAF52DE});//黄紫
        GRADIENT_COLOR_MAP.put(3, new int[] {0xF97794, 0x663CA2});//粉紫
        GRADIENT_COLOR_MAP.put(4, new int[] {0x6666ff, 0xDF5F9D});//蓝粉
//        GRADIENT_COLOR_MAP.put(5, new int[] {0xFF0600, 0xFFBB00});//红橙
//        GRADIENT_COLOR_MAP.put(5, new int[] {0xDEE500, 0x2B9B00});//黄绿
//        GRADIENT_COLOR_MAP.put(6, new int[] {0x00EC66, 0x0580DA});//青蓝
        GRADIENT_COLOR_MAP.put(5, new int[] {0x39016C, 0xE295BA});//紫粉
//        GRADIENT_COLOR_MAP.put(9, new int[] {0x48B74A, 0xD5E15A});//绿黄

    }

    // 从渐变色表中取一组颜色，返回副本避免调用方误改全局预设。
    public static int[] randomGradientColors(RandomSource random) {
        if (GRADIENT_COLOR_MAP.isEmpty()) return DEFAULT_GRADIENT_COLORS.clone();
        int key = random.nextInt(GRADIENT_COLOR_MAP.size());
        int[] colors = GRADIENT_COLOR_MAP.get(key);
        return colors == null ? DEFAULT_GRADIENT_COLORS.clone() : colors.clone();
    }

    // 给拖尾基础方向加入轻微扰动，让连续补点不会完全重叠成死板直线。
    public static Vec3 jitterTrailDirection(Vec3 baseDirection, RandomSource random) {
        Vec3 direction = baseDirection;
        if (direction.lengthSqr() < 1.0E-6D) {
            direction = new Vec3(0.0D, 0.0D, 1.0D);
        }
        Vec3 jitter = new Vec3(
                (random.nextDouble() - 0.5D) * 0.08D,
                (random.nextDouble() - 0.5D) * 0.04D,
                (random.nextDouble() - 0.5D) * 0.08D);
        return direction.add(jitter).normalize();
    }

    // 使用旧版魔法箭拖尾参数在指定位置发射一组 GPU 粒子。
    public static void spawnTrail(Vec3 pos, Vec3 baseDirection, int chargeType, RandomSource random, int[] colors) {
        if (Exe.POST == null) return;
        Vec3 direction = jitterTrailDirection(baseDirection, random);
        int startColor = colors == null || colors.length < 2 ? DEFAULT_GRADIENT_COLORS[0] : colors[0];
        int endColor = colors == null || colors.length < 2 ? DEFAULT_GRADIENT_COLORS[1] : colors[1];

        // 这里保留原 MagicBowParticleEffectEntity.spawnTrail 的速度、寿命、大小和数量参数。
        Exe.POST.addParticle(new ParticleEmitTask()
                .position(pos)
                .direction((float) direction.x, (float) direction.y, (float) direction.z)
                .speed(1F)
                .spread(0.12F)
                .life(2.1F)
                .gravity(0.0F)
                .size(chargeType == MagicBowParticleEffectEntity.CHARGE_SUPER ? 0.11F : chargeType == MagicBowParticleEffectEntity.CHARGE_STRONG ? 0.09F : 0.075F,
                        chargeType == MagicBowParticleEffectEntity.CHARGE_SUPER ? 0.11F : chargeType == MagicBowParticleEffectEntity.CHARGE_STRONG ? 0.09F : 0.075F,
                        random.nextFloat() * 6.28F)
                .color(startColor, 0.95F)
                .endColor(endColor, 0.65F)
                .randomShape(random)
                .motion(ParticleEmitTask.MOTION_BALLISTIC)
                .rate(chargeType == MagicBowParticleEffectEntity.CHARGE_SUPER ? 18 : chargeType == MagicBowParticleEffectEntity.CHARGE_STRONG ? 14 : 12)
                .duration(1.2F)
                .burst(chargeType == MagicBowParticleEffectEntity.CHARGE_SUPER ? 18 : chargeType == MagicBowParticleEffectEntity.CHARGE_STRONG ? 14 : 10));
    }
}
