package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF;

import javax.annotation.Nonnull;

public interface IStateNameable
{
    @Nonnull
    String getStateName();

    default String getStateDescription()
    {
        return getStateName() + ".desc";
    }
}