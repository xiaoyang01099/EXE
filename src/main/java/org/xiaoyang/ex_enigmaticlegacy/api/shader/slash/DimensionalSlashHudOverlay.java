package org.xiaoyang.ex_enigmaticlegacy.api.shader.slash;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

@Mod.EventBusSubscriber(modid = Exe.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class DimensionalSlashHudOverlay {
    private DimensionalSlashHudOverlay() {}

    @SubscribeEvent
    public static void register(RegisterGuiOverlaysEvent event) {
        // 屏幕破裂效果
        event.registerBelowAll("dimensional_slash_break", (gui, graphics, partialTick, width, height) -> {
            boolean customPresentation = ClientEffects.needsScreenOutputGuard();
            if (!customPresentation) {
                return;
            }
            var main = Minecraft.getInstance().getMainRenderTarget();
            ScreenOutputGuard.capture(main, true);
            try {
                ClientEffects.renderScreenOverlay(partialTick);
                PressureOverlay.render(graphics, partialTick, width, height);
                ClientEffects.renderTopChroma(partialTick);
            } finally {
                ScreenOutputGuard.validateCustomOutputAndCommit(main);
                restoreVanillaGuiState();
            }
        });
        event.registerBelow(VanillaGuiOverlay.HOTBAR.id(), "dimensional_slash_gui_guard", (gui, graphics, partialTick, width, height) -> {
            if (!ClientEffects.needsScreenOutputGuard()) {
                return;
            }
            ScreenOutputGuard.finishBeforeHotbar(Minecraft.getInstance().getMainRenderTarget());
            restoreVanillaGuiState();
        });
    }

    private static void restoreVanillaGuiState() {
        var main = Minecraft.getInstance().getMainRenderTarget();
        main.bindWrite(false);

        GL11.glViewport(0, 0, main.width, main.height);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glColorMask(true, true, true, true);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        RenderSystem.viewport(0, 0, main.width, main.height);
        RenderSystem.disableScissor();
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.activeTexture(GL13.GL_TEXTURE0);
        GlStateManager._bindTexture(0);
    }
}
