package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.miaoOutline;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

// MiaoOutlineDiscardVertexConsumer 吃掉不参与描边 mask 的 RenderType 顶点。
public class MiaoOutlineDiscardVertexConsumer implements VertexConsumer {
    public static final MiaoOutlineDiscardVertexConsumer INSTANCE = new MiaoOutlineDiscardVertexConsumer(); // 全局共享的丢弃 consumer。

    // 接收普通顶点坐标但不保存。
    @Override
    public VertexConsumer vertex(double x, double y, double z) {
        return this;
    }

    // 接收矩阵顶点坐标但不保存。
    @Override
    public VertexConsumer vertex(Matrix4f matrix, float x, float y, float z) {
        return this;
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        return this;
    }

    @Override
    public VertexConsumer uv(float u, float v) {
        return this;
    }

    @Override
    public VertexConsumer overlayCoords(int u, int v) {
        return this;
    }

    @Override
    public VertexConsumer uv2(int u, int v) {
        return this;
    }

    @Override
    public VertexConsumer normal(float normalX, float normalY, float normalZ) {
        return this;
    }

    // 结束顶点时不写入任何捕获批次。
    @Override
    public void endVertex() {
    }

    @Override
    public void defaultColor(int red, int green, int blue, int alpha) {
    }

    @Override
    public void unsetDefaultColor() {
    }
}
