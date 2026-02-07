package net.xiaoyang010.ex_enigmaticlegacy.World.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;
import vazkii.botania.common.block.ModBlocks;

import java.util.HashMap;
import java.util.Map;

public class StructureValidator {
    private static final Map<int[], Block> SPECIFIC_BLOCKS = new HashMap<>();
    private static final Map<int[], TagKey<Block>> TAGGED_BLOCKS = new HashMap<>();

    static {
        SPECIFIC_BLOCKS.put(new int[]{0, 0, 0}, ModBlockss.STARLIT_SANCTUM.get());
        SPECIFIC_BLOCKS.put(new int[]{0, 0, -1}, ModBlockss.PRISMATICRADIANCEBLOCK.get());

        SPECIFIC_BLOCKS.put(new int[]{1, 0, -1}, ModBlockss.BLOCKNATURE.get());
        SPECIFIC_BLOCKS.put(new int[]{-1, 0, -1}, ModBlockss.BLOCKNATURE.get());
        SPECIFIC_BLOCKS.put(new int[]{0, 1, -1}, ModBlockss.BLOCKNATURE.get());
        SPECIFIC_BLOCKS.put(new int[]{0, -1, -1}, ModBlockss.BLOCKNATURE.get());

        SPECIFIC_BLOCKS.put(new int[]{1, -1, 0}, ModBlocks.terrasteelBlock);
        SPECIFIC_BLOCKS.put(new int[]{1, 1, 0}, ModBlocks.terrasteelBlock);
        SPECIFIC_BLOCKS.put(new int[]{-1, -1, 0}, ModBlocks.terrasteelBlock);
        SPECIFIC_BLOCKS.put(new int[]{-1, 1, 0}, ModBlocks.terrasteelBlock);

        TAGGED_BLOCKS.put(new int[]{4, 0, -1}, BlockTags.LEAVES);
        TAGGED_BLOCKS.put(new int[]{-4, 0, -1}, BlockTags.LEAVES);
        TAGGED_BLOCKS.put(new int[]{0, 4, -1}, BlockTags.LEAVES);
        TAGGED_BLOCKS.put(new int[]{0, -4, -1}, BlockTags.LEAVES);

        TAGGED_BLOCKS.put(new int[]{-4, 2, -1}, BlockTags.LEAVES);
        TAGGED_BLOCKS.put(new int[]{-4, 1, -1}, BlockTags.LEAVES);
        TAGGED_BLOCKS.put(new int[]{-4, -1, -1}, BlockTags.LEAVES);
        TAGGED_BLOCKS.put(new int[]{-4, -2, -1}, BlockTags.LEAVES);

        TAGGED_BLOCKS.put(new int[]{-1, 4, -1}, BlockTags.LEAVES);
        TAGGED_BLOCKS.put(new int[]{-2, 4, -1}, BlockTags.LEAVES);
        TAGGED_BLOCKS.put(new int[]{2, 4, -1}, BlockTags.LEAVES);
        TAGGED_BLOCKS.put(new int[]{1, 4, -1}, BlockTags.LEAVES);

        TAGGED_BLOCKS.put(new int[]{4, 2, -1}, BlockTags.LEAVES);
        TAGGED_BLOCKS.put(new int[]{4, 1, -1}, BlockTags.LEAVES);
        TAGGED_BLOCKS.put(new int[]{4, -1, -1}, BlockTags.LEAVES);
        TAGGED_BLOCKS.put(new int[]{4, -2, -1}, BlockTags.LEAVES);

        TAGGED_BLOCKS.put(new int[]{-1, -4, -1}, BlockTags.LEAVES);
        TAGGED_BLOCKS.put(new int[]{-2, -4, -1}, BlockTags.LEAVES);
        TAGGED_BLOCKS.put(new int[]{1, -4, -1}, BlockTags.LEAVES);
        TAGGED_BLOCKS.put(new int[]{2, -4, -1}, BlockTags.LEAVES);

        SPECIFIC_BLOCKS.put(new int[]{4, 4, 0}, ModBlockss.ASTRAL_BLOCK.get());
        SPECIFIC_BLOCKS.put(new int[]{-4, 4, 0}, ModBlockss.ASTRAL_BLOCK.get());
        SPECIFIC_BLOCKS.put(new int[]{-4, -4, 0}, ModBlockss.ASTRAL_BLOCK.get());
        SPECIFIC_BLOCKS.put(new int[]{4, -4, 0}, ModBlockss.ASTRAL_BLOCK.get());

        SPECIFIC_BLOCKS.put(new int[]{4, 3, 0}, ModBlockss.MANA_BOX.get());
        SPECIFIC_BLOCKS.put(new int[]{-4, 3, 0}, ModBlockss.MANA_BOX.get());
        SPECIFIC_BLOCKS.put(new int[]{4, -3, 0}, ModBlockss.MANA_BOX.get());
        SPECIFIC_BLOCKS.put(new int[]{-4, -3, 0}, ModBlockss.MANA_BOX.get());

        SPECIFIC_BLOCKS.put(new int[]{3, 4, 0}, ModBlockss.MANA_BOX.get());
        SPECIFIC_BLOCKS.put(new int[]{-3, 4, 0}, ModBlockss.MANA_BOX.get());
        SPECIFIC_BLOCKS.put(new int[]{-3, -4, 0}, ModBlockss.MANA_BOX.get());
        SPECIFIC_BLOCKS.put(new int[]{3, -4, 0}, ModBlockss.MANA_BOX.get());

        SPECIFIC_BLOCKS.put(new int[]{4, 5, 0}, ModBlockss.MANA_BOX.get());
        SPECIFIC_BLOCKS.put(new int[]{-4, 5, 0}, ModBlockss.MANA_BOX.get());
        SPECIFIC_BLOCKS.put(new int[]{-4, -5, 0}, ModBlockss.MANA_BOX.get());
        SPECIFIC_BLOCKS.put(new int[]{4, -5, 0}, ModBlockss.MANA_BOX.get());

        SPECIFIC_BLOCKS.put(new int[]{5, 4, 0}, ModBlockss.MANA_BOX.get());
        SPECIFIC_BLOCKS.put(new int[]{-5, 4, 0}, ModBlockss.MANA_BOX.get());
        SPECIFIC_BLOCKS.put(new int[]{-5, -4, 0}, ModBlockss.MANA_BOX.get());
        SPECIFIC_BLOCKS.put(new int[]{5, -4, 0}, ModBlockss.MANA_BOX.get());

        SPECIFIC_BLOCKS.put(new int[]{4, 4, 1}, ModBlockss.LEBETHRON_CORE.get());
        SPECIFIC_BLOCKS.put(new int[]{4, 4, 2}, ModBlockss.LEBETHRON_CORE.get());
        SPECIFIC_BLOCKS.put(new int[]{4, 4, 3}, ModBlockss.LEBETHRON_CORE.get());
        SPECIFIC_BLOCKS.put(new int[]{4, 4, 4}, ModBlockss.LEBETHRON_CORE.get());
        SPECIFIC_BLOCKS.put(new int[]{4, 4, 5}, ModBlockss.LEBETHRON_CORE.get());
        SPECIFIC_BLOCKS.put(new int[]{4, 4, 6}, ModBlockss.LEBETHRON_CORE.get());

        SPECIFIC_BLOCKS.put(new int[]{-4, 4, 1}, ModBlockss.LEBETHRON_CORE.get());
        SPECIFIC_BLOCKS.put(new int[]{-4, 4, 2}, ModBlockss.LEBETHRON_CORE.get());
        SPECIFIC_BLOCKS.put(new int[]{-4, 4, 3}, ModBlockss.LEBETHRON_CORE.get());
        SPECIFIC_BLOCKS.put(new int[]{-4, 4, 4}, ModBlockss.LEBETHRON_CORE.get());
        SPECIFIC_BLOCKS.put(new int[]{-4, 4, 5}, ModBlockss.LEBETHRON_CORE.get());
        SPECIFIC_BLOCKS.put(new int[]{-4, 4, 6}, ModBlockss.LEBETHRON_CORE.get());

        SPECIFIC_BLOCKS.put(new int[]{-4, -4, 1}, ModBlockss.LEBETHRON_CORE.get());
        SPECIFIC_BLOCKS.put(new int[]{-4, -4, 2}, ModBlockss.LEBETHRON_CORE.get());
        SPECIFIC_BLOCKS.put(new int[]{-4, -4, 3}, ModBlockss.LEBETHRON_CORE.get());
        SPECIFIC_BLOCKS.put(new int[]{-4, -4, 4}, ModBlockss.LEBETHRON_CORE.get());
        SPECIFIC_BLOCKS.put(new int[]{-4, -4, 5}, ModBlockss.LEBETHRON_CORE.get());
        SPECIFIC_BLOCKS.put(new int[]{-4, -4, 6}, ModBlockss.LEBETHRON_CORE.get());

        SPECIFIC_BLOCKS.put(new int[]{4, -4, 1}, ModBlockss.LEBETHRON_CORE.get());
        SPECIFIC_BLOCKS.put(new int[]{4, -4, 2}, ModBlockss.LEBETHRON_CORE.get());
        SPECIFIC_BLOCKS.put(new int[]{4, -4, 3}, ModBlockss.LEBETHRON_CORE.get());
        SPECIFIC_BLOCKS.put(new int[]{4, -4, 4}, ModBlockss.LEBETHRON_CORE.get());
        SPECIFIC_BLOCKS.put(new int[]{4, -4, 5}, ModBlockss.LEBETHRON_CORE.get());
        SPECIFIC_BLOCKS.put(new int[]{4, -4, 6}, ModBlockss.LEBETHRON_CORE.get());

        SPECIFIC_BLOCKS.put(new int[]{4, 4, 7}, ModBlocks.naturaPylon);
        SPECIFIC_BLOCKS.put(new int[]{-4, 4, 7}, ModBlocks.naturaPylon);
        SPECIFIC_BLOCKS.put(new int[]{-4, -4, 7}, ModBlocks.naturaPylon);
        SPECIFIC_BLOCKS.put(new int[]{4, -4, 7}, ModBlocks.naturaPylon);
    }

