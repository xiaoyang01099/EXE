package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public final class Util {
    private Util() {}

    @SuppressWarnings("unchecked")
    public static <T, E extends T> E getField(Class<?> clas, String name, Object instance, Class<T> expectedClass)
    {
        try {
            final Field field = clas.getDeclaredField(name);
            field.setAccessible(true);
            return (E) expectedClass.cast(field.get(instance));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T, E extends T> E getField(Class<?> clas, String unobfuscatedName, String obfuscatedName, Object instance, Class<T> expectedClass) {
        try {
            Field field;
            try {
                field = clas.getDeclaredField(obfuscatedName);
            } catch (Exception e) {
                field = clas.getDeclaredField(unobfuscatedName);
            }
            field.setAccessible(true);
            return (E) expectedClass.cast(field.get(instance));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <E> void setField(Class<?> clas, String name, Object instance, E newInstance) {
        try {
            final Field field = clas.getDeclaredField(name);
            field.setAccessible(true);
            field.set(instance, newInstance);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nonnull
    public static String getModName(@Nonnull final ItemStack itemStack) {
        if (itemStack.isEmpty())
            return "";

        final Item item = itemStack.getItem();
        final ResourceLocation registryName = ForgeRegistries.ITEMS.getKey(item);
        return registryName != null ? registryName.getNamespace() : "";
    }

    public static boolean isFromVanilla(@Nonnull final ItemStack itemStack) {
        return getModName(itemStack).equals("minecraft");
    }

    public static boolean areFromSameMod(@Nonnull final ItemStack stack1, @Nonnull final ItemStack stack2) {
        return getModName(stack1).equals(getModName(stack2));
    }

    public static <O> void fillArray(@Nonnull final O[] array, @Nonnull final O defaultInstance) {
        Arrays.fill(array, defaultInstance);
    }

    @SuppressWarnings("unchecked")
    public static <O> O[] treeDimArrayToTwoDimArray(@Nonnull final O[][] treeDimArray) {
        final List<O> oList = new ArrayList<>();
        for (final O[] twoDimArray : treeDimArray)
            if (twoDimArray != null)
                oList.addAll(Arrays.asList(twoDimArray));
        return (O[]) oList.toArray();
    }

    public static ItemStack getStackFromIngredient(@Nonnull final Ingredient ingredient) {
        final ItemStack[] stacks = ingredient.getItems();
        return stacks.length > 0 ? stacks[0] : ItemStack.EMPTY;
    }

    public static String getTagNameFromIngredient(@Nonnull final Ingredient ingredient) {
        return null;
    }

    public static <R> Predicate<R> not(@Nonnull final Predicate<R> predicate) {
        return predicate.negate();
    }

    public static boolean itemStackHasTags(@Nonnull final ItemStack itemStack) {
        if (itemStack.isEmpty())
            return false;

        final ITagManager<Item> tagManager = ForgeRegistries.ITEMS.tags();
        if (tagManager == null)
            return false;

        return tagManager.getTagNames()
                .anyMatch(tagKey -> tagManager.getTag(tagKey).contains(itemStack.getItem()));
    }

    @Deprecated
    public static boolean isDamageable(@Nonnull final ItemStack stack)
    {
        return stack.isDamageableItem();
    }

    public static boolean isDamageableItem(@Nonnull final ItemStack stack)
    {
        return stack.isDamageableItem();
    }

    public static String getModNameFromModId(@Nonnull final String modId) {
        final Optional<? extends ModContainer> modContainer = getModContainerFromModId(modId);
        return modContainer.map(container -> container.getModInfo().getDisplayName()).orElse(null);
    }

    public static Optional<? extends ModContainer> getModContainerFromModId(@Nonnull final String modId) {
        return ModList.get().getModContainerById(modId);
    }

    public static Optional<? extends ModContainer> getModContainerFromStack(@Nonnull final ItemStack itemStack) {
        final ResourceLocation resourceLocation = ForgeRegistries.ITEMS.getKey(itemStack.getItem());
        return resourceLocation != null ? getModContainerFromModId(resourceLocation.getNamespace()) : Optional.empty();
    }

    public static List<TagKey<Item>> getItemTags(@Nonnull final ItemStack itemStack) {
        if (itemStack.isEmpty())
            return new ArrayList<>();

        final List<TagKey<Item>> tags = new ArrayList<>();
        final ITagManager<Item> tagManager = ForgeRegistries.ITEMS.tags();

        if (tagManager != null) {
            tagManager.getTagNames().forEach(tagKey -> {
                if (tagManager.getTag(tagKey).contains(itemStack.getItem())) {
                    tags.add(tagKey);
                }
            });
        }

        return tags;
    }

    public static boolean isInTag(@Nonnull final ItemStack itemStack, @Nonnull final TagKey<Item> tag) {
        if (itemStack.isEmpty())
            return false;

        final ITagManager<Item> tagManager = ForgeRegistries.ITEMS.tags();
        return tagManager != null && tagManager.getTag(tag).contains(itemStack.getItem());
    }
}