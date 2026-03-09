package net.xiaoyang010.ex_enigmaticlegacy.Client;

import com.integral.enigmaticlegacy.items.CursedRing;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.api.test.VampireWings;
import top.theillusivec4.curios.api.CuriosApi;

public class BloodBarHud implements IIngameOverlay {

    public static final BloodBarHud INSTANCE = new BloodBarHud();
    private static final ResourceLocation BLOOD_BAR_TEX =
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "textures/gui/blood_bar.png");

    @Override
    public void render(ForgeIngameGui gui, PoseStack poseStack, float partialTick, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        ItemStack wings = CuriosApi.getCuriosHelper()
                .findEquippedCurio(s -> s.getItem() instanceof CursedRing, mc.player)
                .map(triple -> triple.getRight())
                .orElse(ItemStack.EMPTY);

        if (wings.isEmpty()) return;

        int blood = VampireWings.getBlood(wings);
        int maxBlood = VampireWings.MAX_BLOOD;

        int barWidth = 135;
        int barHeight = 16;
        int x = width / 2 + 91 + 5;
        int y = height - 39;

        RenderSystem.setShaderTexture(0, BLOOD_BAR_TEX);
        RenderSystem.enableBlend();

        GuiComponent.blit(poseStack, x, y, 0, 0, barWidth, barHeight, 256, 256);

        int fillWidth = (int) ((blood / (float) maxBlood) * barWidth);
        if (fillWidth > 0) {
            GuiComponent.blit(poseStack, x, y, 0, barHeight, fillWidth, barHeight, 256, 256);
        }

        mc.font.drawShadow(poseStack, "§c" + blood + "/" + maxBlood, x, y - 10, 0xFFFFFF);

        RenderSystem.disableBlend();
    }
}