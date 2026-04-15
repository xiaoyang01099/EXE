package org.xiaoyang.ex_enigmaticlegacy.Font.Jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IServerDataProvider;

public class ItemEntityDataProvider implements IServerDataProvider<EntityAccessor> {
    public static final ItemEntityDataProvider INSTANCE = new ItemEntityDataProvider();
    private static final String TAG_ITEM_DATA = "ExWaveItemData";
    private static final ResourceLocation UID = new ResourceLocation("ex_enigmaticlegacy", "item_entity_data");

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public void appendServerData(CompoundTag data, EntityAccessor accessor) {
        if (!(accessor.getEntity() instanceof ItemEntity itemEntity)) return;

        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return;
        if (stack.hasTag()) {
            data.put(TAG_ITEM_DATA, stack.getTag().copy());
        }
    }
}