package net.xiaoyang010.ex_enigmaticlegacy.Compat.Projecte.Factory;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRecipes;
import net.xiaoyang010.ex_enigmaticlegacy.Recipe.MagicTableRecipe;

public class MagicTableRecipeFactory {
    public static void register() {
        ModRecipes.MAGIC_TABLE_SERIALIZER = ModRecipes.SERIALIZERS.register(
                "magic_table", MagicTableRecipe.Serializer::new);

        ModRecipes.MAGIC_TABLE_TYPE = ModRecipes.RECIPE_TYPES.register(
                "magic_table", () -> new RecipeType<>() {
                    @Override
                    public String toString() {
                        return new ResourceLocation(ExEnigmaticlegacyMod.MODID, "magic_table").toString();
                    }
                });
    }
}