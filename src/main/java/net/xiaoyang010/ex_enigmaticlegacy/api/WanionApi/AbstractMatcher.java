package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF.ICopyable;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF.ISmartNBT;

import javax.annotation.Nonnull;

public abstract class AbstractMatcher<M extends AbstractMatcher<M>> implements ISmartNBT, ICopyable<M>
{
    protected AbstractMatching<?> matching;

    public AbstractMatcher(@Nonnull final AbstractMatching<?> matching)
    {
        this.matching = matching;
    }

    public final ItemStack getStack()
    {
        return matching.getStack();
    }

    @Nonnull
    @Override
    public CompoundTag writeNBT()
    {
        return new CompoundTag();
    }

    @Override
    public void readNBT(@Nonnull final CompoundTag compoundTag) {}

    @Nonnull
    public abstract MatcherEnum getMatcherEnum();

    @Nonnull
    public abstract AbstractMatcher<?> validate();

    public boolean canMoveOn()
    {
        return true;
    }

    public final boolean accepts(@Nonnull final ItemStack itemStack)
    {
        return getMatcherEnum().accepts(itemStack);
    }

    public abstract boolean matches(@Nonnull ItemStack otherItemStack);

//    @Nonnull
//    public final String ctFormat()
//    {
//        return CraftTweakerHelper.MatcherToCtFormat(this);
//    }

    @Nonnull
    @OnlyIn(Dist.CLIENT)
    public String getDescription()
    {
        return I18n.get("ex_enigmaticlegacy.matching.matcher." + getMatcherEnum().getLowerCaseName());
    }

    @Nonnull
    public String getDescriptionKey()
    {
        return "ex_enigmaticlegacy.matching.matcher." + getMatcherEnum().getLowerCaseName();
    }
}