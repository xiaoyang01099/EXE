package net.xiaoyang010.ex_enigmaticlegacy.Compat.JEI.AvaritiaJei;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.xiaoyang010.ex_enigmaticlegacy.Client.gui.GuiExtremeAutoCrafter;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class AutoExtremeCrafterGhostHandler implements IGhostIngredientHandler<GuiExtremeAutoCrafter> {

    @Override
    @Nonnull
    public <I> List<Target<I>> getTargets(@Nonnull GuiExtremeAutoCrafter gui, @Nonnull I ingredient, boolean doStart) {
        List<Target<I>> targets = new ArrayList<>();

        final int endsIn = gui.getMenu().slots.size() - 37;
        final int startsIn = endsIn / 2;

        if (ingredient instanceof ItemStack) {
            for (int i = startsIn; i < endsIn; i++) {
                final Slot slot = gui.getMenu().getSlot(i);
                targets.add(new ExtremeTarget<>(
                        slot.index,
                        new Rect2i(
                                gui.getGuiLeft() + slot.x - 1,
                                gui.getGuiTop() + slot.y - 1,
                                18, 18
                        ),
                        gui
                ));
            }
        }

        return targets;
    }

    @Override
    public void onComplete() {}
}