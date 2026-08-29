package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.shader;

import com.mojang.blaze3d.shaders.AbstractUniform;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.joml.Matrix4f;
import org.xiaoyang.ex_enigmaticlegacy.Exe;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.bloom.LightningVertexFormat;

import java.io.IOException;

public class LightningShader {
    public static ShaderInstance shader;
    public static AbstractUniform effectParams;
    public static AbstractUniform renderFlags;
    public static AbstractUniform pannerParams;
    public static AbstractUniform bloomParams;
    public static AbstractUniform lightningSpriteUV;
    public static AbstractUniform noiseSpriteUV;
    public static AbstractUniform noiseSpriteUVAlt;
    public static AbstractUniform uView; // 世界到视图矩阵。

    public static ShaderInstance reloadShaders(ResourceProvider manager) throws IOException {
        return new ShaderInstance(
                manager,
                new ResourceLocation(Exe.MODID, "lightning"),
                LightningVertexFormat.FORMAT
        );
    }

    public static void onLoad(ShaderInstance shaderInstance) {
        shader = shaderInstance;
        effectParams = shaderInstance.safeGetUniform("EffectParams");
        renderFlags = shaderInstance.safeGetUniform("RenderFlags");
        pannerParams = shaderInstance.safeGetUniform("PannerParams");
        bloomParams = shaderInstance.safeGetUniform("BloomParams");
        lightningSpriteUV = shaderInstance.safeGetUniform("LightningSpriteUV");
        noiseSpriteUV = shaderInstance.safeGetUniform("NoiseSpriteUV");
        noiseSpriteUVAlt = shaderInstance.safeGetUniform("NoiseSpriteUVAlt");
        uView = shaderInstance.safeGetUniform("uView");
    }

    public static void setEffectParams(float time, float bloomStrength, float noiseStrength, float intensity) {
        effectParams.set(time, bloomStrength, noiseStrength, intensity);
    }

    public static void setPannerParams(float speedX, float speedY, float flickerStrength, float reserved) {
        pannerParams.set(speedX, speedY, flickerStrength, reserved);
    }

    public static void setBloomParams(float ribbonAlpha, float ribbonColor, float coreAlphaFallback, float coreColorFallback) {
        bloomParams.set(ribbonAlpha, ribbonColor, coreAlphaFallback, coreColorFallback);
    }

    public static void setRenderFlags(int effectType, int bloomEnabled, int reserved0, int reserved1) {
        renderFlags.set(effectType, bloomEnabled, reserved0, reserved1);
    }

    public static void setLightningSpriteUV(float u0, float v0, float u1, float v1) {
        lightningSpriteUV.set(u0, v0, u1, v1);
    }

    public static void setNoiseSpriteUV(float u0, float v0, float u1, float v1) {
        noiseSpriteUV.set(u0, v0, u1, v1);
    }

    public static void setNoiseSpriteUVAlt(float u0, float v0, float u1, float v1) {
        noiseSpriteUVAlt.set(u0, v0, u1, v1);
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
