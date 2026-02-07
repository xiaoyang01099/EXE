package net.xiaoyang010.ex_enigmaticlegacy.World.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.client.fx.WispParticleData;

import java.util.Random;

public class StructureParticleValidator {
    private static final Random RANDOM = new Random();

    /**
     * 生成结构完成的粒子效果（彩虹色）- 公共方法
     */
    public static void spawnCompletionParticles(Level level, BlockPos centerPos) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        double x = centerPos.getX() + 0.5;
        double y = centerPos.getY() + 0.5;
        double z = centerPos.getZ() + 0.5;

        // 播放成功音效
        level.playSound(null, centerPos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 1.0F, 1.0F);

        // 彩虹螺旋效果
        for (int i = 0; i < 50; i++) {
            double angle = (i / 50.0) * Math.PI * 4;
            double radius = 2.0;
            double height = (i / 50.0) * 3.0;

            double particleX = x + Math.cos(angle) * radius;
            double particleY = y + height;
            double particleZ = z + Math.sin(angle) * radius;

            float hue = (i / 50.0F);
            int rgb = java.awt.Color.HSBtoRGB(hue, 1.0F, 1.0F);
            float r = ((rgb >> 16) & 0xFF) / 255.0F;
            float g = ((rgb >> 8) & 0xFF) / 255.0F;
            float b = (rgb & 0xFF) / 255.0F;

            SparkleParticleData sparkleData = SparkleParticleData.sparkle(
                    2.0F,
                    r, g, b,
                    8
            );

            serverLevel.sendParticles(
                    sparkleData,
                    particleX, particleY, particleZ,
                    1,
                    0, 0, 0,
                    0
            );
        }

        // 中心爆发效果
        for (int i = 0; i < 30; i++) {
            double angle = RANDOM.nextDouble() * Math.PI * 2;
            double pitch = RANDOM.nextDouble() * Math.PI;
            double speed = 0.3 + RANDOM.nextDouble() * 0.3;

            double vx = Math.sin(pitch) * Math.cos(angle) * speed;
            double vy = Math.cos(pitch) * speed;
            double vz = Math.sin(pitch) * Math.sin(angle) * speed;

            float hue = RANDOM.nextFloat();
            int rgb = java.awt.Color.HSBtoRGB(hue, 1.0F, 1.0F);
            float r = ((rgb >> 16) & 0xFF) / 255.0F;
            float g = ((rgb >> 8) & 0xFF) / 255.0F;
            float b = (rgb & 0xFF) / 255.0F;

            WispParticleData wispData = WispParticleData.wisp(
                    0.5F,
                    r, g, b,
                    1.0F
            );

            serverLevel.sendParticles(
                    wispData,
                    x, y, z,
                    1,
                    vx, vy, vz,
                    0
            );
        }
    }

    /**
     * 显示结构轮廓粒子（黄色）- 公共方法
     */
    public static void showStructureOutline(Level level, BlockPos centerPos) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        // 播放提示音效
        level.playSound(null, centerPos, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 0.5F, 0.8F);

        // 在中心方块周围显示轮廓粒子
        for (int i = 0; i < 8; i++) {
            double angle = (i / 8.0) * Math.PI * 2;
            double radius = 1.5;

            double particleX = centerPos.getX() + 0.5 + Math.cos(angle) * radius;
            double particleY = centerPos.getY() + 0.5;
            double particleZ = centerPos.getZ() + 0.5 + Math.sin(angle) * radius;

            WispParticleData wispData = WispParticleData.wisp(
                    0.3F,
                    1.0F, 1.0F, 0.0F, // 黄色
                    0.5F
            );

            serverLevel.sendParticles(
                    wispData,
                    particleX, particleY, particleZ,
                    1,
                    0, 0.05, 0,
                    0
            );
        }

        // 向上的粒子柱
        for (int i = 0; i < 10; i++) {
            double particleY = centerPos.getY() + 0.5 + (i * 0.5);

            SparkleParticleData sparkleData = SparkleParticleData.sparkle(
                    1.0F,
                    1.0F, 1.0F, 0.0F, // 黄色
                    5
            );

            serverLevel.sendParticles(
                    sparkleData,
                    centerPos.getX() + 0.5, particleY, centerPos.getZ() + 0.5,
                    1,
                    0, 0, 0,
                    0
            );
        }
    }

    /**
     * 生成正确放置的粒子效果（绿色）
     */
    public static void spawnCorrectParticles(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        for (int i = 0; i < 5; i++) {
            double offsetX = (RANDOM.nextDouble() - 0.5) * 0.5;
            double offsetY = (RANDOM.nextDouble() - 0.5) * 0.5;
            double offsetZ = (RANDOM.nextDouble() - 0.5) * 0.5;

            SparkleParticleData sparkleData = SparkleParticleData.sparkle(
                    1.0F,
                    0.0F, 1.0F, 0.0F, // 绿色
                    5
            );

            serverLevel.sendParticles(
                    sparkleData,
                    x + offsetX, y + offsetY, z + offsetZ,
                    1,
                    0, 0, 0,
                    0
            );
        }

        for (int i = 0; i < 3; i++) {
            double offsetX = (RANDOM.nextDouble() - 0.5) * 0.3;
            double offsetY = RANDOM.nextDouble() * 0.5;
            double offsetZ = (RANDOM.nextDouble() - 0.5) * 0.3;

            WispParticleData wispData = WispParticleData.wisp(
                    0.3F,
                    0.0F, 1.0F, 0.0F, // 绿色
                    1.0F
            );

            serverLevel.sendParticles(
                    wispData,
                    x + offsetX, y + offsetY, z + offsetZ,
                    1,
                    0, 0.05, 0,
                    0
            );
        }
    }

    /**
     * 生成错误放置的粒子效果（红色）
     */
    public static void spawnIncorrectParticles(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        for (int i = 0; i < 3; i++) {
            double offsetX = (RANDOM.nextDouble() - 0.5) * 0.5;
            double offsetY = (RANDOM.nextDouble() - 0.5) * 0.5;
            double offsetZ = (RANDOM.nextDouble() - 0.5) * 0.5;

            SparkleParticleData sparkleData = SparkleParticleData.sparkle(
                    0.5F,
                    1.0F, 0.0F, 0.0F, // 红色
                    3
            );

            serverLevel.sendParticles(
                    sparkleData,
                    x + offsetX, y + offsetY, z + offsetZ,
                    1,
                    0, 0, 0,
                    0
            );
        }

        serverLevel.sendParticles(
                ParticleTypes.SMOKE,
                x, y, z,
                2,
                0.2, 0.2, 0.2,
                0.01
        );
    }
}