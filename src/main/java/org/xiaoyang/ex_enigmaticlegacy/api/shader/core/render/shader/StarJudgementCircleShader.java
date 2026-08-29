package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.shader;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.joml.Matrix4f;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

import java.io.IOException;

public class StarJudgementCircleShader {
    private static ShaderInstance shader;
    private static AbstractUniform effectParams;
    private static AbstractUniform strikeParams;
    private static AbstractUniform uView;

    public static ShaderInstance reloadShaders(ResourceProvider manager) throws IOException {
        return new ShaderInstance(
                manager,
                new ResourceLocation(Exe.MODID, "star_judgement_circle"),
                DefaultVertexFormat.POSITION_COLOR_TEX
        );
    }

    public static void onLoad(ShaderInstance shaderInstance) {
        shader = shaderInstance;
        effectParams = shaderInstance.safeGetUniform("EffectParams");
        strikeParams = shaderInstance.safeGetUniform("StrikeParams");
        uView = shaderInstance.safeGetUniform("uView");
    }

    public static void setEffectParams(float time, float ageProgress, float centerProgress, float outerProgress) {
        effectParams.set(time, ageProgress, centerProgress, outerProgress);
    }

    public static void setStrikeParams(float strikeProgress, float radius, float bloomStrength, float reserved) {
        strikeParams.set(strikeProgress, radius, bloomStrength, reserved);
    }

    public static void setView(Matrix4f view) {
        uView.set(view);
    }

    public static ShaderInstance getShader() {
        return shader;
    }

    public static boolean isLoaded() {
        return shader != null;
    }
}
