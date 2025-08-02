package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.WInteraction;

import javax.annotation.Nonnull;

public interface IField<F extends IField<F>> extends ISmartNBT, ICopyable<F>, INBTMessage
{
    @Nonnull
    String getFieldName();

    default boolean canInteractWith(@Nonnull Player player)
    {
        return true;
    }

    default void startInteraction(@Nonnull Player player) {}

    default void endInteraction(@Nonnull Player player) {}

    @OnlyIn(Dist.CLIENT)
    default String getHoveringText(@Nonnull final WInteraction wInteraction)
    {
        return null;
    }
}