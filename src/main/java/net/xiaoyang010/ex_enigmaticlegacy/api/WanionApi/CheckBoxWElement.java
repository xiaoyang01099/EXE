package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF.INBTMessage;


import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class CheckBoxWElement extends WField<CheckBox> {
    public CheckBoxWElement(@Nonnull final CheckBox field, @Nonnull final WGuiContainer<?> wGuiContainer, final int x, final int y)
    {
        super(field, wGuiContainer, x, y, 18, 18);
        setTooltipSupplier((interaction, stackSupplier) -> Lists.newArrayList(Component.nullToEmpty(field.getHoveringText(interaction))));
        setMouseInteraction(wMouseInteraction -> {
            INBTMessage.sendNBT(getWindowID(), field.toggle().writeNBT());
            playPressSound();
        });
    }

    @Override
    public void draw(@Nonnull final WInteraction wInteraction) {
        final boolean isHovering = wInteraction.isHovering(this);
        bindTexture(GUI_TEXTURES);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int textureX = !isHovering ? 36 : 54;
        int textureY = !field.isChecked() ? 72 : 90;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();

        int x = getUsableX();
        int y = getUsableY();
        float u0 = (float) textureX / 128.0F;
        float v0 = (float) textureY / 128.0F;
        float u1 = (float) (textureX + width) / 128.0F;
        float v1 = (float) (textureY + height) / 128.0F;

        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(x, y + height, 0.0D).uv(u0, v1).endVertex();
        bufferBuilder.vertex(x + width, y + height, 0.0D).uv(u1, v1).endVertex();
        bufferBuilder.vertex(x + width, y, 0.0D).uv(u1, v0).endVertex();
        bufferBuilder.vertex(x, y, 0.0D).uv(u0, v0).endVertex();
        tesselator.end();

        RenderSystem.disableBlend();
    }
}