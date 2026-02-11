package net.xiaoyang010.ex_enigmaticlegacy.Client.particle;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import vazkii.botania.client.fx.WispParticleData;

import java.util.ArrayList;
import java.util.List;

public class StarlitCraftingParticles {
    private static final BlockPos[] PILLAR_POSITIONS = {
            new BlockPos(-4, 7, -4),
            new BlockPos(-4, 7, 4),
            new BlockPos(4, 7, -4),
            new BlockPos(4, 7, 4)
    };

    /**
     * 生成从柱子流向中心的粒子 - 增强版
     */
    public static void spawnPillarToCenterParticles(ServerLevel level, BlockPos center, int tick) {
        for (BlockPos pillarOffset : PILLAR_POSITIONS) {
            BlockPos pillarTop = center.offset(pillarOffset);

            Vec3 start = Vec3.atCenterOf(pillarTop);
            Vec3 end = Vec3.atCenterOf(center);

            for (int stream = 0; stream < 5; stream++) {
                float progress = ((tick + stream * 8) % 40) / 40.0f;
                Vec3 particlePos = start.lerp(end, progress);

                double angle = progress * Math.PI * 4 + stream * Math.PI / 2.5;
                double radius = 0.3 * (1 - progress);

                double offsetX = Math.cos(angle) * radius;
                double offsetZ = Math.sin(angle) * radius;

                float r = 0.2f + progress * 0.8f;
                float g = 0.5f + progress * 0.5f;
                float b = 1.0f;

                WispParticleData data = WispParticleData.wisp(0.5F, r, g, b);
                level.sendParticles(data,
                        particlePos.x + offsetX,
                        particlePos.y,
                        particlePos.z + offsetZ,
                        1, 0, 0, 0, 0);

                if (stream % 2 == 0) {
                    double innerRadius = radius * 0.5;
                    double innerOffsetX = Math.cos(angle) * innerRadius;
                    double innerOffsetZ = Math.sin(angle) * innerRadius;

                    WispParticleData innerData = WispParticleData.wisp(0.35F, r * 1.2f, g * 1.2f, b);
                    level.sendParticles(innerData,
                            particlePos.x + innerOffsetX,
                            particlePos.y,
                            particlePos.z + innerOffsetZ,
                            1, 0, 0, 0, 0);
                }

                if (tick % 2 == 0 && progress > 0.3f) {
                    WispParticleData sparkData = WispParticleData.wisp(0.4F, 1.0f, 1.0f, 1.0f);
                    level.sendParticles(sparkData,
                            particlePos.x + offsetX,
                            particlePos.y,
                            particlePos.z + offsetZ,
                            1, 0, 0, 0, 0);
                }

                if (progress > 0.1f) {
                    Vec3 trailPos = start.lerp(end, progress - 0.1f);
                    double trailAngle = (progress - 0.1f) * Math.PI * 4 + stream * Math.PI / 2.5;
                    double trailRadius = 0.3 * (1 - (progress - 0.1f));

                    double trailOffsetX = Math.cos(trailAngle) * trailRadius;
                    double trailOffsetZ = Math.sin(trailAngle) * trailRadius;

                    WispParticleData trailData = WispParticleData.wisp(0.3F, r * 0.7f, g * 0.7f, b * 0.9f);
                    level.sendParticles(trailData,
                            trailPos.x + trailOffsetX,
                            trailPos.y,
                            trailPos.z + trailOffsetZ,
                            1, 0, 0, 0, 0);
                }
            }

            for (int straight = 0; straight < 3; straight++) {
                float straightProgress = ((tick + straight * 13) % 40) / 40.0f;
                Vec3 straightPos = start.lerp(end, straightProgress);

                double randomOffsetX = (Math.random() - 0.5) * 0.2;
                double randomOffsetZ = (Math.random() - 0.5) * 0.2;

                float brightness = 0.6f + straightProgress * 0.4f;
                WispParticleData straightData = WispParticleData.wisp(0.4F,
                        0.3f * brightness, 0.6f * brightness, 1.0f * brightness);
                level.sendParticles(straightData,
                        straightPos.x + randomOffsetX,
                        straightPos.y,
                        straightPos.z + randomOffsetZ,
                        1, 0, 0, 0, 0);
            }

            if (tick % 5 == 0) {
                for (int i = 0; i < 8; i++) {
                    double topAngle = i * Math.PI / 4;
                    double topRadius = 0.4;

                    double topX = start.x + Math.cos(topAngle) * topRadius;
                    double topZ = start.z + Math.sin(topAngle) * topRadius;

                    WispParticleData topData = WispParticleData.wisp(0.5F, 0.4f, 0.7f, 1.0f);
                    level.sendParticles(topData, topX, start.y, topZ, 1, 0, 0, 0, 0);
                }
            }

            if (tick % 3 == 0) {
                for (int i = 0; i < 6; i++) {
                    double centerAngle = i * Math.PI / 3 + tick * 0.1;
                    double centerRadius = 0.3;

                    double centerX = end.x + Math.cos(centerAngle) * centerRadius;
                    double centerZ = end.z + Math.sin(centerAngle) * centerRadius;

                    WispParticleData centerData = WispParticleData.wisp(0.6F, 0.8f, 0.9f, 1.0f);
                    level.sendParticles(centerData, centerX, end.y, centerZ, 1, 0, 0, 0, 0);
                }
            }
        }
    }

