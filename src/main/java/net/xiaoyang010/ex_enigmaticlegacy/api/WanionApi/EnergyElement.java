package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.Util.EComponent;
import javax.annotation.Nonnull;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class EnergyElement extends WElement<EnergyElement> {
    protected final EnergyControl energyControl;
    public static final ResourceLocation GUI_TEXTURES = new ResourceLocation("ex_enigmaticlegacy:textures/gui/gui_textures.png");

    public EnergyElement(@Nonnull final EnergyControl energyControl, @Nonnull final WGuiContainer<?> wGuiContainer, final int x, final int y) {
        super(wGuiContainer, x, y, 18, 54);
        this.energyControl = energyControl;
        setInteractionCheck(interaction -> false);
        setTooltipSupplier((interaction, stackSupplier) -> {
            List<Component> tooltip = Lists.newArrayList();
            tooltip.add(EComponent.literal(energyControl.getEnergyStored() + " / " + energyControl.getMaxEnergyStored() + " FE"));
            tooltip.add(EComponent.empty());
            tooltip.add(EComponent.literal(I18n.get("ex_enigmaticlegacy.consumes", energyControl.getEnergyUsage())).withStyle(ChatFormatting.GOLD));
            tooltip.add(EComponent.literal(I18n.get("ex_enigmaticlegacy.per.operation")).withStyle(ChatFormatting.GOLD));
            return tooltip;
        });
    }

    @Override
    public void draw(@Nonnull final WInteraction wInteraction) {
        final PoseStack poseStack = wInteraction.getPoseStack();

        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, GUI_TEXTURES);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        final int usableX = getUsableX();
        final int usableY = getUsableY();

        GuiComponent.blit(poseStack, usableX, usableY, 0, 0, width, height, 128, 128);

        final int filledSize = scalePowerPercentage();
        if (filledSize > 0) {
            final int clampedSize = Math.min(filledSize, height);
            final int fillStartY = usableY + height - clampedSize;

            GuiComponent.blit(poseStack, usableX, fillStartY, 18, height - clampedSize, 18, clampedSize, 128, 128);
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private int scalePowerPercentage() {
        final int energyStored = energyControl.getEnergyStored();
        final int maxEnergyStored = energyControl.getMaxEnergyStored();

        if (energyStored <= 0 || maxEnergyStored <= 0) {
            return 0;
        }

        return Math.min(height, (int) Math.ceil(height * energyStored / (double) maxEnergyStored));
    }
}