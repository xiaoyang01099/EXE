package net.xiaoyang010.ex_enigmaticlegacy.Compat.Projecte.Factory;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.TileMagicTable;

public class MagicTableTileFactory {
    public static void register() {
        ModBlockEntities.MAGIC_TABLE_TILE = ModBlockEntities.REGISTRY.register(
                "magic_table_tile",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new TileMagicTable(
                                ModBlockEntities.MAGIC_TABLE_TILE.get(), pos, state),
                        ModBlockss.MAGIC_TABLE.get()
                ).build(null));
    }
}