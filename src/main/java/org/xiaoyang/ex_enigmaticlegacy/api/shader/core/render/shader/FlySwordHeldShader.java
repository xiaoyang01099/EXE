package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.shader;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.joml.Matrix4f;
import org.xiaoyang.ex_enigmaticlegacy.Item.FlySwordHeldItemRenderer;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

import java.io.IOException;

public class FlySwordHeldShader {
    private static ShaderInstance shader;
    private static AbstractUniform modelViewMat;
    private static AbstractUniform effectParams;
    private static AbstractUniform mainSpriteUv;
    private static AbstractUniform noise1SpriteUv;
    private static AbstractUniform noise2SpriteUv;
    private static AbstractUniform noise3SpriteUv;
    private static AbstractUniform fresnelParams;
    private static AbstractUniform emissiveStrength;
    private static AbstractUniform gradientStartColor;
    private static AbstractUniform gradientEndColor;
    private static AbstractUniform screenSize;
    private static AbstractUniform noise1FlowParams;
    private static AbstractUniform noise2FlowParams;
    private static int sceneTextureId;

    public static ShaderInstance reloadShaders(ResourceProvider manager) throws IOException {
        return new ShaderInstance(
                manager,
                new ResourceLocation(Exe.MODID, "fly_sword/fly_sword_held"),
                DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL
        );
    }

    public static void onLoad(ShaderInstance shaderInstance) {
        shader = shaderInstance;
        modelViewMat = shaderInstance.safeGetUniform("FlySwordModelViewMat");
        effectParams = shaderInstance.safeGetUniform("EffectParams");
        mainSpriteUv = shaderInstance.safeGetUniform("MainSpriteUV");
        noise1SpriteUv = shaderInstance.safeGetUniform("Noise1SpriteUV");
        noise2SpriteUv = shaderInstance.safeGetUniform("Noise2SpriteUV");
        noise3SpriteUv = shaderInstance.safeGetUniform("Noise3SpriteUV");
        fresnelParams = shaderInstance.safeGetUniform("FresnelParams");
        emissiveStrength = shaderInstance.safeGetUniform("EmissiveStrength");
        gradientStartColor = shaderInstance.safeGetUniform("GradientStartColor");
        gradientEndColor = shaderInstance.safeGetUniform("GradientEndColor");
        screenSize = shaderInstance.safeGetUniform("ScreenSize");
        noise1FlowParams = shaderInstance.safeGetUniform("Noise1FlowParams");
        noise2FlowParams = shaderInstance.safeGetUniform("Noise2FlowParams");
    }

    public static void setModelViewMat(Matrix4f matrix) {
        modelViewMat.set(matrix);
    }

    public static void setEffectParams(float time, float bloomStrength) {
        effectParams.set(time, bloomStrength);
    }

    public static void setSpriteUvs(TextureAtlasSprite mainSprite, TextureAtlasSprite noise1Sprite, TextureAtlasSprite noise2Sprite, TextureAtlasSprite noise3Sprite) {
        setSpriteUv(mainSpriteUv, mainSprite);
        setSpriteUv(noise1SpriteUv, noise1Sprite);
        setSpriteUv(noise2SpriteUv, noise2Sprite);
        setSpriteUv(noise3SpriteUv, noise3Sprite);
    }

    public static void setSpriteUv(AbstractUniform uniform, TextureAtlasSprite sprite) {
        if (uniform == null || sprite == null) return;
        uniform.set(sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1());
    }

    public static void setFresnelParams(float power, float edgeStart, float edgeEnd) {
        fresnelParams.set(power, edgeStart, edgeEnd);
    }

    public static void setEmissiveStrength(float strength) {
        emissiveStrength.set(strength);
    }

    public static void setGradientStartColor(float red, float green, float blue) {
        gradientStartColor.set(red, green, blue);
    }

    public static void setGradientEndColor(float red, float green, float blue) {
        gradientEndColor.set(red, green, blue);
    }

    public static void setNoiseFlowParams(FlySwordHeldItemRenderer.FlySwordFlowParams flowParams) {
        if (flowParams == null) return;
        noise1FlowParams.set(flowParams.noise1SpeedX, flowParams.noise1SpeedY, flowParams.noise1PhaseX, flowParams.noise1PhaseY);
        noise2FlowParams.set(flowParams.noise2SpeedX, flowParams.noise2SpeedY, flowParams.noise2PhaseX, flowParams.noise2PhaseY);
    }

    public static void setSceneParams(int textureId, int width, int height) {
        sceneTextureId = textureId;
        screenSize.set((float) Math.max(width, 1), (float) Math.max(height, 1));
    }

    public static int getSceneTextureId() {
        return sceneTextureId;
    }

    public static ShaderInstance getShader() {
        return shader;
    }

    public static boolean isLoaded() {
        return shader != null;
    }
}
