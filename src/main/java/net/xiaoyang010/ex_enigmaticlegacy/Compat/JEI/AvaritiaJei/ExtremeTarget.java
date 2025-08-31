package net.xiaoyang010.ex_enigmaticlegacy.Compat.JEI.AvaritiaJei;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.ExtremeAutoCrafterGhostTransferMessage;

import javax.annotation.Nonnull;

public class ExtremeTarget<I> implements IGhostIngredientHandler.Target<I> {
    private final int slot;
    private final Rect2i rectangle;
    private final AbstractContainerScreen<?> guiContainer;

    public ExtremeTarget(int slot, @Nonnull Rect2i rectangle, @Nonnull AbstractContainerScreen<?> guiContainer) {
        this.slot = slot;
        this.rectangle = rectangle;
        this.guiContainer = guiContainer;
    }

    @Override
    @Nonnull
    public Rect2i getArea() {
        return rectangle;
    }

    @Override
    public void accept(@Nonnull I ingredient) {
        if (ingredient instanceof ItemStack itemStack) {
            NetworkHandler.CHANNEL.sendToServer(
                    new ExtremeAutoCrafterGhostTransferMessage(
                            guiContainer.getMenu().containerId,
                            slot,
                            itemStack
                    )
            );
        }
    }
}