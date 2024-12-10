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
import net.xiaoyang010.ex_enigmaticlegacy.World.ores.EndOreFeature;
import net.xiaoyang010.ex_enigmaticlegacy.World.ores.IridiumFeature;
import net.xiaoyang010.ex_enigmaticlegacy.World.ores.NickelFeature;
import net.xiaoyang010.ex_enigmaticlegacy.World.ores.PlatinumFeature;

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
        NICKEL = register("nickel_ore", NickelFeature::feature, new ModFeatureRegistration(GenerationStep.Decoration.UNDERGROUND_ORES, NickelFeature.GENERATE_BIOMES, NickelFeature::placedFeature));
    }
}
