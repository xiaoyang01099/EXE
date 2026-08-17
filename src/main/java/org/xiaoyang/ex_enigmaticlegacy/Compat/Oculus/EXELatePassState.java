package org.xiaoyang.ex_enigmaticlegacy.Compat.Oculus;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;

public final class EXELatePassState {

    public static void prepare() {
        Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
        RenderSystem.disableScissor();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.colorMask(true, true, true, true);RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.defaultBlendFunc();
    }

    public static void finish() {
        Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
        RenderSystem.disableScissor();
        RenderSystem.depthMask(true);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.defaultBlendFunc();
    }

    private EXELatePassState() {}
}