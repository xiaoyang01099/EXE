package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.shader;

import com.mojang.blaze3d.shaders.AbstractUniform;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.joml.Matrix4f;
import org.xiaoyang.ex_enigmaticlegacy.Exe;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.bloom.LightningVertexFormat;

import java.io.IOException;

public class ShockwaveShader {
    public static ShaderInstance shader;
    public static AbstractUniform effectParams;
    public static AbstractUniform tintParams;
    public static AbstractUniform radialParams;
    public static AbstractUniform uvAnimParams;
    public static AbstractUniform shapeParams;
    public static AbstractUniform revealParams;
    public static AbstractUniform shockwaveSpriteUV;
    public static AbstractUniform uView;

    public static ShaderInstance reloadShaders(ResourceProvider manager) throws IOException {
        return new ShaderInstance(
                manager,
                new ResourceLocation(Exe.MODID, "shockwave"),
                LightningVertexFormat.FORMAT
        );
    }

    public static void onLoad(ShaderInstance shaderInstance) {
        shader = shaderInstance;
        effectParams = shaderInstance.safeGetUniform("EffectParams");
        tintParams = shaderInstance.safeGetUniform("TintParams");
        radialParams = shaderInstance.safeGetUniform("RadialParams");
        uvAnimParams = shaderInstance.safeGetUniform("UvAnimParams");
        shapeParams = shaderInstance.safeGetUniform("ShapeParams");
        revealParams = shaderInstance.safeGetUniform("RevealParams");
        shockwaveSpriteUV = shaderInstance.safeGetUniform("ShockwaveSpriteUV");
        uView = shaderInstance.safeGetUniform("uView");
    }

    public static void setEffectParams(float time, float bloomStrength, float reservedRadialScale, float intensity) {
        effectParams.set(time, bloomStrength, reservedRadialScale, intensity);
    }

    public static void setTintParams(float r, float g, float b, float reserved) {
        tintParams.set(r, g, b, reserved);
    }

    public static void setRadialParams(float angleScale, float radiusScale, float radiusNormalize, float angleOffset) {
        radialParams.set(angleScale, radiusScale, radiusNormalize, angleOffset);
    }

    public static void setUvAnimParams(float uvOffsetX, float timeSpeed, float uvOffsetY, float reserved) {
        uvAnimParams.set(uvOffsetX, timeSpeed, uvOffsetY, reserved);
    }

    public static void setShapeParams(float edgeFadeStart, float edgeFadeEnd, float opacityScale, float reserved) {
        shapeParams.set(edgeFadeStart, edgeFadeEnd, opacityScale, reserved);
    }

    public static void setRevealParams(float visibleStart, float visibleEnd, float startSoftness, float endSoftness) {
        revealParams.set(visibleStart, visibleEnd, startSoftness, endSoftness);
    }

    public static void setShockwaveSpriteUV(float u0, float v0, float u1, float v1) {
        shockwaveSpriteUV.set(u0, v0, u1, v1);
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
