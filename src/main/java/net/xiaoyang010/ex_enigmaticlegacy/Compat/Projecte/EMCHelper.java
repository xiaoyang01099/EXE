package net.xiaoyang010.ex_enigmaticlegacy.Compat.Projecte;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.mapper.EMCMapper;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.xiaoyang010.ex_enigmaticlegacy.api.INoEMCItem;

import java.lang.reflect.Field;
import java.util.Map;

public class EMCHelper {

    public static boolean isEMCForbidden(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        }

        Item item = itemStack.getItem();
        return item instanceof INoEMCItem;
    }

    public static boolean isEMCForbidden(Item item) {
        return item instanceof INoEMCItem;
    }

    public static boolean isEMCForbidden(ItemInfo itemInfo) {
        if (itemInfo == null) {
            return false;
        }

        Item item = itemInfo.getItem();
        return item instanceof INoEMCItem;
    }

    public static void forceCleanupEMCMap() {
        try {
            Class<?> emcMappingHandler = Class.forName("moze_intel.projecte.emc.EMCMappingHandler");
            Field emcField = emcMappingHandler.getDeclaredField("emc");
            emcField.setAccessible(true);

            @SuppressWarnings("unchecked")
            Map<ItemInfo, Long> emcMap = (Map<ItemInfo, Long>) emcField.get(null);

            int removedCount = 0;
            var iterator = emcMap.entrySet().iterator();
            while (iterator.hasNext()) {
                var entry = iterator.next();
                ItemInfo itemInfo = entry.getKey();
                if (isEMCForbidden(itemInfo)) {
                    iterator.remove();
                    removedCount++;
                }
            }

            if (removedCount > 0) {
                System.out.println("NoEMC: Cleaned up " + removedCount + " forbidden items from EMC map");
            }

        } catch (Exception e) {
            System.err.println("NoEMC: Failed to cleanup EMC map: " + e.getMessage());
        }
    }

    @EMCMapper(priority = Integer.MAX_VALUE)
    public static class NoEMCMapper implements IEMCMapper<NormalizedSimpleStack, Long> {

        @EMCMapper.Instance
        public static final NoEMCMapper INSTANCE = new NoEMCMapper();

        @Override
        public String getName() {
            return "NoEMC Exclusion Mapper";
        }

        @Override
        public String getDescription() {
            return "Removes EMC values from items implementing INoEMCItem interface";
        }

        @Override
        public void addMappings(IMappingCollector<NormalizedSimpleStack, Long> collector,
                                CommentedFileConfig config,
                                ReloadableServerResources serverResources,
                                ResourceManager resourceManager) {
            ForgeRegistries.ITEMS.forEach(item -> {
                if (item instanceof INoEMCItem) {
                    NormalizedSimpleStack nss = NSSItem.createItem(item);
                    collector.setValueBefore(nss, 0L);
                }
            });
        }
    }
}