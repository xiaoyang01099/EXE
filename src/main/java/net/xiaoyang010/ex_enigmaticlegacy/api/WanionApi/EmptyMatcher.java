package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public final class EmptyMatcher extends AbstractMatcher<EmptyMatcher>
{
    public EmptyMatcher(@Nonnull final AbstractMatching<?> matching)
    {
        super(matching);
    }

    @Nonnull
    @Override
    public EmptyMatcher copy()
    {
        return new EmptyMatcher(matching);
    }

    @Nonnull
    @Override
    public MatcherEnum getMatcherEnum()
    {
        return MatcherEnum.EMPTY;
    }

    @Nonnull
    @Override
    public AbstractMatcher<?> validate()
    {
        return getStack().isEmpty() ? this : matching.getDefaultMatcher();
    }

    @Override
    public boolean matches(@Nonnull final ItemStack otherItemStack)
    {
        return otherItemStack.isEmpty();
    }

    @Override
    public boolean equals(final Object obj)
    {
        return obj instanceof EmptyMatcher;
    }
}