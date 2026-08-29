package org.xiaoyang.ex_enigmaticlegacy.api.shader.slash;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class WorldShard {
    private static final int MIN_POLYGON_POINTS = 4;
    private static final int MAX_POLYGON_POINTS = 7;
    private final Vec3 origin;
    private final Vec3 velocity;
    private final Vec3 axisA;
    private final Vec3 axisB;
    private final Vec3 spinAxis;
    private final List<LocalPoint> polygon;
    private final float size;
    private final float aspect;
    private final float angularA;
    private final float angularB;
    private final float phase;
    private final int lifetime;
    private int age;

    public WorldShard(Vec3 origin, Vec3 velocity, Vec3 axisA, Vec3 axisB, Vec3 spinAxis, float size, float aspect, float angularA, float angularB, float phase, long shapeSeed, int lifetime) {
        this.origin = origin;
        this.velocity = velocity;
        this.axisA = safeNormalize(axisA, new Vec3(1.0, 0.0, 0.0));
        this.axisB = safeNormalize(axisB, new Vec3(0.0, 1.0, 0.0));
        this.spinAxis = safeNormalize(spinAxis, new Vec3(0.0, 1.0, 0.0));
        this.polygon = buildPolygon(shapeSeed);
        this.size = Math.max(0.02f, size);
        this.aspect = Math.max(0.15f, aspect);
        this.angularA = angularA;
        this.angularB = angularB;
        this.phase = phase;
        this.lifetime = Math.max(1, lifetime);
    }

    public void tick() {
        age++;
    }

    public boolean isAlive() {
        return age < lifetime;
    }

    public void render(PoseStack poseStack, BufferBuilder builder, Vec3 camera, float partialTick, float alphaScale) {
        float t = age + partialTick;
        float life = t / lifetime;
        if (life < 0.0f || life >= 1.0f) return;

        float alpha = alphaFade(life)
                * SlashCofig.WorldSlash.WORLD_SHARD_ALPHA
                * Mth.clamp(alphaScale, 0.0f, 1.0f);
        if (alpha <= 0.0001f) return;

        Vec3 gravity = new Vec3(0.0, -SlashCofig.WorldSlash.WORLD_SHARD_GRAVITY * t * t, 0.0);
        Vec3 center = origin.add(velocity.scale(t)).add(gravity).subtract(camera);
        Vec3 a = rotateAround(axisA, spinAxis, t * angularA).scale(size * (0.65f + life * 0.42f));
        Vec3 b = rotateAround(axisB, spinAxis, t * angularB).scale(size * aspect * (0.55f + life * 0.32f));
        Vec3 normal = safeNormalize(a.cross(b), spinAxis);
        float thickness = SlashCofig.WorldSlash.WORLD_SHARD_THICKNESS * (0.72f + phase * 0.46f) * (0.86f + life * 0.22f);
        Vec3 frontOffset = normal.scale(thickness * 0.5f);
        Vec3 backOffset = frontOffset.scale(-1.0);

        Matrix4f matrix = poseStack.last().pose();
        float shade = 0.28f + phase * 0.38f;
        float backShade = Mth.clamp(shade + 0.18f, 0.0f, 1.0f);
        float sideShade = Mth.clamp(shade + 0.42f, 0.0f, 1.0f);
        float faceAlpha = alpha * 0.96f;
        float backAlpha = alpha * 0.42f;
        float sideAlpha = alpha * 0.68f;
        Vec3 backNormal = normal.scale(-1.0);
        Vec3 faceCenterFront = center.add(frontOffset);
        Vec3 faceCenterBack = center.add(backOffset);

        for (int i = 0; i < polygon.size(); i++) {
            LocalPoint p0 = polygon.get(i);
            LocalPoint p1 = polygon.get((i + 1) % polygon.size());
            Vec3 front0 = vertexPosition(center, a, b, frontOffset, p0);
            Vec3 front1 = vertexPosition(center, a, b, frontOffset, p1);
            Vec3 back0 = vertexPosition(center, a, b, backOffset, p0);
            Vec3 back1 = vertexPosition(center, a, b, backOffset, p1);
            Vec3 sideNormal = safeNormalize(front1.subtract(front0).cross(normal), normal);

            addVertex(builder, matrix, faceCenterFront, 0.5f, 0.5f, 0.05f, shade, phase, faceAlpha, normal);
            addVertex(builder, matrix, front0, p0.u, p0.v, 0.82f, shade, phase, faceAlpha, normal);
            addVertex(builder, matrix, front1, p1.u, p1.v, 0.82f, shade, phase, faceAlpha, normal);

            addVertex(builder, matrix, faceCenterBack, 0.5f, 0.5f, 0.18f, backShade, phase, backAlpha, backNormal);
            addVertex(builder, matrix, back1, p1.u, p1.v, 0.72f, backShade, phase, backAlpha, backNormal);
            addVertex(builder, matrix, back0, p0.u, p0.v, 0.72f, backShade, phase, backAlpha, backNormal);

            addVertex(builder, matrix, front0, p0.u, p0.v, 1.0f, sideShade, phase, sideAlpha, sideNormal);
            addVertex(builder, matrix, back0, p0.u, p0.v, 1.0f, sideShade, phase, sideAlpha, sideNormal);
            addVertex(builder, matrix, back1, p1.u, p1.v, 1.0f, sideShade, phase, sideAlpha, sideNormal);

            addVertex(builder, matrix, front0, p0.u, p0.v, 1.0f, sideShade, phase, sideAlpha, sideNormal);
            addVertex(builder, matrix, back1, p1.u, p1.v, 1.0f, sideShade, phase, sideAlpha, sideNormal);
            addVertex(builder, matrix, front1, p1.u, p1.v, 1.0f, sideShade, phase, sideAlpha, sideNormal);
        }
    }

    private static Vec3 vertexPosition(Vec3 center, Vec3 a, Vec3 b, Vec3 extrusion, LocalPoint point) {
        return center.add(a.scale(point.x)).add(b.scale(point.y)).add(extrusion);
    }

    private static void addVertex(BufferBuilder builder, Matrix4f matrix, Vec3 p, float u, float v, float edge, float shade, float phase, float alpha, Vec3 normal) {
        builder.vertex(matrix, (float) p.x, (float) p.y, (float) p.z).color(edge, shade, phase, Mth.clamp(alpha, 0.0f, 1.0f)).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal((float) normal.x, (float) normal.y, (float) normal.z).endVertex();
    }

    private static List<LocalPoint> buildPolygon(long seed) {
        Random random = new Random(seed);
        int count = MIN_POLYGON_POINTS + random.nextInt(MAX_POLYGON_POINTS - MIN_POLYGON_POINTS + 1);
        float startAngle = random.nextFloat() * Mth.TWO_PI;
        float step = Mth.TWO_PI / count;
        List<LocalPoint> raw = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            float angle = startAngle + step * i + (random.nextFloat() - 0.5f) * step * 0.48f;
            float radius = 0.68f + random.nextFloat() * 0.46f;
            if ((i & 1) == 0) radius += random.nextFloat() * 0.16f;
            float x = Mth.cos(angle) * radius * (0.84f + random.nextFloat() * 0.26f);
            float y = Mth.sin(angle) * radius * (0.80f + random.nextFloat() * 0.32f);
            raw.add(new LocalPoint(x, y, 0.0f, 0.0f));
        }

        LocalPoint center = localCentroid(raw);
        List<LocalPoint> polygon = new ArrayList<>(count);
        for (LocalPoint point : raw) {
            float x = point.x - center.x;
            float y = point.y - center.y;
            float u = Mth.clamp(0.5f + x * 0.42f, 0.02f, 0.98f);
            float v = Mth.clamp(0.5f - y * 0.42f, 0.02f, 0.98f);
            polygon.add(new LocalPoint(x, y, u, v));
        }
        return List.copyOf(polygon);
    }

    private static LocalPoint localCentroid(List<LocalPoint> polygon) {
        float area2 = 0.0f;
        float cx = 0.0f;
        float cy = 0.0f;

        for (int i = 0; i < polygon.size(); i++) {
            LocalPoint a = polygon.get(i);
            LocalPoint b = polygon.get((i + 1) % polygon.size());
            float cross = a.x * b.y - b.x * a.y;
            area2 += cross;
            cx += (a.x + b.x) * cross;
            cy += (a.y + b.y) * cross;
        }

        if (Math.abs(area2) < 1.0e-6f) {
            for (LocalPoint point : polygon) {
                cx += point.x;
                cy += point.y;
            }
            float inv = 1.0f / polygon.size();
            return new LocalPoint(cx * inv, cy * inv, 0.0f, 0.0f);
        }

        float inv = 1.0f / (3.0f * area2);
        return new LocalPoint(cx * inv, cy * inv, 0.0f, 0.0f);
    }

    private static Vec3 rotateAround(Vec3 value, Vec3 axis, float angle) {
        float s = Mth.sin(angle);
        float c = Mth.cos(angle);
        Vec3 parallel = axis.scale(value.dot(axis));
        Vec3 perpendicular = value.subtract(parallel);
        Vec3 cross = axis.cross(perpendicular);
        return parallel.add(perpendicular.scale(c)).add(cross.scale(s));
    }

    private static float smooth(float value) {
        value = Mth.clamp(value, 0.0f, 1.0f);
        return value * value * (3.0f - 2.0f * value);
    }

    private static float alphaFade(float life) {
        life = Mth.clamp(life, 0.0f, 1.0f);
        float fadeStart = Mth.clamp(SlashCofig.WorldSlash.WORLD_SHARD_ALPHA_FADE_START, 0.0f, 0.98f);
        if (life <= fadeStart) return 1.0f;

        float fade = (life - fadeStart) / (1.0f - fadeStart);
        return 1.0f - smooth(fade);
    }

    private static Vec3 safeNormalize(Vec3 value, Vec3 fallback) {
        if (value.lengthSqr() < 1.0e-8) return fallback;
        return value.normalize();
    }

    private record LocalPoint(float x, float y, float u, float v) {}
}
