package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import net.minecraft.world.item.ItemStack;
import javax.annotation.Nonnull;

public final class DamagedMatcher extends AbstractMatcher<DamagedMatcher>
{
    public DamagedMatcher(@Nonnull final AbstractMatching<?> matching)
    {
        super(matching);
    }

    @Nonnull
    @Override
    public MatcherEnum getMatcherEnum()
    {
        return MatcherEnum.DAMAGED;
    }

    @Nonnull
    @Override
    public AbstractMatcher<?> validate()
    {
        return getStack().isDamaged() ? this : matching.getDefaultMatcher();
    }

    @Override
    public boolean matches(@Nonnull final ItemStack otherItemStack)
    {
        final ItemStack itemStack = getStack();
        return !itemStack.isEmpty() && itemStack.is(otherItemStack.getItem()) && itemStack.isDamaged() && otherItemStack.isDamaged();
    }

    @Override
    public boolean equals(final Object obj)
    {
        return obj instanceof DamagedMatcher;
    }

    @Nonnull
    @Override
    public DamagedMatcher copy()
    {
        return new DamagedMatcher(matching);
    }
}