    /**
     * 生成围绕中心旋转的粒子
     */
    public static void spawnCirclingParticles(ServerLevel level, BlockPos center, int tick) {
        Vec3 centerPos = Vec3.atCenterOf(center);

        for (int layer = 0; layer < 3; layer++) {
            double radius = 1.0 + layer * 0.4;
            double height = layer * 0.4;
            int particleCount = 12 + layer * 4;

            float r = layer == 0 ? 0.2f : layer == 1 ? 0.5f : 1.0f;
            float g = layer == 0 ? 0.5f : layer == 1 ? 0.8f : 1.0f;
            float b = layer == 0 ? 1.0f : layer == 1 ? 1.0f : 0.5f;

            for (int i = 0; i < particleCount; i++) {
                double angle = (tick * 0.08 + (i * 2 * Math.PI / particleCount) + layer * Math.PI / 3) % (2 * Math.PI);

                double x = centerPos.x + Math.cos(angle) * radius;
                double y = centerPos.y + height;
                double z = centerPos.z + Math.sin(angle) * radius;

                WispParticleData data = WispParticleData.wisp(0.4F, r, g, b);
                level.sendParticles(data, x, y, z, 1, 0, 0, 0, 0);
            }
        }

        if (tick % 20 == 0) {
            for (int i = 0; i < 16; i++) {
                double angle = i * 2 * Math.PI / 16;
                double pulseRadius = 0.3;

                double x = centerPos.x + Math.cos(angle) * pulseRadius;
                double z = centerPos.z + Math.sin(angle) * pulseRadius;

                WispParticleData data = WispParticleData.wisp(0.6F, 1.0f, 1.0f, 0.5f);
                level.sendParticles(data, x, centerPos.y, z, 2, 0, 0, 0, 0);
            }
        }
    }

