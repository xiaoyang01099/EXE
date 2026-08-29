package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.shader;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

import java.io.IOException;

public class TridentPlusGlowShader {
    private static ShaderInstance shader;
    private static AbstractUniform glowParams;

    public static ShaderInstance reloadShaders(ResourceProvider manager) throws IOException {
        return new ShaderInstance(
                manager,
                new ResourceLocation(Exe.MODID, "trident_plus/trident_plus_glow"),
                DefaultVertexFormat.NEW_ENTITY
        );
    }

    public static void onLoad(ShaderInstance shaderInstance) {
        shader = shaderInstance;
        glowParams = shaderInstance.safeGetUniform("GlowParams");
    }

    public static void setGlowParams(float time, float chargeProgress, float glowStrength, float fullyCharged) {
        glowParams.set(time, chargeProgress, glowStrength, fullyCharged);
    }

    public static ShaderInstance getShader() {
        return shader;
    }

    public static boolean isLoaded() {
        return shader != null;
    }
}
