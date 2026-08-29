package org.xiaoyang.ex_enigmaticlegacy.Util;

import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.system.ParticleEmitTask;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

/**
 * 粒子效果模板类，提供常见粒子特效的静态方法。
 * 调用方可根据需要传入颜色等参数进行定制。
 */
public class ParticleTemplate {

    private static final int[] ALL_SHAPES = {
            ParticleEmitTask.SHAPE_CIRCLE,
            ParticleEmitTask.SHAPE_SQUARE,
            ParticleEmitTask.SHAPE_TRIANGLE,
            ParticleEmitTask.SHAPE_HEART,
            ParticleEmitTask.SHAPE_STAR
    };

    /**
     * 对 3D 向量应用欧拉旋转（XYZ 顺序）。
     *
     * @param vec  待旋转的向量（相对偏移量）
     * @param rotX X 轴旋转角度（度）
     * @param rotY Y 轴旋转角度（度）
     * @param rotZ Z 轴旋转角度（度）
     * @return 旋转后的向量
     */
    public static Vec3 rotateEuler(Vec3 vec, float rotX, float rotY, float rotZ) {
        float rx = (float) vec.x;
        float ry = (float) vec.y;
        float rz = (float) vec.z;

        float radX = (float) Math.toRadians(rotX);
        float radY = (float) Math.toRadians(rotY);
        float radZ = (float) Math.toRadians(rotZ);

        float cosX = (float) Math.cos(radX), sinX = (float) Math.sin(radX);
        float cosY = (float) Math.cos(radY), sinY = (float) Math.sin(radY);
        float cosZ = (float) Math.cos(radZ), sinZ = (float) Math.sin(radZ);

        // XYZ 顺序：先绕 X 轴，再绕 Y 轴，最后绕 Z 轴
        // 绕 X 旋转
        float x1 = rx;
        float y1 = ry * cosX - rz * sinX;
        float z1 = ry * sinX + rz * cosX;

        // 绕 Y 旋转
        float x2 = x1 * cosY + z1 * sinY;
        float y2 = y1;
        float z2 = -x1 * sinY + z1 * cosY;

        // 绕 Z 旋转
        float x3 = x2 * cosZ - y2 * sinZ;
        float y3 = x2 * sinZ + y2 * cosZ;
        float z3 = z2;

        return new Vec3(x3, y3, z3);
    }

    /**
     * 三角形全连接粒子效果 — 每个顶点向其他两个顶点持续发射粒子。
     * 共 6 个发射器：v0→v1、v0→v2、v1→v0、v1→v2、v2→v0、v2→v1，
     * 每条边有双向粒子流。每个发射器从所有形状中随机选择一种。
     *
     * @param center        点击的方块位置
     * @param sideLength 三角形边长（格）
     * @param startRgb   粒子起始颜色（如 0xFF4444）
     * @param endRgb     粒子结束颜色（如 0x888888）
     * @param rotX       三角形绕 X 轴旋转角度（度）
     * @param rotY       三角形绕 Y 轴旋转角度（度）
     * @param rotZ       三角形绕 Z 轴旋转角度（度）
     * @param travelTime 粒子从起点飞到终点所需时间（秒），同时也是粒子寿命
     * @param life       单位秒, 粒子生命周期
     * @param random     随机数实例，用于随机选择形状
     */
    public static void emitTriangleFullConnect(Vec3 center, float sideLength,
                                               int startRgb, int endRgb,
                                               float rotX, float rotY, float rotZ,
                                               float travelTime,float life, RandomSource random) {
//        Vec3 center = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        float height = (float) (sideLength * Math.sqrt(3) / 2.0);

        // 等边三角形的三个相对偏移量（水平XZ平面，一个顶点朝下）
        Vec3[] relVertices = {
                new Vec3(0, 0, -height * 2.0f / 3.0f),
                new Vec3(-sideLength / 2.0f, 0, height / 3.0f),
                new Vec3(sideLength / 2.0f, 0, height / 3.0f)
        };

        // 应用旋转后得到世界坐标顶点（rotXYZ 全为 0 时跳过旋转计算）
        Vec3[] vertices = new Vec3[3];
        boolean needRotate = rotX != 0f || rotY != 0f || rotZ != 0f;
        for (int i = 0; i < 3; i++) {
            Vec3 rel = relVertices[i];
            vertices[i] = needRotate ? center.add(rotateEuler(rel, rotX, rotY, rotZ))
                                     : new Vec3(center.x + rel.x, center.y + rel.y, center.z + rel.z);
        }

        for (int i = 0; i < 3; i++) {
            Vec3 from = vertices[i];
            for (int j = 0; j < 3; j++) {
                if (i == j) continue;

                Vec3 to = vertices[j];
                float dx = (float) (to.x - from.x);
                float dy = (float) (to.y - from.y);
                float dz = (float) (to.z - from.z);
                float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
                float speed = len / travelTime;

                // 每个发射器从所有形状中随机选择一种
                int shape = ALL_SHAPES[random.nextInt(ALL_SHAPES.length)];

                Exe.POST.addParticle(new ParticleEmitTask()
                        .position(from)
                        .direction(dx, dy, dz)
                        .speed(speed)
                        .spread(0.02f)
                        .life(travelTime)
                        .gravity(0f)
                        .size(0.08f, 0.08f, 0f)
                        .color(startRgb, 1.0f)
                        .endColor(endRgb, 0.8f)
                        .shape(shape)
                        .motion(ParticleEmitTask.MOTION_BALLISTIC)
                        .rate((int) (30 * sideLength))
                        .duration(life));
            }
        }
    }

