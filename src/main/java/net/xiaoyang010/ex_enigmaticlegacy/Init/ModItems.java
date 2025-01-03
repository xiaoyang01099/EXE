
package net.xiaoyang010.ex_enigmaticlegacy.Init;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.GoldenLaurel;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.ManaReader;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.PlumedBelt;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.ItemBlockFlower;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.curio.HolyRing;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Item.*;
import net.xiaoyang010.ex_enigmaticlegacy.Item.all.ModAmorphous;
import net.xiaoyang010.ex_enigmaticlegacy.Item.all.ModDetermination;
import net.xiaoyang010.ex_enigmaticlegacy.Item.all.ModIngot;

public class ModItems {
	public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, ExEnigmaticlegacyMod.MODID);

	public static final RegistryObject<Item> IRIDIUM_ORE = block(ModBlocks.IRIDIUM_ORE, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> NICKEL_ORE = block(ModBlocks.NICKEL_ORE, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> PLATINUM_ORE = block(ModBlocks.PLATINUM_ORE, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> END_ORE = block(ModBlocks.END_ORE, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> AMETHYST_ORE = block(ModBlocks.AMETHYST_ORE, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> BAUXITE_ORE = block(ModBlocks.BAUXITE_ORE, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> CHROMITE_ORE = block(ModBlocks.CHROMITE_ORE, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> FLUORITE_ORE = block(ModBlocks.FLUORITE_ORE, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> GYPSUM_ORE = block(ModBlocks.GYPSUM_ORE, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> JADE_ORE = block(ModBlocks.JADE_ORE, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> LEAD_ORE = block(ModBlocks.LEAD_ORE, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> BRONZE_ORE = block(ModBlocks.BRONZE_ORE, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> OPAL_ORE = block(ModBlocks.OPAL_ORE, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);

	public static final RegistryObject<Item> ARCANE_ICE_CHUNK = block(ModBlocks.ARCANE_ICE_CHUNK, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> DECAY_BLOCK = block(ModBlocks.DECAY_BLOCK, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> IRIDIUM_BLOCK = block(ModBlocks.IRIDIUM_BLOCK, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> NICKEL_BLOCK = block(ModBlocks.NICKEL_BLOCK, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> AERIALITE_BLOCK = block(ModBlocks.AERIALITE_BLOCK, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);


	public static final RegistryObject<Item> infinitySpreader = block(ModBlocks.infinitySpreader, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);

	public static final RegistryObject<Item> FROST_LOTUS = blockFlower(ModBlocks.FROST_LOTUS, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> DARK_NIGHT_GRASS = blockFlower(ModBlocks.DARK_NIGHT_GRASS, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> ASTRAL_KILLOP = blockFlower(ModBlocks.ASTRAL_KILLOP, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> KILLING_BERRY = blockFlower(ModBlocks.KILLING_BERRY, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> NIGHTSHADE = blockFlower(ModBlocks.NIGHTSHADE, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> DAYBLOOM = blockFlower(ModBlocks.DAYBLOOM, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> BELIEVE = blockFlower(ModBlocks.BELIEVER, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> GENENERGYDANDRON = block(ModBlocks.GENENERGYDANDRON, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> FLUFFY_DANDELION = block(ModBlocks.FLUFFY_DANDELION, ModTabs.TAB_EXENIGMATICLEGACY_ITEM);
	public static final RegistryObject<Item> CELESTIAL_HOLINESS_TRANSMUTER = block(ModBlocks.CELESTIAL_HOLINESS_TRANSMUTER, ModTabs.TAB_EXENIGMATICLEGACY_ITEM);
	public static final RegistryObject<Item> PRISMATICRADIANCEBLOCK = block(ModBlocks.PRISMATICRADIANCEBLOCK, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> ORECHIDENDIUM = block(ModBlocks.ORECHIDENDIUM, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> INFINITYGLASS = block(ModBlocks.INFINITYGlASS, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> BLOCKNATURE = block(ModBlocks.BLOCKNATURE, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> ASGARDANDELION = blockFlower(ModBlocks.ASGARDANDELION, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> EVILBLOCK = block(ModBlocks.EVILBLOCK, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> ICHEST = block(ModBlocks.INFINITYCHEST, ModTabs.TAB_EXENIGMATICLEGACY_ITEM);
	public static final RegistryObject<Item> SOARLEANDER = block(ModBlocks.SOARLEANDER, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> STARLITSANCTUM = block(ModBlocks.STARLITSANCTUM, ModTabs.TAB_EXENIGMATICLEGACY_ITEM);
	public static final RegistryObject<Item> COBBLE_STONE = block(ModBlocks.COBBLE_STONE, ModTabs.TAB_EXENIGMATICLEGACY_ITEM);
	public static final RegistryObject<Item> INFINITY_POTATO = block(ModBlocks.INFINITY_POTATO, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> FLOWEY = block(ModBlocks.FLOWEY,ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> WITCH_OPOOD = block(ModBlocks.WITCH_OPOOD,ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> LYCORISRADIATA = block(ModBlocks.LYCORISRADIATA,ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);



	//31个物品通用属性
	private static final Properties INGOT_PROPERTIES = new Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_MINERAL).stacksTo(64);

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
	public static final RegistryObject<Item> GHASTLYSKULL = REGISTRY.register("ghastly_skull", () -> new ModIngot(INGOT_PROPERTIES.fireResistant().rarity(Rarity.EPIC)));
	public static final RegistryObject<Item> IRIDIUM_INGOT = REGISTRY.register("iridium_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.COMMON)));
	public static final RegistryObject<Item> ICE_INGOT = REGISTRY.register("ice_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.COMMON)));
	public static final RegistryObject<Item> INGOT_ANIMATION = REGISTRY.register("ingot_animation", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.COMMON)));
	public static final RegistryObject<Item> INFINITYDROP = REGISTRY.register("infinitydrop", () -> new ModIngot(INGOT_PROPERTIES.fireResistant().rarity(Rarity.EPIC)));
	public static final RegistryObject<Item> LAVA_INGOT = REGISTRY.register("lava_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.COMMON)));
	public static final RegistryObject<Item> MANA_NETHER_STAR = REGISTRY.register("mana_nether_star", () -> new ModIngot(INGOT_PROPERTIES.fireResistant().rarity(Rarity.EPIC)));

	public static final RegistryObject<Item> NATUREINGOT = REGISTRY.register("natureingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.EPIC)));
	public static final RegistryObject<Item> NICKEL_INGOT = REGISTRY.register("nickel_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.COMMON)));
	public static final RegistryObject<Item> OBSCURE = REGISTRY.register("obscure", () -> new ModIngot(INGOT_PROPERTIES.fireResistant().rarity(ModRarities.MIRACLE)));
	public static final RegistryObject<Item> PLATINUM_INGOT = REGISTRY.register("platinum_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.COMMON)));
	public static final RegistryObject<Item> PRISMATICRADIANCEINGOT = REGISTRY.register("prismaticradianceingot", () -> new ModIngot(INGOT_PROPERTIES.fireResistant().rarity(Rarity.EPIC)));
	public static final RegistryObject<Item> SPECTRITE_INGOT = REGISTRY.register("spectrite_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.EPIC)));
	public static final RegistryObject<Item> SPECTRITE_DUST = REGISTRY.register("spectrite_dust", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.EPIC)));
	public static final RegistryObject<Item> SPECTRITE_STAR = REGISTRY.register("spectrite_star", () -> new ModIngot(INGOT_PROPERTIES.fireResistant().rarity(ModRarities.MIRACLE)));
	public static final RegistryObject<Item> SPECTRITE_GEM = REGISTRY.register("spectrite_gem", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.EPIC)));
	public static final RegistryObject<Item> WHITE_DUST = REGISTRY.register("white_dust", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.COMMON)));
	public static final RegistryObject<Item> WOOD_INGOT = REGISTRY.register("wood_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.COMMON)));

	public static final RegistryObject<Item> SPECTRITE_CRYSTAL = REGISTRY.register("spectrite_crystal", SpectriteCrystal::new);
	public static final RegistryObject<Item> BIBLE = REGISTRY.register("bible", Bible::new);
	public static final RegistryObject<Item> IGNITER = REGISTRY.register("igniter", Igniter::new);
	public static final RegistryObject<Item> BEDROCK_BREAKER = REGISTRY.register("bedrock_breaker", BedrockBreaker::new);
	public static final RegistryObject<Item> IVYREGEN = REGISTRY.register("ivyregen", RegenIvy::new);
	public static final RegistryObject<Item> WEATHERSTONE = REGISTRY.register("weather_stone", WeatherStone::new);
	public static final RegistryObject<Item> DIMENSIONALMIRROR = REGISTRY.register("dimensional_mirror", DimensionalMirror::new);
	public static final RegistryObject<Item> NEBULOUSCORE = REGISTRY.register("nebulous_core", NebulousCore::new);
	public static final RegistryObject<Item> OMEGACORE = REGISTRY.register("omega_core", OmegaCore::new);
	public static final RegistryObject<Item> CHAOSCORE = REGISTRY.register("chaos_core", ChaosCore::new);
	public static final RegistryObject<Item> SHINYSTONE = REGISTRY.register("shiny_stone", ShinyStone::new);
	public static final RegistryObject<Item> STARFLOWERSTONE = REGISTRY.register("starflowerstone", StarflowerStone::new);
	public static final RegistryObject<Item> RAINBOWMANAITA = REGISTRY.register("rainbowmanaita", RainbowManaita::new);
	public static final RegistryObject<Item> HOLY_RING = REGISTRY.register("holy_ring", HolyRing::new);
	public static final RegistryObject<Item> STARFUEL = REGISTRY.register("starfuel", StarfuelItem::new);
	public static final RegistryObject<Item> KILLYOU = REGISTRY.register("killyou", KillyouItem::new);
	public static final RegistryObject<Item> EGG = REGISTRY.register("egg", RainBowEggItem::new);
	public static final RegistryObject<Item> INFINITY_MATTER = REGISTRY.register("infinity_matter", InfinityMatter::new);
	public static final RegistryObject<Item> MEMORIZE = REGISTRY.register("memorize", Memorize::new);
	public static final RegistryObject<Item> MIAOMIAOTOU = REGISTRY.register("miaomiaotou", MiaoMiaoTou::new);
	public static final RegistryObject<Item> LOPPING_PEARL = REGISTRY.register("lopping_pearl",LoppingPearl::new);
	public static final RegistryObject<Item> MERCURIAL_EYE = REGISTRY.register("mercurial_eye",MercurialEye::new);
	public static final RegistryObject<Item> HEARTHSTONE = REGISTRY.register("hearth_stone",HearthStone::new);
	public static final RegistryObject<Item> INFINITY_TOTEM = REGISTRY.register("infinity_totem",InfinityTotem::new);
	public static final RegistryObject<Item> RADIANT_SACRED_RUBY = REGISTRY.register("radiant_sacred_ruby", RadiantSacredRuby::new);
	public static final RegistryObject<Item> MANA_READER = REGISTRY.register("mana_reader", () -> new ManaReader(new Properties().rarity(Rarity.RARE)));
	public static final RegistryObject<Item> PLUMED_BELT = REGISTRY.register("plumed_belt", () -> new PlumedBelt(new Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_ITEM).rarity(Rarity.RARE)));
	public static final RegistryObject<Item> GOLDEN_LAUREL = REGISTRY.register("golden_laurel", () -> new GoldenLaurel(new Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_ITEM).stacksTo(1).rarity(Rarity.RARE)));


	//材料
	public static final RegistryObject<Item> MATTER_AMORPHOUS = REGISTRY.register("matter_amorphous_max", ModAmorphous::new);
	public static final RegistryObject<Item> MATTER_CORPOREAL = REGISTRY.register("matter_corporeal_max", ModAmorphous::new);
	public static final RegistryObject<Item> MATTER_DARK = REGISTRY.register("matter_dark_max", ModAmorphous::new);
	public static final RegistryObject<Item> MATTER_ESSENTIA = REGISTRY.register("matter_essentia_max", ModAmorphous::new);
	public static final RegistryObject<Item> MATTER_KINETIC = REGISTRY.register("matter_kinetic_max", ModAmorphous::new);
	public static final RegistryObject<Item> MATTER_OMNI = REGISTRY.register("matter_omni_max", ModAmorphous::new);
	public static final RegistryObject<Item> MATTER_PROTO = REGISTRY.register("matter_proto_max", ModAmorphous::new);
	public static final RegistryObject<Item> MATTER_TEMPORAL = REGISTRY.register("matter_temporal_max", ModAmorphous::new);
	public static final RegistryObject<Item> MATTER_VOID = REGISTRY.register("matter_void_max", ModAmorphous::new);



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




	public static final RegistryObject<Item> XIAOYANG_010_SPAWN_EGG = REGISTRY.register("xiaoyang_010_spawn_egg",
			() -> new ForgeSpawnEggItem(ModEntities.XIAOYANG_010, -1, -1,
					new Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_ITEM)));


	private static RegistryObject<Item> block(RegistryObject<Block> block, CreativeModeTab tab) {
		return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Properties().tab(tab)));
	}

	private static RegistryObject<Item> blockFlower(RegistryObject<Block> block, CreativeModeTab tab) {
		return REGISTRY.register(block.getId().getPath(), () -> new ItemBlockFlower(block.get(), new Properties().tab(tab)));
	}
}
