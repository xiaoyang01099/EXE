package net.xiaoyang010.ex_enigmaticlegacy.Init;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraftforge.common.crafting.ConditionalAdvancement;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.ItemExistsCondition;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Recipe.RegenIvyRecipe;

import java.util.function.Consumer;

public class RecipeProvider extends net.minecraft.data.recipes.RecipeProvider {

    public RecipeProvider(DataGenerator generatorIn) {
        super(generatorIn);
    }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {
        specialRecipe(consumer, (SimpleRecipeSerializer<?>) RegenIvyRecipe.SERIALIZER);
        registerStandardRecipes(consumer);
    }

    private void registerStandardRecipes(Consumer<FinishedRecipe> consumer) {
        resultExists(consumer,
                ShapelessRecipeBuilder.shapeless(ModItems.IVYREGEN.get())
                        .requires(Items.VINE)
                        .requires(vazkii.botania.common.item.ModItems.lifeEssence)
                        .unlockedBy("has_item", has(vazkii.botania.common.item.ModItems.lifeEssence)),
                has(vazkii.botania.common.item.ModItems.lifeEssence),
                ModItems.IVYREGEN.get());
    }

    private void specialRecipe(Consumer<FinishedRecipe> consumer, SimpleRecipeSerializer<?> serializer) {
        SpecialRecipeBuilder.special(serializer)
                .save(consumer, String.valueOf(new ResourceLocation(ExEnigmaticlegacyMod.MODID,
                        "dynamic/" + serializer.getRegistryName().getPath())));
    }

    private void resultExists(Consumer<FinishedRecipe> consumer,
                              ShapelessRecipeBuilder recipeBuilder,
                              InventoryChangeTrigger.TriggerInstance trigger,
                              net.minecraft.world.level.ItemLike result) {
        ConditionalRecipe.builder()
                .addCondition(new ItemExistsCondition(result.asItem().getRegistryName()))
                .addRecipe(recipeBuilder::save)
                .setAdvancement(
                        new ResourceLocation(ExEnigmaticlegacyMod.MODID,
                                "recipes/ex_enigmaticlegacy/" + result.asItem().getRegistryName().getPath()),
                        ConditionalAdvancement.builder()
                                .addCondition(new ItemExistsCondition(result.asItem().getRegistryName()))
                                .addAdvancement(Advancement.Builder.advancement()
                                        .parent(new ResourceLocation("recipes/root"))
                                        .rewards(AdvancementRewards.Builder.recipe(result.asItem().getRegistryName()))
                                        .addCriterion("has_item", trigger)
                                        .addCriterion("has_the_recipe",
                                                RecipeUnlockedTrigger.unlocked(result.asItem().getRegistryName()))
                                        .requirements(RequirementsStrategy.OR)))
                .build(consumer, result.asItem().getRegistryName());
    }

    private void resultExists(Consumer<FinishedRecipe> consumer,
                              ShapedRecipeBuilder recipeBuilder,
                              InventoryChangeTrigger.TriggerInstance trigger,
                              net.minecraft.world.level.ItemLike result) {
        ConditionalRecipe.builder()
                .addCondition(new ItemExistsCondition(result.asItem().getRegistryName()))
                .addRecipe(recipeBuilder::save)
                .setAdvancement(
                        new ResourceLocation(ExEnigmaticlegacyMod.MODID,
                                "recipes/ex_enigmaticlegacy/" + result.asItem().getRegistryName().getPath()),
                        ConditionalAdvancement.builder()
                                .addCondition(new ItemExistsCondition(result.asItem().getRegistryName()))
                                .addAdvancement(Advancement.Builder.advancement()
                                        .parent(new ResourceLocation("recipes/root"))
                                        .rewards(AdvancementRewards.Builder.recipe(result.asItem().getRegistryName()))
                                        .addCriterion("has_item", trigger)
                                        .addCriterion("has_the_recipe",
                                                RecipeUnlockedTrigger.unlocked(result.asItem().getRegistryName()))
                                        .requirements(RequirementsStrategy.OR)))
                .build(consumer, result.asItem().getRegistryName());
    }

    @Override
    public String getName() {
        return "ExEnigmaticLegacy Crafting Recipes";
    }
}