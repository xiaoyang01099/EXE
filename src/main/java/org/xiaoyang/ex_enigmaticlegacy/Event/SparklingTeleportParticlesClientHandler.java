package org.xiaoyang.ex_enigmaticlegacy.Event;

import net.minecraft.world.phys.Vec3;
import org.xiaoyang.ex_enigmaticlegacy.Client.particle.SparklingTeleportParticles;

// SparklingTeleportParticlesClientHandler 只在客户端处理闪闪果实瞬移粒子通知。
public class SparklingTeleportParticlesClientHandler {
    // 根据网络包同步的位置和尺寸发射本地 GPU 粒子。
    public static void spawn(double x, double y, double z, double targetX, double targetY, double targetZ, float height, float width) {
        SparklingTeleportParticles.spawn(new Vec3(x, y, z), new Vec3(targetX, targetY, targetZ), height, width);
    }
}
