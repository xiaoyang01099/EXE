package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.miaoOutline;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MiaoOutlineCapturedBatch {
    public final VertexFormat.Mode mode;
    public final ResourceLocation texture;
    public final List<MiaoOutlineCapturedVertex> vertices = new ArrayList<>();
    public boolean hasUv;

    public MiaoOutlineCapturedBatch(VertexFormat.Mode mode, ResourceLocation texture) {
        this.mode = mode;
        this.texture = texture;
        this.hasUv = false;
    }

    public void addVertex(double x, double y, double z, float u, float v, boolean hasUv,
                          float normalX, float normalY, float normalZ, boolean hasNormal) {
        vertices.add(new MiaoOutlineCapturedVertex(x, y, z, u, v, hasUv, normalX, normalY, normalZ, hasNormal));
        this.hasUv |= hasUv;
    }

    public boolean hasTexturedMask() {
        return texture != null && hasUv;
    }

    public boolean isEmpty() {
        return vertices.isEmpty();
    }

    public List<MiaoOutlineCapturedVertex> getVertices() {
        return Collections.unmodifiableList(vertices);
    }
}
