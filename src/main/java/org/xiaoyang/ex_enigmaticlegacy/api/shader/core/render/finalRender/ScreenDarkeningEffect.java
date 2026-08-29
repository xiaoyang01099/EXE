package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.system.RawModel;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.frameBuffer.FBO;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.system.ScreenDarkeningShader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ScreenDarkeningEffect {
    public final ScreenDarkeningShader shader;
    public final List<ScreenDarkeningRequest> requests;
    public FBO darkenedSceneFBO;
    public boolean lastFrameRenderedDarkening;
    public long lastGameTime;

    public ScreenDarkeningEffect(int width, int height) {
        shader = new ScreenDarkeningShader();
        requests = new ArrayList<>();
        darkenedSceneFBO = new FBO(width, height, FBO.NONE, 1);
        lastFrameRenderedDarkening = false;
        lastGameTime = Long.MIN_VALUE;
    }

    public void add(float strength, int lifeTicks, int fadeInTicks, int fadeOutTicks) {
        float safeStrength = Math.max(0.0F, Math.min(0.95F, strength));
        int safeLifeTicks = Math.max(1, lifeTicks);
        int safeFadeInTicks = Math.max(0, fadeInTicks);
        int safeFadeOutTicks = Math.max(0, Math.min(fadeOutTicks, safeLifeTicks));
        if (safeStrength <= 0.0F) return;
        requests.add(new ScreenDarkeningRequest(safeStrength, safeLifeTicks, safeFadeInTicks, safeFadeOutTicks));
    }

    public boolean hasActive() {
        return !requests.isEmpty();
    }

    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        if (darkenedSceneFBO != null) {
            darkenedSceneFBO.resize(width, height, FBO.NONE, FBO.defaultInternalFormatForDepthBufferType(FBO.NONE));
            return;
        }
        darkenedSceneFBO = new FBO(width, height, FBO.NONE, 1);
    }

    public int renderIfNeeded(FBO sourceSceneFBO, RawModel quad) {
        if (sourceSceneFBO == null || quad == null || darkenedSceneFBO == null) return sourceSceneFBO == null ? 0 : sourceSceneFBO.getColourTexture();
        float strength = resolveCurrentStrength();
        if (strength <= 0.001F) {
            lastFrameRenderedDarkening = false;
            tickRequests(resolveElapsedTicks());
            return sourceSceneFBO.getColourTexture();
        }

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, darkenedSceneFBO.getFrameBuffer());
        darkenedSceneFBO.setDrawBuffer(0);
        GL11.glViewport(0, 0, darkenedSceneFBO.getWidth(), darkenedSceneFBO.getHeight());
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL30.glBindVertexArray(quad.getVaoID());
        GL20.glEnableVertexAttribArray(0);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, sourceSceneFBO.getColourTexture());
        shader.start();
        shader.loadUniforms(strength);
        GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, quad.getVertexCount());
        shader.stop();
        GL20.glDisableVertexAttribArray(0);
        GL30.glBindVertexArray(0);

        lastFrameRenderedDarkening = true;
        tickRequests(resolveElapsedTicks());
        return darkenedSceneFBO.getColourTexture();
    }

    public float resolveCurrentStrength() {
        float maxStrength = 0.0F;
        for (ScreenDarkeningRequest request : requests) {
            maxStrength = Math.max(maxStrength, request.currentStrength());
        }
        return maxStrength;
    }

    public void tickRequests(int elapsedTicks) {
        int safeElapsedTicks = Math.max(0, elapsedTicks);
        if (safeElapsedTicks <= 0) return;
        Iterator<ScreenDarkeningRequest> iterator = requests.iterator();
        while (iterator.hasNext()) {
            ScreenDarkeningRequest request = iterator.next();
            request.ageTicks += safeElapsedTicks;
            if (request.ageTicks >= request.lifeTicks) {
                iterator.remove();
            }
        }
    }

    public int resolveElapsedTicks() {
        if (Minecraft.getInstance().level == null) return 1;
        long gameTime = Minecraft.getInstance().level.getGameTime();
        if (lastGameTime == Long.MIN_VALUE) {
            lastGameTime = gameTime;
            return 1;
        }
        int elapsedTicks = (int) Math.max(0L, Math.min(5L, gameTime - lastGameTime));
        lastGameTime = gameTime;
        return elapsedTicks;
    }

    public void cleanUp() {
        shader.cleanUp();
        if (darkenedSceneFBO != null) {
            darkenedSceneFBO.cleanUp();
            darkenedSceneFBO = null;
        }
        requests.clear();
    }

    public static class ScreenDarkeningRequest {
        public final float strength; // 最大暗化强度。
        public final int lifeTicks; // 总持续 tick 数。
        public final int fadeInTicks; // 淡入 tick 数。
        public final int fadeOutTicks; // 淡出 tick 数。
        public int ageTicks; // 已推进 tick 数。

        public ScreenDarkeningRequest(float strength, int lifeTicks, int fadeInTicks, int fadeOutTicks) {
            this.strength = strength;
            this.lifeTicks = lifeTicks;
            this.fadeInTicks = fadeInTicks;
            this.fadeOutTicks = fadeOutTicks;
            this.ageTicks = 1;
        }

        public float currentStrength() {
            float fadeIn = fadeInTicks <= 0 ? 1.0F : Math.min(1.0F, ageTicks / (float) fadeInTicks);
            int fadeOutStart = Math.max(0, lifeTicks - fadeOutTicks);
            float fadeOut = fadeOutTicks <= 0 || ageTicks < fadeOutStart
                    ? 1.0F
                    : Math.max(0.0F, (lifeTicks - ageTicks) / (float) fadeOutTicks);
            return strength * Math.min(fadeIn, fadeOut);
        }
    }
}
