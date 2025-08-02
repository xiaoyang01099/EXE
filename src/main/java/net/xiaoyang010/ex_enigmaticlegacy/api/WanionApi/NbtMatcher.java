package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public final class NbtMatcher extends AbstractMatcher<NbtMatcher>
{
    public NbtMatcher(@Nonnull final AbstractMatching<?> matching)
    {
        super(matching);
    }

    @Nonnull
    @Override
    public MatcherEnum getMatcherEnum()
    {
        return MatcherEnum.NBT;
    }

    @Nonnull
    @Override
    public AbstractMatcher<?> validate()
    {
        return getStack().hasTag() ? this : matching.getDefaultMatcher();
    }

    @Override
    public boolean matches(@Nonnull final ItemStack otherItemStack)
    {
        final ItemStack itemStack = getStack();
        return otherItemStack.hasTag() && itemStack.is(otherItemStack.getItem()) && ItemStack.tagMatches(itemStack, otherItemStack);
    }

    @Nonnull
    @Override
    public NbtMatcher copy()
    {
        return new NbtMatcher(matching);
    }


    @Override
    public boolean equals(final Object obj)
    {
        return obj instanceof NbtMatcher;
    }

}