    public static boolean validateStructure(Level level, BlockPos centerPos) {
        for (Map.Entry<int[], Block> entry : SPECIFIC_BLOCKS.entrySet()) {
            int[] offset = entry.getKey();
            Block expectedBlock = entry.getValue();
            BlockPos checkPos = centerPos.offset(offset[0], offset[1], offset[2]);

            if (!level.getBlockState(checkPos).is(expectedBlock)) {
                return false;
            }
        }

        for (Map.Entry<int[], TagKey<Block>> entry : TAGGED_BLOCKS.entrySet()) {
            int[] offset = entry.getKey();
            TagKey<Block> expectedTag = entry.getValue();
            BlockPos checkPos = centerPos.offset(offset[0], offset[1], offset[2]);

            if (!level.getBlockState(checkPos).is(expectedTag)) {
                return false;
            }
        }

        return true;
    }

    public static Map<int[], Block> getSpecificBlocks() {
        return new HashMap<>(SPECIFIC_BLOCKS);
    }

    public static Map<int[], TagKey<Block>> getTaggedBlocks() {
        return new HashMap<>(TAGGED_BLOCKS);
    }

    public static int getTotalBlockCount() {
        return SPECIFIC_BLOCKS.size() + TAGGED_BLOCKS.size();
    }
}