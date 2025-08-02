package net.xiaoyang010.ex_enigmaticlegacy.api;

import morph.avaritia.api.CompressorRecipe;
import morph.avaritia.recipe.CompressorRecipeHelper;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.TileEntityInfinityCompressor;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF.IField;

import javax.annotation.Nonnull;

public class CompressorRecipeField implements IField<CompressorRecipeField>
{
    private final TileEntityInfinityCompressor tileEntityInfinityCompressor;
    private CompressorRecipe compressorRecipe;
    private int progress;

    public CompressorRecipeField(@Nonnull final TileEntityInfinityCompressor tileEntityInfinityCompressor)
    {
        this.tileEntityInfinityCompressor = tileEntityInfinityCompressor;
    }

    @Nonnull
    @Override
    public String getFieldName()
    {
        return "compressor.recipe.field";
    }

    public void addProgress(final int progressToAdd) {
        final int compressionCost = compressorRecipe.getCost();
        progress += progressToAdd;
        final int dif = progress - compressionCost;
        progress = Mth.clamp(progress, 0, compressionCost);
        if (dif >= 0) {
            progress = dif;
            final ItemStack outputStack = tileEntityInfinityCompressor.getItem(0);
            final ItemStack recipeResult = compressorRecipe.getResultItem();
            if (outputStack.isEmpty())
                tileEntityInfinityCompressor.setItem(0, recipeResult.copy());
            else
                outputStack.setCount(outputStack.getCount() + recipeResult.getCount());
            if (progress == 0)
                setCompressorRecipe(null);
        }
    }

    public String getProgress()
    {
        return compressorRecipe != null ? progress + " / " + compressorRecipe.getCost() : I18n.get("ex_enigmaticlegacy.compressor.no.recipe.set");
    }

    public boolean isNull()
    {
        return compressorRecipe == null;
    }

    public void setCompressorRecipe(final CompressorRecipe compressorRecipe)
    {
        this.compressorRecipe = compressorRecipe;
        progress = 0;
    }

    public CompressorRecipe getCompressorRecipe()
    {
        return compressorRecipe;
    }

    public ItemStack getCompressorRecipeOutput()
    {
        return compressorRecipe != null ? compressorRecipe.getResultItem().copy() : ItemStack.EMPTY;
    }

    public boolean matches(@Nonnull final ItemStack stack)
    {
        if (compressorRecipe == null || tileEntityInfinityCompressor.getLevel() == null) {
            return false;
        }

        return CompressorRecipeHelper.inputMatchesRecipeForOutput(
                tileEntityInfinityCompressor.getLevel(),
                stack,
                compressorRecipe.getResultItem()
        );
    }

    @Nonnull
    @Override
    public CompressorRecipeField copy()
    {
        final CompressorRecipeField compressorRecipeField = new CompressorRecipeField(tileEntityInfinityCompressor);
        compressorRecipeField.readNBT(writeNBT());
        return compressorRecipeField;
    }

    @Override
    public void receiveNBT(@Nonnull final CompoundTag compoundTag)
    {
        readNBT(compoundTag);
    }

    @Nonnull
    @Override
    public CompoundTag writeNBT()
    {
        final CompoundTag tag = IField.super.writeNBT();
        final ResourceLocation resourceLocation;
        tag.putString("fieldName", getFieldName());
        if (compressorRecipe != null && (resourceLocation = compressorRecipe.getId()) != null) {
            tag.putString("compressor.recipe", resourceLocation.toString());
            tag.putInt("compressor.recipe.progress", progress);
        }
        return tag;
    }

    @Override
    public void readNBT(@Nonnull final CompoundTag compoundTag)
    {
        if (compoundTag.contains("compressor.recipe") && tileEntityInfinityCompressor.getLevel() != null) {
            ResourceLocation recipeId = new ResourceLocation(compoundTag.getString("compressor.recipe"));
            var recipeMap = CompressorRecipeHelper.getRecipeMap(tileEntityInfinityCompressor.getLevel());
            this.compressorRecipe = recipeMap.get(recipeId);
        } else {
            this.compressorRecipe = null;
        }
        this.progress = compoundTag.getInt("compressor.recipe.progress");
    }

    @Override
    public boolean equals(final Object obj)
    {
        return obj instanceof CompressorRecipeField && ((CompressorRecipeField) obj).compressorRecipe == this.compressorRecipe && ((CompressorRecipeField) obj).progress == this.progress;
    }
}