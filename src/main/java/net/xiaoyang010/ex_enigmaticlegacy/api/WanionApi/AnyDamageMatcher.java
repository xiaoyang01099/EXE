package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public final class AnyDamageMatcher extends AbstractMatcher<AnyDamageMatcher>
{
    public AnyDamageMatcher(@Nonnull final AbstractMatching<?> matching)
    {
        super(matching);
    }

    @Nonnull
    @Override
    public MatcherEnum getMatcherEnum()
    {
        return MatcherEnum.ANY_DAMAGE;
    }

    @Nonnull
    @Override
    public AbstractMatcher<?> validate()
    {
        return Util.isDamageable(getStack()) ? this : matching.getDefaultMatcher();
    }

    @Override
    public boolean matches(@Nonnull final ItemStack otherItemStack)
    {
        final ItemStack itemStack = getStack();
        return !itemStack.isEmpty() && itemStack.is(otherItemStack.getItem());
    }

    @Override
    public boolean equals(final Object obj)
    {
        return obj instanceof AnyDamageMatcher;
    }

    @Nonnull
    @Override
    public AnyDamageMatcher copy()
    {
        return new AnyDamageMatcher(matching);
    }
}