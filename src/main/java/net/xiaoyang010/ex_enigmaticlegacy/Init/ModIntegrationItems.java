package net.xiaoyang010.ex_enigmaticlegacy.Init;

import net.minecraft.world.item.Item;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.PetrifyingWand;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Spell.InfinitasVortex;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Item.InfinityMatter;
import net.xiaoyang010.ex_enigmaticlegacy.Item.all.ModAmorphous;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ModIntegrationItems {
    public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, ExEnigmaticlegacyMod.MODID);

    private static final Map<String, Map<String, RegistryObject<Item>>> INTEGRATION_ITEMS = new HashMap<>();

    // ProjectE 联动物品
    private static final Map<String, RegistryObject<Item>> PROJECTE_ITEMS = new HashMap<>();
    static {
        if(ModList.get().isLoaded("projecte")) {

            PROJECTE_ITEMS.put("infinity_matter", register("infinity_matter", InfinityMatter::new));
            PROJECTE_ITEMS.put("matter_amorphous_max", register("matter_amorphous_max", ModAmorphous::new));
            PROJECTE_ITEMS.put("matter_corporeal_max", register("matter_corporeal_max", ModAmorphous::new));
            PROJECTE_ITEMS.put("matter_dark_max", register("matter_dark_max", ModAmorphous::new));
            PROJECTE_ITEMS.put("matter_essentia_max", register("matter_essentia_max", ModAmorphous::new));
            PROJECTE_ITEMS.put("matter_kinetic_max", register("matter_kinetic_max", ModAmorphous::new));
            PROJECTE_ITEMS.put("matter_omni_max", register("matter_omni_max", ModAmorphous::new));
            PROJECTE_ITEMS.put("matter_proto_max", register("matter_proto_max", ModAmorphous::new));
            PROJECTE_ITEMS.put("matter_temporal_max", register("matter_temporal_max", ModAmorphous::new));
            PROJECTE_ITEMS.put("matter_void_max", register("matter_void_max", ModAmorphous::new));


        }
        INTEGRATION_ITEMS.put("projecte", PROJECTE_ITEMS);
    }

    private static final Map<String, RegistryObject<Item>> SPELLBOOKS_ITEM = new HashMap<>();
    static {
        if(ModList.get().isLoaded("irons_spellbooks")) {

            SPELLBOOKS_ITEM.put("infinitas_vortex", register("infinitas_vortex", InfinitasVortex::new));

        }
        INTEGRATION_ITEMS.put("irons_spellbooks", SPELLBOOKS_ITEM);
    }

    private static final Map<String, RegistryObject<Item>> ICE_AND_FIRE_ITEM = new HashMap<>();
    static {
        if(ModList.get().isLoaded("iceandfire")) {

            ICE_AND_FIRE_ITEM.put("petrifying_wand", register("petrifying_wand", PetrifyingWand::new));

        }
        INTEGRATION_ITEMS.put("iceandfire", ICE_AND_FIRE_ITEM);
    }


    // 通用注册方法
    private static RegistryObject<Item> register(String name, Supplier<Item> item) {
        return REGISTRY.register(name, item);
    }

    // 获取特定模组的所有联动物品
    public static Map<String, RegistryObject<Item>> getModItems(String modid) {
        return INTEGRATION_ITEMS.getOrDefault(modid, new HashMap<>());
    }

    // 检查某个联动物品是否已注册
    public static boolean isItemRegistered(String modid, String itemName) {
        Map<String, RegistryObject<Item>> modItems = INTEGRATION_ITEMS.get(modid);
        return modItems != null && modItems.containsKey(itemName);
    }

    // 获取特定联动物品
    public static Item getItem(String modid, String itemName) {
        Map<String, RegistryObject<Item>> modItems = INTEGRATION_ITEMS.get(modid);
        if (modItems != null && modItems.containsKey(itemName)) {
            return modItems.get(itemName).get();
        }
        return null;
    }
}