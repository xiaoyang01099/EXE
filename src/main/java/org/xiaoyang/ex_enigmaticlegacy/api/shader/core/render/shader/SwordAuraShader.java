package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.shader;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.joml.Matrix4f;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

import java.io.IOException;

public class SwordAuraShader {
    private static ShaderInstance shader;
    private static AbstractUniform globalParams;
    private static AbstractUniform swordSpriteUV;
    private static AbstractUniform gradientSpriteUV;
    private static AbstractUniform blueGradientSpriteUV;
    private static AbstractUniform uView;
    private static AbstractUniform projectionMat;

    public static ShaderInstance reloadShaders(ResourceProvider manager) throws IOException {
        return new ShaderInstance(
                manager,
                new ResourceLocation(Exe.MODID, "sword_aura"),
                DefaultVertexFormat.POSITION_TEX
        );
    }

    public static void onLoad(ShaderInstance shaderInstance) {
        shader = shaderInstance;
        globalParams = shaderInstance.safeGetUniform("GlobalParams");
        swordSpriteUV = shaderInstance.safeGetUniform("SwordSpriteUV");
        gradientSpriteUV = shaderInstance.safeGetUniform("GradientSpriteUV");
        blueGradientSpriteUV = shaderInstance.safeGetUniform("BlueGradientSpriteUV");
        uView = shaderInstance.safeGetUniform("uView");
        projectionMat = shaderInstance.safeGetUniform("ProjMat");
    }

    public static void setGlobalParams(float time, float bloomScale) {
        globalParams.set(time, bloomScale, 0.0F, 0.0F);
    }

    public static void setSpriteUVs(float swordU0, float swordV0, float swordU1, float swordV1,
                                    float gradientU0, float gradientV0, float gradientU1, float gradientV1,
                                    float blueU0, float blueV0, float blueU1, float blueV1) {
        swordSpriteUV.set(swordU0, swordV0, swordU1, swordV1);
        gradientSpriteUV.set(gradientU0, gradientV0, gradientU1, gradientV1);
        blueGradientSpriteUV.set(blueU0, blueV0, blueU1, blueV1);
    }

    public static void setView(Matrix4f view) {
        uView.set(view);
    }

    public static void setProjection(Matrix4f projection) {
        projectionMat.set(projection);
    }

    public static ShaderInstance getShader() {
        return shader;
    }

    public static boolean isLoaded() {
        return shader != null;
    }
}
