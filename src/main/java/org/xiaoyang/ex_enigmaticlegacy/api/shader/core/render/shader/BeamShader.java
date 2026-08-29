package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.shader;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.joml.Matrix4f;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

import java.io.IOException;

public class BeamShader {
    private static ShaderInstance shader;
    private static AbstractUniform effectParams;
    private static AbstractUniform renderFlags;
    private static AbstractUniform uView;
    private static AbstractUniform beamCoreColor;
    private static AbstractUniform beamInnerColor;
    private static AbstractUniform beamOuterColor;

    public static ShaderInstance reloadShaders(ResourceProvider manager) throws IOException {
        return new ShaderInstance(
                manager,
                new ResourceLocation(Exe.MODID, "beam"),
                DefaultVertexFormat.POSITION_COLOR_TEX
        );
    }

    public static void onLoad(ShaderInstance shaderInstance) {
        shader = shaderInstance;
        effectParams = shaderInstance.safeGetUniform("EffectParams");
        renderFlags = shaderInstance.safeGetUniform("RenderFlags");
        uView = shaderInstance.safeGetUniform("uView");
        beamCoreColor = shaderInstance.safeGetUniform("BeamCoreColor");
        beamInnerColor = shaderInstance.safeGetUniform("BeamInnerColor");
        beamOuterColor = shaderInstance.safeGetUniform("BeamOuterColor");
    }

    public static void setEffectParams(float time, float bloomStrength, float noiseStrength, float reserved) {
        effectParams.set(time, bloomStrength, noiseStrength, reserved);
    }

    public static void setRenderFlags(int effectType, int bloomEnabled, int reserved0, int reserved1) {
        renderFlags.set(effectType, bloomEnabled, reserved0, reserved1);
    }

    public static void setView(Matrix4f view) {
        uView.set(view);
    }

    public static void setBeamColors(float coreR, float coreG, float coreB, float innerR, float innerG, float innerB, float outerR, float outerG, float outerB) {
        beamCoreColor.set(coreR, coreG, coreB, 1.0f);
        beamInnerColor.set(innerR, innerG, innerB, 1.0f);
        beamOuterColor.set(outerR, outerG, outerB, 1.0f);
    }

    public static ShaderInstance getShader() {
        return shader;
    }

    public static boolean isLoaded() {
        return shader != null;
    }
}
