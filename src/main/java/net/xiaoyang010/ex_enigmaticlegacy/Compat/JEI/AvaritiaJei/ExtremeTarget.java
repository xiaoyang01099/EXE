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
    private final AbstractContainerScreen<?> containerScreen;

    public ExtremeTarget(final int slot, @Nonnull final Rect2i rectangle, @Nonnull final AbstractContainerScreen<?> containerScreen) {
        this.slot = slot;
        this.rectangle = rectangle;
        this.containerScreen = containerScreen;
    }

    @Override
    @Nonnull
    public Rect2i getArea() {
        return rectangle;
    }

    @Override
    public void accept(@Nonnull final I ingredient) {
        if (ingredient instanceof ItemStack) {
            NetworkHandler.CHANNEL.sendToServer(
                    new ExtremeAutoCrafterGhostTransferMessage(
                            containerScreen.getMenu().containerId,
                            slot,
                            (ItemStack) ingredient
                    )
            );
        }
    }
}