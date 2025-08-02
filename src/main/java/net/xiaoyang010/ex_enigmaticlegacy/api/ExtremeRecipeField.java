package net.xiaoyang010.ex_enigmaticlegacy.api;

import morph.avaritia.api.ExtremeCraftingRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF.IField;
import org.jetbrains.annotations.NotNull;
import java.util.Optional;

public class ExtremeRecipeField implements IField<ExtremeRecipeField> {
    private ExtremeCraftingRecipe extremeRecipe;
    private ResourceLocation recipeId;

    @NotNull
    @Override
    public String getFieldName()
    {
        return "extreme.recipe.field";
    }

    public boolean isNull()
    {
        return extremeRecipe == null;
    }

    public void setExtremeRecipe(final ExtremeCraftingRecipe extremeRecipe) {
        this.extremeRecipe = extremeRecipe;
        this.recipeId = extremeRecipe != null ? extremeRecipe.getId() : null;
    }

    public ExtremeCraftingRecipe getExtremeRecipe()
    {
        return extremeRecipe;
    }

    public ItemStack getExtremeRecipeOutput() {
        return extremeRecipe != null ? extremeRecipe.getResultItem() : ItemStack.EMPTY;
    }

    @NotNull
    @Override
    public ExtremeRecipeField copy() {
        final ExtremeRecipeField extremeRecipeField = new ExtremeRecipeField();
        extremeRecipeField.readNBT(writeNBT());
        return extremeRecipeField;
    }

    @Override
    public void receiveNBT(@NotNull final CompoundTag compoundTag)
    {
        readNBT(compoundTag);
    }

    @NotNull
    @Override
    public CompoundTag writeNBT() {
        final CompoundTag tag = IField.super.writeNBT();
        tag.putString("fieldName", getFieldName());
        if (extremeRecipe != null && extremeRecipe.getId() != null)
            tag.putString("extreme.recipe", extremeRecipe.getId().toString());
        return tag;
    }

    @Override
    public void readNBT(@NotNull final CompoundTag compoundTag) {
        if (compoundTag.contains("extreme.recipe")) {
            String recipeIdString = compoundTag.getString("extreme.recipe");
            ResourceLocation recipeLocation = new ResourceLocation(recipeIdString);
            this.recipeId = recipeLocation;

            Level level = getLevel();
            if (level != null) {
                Optional<? extends ExtremeCraftingRecipe> recipeOpt = level.getRecipeManager()
                        .byKey(recipeLocation)
                        .filter(recipe -> recipe instanceof ExtremeCraftingRecipe)
                        .map(recipe -> (ExtremeCraftingRecipe) recipe);

                this.extremeRecipe = recipeOpt.orElse(null);
            }
        } else {
            this.extremeRecipe = null;
            this.recipeId = null;
        }
    }

    private Level getLevel() {
        if (isClientSide()) {
            return Minecraft.getInstance().level;
        }
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    private boolean isClientSide() {
        try {
            return Minecraft.getInstance() != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof ExtremeRecipeField) {
            ExtremeRecipeField other = (ExtremeRecipeField) obj;
            if (this.extremeRecipe != null && other.extremeRecipe != null) {
                return this.extremeRecipe.getId().equals(other.extremeRecipe.getId());
            }
            return this.extremeRecipe == other.extremeRecipe;
        }

        if (obj instanceof ResourceLocation && extremeRecipe != null) {
            return obj.equals(extremeRecipe.getId());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return extremeRecipe != null && extremeRecipe.getId() != null ?
                extremeRecipe.getId().hashCode() : 0;
    }
}