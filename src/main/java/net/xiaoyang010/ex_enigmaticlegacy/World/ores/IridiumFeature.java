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
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlocks;
import net.xiaoyang010.ex_enigmaticlegacy.World.teleporter.MinersHeavenOldTeleporter;

import java.util.List;
import java.util.Random;
import java.util.Set;

public class IridiumFeature extends OreFeature {
    public static IridiumFeature FEATURE = null;
    public static Holder<ConfiguredFeature<OreConfiguration, ?>> CONFIGURED_FEATURE = null;
    public static Holder<PlacedFeature> PLACED_FEATURE = null;
    public static final Set<ResourceLocation> GENERATE_BIOMES = null;
    private final Set<ResourceKey<Level>> generate_dimensions;

    public static Feature<?> feature() {
        FEATURE = new IridiumFeature();
        CONFIGURED_FEATURE = FeatureUtils.register("ex_enigmaticlegacy:iridium_ore", FEATURE, new OreConfiguration(IridiumFeature.IridiumFeatureRuleTest.INSTANCE, ((Block) ModBlocks.IRIDIUM_ORE.get()).defaultBlockState(), 6));
        PLACED_FEATURE = PlacementUtils.register("ex_enigmaticlegacy:iridium_ore", CONFIGURED_FEATURE, List.of(CountPlacement.of(3), InSquarePlacement.spread(), HeightRangePlacement.uniform(VerticalAnchor.absolute(-30), VerticalAnchor.absolute(60)), BiomeFilter.biome()));
        return FEATURE;
    }

    public static Holder<PlacedFeature> placedFeature() {
        return PLACED_FEATURE;
    }

    public IridiumFeature() {
        super(OreConfiguration.CODEC);
        // 在这里添加你想要生成的维度
        this.generate_dimensions = Set.of(
                Level.OVERWORLD,
                ResourceKey.create(Registry.DIMENSION_REGISTRY,
                        new ResourceLocation("ex_enigmaticlegacy:miners_heaven_old"))
        );
    }

    public boolean place(FeaturePlaceContext<OreConfiguration> context) {
        WorldGenLevel world = context.level();
        return !this.generate_dimensions.contains(world.getLevel().dimension()) ? false : super.place(context);
    }

    @Mod.EventBusSubscriber(
            bus = Mod.EventBusSubscriber.Bus.MOD
    )
    class IridiumFeatureRuleTest extends RuleTest {
        static final net.xiaoyang010.ex_enigmaticlegacy.World.ores.IridiumFeatureRuleTest INSTANCE = new net.xiaoyang010.ex_enigmaticlegacy.World.ores.IridiumFeatureRuleTest();
        private static final Codec<net.xiaoyang010.ex_enigmaticlegacy.World.ores.IridiumFeatureRuleTest> CODEC = Codec.unit(() -> {
            return INSTANCE;
        });
        private static final RuleTestType<net.xiaoyang010.ex_enigmaticlegacy.World.ores.IridiumFeatureRuleTest> CUSTOM_MATCH = () -> {
            return CODEC;
        };
        private List<Block> base_blocks = null;

        private IridiumFeatureRuleTest() {
        }

        @SubscribeEvent
        public static void init(FMLCommonSetupEvent event) {
            Registry.register(Registry.RULE_TEST, new ResourceLocation("ex_enigmaticlegacy:iridium_ore_match"), CUSTOM_MATCH);
        }

        public boolean test(BlockState blockAt, Random random) {
            if (this.base_blocks == null) {
                this.base_blocks = List.of(Blocks.STONE, Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE);
            }

            return this.base_blocks.contains(blockAt.getBlock());
        }

        protected RuleTestType<?> getType() {
            return CUSTOM_MATCH;
        }
    }
}
