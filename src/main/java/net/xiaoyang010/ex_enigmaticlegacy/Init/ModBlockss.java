
package net.xiaoyang010.ex_enigmaticlegacy.Init;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.Material;
import net.xiaoyang010.ex_enigmaticlegacy.Block.*;
import net.xiaoyang010.ex_enigmaticlegacy.Block.custom.CustomSaplingBlock;
import net.xiaoyang010.ex_enigmaticlegacy.Block.ore.*;
import net.xiaoyang010.ex_enigmaticlegacy.Block.AstralBlock;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Avaritia.CosmicBlock;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.*;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.InfinityGaiaSpreader.VariantE;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerBlock.Functional.AstralKillop;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerBlock.Generating.Catnip;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraft.world.level.block.Block;
import net.xiaoyang010.ex_enigmaticlegacy.Block.StarlitSanctumOfMystique;
import net.xiaoyang010.ex_enigmaticlegacy.Block.portal.AnotherPortalBlock;
import net.xiaoyang010.ex_enigmaticlegacy.Block.portal.MinersHeavenPortalBlock;
import net.xiaoyang010.ex_enigmaticlegacy.Block.BlockExtremeAutoCrafter;
import net.xiaoyang010.ex_enigmaticlegacy.Block.BlockInfinityCompressor;
import vazkii.botania.common.block.BlockSpecialFlower;
import static vazkii.botania.common.block.ModBlocks.livingrock;

