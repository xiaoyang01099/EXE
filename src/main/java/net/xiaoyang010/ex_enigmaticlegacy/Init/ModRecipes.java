package net.xiaoyang010.ex_enigmaticlegacy.Init;


import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Recipe.*;
import net.xiaoyang010.ex_enigmaticlegacy.Recipe.CelestialTransmuteRecipe.Type;


public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, ExEnigmaticlegacyMod.MODID);

    public static final RegistryObject<RecipeSerializer<CelestialTransmuteRecipe>> CELESTIAL_TRANSMUTE_SERIALIZER =
            SERIALIZERS.register("celestial_transmute", () -> CelestialTransmuteRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<RainbowTableRecipe>> RAINBOW_TABLE_SERIALIZER =
            SERIALIZERS.register("rainbow_table", RainbowTableRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<PolychromeRecipe>> POLYCHROME_SERIALIZER =
            SERIALIZERS.register("polychrome", PolychromeRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<NidavellirForgeRecipe>> NIDAVELLIR_FORGE_SERIALIZER =
            SERIALIZERS.register("nidavellir_forge", NidavellirForgeRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<AncientAlphirineRecipe>> ANCIENT_ALPHIRINE_SERIALIZER =
            SERIALIZERS.register("ancient_alphirine", AncientAlphirineRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<ManaitaRecipe>> MANAITA =
            SERIALIZERS.register("manaita", () -> new SimpleRecipeSerializer<>(ManaitaRecipe::new));

    public static final RegistryObject<RecipeSerializer<AesirRingRecipe>> AESIR_RING =
            SERIALIZERS.register("aesir_ring", () -> new SimpleRecipeSerializer<>(AesirRingRecipe::new));

    public static final RegistryObject<RecipeSerializer<StarlitSanctumRecipe>> STARLIT_SERIALIZER =
            SERIALIZERS.register("starlit_crafting", StarlitSanctumRecipe.Serializer::new);

    public static final RecipeType<AncientAlphirineRecipe> ANCIENT_ALPHIRINE_TYPE = new AncientAlphirineRecipe.Type();
    public static final RecipeType<PolychromeRecipe> POLYCHROME_TYPE = new PolychromeRecipe.Type();
    public static final RecipeType<CelestialTransmuteRecipe> CHT_TYPE = new Type();
    public static final RecipeType<RainbowTableRecipe> RAINBOW_TABLE_TYPE = new RainbowTableRecipe.Type();
    public static final RecipeType<NidavellirForgeRecipe> NIDAVELLIR_FORGE_TYPE = new NidavellirForgeRecipe.Type();
    public static final RecipeType<StarlitSanctumRecipe> STARLIT_TYPE = new StarlitSanctumRecipe.Type();

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
        Registry.register(Registry.RECIPE_TYPE, NidavellirForgeRecipe.TYPE_ID, NIDAVELLIR_FORGE_TYPE);
        Registry.register(Registry.RECIPE_TYPE, PolychromeRecipe.TYPE_ID, POLYCHROME_TYPE);
        Registry.register(Registry.RECIPE_TYPE, RainbowTableRecipe.TYPE_ID, RAINBOW_TABLE_TYPE);
        Registry.register(Registry.RECIPE_TYPE, CelestialTransmuteRecipe.TYPE_ID, CHT_TYPE);
        Registry.register(Registry.RECIPE_TYPE, AncientAlphirineRecipe.TYPE_ID, ANCIENT_ALPHIRINE_TYPE);
        Registry.register(Registry.RECIPE_TYPE, new ResourceLocation(ExEnigmaticlegacyMod.MODID, "starlit_crafting"), STARLIT_TYPE);
    }
}
