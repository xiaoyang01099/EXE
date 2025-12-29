
package net.xiaoyang010.ex_enigmaticlegacy.Init;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.AesirRing;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.*;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.ModRunes;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.*;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.ItemBlockFlower;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.DarkSunRing;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.ItemHybridSpecialFlower;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.SuperpositionRing;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.HolyRing;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Item.*;
import net.xiaoyang010.ex_enigmaticlegacy.Item.all.ModDetermination;
import net.xiaoyang010.ex_enigmaticlegacy.Item.all.ModIngot;
import net.xiaoyang010.ex_enigmaticlegacy.Item.all.ModSingularity;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.TelekinesisTome;


public class ModItems {
	public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, ExEnigmaticlegacyMod.MODID);

	public static final RegistryObject<Item> IRIDIUM_ORE = block(ModBlockss.IRIDIUM_ORE, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> NICKEL_ORE = block(ModBlockss.NICKEL_ORE, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> PLATINUM_ORE = block(ModBlockss.PLATINUM_ORE, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> END_ORE = block(ModBlockss.END_ORE, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> AMETHYST_ORE = block(ModBlockss.AMETHYST_ORE, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> BAUXITE_ORE = block(ModBlockss.BAUXITE_ORE, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> CHROMITE_ORE = block(ModBlockss.CHROMITE_ORE, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> FLUORITE_ORE = block(ModBlockss.FLUORITE_ORE, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> GYPSUM_ORE = block(ModBlockss.GYPSUM_ORE, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> JADE_ORE = block(ModBlockss.JADE_ORE, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> LEAD_ORE = block(ModBlockss.LEAD_ORE, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> BRONZE_ORE = block(ModBlockss.BRONZE_ORE, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> OPAL_ORE = block(ModBlockss.OPAL_ORE, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> SPECTRITE_ORE = block(ModBlockss.SPECTRITE_ORE, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);





	//扩展花
	public static final RegistryObject<Item> CURSET_THISTLE = blockFlower(ModBlockss.CURSET_THISTLE, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> YU_SHOU_CLOVER = blockFlower(ModBlockss.YU_SHOU_CLOVER, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> VACUITY = blockFlower(ModBlockss.VACUITY, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> STREET_LIGHT = blockFlower(ModBlockss.STREET_LIGHT, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> BLAZING_ORCHID = blockFlower(ModBlockss.BLAZING_ORCHID, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> RAINBOW_GENERATING_FLOWER = blockFlower(ModBlockss.RAINBOW_GENERATING_FLOWER, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> MINGXIANLAN = blockFlower(ModBlockss.MINGXIANLAN, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> FROST_BLOSSOM = blockFlower(ModBlockss.FROST_BLOSSOM, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> FROST_LOTUS = blockFlower(ModBlockss.FROST_LOTUS, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> DARK_NIGHT_GRASS = blockFlower(ModBlockss.DARK_NIGHT_GRASS, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> ASTRAL_KILLOP = blockFlower(ModBlockss.ASTRAL_KILLOP, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> KILLING_BERRY = blockFlower(ModBlockss.KILLING_BERRY, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> NIGHTSHADE = blockFlower(ModBlockss.NIGHTSHADE, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> DAYBLOOM = blockFlower(ModBlockss.DAYBLOOM, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> BELIEVE = blockFlower(ModBlockss.BELIEVER, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> GENENERGYDANDRON = blockFlower(ModBlockss.GENENERGYDANDRON, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> PRISMATICRADIANCEBLOCK = blockFlower(ModBlockss.PRISMATICRADIANCEBLOCK, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> ORECHIDENDIUM = blockFlower(ModBlockss.ORECHIDENDIUM, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> BLOCKNATURE = blockFlower(ModBlockss.BLOCKNATURE, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> ASGARDANDELION = blockFlower(ModBlockss.ASGARDANDELION, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> FLOWEY = blockFlower(ModBlockss.FLOWEY,ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> WITCH_OPOOD = blockFlower(ModBlockss.WITCH_OPOOD,ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> LYCORISRADIATA = blockFlower(ModBlockss.LYCORISRADIATA,ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> SOARLEANDER = blockFlower(ModBlockss.SOARLEANDER, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> ENDER_LAVENDER = blockFlower(ModBlockss.ENDER_LAVENDER, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> AUREA_AMICITIA_CARNATION = blockFlower(ModBlockss.AUREA_AMICITIA_CARNATION, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> CATNIP = blockFlower(ModBlockss.CATNIP, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> MUSICAL_ORCHID = blockFlower(ModBlockss.MUSICAL_ORCHID, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> DICTARIUS = blockFlower(ModBlockss.DICTARIUS, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> ANCIENT_ALPHIRINE = blockFlower(ModBlockss.ANCIENT_ALPHIRINE, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> EVIL_FORGE = blockFlower(ModBlockss.EVIL_FORGE, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> ETHERIUM_FORGE = blockFlower(ModBlockss.ETHERIUM_FORGE, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> ARDENT_AZARCISSUS = blockFlower(ModBlockss.ARDENT_AZARCISSUS, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> AQUATIC_ANGLER_NARCISSUS = blockHyFlower(ModBlockss.AQUATIC_ANGLER_NARCISSUS, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> RUNE_FLOWER = blockHyFlower(ModBlockss.RUNE_FLOWER, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);








	//扩展浮空花







	//其他方块物品
	public static final RegistryObject<Item> MANA_CRYSTAL = block(ModBlockss.MANA_CRYSTAL, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> MANA_CHARGER = block(ModBlockss.MANA_CHARGER, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> infinitySpreader = block(ModBlockss.infinitySpreader, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> FLUFFY_DANDELION = block(ModBlockss.FLUFFY_DANDELION, ModTabs.TAB_EXENIGMATICLEGACY_ITEM);
	public static final RegistryObject<Item> CELESTIAL_HOLINESS_TRANSMUTER = block(ModBlockss.CELESTIAL_HOLINESS_TRANSMUTER, ModTabs.TAB_EXENIGMATICLEGACY_ITEM);
	public static final RegistryObject<Item> INFINITYGLASS = block(ModBlockss.INFINITYGlASS, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> EVILBLOCK = block(ModBlockss.EVILBLOCK, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> ICHEST = block(ModBlockss.INFINITYCHEST, ModTabs.TAB_EXENIGMATICLEGACY_ITEM);
	public static final RegistryObject<Item> COBBLE_STONE = block(ModBlockss.COBBLE_STONE, ModTabs.TAB_EXENIGMATICLEGACY_ITEM);
	public static final RegistryObject<Item> INFINITY_POTATO = block(ModBlockss.INFINITY_POTATO, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> CUSTOM_SAPLING = block(ModBlockss.CUSTOM_SAPLING, ModTabs.TAB_EXENIGMATICLEGACY_ITEM);
	public static final RegistryObject<Item> SPECTRITE_CHEST = block(ModBlockss.SPECTRITE_CHEST, ModTabs.TAB_EXENIGMATICLEGACY_ITEM);
	public static final RegistryObject<Item> DRAGON_CRYSTALS_BLOCK = block(ModBlockss.DRAGON_CRYSTALS_BLOCK, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> POLYCHROME_COLLAPSE_PRISM = block(ModBlockss.POLYCHROME_COLLAPSE_PRISM, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> MANA_CONTAINER = block(ModBlockss.MANA_CONTAINER, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> DILUTED_CONTAINER = block(ModBlockss.DILUTED_CONTAINER, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> CREATIVE_CONTAINER = block(ModBlockss.CREATIVE_CONTAINER, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> MITHRILL_BLOCK = block(ModBlockss.MITHRILL_BLOCK, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> NIDAVELLIR_FORGE = block(ModBlockss.NIDAVELLIR_FORGE, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> GAIA_BLOCK = block(ModBlockss.GAIA_BLOCK, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> PAGED_CHEST = block(ModBlockss.PAGED_CHEST, ModTabs.TAB_EXENIGMATICLEGACY_ITEM);
	public static final RegistryObject<Item> ARCANE_ICE_CHUNK = block(ModBlockss.ARCANE_ICE_CHUNK, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> DECAY_BLOCK = block(ModBlockss.DECAY_BLOCK, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> IRIDIUM_BLOCK = block(ModBlockss.IRIDIUM_BLOCK, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> NICKEL_BLOCK = block(ModBlockss.NICKEL_BLOCK, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> AERIALITE_BLOCK = block(ModBlockss.AERIALITE_BLOCK, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> RAINBOW_TABLE = block(ModBlockss.RAINBOW_TABLE, ModTabs.TAB_EXENIGMATICLEGACY_ITEM);
	public static final RegistryObject<Item> DECON_TABLE_ITEM = block(ModBlockss.DECON_TABLE, ModTabs.TAB_EXENIGMATICLEGACY_ITEM);
	public static final RegistryObject<Item> FULL_ALTAR = block(ModBlockss.FULL_ALTAR, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> GAME_BOARD = block(ModBlockss.GAME_BOARD, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> BOARD_FATE = block(ModBlockss.BOARD_FATE, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> TERRA_FARMLAND = block(ModBlockss.TERRA_FARMLAND, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> ASTRAL_BLOCK = block(ModBlockss.ASTRAL_BLOCK, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> MANA_BRACKET = block(ModBlockss.MANA_BRACKET, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> ENGINEER_HOPPER = block(ModBlockss.ENGINEER_HOPPER, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> EXTREME_AUTO_CRAFTER = block(ModBlockss.EXTREME_AUTO_CRAFTER, ModTabs.TAB_EXENIGMATICLEGACY_ITEM);
	public static final RegistryObject<Item> INFINITY_COMPRESSOR = block(ModBlockss.INFINITY_COMPRESSOR, ModTabs.TAB_EXENIGMATICLEGACY_ITEM);
	public static final RegistryObject<Item> EXTREME_CRAFTING_DISASSEMBLY_TABLE = block(ModBlockss.EXTREME_CRAFTING_DISASSEMBLY_TABLE, ModTabs.TAB_EXENIGMATICLEGACY_ITEM);

	//public static final RegistryObject<Item> STARLITSANCTUM = block(ModBlocks.STARLITSANCTUM, ModTabs.TAB_EXENIGMATICLEGACY_ITEM);

	public static final RegistryObject<Item> MANA_BOX_ITEM = REGISTRY.register("mana_box", BlockItemManaBox::new);


	//31个物品通用属性
	private static final Properties INGOT_PROPERTIES = new Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_MINERAL).stacksTo(64);
	public static final RegistryObject<Item> NETHER_STAR_NUGGET = REGISTRY.register("nether_star_nugget", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.EPIC)));
	public static final RegistryObject<Item> ENHANCEMENT_CRYSTAL = REGISTRY.register("enhancement_crystal", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.EPIC)));
	public static final RegistryObject<Item> FROST_ENCHANTRESS = REGISTRY.register("frost_enchantress", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.EPIC)));
	public static final RegistryObject<Item> DEAD_SUBSTANCE = REGISTRY.register("dead_substance", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.RARE)));
	public static final RegistryObject<Item> AMETHYST_INGOT = REGISTRY.register("amethyst_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.RARE)));
	public static final RegistryObject<Item> BAUXITE_INGOT = REGISTRY.register("bauxite_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.RARE)));
	public static final RegistryObject<Item> BRONZE_INGOT = REGISTRY.register("bronze_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.RARE)));
	public static final RegistryObject<Item> CHROMITE_INGOT = REGISTRY.register("chromite_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.RARE)));
	public static final RegistryObject<Item> FLUORITE_INGOT = REGISTRY.register("fluorite_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.RARE)));
	public static final RegistryObject<Item> GYPSUM_INGOT = REGISTRY.register("gypsum_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.RARE)));
	public static final RegistryObject<Item> JADE_INGOT = REGISTRY.register("jade_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.RARE)));
	public static final RegistryObject<Item> LEAD_INGOT = REGISTRY.register("lead_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.RARE)));
	public static final RegistryObject<Item> OPALLY = REGISTRY.register("opally", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.RARE)));
	public static final RegistryObject<Item> OPAL_INGOT = REGISTRY.register("opal_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.RARE)));
	public static final RegistryObject<Item> ASTRAL_PILE = REGISTRY.register("astral_pile", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.EPIC)));
	public static final RegistryObject<Item> ASTRAL_NUGGET = REGISTRY.register("astral_nugget", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.EPIC)));
	public static final RegistryObject<Item> ASTRAL_INGOT = REGISTRY.register("astral_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(ModRarities.MIRACLE)));
	public static final RegistryObject<Item> CUSTOM_INGOT = REGISTRY.register("custom_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.COMMON)));
	public static final RegistryObject<Item> CUSTOM_NUGGET = REGISTRY.register("custom_nugget", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.COMMON)));
	public static final RegistryObject<Item> COLORFUL_SHADOW_SHARD = REGISTRY.register("colorful_shadow_shard", () -> new ModIngot(INGOT_PROPERTIES.fireResistant().rarity(ModRarities.MIRACLE)));
	public static final RegistryObject<Item> DEATH_INGOT =REGISTRY.register("death_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.COMMON)));
	public static final RegistryObject<Item> DIRT_INGOT = REGISTRY.register("dirt_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.COMMON)));
	public static final RegistryObject<Item> DYEABLE_REDSTONE = REGISTRY.register("dyeable_redstone", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.EPIC)));
	public static final RegistryObject<Item> END_CRYSTAIC = REGISTRY.register("end_crystaic", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.EPIC)));
	public static final RegistryObject<Item> ELECTRIC_INGOT = REGISTRY.register("electric_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.COMMON)));
	public static final RegistryObject<Item> GRASS_INGOT = REGISTRY.register("grass_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.COMMON)));
	public static final RegistryObject<Item> IRIDIUM_INGOT = REGISTRY.register("iridium_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.COMMON)));
	public static final RegistryObject<Item> ICE_INGOT = REGISTRY.register("ice_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.COMMON)));
	public static final RegistryObject<Item> INGOT_ANIMATION = REGISTRY.register("ingot_animation", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.COMMON)));
	public static final RegistryObject<Item> INFINITYDROP = REGISTRY.register("infinitydrop", () -> new ModIngot(INGOT_PROPERTIES.fireResistant().rarity(Rarity.EPIC)));
	public static final RegistryObject<Item> LAVA_INGOT = REGISTRY.register("lava_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.COMMON)));
	public static final RegistryObject<Item> MANA_NETHER_STAR = REGISTRY.register("mana_nether_star", () -> new ModIngot(INGOT_PROPERTIES.fireResistant().rarity(Rarity.EPIC)));
	public static final RegistryObject<Item> MITHRILL = REGISTRY.register("mithrill", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.EPIC)));
	public static final RegistryObject<Item> MITHRILL_NUGGET = REGISTRY.register("mithrill_nugget", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.EPIC)));
	public static final RegistryObject<Item> NEBULA_FRAMGMENT = REGISTRY.register("nebula_fragment", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.EPIC)));
	public static final RegistryObject<Item> PIECE_OF_NEBULA = REGISTRY.register("piece_of_nebula", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.EPIC)));
	public static final RegistryObject<Item> MAT_INSCRIBED_INGOT = REGISTRY.register("mat_inscribed_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.EPIC)));
	public static final RegistryObject<Item> DRAGON_CRYTAL = REGISTRY.register("dragoncrystal", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.EPIC)));
	public static final RegistryObject<Item> FOCUS_INFUSION = REGISTRY.register("focus_infusion", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.EPIC)));
	public static final RegistryObject<Item> NATURE_INGOT = REGISTRY.register("natureingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.EPIC)));
	public static final RegistryObject<Item> NICKEL_INGOT = REGISTRY.register("nickel_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.COMMON)));
	public static final RegistryObject<Item> PLATINUM_INGOT = REGISTRY.register("platinum_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.COMMON)));
	public static final RegistryObject<Item> PRISMATICRADIANCEINGOT = REGISTRY.register("prismaticradianceingot", () -> new ModIngot(INGOT_PROPERTIES.fireResistant().rarity(Rarity.EPIC)));
	public static final RegistryObject<Item> SPECTRITE_INGOT = REGISTRY.register("spectrite_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.EPIC)));
	public static final RegistryObject<Item> SPECTRITE_DUST = REGISTRY.register("spectrite_dust", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.EPIC)));
	public static final RegistryObject<Item> SPECTRITE_STAR = REGISTRY.register("spectrite_star", () -> new ModIngot(INGOT_PROPERTIES.fireResistant().rarity(ModRarities.MIRACLE)));
	public static final RegistryObject<Item> SPECTRITE_GEM = REGISTRY.register("spectrite_gem", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.EPIC)));
	public static final RegistryObject<Item> WHITE_DUST = REGISTRY.register("white_dust", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.COMMON)));
	public static final RegistryObject<Item> WOOD_INGOT = REGISTRY.register("wood_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.COMMON)));
	public static final RegistryObject<Item> CRYSTALLINE_STARDUST_INGOT = REGISTRY.register("crystalline_stardust_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(ModRarities.MIRACLE)));
	public static final RegistryObject<Item> STARLIGHT_CRYSTALLINE_INGOT = REGISTRY.register("starlight_crystalline_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(ModRarities.MIRACLE)));
	public static final RegistryObject<Item> TAINTED_ASTRAL_CRYSTAL_INGOT = REGISTRY.register("tainted_astral_crystal_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(ModRarities.MIRACLE)));
	public static final RegistryObject<Item> NEBULA_INGOT = REGISTRY.register("nebula_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(ModRarities.MIRACLE)));
	public static final RegistryObject<Item> NEBULA_STAR = REGISTRY.register("nebula_star", () -> new ModIngot(INGOT_PROPERTIES.rarity(ModRarities.MIRACLE)));
	public static final RegistryObject<Item> NEBULA_NUGGET = REGISTRY.register("nebula_nugget", () -> new ModIngot(INGOT_PROPERTIES.rarity(ModRarities.MIRACLE)));
	public static final RegistryObject<Item> PIECE_OF_WILD_HUNT = REGISTRY.register("piece_of_wild_hunt", () -> new ModIngot(INGOT_PROPERTIES.rarity(ModRarities.MIRACLE)));
	public static final RegistryObject<Item> WILD_HUNT_NUGGET = REGISTRY.register("wild_hunt_nugget", () -> new ModIngot(INGOT_PROPERTIES.rarity(ModRarities.MIRACLE)));
	public static final RegistryObject<Item> WILD_HUNT_FRAGMENT = REGISTRY.register("wild_hunt_fragment", () -> new ModIngot(INGOT_PROPERTIES.rarity(ModRarities.MIRACLE)));
	public static final RegistryObject<Item> WILD_HUNT_INGOT = REGISTRY.register("wild_hunt_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(ModRarities.MIRACLE)));
	public static final RegistryObject<Item> WILD_HUNT_STAR = REGISTRY.register("wild_hunt_star", () -> new ModIngot(INGOT_PROPERTIES.rarity(ModRarities.MIRACLE)));
	public static final RegistryObject<Item> PLAGUE_BANE_INGOT = REGISTRY.register("plague_bane_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(ModRarities.MIRACLE)));
	public static final RegistryObject<Item> DEAD_CORAL_INGOT = REGISTRY.register("dead_coral_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(ModRarities.MIRACLE)));
	public static final RegistryObject<Item> ETERNAL_CATALYST = REGISTRY.register("eternal_catalyst", () -> new ModIngot(INGOT_PROPERTIES.rarity(ModRarities.MIRACLE)));
	public static final RegistryObject<Item> GLACIAL_INGOT = REGISTRY.register("glacial_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(ModRarities.MIRACLE)));
	public static final RegistryObject<Item> HAOL_CATALYST = REGISTRY.register("halo_catalyst", () -> new ModIngot(INGOT_PROPERTIES.rarity(ModRarities.MIRACLE)));
	public static final RegistryObject<Item> RAINBOW_CATALYST = REGISTRY.register("rainbow_catalyst", () -> new ModIngot(INGOT_PROPERTIES.rarity(ModRarities.MIRACLE)));
	public static final RegistryObject<Item> FANTASTIC_CATALYST = REGISTRY.register("fantastic_catalyst", () -> new ModIngot(INGOT_PROPERTIES.rarity(ModRarities.MIRACLE)));




	//其他物品注册
	public static final RegistryObject<Item> SPECTRITE_CRYSTAL = REGISTRY.register("spectrite_crystal", SpectriteCrystal::new);
	public static final RegistryObject<Item> BIBLE = REGISTRY.register("bible", Bible::new);
	public static final RegistryObject<Item> IGNITER = REGISTRY.register("igniter", Igniter::new);
	public static final RegistryObject<Item> BEDROCK_BREAKER = REGISTRY.register("bedrock_breaker", BedrockBreaker::new);
	public static final RegistryObject<Item> MANA_IVY_REGEN = REGISTRY.register("mana_ivy_regen", ManaIvyRegen::new);
	public static final RegistryObject<Item> WEATHER_STONE = REGISTRY.register("weather_stone", WeatherStone::new);
	public static final RegistryObject<Item> DIMENSIONAL_MIRROR = REGISTRY.register("dimensional_mirror", DimensionalMirror::new);
	public static final RegistryObject<Item> OMEGA_CORE = REGISTRY.register("omega_core", OmegaCore::new);
	public static final RegistryObject<Item> CHAOS_CORE = REGISTRY.register("chaos_core", ChaosCore::new);
	public static final RegistryObject<Item> STARFLOWER_STONE = REGISTRY.register("starflowerstone", StarflowerStone::new);
	public static final RegistryObject<Item> RAINBOW_MANAITA = REGISTRY.register("rainbowmanaita", RainbowManaita::new);
	public static final RegistryObject<Item> HOLY_RING = REGISTRY.register("holy_ring", HolyRing::new);
	public static final RegistryObject<Item> STAR_FUEL = REGISTRY.register("starfuel", StarfuelItem::new);
	public static final RegistryObject<Item> KILLYOU = REGISTRY.register("killyou", KillyouItem::new);
	public static final RegistryObject<Item> EGG = REGISTRY.register("egg", RainBowEggItem::new);
	public static final RegistryObject<Item> MEMORIZE = REGISTRY.register("memorize", Memorize::new);
	public static final RegistryObject<Item> MIAOMIAOTOU = REGISTRY.register("miaomiaotou", MiaoMiaoTou::new);
	public static final RegistryObject<Item> LOPPING_PEARL = REGISTRY.register("lopping_pearl",LoppingPearl::new);
	public static final RegistryObject<Item> MERCURIAL_EYE = REGISTRY.register("mercurial_eye",MercurialEye::new);
	public static final RegistryObject<Item> HEARTH_STONE = REGISTRY.register("hearth_stone",HearthStone::new);
	public static final RegistryObject<Item> INFINITY_TOTEM = REGISTRY.register("infinity_totem",InfinityTotem::new);
	public static final RegistryObject<Item> RADIANT_SACRED_RUBY = REGISTRY.register("radiant_sacred_ruby", RadiantSacredRuby::new);
	public static final RegistryObject<Item> MANA_READER = REGISTRY.register("mana_reader", () -> new ManaReader(new Properties().rarity(Rarity.RARE)));
	public static final RegistryObject<Item> PLUMED_BELT = REGISTRY.register("plumed_belt", () -> new PlumedBelt(new Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA).rarity(Rarity.EPIC)));
	public static final RegistryObject<Item> IVY_REGEN = REGISTRY.register("ivy_regen", () -> new IvyRegen(new Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA)));
	public static final RegistryObject<Item> MANA_FLOWER = REGISTRY.register("mana_flower", ManaFlower::new);
	public static final RegistryObject<Item> INFINITY_ROD = REGISTRY.register("infinity_rod", InfinityRod::new);



	//奇点
	public static final RegistryObject<Item> ETERNAL_SINGULARITY = REGISTRY.register("eternal_singularity", ModSingularity::new);
	public static final RegistryObject<Item> RAINBOW_SINGULARITY = REGISTRY.register("rainbow_singularity", ModSingularity::new);
	public static final RegistryObject<Item> WOODEN_SINGULARITY = REGISTRY.register("wooden_singularity", ModSingularity::new);
	public static final RegistryObject<Item> MANYYULLYN_SINGULARITY = REGISTRY.register("manyyullyn_singularity", ModSingularity::new);
	public static final RegistryObject<Item> COBALT_SINGULARITY = REGISTRY.register("cobalt_singularity", ModSingularity::new);
	public static final RegistryObject<Item> GAIA_SINGULARITY = REGISTRY.register("gaia_singularity", ModSingularity::new);
	public static final RegistryObject<Item> BEDROCK_SINGULARITY = REGISTRY.register("bedrock_singularity", ModSingularity::new);
	public static final RegistryObject<Item> GOBBER_SINGULARITY_END = REGISTRY.register("gobber_singularity_end", ModSingularity::new);
	public static final RegistryObject<Item> WHITE_MATTER_SINGULARITY = REGISTRY.register("white_matter_singularity", ModSingularity::new);
	public static final RegistryObject<Item> POLONIUM_210_SINGULARITY = REGISTRY.register("polonium_singularity", ModSingularity::new);
	public static final RegistryObject<Item> MANAITA_SINGULARITY = REGISTRY.register("manaita_singularity", ModSingularity::new);
	public static final RegistryObject<Item> NEUTRON_SINGULARITY = REGISTRY.register("neutron_singularity", ModSingularity::new);
	public static final RegistryObject<Item> YUANSHI_SINGULARITY = REGISTRY.register("yuanshi_singularity", ModSingularity::new);
	public static final RegistryObject<Item> ORICHALCOS_SINGULARITY = REGISTRY.register("orichalcos_singularity", ModSingularity::new);
	public static final RegistryObject<Item> SHADOWIUM_SINGULARITY = REGISTRY.register("shadowium_singularity", ModSingularity::new);
	public static final RegistryObject<Item> CRYSTAL_MATRIX_SINGULARITY = REGISTRY.register("crystal_matrix_singularity", ModSingularity::new);
	public static final RegistryObject<Item> ETHERIUM_SINGULARITY = REGISTRY.register("etherium_singularity", ModSingularity::new);
	public static final RegistryObject<Item> EVIL_SINGULARITY = REGISTRY.register("evil_singularity", ModSingularity::new);
	public static final RegistryObject<Item> COMPRESSED_INFINITY_SINGULARITY = REGISTRY.register("compressed_infinity_singularity", ModSingularity::new);


	//决心
	public static final RegistryObject<Item> GREEN_DETERMINATION = REGISTRY.register("green_determination", ModDetermination::new);
	public static final RegistryObject<Item> RED_DETERMINATION = REGISTRY.register("red_determination", ModDetermination::new);
	public static final RegistryObject<Item> BLUE_DETERMINATION = REGISTRY.register("blue_determination", ModDetermination::new);
	public static final RegistryObject<Item> ORANGE_DETERMINATION = REGISTRY.register("orange_determination", ModDetermination::new);
	public static final RegistryObject<Item> YELLOW_DETERMINATION = REGISTRY.register("yellow_determination", ModDetermination::new);
	public static final RegistryObject<Item> CYAN_DETERMINATION = REGISTRY.register("cyan_determination", ModDetermination::new);
	public static final RegistryObject<Item> LIGHTER_PURPLE_DETERMINATION = REGISTRY.register("lighter_purple_determination", ModDetermination::new);
	public static final RegistryObject<Item> BLACK_DETERMINATION = REGISTRY.register("black_determination", ModDetermination::new);
	public static final RegistryObject<Item> PINK_DETERMINATION = REGISTRY.register("pink_determination", ModDetermination::new);
	public static final RegistryObject<Item> DEEPER_PURPLE_DETERMINATION = REGISTRY.register("deeper_purple_determination", ModDetermination::new);

	//符文
	public static final RegistryObject<Item> RadianceRune = REGISTRY.register("radiance_rune", ModRunes::new);
	public static final RegistryObject<Item> ShadowyRune = REGISTRY.register("shadowy_rune", ModRunes::new);
	public static final RegistryObject<Item> IllusoryWorldSpiritSovereignRune = REGISTRY.register("illusory_world_spirit_sovereign_rune", ModRunes::new);
	public static final RegistryObject<Item> MysticalSacredPactRune = REGISTRY.register("mystical_sacred_pact_rune", ModRunes::new);
	public static final RegistryObject<Item> StellarVaultDivineRevelationRune = REGISTRY.register("stellar_vault_divine_revelation_rune", ModRunes::new);
	public static final RegistryObject<Item> SereneMysteriousHeavenlyDecreeRune = REGISTRY.register("serene_mysterious_heavenly_decree_rune", ModRunes::new);
	public static final RegistryObject<Item> StellarRune = REGISTRY.register("stellar_rune", ModRunes::new);
	public static final RegistryObject<Item> HolyBloodCrystalRune = REGISTRY.register("holy_blood_crystal_rune", ModRunes::new);
	public static final RegistryObject<Item> VerdantLeafSpiritualRhymeRune = REGISTRY.register("verdant_leaf_spiritual_rhyme_rune", ModRunes::new);
	public static final RegistryObject<Item> DreamFeatherBlueButterflyRune = REGISTRY.register("dream_feather_blue_butterfly_rune", ModRunes::new);
	public static final RegistryObject<Item> MysteriousPurpleWisteriaSpiritRune = REGISTRY.register("mysterious_purple_wisteria_spirit_rune", ModRunes::new);

	public static final RegistryObject<Item> EMPTY_MANA_BUCKET = REGISTRY.register("empty_mana_bucket",
			() -> new ManaBucket(new Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA).stacksTo(16), false));

	public static final RegistryObject<Item> FILLED_MANA_BUCKET = REGISTRY.register("filled_mana_bucket",
			() -> new ManaBucket(new Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA).stacksTo(16), true));

	public static final RegistryObject<Item> SPRAWL_ROD = REGISTRY.register("sprawl_rod",
			() -> new SprawlRod(new Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA).rarity(Rarity.UNCOMMON)));

	public static final RegistryObject<Item> MITHRILL_MULTI_TOOL = REGISTRY.register("mithrill_multi_tool",
			() -> new MithrillMultiTool(new Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA).rarity(Rarity.EPIC)));

	public static final RegistryObject<Item> TERRA_SHOVEL = REGISTRY.register("terra_shovel",
			() -> new TerraShovel(new Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA).rarity(Rarity.EPIC)));

	public static final RegistryObject<Item> TERRA_HOE = REGISTRY.register("terra_hoe",
			() -> new TerraHoe(new Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA).rarity(Rarity.EPIC)));

	public static final RegistryObject<Item> RIDEABLE_PEARL = REGISTRY.register("rideable_pearl",
			() -> new RideablePearl(new Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_ITEM).durability(30)));

	public static final RegistryObject<Item> SPOTTED_GARDEN_EEL_BUCKET = REGISTRY.register("spotted_garden_eel_bucket",
			() -> new MobBucketItem(ModEntities.SPOTTED_GARDEN_EEL, () -> Fluids.WATER, () -> SoundEvents.BUCKET_EMPTY_FISH,
					new Properties().stacksTo(1)));

	public static final RegistryObject<Item> SACABAMBASPIS_BUCKET = REGISTRY.register("sacabambaspis_bucket",
			() -> new MobBucketItem(ModEntities.SACABAMBASPIS, () -> Fluids.WATER, () -> SoundEvents.BUCKET_EMPTY_FISH,
					new Properties().stacksTo(1)));

	public static final RegistryObject<Item> SPOTTED_GARDEN_EEL_SPAWN_EGG = REGISTRY.register("spotted_garden_eel_spawn_egg",
			() -> new ForgeSpawnEggItem(ModEntities.SPOTTED_GARDEN_EEL, 0x8BA673 , 0xCCC168,
					new Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_ITEM)));

	public static final RegistryObject<Item> SACABAMBASPIS_SPAWN_EGG = REGISTRY.register("sacabambaspis_spawn_egg",
			() -> new ForgeSpawnEggItem(ModEntities.SACABAMBASPIS, 0x8B0000, 0x808080,
					new Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_ITEM)));

	public static final RegistryObject<Item> SEA_SERPENT_SPAWN_EGG = REGISTRY.register("sea_serpent_spawn_egg",
			() -> new ForgeSpawnEggItem(ModEntities.SEA_SERPENT,  0x2E8B57, 0x98FB98,
					new Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_ITEM)));

	public static final RegistryObject<Item> CAPYBARA_SPAWN_EGG = REGISTRY.register("capybara_spawn_egg",
			() -> new ForgeSpawnEggItem(ModEntities.CAPYBARA, 0x8B4513, 0xDEB887,
					new Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_ITEM)));

	public static final RegistryObject<Item> XIAOYANG_010_SPAWN_EGG = REGISTRY.register("xiaoyang_010_spawn_egg",
			() -> new ForgeSpawnEggItem(ModEntities.XIAOYANG_010, -1, -1,
					new Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_ITEM)));

	public static final RegistryObject<Item> NEBULA_ROD = REGISTRY.register("nebula_rod",
			() -> new NebulaRod(new Properties()
					.tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA).rarity(Rarity.EPIC)));

	public static final RegistryObject<Item> MITHRILL_RING = REGISTRY.register("mithrill_ring",
			() -> new MithrillRing(new Properties()
					.tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA).rarity(Rarity.EPIC)));

	public static final RegistryObject<Item> NEBULA_RING = REGISTRY.register("nebula_ring",
			() -> new NebulaRing(new Properties()
					.tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA).rarity(Rarity.EPIC)));

	public static final RegistryObject<Item> ADVANCED_SPARK = REGISTRY.register("advanced_spark",
			() -> new AdvancedSpark(new Properties()
					.tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA)));

	public static final RegistryObject<Item> ADMIN_CONTROLLER = REGISTRY.register("admin_controller",
			() -> new AdminController(new Properties()
					.tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA)));


	//遗物
	public static final RegistryObject<Item> AESIR_RING = REGISTRY.register("aesir_ring",
			() -> new AesirRing(new Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA).stacksTo(1).rarity(Rarity.EPIC)));

	public static final RegistryObject<Item> FREYR_SLINGSHOT = REGISTRY.register("freyr_slingshot",
			() -> new FreyrSlingshot(new Properties().stacksTo(1).rarity(Rarity.EPIC).tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA)));

	public static final RegistryObject<Item> COLOURFUL_DICE = REGISTRY.register("colourful_dice",
			() -> new ColourfulDice(new Properties().stacksTo(1).rarity(ModRarities.MIRACLE).tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA)));

	public static final RegistryObject<Item> SHINY_STONE = REGISTRY.register("shiny_stone",
			() -> new ShinyStone(new Properties().stacksTo(1).rarity(Rarity.EPIC).tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA).fireResistant()));

	public static final RegistryObject<Item> ELDRITCH_SPELL = REGISTRY.register("eldritch_spell",
			() -> new EldritchSpell(new Properties().stacksTo(1).rarity(Rarity.EPIC).tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA)));

	public static final RegistryObject<Item> CRIMSON_SPELL = REGISTRY.register("crimson_spell",
			() -> new CrimsonSpell(new Properties().stacksTo(1).rarity(Rarity.EPIC).tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA)));

	public static final RegistryObject<Item> FALSE_JUSTICE = REGISTRY.register("false_justice",
			() -> new FalseJustice(new Properties().stacksTo(1).rarity(Rarity.EPIC).tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA)));

	public static final RegistryObject<Item> XP_TOME = REGISTRY.register("xp_tome",
			() -> new XPTome(new Properties().stacksTo(1).rarity(Rarity.EPIC).tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA)));

	public static final RegistryObject<Item> ANCIENT_AEGIS = REGISTRY.register("ancient_aegis",
			() -> new AncientAegis(new Properties().stacksTo(1).rarity(Rarity.EPIC).tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA)));

	public static final RegistryObject<Item> GHASTLY_SKULL = REGISTRY.register("ghastly_skull",
			() -> new GhastlySkull(new Properties().stacksTo(1).rarity(Rarity.EPIC).tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA)));

	public static final RegistryObject<Item> CHAO_TOME = REGISTRY.register("chao_tome",
			() -> new ChaosTome(new Properties().stacksTo(1).rarity(Rarity.EPIC).tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA)));

	public static final RegistryObject<Item> OVERTHROWER = REGISTRY.register("overthrower",
			() -> new Overthrower(new Properties().stacksTo(1).rarity(Rarity.EPIC).tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA)));

	public static final RegistryObject<Item> MANAITA_SHEARS = REGISTRY.register("manaita_shears",
			() -> new ManaitaShears(new Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA).rarity(Rarity.EPIC).durability(Integer.MAX_VALUE)));

	public static final RegistryObject<Item> MANAITA = REGISTRY.register("manaita",
			() -> new Manaita(new Properties().stacksTo(1).rarity(Rarity.EPIC).tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA)));

	public static final RegistryObject<Item> MISSILE_TOME = REGISTRY.register("missile_tome",
			() -> new MissileTome(new Properties().stacksTo(1).rarity(Rarity.EPIC).tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA)));

	public static final RegistryObject<Item> APOTHEOSIS = REGISTRY.register("apotheosis",
			() -> new Apotheosis(new Properties().stacksTo(1).rarity(Rarity.EPIC).tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA)));

	public static final RegistryObject<Item> LUNAR_FLARE = REGISTRY.register("lunar_flare",
			() -> new LunarFlares(new Properties().stacksTo(1).rarity(Rarity.EPIC).tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA)));

	public static final RegistryObject<Item> DEIFIC_AMULET = REGISTRY.register("deific_amulet",
			() -> new DeificAmulet(new Properties().stacksTo(1).rarity(Rarity.EPIC).tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA)));

	public static final RegistryObject<Item> THUNDER_PEAL = REGISTRY.register("thunder_peal",
			() -> new Thunderpeal(new Properties().stacksTo(1).rarity(Rarity.EPIC).tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA)));

	public static final RegistryObject<Item> SOUL_TOME = REGISTRY.register("soul_tome",
			() -> new SoulTome(new Properties().stacksTo(1).rarity(Rarity.EPIC).tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA)));

	public static final RegistryObject<Item> FATE_TOME = REGISTRY.register("fate_tome",
			() -> new FateTome(new Properties().stacksTo(1).rarity(Rarity.EPIC).tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA)));

	public static final RegistryObject<Item> VOID_GRIMOIRE = REGISTRY.register("void_grimoire",
			() -> new VoidGrimoire(new Properties().stacksTo(1).rarity(Rarity.EPIC).tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA)));

	public static final RegistryObject<Item> SUPERPOSITION_RING = REGISTRY.register("superposition_ring",
			() -> new SuperpositionRing(new Properties().stacksTo(1).rarity(Rarity.EPIC).tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA)));

	public static final RegistryObject<Item> DORMANT_ARCANUM = REGISTRY.register("dormant_arcanum",
			() -> new DormantArcanum(new Properties().stacksTo(1).rarity(Rarity.EPIC).tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA)));

	public static final RegistryObject<Item> NEBULOUS_CORE = REGISTRY.register("nebulous_core", () -> new NebulousCore(
			new Properties().stacksTo(1).rarity(Rarity.EPIC).tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA)));

	public static final RegistryObject<Item> TELEKINESIS_TOME = REGISTRY.register("telekinesis_tome",
			() -> new TelekinesisTome(new Properties().stacksTo(1).rarity(Rarity.EPIC).tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA)));

	public static final RegistryObject<Item> DARK_SUN_RING = REGISTRY.register("dark_sun_ring",
			() -> new DarkSunRing(new Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA).stacksTo(1).rarity(Rarity.EPIC)));

	public static final RegistryObject<Item> FLOWER_FINDER_WAND = REGISTRY.register("flower_finder_wand",
			() -> new FlowerFinderWand(new Properties()
					.tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA).stacksTo(1).setNoRepair().rarity(Rarity.EPIC)));

	public static final RegistryObject<Item> GOLDEN_LAUREL = REGISTRY.register("golden_laurel",
			() -> new GoldenLaurel(new Properties()
					.tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA).stacksTo(1).rarity(ModRarities.MIRACLE)));

	public static final RegistryObject<Item> ANTIGRAVITY_CHARM = REGISTRY.register("antigravity_charm",
			() -> new AntigravityCharm(new Properties()
					.tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA).stacksTo(1).rarity(Rarity.EPIC).setNoRepair()));

	public static final RegistryObject<Item> TALISMAN_HIDDEN_RICHES = REGISTRY.register("talisman_hidden_riches",
				() -> new TalismanHiddenRiches(new Properties()
					.tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA) .stacksTo(1).setNoRepair().rarity(Rarity.EPIC)));

	public static final RegistryObject<Item> BLACK_HALO = REGISTRY.register("black_halo",
			() -> new BlackHalo(new Properties()
					.tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA).stacksTo(1).rarity(Rarity.EPIC)));

	public static final RegistryObject<Item> POCKET_WARDROBE = REGISTRY.register("pocket_wardrobe",
			() -> new PocketWardrobe(new Properties()
					.tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA).stacksTo(1).rarity(Rarity.EPIC)));

	private static RegistryObject<Item> block(RegistryObject<Block> block, CreativeModeTab tab) {
		return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Properties().tab(tab)));
	}

	public static RegistryObject<Item> blockFlower(RegistryObject<Block> block, CreativeModeTab tab) {
		return REGISTRY.register(block.getId().getPath(), () -> new ItemBlockFlower(block.get(), new Properties().tab(tab)));
	}

	public static RegistryObject<Item> blockHyFlower(RegistryObject<Block> block, CreativeModeTab tab) {
		return REGISTRY.register(block.getId().getPath(), () -> new ItemHybridSpecialFlower(block.get(), new Properties().tab(tab)));
	}
}
