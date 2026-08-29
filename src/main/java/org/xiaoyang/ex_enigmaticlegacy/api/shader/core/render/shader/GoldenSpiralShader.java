package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.shader;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.joml.Matrix4f;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

import java.io.IOException;

public class GoldenSpiralShader {
    public static ShaderInstance shader; // 当前金色螺旋光效 shader 实例。
    public static AbstractUniform effectParams; // x=时间秒，y=bloom强度，z=自发光强度，w=全局透明度。
    public static AbstractUniform colorParams; // xyz=核心金色，w=颜色曲线指数。
    public static AbstractUniform edgeColor; // xyz=高亮金色，w=预留。
    public static AbstractUniform noise1SpriteUV; // 第一张噪声 sprite UV。
    public static AbstractUniform noise2SpriteUV; // 第二张 fx_noise015 噪声 sprite UV。
    public static AbstractUniform noise3SpriteUV; // 第三张 RG 扰动噪声 sprite UV。
    public static AbstractUniform noise1Params; // xy=第一张噪声平铺，zw=预留。
    public static AbstractUniform noise2Params; // xy=第二张噪声平铺，zw=预留。
    public static AbstractUniform noise3Params; // xy=第三张噪声平铺，z=扰动强度，w=预留。
    public static AbstractUniform noise1Flow; // xy=第一张噪声流速，zw=起始相位。
    public static AbstractUniform noise2Flow; // xy=第二张噪声流速，zw=起始相位。
    public static AbstractUniform noise3Flow; // xy=第三张噪声流速，zw=起始相位。
    public static AbstractUniform maskParams; // xy=mask 比例，z=半径，w=柔化宽度。
    public static AbstractUniform heightFade; // x=底部淡入，y=顶部淡出。
    public static AbstractUniform noiseCutoff; // x=噪声低阈值，y=噪声高阈值。
    public static AbstractUniform uView; // 世界到视图矩阵。

    public static ShaderInstance reloadShaders(ResourceProvider manager) throws IOException {
        return new ShaderInstance(
                manager,
                new ResourceLocation(Exe.MODID, "golden_spiral/golden_spiral"),
                DefaultVertexFormat.POSITION_TEX_COLOR
        );
    }

    public static void onLoad(ShaderInstance shaderInstance) {
        shader = shaderInstance;
        effectParams = shaderInstance.safeGetUniform("EffectParams");
        colorParams = shaderInstance.safeGetUniform("ColorParams");
        edgeColor = shaderInstance.safeGetUniform("EdgeColor");
        noise1SpriteUV = shaderInstance.safeGetUniform("Noise1SpriteUV");
        noise2SpriteUV = shaderInstance.safeGetUniform("Noise2SpriteUV");
        noise3SpriteUV = shaderInstance.safeGetUniform("Noise3SpriteUV");
        noise1Params = shaderInstance.safeGetUniform("Noise1Params");
        noise2Params = shaderInstance.safeGetUniform("Noise2Params");
        noise3Params = shaderInstance.safeGetUniform("Noise3Params");
        noise1Flow = shaderInstance.safeGetUniform("Noise1Flow");
        noise2Flow = shaderInstance.safeGetUniform("Noise2Flow");
        noise3Flow = shaderInstance.safeGetUniform("Noise3Flow");
        maskParams = shaderInstance.safeGetUniform("MaskParams");
        heightFade = shaderInstance.safeGetUniform("HeightFade");
        noiseCutoff = shaderInstance.safeGetUniform("NoiseCutoff");
        uView = shaderInstance.safeGetUniform("uView");
    }

    public static void setEffectParams(float time, float bloomStrength, float intensity, float opacity) {
        effectParams.set(time, bloomStrength, intensity, opacity);
    }

    public static void setColorParams(float r, float g, float b, float power) {
        colorParams.set(r, g, b, power);
    }

    public static void setEdgeColor(float r, float g, float b, float reserved) {
        edgeColor.set(r, g, b, reserved);
    }

    public static void setNoiseSpriteUVs(float n1u0, float n1v0, float n1u1, float n1v1,
                                         float n2u0, float n2v0, float n2u1, float n2v1,
                                         float n3u0, float n3v0, float n3u1, float n3v1) {
        noise1SpriteUV.set(n1u0, n1v0, n1u1, n1v1);
        noise2SpriteUV.set(n2u0, n2v0, n2u1, n2v1);
        noise3SpriteUV.set(n3u0, n3v0, n3u1, n3v1);
    }

    public static void setNoiseParams(float n1x, float n1y, float n2x, float n2y,
                                      float n3x, float n3y, float n3Strength) {
        noise1Params.set(n1x, n1y, 0.0F, 0.0F);
        noise2Params.set(n2x, n2y, 0.0F, 0.0F);
        noise3Params.set(n3x, n3y, n3Strength, 0.0F);
    }

    public static void setNoiseFlows(float n1sx, float n1sy, float n1px, float n1py,
                                     float n2sx, float n2sy, float n2px, float n2py,
                                     float n3sx, float n3sy, float n3px, float n3py) {
        noise1Flow.set(n1sx, n1sy, n1px, n1py);
        noise2Flow.set(n2sx, n2sy, n2px, n2py);
        noise3Flow.set(n3sx, n3sy, n3px, n3py);
    }

    public static void setMaskParams(float scaleX, float scaleY, float radius, float softness,
                                     float bottomFade, float topFade, float cutoffLow, float cutoffHigh) {
        maskParams.set(scaleX, scaleY, radius, softness);
        heightFade.set(bottomFade, topFade);
        noiseCutoff.set(cutoffLow, cutoffHigh);
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
