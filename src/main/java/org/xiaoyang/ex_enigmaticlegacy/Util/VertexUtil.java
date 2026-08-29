package org.xiaoyang.ex_enigmaticlegacy.Util;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class VertexUtil {
    public static void putVertexUV2(VertexConsumer buffer, Matrix4f matrix, Vec3 point, float u, float t) {
        buffer.vertex(matrix, (float) point.x, (float) point.y, (float) point.z)
                .color(u, t, 0.5f, t)
                .uv(u, t)
                .uv2(0xF000F0)
                .endVertex();
    }


    public static void putVertex(VertexConsumer consumer, Vec3 s0, float u, float v, float r, float g, float b, float alpha) {
        consumer.vertex( (float) s0.x, (float) s0.y, (float) s0.z)
                .color(r, g, b, alpha)
                .uv(u, v)
                .endVertex();
    }
    public static void putVertex(VertexConsumer buffer, Matrix4f matrix, Vec3 point, float u, float t) {
        buffer.vertex(matrix, (float) point.x, (float) point.y, (float) point.z)
                .color(u, t, 0.5f, t)
                .uv(u, t)
                .endVertex();
    }

    public static void putVertex(VertexConsumer consumer, Matrix4f matrix, Vec3 s0, float u, float v, float r, float g, float b, float alpha) {
        consumer.vertex(matrix, (float) s0.x, (float) s0.y, (float) s0.z)
                .color(r, g, b, alpha)
                .uv(u, v)
                .endVertex();
    }
}
