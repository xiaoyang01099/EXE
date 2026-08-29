package org.xiaoyang.ex_enigmaticlegacy.Entity.others.trident;

import net.minecraft.world.phys.Vec3;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

// HeavenlyThunderCloudRingEffects 负责天雷法阵横向旋转云环的提交参数和入口。
public class HeavenlyThunderCloudRingEffects {
    public static final long CLOUD_RING_SEED_SALT = 0x5A10C10DL; // 天雷云环视觉随机盐值。

    public HeavenlyThunderCloudRingEffects() {
    }

    // 提交持续到天雷技能结束的横向旋转云环，真正渲染由 SmokeParticleQueue 负责。
    public static void submit(HeavenlyThunderEntity entity) {
        if (Exe.POST == null || entity == null) return;
        Vec3 center = entity.skyCircleCenter();
        float lifeTime = Math.max(0.1F, (HeavenlyThunderEntity.LIFE_TICKS - 1) / 20.0F);
        Exe.POST.effects().addHeavenlyThunderCloudRing(center, entity.getVisualSeed(0, CLOUD_RING_SEED_SALT), lifeTime);
    }
}
