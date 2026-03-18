package net.xiaoyang010.ex_enigmaticlegacy.Compat.Projecte;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.emc.EMCMappingHandler;
import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.network.packets.to_client.SyncEmcPKT;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EMCWandHandler {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    static final Map<String, Long> customEmcValues = new ConcurrentHashMap<>();
    private static final Map<String, Long> originalEmcBackup = new ConcurrentHashMap<>();
    private static Field emcField = null;
    private static Method fireRemapMethod = null;
    private static Method syncMethod = null;

    static {
        try {
            emcField = EMCMappingHandler.class.getDeclaredField("emc");
            emcField.setAccessible(true);
            LOGGER.info("[EMCWand] Reflected EMCMappingHandler.emc successfully");
        } catch (Exception e) {
            LOGGER.error("[EMCWand] Failed to reflect EMCMappingHandler.emc", e);
        }

        try {
            fireRemapMethod = EMCMappingHandler.class.getDeclaredMethod("fireEmcRemapEvent");
            fireRemapMethod.setAccessible(true);
            LOGGER.info("[EMCWand] Reflected fireEmcRemapEvent successfully");
        } catch (Exception e) {
            LOGGER.error("[EMCWand] Failed to reflect fireEmcRemapEvent", e);
        }

        try {
            for (Method m : PacketHandler.class.getDeclaredMethods()) {
                if (m.getName().equals("sendToAll") && m.getParameterCount() == 1) {
                    syncMethod = m;
                    syncMethod.setAccessible(true);
                    break;
                }
            }
            if (syncMethod != null) {
                LOGGER.info("[EMCWand] Reflected PacketHandler.sendToAll successfully");
            }
        } catch (Exception e) {
            LOGGER.warn("[EMCWand] Failed to reflect PacketHandler.sendToAll", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<ItemInfo, Long> getEmcMap() {
        try {
            if (emcField != null) {
                return (Map<ItemInfo, Long>) emcField.get(null);
            }
        } catch (Exception e) {
            LOGGER.error("[EMCWand] Failed to get emc map", e);
        }
        return null;
    }

    private static void fireRemapAndSync() {
        try {
            if (fireRemapMethod != null) {
                fireRemapMethod.invoke(null);
                LOGGER.info("[EMCWand] Fired EMC remap event");
            }
        } catch (Exception e) {
            LOGGER.error("[EMCWand] Failed to fire remap event", e);
        }

        try {
            SyncEmcPKT.EmcPKTInfo[] data = EMCMappingHandler.createPacketData();
            SyncEmcPKT pkt = new SyncEmcPKT(data);

            if (syncMethod != null) {
                syncMethod.invoke(null, pkt);
                LOGGER.info("[EMCWand] Synced EMC to all clients ({} entries)", data.length);
            } else {
                MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                if (server != null) {
                    for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                        PacketHandler.sendTo(pkt, player);
                    }
                    LOGGER.info("[EMCWand] Synced EMC to all clients (fallback)");
                }
            }
        } catch (Exception e) {
            LOGGER.error("[EMCWand] Failed to sync EMC to clients", e);
        }
    }

    private static Path getDataDir() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            return server.getWorldPath(
                            net.minecraft.world.level.storage.LevelResource.ROOT)
                    .resolve("ex_enigmaticlegacy");
        }
        return Path.of("ex_enigmaticlegacy");
    }

    private static Path getWandDataFile() {
        return getDataDir().resolve("emc_wand_data.json");
    }

    private static Path getBackupFile() {
        return getDataDir().resolve("emc_wand_backup.json");
    }

    public static boolean hasCustomValues() {
        return !customEmcValues.isEmpty();
    }

    public static void loadData() {
        customEmcValues.clear();
        originalEmcBackup.clear();

        loadJsonMap(getWandDataFile(), customEmcValues);
        loadJsonMap(getBackupFile(), originalEmcBackup);

        LOGGER.info("[EMCWand] Loaded {} custom, {} backup entries",
                customEmcValues.size(), originalEmcBackup.size());
    }

    private static void loadJsonMap(Path file, Map<String, Long> target) {
        if (!Files.exists(file)) return;
        try (Reader r = Files.newBufferedReader(file)) {
            Type type = new TypeToken<Map<String, Long>>() {}.getType();
            Map<String, Long> loaded = GSON.fromJson(r, type);
            if (loaded != null) target.putAll(loaded);
        } catch (Exception e) {
            LOGGER.error("[EMCWand] Failed to load {}", file, e);
        }
    }

    public static void saveData() {
        try {
            Path dir = getDataDir();
            Files.createDirectories(dir);
            try (Writer w = Files.newBufferedWriter(getWandDataFile())) {
                GSON.toJson(customEmcValues, w);
            }
            try (Writer w = Files.newBufferedWriter(getBackupFile())) {
                GSON.toJson(originalEmcBackup, w);
            }
        } catch (Exception e) {
            LOGGER.error("[EMCWand] Failed to save data", e);
        }
    }

    public static void applyCustomValuesAfterRemap() {
        if (customEmcValues.isEmpty()) return;

        Map<ItemInfo, Long> emcMap = getEmcMap();
        if (emcMap == null) return;

        int applied = 0;
        for (Map.Entry<String, Long> entry : customEmcValues.entrySet()) {
            ItemInfo info = toItemInfo(entry.getKey());
            if (info == null) continue;

            if (entry.getValue() > 0) {
                emcMap.put(info, entry.getValue());
            } else {
                emcMap.remove(info);
            }
            applied++;
        }
        LOGGER.info("[EMCWand] Re-applied {} custom EMC values after remap", applied);
    }

    private static ItemInfo toItemInfo(String itemId) {
        ResourceLocation rl = ResourceLocation.tryParse(itemId);
        if (rl == null) return null;
        Item item = ForgeRegistries.ITEMS.getValue(rl);
        if (item == null) return null;
        return ItemInfo.fromItem(item);
    }

    public static long getEmcValue(String itemId) {
        ItemInfo info = toItemInfo(itemId);
        if (info == null) return 0;
        return EMCMappingHandler.getStoredEmcValue(info);
    }

    public static void setEmcValue(String itemId, long value, ServerPlayer player) {
        try {
            ItemInfo info = toItemInfo(itemId);
            if (info == null) {
                sendResult(player, itemId, 0, false);
                return;
            }

            Map<ItemInfo, Long> emcMap = getEmcMap();
            if (emcMap == null) {
                sendResult(player, itemId, 0, false);
                return;
            }

            if (!originalEmcBackup.containsKey(itemId)) {
                long original = EMCMappingHandler.getStoredEmcValue(info);
                originalEmcBackup.put(itemId, original > 0 ? original : -1L);
            }

            if (value > 0) {
                emcMap.put(info, value);
            } else {
                emcMap.remove(info);
            }

            customEmcValues.put(itemId, value);

            saveData();

            fireRemapAndSync();

            LOGGER.info("[EMCWand] {} set EMC {} = {}",
                    player.getName().getString(), itemId, value);

            sendResult(player, itemId, value, true);

        } catch (Exception e) {
            LOGGER.error("[EMCWand] Failed to set EMC for " + itemId, e);
            sendResult(player, itemId, 0, false);
        }
    }

    public static void resetAllEmc(ServerPlayer player) {
        try {
            Map<ItemInfo, Long> emcMap = getEmcMap();
            if (emcMap == null) {
                sendResult(player, "__reset_all__", 0, false);
                return;
            }

            int count = 0;

            Set<String> allModified = new HashSet<>();
            allModified.addAll(customEmcValues.keySet());
            allModified.addAll(originalEmcBackup.keySet());

            for (String itemId : allModified) {
                ItemInfo info = toItemInfo(itemId);
                if (info == null) continue;

                long originalValue = originalEmcBackup.getOrDefault(itemId, -1L);

                if (originalValue > 0) {
                    emcMap.put(info, originalValue);
                } else {
                    emcMap.remove(info);
                }
                count++;
            }

            customEmcValues.clear();
            originalEmcBackup.clear();
            saveData();

            fireRemapAndSync();

            LOGGER.info("[EMCWand] {} reset all EMC ({} items)",
                    player.getName().getString(), count);

            sendResult(player, "__reset_all__", 0, true);

        } catch (Exception e) {
            LOGGER.error("[EMCWand] Failed to reset all EMC", e);
            sendResult(player, "__reset_all__", 0, false);
        }
    }

    public static void restoreModifiedEmc(ServerPlayer player) {
        try {
            Map<ItemInfo, Long> emcMap = getEmcMap();
            if (emcMap == null) {
                sendResult(player, "__restore__", 0, false);
                return;
            }

            int count = 0;

            for (Map.Entry<String, Long> entry : originalEmcBackup.entrySet()) {
                String itemId = entry.getKey();
                long originalValue = entry.getValue();

                ItemInfo info = toItemInfo(itemId);
                if (info == null) continue;

                if (originalValue > 0) {
                    emcMap.put(info, originalValue);
                } else {
                    emcMap.remove(info);
                }

                customEmcValues.remove(itemId);
                count++;
            }

            originalEmcBackup.clear();
            saveData();

            fireRemapAndSync();

            LOGGER.info("[EMCWand] {} restored {} modified EMC values",
                    player.getName().getString(), count);

            sendResult(player, "__restore__", 0, true);

        } catch (Exception e) {
            LOGGER.error("[EMCWand] Failed to restore EMC", e);
            sendResult(player, "__restore__", 0, false);
        }
    }

    private static void sendResult(ServerPlayer player, String itemId, long value, boolean success) {
        NetworkHandler.CHANNEL.send(
                net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                new EMCWandResultPacket(itemId, value, success));
    }

    public static int getModifiedCount() {
        return originalEmcBackup.size();
    }
}