package net.xiaoyang010.ex_enigmaticlegacy.World.ores;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;

import java.util.List;
import java.util.Random;
import java.util.Set;

public class EndOreFeature extends OreFeature {
    public static EndOreFeature FEATURE = null;
    public static Holder<ConfiguredFeature<OreConfiguration, ?>> CONFIGURED_FEATURE = null;
    public static Holder<PlacedFeature> PLACED_FEATURE = null;
    public static final Set<ResourceLocation> GENERATE_BIOMES = null;
    private final Set<ResourceKey<Level>> generate_dimensions;

    public static Feature<?> feature() {
        FEATURE = new EndOreFeature();
        CONFIGURED_FEATURE = FeatureUtils.register("ex_enigmaticlegacy:end_ore", FEATURE, new OreConfiguration(
                EndOreFeatureRuleTest.INSTANCE, ((Block) ModBlockss.END_ORE.get()).defaultBlockState(), 6));
        PLACED_FEATURE = PlacementUtils.register("ex_enigmaticlegacy:end_ore", CONFIGURED_FEATURE, List.of(
                CountPlacement.of(3),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(80)),
                BiomeFilter.biome()));
        return FEATURE;
    }

    public static Holder<PlacedFeature> placedFeature() {
        return PLACED_FEATURE;
    }

    public EndOreFeature() {
        super(OreConfiguration.CODEC);
        this.generate_dimensions = Set.of(Level.END);
    }

    public boolean place(FeaturePlaceContext<OreConfiguration> context) {
        WorldGenLevel world = context.level();
        return this.generate_dimensions.contains(world.getLevel().dimension()) && super.place(context);
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class EndOreFeatureRuleTest extends RuleTest {
        static final EndOreFeatureRuleTest INSTANCE = new EndOreFeatureRuleTest();
        private static final Codec<EndOreFeatureRuleTest> CODEC = Codec.unit(() -> INSTANCE);
        private static final RuleTestType<EndOreFeatureRuleTest> CUSTOM_MATCH = () -> CODEC;
        private List<Block> base_blocks = null;

        private EndOreFeatureRuleTest() {}

        @SubscribeEvent
        public static void init(FMLCommonSetupEvent event) {
            Registry.register(Registry.RULE_TEST, new ResourceLocation("ex_enigmaticlegacy:end_ore_match"), CUSTOM_MATCH);
        }

        @Override
        public boolean test(BlockState blockAt, Random random) {
            if (this.base_blocks == null) {
                this.base_blocks = List.of(Blocks.END_STONE);
            }
            return this.base_blocks.contains(blockAt.getBlock());
        }

        @Override
        protected RuleTestType<?> getType() {
            return CUSTOM_MATCH;
        }
    }
}
