package org.xiaoyang.ex_enigmaticlegacy.api.shader.bladeflash;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.BlendMode;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.ShaderInstance;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.xiaoyang.ex_enigmaticlegacy.Mixin.BlendModeAccessor;

public final class BladeRenderState {
    private final int read = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
    private final int draw = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
    private final int activeTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
    // VertexBuffer populates Sampler0..11 from RenderSystem, independently of GL bindings.
    private final int[] textures = new int[12];
    private final int[] shaderTextures = new int[12];
    private final int[] viewport = new int[4];
    private final boolean depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
    private final boolean depthWrite = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
    private final boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
    private final BlendMode blendMode = BlendModeAccessor.ex_enigmaticlegacy$getLastApplied();
    private final boolean cull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
    private final boolean scissor = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
    private final int depthFunc = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);
    private final int srcRgb = GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB);
    private final int dstRgb = GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
    private final int srcAlpha = GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA);
    private final int dstAlpha = GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
    private final ShaderInstance shader = RenderSystem.getShader();

    public BladeRenderState() {
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
        for (int i = 0; i < textures.length; i++) {
            shaderTextures[i] = RenderSystem.getShaderTexture(i);
            RenderSystem.activeTexture(GL13.GL_TEXTURE0 + i);
            textures[i] = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        }
        RenderSystem.activeTexture(activeTexture);
    }

    public void restore() {
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, read);
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, draw);
        RenderSystem.viewport(viewport[0], viewport[1], viewport[2], viewport[3]);
        if (depth) RenderSystem.enableDepthTest(); else RenderSystem.disableDepthTest();
        if (blend) RenderSystem.enableBlend(); else RenderSystem.disableBlend();
        if (cull) RenderSystem.enableCull(); else RenderSystem.disableCull();
        if (scissor) GlStateManager._enableScissorTest(); else RenderSystem.disableScissor();
        RenderSystem.depthMask(depthWrite);
        RenderSystem.depthFunc(depthFunc);
        GlStateManager._blendFuncSeparate(srcRgb, dstRgb, srcAlpha, dstAlpha);
        // ShaderInstance.apply also consults this cache. Leaving an alpha shader
        // here makes vanilla's next opaque shader disable GUI/vignette blending.
        BlendModeAccessor.ex_enigmaticlegacy$setLastApplied(blendMode);
        for (int i = 0; i < textures.length; i++) {
            RenderSystem.setShaderTexture(i, shaderTextures[i]);
            RenderSystem.activeTexture(GL13.GL_TEXTURE0 + i);
            RenderSystem.bindTexture(textures[i]);
        }
        RenderSystem.activeTexture(activeTexture);
        RenderSystem.setShader(() -> shader);
    }
}
