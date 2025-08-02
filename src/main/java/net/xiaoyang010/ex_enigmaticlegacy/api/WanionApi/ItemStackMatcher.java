package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public final class ItemStackMatcher extends AbstractMatcher<ItemStackMatcher>
{
    public ItemStackMatcher(@Nonnull final AbstractMatching<?> matching)
    {
        super(matching);
    }

    @Nonnull
    @Override
    public MatcherEnum getMatcherEnum()
    {
        return MatcherEnum.ITEM_STACK;
    }

    @Nonnull
    @Override
    public AbstractMatcher<?> validate()
    {
        return !getStack().isEmpty() ? this : new EmptyMatcher(matching);
    }

    @Override
    public boolean matches(@Nonnull final ItemStack otherItemStack)
    {
        return ItemStack.isSameItemSameTags(getStack(), otherItemStack);
    }

    @Override
    public boolean equals(final Object obj)
    {
        return obj instanceof ItemStackMatcher;
    }

    @Nonnull
    @Override
    public ItemStackMatcher copy()
    {
        return new ItemStackMatcher(matching);
    }
}