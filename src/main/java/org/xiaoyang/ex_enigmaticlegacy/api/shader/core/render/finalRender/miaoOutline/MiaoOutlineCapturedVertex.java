package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.miaoOutline;

public class MiaoOutlineCapturedVertex {
    public final double x;
    public final double y;
    public final double z;
    public final float u;
    public final float v;
    public final boolean hasUv;
    public final float normalX;
    public final float normalY;
    public final float normalZ;
    public final boolean hasNormal;

    public MiaoOutlineCapturedVertex(double x, double y, double z, float u, float v, boolean hasUv,
                                     float normalX, float normalY, float normalZ, boolean hasNormal) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.u = u;
        this.v = v;
        this.hasUv = hasUv;
        this.normalX = normalX;
        this.normalY = normalY;
        this.normalZ = normalZ;
        this.hasNormal = hasNormal;
    }
}
