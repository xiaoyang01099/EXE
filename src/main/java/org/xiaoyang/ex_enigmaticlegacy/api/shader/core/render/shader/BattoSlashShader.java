package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.shader;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.joml.Matrix4f;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

import java.io.IOException;

public class BattoSlashShader {
    public static ShaderInstance shader;
    public static AbstractUniform materialParams;
    public static AbstractUniform pannerParams;
    public static AbstractUniform mainSpriteUV;
    public static AbstractUniform texBSpriteUV;
    public static AbstractUniform maskSpriteUV;
    public static AbstractUniform uView;

    public static ShaderInstance reloadShaders(ResourceProvider manager) throws IOException {
        return new ShaderInstance(
                manager,
                new ResourceLocation(Exe.MODID, "batto_slash"),
                DefaultVertexFormat.POSITION_COLOR_TEX
        );
    }

    public static void onLoad(ShaderInstance shaderInstance) {
        shader = shaderInstance;
        materialParams = shaderInstance.safeGetUniform("MaterialParams");
        pannerParams = shaderInstance.safeGetUniform("PannerParams");
        mainSpriteUV = shaderInstance.safeGetUniform("MainSpriteUV");
        texBSpriteUV = shaderInstance.safeGetUniform("TexBSpriteUV");
        maskSpriteUV = shaderInstance.safeGetUniform("MaskSpriteUV");
        uView = shaderInstance.safeGetUniform("uView");
    }

    public static void setMaterialParams(float time, float bloomStrength, float noiseStrength, float intensity) {
        materialParams.set(time, bloomStrength, noiseStrength, intensity);
    }

    public static void setPannerParams(float speedAX, float speedAY, float speedBX, float speedBY) {
        pannerParams.set(speedAX, speedAY, speedBX, speedBY);
    }

    public static void setSpriteUVs(float mainU0, float mainV0, float mainU1, float mainV1,
                                    float texBU0, float texBV0, float texBU1, float texBV1,
                                    float maskU0, float maskV0, float maskU1, float maskV1) {
        mainSpriteUV.set(mainU0, mainV0, mainU1, mainV1);
        texBSpriteUV.set(texBU0, texBV0, texBU1, texBV1);
        maskSpriteUV.set(maskU0, maskV0, maskU1, maskV1);
    }

    public static void setView(Matrix4f view) {
        uView.set(view);
    }

    public static void setSamplers(int atlasTextureId) {
        if (shader == null) return;
        shader.setSampler("Sampler0", atlasTextureId);
    }

    public static ShaderInstance getShader() {
        return shader;
    }

    public static boolean isLoaded() {
        return shader != null;
    }
}
