package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import com.mojang.blaze3d.vertex.PoseStack;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class WInteraction {
    private final WGuiContainer<?> wGuiContainer;
    private final Player player;
    private final int mouseX, mouseY;
    private final PoseStack poseStack;
    private boolean proceed = true;

    public WInteraction(@Nonnull final WGuiContainer<?> wGuiContainer) {
        this(wGuiContainer, 0, 0);
    }

    public WInteraction(@Nonnull final WGuiContainer<?> wGuiContainer, final int mouseX, int mouseY) {
        this(wGuiContainer, mouseX, mouseY, new PoseStack());
    }

    public WInteraction(@Nonnull final WGuiContainer<?> wGuiContainer, final int mouseX, int mouseY, @Nonnull final PoseStack poseStack) {
        this.wGuiContainer = wGuiContainer;
        this.player = wGuiContainer.getEntityPlayer();
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.poseStack = poseStack;
    }

    @Nonnull
    public final WGuiContainer<?> getWGuiContainer() {
        return wGuiContainer;
    }

    @Nonnull
    public final WContainer<?> getWContainer() {
        return wGuiContainer.getContainer();
    }

    @Nonnull
    public Player getEntityPlayer() {
        return player;
    }

    @Nonnull
    public PoseStack getPoseStack() {
        return poseStack;
    }

    public int getMouseX() {
        return mouseX;
    }

    public int getMouseY() {
        return mouseY;
    }

    public boolean isHovering(@Nonnull final WElement<?> wElement) {
        return mouseX >= wElement.getUsableX() &&
                mouseY >= wElement.getUsableY() &&
                mouseX < wElement.getUsableX() + wElement.getWidth() &&
                mouseY < wElement.getUsableY() + wElement.getHeight();
    }

    public final void proceed() {
        proceed = true;
    }

    public final void notProceed() {
        proceed = false;
    }

    public final boolean shouldProceed(){
        return proceed;
    }
}