package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.frameBuffer;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class GlStateSnapshot {

    // --- FBO ---
    public int prevDrawFbo;
    public int prevReadFbo;

    // --- Viewport ---
    public int prevViewportX, prevViewportY, prevViewportW, prevViewportH;

    // --- VAO ---
    public int prevVao;
    public boolean attrib0Enabled;

    // --- Shader ---
    public int prevShaderProgram;

    // --- Depth ---
    public boolean depthTestEnabled;
    public boolean depthMaskEnabled;

    // --- Blend ---
    public boolean blendEnabled;
    public int blendSrcRgb, blendDstRgb;

    // --- Texture (active unit + bindings at touched units) ---
    public int prevActiveTexture;
    public static final int TEXTURE_UNIT_COUNT = 5;
    public int[] prevTextures = new int[TEXTURE_UNIT_COUNT];

    // --- Cull face ---
    public boolean cullEnabled;

    // --- Color write mask ---
    public boolean colorMaskR, colorMaskG, colorMaskB, colorMaskA;

    // --- Scissor test ---
    public boolean scissorEnabled;

    public void save() {
        prevDrawFbo = GL30.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        prevReadFbo = GL30.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        int[] vp = new int[4];
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, vp);
        prevViewportX = vp[0]; prevViewportY = vp[1];
        prevViewportW = vp[2]; prevViewportH = vp[3];
        prevVao = GL30.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
        attrib0Enabled = prevVao != 0
                && GL20.glGetVertexAttribi(0, GL20.GL_VERTEX_ATTRIB_ARRAY_ENABLED) == GL11.GL_TRUE;
        prevShaderProgram = GL20.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        depthTestEnabled =  GL11.glIsEnabled(GL11.GL_DEPTH_TEST) ;
        depthMaskEnabled = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        blendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
        blendSrcRgb = GL11.glGetInteger(GL11.GL_BLEND_SRC);
        blendDstRgb = GL11.glGetInteger(GL11.GL_BLEND_DST);

        // --- Texture ---
        saveTextureState();

        // --- Cull face ---
        cullEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);

        // --- Color write mask ---
        int[] cm = new int[4];
        GL11.glGetIntegerv(GL11.GL_COLOR_WRITEMASK, cm);
        colorMaskR = cm[0] == GL11.GL_TRUE;
        colorMaskG = cm[1] == GL11.GL_TRUE;
        colorMaskB = cm[2] == GL11.GL_TRUE;
        colorMaskA = cm[3] == GL11.GL_TRUE;

        // --- Scissor test ---
        scissorEnabled = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);

    }

    public void restore() {
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, prevDrawFbo);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, prevReadFbo);
        GL11.glViewport(prevViewportX, prevViewportY, prevViewportW, prevViewportH);
        GL30.glBindVertexArray(prevVao);
        if (prevVao != 0) {
            if (attrib0Enabled) GL20.glEnableVertexAttribArray(0);
            else GL20.glDisableVertexAttribArray(0);
        }
        GL20.glUseProgram(prevShaderProgram);
        if (depthTestEnabled) GL11.glEnable(GL11.GL_DEPTH_TEST);
        else GL11.glDisable(GL11.GL_DEPTH_TEST);
        RenderSystem.depthMask(depthMaskEnabled);
        if (blendEnabled) RenderSystem.enableBlend();
        else RenderSystem.disableBlend();
        RenderSystem.blendFunc(blendSrcRgb, blendDstRgb);

        // --- Texture ---
        for (int i = 0; i < TEXTURE_UNIT_COUNT; i++) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + i);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, prevTextures[i]);
        }
        GL13.glActiveTexture(prevActiveTexture);

        // --- Cull face ---
        if (cullEnabled) GL11.glEnable(GL11.GL_CULL_FACE);
        else GL11.glDisable(GL11.GL_CULL_FACE);

        // --- Color write mask ---
        GL11.glColorMask(colorMaskR, colorMaskG, colorMaskB, colorMaskA);

        // --- Scissor test ---
        if (scissorEnabled) GL11.glEnable(GL11.GL_SCISSOR_TEST);
        else GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    public void setToMCVao(){
        if (depthTestEnabled) GL11.glEnable(GL11.GL_DEPTH_TEST);
        else GL11.glDisable(GL11.GL_DEPTH_TEST);
        RenderSystem.depthMask(depthMaskEnabled);
        GL30.glBindVertexArray(prevVao);
    }

    public void saveTextureState() {
        prevActiveTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
        for (int i = 0; i < TEXTURE_UNIT_COUNT; i++) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + i);
            prevTextures[i] = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        }
        GL13.glActiveTexture(prevActiveTexture);
    }
}
