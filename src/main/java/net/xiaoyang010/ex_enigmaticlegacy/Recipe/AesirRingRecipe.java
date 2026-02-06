package net.xiaoyang010.ex_enigmaticlegacy.Recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRecipes;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.xplat.IXplatAbstractions;
import javax.annotation.Nonnull;
import java.util.UUID;

public class AesirRingRecipe implements CraftingRecipe {
    private final ResourceLocation id;

    public AesirRingRecipe(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public boolean matches(@Nonnull CraftingContainer inv, @Nonnull Level world) {
        boolean foundThorRing = false;
        boolean foundOdinRing = false;
        boolean foundLokiRing = false;

        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                if (stack.getItem() == ModItems.thorRing && !foundThorRing) {
                    foundThorRing = true;
                } else if (stack.getItem() == ModItems.odinRing && !foundOdinRing) {
                    foundOdinRing = true;
                } else {
                    if (stack.getItem() != ModItems.lokiRing || foundLokiRing) {
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
    public ItemStack assemble(@Nonnull CraftingContainer inv) {
        UUID soulbindUUID = null;

        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                var relic = IXplatAbstractions.INSTANCE.findRelic(stack);
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

        ItemStack result = new ItemStack(net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems.AESIR_RING.get());

        if (soulbindUUID != null) {
            var resultRelic = IXplatAbstractions.INSTANCE.findRelic(result);
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

    @Nonnull
    @Override
    public ItemStack getResultItem() {
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
}