package net.xiaoyang010.ex_enigmaticlegacy.World.ores;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(
        bus = Mod.EventBusSubscriber.Bus.MOD
)
class IridiumFeatureRuleTest extends RuleTest {
    static final IridiumFeatureRuleTest INSTANCE = new IridiumFeatureRuleTest();
    private static final Codec<IridiumFeatureRuleTest> CODEC = Codec.unit(() -> {
        return INSTANCE;
    });
    private static final RuleTestType<IridiumFeatureRuleTest> CUSTOM_MATCH = () -> {
        return CODEC;
    };
    private List<Block> base_blocks = null;

    IridiumFeatureRuleTest() {
    }

    @SubscribeEvent
    public static void init(FMLCommonSetupEvent event) {
        Registry.register(Registry.RULE_TEST, new ResourceLocation("ex_enigmaticlegacy:iridium_match"), CUSTOM_MATCH);
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
