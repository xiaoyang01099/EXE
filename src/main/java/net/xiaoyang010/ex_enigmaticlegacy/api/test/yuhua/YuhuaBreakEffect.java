package net.xiaoyang010.ex_enigmaticlegacy.api.test.yuhua;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.Client.ModParticleTypes;

import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class YuhuaBreakEffect {
    private static final Random RANDOM = new Random();

    public static void spawnBreakEffect(LivingEntity entity) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        double cx = entity.getX();
        double cy = entity.getY() + entity.getBbHeight() * 0.5;
        double cz = entity.getZ();

        float w = entity.getBbWidth();
        float h = entity.getBbHeight();

        int count = 40 + RANDOM.nextInt(20);
        for (int i = 0; i < count; i++) {
            double ox = (RANDOM.nextDouble() - 0.5) * w * 0.8;
            double oy = (RANDOM.nextDouble() - 0.2) * h * 0.8;
            double oz = (RANDOM.nextDouble() - 0.5) * w * 0.8;

            double speed = 0.15 + RANDOM.nextDouble() * 0.35;
            double theta = RANDOM.nextDouble() * Math.PI * 2;
            double phi   = RANDOM.nextDouble() * Math.PI;

            double vx = Math.sin(phi) * Math.cos(theta) * speed;
            double vy = Math.cos(phi) * speed * 0.8 + 0.05;
            double vz = Math.sin(phi) * Math.sin(theta) * speed;

            mc.level.addParticle(
                    ModParticleTypes.YUHUA_COSMIC.get(),
                    cx + ox, cy + oy, cz + oz,
                    vx, vy, vz
            );
        }

        int groundCount = 30 + RANDOM.nextInt(15);
        double groundY = entity.getY() + 0.05;
        for (int i = 0; i < groundCount; i++) {
            double ox = (RANDOM.nextDouble() - 0.5) * w * 1.2;
            double oy = RANDOM.nextDouble() * h;
            double oz = (RANDOM.nextDouble() - 0.5) * w * 1.2;

            double angle = RANDOM.nextDouble() * Math.PI * 2;
            double outSpeed = 0.02 + RANDOM.nextDouble() * 0.06;
            double vx = Math.cos(angle) * outSpeed;
            double vy = -(0.04 + RANDOM.nextDouble() * 0.12); // 向下
            double vz = Math.sin(angle) * outSpeed;

            mc.level.addParticle(
                    ModParticleTypes.YUHUA_COSMIC.get(),
                    cx + ox, entity.getY() + oy, cz + oz,
                    vx, vy, vz
            );
        }

        int ringCount = 20 + RANDOM.nextInt(10);
        for (int i = 0; i < ringCount; i++) {
            double angle  = RANDOM.nextDouble() * Math.PI * 2;
            double radius = w * 0.3 + RANDOM.nextDouble() * w * 0.5;
            double ox = Math.cos(angle) * radius;
            double oz = Math.sin(angle) * radius;

            double outSpeed = 0.03 + RANDOM.nextDouble() * 0.07;
            double vx = Math.cos(angle) * outSpeed;
            double vy = 0.01 + RANDOM.nextDouble() * 0.04;
            double vz = Math.sin(angle) * outSpeed;

            mc.level.addParticle(
                    ModParticleTypes.YUHUA_COSMIC.get(),
                    cx + ox, groundY, cz + oz,
                    vx, vy, vz
            );
        }

        for (int i = 0; i < 15; i++) {
            double vx = (RANDOM.nextDouble() - 0.5) * 0.2;
            double vy =  RANDOM.nextDouble() * 0.15;
            double vz = (RANDOM.nextDouble() - 0.5) * 0.2;
            mc.level.addParticle(
                    ParticleTypes.END_ROD,
                    cx, cy, cz,
                    vx, vy, vz
            );
        }
    }

    public static void spawnHitEffect(LivingEntity entity) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        double cx = entity.getX();
        double cy = entity.getY() + entity.getBbHeight() * 0.6;
        double cz = entity.getZ();

        int count = 5 + RANDOM.nextInt(6);
        for (int i = 0; i < count; i++) {
            double theta = RANDOM.nextDouble() * Math.PI * 2;
            double speed = 0.05 + RANDOM.nextDouble() * 0.12;
            double vx = Math.cos(theta) * speed;
            double vy = 0.05 + RANDOM.nextDouble() * 0.08;
            double vz = Math.sin(theta) * speed;

            mc.level.addParticle(
                    ModParticleTypes.YUHUA_COSMIC.get(),
                    cx, cy, cz,
                    vx, vy, vz
            );
        }
    }
}