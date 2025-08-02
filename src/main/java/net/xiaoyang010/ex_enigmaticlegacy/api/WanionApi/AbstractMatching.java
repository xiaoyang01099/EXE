package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF.IControlNameable;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF.ICopyable;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF.ISmartNBT;
import javax.annotation.Nonnull;
import java.util.function.Supplier;

public abstract class AbstractMatching<M extends AbstractMatching<M>> implements ISmartNBT, ICopyable<M>, IControlNameable
{
    private static final Supplier<ItemStack> EMPTY_SUPPLIER = () -> ItemStack.EMPTY;
    protected final Supplier<ItemStack> stackSupplier;
    protected final int number;
    protected AbstractMatcher<?> matcher = new ItemStackMatcher(this);

    public AbstractMatching(final Supplier<ItemStack> stackSupplier, final int number)
    {
        this(stackSupplier, number, null);
    }

    public AbstractMatching(final Supplier<ItemStack> stackSupplier, final int number, final CompoundTag tagToRead)
    {
        this.stackSupplier = stackSupplier != null ? stackSupplier : EMPTY_SUPPLIER;
        this.number = number;
        if (tagToRead != null)
            readNBT(tagToRead);
    }

    public final void resetMatcher()
    {
        this.matcher = getDefaultMatcher();
    }

    @Nonnull
    public AbstractMatcher<?> getDefaultMatcher()
    {
        return new ItemStackMatcher(this).validate();
    }

    public void nextMatcher() {}

    public final AbstractMatcher<?> getMatcher()
    {
        return matcher;
    }

    public final void setMatcher(@Nonnull final AbstractMatcher<?> matcher)
    {
        this.matcher = matcher.validate();
    }

    public final boolean accepts(@Nonnull final ItemStack itemStack)
    {
        return matcher.accepts(itemStack);
    }

    public final boolean matches(@Nonnull final ItemStack otherItemStack)
    {
        return matcher.matches(otherItemStack);
    }

    public void validate()
    {
        this.matcher = matcher.validate();
    }

    public ItemStack getStack()
    {
        return stackSupplier.get();
    }

    public boolean isEmpty()
    {
        return getStack().isEmpty();
    }

    @Override
    @Nonnull
    public final CompoundTag writeNBT()
    {
        final CompoundTag matcherNBT = new CompoundTag();
        matcherNBT.putInt("number", number);
        matcherNBT.putString("matcherType", matcher.getMatcherEnum().getLowerCaseName());
        matcherNBT.put("matcher", matcher.writeNBT());
        customWriteNBT(matcherNBT);
        return matcherNBT;
    }

    @Override
    public final void readNBT(@Nonnull final CompoundTag compoundTag)
    {
        final AbstractMatcher<?> matcher = MatcherEnum.getMatcherEnumByName(compoundTag.getString("matcherType")).getMatcher(this);
        matcher.readNBT(compoundTag.getCompound("matcher"));
        customReadNBT(compoundTag);
        setMatcher(matcher);
    }

    public void customWriteNBT(@Nonnull final CompoundTag compoundTag) {}
    public void customReadNBT(@Nonnull final CompoundTag compoundTag) {}

    @Override
    @Nonnull
    public final String getControlName()
    {
        return "ex_enigmaticlegacy.matching.control";
    }

    public final int getNumber()
    {
        return number;
    }

    @Override
    public final int hashCode()
    {
        return number;
    }
}