package net.xiaoyang010.ex_enigmaticlegacy.Util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Avaritia.block.EXEShaders;
import org.jetbrains.annotations.NotNull;

public enum CosmicParticleType implements ParticleRenderType {

    INSTANCE;

    @Override
    public void begin(BufferBuilder builder, @NotNull TextureManager texMgr) {

        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);

        RenderSystem.setShader(() -> EXEShaders.cosmicShader);
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);

        Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
        EXEShaders.uploadCommonUniformsForParticles();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
    }

    @Override
    public void end(Tesselator tessellate) {
        tessellate.end();
        Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
    }

    @Override
    public String toString() {
        return "ex_enigmaticlegacy:cosmic_particle";
    }
}