    /**
     * 生成从柱子向下流动的粒子
     */
    public static void spawnPillarDownwardParticles(ServerLevel level, BlockPos center, int tick) {
        for (BlockPos pillarOffset : PILLAR_POSITIONS) {
            BlockPos pillarTop = center.offset(pillarOffset);
            double pillarHeight = 7.0;

            for (int stream = 0; stream < 8; stream++) {
                double angleOffset = stream * Math.PI / 4;
                double streamRadius = 0.3 + (stream % 2) * 0.15;

                double baseX = pillarTop.getX() + 0.5 + Math.cos(angleOffset) * streamRadius;
                double baseZ = pillarTop.getZ() + 0.5 + Math.sin(angleOffset) * streamRadius;

                for (int segment = 0; segment < 12; segment++) {
                    double progress = ((tick + stream * 5 + segment * 5) % 60) / 60.0;

                    double y = pillarTop.getY() - progress * pillarHeight;
                    double swayAngle = tick * 0.05 + segment * 0.3;
                    double swayAmount = 0.1 * Math.sin(progress * Math.PI);

                    double x = baseX + Math.cos(swayAngle) * swayAmount;
                    double z = baseZ + Math.sin(swayAngle) * swayAmount;

                    float brightness = 1.0f - (float)progress * 0.4f;
                    float r = 0.3f * brightness;
                    float g = 0.7f * brightness;
                    float b = 1.0f * brightness;

                    WispParticleData data = WispParticleData.wisp(0.4F, r, g, b);
                    level.sendParticles(data, x, y, z, 1, 0, 0, 0, 0);

                    if (segment % 3 == 0 && Math.random() < 0.3) {
                        double splashX = x + (Math.random() - 0.5) * 0.15;
                        double splashZ = z + (Math.random() - 0.5) * 0.15;

                        WispParticleData splashData = WispParticleData.wisp(0.25F,
                                r * 1.2f, g * 1.2f, b * 1.2f);
                        level.sendParticles(splashData, splashX, y, splashZ, 1, 0, 0, 0, 0);
                    }
                }
            }

            if (tick % 2 == 0) {
                for (int i = 0; i < 12; i++) {
                    double sourceAngle = i * Math.PI / 6;
                    double sourceRadius = 0.4;

                    double sourceX = pillarTop.getX() + 0.5 + Math.cos(sourceAngle) * sourceRadius;
                    double sourceZ = pillarTop.getZ() + 0.5 + Math.sin(sourceAngle) * sourceRadius;

                    WispParticleData sourceData = WispParticleData.wisp(0.5F, 0.4f, 0.8f, 1.0f);
                    level.sendParticles(sourceData, sourceX, pillarTop.getY() + 0.2, sourceZ, 1, 0, 0, 0, 0);
                }
            }

            BlockPos groundPos = center.below();
            double groundY = groundPos.getY() + 0.1;

            if (tick % 3 == 0) {
                for (int splash = 0; splash < 16; splash++) {
                    double splashAngle = splash * Math.PI / 8 + tick * 0.1;
                    double splashRadius = 0.3 + Math.random() * 0.4;

                    double splashX = pillarTop.getX() + 0.5 + Math.cos(splashAngle) * splashRadius;
                    double splashZ = pillarTop.getZ() + 0.5 + Math.sin(splashAngle) * splashRadius;

                    WispParticleData groundSplash = WispParticleData.wisp(0.35F, 0.5f, 0.9f, 1.0f);
                    level.sendParticles(groundSplash, splashX, groundY, splashZ, 1, 0, 0, 0, 0);
                }
            }

            if (tick % 4 == 0) {
                for (int mist = 0; mist < 6; mist++) {
                    double mistHeight = Math.random() * pillarHeight;
                    double mistAngle = Math.random() * Math.PI * 2;
                    double mistRadius = 0.5 + Math.random() * 0.3;

                    double mistX = pillarTop.getX() + 0.5 + Math.cos(mistAngle) * mistRadius;
                    double mistY = pillarTop.getY() - mistHeight;
                    double mistZ = pillarTop.getZ() + 0.5 + Math.sin(mistAngle) * mistRadius;

                    WispParticleData mistData = WispParticleData.wisp(0.3F, 0.6f, 0.8f, 1.0f);
                    level.sendParticles(mistData, mistX, mistY, mistZ, 1,
                            (float)(Math.random() - 0.5) * 0.02f,
                            (float)(Math.random() - 0.5) * 0.02f,
                            (float)(Math.random() - 0.5) * 0.02f,
                            0);
                }
            }

            if (tick % 10 == 0) {
                for (int ripple = 0; ripple < 3; ripple++) {
                    int ripplePoints = 16;
                    double rippleRadius = 0.5 + ripple * 0.4;

                    for (int i = 0; i < ripplePoints; i++) {
                        double rippleAngle = i * 2 * Math.PI / ripplePoints;

                        double rippleX = pillarTop.getX() + 0.5 + Math.cos(rippleAngle) * rippleRadius;
                        double rippleZ = pillarTop.getZ() + 0.5 + Math.sin(rippleAngle) * rippleRadius;

                        float rippleBrightness = 1.0f - ripple * 0.3f;
                        WispParticleData rippleData = WispParticleData.wisp(0.3F,
                                0.4f * rippleBrightness,
                                0.7f * rippleBrightness,
                                1.0f * rippleBrightness);
                        level.sendParticles(rippleData, rippleX, groundY, rippleZ, 1, 0, 0, 0, 0);
                    }
                }
            }

            for (int main = 0; main < 15; main++) {
                double mainProgress = ((tick + main * 4) % 60) / 60.0;
                double mainY = pillarTop.getY() - mainProgress * pillarHeight;

                double mainX = pillarTop.getX() + 0.5 + (Math.random() - 0.5) * 0.15;
                double mainZ = pillarTop.getZ() + 0.5 + (Math.random() - 0.5) * 0.15;

                float mainBrightness = 1.0f - (float)mainProgress * 0.3f;
                WispParticleData mainData = WispParticleData.wisp(0.5F,
                        0.3f * mainBrightness,
                        0.8f * mainBrightness,
                        1.0f * mainBrightness);
                level.sendParticles(mainData, mainX, mainY, mainZ, 1, 0, 0, 0, 0);

                if (main % 2 == 0) {
                    WispParticleData glowData = WispParticleData.wisp(0.6F,
                            0.5f * mainBrightness,
                            0.9f * mainBrightness,
                            1.0f * mainBrightness);
                    level.sendParticles(glowData, mainX, mainY, mainZ, 1, 0, 0, 0, 0);
                }
            }
        }
    }

