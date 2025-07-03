package net.xiaoyang010.ex_enigmaticlegacy.api;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;

public interface IRenderHud {
    void renderHud(Minecraft minecraft, PoseStack poseStack, float partialTicks, int width, int height);
}
