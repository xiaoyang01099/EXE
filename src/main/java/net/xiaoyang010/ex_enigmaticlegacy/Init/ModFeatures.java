package net.xiaoyang010.ex_enigmaticlegacy.Init;

import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.xiaoyang010.ex_enigmaticlegacy.World.ores.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

@Mod.EventBusSubscriber
public class ModFeatures {
    public static final DeferredRegister<Feature<?>> REGISTRY;
    private static final List<ModFeatureRegistration> FEATURE_REGISTRATIONS;
    public static final RegistryObject<Feature<?>> PLATINUM;
    public static final RegistryObject<Feature<?>> IRIDIUM;
    public static final RegistryObject<Feature<?>> NICKEL;
    public static final RegistryObject<Feature<?>> END;
    public static final RegistryObject<Feature<?>> AMETHYST;
    public static final RegistryObject<Feature<?>> BAUXITE;
    public static final RegistryObject<Feature<?>> CHROMITE;
    public static final RegistryObject<Feature<?>> FLUORITE;
    public static final RegistryObject<Feature<?>> GYPSUM;
    public static final RegistryObject<Feature<?>> JADE;
    public static final RegistryObject<Feature<?>> LEAD;
    public static final RegistryObject<Feature<?>> BRONZE;
    public static final RegistryObject<Feature<?>> OPAL;
    public static final RegistryObject<Feature<?>> SPECTRITE;



    public ModFeatures() {
    }

    private static RegistryObject<Feature<?>> register(String registryname, Supplier<Feature<?>> feature, ModFeatureRegistration featureRegistration) {
        FEATURE_REGISTRATIONS.add(featureRegistration);
        return REGISTRY.register(registryname, feature);
    }

    @SubscribeEvent
    public static void addFeaturesToBiomes(BiomeLoadingEvent event) {
        Iterator var1 = FEATURE_REGISTRATIONS.iterator();

        while(true) {
            ModFeatureRegistration registration;
            do {
                if (!var1.hasNext()) {
                    return;
                }

                registration = (ModFeatureRegistration)var1.next();
            } while(registration.biomes() != null && !registration.biomes().contains(event.getName()));

            event.getGeneration().getFeatures(registration.stage()).add((Holder)registration.placedFeature().get());
        }
    }

    static {
        REGISTRY = DeferredRegister.create(ForgeRegistries.FEATURES, "ex_enigmaticlegacy");
        FEATURE_REGISTRATIONS = new ArrayList();
        PLATINUM = register("platinum_ore", PlatinumFeature::feature, new ModFeatureRegistration(GenerationStep.Decoration.UNDERGROUND_ORES, PlatinumFeature.GENERATE_BIOMES, PlatinumFeature::placedFeature));
        IRIDIUM = register("iridium_ore", IridiumFeature::feature, new ModFeatureRegistration(GenerationStep.Decoration.UNDERGROUND_ORES, IridiumFeature.GENERATE_BIOMES, IridiumFeature::placedFeature));
        END = register("end_ore", EndOreFeature::feature, new ModFeatureRegistration(GenerationStep.Decoration.UNDERGROUND_ORES, EndOreFeature.GENERATE_BIOMES, EndOreFeature::placedFeature));
        AMETHYST = register("amethyst_ore", AmethystOreFeature::feature, new ModFeatureRegistration(GenerationStep.Decoration.UNDERGROUND_ORES, AmethystOreFeature.GENERATE_BIOMES, AmethystOreFeature::placedFeature));
        BAUXITE = register("bauxite_ore", BauxiteOreFeature::feature, new ModFeatureRegistration(GenerationStep.Decoration.UNDERGROUND_ORES, BauxiteOreFeature.GENERATE_BIOMES, BauxiteOreFeature::placedFeature));
        NICKEL = register("nickel_ore", NickelFeature::feature, new ModFeatureRegistration(GenerationStep.Decoration.UNDERGROUND_ORES, NickelFeature.GENERATE_BIOMES, NickelFeature::placedFeature));
        CHROMITE = register("chromite_ore", ChromiteOreFeature::feature, new ModFeatureRegistration(GenerationStep.Decoration.UNDERGROUND_ORES, ChromiteOreFeature.GENERATE_BIOMES, ChromiteOreFeature::placedFeature));
        FLUORITE = register("fluorite_ore", FluoriteOreFeature::feature, new ModFeatureRegistration(GenerationStep.Decoration.UNDERGROUND_ORES, FluoriteOreFeature.GENERATE_BIOMES, FluoriteOreFeature::placedFeature));
        GYPSUM = register("gypsum_ore", GypsumOreFeature::feature, new ModFeatureRegistration(GenerationStep.Decoration.UNDERGROUND_ORES, GypsumOreFeature.GENERATE_BIOMES, GypsumOreFeature::placedFeature));
        JADE = register("jade_ore", JadeOreFeature::feature, new ModFeatureRegistration(GenerationStep.Decoration.UNDERGROUND_ORES, JadeOreFeature.GENERATE_BIOMES, JadeOreFeature::placedFeature));
        LEAD = register("lead_ore", LeadOreFeature::feature, new ModFeatureRegistration(GenerationStep.Decoration.UNDERGROUND_ORES, LeadOreFeature.GENERATE_BIOMES, LeadOreFeature::placedFeature));
        BRONZE = register("bronze_ore", BronzeOreFeature::feature, new ModFeatureRegistration(GenerationStep.Decoration.UNDERGROUND_ORES, BronzeOreFeature.GENERATE_BIOMES, BronzeOreFeature::placedFeature));
        OPAL = register("opal_ore", OpalOreFeature::feature, new ModFeatureRegistration(GenerationStep.Decoration.UNDERGROUND_ORES, OpalOreFeature.GENERATE_BIOMES, OpalOreFeature::placedFeature));
        SPECTRITE = register("spectrite_ore", SpectriteOreFeature::feature, new ModFeatureRegistration(GenerationStep.Decoration.UNDERGROUND_ORES, SpectriteOreFeature.GENERATE_BIOMES, SpectriteOreFeature::placedFeature));

    }
}
