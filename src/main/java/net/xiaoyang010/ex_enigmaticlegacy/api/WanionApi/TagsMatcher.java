package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.ChatFormatting;
import net.minecraft.tags.TagKey;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

public final class TagsMatcher extends AbstractMatcher<TagsMatcher>
{
    private String tagName;
    private List<TagKey<Item>> tags;
    private int actualTag;

    public TagsMatcher(@Nonnull final AbstractMatching<?> matching)
    {
        this(matching, null);
    }

    public TagsMatcher(@Nonnull final AbstractMatching<?> matching, final String tagName)
    {
        super(matching);
        this.tagName = tagName;
    }

    @Nonnull
    @Override
    public CompoundTag writeNBT()
    {
        final CompoundTag matchingNbt = super.writeNBT();
        matchingNbt.putString("tagName", tagName != null ? tagName : "");
        return matchingNbt;
    }

    @Override
    public void readNBT(@Nonnull final CompoundTag compoundTag)
    {
        tagName = compoundTag.getString("tagName");
    }

    @Nonnull
    @Override
    public MatcherEnum getMatcherEnum()
    {
        return MatcherEnum.TAGS;
    }

    @Nonnull
    @Override
    public AbstractMatcher<?> validate()
    {
        final ItemStack matchingStack = getStack();
        if (!matchingStack.isEmpty() && this.tags == null) {
            this.tags = getItemTags(matchingStack);
        }
        if (this.tags == null || this.tags.isEmpty())
            return matching.getDefaultMatcher();

        if ((tagName == null || tagName.isEmpty()) && !tags.isEmpty()) {
            actualTag = 0;
            this.tagName = tags.get(actualTag).location().toString();
        }

        if (this.tags != null && !(tagName == null || tagName.isEmpty()) && actualTag < this.tags.size()) {
            TagKey<Item> targetTag = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(tagName));
            if (matchingStack.is(targetTag)) {
                for (int x = 0; x < tags.size(); x++) {
                    if (tags.get(x).location().toString().equals(tagName)) {
                        this.actualTag = x;
                        return this;
                    }
                }
            }
        }
        return matching.getDefaultMatcher();
    }

    @Override
    public boolean canMoveOn()
    {
        if (++actualTag >= tags.size())
            return true;
        this.tagName = tags.get(actualTag).location().toString();
        return false;
    }

    @Override
    public boolean matches(@Nonnull final ItemStack otherItemStack)
    {
        if (tags == null || tags.isEmpty()) return false;
        TagKey<Item> currentTag = tags.get(actualTag);
        return otherItemStack.is(currentTag);
    }

    @Nonnull
    @Override
    public String getDescription()
    {
        return super.getDescription() + " " + ChatFormatting.GOLD + tagName;
    }

    public TagKey<Item> getCurrentTag()
    {
        if (tags == null || actualTag >= tags.size()) return null;
        return tags.get(actualTag);
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == this)
            return true;
        else if (obj instanceof TagsMatcher) {
            final TagsMatcher tagMatcher = (TagsMatcher) obj;
            if (tagName == null || !tagName.equals(tagMatcher.tagName))
                return false;
            if (this.actualTag != tagMatcher.actualTag ||
                    (tags != null && tagMatcher.tags != null && tags.size() != tagMatcher.tags.size()))
                return false;
            if (tags != null && tagMatcher.tags != null) {
                for (int i = 0; i < tags.size(); i++)
                    if (!tags.get(i).equals(tagMatcher.tags.get(i)))
                        return false;
            }
            return true;
        } else return false;
    }

    private List<TagKey<Item>> getItemTags(ItemStack itemStack)
    {
        List<TagKey<Item>> itemTags = new ArrayList<>();
        StreamSupport.stream(Registry.ITEM.getTagNames().spliterator(), false)
                .filter(tagKey -> itemStack.is(tagKey))
                .forEach(itemTags::add);
        return itemTags;
    }

    @Nonnull
    @Override
    public TagsMatcher copy()
    {
        final TagsMatcher newMatcher = new TagsMatcher(matching);
        newMatcher.readNBT(writeNBT());
        return newMatcher;
    }
}