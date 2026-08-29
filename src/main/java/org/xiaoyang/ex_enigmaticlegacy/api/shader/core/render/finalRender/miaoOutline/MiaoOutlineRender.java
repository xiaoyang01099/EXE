package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.miaoOutline;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.system.RawModel;
import org.xiaoyang.ex_enigmaticlegacy.Util.MathUtil;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.frameBuffer.FBO;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.texture.EXETextureAtlas;

public class MiaoOutlineRender {
    private final MiaoOutlineShader shader;

    public MiaoOutlineRender() {
        shader = new MiaoOutlineShader();
        shader.start();
        shader.loadTextureUnits();
        shader.stop();
    }

    public void render(FBO mainFBO, RawModel quad, MiaoOutlineStyle style, float partialTick) {
        if (mainFBO == null || quad == null || style == null) return;

        int width = mainFBO.getWidth();
        int height = mainFBO.getHeight();
        float time = MathUtil.getClientTime(partialTick);

        mainFBO.setDrawBuffers(0, 1);
        BufferUploader.reset();
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glEnable(GL11.GL_BLEND);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        GL30.glBindVertexArray(quad.getVaoID());
        GL20.glEnableVertexAttribArray(0);

        shader.start();
        shader.loadUniforms(width, height, time, style);
        loadAtlasSpriteUvs();
        bindTextures(mainFBO);
        GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, quad.getVertexCount());
        shader.stop();

        GL20.glDisableVertexAttribArray(0);
        GL30.glBindVertexArray(0);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDepthMask(false);
    }

    public void bindTextures(FBO mainFBO) {
        int atlasTextureId = EXETextureAtlas.EXE_TOOL_ATLAS == null ? 0 : EXETextureAtlas.EXE_TOOL_ATLAS.getId();
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, mainFBO.getColourTexture(2));
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, atlasTextureId);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
    }

    public void loadAtlasSpriteUvs() {
        TextureAtlasSprite noise = EXETextureAtlas.getTextureLocation(EXETextureAtlas.noise_002_128x);
        TextureAtlasSprite gradient = EXETextureAtlas.getTextureLocation(EXETextureAtlas.yellow_gradient);
        shader.loadNoiseSpriteUv(noise.getU0(), noise.getV0(), noise.getU1(), noise.getV1());
        shader.loadGradientSpriteUv(gradient.getU0(), gradient.getV0(), gradient.getU1(), gradient.getV1());
    }

    public void cleanUp() {
        shader.cleanUp();
    }
}
