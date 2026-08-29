package org.xiaoyang.ex_enigmaticlegacy.api.shader.slash;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModParticleTypes;

import java.util.List;
import java.util.Random;

public final class ParticleBurst {
    private ParticleBurst() {
    }

    public static void spawnSlashShatter(ClientLevel level, SlashLine line, Random random, long roundId) {
        ParticleOwnership.spawnOwned(roundId, () -> spawnSlashShatterOwned(level, line, random));
    }

    private static void spawnSlashShatterOwned(ClientLevel level, SlashLine line, Random random) {
        Vec3 start = line.start();
        Vec3 end = line.end();
        Vec3 dir = line.direction();
        Vec3 side = line.normal().cross(dir);
        if (side.lengthSqr() < 1.0e-8) side = pickSide(dir);
        side = side.normalize();

        int particleCount = Math.max(8, Math.min(28, Math.round(line.length() * 2.2f)));
        for (int i = 0; i < particleCount; i++) {
            float t = (i + random.nextFloat()) / particleCount;
            Vec3 pos = start.lerp(end, t).add(side.scale((random.nextFloat() - 0.5f) * line.width() * 1.5f));
            Vec3 vel = dir.scale((random.nextFloat() - 0.5f) * 1.9f).add(side.scale((random.nextFloat() - 0.5f) * 1.4f)).add(line.normal().scale((random.nextFloat() - 0.5f) * 1.2f));
            level.addParticle(ModParticleTypes.TRUE_DEMON_SLASH_PARTICLE.get(), pos.x, pos.y, pos.z, vel.x, vel.y, vel.z);
        }
    }

    static void spawnDomainCorrosion(ClientLevel level, Vec3 groundCenter, float radius, float densityScale, Random random, long roundId) {
        if (level == null || groundCenter == null || radius <= 0.001f) return;

        int minCount = Math.max(0, SlashCofig.WorldSlash.Domain.PARTICLE_MIN_COUNT);
        int maxCount = Math.max(minCount, SlashCofig.WorldSlash.Domain.PARTICLE_MAX_COUNT);
        int particleCount = Mth.clamp(
                Math.round(radius * SlashCofig.WorldSlash.Domain.PARTICLE_COUNT_PER_RADIUS),
                minCount,
                maxCount
        );
        float safeDensity = Mth.clamp(densityScale, 0.0f, 1.0f);
        ParticleOwnership.spawnOwned(roundId, () -> {
            for (int index = 0; index < particleCount; index++) {
                if (random.nextFloat() > safeDensity) continue;

                float angle = random.nextFloat() * Mth.TWO_PI;
                float distance = domainRadialDistance(random.nextFloat(), radius);
                double x = groundCenter.x + Mth.cos(angle) * distance;
                double z = groundCenter.z + Mth.sin(angle) * distance;
                Vec3 spawnPosition = resolveDomainGround(level, groundCenter, x, z);
                if (spawnPosition == null) continue;

                level.addParticle(
                        ModParticleTypes.TRUE_DEMON_CORROSION_PARTICLE.get(),
                        spawnPosition.x,
                        spawnPosition.y,
                        spawnPosition.z,
                        SlashCofig.WorldSlash.Domain.PARTICLE_SIZE_SCALE,
                        0.0,
                        0.0
                );
            }
        });
    }

    static float domainRadialDistance(float unitSample, float radius) {
        return Mth.sqrt(Mth.clamp(unitSample, 0.0f, 1.0f)) * Math.max(0.0f, radius);
    }