    /**
     * 生成六芒星阵
     */
    public static void spawnHexagramParticles(ServerLevel level, BlockPos center, int tick) {
        Vec3 centerPos = Vec3.atCenterOf(center.below());
        double radius = 2.5;

        List<Vec3> points = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            double angle = i * 2 * Math.PI / 3;
            points.add(new Vec3(
                    centerPos.x + Math.cos(angle) * radius,
                    centerPos.y + 0.05,
                    centerPos.z + Math.sin(angle) * radius
            ));
        }

        for (int i = 0; i < 3; i++) {
            double angle = i * 2 * Math.PI / 3 + Math.PI;
            points.add(new Vec3(
                    centerPos.x + Math.cos(angle) * radius,
                    centerPos.y + 0.05,
                    centerPos.z + Math.sin(angle) * radius
            ));
        }

        drawParticleLine(level, points.get(0), points.get(2), 25, 0.8f, 0.3f, 1.0f);
        drawParticleLine(level, points.get(2), points.get(4), 25, 0.8f, 0.3f, 1.0f);
        drawParticleLine(level, points.get(4), points.get(0), 25, 0.8f, 0.3f, 1.0f);

        drawParticleLine(level, points.get(1), points.get(3), 25, 0.8f, 0.3f, 1.0f);
        drawParticleLine(level, points.get(3), points.get(5), 25, 0.8f, 0.3f, 1.0f);
        drawParticleLine(level, points.get(5), points.get(1), 25, 0.8f, 0.3f, 1.0f);

