
package net.xiaoyang010.ex_enigmaticlegacy.Init;

import net.xiaoyang010.ex_enigmaticlegacy.Compat.curio.HolyRing;
import net.xiaoyang010.ex_enigmaticlegacy.Item.*;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.common.ForgeSpawnEggItem;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.BlockItem;
import net.xiaoyang010.ex_enigmaticlegacy.Item.ingot.*;
import vazkii.botania.common.item.block.ItemBlockSpecialFlower;

public class ModItems {
	public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, ExEnigmaticlegacyMod.MODID);

	public static final RegistryObject<Item> IRIDIUM_ORE = block(ModBlockss.IRIDIUM_ORE, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> NICKEL_ORE = block(ModBlockss.NICKEL_ORE, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> PLATINUM_ORE = block(ModBlockss.PLATINUM_ORE, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> END_ORE = block(ModBlockss.END_ORE, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);


	public static final RegistryObject<Item> IRIDIUM_BLOCK = block(ModBlockss.IRIDIUM_BLOCK, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> NICKEL_BLOCK = block(ModBlockss.NICKEL_BLOCK, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> AERIALITE_BLOCK = block(ModBlockss.AERIALITE_BLOCK, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);


	public static final RegistryObject<Item> ASTRAL_KILLOP = blockFlower(ModBlockss.ASTRAL_KILLOP, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> NIGHTSHADE = blockFlower(ModBlockss.NIGHTSHADE, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> DAYBLOOM = blockFlower(ModBlockss.DAYBLOOM, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> BELIEVE = blockFlower(ModBlockss.BELIEVER, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> GENENERGYDANDRON = block(ModBlockss.GENENERGYDANDRON, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> FLUFFY_DANDELION = block(ModBlockss.FLUFFY_DANDELION, ModTabs.TAB_EXENIGMATICLEGACY_ITEM);
	public static final RegistryObject<Item> CELESTIAL_HOLINESS_TRANSMUTER = block(ModBlockss.CELESTIAL_HOLINESS_TRANSMUTER, ModTabs.TAB_EXENIGMATICLEGACY_ITEM);
	public static final RegistryObject<Item> PRISMATICRADIANCEBLOCK = block(ModBlockss.PRISMATICRADIANCEBLOCK, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> ORECHIDENDIUM = block(ModBlockss.ORECHIDENDIUM, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> INFINITYGLASS = block(ModBlockss.INFINITYGlASS, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> BLOCKNATURE = block(ModBlockss.BLOCKNATURE, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> ASGARDANDELION = blockFlower(ModBlockss.ASGARDANDELION, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> EVILBLOCK = block(ModBlockss.EVILBLOCK, ModTabs.TAB_EXENIGMATICLEGACY_BLOCK);
	public static final RegistryObject<Item> ICHEST = block(ModBlockss.INFINITYCHEST, ModTabs.TAB_EXENIGMATICLEGACY_ITEM);
	public static final RegistryObject<Item> SOARLEANDER = block(ModBlockss.SOARLEANDER, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> STARLITSANCTUM = block(ModBlockss.STARLITSANCTUM, ModTabs.TAB_EXENIGMATICLEGACY_ITEM);
	public static final RegistryObject<Item> COBBLE_STONE = block(ModBlockss.COBBLE_STONE, ModTabs.TAB_EXENIGMATICLEGACY_ITEM);
	public static final RegistryObject<Item> INFINITY_POTATO = block(ModBlockss.INFINITY_POTATO, ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> FLOWEY = block(ModBlockss.FLOWEY,ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);
	public static final RegistryObject<Item> WITCH_OPOOD = block(ModBlockss.WITCH_OPOOD,ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA);


	public static final RegistryObject<Item> IRIDIUM_INGOT = REGISTRY.register("iridium_ingot", () -> new IridiumIngot());
	public static final RegistryObject<Item> NICKEL_INGOT = REGISTRY.register("nickel_ingot", () -> new NickelIngot());
	public static final RegistryObject<Item> PLATINUM_INGOT = REGISTRY.register("platinum_ingot", () -> new PlatinumIngot());

	public static final RegistryObject<Item> MANA_NETHER_STAR = REGISTRY.register("mana_nether_star", () -> new ManaNetherStar());
	public static final RegistryObject<Item> END_CRYSTAIC = REGISTRY.register("end_crystaic", () -> new EndCrystaic());
	public static final RegistryObject<Item> CUSTOM_NUGGET = REGISTRY.register("custom_nugget", () -> new CustomNugget());
	public static final RegistryObject<Item> ASTRAL_PILE = REGISTRY.register("astral_pile", () -> new AstralPile());
	public static final RegistryObject<Item> ASTRAL_NUGGET = REGISTRY.register("astral_nugget", () -> new AstralNugget());
	public static final RegistryObject<Item> ASTRAL_INGOT = REGISTRY.register("astral_ingot", () -> new AstralInogt());
	public static final RegistryObject<Item> CUSTOM_INGOT = REGISTRY.register("custom_ingot", () -> new CustomIngot());
	public static final RegistryObject<Item> SPECTRITE_INGOT = REGISTRY.register("spectrite_ingot", () -> new SpectriteIngot());
	public static final RegistryObject<Item> NATUREINGOT = REGISTRY.register("natureingot", () -> new Natureingot());
	public static final RegistryObject<Item> PRISMATICRADIANCEINGOT = REGISTRY.register("prismaticradianceingot", () -> new PrismaticRadianceIngot());
	public static final RegistryObject<Item> ICE_INGOT = REGISTRY.register("ice_ingot", () -> new IceIngot());
	public static final RegistryObject<Item> DEATH_INGOT =REGISTRY.register("death_ingot", () -> new DeathIngot());
	public static final RegistryObject<Item> DIRT_INGOT = REGISTRY.register("dirt_ingot", () -> new DirtIngot());
	public static final RegistryObject<Item> ELECTRIC_INGOT = REGISTRY.register("electric_ingot", () -> new ElectricIngot());
	public static final RegistryObject<Item> GRASS_INGOT = REGISTRY.register("grass_ingot", () -> new GrassIngot());
	public static final RegistryObject<Item> LAVA_INGOT = REGISTRY.register("lava_ingot", () -> new LavaIngot());
	public static final RegistryObject<Item> WOOD_INGOT = REGISTRY.register("wood_ingot", () -> new WoodIngot());
	public static final RegistryObject<Item> INGOT_ANIMATION = REGISTRY.register("ingot_animation", () -> new IngotAnimation());
	public static final RegistryObject<Item> OBSCURE = REGISTRY.register("obscure", () -> new Obscure());
	public static final RegistryObject<Item> COLORFUL_SHADOW_SHARD = REGISTRY.register("colorful_shadow_shard", () -> new ColorfulShadowShard());
	public static final RegistryObject<Item> SPECTRITE_CRYSTAL = REGISTRY.register("spectrite_crystal", () -> new SpectriteCrystal());
	public static final RegistryObject<Item> DYEABLE_REDSTONE = REGISTRY.register("dyeable_redstone", () -> new DyeableRedstone());


	public static final RegistryObject<Item> BIBLE = REGISTRY.register("bible", () -> new Bible());

	public static final RegistryObject<Item> IGNITER = REGISTRY.register("igniter", () -> new Igniter());

	public static final RegistryObject<Item> WHITE_DUST = REGISTRY.register("white_dust", () -> new WhiteDust());
	public static final RegistryObject<Item> BEDROCK_BREAKER = REGISTRY.register("bedrock_breaker", () -> new BedrockBreaker());
	public static final RegistryObject<Item> MEMORIZE = REGISTRY.register("memorize", () -> new Memorize());
	public static final RegistryObject<Item> SPECTRITE_DUST = REGISTRY.register("spectrite_dust", () -> new SpectriteDust());
	public static final RegistryObject<Item> SPECTRITE_STAR = REGISTRY.register("spectrite_star", () -> new SpectriteStar());
	public static final RegistryObject<Item> SPECTRITE_GEM = REGISTRY.register("spectrite_gem", () -> new SpectriteGem());
	public static final RegistryObject<Item> IVYREGEN = REGISTRY.register("ivyregen", () -> new RegenIvy());
	public static final RegistryObject<Item> WEATHERSTONE = REGISTRY.register("weather_stone", () -> new WeatherStone());
	public static final RegistryObject<Item> DIMENSIONALMIRROR = REGISTRY.register("dimensional_mirror", () -> new DimensionalMirror());
	public static final RegistryObject<Item> GHASTLYSKULL = REGISTRY.register("ghastly_skull", () -> new GhastlySkull());
	public static final RegistryObject<Item> NEBULOUSCORE = REGISTRY.register("nebulous_core", () -> new NebulousCore());
	public static final RegistryObject<Item> OMEGACORE = REGISTRY.register("omega_core", () -> new OmegaCore());
	public static final RegistryObject<Item> CHAOSCORE = REGISTRY.register("chaos_core", () -> new ChaosCore());
	public static final RegistryObject<Item> SHINYSTONE = REGISTRY.register("shiny_stone", () -> new ShinyStone());
	public static final RegistryObject<Item> STARFLOWERSTONE = REGISTRY.register("starflowerstone", () -> new StarflowerStone());
	public static final RegistryObject<Item> RAINBOWMANAITA = REGISTRY.register("rainbowmanaita", () -> new RainbowManaita());
	public static final RegistryObject<Item> HOLY_RING = REGISTRY.register("holy_ring", () -> new HolyRing());
	public static final RegistryObject<Item> INFINITYDROP = REGISTRY.register("infinitydrop", () -> new InfinityDrop());
	public static final RegistryObject<Item> STARFUEL = REGISTRY.register("starfuel", () -> new StarfuelItem());
	public static final RegistryObject<Item> KILLYOU = REGISTRY.register("killyou", () -> new KillyouItem());
	public static final RegistryObject<Item> MIAOMIAOTOU = REGISTRY.register("miaomiaotou", () -> new MiaomiaotouItem());
	public static final RegistryObject<Item> EGG = REGISTRY.register("egg", () -> new RainBowEggItem());
	public static final RegistryObject<Item> INFINITY_MATTER = REGISTRY.register("infinity_matter", () -> new InfinityMatter());
	/*public static final RegistryObject<Item> LOLIPICKAXE = REGISTRY.register("loli_pickaxe", () -> new LolipickaxeItem());
	public static final RegistryObject<Item> DEATHITEM = REGISTRY.register("loli_death",()-> new DeathItem(new Item.Properties()));*/




	public static final RegistryObject<Item> XIAOYANG_010_SPAWN_EGG = REGISTRY.register("xiaoyang_010_spawn_egg",
			() -> new ForgeSpawnEggItem(ModEntities.XIAOYANG_010, -1, -1,
					new Item.Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_ITEM)));

	/*public static final RegistryObject<Item> SACABAMBASPIS_SPAWN_EGG = REGISTRY.register("sacabambaspis_spawn_egg",
			() -> new ForgeSpawnEggItem(ModEntities.SACABAMBASPIS, 0xFF0000, 0xAA0000,
					new Item.Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_ITEM)));*/


	private static RegistryObject<Item> block(RegistryObject<Block> block, CreativeModeTab tab) {
		return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties().tab(tab)));
	}

	private static RegistryObject<Item> blockFlower(RegistryObject<Block> block, CreativeModeTab tab) {
		return REGISTRY.register(block.getId().getPath(), () -> new ItemBlockSpecialFlower(block.get(), new Item.Properties().tab(tab)));
	}
}
