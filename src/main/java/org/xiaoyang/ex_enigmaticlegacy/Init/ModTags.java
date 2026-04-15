package org.xiaoyang.ex_enigmaticlegacy.Init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.registries.Registries;
import org.xiaoyang.ex_enigmaticlegacy.Exe;


@SuppressWarnings("removal")
public class ModTags {
    public static class Blocks {
        public static final TagKey<Block> HYBRID_SPECIAL_FLOWERS = tag("hybrid_special_flowers");
        public static final TagKey<Block> NEEDS_MIRACLE_TOOL = tag("needs_miracle_tool");
        public static final TagKey<Block> SPECTRITE_CONTAINER = tag("spectrite_container");

        private static TagKey<Block> tag(String name) {
            return BlockTags.create(new ResourceLocation(Exe.MODID, name));
        }
    }

    public static class Items {
        public static final TagKey<Item> DETERMINATION = tag("determination");
        public static final TagKey<Item> HYBRID_SPECIAL_FLOWERS = tag("hybrid_special_flowers");
        public static final TagKey<Item> SPECTRITE_ITEMS = tag("spectrite_items");
        public static final TagKey<Item> SINGULARITY = tag("singularity");
        public static final TagKey<Item> MITHRIL_TOOLS = tag("mithril_tools");

        private static TagKey<Item> tag(String name) {
            return ItemTags.create(new ResourceLocation(Exe.MODID, name));
        }
    }

    public static class BlockEntities {
        public static final TagKey<BlockEntityType<?>> EXAMPLE = tag("example");

        private static TagKey<BlockEntityType<?>> tag(String name) {
            return TagKey.create(Registries.BLOCK_ENTITY_TYPE, new ResourceLocation(Exe.MODID, name));
        }
    }
}