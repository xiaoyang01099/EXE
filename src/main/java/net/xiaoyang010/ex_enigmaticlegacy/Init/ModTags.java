package net.xiaoyang010.ex_enigmaticlegacy.Init;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;

public class ModTags {
    public static class Blocks {
        public static final TagKey<Block> HYBRID_SPECIAL_FLOWERS = tag("hybrid_special_flowers");

        public static final TagKey<Block> NEEDS_MIRACLE_TOOL = tag("needs_miracle_tool");

        public static final TagKey<Block> SPECTRITE_CONTAINER = tag("spectrite_container");

        private static TagKey<Block> tag(String name) {
            return BlockTags.create(new ResourceLocation(ExEnigmaticlegacyMod.MODID, name));
        }

        private static TagKey<Block> forgeTag(String name) {
            return BlockTags.create(new ResourceLocation("forge", name));
        }
    }

    public static class Items {
        public static final TagKey<Item> DETERMINATION = tag("determination");

        public static final TagKey<Item> HYBRID_SPECIAL_FLOWERS = tag("hybrid_special_flowers");

        public static final TagKey<Item> SPECTRITE_ITEMS = tag("spectrite_items");

        public static final TagKey<Item> SINGULARITY = tag("singularity");

        public static final TagKey<Item> MITHRIL_TOOLS = TagKey.create(Registry.ITEM_REGISTRY,
                new ResourceLocation("ex_enigmaticlegacy", "mithril_tool"));

        private static TagKey<Item> tag(String name) {
            return ItemTags.create(new ResourceLocation(ExEnigmaticlegacyMod.MODID, name));
        }

        private static TagKey<Item> forgeTag(String name) {
            return ItemTags.create(new ResourceLocation("forge", name));
        }
    }

    public static class BlockEntities {

        private static TagKey<BlockEntityType<?>> tag(String name) {
            return TagKey.create(Registry.BLOCK_ENTITY_TYPE_REGISTRY, new ResourceLocation(ExEnigmaticlegacyMod.MODID, name));
        }

        private static TagKey<BlockEntityType<?>> forgeTag(String name) {
            return TagKey.create(Registry.BLOCK_ENTITY_TYPE_REGISTRY, new ResourceLocation("forge", name));
        }
    }
}