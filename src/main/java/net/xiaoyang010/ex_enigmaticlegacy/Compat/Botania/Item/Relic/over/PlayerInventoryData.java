package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerInventoryData extends SavedData {
    private static final String DATA_NAME = "ex_enigmaticlegacy_inventory_data";
    private static final Map<UUID, InventoryOverpowered> INVENTORIES = new HashMap<>();
    private static final Map<UUID, InventoryOverpowered> SERVER_INVENTORIES = new HashMap<>();
    private static final String TAG_PLAYERS = "Players";
    private static final String TAG_UUID = "UUID";
    private static final String TAG_INVENTORY = "Inventory";
    private static final Map<UUID, InventoryOverpowered> CLIENT_CACHE = new HashMap<>();

    public static PlayerInventoryData load(CompoundTag tag) {
        PlayerInventoryData data = new PlayerInventoryData();

        if (!tag.contains(TAG_PLAYERS)) {
            return data;
        }

        ListTag playerList = tag.getList(TAG_PLAYERS, Tag.TAG_COMPOUND);

        for (int i = 0; i < playerList.size(); i++) {
            CompoundTag playerTag = playerList.getCompound(i);

            try {
                UUID uuid = playerTag.getUUID(TAG_UUID);
                CompoundTag inventoryTag = playerTag.getCompound(TAG_INVENTORY);

                InventoryOverpowered inventory = new InventoryOverpowered(null);
                inventory.readFromNBT(inventoryTag);

                INVENTORIES.put(uuid, inventory);
            } catch (Exception e) {
                ExEnigmaticlegacyMod.LOGGER.error("Failed to load player inventory", e);
            }
        }

        ExEnigmaticlegacyMod.LOGGER.info("Loaded {} player inventories", INVENTORIES.size());
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag playerList = new ListTag();

        INVENTORIES.forEach((uuid, inventory) -> {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID(TAG_UUID, uuid);

            CompoundTag inventoryTag = new CompoundTag();
            inventory.writeToNBT(inventoryTag);
            playerTag.put(TAG_INVENTORY, inventoryTag);

            playerList.add(playerTag);
        });

        tag.put(TAG_PLAYERS, playerList);
        ExEnigmaticlegacyMod.LOGGER.debug("Saved {} player inventories", INVENTORIES.size());
        return tag;
    }

    public static PlayerInventoryData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                PlayerInventoryData::load,
                PlayerInventoryData::new,
                DATA_NAME
        );
    }

    public static InventoryOverpowered getInventory(Player player) {
        UUID uuid = player.getUUID();

        if (player.level.isClientSide) {
            return CLIENT_CACHE.computeIfAbsent(uuid, k -> {
                InventoryOverpowered inv = new InventoryOverpowered(player);
                return inv;
            });
        }

        InventoryOverpowered inventory = SERVER_INVENTORIES.get(uuid);
        if (inventory == null) {
            inventory = new InventoryOverpowered(player);
            SERVER_INVENTORIES.put(uuid, inventory);

            if (player.level instanceof ServerLevel serverLevel) {
                get(serverLevel).setDirty();
            }
        } else {
            inventory.player = player;
        }

        return inventory;
    }

    public static void saveInventory(Player player, InventoryOverpowered inventory) {
        INVENTORIES.put(player.getUUID(), inventory);

        if (!player.level.isClientSide && player.level instanceof ServerLevel serverLevel) {
            get(serverLevel).setDirty();
        }
    }

    public static void clearInventory(Player player) {
        InventoryOverpowered inventory = new InventoryOverpowered(player);
        INVENTORIES.put(player.getUUID(), inventory);

        if (!player.level.isClientSide && player.level instanceof ServerLevel serverLevel) {
            get(serverLevel).setDirty();
        }
    }

    public static void saveAll(ServerLevel level) {
        PlayerInventoryData data = get(level);
        data.setDirty();
        ExEnigmaticlegacyMod.LOGGER.info("Force saved all inventories");
    }
}