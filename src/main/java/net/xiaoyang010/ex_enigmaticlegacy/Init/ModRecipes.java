package net.xiaoyang010.ex_enigmaticlegacy.Init;


import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Recipe.CelestialTransmuteRecipe;
import net.xiaoyang010.ex_enigmaticlegacy.Recipe.RegenIvyRecipe;


public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, ExEnigmaticlegacyMod.MODID);

    public static final RegistryObject<RecipeSerializer<CelestialTransmuteRecipe>> CELESTIAL_TRANSMUTE_SERIALIZER =
            SERIALIZERS.register("celestial_transmute", () -> CelestialTransmuteRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<RegenIvyRecipe>> REGEN_IVY_SERIALIZER =
            SERIALIZERS.register("crafting_special_regen_ivy",
                    () -> RegenIvyRecipe.SERIALIZER);

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }
}
