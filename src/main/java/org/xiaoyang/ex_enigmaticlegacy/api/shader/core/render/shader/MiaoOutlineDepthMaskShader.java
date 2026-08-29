package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.shader;

import com.mojang.blaze3d.shaders.AbstractUniform;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.joml.Matrix4f;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

import java.io.IOException;

public class MiaoOutlineDepthMaskShader {
    private static ShaderInstance shader;
    private static AbstractUniform uView;
    private static AbstractUniform depthParams;

    public static ShaderInstance reloadShaders(ResourceProvider manager) throws IOException {
        return new ShaderInstance(
                manager,
                new ResourceLocation(Exe.MODID, "miao_outline_depth_mask"),
                com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL
        );
    }

    public static void onLoad(ShaderInstance shaderInstance) {
        shader = shaderInstance;
        uView = shaderInstance.safeGetUniform("uView");
        depthParams = shaderInstance.safeGetUniform("DepthParams");
    }

    public static ShaderInstance getShader() {
        return shader;
    }

    public static boolean isLoaded() {
        return shader != null;
    }

    public static void setView(Matrix4f viewMatrix) {
        if (uView == null || viewMatrix == null) return;
        uView.set(viewMatrix);
    }

    public static void setDepthParams(float nearDepth, float depthRange, float maskValue, float alphaCutoff) {
        if (depthParams == null) return;
        depthParams.set(nearDepth, Math.max(depthRange, 1.0f), maskValue, alphaCutoff);
    }
}