        for (int i = 0; i < 6; i++) {
            Vec3 point = points.get(i);

            WispParticleData coreData = WispParticleData.wisp(0.6F, 1.0f, 0.8f, 0.3f);
            level.sendParticles(coreData, point.x, point.y + 0.1, point.z, 1, 0, 0, 0, 0);

            if (tick % 20 == i * 3) {
                for (int j = 0; j < 8; j++) {
                    double smallAngle = j * Math.PI / 4;
                    double smallRadius = 0.15;
                    double px = point.x + Math.cos(smallAngle) * smallRadius;
                    double pz = point.z + Math.sin(smallAngle) * smallRadius;

                    WispParticleData pulseData = WispParticleData.wisp(0.4F, 1.0f, 1.0f, 0.5f);
                    level.sendParticles(pulseData, px, point.y + 0.15, pz, 1, 0, 0, 0, 0);
                }
            }
        }

        WispParticleData centerData = WispParticleData.wisp(0.7F, 0.9f, 0.5f, 1.0f);
        level.sendParticles(centerData, centerPos.x, centerPos.y + 0.1, centerPos.z, 1, 0, 0, 0, 0);

        double runeAngle = tick * 0.03;
        for (int i = 0; i < 6; i++) {
            double angle = runeAngle + i * Math.PI / 3;
            double runeRadius = radius * 0.6;

            double x = centerPos.x + Math.cos(angle) * runeRadius;
            double z = centerPos.z + Math.sin(angle) * runeRadius;

            WispParticleData data = WispParticleData.wisp(0.5F, 1.0f, 0.8f, 0.3f);
            level.sendParticles(data, x, centerPos.y + 0.2, z, 1, 0, 0, 0, 0);
        }

        double innerRuneAngle = -tick * 0.04;
        for (int i = 0; i < 6; i++) {
            double angle = innerRuneAngle + i * Math.PI / 3;
            double innerRadius = radius * 0.35;

            double x = centerPos.x + Math.cos(angle) * innerRadius;
            double z = centerPos.z + Math.sin(angle) * innerRadius;

            WispParticleData data = WispParticleData.wisp(0.4F, 0.5f, 1.0f, 0.8f);
            level.sendParticles(data, x, centerPos.y + 0.15, z, 1, 0, 0, 0, 0);
        }

        if (tick % 2 == 0) {
            int circlePoints = 36;
            double outerRadius = radius * 1.1;
            for (int i = 0; i < circlePoints; i++) {
                double angle = i * 2 * Math.PI / circlePoints;
                double x = centerPos.x + Math.cos(angle) * outerRadius;
                double z = centerPos.z + Math.sin(angle) * outerRadius;

                if (Math.random() < 0.3) {
                    WispParticleData data = WispParticleData.wisp(0.3F, 0.6f, 0.2f, 0.9f);
                    level.sendParticles(data, x, centerPos.y + 0.05, z, 1, 0, 0, 0, 0);
                }
            }
        }

