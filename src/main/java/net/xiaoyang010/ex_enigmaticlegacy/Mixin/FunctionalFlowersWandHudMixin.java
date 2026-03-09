package net.xiaoyang010.ex_enigmaticlegacy.Mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.resources.language.I18n;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.botania.api.BotaniaAPIClient;
import vazkii.botania.api.subtile.TileEntityFunctionalFlower;

@Mixin(TileEntityFunctionalFlower.FunctionalWandHud.class)
public abstract class FunctionalFlowersWandHudMixin {

    @Shadow(remap = false) @Final
    protected TileEntityFunctionalFlower flower;

    @Inject(method = "renderHUD", at = @At("HEAD"), cancellable = true, remap = false)
    public void mana_display$renderManaBar(PoseStack ms, Minecraft mc, CallbackInfo ci) {
        String name = I18n.get(flower.getBlockState().getBlock().getDescriptionId());
        int color = flower.getColor();
        int centerX = mc.getWindow().getGuiScaledWidth() / 2;
        int centerY = mc.getWindow().getGuiScaledHeight() / 2;

        int textWidth = mc.font.width(name);
        int boxWidth = Math.max(102, textWidth) + 4;
        int left = boxWidth / 2;

        mana_display$renderHUDBox(ms,
                centerX - left,
                centerY - 4,
                centerX + left + 20,
                centerY + 30
        );

        BotaniaAPIClient.instance().drawComplexManaHUD(ms, color, flower.getMana(), flower.getMaxMana(), name, flower.getHudIcon(), flower.isValidBinding());

        ci.cancel();
    }

    @Unique
    private void mana_display$renderHUDBox(PoseStack ms, int x1, int y1, int x2, int y2) {
        GuiComponent.fill(ms, x1, y1, x2, y2, 0x55000000);
        GuiComponent.fill(ms, x1, y1, x2, y1 + 1, 0x55FFFFFF);
        GuiComponent.fill(ms, x1, y1, x1 + 1, y2, 0x55FFFFFF);
        GuiComponent.fill(ms, x2 - 1, y1, x2, y2, 0x55000000);
        GuiComponent.fill(ms, x1, y2 - 1, x2, y2, 0x55000000);
    }
}