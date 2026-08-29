package org.xiaoyang.ex_enigmaticlegacy.Entity.others.sword;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.xiaoyang.ex_enigmaticlegacy.Util.CameraShakeUtil;

// DimensionSlashClientEffects 负责次元斩客户端非粒子类效果。
public class DimensionSlashClientEffects {
    // 破碎阶段触发一次本地相机抖动。
    public static void tryPlayShake(DimensionSlashDomainEntity entity) {
        if (entity.clientShakePlayed) return;
        if (entity.getAge() < DimensionSlashConfig.FINAL_HIT_TICK) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gameRenderer == null) return;
        Vec3 cameraPos = minecraft.gameRenderer.getMainCamera().getPosition();
        double distanceSqr = cameraPos.distanceToSqr(entity.position());
        double radiusSqr = DimensionSlashConfig.RADIUS * DimensionSlashConfig.RADIUS;
        if (distanceSqr > radiusSqr) return;
        CameraShakeUtil.addShake(entity.position(), (float) DimensionSlashConfig.RADIUS, DimensionSlashConfig.SHAKE_TICKS, DimensionSlashConfig.SHAKE_STRENGTH);
        entity.clientShakePlayed = true;
    }
}
