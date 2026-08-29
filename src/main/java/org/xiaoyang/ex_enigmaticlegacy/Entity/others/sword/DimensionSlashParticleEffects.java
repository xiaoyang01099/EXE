package org.xiaoyang.ex_enigmaticlegacy.Entity.others.sword;

import net.minecraft.world.phys.Vec3;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.system.ParticleEmitTask;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

import java.util.Random;

// DimensionSlashParticleEffects 集中提交次元斩领域扩散、边缘环绕和碎屑粒子。
public class DimensionSlashParticleEffects {
    // 根据领域阶段并行提交扩散和边缘环绕粒子。
    public static void emitDomainParticles(DimensionSlashDomainEntity entity, float partialTick) {
        if (Exe.POST == null) return;
        int age = entity.getAge();
        if (entity.clientLastParticleTick == age) return;
        entity.clientLastParticleTick = age;
        if (age <= DimensionSlashConfig.PARTICLE_EXPAND_END_TICK) {
            emitExpandParticles(entity);
        }
        if (age >= DimensionSlashConfig.PARTICLE_ORBIT_START_TICK && age <= DimensionSlashConfig.DOMAIN_LIFE_TICKS) {
            emitOrbitParticles(entity);
        }
        emitFinalDebrisIfNeeded(entity);
    }

    // 领域展开阶段生成半径逐帧增大的圆形粒子环。
    public static void emitExpandParticles(DimensionSlashDomainEntity entity) {
        float progress = Math.min(1.0F, entity.getAge() / Math.max(1.0F, (float) DimensionSlashConfig.PARTICLE_EXPAND_END_TICK));
        float radius = Math.max(0.2F, (float) DimensionSlashConfig.RADIUS * progress);
        Exe.POST.addParticle(new ParticleEmitTask()
                .position(entity.position())
                .direction(0.0F, 1.0F, 0.0F)
                .speed(0.0F)
                .spread(0.02F)
                .life(0.48F)
                .gravity(0.0F)
                .size(DimensionSlashConfig.PARTICLE_RING_SIZE, DimensionSlashConfig.PARTICLE_RING_SIZE, 0.0F)
                .color(0xD8FFFF, 0.96F)
                .endColor(0x356DFF, 0.0F)
                .shape(ParticleEmitTask.SHAPE_CIRCLE)
                .motion(ParticleEmitTask.MOTION_CIRCULAR)
                .orbit(radius, 0.0F, 0.0F)
                .orbitPlane(0.0F, 0.0F, 0.0F)
                .orbitSpawnMode(ParticleEmitTask.ORBIT_SPAWN_DISTRIBUTED)
                .burst(DimensionSlashConfig.PARTICLE_RATE + 220)
                .duration(0.01F));
    }

    // 领域稳定阶段使用 orbit 方法生成边缘旋转粒子。
    public static void emitOrbitParticles(DimensionSlashDomainEntity entity) {
        Exe.POST.addParticle(new ParticleEmitTask()
                .position(entity.position())
                .direction(0.0F, 1.0F, 0.0F)
                .speed(0.18F)
                .spread(0.03F)
                .life(0.9F)
                .gravity(0.0F)
                .size(0.52F, 0.52F, 0.0F)
                .color(0x2F7DFF, 1.0F)
                .endColor(0x0628FF, 0.0F)
                .shape(ParticleEmitTask.SHAPE_STAR)
                .motion(ParticleEmitTask.MOTION_CIRCULAR)
                .orbit((float) DimensionSlashConfig.RADIUS, 9.2F, 0.0F)
                .orbitPlane(0.0F, 0.0F, 0.0F)
                .orbitSpawnMode(ParticleEmitTask.ORBIT_SPAWN_DISTRIBUTED)
                .rate(DimensionSlashConfig.PARTICLE_RATE * 2)
                .duration(0.1F));
    }

    // 终结破碎阶段生成贴近地面的棕色碎屑。
    public static void emitFinalDebrisIfNeeded(DimensionSlashDomainEntity entity) {
        if (entity.clientDebrisPlayed) return;
        if (entity.getAge() < DimensionSlashConfig.GLASS_START_TICK) return;
        Random random = new Random(entity.getVisualSeed() * 97L + 13L);
        Vec3 center = entity.position();
        for (int i = 0; i < DimensionSlashConfig.DEBRIS_COUNT; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double radius = random.nextDouble() * DimensionSlashConfig.RADIUS * 0.92D;
            Vec3 pos = center.add(Math.cos(angle) * radius, 0.18D + random.nextDouble() * 0.35D, Math.sin(angle) * radius);
            Vec3 dir = pos.subtract(center).normalize().add(0.0D, 0.28D + random.nextDouble() * 0.28D, 0.0D).normalize();
            int color = random.nextBoolean() ? 0x8A5A35 : 0xB17A4A;
            Exe.POST.addParticle(new ParticleEmitTask()
                    .position(pos)
                    .direction((float) dir.x, (float) dir.y, (float) dir.z)
                    .speed(1.2F + random.nextFloat() * 2.1F)
                    .spread(0.22F)
                    .life(1.1F + random.nextFloat() * 0.7F)
                    .gravity(0.48F)
                    .size(0.08F + random.nextFloat() * 0.08F, 0.08F + random.nextFloat() * 0.08F, random.nextFloat() * 6.28F)
                    .color(color, 0.92F)
                    .endColor(0x5F452F, 0.0F)
                    .shape(ParticleEmitTask.SHAPE_SQUARE)
                    .motion(ParticleEmitTask.MOTION_BALLISTIC)
                    .burst(3 + random.nextInt(4))
                    .duration(0.02F));
        }
        entity.clientDebrisPlayed = true;
    }
}
