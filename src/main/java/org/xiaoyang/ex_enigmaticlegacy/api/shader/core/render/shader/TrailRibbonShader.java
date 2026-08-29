package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.shader;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

import java.io.IOException;

public class TrailRibbonShader {
    public static ShaderInstance TrailRibbonShader;
    public static AbstractUniform spriteUV0;
    public static AbstractUniform gameTime;


    public static ShaderInstance reloadShaders(ResourceProvider manager) throws IOException {
        ShaderInstance shader = new ShaderInstance(
                manager,
                new ResourceLocation(Exe.MODID, "trail_ribbon_shader"),
                DefaultVertexFormat.POSITION_COLOR_TEX
        );
        return shader;
    }

    public static void onLoad(ShaderInstance shader) {
        TrailRibbonShader = shader;
        spriteUV0 = shader.safeGetUniform("SpriteUV0");
        gameTime = shader.safeGetUniform("GameTime");

    }
}
