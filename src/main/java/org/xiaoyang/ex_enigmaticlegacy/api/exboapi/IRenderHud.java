package org.xiaoyang.ex_enigmaticlegacy.api.exboapi;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IRenderHud {

    @OnlyIn(Dist.CLIENT)
    void renderHud(Minecraft mc, GuiGraphics poseStack, int screenWidth, int screenHeight);
}