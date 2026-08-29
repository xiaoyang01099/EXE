package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.shader;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.joml.Matrix4f;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

import java.io.IOException;

public class DimensionSlashStrikeShader {
    public static ShaderInstance shader;
    public static AbstractUniform effectParams;
    public static AbstractUniform uView;

    public static ShaderInstance reloadShaders(ResourceProvider manager) throws IOException {
        return new ShaderInstance(
                manager,
                new ResourceLocation(Exe.MODID, "dimension_slash_strike"),
                DefaultVertexFormat.POSITION_COLOR_TEX
        );
    }

    public static void onLoad(ShaderInstance shaderInstance) {
        shader = shaderInstance;
        effectParams = shaderInstance.safeGetUniform("EffectParams");
        uView = shaderInstance.safeGetUniform("uView");
    }

    public static void setEffectParams(float time, float bloomStrength) {
        effectParams.set(time, bloomStrength, 0.0F, 0.0F);
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
