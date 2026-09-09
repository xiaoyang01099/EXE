package org.xiaoyang.ex_enigmaticlegacy.api.shader.bladeflash;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.xiaoyang.ex_enigmaticlegacy.Config.ConfigHandler;

import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

final class TrailStore {
    private record Sample(Vec3 root, Vec3 tip, double time) {}
    private static final class Trail {
        org.xiaoyang.ex_enigmaticlegacy.api.shader.ItemEffectTypes.Blade style;
        final ArrayDeque<Sample> samples = new ArrayDeque<>();
        double lifetime;
    }

    private final Map<Object, Trail> active = new LinkedHashMap<>();
    private final ArrayDeque<Trail> fading = new ArrayDeque<>();

    void sample(Object key, Vec3 root, Vec3 tip, double time, double lifetime) {
        sample(key,root,tip,time,lifetime,org.xiaoyang.ex_enigmaticlegacy.api.shader.ItemEffectTypes.Blade.CLASSIC);
    }
    void sample(Object key, Vec3 root, Vec3 tip, double time, double lifetime,org.xiaoyang.ex_enigmaticlegacy.api.shader.ItemEffectTypes.Blade style) {
        Objects.requireNonNull(key, "trail key");
        if (!finite(root) || !finite(tip) || !Double.isFinite(time)
            || !Double.isFinite(lifetime) || lifetime < 0.05 || lifetime > 2.0) {
            throw new IllegalArgumentException("Finite world positions, time, and lifetime in [0.05, 2] required");
        }
        Trail trail = active.get(key);
        if (trail != null && !trail.samples.isEmpty()) {
            Sample last = trail.samples.getLast();
            if (trail.style!=style || time < last.time || time - last.time > lifetime
                || root.distanceToSqr(last.root) > 16 || tip.distanceToSqr(last.tip) > 64) {
                breakTrail(key);
                trail = null;
            } else if (time - last.time < 1.0 / 240.0) {
                // Multiple render passes must not append duplicate timestamps.
                return;
            }
        }
        if (trail == null) {
            trail = new Trail();
            active.put(key, trail);
        }
        trail.lifetime = lifetime;
        trail.style=style;
        trail.samples.addLast(new Sample(root, tip, time));
        while (trail.samples.size() > ConfigHandler.MAX_SAMPLES.get()) trail.samples.removeFirst();
        trimCount();
    }

    void breakTrail(Object key) {
        Trail trail = active.remove(key);
        if (trail != null) fading.addLast(trail);
        trimCount();
    }

    void prune(double now) {
        active.values().removeIf(trail -> expired(trail, now));
        fading.removeIf(trail -> expired(trail, now));
    }

    private boolean expired(Trail trail, double now) {
        while (!trail.samples.isEmpty() && now - trail.samples.getFirst().time > trail.lifetime) {
            trail.samples.removeFirst();
        }
        return trail.samples.isEmpty() || trail.samples.getLast().time > now + 0.1;
    }

    boolean hasGeometry() {
        return active.values().stream().anyMatch(t -> t.samples.size() > 1)
            || fading.stream().anyMatch(t -> t.samples.size() > 1);
    }

    void draw(MaskCanvas canvas, double now) {
        for (Trail trail : fading) drawTrail(canvas, trail, now);
        for (Trail trail : active.values()) drawTrail(canvas, trail, now);
    }

    private void drawTrail(MaskCanvas canvas, Trail trail, double now) {
        canvas.style(trail.style);
        Sample previous = null;
        for (Sample current : trail.samples) {
            if (previous != null) {
                float a = Mth.clamp((float) ((now - previous.time) / trail.lifetime), 0, 1);
                float b = Mth.clamp((float) ((now - current.time) / trail.lifetime), 0, 1);
                canvas.quad(new MaskCanvas.Vertex(previous.root, a, 0, 1, 1),
                    new MaskCanvas.Vertex(previous.tip, a, 1, 1, 1),
                    new MaskCanvas.Vertex(current.tip, b, 1, 1, 1),
                    new MaskCanvas.Vertex(current.root, b, 0, 1, 1));
            }
            previous = current;
        }
    }

    private void trimCount() {
        while (active.size() + fading.size() > ConfigHandler.MAX_TRAILS.get()) {
            if (!fading.isEmpty()) fading.removeFirst();
            else active.remove(active.keySet().iterator().next());
        }
    }

    void clear() {
        active.clear();
        fading.clear();
    }

    private static boolean finite(Vec3 v) {
        return v != null && Double.isFinite(v.x) && Double.isFinite(v.y) && Double.isFinite(v.z);
    }
}
