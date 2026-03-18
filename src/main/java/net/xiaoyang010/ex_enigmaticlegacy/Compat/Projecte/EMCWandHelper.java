package net.xiaoyang010.ex_enigmaticlegacy.Compat.Projecte;

import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.event.EMCRemapEvent;
import moze_intel.projecte.api.proxy.IEMCProxy;
import moze_intel.projecte.emc.EMCMappingHandler;
import moze_intel.projecte.network.PacketHandler;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.*;

public class EMCWandHelper {
    private static final Logger LOGGER = LogManager.getLogger("EMCWand");
    private static Map<ItemInfo, Long> emcMapRef = null;
    private static final Set<UUID> authorizedPlayers = new HashSet<>();

    @SuppressWarnings("unchecked")
    private static Map<ItemInfo, Long> getEmcMap() {
        if (emcMapRef == null) {
            try {
                Field emcField = EMCMappingHandler.class.getDeclaredField("emc");
                emcField.setAccessible(true);
                emcMapRef = (Map<ItemInfo, Long>) emcField.get(null);
            } catch (Exception e) {
                LOGGER.error("Failed to access EMCMappingHandler.emc field", e);
                return Collections.emptyMap();
            }
        }
        return emcMapRef;
    }

    public static long getEmcValue(Item item) {
        IEMCProxy proxy = ProjectEAPI.getEMCProxy();
        if (proxy == null) return 0;
        return proxy.getValue(item);
    }

    public static long getEmcValue(ItemStack stack) {
        IEMCProxy proxy = ProjectEAPI.getEMCProxy();
        if (proxy == null) return 0;
        return proxy.getValue(stack);
    }

    public static boolean hasEmcValue(Item item) {
        return getEmcValue(item) > 0;
    }

    public static boolean setEmcValue(Item item, long value) {
        if (value < 0) return false;
        Map<ItemInfo, Long> emcMap = getEmcMap();
        if (emcMap == null || emcMap.isEmpty() && emcMapRef == null) return false;

        ItemInfo info = ItemInfo.fromItem(item);

        if (value == 0) {
            emcMap.remove(info);
        } else {
            emcMap.put(info, value);
        }

        try {
            PacketHandler.sendFragmentedEmcPacketToAll();
        } catch (Exception e) {
            LOGGER.error("Failed to sync EMC to clients", e);
        }

        MinecraftForge.EVENT_BUS.post(new EMCRemapEvent());

        LOGGER.info("Set EMC for {} to {}", ForgeRegistries.ITEMS.getKey(item), value);
        return true;
    }

    public static boolean removeEmcValue(Item item) {
        return setEmcValue(item, 0);
    }

    public static List<Item> getItemsWithoutEmc() {
        List<Item> result = new ArrayList<>();
        IEMCProxy proxy = ProjectEAPI.getEMCProxy();
        if (proxy == null) return result;

        for (Item item : ForgeRegistries.ITEMS) {
            if (item.getDefaultInstance().isEmpty()) continue;
            if (!proxy.hasValue(item)) {
                result.add(item);
            }
        }
        return result;
    }

    public static Map<Item, Long> getItemsWithEmc() {
        Map<Item, Long> result = new LinkedHashMap<>();
        IEMCProxy proxy = ProjectEAPI.getEMCProxy();
        if (proxy == null) return result;

        for (Item item : ForgeRegistries.ITEMS) {
            if (item.getDefaultInstance().isEmpty()) continue;
            long emc = proxy.getValue(item);
            if (emc > 0) {
                result.put(item, emc);
            }
        }
        return result;
    }

    public static List<ItemEmcEntry> getAllItems() {
        List<ItemEmcEntry> result = new ArrayList<>();
        IEMCProxy proxy = ProjectEAPI.getEMCProxy();

        for (Item item : ForgeRegistries.ITEMS) {
            if (item.getDefaultInstance().isEmpty()) continue;
            long emc = proxy != null ? proxy.getValue(item) : 0;
            result.add(new ItemEmcEntry(item, emc));
        }
        return result;
    }

    public static void setAuthorized(UUID playerUUID, boolean authorized) {
        if (authorized) {
            authorizedPlayers.add(playerUUID);
        } else {
            authorizedPlayers.remove(playerUUID);
        }
    }

    public static boolean isAuthorized(UUID playerUUID) {
        return authorizedPlayers.contains(playerUUID);
    }

    public record ItemEmcEntry(Item item, long emcValue) {
        public boolean hasEmc() {
            return emcValue > 0;
        }
    }
}