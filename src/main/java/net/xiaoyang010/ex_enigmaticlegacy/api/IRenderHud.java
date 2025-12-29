package net.xiaoyang010.ex_enigmaticlegacy.api;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IRenderHud {

    @OnlyIn(Dist.CLIENT)
    void renderHud(Minecraft mc, PoseStack poseStack, int screenWidth, int screenHeight);
}