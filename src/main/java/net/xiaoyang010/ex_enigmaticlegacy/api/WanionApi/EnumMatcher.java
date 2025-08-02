package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;

public final class EnumMatcher extends AbstractMatcher<EnumMatcher>
{
    private MatcherEnum matcherEnum;

    public EnumMatcher(@Nonnull final AbstractMatching<?> matching)
    {
        this(matching, MatcherEnum.DISABLED);
    }

    public EnumMatcher(@Nonnull final AbstractMatching<?> matching, @Nonnull final MatcherEnum matcherEnum)
    {
        super(matching);
        this.matcherEnum = matcherEnum;
    }

    @Override
    @Nonnull
    public CompoundTag writeNBT()
    {
        final CompoundTag matchingNbt = super.writeNBT();
        if (matcherEnum != MatcherEnum.DISABLED)
            matchingNbt.putString("enum", matcherEnum.lowerCaseName);
        return matchingNbt;
    }

    @Override
    public void readNBT(@Nonnull final CompoundTag compoundTag)
    {
        matcherEnum = compoundTag.contains("enum") ? MatcherEnum.getMatcherEnumByName(compoundTag.getString("enum")) : MatcherEnum.DISABLED;
    }

    @Nonnull
    @Override
    public EnumMatcher copy()
    {
        return new EnumMatcher(matching, matcherEnum);
    }

    @Nonnull
    @Override
    public MatcherEnum getMatcherEnum()
    {
        return MatcherEnum.ENUM;
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
        return matcherEnum.accepts(otherItemStack);
    }
}