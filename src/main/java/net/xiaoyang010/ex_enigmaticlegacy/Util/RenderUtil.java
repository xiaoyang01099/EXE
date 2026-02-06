package net.xiaoyang010.ex_enigmaticlegacy.Util;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.math.Matrix4f;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class RenderUtil {
    public static ResourceLocation beam_texture = new ResourceLocation("ex_enigmaticlegacy", "textures/effect/beam.png");
    public static ResourceLocation glow_texture = new ResourceLocation("ex_enigmaticlegacy", "textures/effect/glow.png");
    public static int maxLightX = 0xF000F0;
    public static int maxLightY = 0xF000F0;

    public static void renderBeam(BufferBuilder buf, Matrix4f matrix,
                                  double x1, double y1, double z1,
                                  double x2, double y2, double z2,
                                  float r1, float g1, float b1, float a1,
                                  float r2, float g2, float b2, float a2,
                                  double width) {
        float yaw = (float) Math.atan2(x2 - x1, z2 - z1);
        float pitch = (float) Math.atan2(y2 - y1, Mth.sqrt((float) (Math.pow(x2 - x1, 2.0) + Math.pow(z2 - z1, 2.0))));

        double tX1 = width * Mth.cos(yaw);
        double tY1 = 0.0;
        double tZ1 = -width * Mth.sin(yaw);
        double tX2 = width * Mth.sin(yaw) * (-Mth.sin(pitch));
        double tY2 = width * Mth.cos(pitch);
        double tZ2 = width * Mth.cos(yaw) * (-Mth.sin(pitch));

        addVertex(buf, matrix, x1 - tX1, y1 - tY1, z1 - tZ1, 0.0f, 0.0f, r1, g1, b1, a1);
        addVertex(buf, matrix, x2 - tX1, y2 - tY1, z2 - tZ1, 1.0f, 0.0f, r2, g2, b2, a2);
        addVertex(buf, matrix, x2 + tX1, y2 + tY1, z2 + tZ1, 1.0f, 1.0f, r2, g2, b2, a2);
        addVertex(buf, matrix, x1 + tX1, y1 + tY1, z1 + tZ1, 0.0f, 1.0f, r1, g1, b1, a1);

        addVertex(buf, matrix, x1 - tX2, y1 - tY2, z1 - tZ2, 0.0f, 0.0f, r1, g1, b1, a1);
        addVertex(buf, matrix, x2 - tX2, y2 - tY2, z2 - tZ2, 1.0f, 0.0f, r2, g2, b2, a2);
        addVertex(buf, matrix, x2 + tX2, y2 + tY2, z2 + tZ2, 1.0f, 1.0f, r2, g2, b2, a2);
        addVertex(buf, matrix, x1 + tX2, y1 + tY2, z1 + tZ2, 0.0f, 1.0f, r1, g1, b1, a1);
    }

    public static void renderBeam(BufferBuilder buf, Matrix4f matrix,
                                  double x1, double y1, double z1,
                                  double x2, double y2, double z2,
                                  float r1, float g1, float b1, float a1,
                                  float r2, float g2, float b2, float a2,
                                  double width, double angle) {
        float rads = (float) Math.toRadians(angle);
        double ac = Mth.cos(rads);
        double as = Mth.sin(rads);

        float yaw = (float) Math.atan2(x2 - x1, z2 - z1);
        float pitch = (float) Math.atan2(y2 - y1, Mth.sqrt((float) (Math.pow(x2 - x1, 2.0) + Math.pow(z2 - z1, 2.0))));

        double tX1 = width * Mth.cos(yaw);
        double tY1 = 0.0;
        double tZ1 = -width * Mth.sin(yaw);
        double tX2 = width * Mth.sin(yaw) * (-Mth.sin(pitch));
        double tY2 = width * Mth.cos(pitch);
        double tZ2 = width * Mth.cos(yaw) * (-Mth.sin(pitch));

        double tXc = tX1 * ac + tX2 * as;
        double tYc = tY1 * ac + tY2 * as;
        double tZc = tZ1 * ac + tZ2 * as;
        double tXs = tX1 * -as + tX2 * ac;
        double tYs = tY1 * -as + tY2 * ac;
        double tZs = tZ1 * -as + tZ2 * ac;

        addVertex(buf, matrix, x1 - tXs, y1 - tYs, z1 - tZs, 0.0f, 0.0f, r1, g1, b1, a1);
        addVertex(buf, matrix, x2 - tXs, y2 - tYs, z2 - tZs, 1.0f, 0.0f, r2, g2, b2, a2);
        addVertex(buf, matrix, x2 + tXs, y2 + tYs, z2 + tZs, 1.0f, 1.0f, r2, g2, b2, a2);
        addVertex(buf, matrix, x1 + tXs, y1 + tYs, z1 + tZs, 0.0f, 1.0f, r1, g1, b1, a1);

        addVertex(buf, matrix, x1 - tXc, y1 - tYc, z1 - tZc, 0.0f, 0.0f, r1, g1, b1, a1);
        addVertex(buf, matrix, x2 - tXc, y2 - tYc, z2 - tZc, 1.0f, 0.0f, r2, g2, b2, a2);
        addVertex(buf, matrix, x2 + tXc, y2 + tYc, z2 + tZc, 1.0f, 1.0f, r2, g2, b2, a2);
        addVertex(buf, matrix, x1 + tXc, y1 + tYc, z1 + tZc, 0.0f, 1.0f, r1, g1, b1, a1);
    }

    public static void renderBeam(BufferBuilder buf, Matrix4f matrix,
                                  double x1, double y1, double z1,
                                  double x2, double y2, double z2,
                                  float r1, float g1, float b1, float a1,
                                  float r2, float g2, float b2, float a2,
                                  double width1, double width2, double angle) {
        float rads = (float) Math.toRadians(angle);
        double ac = Mth.cos(rads);
        double as = Mth.sin(rads);

        float yaw = (float) Math.atan2(x2 - x1, z2 - z1);
        float pitch = (float) Math.atan2(y2 - y1, Mth.sqrt((float) (Math.pow(x2 - x1, 2.0) + Math.pow(z2 - z1, 2.0))));

        double tX1 = Mth.cos(yaw);
        double tY1 = 0.0;
        double tZ1 = -Mth.sin(yaw);
        double tX2 = Mth.sin(yaw) * (-Mth.sin(pitch));
        double tY2 = Mth.cos(pitch);
        double tZ2 = Mth.cos(yaw) * (-Mth.sin(pitch));

        double tXc1 = width1 * (tX1 * ac + tX2 * as);
        double tYc1 = width1 * (tY1 * ac + tY2 * as);
        double tZc1 = width1 * (tZ1 * ac + tZ2 * as);
        double tXs1 = width1 * (tX1 * -as + tX2 * ac);
        double tYs1 = width1 * (tY1 * -as + tY2 * ac);
        double tZs1 = width1 * (tZ1 * -as + tZ2 * ac);
        double tXc2 = width2 * (tX1 * ac + tX2 * as);
        double tYc2 = width2 * (tY1 * ac + tY2 * as);
        double tZc2 = width2 * (tZ1 * ac + tZ2 * as);
        double tXs2 = width2 * (tX1 * -as + tX2 * ac);
        double tYs2 = width2 * (tY1 * -as + tY2 * ac);
        double tZs2 = width2 * (tZ1 * -as + tZ2 * ac);

        addVertex(buf, matrix, x1 - tXs1, y1 - tYs1, z1 - tZs1, 0.0f, 0.0f, r1, g1, b1, a1);
        addVertex(buf, matrix, x2 - tXs2, y2 - tYs2, z2 - tZs2, 1.0f, 0.0f, r2, g2, b2, a2);
        addVertex(buf, matrix, x2 + tXs2, y2 + tYs2, z2 + tZs2, 1.0f, 1.0f, r2, g2, b2, a2);
        addVertex(buf, matrix, x1 + tXs1, y1 + tYs1, z1 + tZs1, 0.0f, 1.0f, r1, g1, b1, a1);

        addVertex(buf, matrix, x1 - tXc1, y1 - tYc1, z1 - tZc1, 0.0f, 0.0f, r1, g1, b1, a1);
        addVertex(buf, matrix, x2 - tXc2, y2 - tYc2, z2 - tZc2, 1.0f, 0.0f, r2, g2, b2, a2);
        addVertex(buf, matrix, x2 + tXc2, y2 + tYc2, z2 + tZc2, 1.0f, 1.0f, r2, g2, b2, a2);
        addVertex(buf, matrix, x1 + tXc1, y1 + tYc1, z1 + tZc1, 0.0f, 1.0f, r1, g1, b1, a1);
    }

    public static void renderSlash(BufferBuilder buf, Matrix4f matrix,
                                   double x0, double y0, double z0,
                                   float r, float g, float b, float a,
                                   float radius, float width, float angleRange) {
        float halfRange = angleRange / 2.0f;
        float step = angleRange / 16.0f;

        for (float i = -halfRange; i < halfRange; i += step) {
            float nextI = i + step;
            float coeff1 = 1.0f - Math.abs(i) / halfRange;
            float coeff2 = 1.0f - Math.abs(nextI) / halfRange;

            double zOffset = radius / 2.0f;

            double x1 = x0 + radius * Math.sin(Math.toRadians(i));
            double z1 = z0 + radius * Math.cos(Math.toRadians(i));
            double x2 = x0 + (radius + 0.5f * coeff1 * width) * Math.sin(Math.toRadians(i));
            double z2 = z0 + (radius + 0.5f * coeff1 * width) * Math.cos(Math.toRadians(i));
            double x3 = x0 + (radius + 0.5f * coeff2 * width) * Math.sin(Math.toRadians(nextI));
            double z3 = z0 + (radius + 0.5f * coeff2 * width) * Math.cos(Math.toRadians(nextI));
            double x4 = x0 + radius * Math.sin(Math.toRadians(nextI));
            double z4 = z0 + radius * Math.cos(Math.toRadians(nextI));

            addVertex(buf, matrix, x1, y0, z1 - zOffset, 1.0f, 1.0f, r, g, b, a * coeff1);
            addVertex(buf, matrix, x2, y0, z2 - zOffset, 1.0f, 0.0f, r, g, b, a * coeff1);
            addVertex(buf, matrix, x3, y0, z3 - zOffset, 0.0f, 0.0f, r, g, b, a * coeff2);
            addVertex(buf, matrix, x4, y0, z4 - zOffset, 0.0f, 1.0f, r, g, b, a * coeff2);

            x1 = x0 + radius * Math.sin(Math.toRadians(i));
            z1 = z0 + radius * Math.cos(Math.toRadians(i));
            x2 = x0 + (radius - 0.5f * coeff1 * width) * Math.sin(Math.toRadians(i));
            z2 = z0 + (radius - 0.5f * coeff1 * width) * Math.cos(Math.toRadians(i));
            x3 = x0 + (radius - 0.5f * coeff2 * width) * Math.sin(Math.toRadians(nextI));
            z3 = z0 + (radius - 0.5f * coeff2 * width) * Math.cos(Math.toRadians(nextI));
            x4 = x0 + radius * Math.sin(Math.toRadians(nextI));
            z4 = z0 + radius * Math.cos(Math.toRadians(nextI));

            addVertex(buf, matrix, x1, y0, z1 - zOffset, 1.0f, 1.0f, r, g, b, a * coeff1);
            addVertex(buf, matrix, x2, y0, z2 - zOffset, 1.0f, 0.0f, r, g, b, a * coeff1);
            addVertex(buf, matrix, x3, y0, z3 - zOffset, 0.0f, 0.0f, r, g, b, a * coeff2);
            addVertex(buf, matrix, x4, y0, z4 - zOffset, 0.0f, 1.0f, r, g, b, a * coeff2);

            addVertex(buf, matrix, x1, y0, z1 - zOffset, 1.0f, 1.0f, r, g, b, a * coeff1);
            addVertex(buf, matrix, x2, y0, z2 - zOffset, 1.0f, 0.0f, r, g, b, a * coeff1);
            addVertex(buf, matrix, x3, y0, z3 - zOffset, 0.0f, 0.0f, r, g, b, a * coeff2);
            addVertex(buf, matrix, x4, y0, z4 - zOffset, 0.0f, 1.0f, r, g, b, a * coeff2);

            x1 = x0 + radius * Math.sin(Math.toRadians(i));
            z1 = z0 + radius * Math.cos(Math.toRadians(i));
            x2 = x0 + radius * Math.sin(Math.toRadians(i));
            z2 = z0 + radius * Math.cos(Math.toRadians(i));
            x3 = x0 + radius * Math.sin(Math.toRadians(nextI));
            z3 = z0 + radius * Math.cos(Math.toRadians(nextI));
            x4 = x0 + radius * Math.sin(Math.toRadians(nextI));
            z4 = z0 + radius * Math.cos(Math.toRadians(nextI));

            addVertex(buf, matrix, x1, y0, z1 - zOffset, 1.0f, 1.0f, r, g, b, a * coeff1);
            addVertex(buf, matrix, x2, y0 - (width * 0.5f * coeff1), z2 - zOffset, 1.0f, 0.0f, r, g, b, a * coeff1);
            addVertex(buf, matrix, x3, y0 - (width * 0.5f * coeff2), z3 - zOffset, 0.0f, 0.0f, r, g, b, a * coeff2);
            addVertex(buf, matrix, x4, y0, z4 - zOffset, 0.0f, 1.0f, r, g, b, a * coeff2);

            addVertex(buf, matrix, x1, y0, z1 - zOffset, 1.0f, 1.0f, r, g, b, a * coeff1);
            addVertex(buf, matrix, x2, y0 + (width * 0.5f * coeff1), z2 - zOffset, 1.0f, 0.0f, r, g, b, a * coeff1);
            addVertex(buf, matrix, x3, y0 + (width * 0.5f * coeff2), z3 - zOffset, 0.0f, 0.0f, r, g, b, a * coeff2);
            addVertex(buf, matrix, x4, y0, z4 - zOffset, 0.0f, 1.0f, r, g, b, a * coeff2);
        }
    }

    private static void addVertex(BufferBuilder buf, Matrix4f matrix,
                                  double x, double y, double z,
                                  float u, float v,
                                  float r, float g, float b, float a) {
        buf.vertex(matrix, (float) x, (float) y, (float) z)
                .uv(u, v)
                .uv2(maxLightX, maxLightY)
                .color(r, g, b, a)
                .endVertex()
        ;
    }
}
