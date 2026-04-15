package org.xiaoyang.ex_enigmaticlegacy.Init;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.xiaoyang.ex_enigmaticlegacy.Block.GaiaBlock;
import org.xiaoyang.ex_enigmaticlegacy.Block.PagedChestBlock;
import org.xiaoyang.ex_enigmaticlegacy.Block.RainbowTable;
import org.xiaoyang.ex_enigmaticlegacy.Block.custom.CustomSaplingBlock;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Block.*;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Block.TerraFarmland;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Flower.functional.*;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Flower.generating.*;
import org.xiaoyang.ex_enigmaticlegacy.Block.*;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Flower.hybrid.AquaticAnglerNarcissus;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Flower.hybrid.RuneFlower;

import static org.xiaoyang.ex_enigmaticlegacy.Exe.MODID;
import static vazkii.botania.common.block.BotaniaBlocks.livingrock;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    private static final BlockBehaviour.StateArgumentPredicate<EntityType<?>> NO_SPAWN = (state, world, pos, et) -> false;
    public static final BlockBehaviour.Properties FLOWER_PROPS = BlockBehaviour.Properties.copy(Blocks.POPPY);


    //方块
    public static final RegistryObject<Block> ANTIGRAVITATION_BLOCK = BLOCKS.register("antigravitation_block", BlockAntigravitation::new);
    public static final RegistryObject<Block> TERRA_FARMLAND = BLOCKS.register("terra_farmland", TerraFarmland::new);
    public static final RegistryObject<Block> MANA_BOX = BLOCKS.register("mana_box", BlockManaBox::new);
    public static final RegistryObject<Block> GAME_BOARD = BLOCKS.register("game_board", () -> new BlockBoardFate(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.0f, 2.0f).sound(SoundType.STONE).noOcclusion()));
    public static final RegistryObject<Block> BOARD_FATE = BLOCKS.register("board_fate", () -> new BlockBoardFate(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.0f, 2.0f).sound(SoundType.STONE).noOcclusion()));
    public static final RegistryObject<Block> FULL_ALTAR = BLOCKS.register("full_altar", () -> new FullAltarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.0f, 2.0f).sound(SoundType.STONE).noOcclusion()));
    public static final RegistryObject<Block> NIDAVELLIR_FORGE = BLOCKS.register("nidavellir_forge", () -> new NidavellirForgeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.0f, 10.0f).sound(SoundType.METAL).noOcclusion()));
    public static final RegistryObject<Block> MANA_CHARGER = BLOCKS.register("mana_charger", () -> new ManaChargerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.0f, 10.0f).sound(SoundType.METAL)));
    public static final RegistryObject<Block> MANA_CRYSTAL = BLOCKS.register("mana_crystal", () -> new ManaCrystalCubeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.0f, 10.0f).sound(SoundType.METAL)));
    public static final RegistryObject<Block> INFINITY_POTATO = BLOCKS.register("infinity_potato", InfinityPotato::new);
    public static final RegistryObject<Block> POLYCHROME_COLLAPSE_PRISM = BLOCKS.register("polychrome_collapse_prism", () -> new PolychromeCollapsePrism(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.0f, 10.0f).sound(SoundType.METAL)));
    public static final RegistryObject<Block> MANA_CONTAINER = BLOCKS.register("mana_container", () -> new ManaContainerBlock(ManaContainerBlock.Variant.DEFAULT, BlockBehaviour.Properties.copy(livingrock)));
    public static final RegistryObject<Block> CREATIVE_CONTAINER = BLOCKS.register("creative_container", () -> new ManaContainerBlock(ManaContainerBlock.Variant.CREATIVE, BlockBehaviour.Properties.copy(livingrock)));
    public static final RegistryObject<Block> DILUTED_CONTAINER = BLOCKS.register("diluted_container", () -> new ManaContainerBlock(ManaContainerBlock.Variant.DILUTED, BlockBehaviour.Properties.copy(livingrock)));
    public static final RegistryObject<Block> ASTRAL_BLOCK = BLOCKS.register("astral_block", () -> new AstralBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.0f, 2.0f).sound(SoundType.STONE).noOcclusion()));
    public static final RegistryObject<Block> MANA_BRACKET = BLOCKS.register("mana_bracket", () -> new ManaBracket(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.0f, 2.0f).sound(SoundType.STONE).noOcclusion()));
    public static final RegistryObject<Block> ENGINEER_HOPPER = BLOCKS.register("engineer_hopper", () -> new BlockEngineerHopper(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5F, 8.0F).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion()));
