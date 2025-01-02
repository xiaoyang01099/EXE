package net.xiaoyang010.ex_enigmaticlegacy.Init;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Item.armor.*;

public class ModArmors {
    public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, ExEnigmaticlegacyMod.MODID);

    public static final RegistryObject<Item> MANAITA_CHESTPLATE = REGISTRY.register("manaita_chestplate", () -> new ManaitaArmor(EquipmentSlot.CHEST));
    public static final RegistryObject<Item> MANAITA_LEGGINGS = REGISTRY.register("manaita_leggings", () -> new ManaitaArmor(EquipmentSlot.LEGS));
    public static final RegistryObject<Item> MANAITA_BOOTS = REGISTRY.register("manaita_boots", () -> new ManaitaArmor(EquipmentSlot.FEET));
    public static final RegistryObject<Item> MANAITA_HELMET = REGISTRY.register("manaita_helmet", () -> new ManaitaArmor(EquipmentSlot.HEAD));

    public static final RegistryObject<Item> TERRO_RCROWN = REGISTRY.register("terror_crown", () -> new TerrorCrown(new SMaterial(), EquipmentSlot.HEAD));

    public static final RegistryObject<Item> DRAGON_WINGS = REGISTRY.register("dragon_wings", () -> new DragonWings(
                    new Item.Properties()
                            .tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR)
                            .rarity(Rarity.EPIC)
                            .durability(0)

            ));

    public static final RegistryObject<Item> NEBULA_HELMET = REGISTRY.register("nebula_helmet", () -> new NebulaArmor(EquipmentSlot.HEAD));
    public static final RegistryObject<Item> NEBULA_HELMET_REVEAL = REGISTRY.register("nebula_helmet_reveal",() -> new NebulaArmor(EquipmentSlot.HEAD));
    public static final RegistryObject<Item> NEBULA_CHESTPLATE = REGISTRY.register("nebula_chestplate",() -> new NebulaArmor(EquipmentSlot.CHEST));
    public static final RegistryObject<Item> NEBULA_LEGGINGS = REGISTRY.register("nebula_leggings",() -> new NebulaArmor(EquipmentSlot.LEGS));
    public static final RegistryObject<Item> NEBULA_BOOTS = REGISTRY.register("nebula_boots",() -> new NebulaArmor(EquipmentSlot.FEET));
}