    private static Vec3 resolveDomainGround(ClientLevel level, Vec3 groundCenter, double x, double z) {
        BlockPos samplePosition = BlockPos.containing(x, groundCenter.y, z);
        if (!level.hasChunkAt(samplePosition)) return null;

        double startY = Math.min(
                level.getMaxBuildHeight() - 1.0e-3,
                groundCenter.y + Math.max(0.0f, SlashCofig.WorldSlash.Domain.PARTICLE_GROUND_SEARCH_ABOVE)
        );
        double endY = Math.max(
                level.getMinBuildHeight(),
                groundCenter.y - Math.max(0.0f, SlashCofig.WorldSlash.Domain.PARTICLE_GROUND_SEARCH_BELOW)
        );
        if (startY <= endY) return null;

        Vec3 start = new Vec3(x, startY, z);
        Vec3 end = new Vec3(x, endY, z);
        HitResult hit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));
        if (hit.getType() != HitResult.Type.BLOCK) return null;

        Vec3 location = hit.getLocation();
        return new Vec3(
                x,
                location.y + SlashCofig.WorldSlash.Domain.PARTICLE_GROUND_OFFSET,
                z
        );
    }

    public static void spawnFinalBurst(ClientLevel level, Vec3 center, float radius, Random random, long roundId) {
        ParticleOwnership.spawnOwned(roundId, () -> spawnFinalBurstOwned(level, center, radius, random));
    }

    private static void spawnFinalBurstOwned(ClientLevel level, Vec3 center, float radius, Random random) {
        int minCount = Math.max(0, SlashCofig.WorldSlash.TRUE_DEMON_FINAL_PARTICLE_MIN_COUNT);
        int maxCount = SlashCofig.WorldSlash.TRUE_DEMON_FINAL_PARTICLE_MAX_COUNT;
        int particleCount = Mth.clamp(Math.round(radius * SlashCofig.WorldSlash.TRUE_DEMON_FINAL_PARTICLE_COUNT_PER_RADIUS), minCount, maxCount);
        float spawnRadius = Math.max(Math.max(0.0f, SlashCofig.WorldSlash.TRUE_DEMON_FINAL_PARTICLE_SPAWN_RADIUS), radius);
        float speedMin = Math.max(0.0f, SlashCofig.WorldSlash.TRUE_DEMON_FINAL_PARTICLE_SPEED_MIN);
        float speedRandom = Math.max(0.0f, SlashCofig.WorldSlash.TRUE_DEMON_FINAL_PARTICLE_SPEED_RANDOM);
        for (int i = 0; i < particleCount; i++) {
            Vec3 dir = randomUnit(random);
            float speed = speedMin + random.nextFloat() * speedRandom;
            Vec3 pos = center.add(dir.scale(random.nextFloat() * spawnRadius));
            Vec3 vel = dir.scale(speed);
            level.addParticle(ModParticleTypes.TRUE_DEMON_PARTICLE.get(), pos.x, pos.y, pos.z, vel.x, vel.y, vel.z);
        }

        int starMinCount = Math.max(0, SlashCofig.WorldSlash.TRUE_DEMON_FINAL_STAR_MIN_COUNT);
        int starMaxCount = SlashCofig.WorldSlash.TRUE_DEMON_FINAL_STAR_MAX_COUNT;
        int starCount = Mth.clamp(Math.round(radius * SlashCofig.WorldSlash.TRUE_DEMON_FINAL_STAR_COUNT_PER_RADIUS), starMinCount, starMaxCount);
        float starSpawnRadius = Math.min(Math.max(0.0f, radius * SlashCofig.WorldSlash.TRUE_DEMON_FINAL_STAR_SPAWN_RADIUS_SCALE), Math.max(0.0f, SlashCofig.WorldSlash.TRUE_DEMON_FINAL_STAR_SPAWN_RADIUS_MAX));
        float starSpeedMin = Math.max(0.0f, SlashCofig.WorldSlash.TRUE_DEMON_FINAL_STAR_SPEED_MIN);
        float starSpeedRandom = Math.max(0.0f, SlashCofig.WorldSlash.TRUE_DEMON_FINAL_STAR_SPEED_RANDOM);
        for (int i = 0; i < starCount; i++) {
            Vec3 dir = randomUnit(random);
            float speed = starSpeedMin + random.nextFloat() * starSpeedRandom;
            Vec3 pos = center.add(dir.scale(random.nextFloat() * starSpawnRadius));
            Vec3 drift = randomUnit(random).scale(speed * 0.18f * random.nextFloat());
            Vec3 vel = dir.scale(speed).add(drift);
            level.addParticle(ModParticleTypes.TRUE_DEMON_STAR_PARTICLE.get(), pos.x, pos.y, pos.z, vel.x, vel.y, vel.z);
        }
    }

    public static void spawnFinalWorldShards(Vec3 center, float radius, Random random, List<WorldShard> shards) {
        if (!SlashCofig.WorldSlash.WORLD_SHARDS_ENABLED) return;

        int count = Math.max(0, SlashCofig.WorldSlash.WORLD_SHARD_COUNT);
        int lifetime = Math.max(1, SlashCofig.WorldSlash.WORLD_SHARD_LIFETIME_TICKS);
        for (int i = 0; i < count; i++) {
            Vec3 dir = randomUnit(random);
            Vec3 tangent = pickSide(dir);
            Vec3 bitangent = dir.cross(tangent);
            if (bitangent.lengthSqr() < 1.0e-8) bitangent = pickSide(tangent);
            bitangent = bitangent.normalize();

            float spawnRadius = Math.max(SlashCofig.WorldSlash.WORLD_SHARD_SPAWN_RADIUS, radius);
            Vec3 origin = center.add(dir.scale(random.nextFloat() * Math.max(0.1f, spawnRadius)));
            float speed = SlashCofig.WorldSlash.WORLD_SHARD_EXPLOSION_SPEED + random.nextFloat() * SlashCofig.WorldSlash.WORLD_SHARD_EXPLOSION_RANDOM;
            Vec3 velocity = dir.scale(speed).add(0.0, SlashCofig.WorldSlash.WORLD_SHARD_UPWARD_BIAS * random.nextFloat(), 0.0);
            float size = Mth.lerp(random.nextFloat(), SlashCofig.WorldSlash.WORLD_SHARD_SIZE_MIN, SlashCofig.WorldSlash.WORLD_SHARD_SIZE_MAX);
            float aspect = 0.35f + random.nextFloat() * 0.95f;
            float angularA = (random.nextFloat() - 0.5f) * 2.0f * SlashCofig.WorldSlash.WORLD_SHARD_TUMBLE_SPEED;
            float angularB = (random.nextFloat() - 0.5f) * 1.4f * SlashCofig.WorldSlash.WORLD_SHARD_TUMBLE_SPEED;
            shards.add(new WorldShard(origin, velocity, tangent, bitangent, dir, size, aspect, angularA, angularB, random.nextFloat(), random.nextLong(), lifetime + random.nextInt(lifetime / 3)));
        }
    }

    private static Vec3 randomUnit(Random random) {
        float yaw = random.nextFloat() * Mth.TWO_PI;
        float y = random.nextFloat() * 2.0f - 1.0f;
        float h = Mth.sqrt(Math.max(0.0f, 1.0f - y * y));
        return new Vec3(Mth.cos(yaw) * h, y, Mth.sin(yaw) * h);
    }

    private static Vec3 pickSide(Vec3 direction) {
        Vec3 up = Math.abs(direction.y) > 0.85 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
        Vec3 side = direction.cross(up);
        if (side.lengthSqr() < 1.0e-8) return new Vec3(1.0, 0.0, 0.0);
        return side.normalize();
    }
}
