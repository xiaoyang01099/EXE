package org.xiaoyang.ex_enigmaticlegacy.Client.renderer.bolt;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.xiaoyang.ex_enigmaticlegacy.Client.help.EXERenderHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TrueBolt {
    public final Vec3 start;
    public final Vec3 end;
    public final long seed;
    public final int maxAge;
    public final float width;
    public final float r, g, b;
    public int age;
    private int shapeTick = -1;
    private Vec3 shapeCamera;
    private Vec3 shapeTip;
    private List<BoltGeometry.Strand> shape;
    private List<Vec3> helixA;
    private List<Vec3> helixB;

    public TrueBolt(Vec3 start, Vec3 end, long seed, int maxAge, float width, float r, float g, float b) {
        this.start = start;
        this.end = end;
        this.seed = seed;
        this.maxAge = maxAge;
        this.width = width;
        this.r = r; this.g = g; this.b = b;
    }

    public static TrueBolt of(Vec3 start, Vec3 end) {
        return new TrueBolt(start, end, RandomSource.create().nextLong(), 10, 0.13f,
                0.60f, 0.72f, 1.0f);
    }

    public boolean isDone() {
        return age >= maxAge;
    }

    public void render(Matrix4f mat, VertexConsumer vc, Vec3 camPos, float partialTick) {
        float t = age + partialTick;

        float extend = Mth.clamp(t / 2.0f, 0f, 1f);
        extend = 1f - (1f - extend) * (1f - extend); // ease-out
        Vec3 tip = start.add(end.subtract(start).scale(extend));

        float life = t / maxAge;
        float alpha = (t < 1.5f)
                ? 0.55f + 0.45f * (t / 1.5f)
                : Mth.clamp(1.0f - (life - 0.15f) / 0.85f, 0f, 1f);
        alpha = (float) Math.pow(alpha, 0.75);
        if (alpha <= 0.01f) return;

        boolean rebuildShape =
                shapeTick != age
                        || shapeCamera == null
                        || shapeTip == null
                        || shapeCamera.distanceToSqr(camPos) > 1.0E-8D
                        || shapeTip.distanceToSqr(tip) > 1.0E-8D;

        if (rebuildShape) {
            shapeTick = age;
            shapeCamera = camPos;
            shapeTip = tip;
            RandomSource rand = RandomSource.create(seed + age * 31L);
            BoltGeometry.Cfg cfg = new BoltGeometry.Cfg();
            cfg.detail = 5;
            cfg.chaos = 0.045;
            cfg.roughness = 0.60;
            cfg.branchChance = 0.13F;
            cfg.maxBranchDepth = 1;
            cfg.branchLenMin = 0.06;
            cfg.branchLenMax = 0.18;
            cfg.branchForward = 0.35;
            cfg.branchAngle = 1.4;
            Vec3 localStart = start.subtract(camPos);
            Vec3 localTip = tip.subtract(camPos);

            shape = BoltGeometry.build(localStart, localTip, cfg, rand);
            helixA = helix(localStart, localTip, 0.32, 2.6, rand.nextDouble() * Math.PI * 2.0D, 48);
            helixB = helix(localStart, localTip, 0.26, -3.4, rand.nextDouble() * Math.PI * 2.0D, 48);
        } else {
            if (extend < 1f) {
                RandomSource rand = RandomSource.create(seed + age * 31L);
                BoltGeometry.Cfg cfg = new BoltGeometry.Cfg();
                cfg.detail = 5; cfg.chaos = 0.045; cfg.maxBranchDepth = 1; cfg.branchChance = 0.13f;
                shape = BoltGeometry.build(start.subtract(camPos), tip.subtract(camPos), cfg, rand);
            }
        }

        Vec3 camLocal = Vec3.ZERO;

        for (BoltGeometry.Strand s : shape) {
            BoltRenderer.renderStrand(mat, vc, s.points(), camLocal,
                    width * s.widthScale(), r, g, b, alpha * s.alphaScale());
        }

        if (helixA != null) {
            BoltRenderer.ribbon(mat, vc, helixA, camLocal, width * 0.45f, r, g, b, alpha * 0.55f);
            BoltRenderer.ribbon(mat, vc, helixB, camLocal, width * 0.35f, r, g, b, alpha * 0.40f);
        }

        BoltRenderer.renderFlare(mat, vc, start.subtract(camPos), camLocal,
                0.9f + 0.4f * alpha, 0.9f, 0.95f, 1.0f, alpha * 0.7f);

        if (extend >= 0.999f) {
            float hit = Mth.clamp(1.0f - (t - 2.0f) / 4.0f, 0f, 1f) * alpha;
            BoltRenderer.renderFlare(mat, vc, end.subtract(camPos), camLocal,
                    1.2f + 3.2f * hit, 0.9f, 0.95f, 1.0f, hit * 0.85f);
        }
    }

    private static List<Vec3> helix(Vec3 a, Vec3 b, double radius, double turns, double phase, int steps) {
        List<Vec3> pts = new ArrayList<>(steps + 1);
        Vec3 axis = b.subtract(a);
        double len = axis.length();
        if (len < 1e-4) return pts;
        Vec3 dir = axis.scale(1.0 / len);
        Vec3 ref = Math.abs(dir.y) > 0.95 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 n1 = dir.cross(ref).normalize();
        Vec3 n2 = dir.cross(n1).normalize();

        for (int i = 0; i <= steps; i++) {
            double f = (double) i / steps;
            double ang = phase + f * turns * Math.PI * 2;
            double rr = radius * Math.sin(f * Math.PI);
            pts.add(a.add(dir.scale(len * f))
                    .add(n1.scale(Math.cos(ang) * rr))
                    .add(n2.scale(Math.sin(ang) * rr)));
        }
        return pts;
    }

    public static final class Manager {
        private static final List<TrueBolt> ACTIVE = new ArrayList<>();

        public static void spawn(TrueBolt bolt) {
            synchronized (ACTIVE) {
                if (ACTIVE.size() > 64) ACTIVE.remove(0);
                ACTIVE.add(bolt);
            }
        }

        public static void spawn(Vec3 from, Vec3 to) {
            spawn(TrueBolt.of(from, to));
        }

        public static void tick() {
            synchronized (ACTIVE) {
                Iterator<TrueBolt> it = ACTIVE.iterator();
                while (it.hasNext()) {
                    TrueBolt s = it.next();
                    s.age++;
                    if (s.isDone()) it.remove();
                }
            }
        }

        public static void clear() {
            synchronized (ACTIVE) { ACTIVE.clear(); }
        }

        public static void renderAll(PoseStack pose, Vec3 camPos, float partialTick) {
            List<TrueBolt> snapshot;
            synchronized (ACTIVE) {
                if (ACTIVE.isEmpty()) return;
                snapshot = new ArrayList<>(ACTIVE);
            }

            MultiBufferSource.BufferSource src = Minecraft.getInstance().renderBuffers().bufferSource();
            VertexConsumer vc = src.getBuffer(EXERenderHelper.BOLT);
            Matrix4f mat = pose.last().pose();

            for (TrueBolt s : snapshot) {
                s.render(mat, vc, camPos, partialTick);
            }
            src.endBatch(EXERenderHelper.BOLT);
        }

        private Manager() {}
    }
}
