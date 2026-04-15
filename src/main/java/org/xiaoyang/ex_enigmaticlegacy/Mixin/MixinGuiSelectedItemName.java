package org.xiaoyang.ex_enigmaticlegacy.Mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.xiaoyang.ex_enigmaticlegacy.Font.ModRarities;

@Mixin(Gui.class)
public class MixinGuiSelectedItemName {
    @Shadow protected int toolHighlightTimer;
    @Shadow protected ItemStack lastToolHighlight;
    @Shadow protected int screenWidth;
    @Shadow protected int screenHeight;

    @Inject(
            method = "renderSelectedItemName(Lnet/minecraft/client/gui/GuiGraphics;I)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void onRenderSelectedItemName(GuiGraphics graphics, int yShift, CallbackInfo ci) {
        if (this.toolHighlightTimer <= 0)     return;
        if (this.lastToolHighlight.isEmpty()) return;
        if (!ModRarities.shouldAnimate(this.lastToolHighlight)) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode == null) return;

        int alpha = (int)((float)this.toolHighlightTimer * 256.0F / 10.0F);
        if (alpha > 255) alpha = 255;
        if (alpha <= 0)  return;

        MutableComponent comp = Component.literal("")
                .append(this.lastToolHighlight.getHoverName())
                .withStyle(this.lastToolHighlight.getRarity().getStyleModifier());
        if (this.lastToolHighlight.hasCustomHoverName()) {
            comp.withStyle(ChatFormatting.ITALIC);
        }
        Component highlightTip = this.lastToolHighlight.getHighlightTip(comp);

        Font font = mc.font;

        int textWidth = font.width(highlightTip);
        int x = (this.screenWidth - textWidth) / 2;
        int y = this.screenHeight - Math.max(yShift, 59);
        if (!mc.gameMode.canHurtPlayer()) {
            y += 14;
        }

        int bgColor = mc.options.getBackgroundColor(0);
        if (bgColor != 0) {
            graphics.fill(x - 2, y - 2, x + textWidth + 2, y + 9 + 2, bgColor);
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        ModRarities.drawWaveName(graphics, font, this.lastToolHighlight, x, y, alpha);
        RenderSystem.disableBlend();

        ci.cancel();
    }
}