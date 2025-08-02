package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF;

import javax.annotation.Nonnull;

public interface ICopyable<C extends ICopyable<C>>
{
    @Nonnull
    C copy();
}