        if (tick % 30 == 0) {
            for (int wave = 0; wave < 3; wave++) {
                int wavePoints = 24;
                double waveRadius = radius * 0.2 * (wave + 1);

                for (int i = 0; i < wavePoints; i++) {
                    double angle = i * 2 * Math.PI / wavePoints;
                    double x = centerPos.x + Math.cos(angle) * waveRadius;
                    double z = centerPos.z + Math.sin(angle) * waveRadius;

                    float brightness = 1.0f - wave * 0.25f;
                    WispParticleData data = WispParticleData.wisp(0.5F,
                            brightness, brightness * 0.7f, brightness);
                    level.sendParticles(data, x, centerPos.y + 0.2, z, 1, 0, 0, 0, 0);
                }
            }
        }
    }

    /**
     * 绘制粒子线
     */
    private static void drawParticleLine(ServerLevel level, Vec3 start, Vec3 end, int points,
                                         float r, float g, float b) {
        for (int i = 0; i <= points; i++) {
            double progress = i / (double) points;
            Vec3 pos = start.lerp(end, progress);

            WispParticleData data = WispParticleData.wisp(0.35F, r, g, b);
            level.sendParticles(data, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);

            if (i % 3 == 0) {
                WispParticleData brightData = WispParticleData.wisp(0.45F,
                        Math.min(1.0f, r * 1.2f),
                        Math.min(1.0f, g * 1.2f),
                        Math.min(1.0f, b * 1.2f));
                level.sendParticles(brightData, pos.x, pos.y + 0.05, pos.z, 1, 0, 0, 0, 0);
            }
        }
    }

    /**
     * 完成合成时的爆发效果
     */
    public static void spawnCompletionBurst(ServerLevel level, BlockPos center) {
        Vec3 centerPos = Vec3.atCenterOf(center);

        for (int i = 0; i < 80; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double radius = Math.random() * 0.8;
            double height = Math.random() * 6;

            double x = centerPos.x + Math.cos(angle) * radius;
            double y = centerPos.y + height;
            double z = centerPos.z + Math.sin(angle) * radius;

            WispParticleData data = WispParticleData.wisp(0.6F, 0.5f, 1.0f, 1.0f);
            level.sendParticles(data, x, y, z, 1, 0, 0, 0, 0);
        }

        for (int ring = 0; ring < 5; ring++) {
            double ringRadius = 0.5 + ring * 0.6;
            int points = 24 + ring * 6;

            for (int i = 0; i < points; i++) {
                double angle = i * 2 * Math.PI / points;

                double x = centerPos.x + Math.cos(angle) * ringRadius;
                double z = centerPos.z + Math.sin(angle) * ringRadius;

                float brightness = 1.0f - ring * 0.15f;
                WispParticleData data = WispParticleData.wisp(0.7F, brightness, brightness * 0.8f, brightness);
                level.sendParticles(data, x, centerPos.y, z, 2, 0, 0, 0, 0);
            }
        }

        for (int i = 0; i < 150; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double pitch = (Math.random() - 0.5) * Math.PI;
            double distance = 0.5 + Math.random() * 3.0;

            double x = centerPos.x + Math.cos(angle) * Math.cos(pitch) * distance;
            double y = centerPos.y + Math.sin(pitch) * distance;
            double z = centerPos.z + Math.sin(angle) * Math.cos(pitch) * distance;

            float hue = (float) Math.random();
            float r = hue < 0.33f ? 1.0f : hue < 0.66f ? 0.5f : 1.0f;
            float g = hue < 0.33f ? 0.5f : hue < 0.66f ? 1.0f : 0.5f;
            float b = hue < 0.33f ? 1.0f : hue < 0.66f ? 1.0f : 0.5f;

            WispParticleData data = WispParticleData.wisp(0.5F, r, g, b);
            level.sendParticles(data, x, y, z, 1, 0, 0, 0, 0);
        }

        for (int i = 0; i < 30; i++) {
            WispParticleData data = WispParticleData.wisp(1.0F, 1.0f, 1.0f, 1.0f);
            level.sendParticles(data, centerPos.x, centerPos.y, centerPos.z, 3, 0.1, 0.1, 0.1, 0.05);
        }
    }

    /**
     * 合成进行中的持续效果
     */
    public static void spawnCraftingProgressParticles(ServerLevel level, BlockPos center, int tick, float progress) {
        Vec3 centerPos = Vec3.atCenterOf(center);

        int totalPoints = 48;
        int visiblePoints = (int) (totalPoints * progress);
        double radius = 2.0;

        for (int i = 0; i < visiblePoints; i++) {
            double angle = i * 2 * Math.PI / totalPoints;
            double x = centerPos.x + Math.cos(angle) * radius;
            double z = centerPos.z + Math.sin(angle) * radius;

            float r = progress;
            float g = 1.0f - progress * 0.5f;
            float b = 1.0f;

            WispParticleData data = WispParticleData.wisp(0.4F, r, g, b);
            level.sendParticles(data, x, centerPos.y - 1 + 0.05, z, 1, 0, 0, 0, 0);
        }

        if (tick % 3 == 0) {
            double indicatorAngle = progress * 2 * Math.PI;
            double x = centerPos.x + Math.cos(indicatorAngle) * radius;
            double z = centerPos.z + Math.sin(indicatorAngle) * radius;

            for (int i = 0; i < 5; i++) {
                WispParticleData data = WispParticleData.wisp(0.6F, 1.0f, 1.0f, 0.5f);
                level.sendParticles(data, x, centerPos.y - 1 + 0.3 + i * 0.3, z, 1, 0, 0, 0, 0);
            }
        }
    }

    /**
     * 生成竞技场边界粒子
     */
    public static void spawnArenaRingParticles(ServerLevel level, BlockPos source, int tick) {
        for (int i = 0; i < 360; i += 8) {
            float r = 0.6F;
            float g = 0F;
            float b = 0.2F;

            float rad = i * (float) Math.PI / 180F;
            double x = source.getX() + 0.5 - Math.cos(rad) * 12; // ARENA_RANGE = 12
            double y = source.getY() + 0.5;
            double z = source.getZ() + 0.5 - Math.sin(rad) * 12;

            WispParticleData data = WispParticleData.wisp(0.5F, r, g, b);
            level.sendParticles(data, x, y, z, 1,
                    (float) (Math.random() - 0.5F) * 0.15F,
                    (float) (Math.random() - 0.5F) * 0.35F,
                    (float) (Math.random() - 0.5F) * 0.15F,
                    0);
        }
    }

    /**
     * 生成从柱子到中心的能量流
     */
    public static void spawnPylonEnergyParticles(ServerLevel level, BlockPos center, int tick) {
        Vec3 centerVec = Vec3.atCenterOf(center).subtract(0, 0.2, 0);

        for (BlockPos pillarOffset : PILLAR_POSITIONS) {
            BlockPos pillarPos = center.offset(pillarOffset);
            Vec3 pylonPos = new Vec3(pillarPos.getX(), pillarPos.getY(), pillarPos.getZ());

            double worldTime = tick;
            worldTime /= 5;

            float rad = 0.75F + (float) Math.random() * 0.05F;
            double xp = pylonPos.x + 0.5 + Math.cos(worldTime) * rad;
            double zp = pylonPos.z + 0.5 + Math.sin(worldTime) * rad;

            Vec3 partPos = new Vec3(xp, pylonPos.y, zp);
            Vec3 mot = centerVec.subtract(partPos).scale(0.04);

            float r = 0.7F + (float) Math.random() * 0.3F;
            float g = (float) Math.random() * 0.3F;
            float b = 0.7F + (float) Math.random() * 0.3F;

            WispParticleData data1 = WispParticleData.wisp(0.25F + (float) Math.random() * 0.1F, r, g, b, 1);
            level.sendParticles(data1, partPos.x, partPos.y, partPos.z, 1,
                    0, -(-0.075F - (float) Math.random() * 0.015F), 0, 0);

            WispParticleData data2 = WispParticleData.wisp(0.4F, r, g, b);
            level.sendParticles(data2, partPos.x, partPos.y, partPos.z, 1,
                    (float) mot.x, (float) mot.y, (float) mot.z, 0);
        }
    }

    /**
     * 组合所有效果
     */
    public static void spawnAllCraftingParticles(ServerLevel level, BlockPos center, int tick, float progress) {
        if (tick % 2 == 0) {
            spawnArenaRingParticles(level, center.below(), tick);
        }

        spawnPylonEnergyParticles(level, center, tick);

        spawnPillarToCenterParticles(level, center, tick);

        spawnCirclingParticles(level, center, tick);

        if (tick % 2 == 0) {
            spawnPillarDownwardParticles(level, center, tick);
        }

        spawnHexagramParticles(level, center, tick);

        spawnCraftingProgressParticles(level, center, tick, progress);
    }
}