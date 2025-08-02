package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF;

import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IState<S extends IState<?>>
{
    @Nonnull
    S getNextState();

    @Nonnull
    S getPreviousState();

    @Nullable
    ResourceLocation getTextureResourceLocation();

    @Nullable
    default Pair<Integer, Integer> getTexturePos(boolean hovered)
    {
        return null;
    }
}