package org.xiaoyang.ex_enigmaticlegacy.Recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModItems;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModRecipes;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.xplat.XplatAbstractions;

import javax.annotation.Nonnull;
import java.util.UUID;

public class AesirRingRecipe implements CraftingRecipe {
    private final ResourceLocation id;
    private final CraftingBookCategory category;

    public AesirRingRecipe(ResourceLocation id,CraftingBookCategory category) {
        this.id = id;
        this.category = category;
    }

    @Override
    public boolean matches(@Nonnull CraftingContainer inv, @Nonnull Level world) {
        boolean foundThorRing = false;
        boolean foundOdinRing = false;
        boolean foundLokiRing = false;

        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                if (stack.getItem() == BotaniaItems.thorRing && !foundThorRing) {
                    foundThorRing = true;
                } else if (stack.getItem() == BotaniaItems.odinRing && !foundOdinRing) {
                    foundOdinRing = true;
                } else {
                    if (stack.getItem() != BotaniaItems.lokiRing || foundLokiRing) {
                        return false;
                    }
                    foundLokiRing = true;
                }
            }
        }

        return foundThorRing && foundOdinRing && foundLokiRing;
    }

    @Nonnull
    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
        UUID soulbindUUID = null;

        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                var relic = XplatAbstractions.INSTANCE.findRelic(stack);
                if (relic == null) {
                    return ItemStack.EMPTY;
                }

                UUID bindUUID = relic.getSoulbindUUID();
                if (bindUUID != null) {
                    if (soulbindUUID == null) {
                        soulbindUUID = bindUUID;
                    } else if (!soulbindUUID.equals(bindUUID)) {
                        return ItemStack.EMPTY;
                    }
                }
            }
        }

        ItemStack result = new ItemStack(ModItems.AESIR_RING.get());

        if (soulbindUUID != null) {
            var resultRelic = XplatAbstractions.INSTANCE.findRelic(result);
            if (resultRelic != null) {
                resultRelic.bindToUUID(soulbindUUID);
            }
        }

        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 3;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Nonnull
    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.AESIR_RING.get();
    }

    @Nonnull
    @Override
    public NonNullList<ItemStack> getRemainingItems(@Nonnull CraftingContainer inv) {
        return NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
    }

    @Override
    public CraftingBookCategory category() {
        return category;
    }
}