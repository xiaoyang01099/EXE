package net.xiaoyang010.ex_enigmaticlegacy.Init;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.TerrorCrown;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Item.armor.*;
import net.xiaoyang010.ex_enigmaticlegacy.Item.armor.UV.UltimateValkyrieBoots;
import net.xiaoyang010.ex_enigmaticlegacy.Item.armor.UV.UltimateValkyrieChestplate;
import net.xiaoyang010.ex_enigmaticlegacy.Item.armor.UV.UltimateValkyrieHelmet;
import net.xiaoyang010.ex_enigmaticlegacy.Item.armor.UV.UltimateValkyrieLeggings;

public class ModArmors {
    public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, ExEnigmaticlegacyMod.MODID);

    public static final RegistryObject<Item> MANAITA_CHESTPLATE = REGISTRY.register("manaita_chestplate", () -> new ManaitaArmor(EquipmentSlot.CHEST));
    public static final RegistryObject<Item> MANAITA_LEGGINGS = REGISTRY.register("manaita_leggings", () -> new ManaitaArmor(EquipmentSlot.LEGS));
    public static final RegistryObject<Item> MANAITA_BOOTS = REGISTRY.register("manaita_boots", () -> new ManaitaArmor(EquipmentSlot.FEET));
    public static final RegistryObject<Item> MANAITA_HELMET = REGISTRY.register("manaita_helmet", () -> new ManaitaArmor(EquipmentSlot.HEAD));
    public static final RegistryObject<Item> TERRO_RCROWN = REGISTRY.register("terror_crown", () -> new TerrorCrown(new SMaterial(), EquipmentSlot.HEAD));
    public static final RegistryObject<Item> DRAGON_WINGS = REGISTRY.register("dragon_wings", () -> new DragonWings(new Item.Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR).rarity(Rarity.EPIC).durability(0)));

    public static final RegistryObject<Item> NEBULA_HELMET = REGISTRY.register("nebula_helmet", () -> new NebulaArmor(EquipmentSlot.HEAD));
    public static final RegistryObject<Item> NEBULA_HELMET_REVEAL = REGISTRY.register("nebula_helmet_reveal",() -> new NebulaArmor(EquipmentSlot.HEAD));
    public static final RegistryObject<Item> NEBULA_CHESTPLATE = REGISTRY.register("nebula_chestplate",() -> new NebulaArmor(EquipmentSlot.CHEST));
    public static final RegistryObject<Item> NEBULA_LEGGINGS = REGISTRY.register("nebula_leggings",() -> new NebulaArmor(EquipmentSlot.LEGS));
    public static final RegistryObject<Item> NEBULA_BOOTS = REGISTRY.register("nebula_boots",() -> new NebulaArmor(EquipmentSlot.FEET));

    public static final RegistryObject<Item> WILD_HUNT_HELMET = REGISTRY.register("wild_hunt_helmet",() -> new WildHuntArmor(EquipmentSlot.HEAD));
    public static final RegistryObject<Item> WILD_HUNT_CHESTPLATE = REGISTRY.register("wild_hunt_chestplate",() -> new WildHuntArmor(EquipmentSlot.CHEST));
    public static final RegistryObject<Item> WILD_HUNT_LEGGINGS = REGISTRY.register("wild_hunt_leggings",() -> new WildHuntArmor(EquipmentSlot.LEGS));
    public static final RegistryObject<Item> WILD_HUNT_BOOTS = REGISTRY.register("wild_hunt_boots",() -> new WildHuntArmor(EquipmentSlot.FEET));

    public static final RegistryObject<Item> dragonArmorHelm = REGISTRY.register("dragon_helmet", () -> new DragonCrystalArmor(ArmorMaterials.NETHERITE, EquipmentSlot.HEAD));
    public static final RegistryObject<Item> dragonArmorChest = REGISTRY.register("dragon_chestplate", () -> new DragonCrystalArmor(ArmorMaterials.NETHERITE, EquipmentSlot.CHEST));
    public static final RegistryObject<Item> dragonArmorLegs = REGISTRY.register("dragon_leggings", () -> new DragonCrystalArmor(ArmorMaterials.NETHERITE, EquipmentSlot.LEGS));
    public static final RegistryObject<Item> dragonArmorBoots = REGISTRY.register("dragon_boots", () -> new DragonCrystalArmor(ArmorMaterials.NETHERITE, EquipmentSlot.FEET));

    public static final RegistryObject<Item> ENTITY_STANDABLE_BOOTS = REGISTRY.register("entity_standable_boots",
            () -> new EntityStandableBoots(
                    ArmorMaterials.NETHERITE,
                    new Item.Properties()//.tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR)
            ));

    public static final RegistryObject<Item> ULTIMATE_VALKYRIE_HELMET = REGISTRY.register("ultimate_valkyrie_helmet",
            () -> new UltimateValkyrieHelmet(
                    new Item.Properties()
                            .tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR)
                            .rarity(Rarity.EPIC)
            ));

    public static final RegistryObject<Item> ULTIMATE_VALKYRIE_CHESTPLATE = REGISTRY.register("ultimate_valkyrie_chestplate",
            () -> new UltimateValkyrieChestplate(
                    new Item.Properties()
                            .tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR)
                            .rarity(Rarity.EPIC)
            ));

    public static final RegistryObject<Item> ULTIMATE_VALKYRIE_LEGGINGS = REGISTRY.register("ultimate_valkyrie_leggings",
            () -> new UltimateValkyrieLeggings(
                    new Item.Properties()
                            .tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR)
                            .rarity(Rarity.EPIC)
            ));

    public static final RegistryObject<Item> ULTIMATE_VALKYRIE_BOOTS = REGISTRY.register("ultimate_valkyrie_boots",
            () -> new UltimateValkyrieBoots(
                    new Item.Properties()
                            .tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR)
                            .rarity(Rarity.EPIC)
            ));

}
