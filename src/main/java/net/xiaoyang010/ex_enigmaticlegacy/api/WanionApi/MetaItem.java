package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import it.unimi.dsi.fastutil.ints.*;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.Collection;

@SuppressWarnings("unused")
public final class MetaItem
{
    private MetaItem() {}

    public static int get(final ItemStack itemStack)
    {
        if (itemStack == null || itemStack.isEmpty())
            return 0;

        final Item item = itemStack.getItem();
        if (item == null)
            return 0;

        final ResourceLocation itemKey = ForgeRegistries.ITEMS.getKey(item);
        if (itemKey == null)
            return 0;

        final int id = Math.abs(itemKey.hashCode());
        if (id <= 0)
            return 0;

        final int damage = itemStack.getDamageValue();
        return id | ((damage + 1) << 16);
    }

    public static int get(final Item item)
    {
        if (item == null)
            return 0;
        final ResourceLocation itemKey = ForgeRegistries.ITEMS.getKey(item);
        if (itemKey == null)
            return 0;
        final int id = Math.abs(itemKey.hashCode());
        return id > 0 ? id | 65536 : 0;
    }

    public static ItemStack toItemStack(final int metaItemKey)
    {
        if (metaItemKey <= 0)
            return ItemStack.EMPTY;

        final int damage = (metaItemKey >> 16) - 1;

        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
            if (key != null && Math.abs(key.hashCode()) == (metaItemKey & 65535)) {
                ItemStack stack = new ItemStack(item, 1);

                if (damage > 0 && stack.isDamageableItem()) {
                    stack.setDamageValue(Math.min(damage, stack.getMaxDamage()));
                }

                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    public static int getCumulative(@Nonnull final ItemStack... itemStacks)
    {
        int cumulativeKey = 0;
        for (final ItemStack itemStack : itemStacks)
            cumulativeKey += get(itemStack);
        return cumulativeKey;
    }

    public static int[] getArray(final Collection<ItemStack> itemStackCollection)
    {
        return getList(itemStackCollection).toIntArray();
    }

    public static IntList getList(final Collection<ItemStack> itemStackCollection)
    {
        final IntList keys = new IntArrayList();
        int hash;
        for (final ItemStack itemStack : itemStackCollection)
            if ((hash = get(itemStack)) != 0)
                keys.add(hash);
        return keys;
    }

    public static IntSet getSet(final Collection<ItemStack> itemStackCollection)
    {
        return new IntOpenHashSet(getList(itemStackCollection));
    }

    public static <E> void populateMap(final Collection<ItemStack> itemStackCollection, final Int2ObjectMap<E> map, final E defaultValue)
    {
        for (final int id : getArray(itemStackCollection))
            map.put(id, defaultValue);
    }

    public static void populateMap(final Collection<ItemStack> itemStackCollection, final Int2LongMap map, long defaultValue)
    {
        for (final int id : getArray(itemStackCollection))
            map.put(id, defaultValue);
    }

    public static Int2IntMap getKeySizeMap(final int startIndex, final int endIndex, @Nonnull NonNullList<ItemStack> itemStacks)
    {
        final Int2IntMap keySizeMap = new Int2IntOpenHashMap();
        for (int i = startIndex; i < endIndex && i < itemStacks.size(); i++) {
            final ItemStack itemStack = itemStacks.get(i);
            if (itemStack.isEmpty())
                continue;
            final int key = get(itemStack);
            if (key == 0)
                continue;
            keySizeMap.put(key, keySizeMap.getOrDefault(key, 0) + 1);
        }
        return keySizeMap;
    }

    public static Int2IntMap getKeySizeMap(final int startIndex, final int endIndex, @Nonnull ItemStack[] itemStacks)
    {
        final Int2IntMap keySizeMap = new Int2IntOpenHashMap();
        for (int i = startIndex; i < endIndex && i < itemStacks.length; i++) {
            final ItemStack itemStack = itemStacks[i];
            if (itemStack == null || itemStack.isEmpty())
                continue;
            final int key = get(itemStack);
            if (key == 0)
                continue;
            keySizeMap.put(key, keySizeMap.getOrDefault(key, 0) + 1);
        }
        return keySizeMap;
    }

    public static Int2IntMap getSmartKeySizeMap(final int startIndex, final int endIndex, @Nonnull NonNullList<ItemStack> itemStacks)
    {
        final Int2IntMap smartKeySizeMap = new Int2IntOpenHashMap();
        for (int i = startIndex; i < endIndex && i < itemStacks.size(); i++) {
            final ItemStack itemStack = itemStacks.get(i);
            if (itemStack.isEmpty())
                continue;
            final int key = get(itemStack);
            if (key == 0)
                continue;
            smartKeySizeMap.put(key, smartKeySizeMap.getOrDefault(key, 0) + itemStack.getCount());
        }
        return smartKeySizeMap;
    }

    public static Int2IntMap getSmartKeySizeMap(final int startIndex, final int endIndex, @Nonnull ItemStack[] itemStacks)
    {
        final Int2IntMap smartKeySizeMap = new Int2IntOpenHashMap();
        for (int i = startIndex; i < endIndex && i < itemStacks.length; i++) {
            final ItemStack itemStack = itemStacks[i];
            if (itemStack == null || itemStack.isEmpty())
                continue;
            final int key = get(itemStack);
            if (key == 0)
                continue;
            smartKeySizeMap.put(key, smartKeySizeMap.getOrDefault(key, 0) + itemStack.getCount());
        }
        return smartKeySizeMap;
    }
}