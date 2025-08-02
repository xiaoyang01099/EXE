package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public final class DisabledMatcher extends AbstractMatcher<DisabledMatcher>
{
    public DisabledMatcher(@Nonnull final AbstractMatching<?> matching)
    {
        super(matching);
    }

    @Nonnull
    @Override
    public DisabledMatcher copy()
    {
        return new DisabledMatcher(matching);
    }

    @Nonnull
    @Override
    public MatcherEnum getMatcherEnum()
    {
        return MatcherEnum.DISABLED;
    }

    @Nonnull
    @Override
    public AbstractMatcher<?> validate()
    {
        return this;
    }

    @Override
    public boolean matches(@Nonnull final ItemStack otherItemStack)
    {
        return false;
    }

    @Override
    public boolean equals(final Object obj)
    {
        return obj instanceof DisabledMatcher;
    }
}