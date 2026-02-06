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

public class PlayerUnlockData extends SavedData {
    private static final String DATA_NAME = "ex_enigmaticlegacy_unlocks";
    static final Map<UUID, UnlockInfo> CLIENT_CACHE = new HashMap<>();

    static class UnlockInfo {
        boolean ePearlUnlocked = false;
        boolean eChestUnlocked = false;
        int storageCount = 0;

        CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("EPearl", ePearlUnlocked);
            tag.putBoolean("EChest", eChestUnlocked);
            tag.putInt("Storage", storageCount);
            return tag;
        }

        static UnlockInfo load(CompoundTag tag) {
            UnlockInfo info = new UnlockInfo();
            info.ePearlUnlocked = tag.getBoolean("EPearl");
            info.eChestUnlocked = tag.getBoolean("EChest");
            info.storageCount = tag.getInt("Storage");
            return info;
        }

        UnlockInfo copy() {
            UnlockInfo copy = new UnlockInfo();
            copy.ePearlUnlocked = this.ePearlUnlocked;
            copy.eChestUnlocked = this.eChestUnlocked;
            copy.storageCount = this.storageCount;
            return copy;
        }
    }

    private final Map<UUID, UnlockInfo> unlocks = new HashMap<>();

    public static PlayerUnlockData load(CompoundTag tag) {
        PlayerUnlockData data = new PlayerUnlockData();

        if (!tag.contains("PlayerUnlocks")) {
            return data;
        }

        ListTag playerList = tag.getList("PlayerUnlocks", Tag.TAG_COMPOUND);

        for (int i = 0; i < playerList.size(); i++) {
            CompoundTag playerTag = playerList.getCompound(i);

            try {
                UUID uuid = playerTag.getUUID("UUID");
                UnlockInfo info = UnlockInfo.load(playerTag.getCompound("Data"));
                data.unlocks.put(uuid, info);
            } catch (Exception e) {
                ExEnigmaticlegacyMod.LOGGER.error("Failed to load unlock data", e);
            }
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag playerList = new ListTag();

        unlocks.forEach((uuid, info) -> {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("UUID", uuid);
            playerTag.put("Data", info.save());
            playerList.add(playerTag);
        });

        tag.put("PlayerUnlocks", playerList);
        return tag;
    }

    public static PlayerUnlockData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                PlayerUnlockData::load,
                PlayerUnlockData::new,
                DATA_NAME
        );
    }

    private static UnlockInfo getUnlockInfo(Player player) {
        UUID uuid = player.getUUID();

        if (player.level.isClientSide) {
            return CLIENT_CACHE.computeIfAbsent(uuid, k -> new UnlockInfo());
        }

        ServerLevel level = (ServerLevel) player.level;
        PlayerUnlockData data = get(level);
        return data.unlocks.computeIfAbsent(uuid, k -> new UnlockInfo());
    }

    public static void updateClientCache(Player player, boolean pearl, boolean chest, int storage) {
        if (player.level.isClientSide) {
            UnlockInfo info = CLIENT_CACHE.computeIfAbsent(
                    player.getUUID(), k -> new UnlockInfo());
            info.ePearlUnlocked = pearl;
            info.eChestUnlocked = chest;
            info.storageCount = storage;
        }
    }

    public static boolean isEPearlUnlocked(Player player) {
        return getUnlockInfo(player).ePearlUnlocked;
    }

    public static void setEPearlUnlocked(Player player, boolean unlocked) {
        UnlockInfo info = getUnlockInfo(player);
        info.ePearlUnlocked = unlocked;

        if (!player.level.isClientSide) {
            ServerLevel level = (ServerLevel) player.level;
            get(level).setDirty();
        }
    }

    public static boolean isEChestUnlocked(Player player) {
        return getUnlockInfo(player).eChestUnlocked;
    }

    public static void setEChestUnlocked(Player player, boolean unlocked) {
        UnlockInfo info = getUnlockInfo(player);
        info.eChestUnlocked = unlocked;

        if (!player.level.isClientSide) {
            ServerLevel level = (ServerLevel) player.level;
            get(level).setDirty();
        }
    }

    public static int getStorageCount(Player player) {
        return getUnlockInfo(player).storageCount;
    }

    public static void setStorageCount(Player player, int count) {
        UnlockInfo info = getUnlockInfo(player);
        info.storageCount = count;

        if (!player.level.isClientSide) {
            ServerLevel level = (ServerLevel) player.level;
            get(level).setDirty();
        }
    }

    public static boolean hasStorage(Player player, int section) {
        return getStorageCount(player) >= section;
    }
}