    // 生成水平向外扩散的通用粒子环，供箭落地、流星落点、光束扩散和最终爆炸复用。
    public static void emitGroundDiffusion(Vec3 center, float radius, int burst, float lifeSeconds,
                                           float size, int startRgb, int endRgb, RandomSource random) {
        if (Exe.POST == null || radius <= 0.0F || burst <= 0) return;

        int ringCount = Math.max(1, Math.min(3, (int) Math.ceil(radius / 5.0F))); // 大范围扩散拆成多层圆环，避免外圈缺口。
        for (int ringIndex = 0; ringIndex < ringCount; ringIndex++) {
            int pointsInRing = Math.max(1, (burst + ringCount - 1 - ringIndex) / ringCount);
            float ringScale = ringCount == 1 ? 1.0F : 0.58F + 0.42F * ringIndex / (ringCount - 1);
            float radiusJitter = 0.92F + random.nextFloat() * 0.14F;
            float speed = radius * ringScale * radiusJitter / Math.max(0.35F, lifeSeconds * 0.72F);
            float particleSize = size * (0.82F + random.nextFloat() * 0.36F);
            float angleJitter = (float) Math.PI * 2.0F / Math.max(14.0F, pointsInRing * 1.85F);
            float verticalSpeed = 0.055F + random.nextFloat() * 0.055F;
            float verticalJitter = 0.06F + random.nextFloat() * 0.05F;

            // 每层圆环只提交一个 GPU 径向扩散任务，兼顾旧版外炸观感和大数量流星性能。
            Exe.POST.addParticle(new ParticleEmitTask()
                    .position(center.add(0.0D, 0.045D + random.nextDouble() * 0.045D, 0.0D))
                    .direction(0.0F, 1.0F, 0.0F)
                    .speed(speed)
                    .spread(angleJitter)
                    .life(lifeSeconds)
                    .gravity(0.02F)
                    .size(particleSize, particleSize, random.nextFloat() * 6.28F)
                    .color(startRgb, 0.95F)
                    .endColor(endRgb, 0.0F)
                    .randomShape(random)
                    .motion(ParticleEmitTask.MOTION_RADIAL_DIFFUSION)
                    .radialDiffusion(0.16F + radius * 0.018F, verticalSpeed, verticalJitter)
                    .rate(0)
                    .duration(0.0F)
                    .burst(pointsInRing));
        }
    }

}
