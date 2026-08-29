package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.miaoOutline;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class MiaoOutlineCaptureOnlyVertexConsumer implements VertexConsumer {
    private final MiaoOutlineCapturedBatch batch;
    private double x;
    private double y;
    private double z;
    private float u;
    private float v;
    private float normalX;
    private float normalY;
    private float normalZ;
    private boolean hasPosition;
    private boolean hasUv;
    private boolean hasNormal;

    public MiaoOutlineCaptureOnlyVertexConsumer(MiaoOutlineCapturedBatch batch) {
        this.batch = batch;
    }

    @Override
    public VertexConsumer vertex(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.hasPosition = true;
        this.hasUv = false;
        this.hasNormal = false;
        return this;
    }

    @Override
    public VertexConsumer vertex(Matrix4f matrix, float x, float y, float z) {
        Vector3f transformed = matrix.transformPosition(x, y, z, new Vector3f());
        return vertex(transformed.x(), transformed.y(), transformed.z());
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        return this;
    }

    @Override
    public VertexConsumer uv(float u, float v) {
        this.u = u;
        this.v = v;
        this.hasUv = true;
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
        this.normalX = normalX;
        this.normalY = normalY;
        this.normalZ = normalZ;
        this.hasNormal = true;
        return this;
    }

    @Override
    public void endVertex() {
        if (hasPosition) {
            batch.addVertex(x, y, z, u, v, hasUv, normalX, normalY, normalZ, hasNormal);
        }
        hasPosition = false;
        hasUv = false;
        hasNormal = false;
    }

    @Override
    public void defaultColor(int red, int green, int blue, int alpha) {
    }

    @Override
    public void unsetDefaultColor() {
    }
}
