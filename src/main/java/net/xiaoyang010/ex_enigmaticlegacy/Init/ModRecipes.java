package net.xiaoyang010.ex_enigmaticlegacy.Init;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Projecte.Factory.MagicTableRecipeFactory;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Recipe.*;
import net.xiaoyang010.ex_enigmaticlegacy.Recipe.MagicTableRecipe;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, ExEnigmaticlegacyMod.MODID);

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registry.RECIPE_TYPE_REGISTRY, ExEnigmaticlegacyMod.MODID);

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

    public static RegistryObject<RecipeSerializer<MagicTableRecipe>> MAGIC_TABLE_SERIALIZER = null;

    public static final RegistryObject<RecipeType<NidavellirForgeRecipe>> NIDAVELLIR_FORGE_TYPE =
            RECIPE_TYPES.register("nidavellir_forge", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return new ResourceLocation(ExEnigmaticlegacyMod.MODID, "nidavellir_forge").toString();
                }
            });

    public static final RegistryObject<RecipeType<PolychromeRecipe>> POLYCHROME_TYPE =
            RECIPE_TYPES.register("polychrome", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return new ResourceLocation(ExEnigmaticlegacyMod.MODID, "polychrome").toString();
                }
            });

    public static final RegistryObject<RecipeType<RainbowTableRecipe>> RAINBOW_TABLE_TYPE =
            RECIPE_TYPES.register("rainbow_table", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return new ResourceLocation(ExEnigmaticlegacyMod.MODID, "rainbow_table").toString();
                }
            });

    public static final RegistryObject<RecipeType<CelestialTransmuteRecipe>> CHT_TYPE =
            RECIPE_TYPES.register("celestial_transmute", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return new ResourceLocation(ExEnigmaticlegacyMod.MODID, "celestial_transmute").toString();
                }
            });

    public static final RegistryObject<RecipeType<AncientAlphirineRecipe>> ANCIENT_ALPHIRINE_TYPE =
            RECIPE_TYPES.register("ancient_alphirine", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return new ResourceLocation(ExEnigmaticlegacyMod.MODID, "ancient_alphirine").toString();
                }
            });

    public static final RegistryObject<RecipeType<StarlitSanctumRecipe>> STARLIT_TYPE =
            RECIPE_TYPES.register("starlit_crafting", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return new ResourceLocation(ExEnigmaticlegacyMod.MODID, "starlit_crafting").toString();
                }
            });

    public static RegistryObject<RecipeType<MagicTableRecipe>> MAGIC_TABLE_TYPE = null;

    public static void register(IEventBus eventBus) {
        if (ModList.get().isLoaded("projecte")) {
            MagicTableRecipeFactory.register();
        }

        RECIPE_TYPES.register(eventBus);
        SERIALIZERS.register(eventBus);
    }
}