public class ModBlockss {
	public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, ExEnigmaticlegacyMod.MODID);
	public static final Properties FLOWER_PROPS = Properties.copy(Blocks.POPPY);
	private static final BlockBehaviour.StateArgumentPredicate<EntityType<?>> NO_SPAWN = (state, world, pos, et) -> false;

	//botania联动
	//方块放置
	public static final RegistryObject<Block> NIGHTSHADE = REGISTRY.register("nightshade",() -> new BlockSpecialFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.NIGHTSHADE_TILE::get));
	public static final RegistryObject<Block> ASTRAL_KILLOP = REGISTRY.register("astral_killop",() -> new AstralKillop(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.ASTRAL_KILLOP_TILE::get));
	public static final RegistryObject<Block> DAYBLOOM = REGISTRY.register("daybloom",() -> new BlockSpecialFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.DAYBLOOM_TILE::get));
	public static final RegistryObject<Block> ASGARDANDELION = REGISTRY.register("asgardandelion",() -> new BlockSpecialFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.ASGARDANDELIONTILE::get));
	public static final RegistryObject<Block> FLOWEY = REGISTRY.register("flowey",() -> new BlockSpecialFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.FLOWEYTILE::get));
    public static final RegistryObject<Block> BELIEVER = REGISTRY.register("believer",() -> new BlockSpecialFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.BELIEVERTILE::get));
	public static final RegistryObject<Block> SOARLEANDER = REGISTRY.register("soarleander", () -> new BlockSpecialFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.SOARLEANDERTILE::get));
	public static final RegistryObject<Block> ORECHIDENDIUM = REGISTRY.register("orechid_endium", () -> new BlockSpecialFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.ORECHIDENDIUMTILE::get));
	public static final RegistryObject<Block> WITCH_OPOOD = REGISTRY.register("witch_opood", () -> new BlockSpecialFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.WITCH_OPOOD_TILE::get));
	public static final RegistryObject<Block> GENENERGYDANDRON = REGISTRY.register("gen_energydandron", () -> new BlockSpecialFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.GENENERGYDANDRON::get));
	public static final RegistryObject<Block> KILLING_BERRY = REGISTRY.register("killing_berry", () -> new BlockSpecialFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.KILLING_BERRY_TILE::get));
	public static final RegistryObject<Block> DARK_NIGHT_GRASS = REGISTRY.register("dark_night_grass", () -> new BlockSpecialFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.DARK_NIGHT_GRASS_TILE::get));
	public static final RegistryObject<Block> FROST_LOTUS = REGISTRY.register("frost_lotus", () -> new BlockSpecialFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.FROST_LOTUS_TILE::get));
	public static final RegistryObject<Block> LYCORISRADIATA = REGISTRY.register("lycorisradiata", () -> new BlockSpecialFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.LYCORISRADIATA_TILE::get));
	public static final RegistryObject<Block> FROST_BLOSSOM = REGISTRY.register("frost_blossom", () -> new BlockSpecialFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.FROST_BLOSSOM_TILE::get));
	public static final RegistryObject<Block> MINGXIANLAN = REGISTRY.register("mingxianlan", () -> new BlockSpecialFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.MINGXIANLAN_TILE::get));
	public static final RegistryObject<Block> RAINBOW_GENERATING_FLOWER = REGISTRY.register("rainbow_generating_flower", () -> new BlockSpecialFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.RAINBOW_GENERATING_FLOWER_TILE::get));
	public static final RegistryObject<Block> BLAZING_ORCHID = REGISTRY.register("blazing_orchid", () -> new BlockSpecialFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.BLAZING_ORCHID_TILE::get));
	public static final RegistryObject<Block> STREET_LIGHT = REGISTRY.register("street_light", () -> new BlockSpecialFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.STREET_LIGHT_TILE::get));
	public static final RegistryObject<Block> VACUITY = REGISTRY.register("vacuity", () -> new BlockSpecialFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.VACUITY_TILE::get));
	public static final RegistryObject<Block> YU_SHOU_CLOVER = REGISTRY.register("yu_shou_clover", () -> new BlockSpecialFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.YU_SHOU_CLOVER_TILE::get));
	public static final RegistryObject<Block> CURSET_THISTLE = REGISTRY.register("curse_thistle", () -> new BlockSpecialFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.CURSET_THISTLE_TILE::get));
	//public static final RegistryObject<Block> RUNE_FLOWER = REGISTRY.register("rune_flower", () -> new BlockSpecialFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.RUNE_FLOWER_TILE::get));
	public static final RegistryObject<Block> ENDER_LAVENDER = REGISTRY.register("ender_lavender", () -> new BlockSpecialFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.ENDER_LAVENDER_TILE::get));
	public static final RegistryObject<Block> AUREA_AMICITIA_CARNATION = REGISTRY.register("aurea_amicitia_carnation", () -> new BlockSpecialFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.AUREA_AMICITIA_CARNATION_TILE::get));
	public static final RegistryObject<Block> MUSICAL_ORCHID = REGISTRY.register("musical_orchid", () -> new BlockSpecialFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.MUSICAL_ORCHID_TILE::get));
	public static final RegistryObject<Block> CATNIP = REGISTRY.register("catnip", () -> new Catnip(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.CATNIP_TILE::get));
	public static final RegistryObject<Block> ANCIENT_ALPHIRINE = REGISTRY.register("ancient_alphirine", () -> new BlockSpecialFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.ANCIENT_ALPHIRINE_TILE::get));
	public static final RegistryObject<Block> DICTARIUS = REGISTRY.register("dictarius", () -> new BlockSpecialFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.DICTARIUS_TILE::get));
	public static final RegistryObject<Block> EVIL_FORGE = REGISTRY.register("evil_forge", () -> new BlockSpecialFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.EVIL_FORGE_TILE::get));
	public static final RegistryObject<Block> ETHERIUM_FORGE = REGISTRY.register("etherium_forge", () -> new BlockSpecialFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.ETHERIUM_FORGE_TILE::get));
	public static final RegistryObject<Block> ARDENT_AZARCISSUS = REGISTRY.register("ardent_azarcissus", () -> new BlockSpecialFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.ARDENT_AZARCISSUS_TILE::get));



	public static final RegistryObject<Block> GAME_BOARD = REGISTRY.register("game_board", () -> new BlockBoardFate(BlockBehaviour.Properties.of(Material.STONE).strength(3.0f, 2.0f).sound(SoundType.STONE).noOcclusion()));
	public static final RegistryObject<Block> BOARD_FATE = REGISTRY.register("board_fate", () -> new BlockBoardFate(BlockBehaviour.Properties.of(Material.STONE).strength(3.0f, 2.0f).sound(SoundType.STONE).noOcclusion()));
	public static final RegistryObject<Block> FULL_ALTAR = REGISTRY.register("full_altar", () -> new FullAltarBlock(BlockBehaviour.Properties.of(Material.STONE).strength(3.0f, 2.0f).sound(SoundType.STONE).noOcclusion()));
	public static final RegistryObject<Block> infinitySpreader = REGISTRY.register("infinity_spreader", () -> new InfinityGaiaSpreader(VariantE.INFINITY, Properties.copy(Blocks.BIRCH_WOOD).isValidSpawn(NO_SPAWN)));
	public static final RegistryObject<Block> NIDAVELLIR_FORGE = REGISTRY.register("nidavellir_forge", () -> new NidavellirForgeBlock(BlockBehaviour.Properties.of(Material.STONE).strength(3.0f, 10.0f).sound(SoundType.METAL).noOcclusion()));
	public static final RegistryObject<Block> MANA_CHARGER = REGISTRY.register("mana_charger", () -> new ManaChargerBlock(BlockBehaviour.Properties.of(Material.STONE).strength(3.0f, 10.0f).sound(SoundType.METAL)));
	public static final RegistryObject<Block> MANA_CRYSTAL = REGISTRY.register("mana_crystal", () -> new ManaCrystalCubeBlock(BlockBehaviour.Properties.of(Material.STONE).strength(3.0f, 10.0f).sound(SoundType.METAL)));
	public static final RegistryObject<Block> INFINITY_POTATO = REGISTRY.register("infinity_potato", InfinityPotato::new);
	public static final RegistryObject<Block> POLYCHROME_COLLAPSE_PRISM = REGISTRY.register("polychrome_collapse_prism", () -> new PolychromeCollapsePrism(BlockBehaviour.Properties.of(Material.STONE).strength(3.0f, 10.0f).sound(SoundType.METAL)));
	public static final RegistryObject<Block> MANA_CONTAINER = REGISTRY.register("mana_container", () -> new ManaContainerBlock(ManaContainerBlock.Variant.DEFAULT, BlockBehaviour.Properties.copy(livingrock)));
	public static final RegistryObject<Block> CREATIVE_CONTAINER = REGISTRY.register("creative_container", () -> new ManaContainerBlock(ManaContainerBlock.Variant.CREATIVE, BlockBehaviour.Properties.copy(livingrock)));
	public static final RegistryObject<Block> TERRA_FARMLAND = REGISTRY.register("terra_farmland", TerraFarmland::new);
	public static final RegistryObject<Block> DILUTED_CONTAINER = REGISTRY.register("diluted_container", () -> new ManaContainerBlock(ManaContainerBlock.Variant.DILUTED, BlockBehaviour.Properties.copy(livingrock)));
	//public static final RegistryObject<Block> FABULOUS_CONTAINER = REGISTRY.register("fabulous_container", () -> new ManaContainerBlock(ManaContainerBlock.Variant.FABULOUS, BlockBehaviour.Properties.copy(livingrock)));
	public static final RegistryObject<Block> ASTRAL_BLOCK = REGISTRY.register("astral_block", () -> new AstralBlock(BlockBehaviour.Properties.of(Material.STONE).strength(3.0f, 2.0f).sound(SoundType.STONE).noOcclusion()));
	public static final RegistryObject<Block> MANA_BRACKET = REGISTRY.register("mana_bracket", () -> new ManaBracket(BlockBehaviour.Properties.of(Material.STONE).strength(1.0f, 2.0f).sound(SoundType.STONE).noOcclusion()));
	public static final RegistryObject<Block> ENGINEER_HOPPER = REGISTRY.register("engineer_hopper", () -> new BlockEngineerHopper(BlockBehaviour.Properties.of(Material.METAL).strength(3.5F, 8.0F).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion()));
	public static final RegistryObject<Block> COSMIC_BLOCK = REGISTRY.register("cosmic_block", () -> new CosmicBlock(BlockBehaviour.Properties.of(Material.STONE).strength(3.0f, 2.0f).sound(SoundType.STONE).noOcclusion()));
	public static final RegistryObject<Block> EXTREME_AUTO_CRAFTER = REGISTRY.register("extreme_auto_crafter", () -> new BlockExtremeAutoCrafter(Properties.of(Material.METAL).strength(50F, 2000F).requiresCorrectToolForDrops().sound(SoundType.GLASS)));
	public static final RegistryObject<Block> INFINITY_COMPRESSOR = REGISTRY.register("infinity_compressor", () -> new BlockInfinityCompressor(BlockBehaviour.Properties.of(Material.METAL).strength(50F, 2000F).requiresCorrectToolForDrops().lightLevel((state) -> 15)));


	//其他
