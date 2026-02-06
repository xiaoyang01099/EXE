package net.xiaoyang010.ex_enigmaticlegacy.Mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.botania.client.gui.HUDHandler;

import static vazkii.botania.client.gui.HUDHandler.renderManaBar;

@Mixin(HUDHandler.class)
public class HUDHandlerMixin {


    @Inject(method = "renderManaBar", at = @At("TAIL"), remap = false)
    private static void mana_display$renderManaBar(PoseStack ms, int x, int y, int color, float alpha, int mana, int maxMana, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        String text = mana + " / " + maxMana;
        int a = x + 51 - mc.font.width(text) / 2;
        boolean isJEI = (x == 20 && y == 50);
        boolean isNidavellir = (x == 6 && y == 98);
        if (isNidavellir) {
            return;
        }

        int offset = isJEI ? 30 : 0;
        int b = y - mc.font.lineHeight - 11 + offset;
        mc.font.draw(ms, text, a, b, color);
    }
//    @Inject(method = "renderManaBar", at = @At("TAIL"), remap = false)
//    private static void mana_display$renderManaBar(PoseStack ms, int x, int y, int color, float alpha, int mana, int maxMana, CallbackInfo ci) {
//        Minecraft mc = Minecraft.getInstance();
//        String text = mana + " / " + maxMana;
//        int a = x + 51 - mc.font.width(text) / 2;
//        boolean isJEI = (x == 20 && y == 50);
//        int offset = isJEI ? 30 : 0;
//        int b = y - mc.font.lineHeight - 11 + offset;
//        mc.font.draw(ms, text, a, b, color);
//    }

    @Inject(method = "drawSimpleManaHUD", at = @At("TAIL"), remap = false, cancellable = true)
    private static void drawSimpleManaHUD(PoseStack ms, int color, int mana, int maxMana, String name, CallbackInfo ci) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(770, 771);
        Minecraft mc = Minecraft.getInstance();
        int x = mc.getWindow().getGuiScaledWidth() / 2 - mc.font.width(name) / 2;
        int y = mc.getWindow().getGuiScaledHeight() / 2 + 10;
        mc.font.drawShadow(ms, name, (float)x, (float)y, color);
        x = mc.getWindow().getGuiScaledWidth() / 2 - 51;
        y += 10;
        renderManaBar(ms, x, y, color, 1.0F, mana, maxMana);
        RenderSystem.disableBlend();
        ci.cancel();
    }
}
