package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.ChatFormatting;

import javax.annotation.Nonnull;

public final class ModMatcher extends AbstractMatcher<ModMatcher>
{
    private String modId;

    public ModMatcher(@Nonnull final AbstractMatching<?> matching)
    {
        super(matching);
    }

    @Override
    @Nonnull
    public ModMatcher copy()
    {
        final ModMatcher nModMatcher = new ModMatcher(matching);
        nModMatcher.readNBT(writeNBT());
        return nModMatcher;
    }

    @Override
    @Nonnull
    public CompoundTag writeNBT()
    {
        final CompoundTag matchingNbt = super.writeNBT();
        if (modId != null)
            matchingNbt.putString("modId", modId);
        return matchingNbt;
    }

    @Override
    public void readNBT(@Nonnull final CompoundTag compoundTag)
    {
        if (compoundTag.contains("modId"))
            modId = compoundTag.getString("modId");
    }

    @Nonnull
    @Override
    public MatcherEnum getMatcherEnum()
    {
        return MatcherEnum.MOD;
    }

    @Nonnull
    @Override
    public AbstractMatcher<?> validate()
    {
        final ItemStack stack = getStack();
        if (!stack.isEmpty())
            modId = Util.getModName(stack);
        return modId != null ? this : matching.getDefaultMatcher();
    }

    @Override
    public boolean matches(@Nonnull final ItemStack otherItemStack)
    {
        final ItemStack stack = getStack();
        return !stack.isEmpty() && !otherItemStack.isEmpty() && modId.equals(Util.getModName(otherItemStack));
    }

    @Nonnull
    @Override
    public String getDescription()
    {
        return super.getDescription() + " " + ChatFormatting.GOLD + modId;
    }

    @Override
    public boolean equals(final Object obj)
    {
        return obj == this || (obj instanceof ModMatcher && modId != null && modId.equals(((ModMatcher) obj).modId));
    }
}