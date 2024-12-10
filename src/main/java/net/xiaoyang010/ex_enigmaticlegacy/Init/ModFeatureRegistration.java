package net.xiaoyang010.ex_enigmaticlegacy.Init;

import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

record ModFeatureRegistration(GenerationStep.Decoration stage, Set<ResourceLocation> biomes, Supplier<Holder<PlacedFeature>> placedFeature) {
    ModFeatureRegistration(GenerationStep.Decoration stage, Set<ResourceLocation> biomes, Supplier<Holder<PlacedFeature>> placedFeature) {
        this.stage = stage;
        this.biomes = biomes;
        this.placedFeature = placedFeature;
    }

    public GenerationStep.Decoration stage() {
        return this.stage;
    }

    public Set<ResourceLocation> biomes() {
        return this.biomes;
    }

    public Supplier<Holder<PlacedFeature>> placedFeature() {
        return this.placedFeature;
    }
}