//    public static final RegistryObject<Block> COSMIC_BLOCK = BLOCKS.register("cosmic_block", () -> new CosmicBlock(BlockBehaviour.Properties.of(Material.STONE).strength(3.0f, 2.0f).sound(SoundType.STONE).noOcclusion()));
    public static final RegistryObject<Block> EXTREME_AUTO_CRAFTER = BLOCKS.register("extreme_auto_crafter", () -> new BlockExtremeAutoCrafter(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(50F, 2000F).requiresCorrectToolForDrops().sound(SoundType.GLASS)));
//    public static final RegistryObject<Block> INFINITY_COMPRESSOR = BLOCKS.register("infinity_compressor", () -> new BlockInfinityCompressor(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(50F, 2000F).requiresCorrectToolForDrops().lightLevel((state) -> 15)));
    public static final RegistryObject<Block> EXTREME_CRAFTING_DISASSEMBLY_TABLE = BLOCKS.register("extreme_crafting_disassembly_table", ExtremeCraftingDisassembly::new);
    public static final RegistryObject<Block> NEUTRONIUM_DECOMPRESSOR = BLOCKS.register("neutronium_decompressor", NeutroniumDecompressorBlock::new);
//    public static final RegistryObject<Block> STARRY_SKY_BLOCK = BLOCKS.register("starry", () -> new StarrySkyBlock(BlockBehaviour.Properties.of(Material.STONE).strength(3.0f, 2.0f).sound(SoundType.STONE).noOcclusion()));
    public static final RegistryObject<Block> ADVANCED_SPREADER = BLOCKS.register("advanced_spreader", () -> new BlockAdvancedSpreader(BlockAdvancedSpreader.VariantN.NATURE, BlockBehaviour.Properties.copy(Blocks.BIRCH_WOOD).isValidSpawn(NO_SPAWN)));
    public static final RegistryObject<Block> SPECTRITE_CHEST = BLOCKS.register("spectrite_chest", SpectriteChest::new);
    public static final RegistryObject<Block> INFINITYGlASS = BLOCKS.register("infinityglass", InfinityGlass::new);
    public static final RegistryObject<Block> STARLIT_SANCTUM = BLOCKS.register("starlit_sanctum", StarlitSanctum::new);
    public static final RegistryObject<Block> CELESTIAL_HOLINESS_TRANSMUTER = BLOCKS.register("celestial_holiness_transmuter", CelestialHolinessTransmuter::new);
    public static final RegistryObject<Block> ENDLESS_CAKE = BLOCKS.register("endless_cake", EndlessCakeBlock::new);
    public static final RegistryObject<Block> COBBLE_STONE = BLOCKS.register("cobble_stone", Cobblestone::new);
    public static final RegistryObject<Block> BLOCKNATURE = BLOCKS.register("blocknature", BlocknatureBlock::new);
    public static final RegistryObject<Block> EVILBLOCK = BLOCKS.register("evilblock", EvilBlock::new);
    public static final RegistryObject<Block> PRISMATICRADIANCEBLOCK = BLOCKS.register("prismaticradianceblock", PrismaticRadianceBlock::new);
    public static final RegistryObject<Block> AERIALITE_BLOCK = BLOCKS.register("aerialite_block", AerialiteBlock::new);
    public static final RegistryObject<Block> DECAY_BLOCK = BLOCKS.register("decay_block", DecayBlock::new);
    public static final RegistryObject<Block> ARCANE_ICE_CHUNK = BLOCKS.register("arcane_ice_chunk", ArcaneIceChunk::new);
    public static final RegistryObject<Block> PAGED_CHEST = BLOCKS.register("paged_chest", () -> new PagedChestBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistryObject<Block> RAINBOW_TABLE = BLOCKS.register("rainbow_table", () -> new RainbowTable(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(3.5F).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistryObject<Block> CUSTOM_SAPLING = BLOCKS.register("custom_sapling", () -> new CustomSaplingBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().instabreak()));
    public static final RegistryObject<Block> GAIA_BLOCK = BLOCKS.register("gaia_block", GaiaBlock::new);
    public static final RegistryObject<Block> MITHRILL_BLOCK = BLOCKS.register("mithrill_block", MithrillBlock::new);
    public static final RegistryObject<Block> DRAGON_CRYSTALS_BLOCK = BLOCKS.register("dragon_crystal_block", DragonCrystalBlock::new);
    public static final RegistryObject<Block> DECON_TABLE = BLOCKS.register("deconstruction_table", DeconTableBlock::new);
    public static final RegistryObject<Block> PEACEFUL_TABLE = BLOCKS.register("peaceful_table", () -> new BlockPeacefulTable(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0F, 10.0F).sound(SoundType.WOOD).randomTicks().noOcclusion()));
    public static final RegistryObject<Block> LEBETHRON_WOOD = BLOCKS.register("lebethron_wood", () -> new BlockLebethronWood(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)));
    public static final RegistryObject<Block> LEBETHRON_LOG = BLOCKS.register("lebethron_wood_glowing", () -> new BlockLebethronWoodGlowing(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)));
    public static final RegistryObject<Block> SPECTRITE_ORE = BLOCKS.register("spectrite_ore", SpectriteOre::new);
    public static final RegistryObject<Block> LEBETHRON_CORE = BLOCKS.register("lebethron_core", () -> new BlockLebethronCore(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)));
    public static RegistryObject<Block> MAGIC_TABLE = null;







    //魔法花

    public static final RegistryObject<Block> ASTRAL_KILLOP = BLOCKS.register("astral_killop",
            () -> new AstralKillop(
                    MobEffects.HEALTH_BOOST,
                    120,
                    Block.Properties.copy(Blocks.POPPY),
                    ModBlockEntities.ASTRAL_KILLOP_TILE::get
            )
    );

    public static final RegistryObject<Block> RAINBOW_GENERATING_FLOWER = BLOCKS.register("rainbow_generating_flower",
            () -> new RainbowGeneratingFlowerBlock(
                    MobEffects.HEALTH_BOOST,
                    120,
                    Block.Properties.copy(Blocks.POPPY),
                    ModBlockEntities.RAINBOW_GENERATING_FLOWER_TILE::get
            )
    );

    public static final RegistryObject<Block> NIGHTSHADE = BLOCKS.register("nightshade",() -> new NightshadeBlock(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.NIGHTSHADE_TILE::get));
    public static final RegistryObject<Block> DAYBLOOM = BLOCKS.register("daybloom",() -> new DaybloomBlock(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.DAYBLOOM_TILE::get));
    public static final RegistryObject<Block> FLOWEY = BLOCKS.register("flowey",() -> new FloweyBlock(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.FLOWEYTILE::get));
    public static final RegistryObject<Block> BELIEVER = BLOCKS.register("believer",() -> new BelieverBlock(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.BELIEVERTILE::get));
    public static final RegistryObject<Block> SOARLEANDER = BLOCKS.register("soarleander", () -> new Soarleander(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.SOARLEANDERTILE::get));
    public static final RegistryObject<Block> ORECHIDENDIUM = BLOCKS.register("orechid_endium", () -> new OrechidEndium(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.ORECHIDENDIUMTILE::get));
    public static final RegistryObject<Block> WITCH_OPOOD = BLOCKS.register("witch_opood", () -> new WitchOpoodBlock(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.WITCH_OPOOD_TILE::get));
    public static final RegistryObject<Block> GENENERGYDANDRON = BLOCKS.register("gen_energydandron", () -> new GenEnergydandron(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.GENENERGYDANDRON::get));
    public static final RegistryObject<Block> KILLING_BERRY = BLOCKS.register("killing_berry", () -> new KillingBerry(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.KILLING_BERRY_TILE::get));
    public static final RegistryObject<Block> DARK_NIGHT_GRASS = BLOCKS.register("dark_night_grass", () -> new DarkNightGrass(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.DARK_NIGHT_GRASS_TILE::get));
    public static final RegistryObject<Block> FROST_LOTUS = BLOCKS.register("frost_lotus", () -> new FrostLotusFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.FROST_LOTUS_TILE::get));
    public static final RegistryObject<Block> LYCORISRADIATA = BLOCKS.register("lycorisradiata", () -> new Lycorisradiata(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.LYCORISRADIATA_TILE::get));
    public static final RegistryObject<Block> FROST_BLOSSOM = BLOCKS.register("frost_blossom", () -> new FrostBlossomBlock(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.FROST_BLOSSOM_TILE::get));
    public static final RegistryObject<Block> MINGXIANLAN = BLOCKS.register("mingxianlan", () -> new MingXianLanBlock(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.MINGXIANLAN_TILE::get));
    public static final RegistryObject<Block> BLAZING_ORCHID = BLOCKS.register("blazing_orchid", () -> new BlazingOrchidFlowerBlock(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.BLAZING_ORCHID_TILE::get));
    public static final RegistryObject<Block> STREET_LIGHT = BLOCKS.register("street_light", () -> new StreetLightFlowerBlock(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.STREET_LIGHT_TILE::get));
    public static final RegistryObject<Block> VACUITY = BLOCKS.register("vacuity", () -> new Vacuity(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.VACUITY_TILE::get));
    public static final RegistryObject<Block> YU_SHOU_CLOVER = BLOCKS.register("yu_shou_clover", () -> new YushouClover(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.YU_SHOU_CLOVER_TILE::get));
    public static final RegistryObject<Block> CURSET_THISTLE = BLOCKS.register("curse_thistle", () -> new CurseThistle(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.CURSET_THISTLE_TILE::get));
    public static final RegistryObject<Block> RUNE_FLOWER = BLOCKS.register("rune_flower", () -> new RuneFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.RUNE_FLOWER_TILE::get));
    public static final RegistryObject<Block> ENDER_LAVENDER = BLOCKS.register("ender_lavender", () -> new EnderLavender(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.ENDER_LAVENDER_TILE::get));
    public static final RegistryObject<Block> AUREA_AMICITIA_CARNATION = BLOCKS.register("aurea_amicitia_carnation", () -> new AureaAmicitiaCarnation(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.AUREA_AMICITIA_CARNATION_TILE::get));
    public static final RegistryObject<Block> MUSICAL_ORCHID = BLOCKS.register("musical_orchid", () -> new MusicalOrchid(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.MUSICAL_ORCHID_TILE::get));
    public static final RegistryObject<Block> CATNIP = BLOCKS.register("catnip", () -> new Catnip(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.CATNIP_TILE::get));
    public static final RegistryObject<Block> ANCIENT_ALPHIRINE = BLOCKS.register("ancient_alphirine", () -> new AncientAlphirine(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.ANCIENT_ALPHIRINE_TILE::get));
    public static final RegistryObject<Block> DICTARIUS = BLOCKS.register("dictarius", () -> new Dictarius(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.DICTARIUS_TILE::get));
    public static final RegistryObject<Block> EVIL_FORGE = BLOCKS.register("evil_forge", () -> new EvilForge(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.EVIL_FORGE_TILE::get));
    public static final RegistryObject<Block> ETHERIUM_FORGE = BLOCKS.register("etherium_forge", () -> new EtheriumForge(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.ETHERIUM_FORGE_TILE::get));
    public static final RegistryObject<Block> ARDENT_AZARCISSUS = BLOCKS.register("ardent_azarcissus", () -> new ArdentAzarcissus(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.ARDENT_AZARCISSUS_TILE::get));
    public static final RegistryObject<Block> AQUATIC_ANGLER_NARCISSUS = BLOCKS.register("aquatic_angler_narcissus", () -> new AquaticAnglerNarcissus(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.AQUATIC_ANGLER_NARCISSUS_TILE::get));
}
