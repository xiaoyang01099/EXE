package net.xiaoyang010.ex_enigmaticlegacy.Compat.Jade;

import mcp.mobius.waila.api.IServerDataProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemEntityDataProvider implements IServerDataProvider<Entity> {

    public static final ItemEntityDataProvider INSTANCE = new ItemEntityDataProvider();

    private static final String TAG_ITEM_DATA = "ExWaveItemData";

    @Override
    public void appendServerData(CompoundTag data, ServerPlayer player, Level world, Entity entity, boolean showDetails) {
        if (!(entity instanceof ItemEntity itemEntity)) return;

        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return;
        if (stack.hasTag()) {
            data.put(TAG_ITEM_DATA, stack.getTag().copy());
        }
    }
}