package org.xiaoyang.ex_enigmaticlegacy.Entity.others.colorful;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.system.ParticleEmitTask;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class BeamClientEffects {
    private static final Map<Integer, Long> TRIGGERED_BEAMS = new HashMap<>();
    private static final long TRIGGER_CACHE_TICKS = 80L;
    private static final double LIGHTNING_START_TRIM = 0.5;

    public static void triggerOnce(RailgunBeamEntity entity, float partialTick) {
        if (entity == null || Exe.POST == null) return;
        if (!markTriggered(entity)) return;
        emitBeamEffects(entity, entity.getOrigin(partialTick), entity.getEndpoint(partialTick), false);
    }

    public static void triggerOnce(ColorfulEntity entity, float partialTick) {
        if (entity == null || Exe.POST == null) return;
        if (!markTriggered(entity)) return;
        emitBeamEffects(entity, entity.getOrigin(partialTick), entity.getEndpoint(partialTick), true);
    }

    private static boolean markTriggered(Entity entity) {
        long now = entity.level().getGameTime();
        Iterator<Map.Entry<Integer, Long>> iterator = TRIGGERED_BEAMS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Long> entry = iterator.next();
            if (entry.getValue() <= now) {
                iterator.remove();
            }
        }

        int id = entity.getId();
        if (TRIGGERED_BEAMS.containsKey(id)) {
            return false;
        }
        TRIGGERED_BEAMS.put(id, now + TRIGGER_CACHE_TICKS);
        return true;
    }

    private static void emitBeamEffects(Entity entity, Vec3 origin, Vec3 endpoint, boolean colorful) {
        Vec3 delta = endpoint.subtract(origin);
        double length = delta.length();
        if (length < 0.1) return;

        Vec3 direction = delta.scale(1.0 / length);
        long seed = entity.getUUID().getLeastSignificantBits() ^ ((long) entity.getId() * 91815541L);
        Random random = new Random(seed);
        int lightningCount = colorful ? 6 + random.nextInt(4) : 4 + random.nextInt(3);
        float lightningWidth = colorful ? 0.55f : 0.35f;
        Vec3 lightningStart = origin.add(direction.scale(Math.min(LIGHTNING_START_TRIM, length * 0.08)));
        for (int i = 0; i < lightningCount; i++) {
            float lifetime = colorful ? 0.65f + i * 0.06f : 0.60f + i * 0.05f;
            Exe.POST.effects().addLightningStartToEnd(lightningStart, endpoint, lifetime, lightningWidth, seed + i * 9973L,
                    1f, 1f, 1.0f,
                    0.2f,
                    0.4f,
                    1.0f);
        }

        emitDissolveParticles(origin, endpoint, direction, length, colorful);
    }

    private static void emitDissolveParticles(Vec3 origin, Vec3 endpoint, Vec3 direction, double length, boolean colorful) {
        int maxParticlePoints = colorful ? 128 : 96;
        int pointCount = maxParticlePoints;
        float particleLife = colorful ? 8.4f : 7.0f;
        int particleBurst = colorful ? 16 : 10;
        float particleSize = colorful ? 0.038f : 0.032f;
        float particleSpread = colorful ? 0.3f : 0.2f;
        int rgb = colorful ? 0x864e74 : 0xA5D8FF;
        int endRGB = colorful ? 0xFF9F3F : 0xA5D8FF;

        for (int i = 0; i <= pointCount; i++) {
            float t = (float) i / (float) pointCount;
            Vec3 pos = origin.add(endpoint.subtract(origin).scale(t));
            ParticleEmitTask task = new ParticleEmitTask()
                    .position(pos)
                    .direction((float) direction.x, (float) direction.y, (float) direction.z)
                    .speed(0.1f)
                    .spread(particleSpread)
                    .life(particleLife)
                    .size(particleSize, particleSize, 0.0f)
                    .color(rgb, 1.0f)
                    .endColor(endRGB, 0.85f)
                    .burst(particleBurst)
                    .shape(i % 2 == 0 ? ParticleEmitTask.SHAPE_STAR : ParticleEmitTask.SHAPE_HEART)
                    .duration(0.0f);
            Exe.POST.addParticle(task);
        }
    }
}
