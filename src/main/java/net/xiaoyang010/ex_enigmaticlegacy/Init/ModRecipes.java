package net.xiaoyang010.ex_enigmaticlegacy.Init;


import net.minecraft.core.Registry;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Recipe.CelestialTransmuteRecipe;
import net.xiaoyang010.ex_enigmaticlegacy.Recipe.CelestialTransmuteRecipe.Type;


public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, ExEnigmaticlegacyMod.MODID);

    public static final RegistryObject<RecipeSerializer<CelestialTransmuteRecipe>> CELESTIAL_TRANSMUTE_SERIALIZER =
            SERIALIZERS.register("celestial_transmute", () -> CelestialTransmuteRecipe.Serializer.INSTANCE);

    public static final RecipeType<CelestialTransmuteRecipe> CHT_TYPE = new Type();

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);

        Registry.register(Registry.RECIPE_TYPE, CelestialTransmuteRecipe.TYPE_ID, CHT_TYPE);
    }
}
