package org.xiaoyang.ex_enigmaticlegacy.Init;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item.Relic.TerrorCrown;
import org.xiaoyang.ex_enigmaticlegacy.Exe;
import org.xiaoyang.ex_enigmaticlegacy.Item.armor.*;
import org.xiaoyang.ex_enigmaticlegacy.Item.armor.UV.*;
import org.xiaoyang.ex_enigmaticlegacy.Item.armor.material.SMaterial;

public class ModArmors {
    public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, Exe.MODID);

    public static final RegistryObject<Item> MANAITA_CHESTPLATE = REGISTRY.register(
            "manaita_chestplate", () -> new ManaitaArmor(ArmorItem.Type.CHESTPLATE));
    public static final RegistryObject<Item> MANAITA_LEGGINGS = REGISTRY.register(
            "manaita_leggings", () -> new ManaitaArmor(ArmorItem.Type.LEGGINGS));
    public static final RegistryObject<Item> MANAITA_BOOTS = REGISTRY.register(
            "manaita_boots", () -> new ManaitaArmor(ArmorItem.Type.BOOTS));
    public static final RegistryObject<Item> MANAITA_HELMET = REGISTRY.register(
            "manaita_helmet", () -> new ManaitaArmor(ArmorItem.Type.HELMET));

    public static final RegistryObject<Item> TERRO_RCROWN = REGISTRY.register(
            "terror_crown", () -> new TerrorCrown(new SMaterial(),ArmorItem.Type.HELMET));
    public static final RegistryObject<Item> DRAGON_WINGS = REGISTRY.register(
            "dragon_wings", () -> new DragonWings(new Item.Properties().rarity(Rarity.EPIC).stacksTo(1)));

    public static final RegistryObject<Item> NEBULA_HELMET = REGISTRY.register(
            "nebula_helmet", () -> new NebulaArmor(ArmorItem.Type.HELMET));
    public static final RegistryObject<Item> NEBULA_HELMET_REVEAL = REGISTRY.register(
            "nebula_helmet_reveal", () -> new NebulaArmor(ArmorItem.Type.HELMET));
    public static final RegistryObject<Item> NEBULA_CHESTPLATE = REGISTRY.register(
            "nebula_chestplate", () -> new NebulaArmor(ArmorItem.Type.CHESTPLATE));
    public static final RegistryObject<Item> NEBULA_LEGGINGS = REGISTRY.register(
            "nebula_leggings", () -> new NebulaArmor(ArmorItem.Type.LEGGINGS));
    public static final RegistryObject<Item> NEBULA_BOOTS = REGISTRY.register(
            "nebula_boots", () -> new NebulaArmor(ArmorItem.Type.BOOTS));

    public static final RegistryObject<Item> WILD_HUNT_HELMET = REGISTRY.register(
            "wild_hunt_helmet", () -> new WildHuntArmor(ArmorItem.Type.HELMET));
    public static final RegistryObject<Item> WILD_HUNT_CHESTPLATE = REGISTRY.register(
            "wild_hunt_chestplate", () -> new WildHuntArmor(ArmorItem.Type.CHESTPLATE));
    public static final RegistryObject<Item> WILD_HUNT_LEGGINGS = REGISTRY.register(
            "wild_hunt_leggings", () -> new WildHuntArmor(ArmorItem.Type.LEGGINGS));
    public static final RegistryObject<Item> WILD_HUNT_BOOTS = REGISTRY.register(
            "wild_hunt_boots", () -> new WildHuntArmor(ArmorItem.Type.BOOTS));

    public static final RegistryObject<Item> dragonArmorHelm = REGISTRY.register(
            "dragon_helmet", () -> new DragonCrystalArmor(ArmorMaterials.NETHERITE, ArmorItem.Type.HELMET));
    public static final RegistryObject<Item> dragonArmorChest = REGISTRY.register(
            "dragon_chestplate", () -> new DragonCrystalArmor(ArmorMaterials.NETHERITE, ArmorItem.Type.CHESTPLATE));
    public static final RegistryObject<Item> dragonArmorLegs = REGISTRY.register(
            "dragon_leggings", () -> new DragonCrystalArmor(ArmorMaterials.NETHERITE, ArmorItem.Type.LEGGINGS));
    public static final RegistryObject<Item> dragonArmorBoots = REGISTRY.register(
            "dragon_boots", () -> new DragonCrystalArmor(ArmorMaterials.NETHERITE, ArmorItem.Type.BOOTS));

    public static final RegistryObject<Item> ULTIMATE_VALKYRIE_HELMET = REGISTRY.register(
            "ultimate_valkyrie_helmet", () -> new UltimateValkyrieHelmet(new Item.Properties()));
    public static final RegistryObject<Item> ULTIMATE_VALKYRIE_CHESTPLATE = REGISTRY.register(
            "ultimate_valkyrie_chestplate", () -> new UltimateValkyrieChestplate(new Item.Properties()));
    public static final RegistryObject<Item> ULTIMATE_VALKYRIE_LEGGINGS = REGISTRY.register(
            "ultimate_valkyrie_leggings", () -> new UltimateValkyrieLeggings(new Item.Properties()));
    public static final RegistryObject<Item> ULTIMATE_VALKYRIE_BOOTS = REGISTRY.register(
            "ultimate_valkyrie_boots", () -> new UltimateValkyrieBoots(new Item.Properties()));

    public static final RegistryObject<Item> SLIME_HELMET = REGISTRY.register(
            "slime_helmet", () -> new SlimeArmor(ArmorMaterials.NETHERITE, ArmorItem.Type.HELMET));
    public static final RegistryObject<Item> SLIME_CHESTPLATE = REGISTRY.register(
            "slime_chestplate", () -> new SlimeArmor(ArmorMaterials.NETHERITE,ArmorItem.Type.CHESTPLATE));
    public static final RegistryObject<Item> SLIME_LEGGINGS = REGISTRY.register(
            "slime_leggings", () -> new SlimeArmor(ArmorMaterials.NETHERITE,ArmorItem.Type.LEGGINGS));
    public static final RegistryObject<Item> SLIME_BOOTS = REGISTRY.register(
            "slime_boots", () -> new SlimeArmor(ArmorMaterials.NETHERITE,ArmorItem.Type.BOOTS));

    public static final RegistryObject<ArmorSunmaker> SUNMAKER_HELMET = REGISTRY.register(
            "sunmaker_helmet", () -> new ArmorSunmaker(ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<ArmorSunmaker> SUNMAKER_CHESTPLATE = REGISTRY.register(
            "sunmaker_chestplate", () -> new ArmorSunmaker(ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<ArmorSunmaker> SUNMAKER_LEGGINGS = REGISTRY.register(
            "sunmaker_leggings", () -> new ArmorSunmaker(ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<ArmorSunmaker> SUNMAKER_BOOTS = REGISTRY.register(
            "sunmaker_boots", () -> new ArmorSunmaker(ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1)));
}