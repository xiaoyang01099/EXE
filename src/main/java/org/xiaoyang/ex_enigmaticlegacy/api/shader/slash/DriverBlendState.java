package org.xiaoyang.ex_enigmaticlegacy.api.shader.slash;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;


public final class DriverBlendState {
    private DriverBlendState() {
    }

    static void disable() {
        RenderSystem.assertOnRenderThread();
        GL11.glDisable(GL11.GL_BLEND);
        RenderSystem.disableBlend();
    }

    static void enableDefault() {
        RenderSystem.assertOnRenderThread();
        forceAddBlendEquationAndDisableLogicOp();
        GL11.glEnable(GL11.GL_BLEND);
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }

    static void enableAdditive() {
        RenderSystem.assertOnRenderThread();
        forceAddBlendEquationAndDisableLogicOp();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE
        );
    }

    static void prepareVanillaVignette() {
        RenderSystem.assertOnRenderThread();
        forceAddBlendEquationAndDisableLogicOp();
        GL11.glDisable(GL11.GL_BLEND);
        RenderSystem.disableBlend();

        GL14.glBlendFuncSeparate(GL11.GL_ONE, GL11.GL_ZERO, GL11.GL_ONE, GL11.GL_ZERO);
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );
    }

//    public static void forceVanillaVignetteDrawState() {
//        if (!shouldFenceOculusVanillaPipeline()) {
//            return;
//        }
//
//        RenderSystem.assertOnRenderThread();
//        var main = Minecraft.getInstance().getMainRenderTarget();
//        if (main == null || main.width <= 0 || main.height <= 0) {
//            return;
//        }
//
//        // Gui.renderVignette uses a multiplicative blend.  Oculus' no-pack
//        // GUI path can retain the disabled blend bit from our preceding
//        // fullscreen pass, turning the black vignette texture into an opaque
//        // full-screen overwrite.  Bind through RenderTarget so Minecraft's
//        // framebuffer bookkeeping stays in sync; only the attachment and
//        // blend state need an explicit fence here.
//        main.bindWrite(false);
//        GL11.glDrawBuffer(main.frameBufferId == 0 ? GL11.GL_BACK : GL30.GL_COLOR_ATTACHMENT0);
//        RenderSystem.viewport(0, 0, main.width, main.height);
//        RenderSystem.disableScissor();
//        RenderSystem.colorMask(true, true, true, true);
//        RenderSystem.depthMask(false);
//        RenderSystem.disableDepthTest();
//        forceAddBlendEquationAndDisableLogicOp();
//        GL11.glEnable(GL11.GL_BLEND);
//        GL14.glBlendFuncSeparate(GL11.GL_ZERO, GL11.GL_ONE_MINUS_SRC_COLOR, GL11.GL_ONE, GL11.GL_ZERO);
//        RenderSystem.enableBlend();
//        RenderSystem.blendFuncSeparate(
//                GlStateManager.SourceFactor.ZERO,
//                GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
//                GlStateManager.SourceFactor.ONE,
//                GlStateManager.DestFactor.ZERO
//        );
//    }

//    public static void restoreAfterVanillaVignette() {
//        if (!shouldFenceOculusVanillaPipeline()) {
//            return;
//        }
//
//        RenderSystem.assertOnRenderThread();
//        var main = Minecraft.getInstance().getMainRenderTarget();
//        if (main == null || main.width <= 0 || main.height <= 0) {
//            return;
//        }
//
//        main.bindWrite(false);
//        GL11.glDrawBuffer(main.frameBufferId == 0 ? GL11.GL_BACK : GL30.GL_COLOR_ATTACHMENT0);
//        RenderSystem.viewport(0, 0, main.width, main.height);
//        RenderSystem.colorMask(true, true, true, true);
//        RenderSystem.depthMask(true);
//        RenderSystem.enableDepthTest();
//        enableDefault();
//    }

//    public static boolean shouldBypassNoPackVignette() {
//        return ColdNoPackVignettePolicy.shouldBypass(
//                shouldFenceOculusVanillaPipeline(),
//                ClientEffects.needsScreenOutputGuard()
//        );
//    }

    private static void forceAddBlendEquationAndDisableLogicOp() {
        GL20.glBlendEquationSeparate(GL14.GL_FUNC_ADD, GL14.GL_FUNC_ADD);
        GL11.glDisable(GL11.GL_COLOR_LOGIC_OP);
        RenderSystem.blendEquation(GL14.GL_FUNC_ADD);
        RenderSystem.disableColorLogicOp();
    }

//    private static boolean shouldFenceOculusVanillaPipeline() {
//        // Do not touch vanilla's GUI outside the exact overlap that needs the
//        // fence.  In particular, shader-pack transitions are handled by the
//        // world-render quiet window, not by mutating GUI state here.
//        return ItemShaderModCompat.isOculusLoaded()
//                && !ItemShaderModCompat.isOculusShaderPackActive()
//                && ClientEffects.needsScreenOutputGuard();
//    }
}
