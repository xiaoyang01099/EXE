package org.xiaoyang.ex_enigmaticlegacy.Client.renderer.others;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class TrailRibbonRenderer {
    public static final float HALF_WIDTH = 0.15f;

    public static void render(List<Vec3> points, VertexConsumer buffer,
                              Matrix4f matrix, Vec3 cameraPos, Vec3 offset) {
        int size = points.size();
        if (size < 2) return;

        List<RibbonGeometryRender.RibbonCenter> centers = buildCenters(points, offset);
        List<RibbonGeometryRender.RibbonPoint> ribbonPoints = RibbonGeometryRender.buildBillboardRibbon(centers, cameraPos, HALF_WIDTH);
        if (ribbonPoints.size() < 2) return;

        for (int i = 0; i < ribbonPoints.size() - 1; i++) {
            RibbonGeometryRender.RibbonPoint start = ribbonPoints.get(i);
            RibbonGeometryRender.RibbonPoint end = ribbonPoints.get(i + 1);
            putVertex(buffer, matrix, start.left.subtract(cameraPos), 0.0f, start.t);
            putVertex(buffer, matrix, start.right.subtract(cameraPos), 1.0f, start.t);
            putVertex(buffer, matrix, end.right.subtract(cameraPos), 1.0f, end.t);
            putVertex(buffer, matrix, end.left.subtract(cameraPos), 0.0f, end.t);
        }
    }

    public static List<RibbonGeometryRender.RibbonCenter> buildCenters(List<Vec3> points, Vec3 offset) {
        List<RibbonGeometryRender.RibbonCenter> centers = new ArrayList<>();
        int size = points.size();
        if (size == 0) return centers;

        for (int i = 0; i < size; i++) {
            float t = size == 1 ? 0.0F : (float) i / (float) (size - 1);
            centers.add(new RibbonGeometryRender.RibbonCenter(points.get(i).add(offset), t));
        }
        return centers;
    }

    public static void putVertex(VertexConsumer buffer, Matrix4f matrix, Vec3 point, float u, float t) {
        buffer.vertex(matrix, (float) point.x, (float) point.y, (float) point.z)
                .color(u, t, 0.5f, t)
                .uv(u, t)
                .endVertex();
    }
}
