package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import net.minecraft.world.item.ItemStack;
import net.minecraft.client.gui.screens.Screen;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class RecipeResultItemBox extends ItemBoxElement
{
    public RecipeResultItemBox(@Nonnull final Supplier<ItemStack> stackSupplier, @Nonnull final WGuiContainer<?> wGuiContainer, final int x, final int y)
    {
        super(stackSupplier, wGuiContainer, x, y);
        setInteractionCheck(interaction -> interaction.isHovering(this) && Screen.hasShiftDown());
    }

    @Override
    public void interact(@Nonnull final WMouseInteraction wMouseInteraction)
    {
        ClearShapeMessage.sendToServer(getWContainer());
        playPressSound();
    }
}