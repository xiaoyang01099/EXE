package org.xiaoyang.ex_enigmaticlegacy.Client.renderer.bolt;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.List;

public final class BoltRenderer {
    public static void renderStrand(Matrix4f mat, VertexConsumer vc, List<Vec3> pts, Vec3 camLocal, float baseWidth, float r, float g, float b, float alpha) {
        ribbon(mat, vc, pts, camLocal, baseWidth * 4.5f, r, g, b, alpha * 0.16f);
        ribbon(mat, vc, pts, camLocal, baseWidth * 1.8f, r, g, b, alpha * 0.45f);
        ribbon(mat, vc, pts, camLocal, baseWidth, r, g, b, alpha);
    }

    public static void ribbon(Matrix4f mat, VertexConsumer vc, List<Vec3> pts, Vec3 camLocal, float width, float r, float g, float b, float alpha) {
        int n = pts.size();
        if (n < 2 || alpha <= 0.003f) return;

        Vec3[] side = new Vec3[n];
        for (int i = 0; i < n; i++) {
            Vec3 p = pts.get(i);
            Vec3 prev = pts.get(Math.max(i - 1, 0));
            Vec3 next = pts.get(Math.min(i + 1, n - 1));

            Vec3 dir = next.subtract(prev);
            if (dir.lengthSqr() < 1e-8) dir = new Vec3(0, 1, 0);
            dir = dir.normalize();

            Vec3 view = p.subtract(camLocal);
            if (view.lengthSqr() < 1e-8) view = new Vec3(0, 0, 1);
            view = view.normalize();

            Vec3 s = dir.cross(view);
            if (s.lengthSqr() < 1e-8) s = dir.cross(new Vec3(0, 1, 0));
            if (s.lengthSqr() < 1e-8) s = new Vec3(1, 0, 0);

            double dist = p.distanceTo(camLocal);
            float w = (float) Math.max(width, dist * 0.0022);
            side[i] = s.normalize().scale(w * 0.5);
        }

        double total = 0;
        double[] acc = new double[n];
        for (int i = 1; i < n; i++) {
            total += pts.get(i).distanceTo(pts.get(i - 1));
            acc[i] = total;
        }
        if (total < 1e-6) return;

        int ri = c255(r), gi = c255(g), bi = c255(b), ai = c255(alpha);
        if (ai <= 0) return;

        for (int i = 0; i < n - 1; i++) {
            Vec3 p = pts.get(i), q = pts.get(i + 1);
            Vec3 sp = side[i], sq = side[i + 1];
            float v0 = (float) (acc[i] / total);
            float v1 = (float) (acc[i + 1] / total);

            vert(mat, vc, p.subtract(sp), 0f, v0, ri, gi, bi, ai);
            vert(mat, vc, q.subtract(sq), 0f, v1, ri, gi, bi, ai);
            vert(mat, vc, q.add(sq),      1f, v1, ri, gi, bi, ai);
            vert(mat, vc, p.add(sp),      1f, v0, ri, gi, bi, ai);
        }
    }

    public static void renderFlare(Matrix4f mat, VertexConsumer vc, Vec3 center, Vec3 camLocal, float size, float r, float g, float b, float alpha) {
        if (alpha <= 0.003f || size <= 0f) return;

        Vec3 view = center.subtract(camLocal);
        if (view.lengthSqr() < 1e-8) return;
        view = view.normalize();
        Vec3 up = Math.abs(view.y) > 0.95 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 right = view.cross(up).normalize().scale(size * 0.5);
        up = right.cross(view).normalize().scale(size * 0.5);

        radialQuad(mat, vc, center, right, up, r, g, b, alpha);
    }

    public static void renderDisc(Matrix4f mat, VertexConsumer vc, Vec3 center, Vec3 axis, float size, float r, float g, float b, float alpha) {
        if (alpha <= 0.003f || size <= 0f) return;
        Vec3 a = axis.lengthSqr() < 1e-8 ? new Vec3(0, 1, 0) : axis.normalize();
        Vec3 ref = Math.abs(a.y) > 0.95 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 x = a.cross(ref).normalize().scale(size * 0.5);
        Vec3 y = a.cross(x).normalize().scale(size * 0.5);
        radialQuad(mat, vc, center, x, y, r, g, b, alpha);
    }

    private static void radialQuad(Matrix4f mat, VertexConsumer vc, Vec3 center, Vec3 x, Vec3 y, float r, float g, float b, float alpha) {
        int ri = c255(r), gi = c255(g), bi = c255(b), ai = c255(alpha);
        if (ai <= 0) return;
        vert(mat, vc, center.subtract(x).subtract(y), 0f, 2f, ri, gi, bi, ai);
        vert(mat, vc, center.subtract(x).add(y),      0f, 3f, ri, gi, bi, ai);
        vert(mat, vc, center.add(x).add(y),           1f, 3f, ri, gi, bi, ai);
        vert(mat, vc, center.add(x).subtract(y),      1f, 2f, ri, gi, bi, ai);
    }

    private static int c255(float v) { return (int) (Mth.clamp(v, 0f, 1f) * 255f); }

    private static void vert(Matrix4f mat, VertexConsumer vc, Vec3 p, float u, float v, int r, int g, int b, int a) {
        vc.vertex(mat, (float) p.x, (float) p.y, (float) p.z)
                .color(r, g, b, a)
                .uv(u, v)
                .endVertex();
    }

    private BoltRenderer() {}
}