//	public static final RegistryObject<Block> MAGIC_TABLE = REGISTRY.register("magic_table", () -> new MagicTableBlock(Properties.of(Material.WOOD).strength(2.5F).sound(SoundType.WOOD).noOcclusion()));
	public static final RegistryObject<Block> SPECTRITE_CHEST = REGISTRY.register("spectrite_chest", SpectriteChest::new);
	public static final RegistryObject<Block> INFINITYGlASS = REGISTRY.register("infinityglass", InfinityGlass::new);
	public static final RegistryObject<Block> STARLITSANCTUM = REGISTRY.register("starlit_sanctum", StarlitSanctumOfMystique::new);
	public static final RegistryObject<Block> CELESTIAL_HOLINESS_TRANSMUTER = REGISTRY.register("celestial_holiness_transmuter", CelestialHolinessTransmuter::new);
	public static final RegistryObject<Block> ENDLESS_CAKE = REGISTRY.register("endless_cake", EndlessCakeBlock::new);
	public static final RegistryObject<Block> FLUFFY_DANDELION = REGISTRY.register("fluffy_dandelion", FluffyDandelionBlock::new);
	public static final RegistryObject<Block> COBBLE_STONE = REGISTRY.register("cobble_stone", Cobblestone::new);
	public static final RegistryObject<Block> BLOCKNATURE = REGISTRY.register("blocknature", BlocknatureBlock::new);
	public static final RegistryObject<Block> EVILBLOCK = REGISTRY.register("evilblock", EvilBlock::new);
	public static final RegistryObject<Block> PRISMATICRADIANCEBLOCK = REGISTRY.register("prismaticradianceblock", PrismaticRadianceBlock::new);
	public static final RegistryObject<Block> INFINITYCHEST = REGISTRY.register("infinity_chest", InfinityChest::new);
	public static final RegistryObject<Block> IRIDIUM_BLOCK = REGISTRY.register("iridium_block", IridiumBlock::new);
	public static final RegistryObject<Block> NICKEL_BLOCK = REGISTRY.register("nickel_block", NickelBlock::new);
	public static final RegistryObject<Block> AERIALITE_BLOCK = REGISTRY.register("aerialite_block", AerialiteBlock::new);
	public static final RegistryObject<Block> DECAY_BLOCK = REGISTRY.register("decay_block", DecayBlock::new);
	public static final RegistryObject<Block> ARCANE_ICE_CHUNK = REGISTRY.register("arcane_ice_chunk", ArcaneIceChunk::new);
	public static final RegistryObject<Block> PAGED_CHEST = REGISTRY.register("paged_chest", () -> new PagedChestBlock(Properties.of(Material.WOOD).strength(2.5F).sound(SoundType.WOOD).noOcclusion()));
	public static final RegistryObject<Block> RAINBOW_TABLE = REGISTRY.register("rainbow_table", () -> new RainbowTable(BlockBehaviour.Properties.of(Material.METAL).strength(3.5F).requiresCorrectToolForDrops().noOcclusion()));
	public static final RegistryObject<Block> CUSTOM_SAPLING = REGISTRY.register("custom_sapling", () -> new CustomSaplingBlock(BlockBehaviour.Properties.of(Material.PLANT).noCollission().instabreak()));
	public static final RegistryObject<Block> GAIA_BLOCK = REGISTRY.register("gaia_block", GaiaBlock::new);
	public static final RegistryObject<Block> MITHRILL_BLOCK = REGISTRY.register("mithrill_block", MithrillBlock::new);
	public static final RegistryObject<Block> DRAGON_CRYSTALS_BLOCK = REGISTRY.register("dragon_crystal_block", DragonCrystalBlock::new);
	public static final RegistryObject<Block> DECON_TABLE = REGISTRY.register("deconstruction_table", DeconTableBlock::new);
	public static final RegistryObject<Block> ANTIGRAVITATION_BLOCK = REGISTRY.register("antigravitation_block", BlockAntigravitation::new);





	//矿物
	public static final RegistryObject<Block> IRIDIUM_ORE = REGISTRY.register("iridium_ore", IridiumOre::new);
	public static final RegistryObject<Block> NICKEL_ORE = REGISTRY.register("nickel_ore", NickelOre::new);
	public static final RegistryObject<Block> PLATINUM_ORE = REGISTRY.register("platinum_ore", PlatinumOre::new);
	public static final RegistryObject<Block> END_ORE = REGISTRY.register("end_ore", EndOre::new);
	public static final RegistryObject<Block> AMETHYST_ORE = REGISTRY.register("amethyst_ore", AmethystOre::new);
	public static final RegistryObject<Block> BAUXITE_ORE = REGISTRY.register("bauxite_ore", BauxiteOre::new);
	public static final RegistryObject<Block> CHROMITE_ORE = REGISTRY.register("chromite_ore", ChromiteOre::new);
	public static final RegistryObject<Block> FLUORITE_ORE = REGISTRY.register("fluorite_ore", FluoriteOre::new);
	public static final RegistryObject<Block> GYPSUM_ORE = REGISTRY.register("gypsum_ore", GypsumOre::new);
	public static final RegistryObject<Block> JADE_ORE = REGISTRY.register("jade_ore", JadeOre::new);
	public static final RegistryObject<Block> LEAD_ORE = REGISTRY.register("lead_ore", LeadOre::new);
	public static final RegistryObject<Block> BRONZE_ORE = REGISTRY.register("bronze_ore", BronzeOre::new);
	public static final RegistryObject<Block> OPAL_ORE = REGISTRY.register("opal_ore", OpalOre::new);
	public static final RegistryObject<Block> SPECTRITE_ORE = REGISTRY.register("spectrite_ore", SpectriteOre::new);






	//传送门方块
	public static final RegistryObject<Block> MINERS_HEAVEN_PORTAL = REGISTRY.register("heaven_portal", MinersHeavenPortalBlock::new);
	public static final RegistryObject<Block> ANOTHER_PORTAL = REGISTRY.register("another_portal", AnotherPortalBlock::new);

}