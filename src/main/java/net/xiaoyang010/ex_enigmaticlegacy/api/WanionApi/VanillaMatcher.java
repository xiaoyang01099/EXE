package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import net.minecraft.world.item.ItemStack;
import javax.annotation.Nonnull;

public final class VanillaMatcher extends AbstractMatcher<VanillaMatcher> {
    public VanillaMatcher(@Nonnull final AbstractMatching<?> matching) {
        super(matching);
    }

    @Override
    @Nonnull
    public VanillaMatcher copy()
    {
        return new VanillaMatcher(matching);
    }

    @Nonnull
    @Override
    public MatcherEnum getMatcherEnum()
    {
        return MatcherEnum.VANILLA;
    }

    @Nonnull
    @Override
    public AbstractMatcher<?> validate()
    {
        return Util.isFromVanilla(getStack()) ? this : matching.getDefaultMatcher();
    }

    @Override
    public boolean matches(@Nonnull final ItemStack otherItemStack)
    {
        final ItemStack stack = getStack();
        return !stack.isEmpty() && !otherItemStack.isEmpty() && Util.areFromSameMod(stack, otherItemStack);
    }

    @Override
    public boolean equals(final Object obj)
    {
        return obj == this || (obj instanceof VanillaMatcher && Util.areFromSameMod(getStack(), ((VanillaMatcher) obj).getStack()));
    }
}