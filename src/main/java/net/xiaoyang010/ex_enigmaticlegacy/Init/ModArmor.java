package net.xiaoyang010.ex_enigmaticlegacy.Init;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Item.armor.*;

public class ModArmor {
    public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, ExEnigmaticlegacyMod.MODID);
    public static final RegistryObject<Item> MANAITA_CHESTPLATE = REGISTRY.register("manaita_chestplate",
            () -> new ManaitaChestplate(
                    new ZMaterial(),
                    EquipmentSlot.CHEST,
                    new Item.Properties()
                            .tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR)
                            .rarity(ModRarities.MIRACLE)
            ));

    public static final RegistryObject<Item> MANAITA_LEGGINGS = REGISTRY.register("manaita_leggings",
            () -> new ManaitaLeggings(
                    new ZMaterial(),
                    EquipmentSlot.LEGS,
                    new Item.Properties()
                            .tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR)
                            .rarity(ModRarities.MIRACLE)
            ));

    public static final RegistryObject<Item> MANAITA_BOOTS = REGISTRY.register("manaita_boots",
            () -> new ManaitaBoots(
                    new ZMaterial(),
                    EquipmentSlot.FEET,
                    new Item.Properties()
                            .tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR)
                            .rarity(ModRarities.MIRACLE)
            ));

    public static final RegistryObject<Item> MANAITA_HELMET = REGISTRY.register("manaita_helmet",
            () -> new ManaitaHelmet(
                    new ZMaterial(),
                    EquipmentSlot.HEAD,
                    new Item.Properties()
                            .tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR)
                            .rarity(ModRarities.MIRACLE)
            ));

    public static final RegistryObject<Item> TERRO_RCROWN = REGISTRY.register("terror_crown",
            () -> new TerrorCrown(
                    new SMaterial(),
                    EquipmentSlot.HEAD,
                    new Item.Properties()
                            .tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR)
                            .rarity(ModRarities.MIRACLE)
            ));

    public static final RegistryObject<Item> DRAGON_WINGS = REGISTRY.register("dragon_wings",
            () -> new Dragonwings(
                    new Item.Properties()
                            .tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR)
                            .rarity(Rarity.EPIC)
                            .durability(0)

            ));

    public static final RegistryObject<Item> NEBULA_HELMET = REGISTRY.register("nebula_helmet",
            () -> new NebulaHelm(
                    new Item.Properties()
                            .rarity(Rarity.EPIC)
            ));

    public static final RegistryObject<Item> NEBULA_HELMET_REVEAL = REGISTRY.register("nebula_helmet_reveal",
            () -> new NebulaHelmReveal(
                    new Item.Properties()
                            .rarity(Rarity.EPIC)
            ));

    public static final RegistryObject<Item> NEBULA_CHESTPLATE = REGISTRY.register("nebula_chestplate",
            () -> new NebulaChest(
                    new Item.Properties()
                            .rarity(Rarity.EPIC)
            ));

    public static final RegistryObject<Item> NEBULA_LEGGINGS = REGISTRY.register("nebula_leggings",
            () -> new NebulaLegs(
                    new Item.Properties()
                            .rarity(Rarity.EPIC)
            ));

    public static final RegistryObject<Item> NEBULA_BOOTS = REGISTRY.register("nebula_boots",
            () -> new NebulaBoots(
                    new Item.Properties()
                            .rarity(Rarity.EPIC)
            ));
}
