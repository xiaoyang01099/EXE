package org.xiaoyang.ex_enigmaticlegacy.Init;

import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item.*;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item.Relic.*;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over.ItemPowerRing;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.ItemBlockFlower;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.ItemHybridSpecialFlower;
import org.xiaoyang.ex_enigmaticlegacy.Font.ModRarities;
import org.xiaoyang.ex_enigmaticlegacy.Item.all.ModDetermination;
import org.xiaoyang.ex_enigmaticlegacy.Item.all.ModIngot;
import org.xiaoyang.ex_enigmaticlegacy.Item.all.ModSingularity;
import org.xiaoyang.ex_enigmaticlegacy.Item.res.InfinityRod;
import org.xiaoyang.ex_enigmaticlegacy.Item.res.RideablePearl;
import org.xiaoyang.ex_enigmaticlegacy.Item.res.*;

import static org.xiaoyang.ex_enigmaticlegacy.Exe.MODID;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final RegistryObject<Item> DIVINE_CLOAK_NJORD = ITEMS.register("divine_cloak_njord",
            () -> new DivineCloak(new Item.Properties(), DivineCloak.NJORD));
    public static final RegistryObject<Item> DIVINE_CLOAK_IDUNN = ITEMS.register("divine_cloak_idunn",
            () -> new DivineCloak(new Item.Properties(), DivineCloak.IDUNN));
    public static final RegistryObject<Item> DIVINE_CLOAK_THOR = ITEMS.register("divine_cloak_thor",
            () -> new DivineCloak(new Item.Properties(), DivineCloak.THOR));
    public static final RegistryObject<Item> DIVINE_CLOAK_HEIMDALL = ITEMS.register("divine_cloak_heimdall",
            () -> new DivineCloak(new Item.Properties(), DivineCloak.HEIMDALL));
    public static final RegistryObject<Item> DIVINE_CLOAK_LOKI = ITEMS.register("divine_cloak_loki",
            () -> new DivineCloak(new Item.Properties(), DivineCloak.LOKI));

    //扩展花
    public static final RegistryObject<Item> CURSET_THISTLE = blockFlower(ModBlocks.CURSET_THISTLE);
    public static final RegistryObject<Item> YU_SHOU_CLOVER = blockFlower(ModBlocks.YU_SHOU_CLOVER);
    public static final RegistryObject<Item> VACUITY = blockFlower(ModBlocks.VACUITY);
    public static final RegistryObject<Item> STREET_LIGHT = blockFlower(ModBlocks.STREET_LIGHT);
    public static final RegistryObject<Item> BLAZING_ORCHID = blockFlower(ModBlocks.BLAZING_ORCHID);
    public static final RegistryObject<Item> MINGXIANLAN = blockFlower(ModBlocks.MINGXIANLAN);
    public static final RegistryObject<Item> FROST_BLOSSOM = blockFlower(ModBlocks.FROST_BLOSSOM);
    public static final RegistryObject<Item> FROST_LOTUS = blockFlower(ModBlocks.FROST_LOTUS);
    public static final RegistryObject<Item> DARK_NIGHT_GRASS = blockFlower(ModBlocks.DARK_NIGHT_GRASS);
    public static final RegistryObject<Item> KILLING_BERRY = blockFlower(ModBlocks.KILLING_BERRY);
    public static final RegistryObject<Item> NIGHTSHADE = blockFlower(ModBlocks.NIGHTSHADE);
    public static final RegistryObject<Item> DAYBLOOM = blockFlower(ModBlocks.DAYBLOOM);
    public static final RegistryObject<Item> BELIEVE = blockFlower(ModBlocks.BELIEVER);
    public static final RegistryObject<Item> GENENERGYDANDRON = blockFlower(ModBlocks.GENENERGYDANDRON);
    public static final RegistryObject<Item> ORECHIDENDIUM = blockFlower(ModBlocks.ORECHIDENDIUM);
    public static final RegistryObject<Item> FLOWEY = blockFlower(ModBlocks.FLOWEY);
    public static final RegistryObject<Item> WITCH_OPOOD = blockFlower(ModBlocks.WITCH_OPOOD);
    public static final RegistryObject<Item> LYCORISRADIATA = blockFlower(ModBlocks.LYCORISRADIATA);
    public static final RegistryObject<Item> SOARLEANDER = blockFlower(ModBlocks.SOARLEANDER);
    public static final RegistryObject<Item> ENDER_LAVENDER = blockFlower(ModBlocks.ENDER_LAVENDER);
    public static final RegistryObject<Item> AUREA_AMICITIA_CARNATION = blockFlower(ModBlocks.AUREA_AMICITIA_CARNATION);
    public static final RegistryObject<Item> CATNIP = blockFlower(ModBlocks.CATNIP);
    public static final RegistryObject<Item> MUSICAL_ORCHID = blockFlower(ModBlocks.MUSICAL_ORCHID);
    public static final RegistryObject<Item> DICTARIUS = blockFlower(ModBlocks.DICTARIUS);
    public static final RegistryObject<Item> ANCIENT_ALPHIRINE = blockFlower(ModBlocks.ANCIENT_ALPHIRINE);
    public static final RegistryObject<Item> EVIL_FORGE = blockFlower(ModBlocks.EVIL_FORGE);
    public static final RegistryObject<Item> ETHERIUM_FORGE = blockFlower(ModBlocks.ETHERIUM_FORGE);
    public static final RegistryObject<Item> ARDENT_AZARCISSUS = blockFlower(ModBlocks.ARDENT_AZARCISSUS);
    public static final RegistryObject<Item> AQUATIC_ANGLER_NARCISSUS = blockHyFlower(ModBlocks.AQUATIC_ANGLER_NARCISSUS);
    public static final RegistryObject<Item> RUNE_FLOWER = blockHyFlower(ModBlocks.RUNE_FLOWER);
    public static final RegistryObject<Item> ASTRAL_KILLOP = blockFlower(ModBlocks.ASTRAL_KILLOP);
    public static final RegistryObject<Item> RAINBOW_GENERATING_FLOWER = blockFlower(ModBlocks.RAINBOW_GENERATING_FLOWER);


    //方块物品
    public static final RegistryObject<Item> MANA_BOX_ITEM = ITEMS.register("mana_box", BlockItemManaBox::new);
    public static final RegistryObject<Item> BLOCKNATURE = blockFlower(ModBlocks.BLOCKNATURE);
    public static final RegistryObject<Item> PRISMATICRADIANCEBLOCK = blockFlower(ModBlocks.PRISMATICRADIANCEBLOCK);
    public static final RegistryObject<Item> ADVANCED_SPREADER = block(ModBlocks.ADVANCED_SPREADER);
    public static final RegistryObject<Item> MANA_CRYSTAL = block(ModBlocks.MANA_CRYSTAL);
    public static final RegistryObject<Item> MANA_CHARGER = block(ModBlocks.MANA_CHARGER);
    public static final RegistryObject<Item> CELESTIAL_HOLINESS_TRANSMUTER = block(ModBlocks.CELESTIAL_HOLINESS_TRANSMUTER);
    public static final RegistryObject<Item> INFINITYGLASS = block(ModBlocks.INFINITYGlASS);
    public static final RegistryObject<Item> EVILBLOCK = block(ModBlocks.EVILBLOCK);
    public static final RegistryObject<Item> COBBLE_STONE = block(ModBlocks.COBBLE_STONE);
    public static final RegistryObject<Item> INFINITY_POTATO = block(ModBlocks.INFINITY_POTATO);
    public static final RegistryObject<Item> CUSTOM_SAPLING = block(ModBlocks.CUSTOM_SAPLING);
    public static final RegistryObject<Item> SPECTRITE_CHEST = block(ModBlocks.SPECTRITE_CHEST);
    public static final RegistryObject<Item> DRAGON_CRYSTALS_BLOCK = block(ModBlocks.DRAGON_CRYSTALS_BLOCK);
    public static final RegistryObject<Item> POLYCHROME_COLLAPSE_PRISM = block(ModBlocks.POLYCHROME_COLLAPSE_PRISM);
    public static final RegistryObject<Item> MANA_CONTAINER = block(ModBlocks.MANA_CONTAINER);
    public static final RegistryObject<Item> DILUTED_CONTAINER = block(ModBlocks.DILUTED_CONTAINER);
    public static final RegistryObject<Item> CREATIVE_CONTAINER = block(ModBlocks.CREATIVE_CONTAINER);
    public static final RegistryObject<Item> MITHRILL_BLOCK = block(ModBlocks.MITHRILL_BLOCK);
    public static final RegistryObject<Item> NIDAVELLIR_FORGE = block(ModBlocks.NIDAVELLIR_FORGE);
    public static final RegistryObject<Item> GAIA_BLOCK = block(ModBlocks.GAIA_BLOCK);
    public static final RegistryObject<Item> PAGED_CHEST = block(ModBlocks.PAGED_CHEST);
    public static final RegistryObject<Item> ARCANE_ICE_CHUNK = block(ModBlocks.ARCANE_ICE_CHUNK);
    public static final RegistryObject<Item> DECAY_BLOCK = block(ModBlocks.DECAY_BLOCK);
    public static final RegistryObject<Item> AERIALITE_BLOCK = block(ModBlocks.AERIALITE_BLOCK);
    public static final RegistryObject<Item> RAINBOW_TABLE = block(ModBlocks.RAINBOW_TABLE);
    public static final RegistryObject<Item> DECON_TABLE_ITEM = block(ModBlocks.DECON_TABLE);
    public static final RegistryObject<Item> FULL_ALTAR = block(ModBlocks.FULL_ALTAR);
    public static final RegistryObject<Item> GAME_BOARD = block(ModBlocks.GAME_BOARD);
    public static final RegistryObject<Item> BOARD_FATE = block(ModBlocks.BOARD_FATE);
    public static final RegistryObject<Item> TERRA_FARMLAND = block(ModBlocks.TERRA_FARMLAND);
    public static final RegistryObject<Item> ASTRAL_BLOCK = block(ModBlocks.ASTRAL_BLOCK);
    public static final RegistryObject<Item> MANA_BRACKET = block(ModBlocks.MANA_BRACKET);
    public static final RegistryObject<Item> ENGINEER_HOPPER = block(ModBlocks.ENGINEER_HOPPER);
    public static final RegistryObject<Item> EXTREME_AUTO_CRAFTER = block(ModBlocks.EXTREME_AUTO_CRAFTER);
//    public static final RegistryObject<Item> INFINITY_COMPRESSOR = block(ModBlocks.INFINITY_COMPRESSOR);
    public static final RegistryObject<Item> EXTREME_CRAFTING_DISASSEMBLY_TABLE = block(ModBlocks.EXTREME_CRAFTING_DISASSEMBLY_TABLE);
    public static final RegistryObject<Item> NEUTRONIUM_DECOMPRESSOR = block(ModBlocks.NEUTRONIUM_DECOMPRESSOR);
    public static final RegistryObject<Item> LEBETHRON_WOOD = block(ModBlocks.LEBETHRON_WOOD);
    public static final RegistryObject<Item> LEBETHRON_CORE = block(ModBlocks.LEBETHRON_CORE);
    public static final RegistryObject<Item> LEBETHRON_LOG = block(ModBlocks.LEBETHRON_LOG);
    public static final RegistryObject<Item> STARLIT_SANCTUM = block(ModBlocks.STARLIT_SANCTUM);
    public static final RegistryObject<Item> PEACEFUL_TABLE = block(ModBlocks.PEACEFUL_TABLE);
    public static final RegistryObject<Item> SPECTRITE_ORE = block(ModBlocks.SPECTRITE_ORE);





    //物品
    public static RegistryObject<Item> MAGIC_TABLE_ITEM = null;
    public static RegistryObject<Item> EMC_WAND = null;
    public static final RegistryObject<Item> SPAWN_CONTROL_STAFF = ITEMS.register("spawn_control_staff", () -> new SpawnControlStaff(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MANA_FLOWER = ITEMS.register("mana_flower", ManaFlower::new);
    public static final RegistryObject<Item> MANA_IVY_REGEN = ITEMS.register("mana_ivy_regen", ManaIvyRegen::new);
    public static final RegistryObject<Item> SPRAWL_ROD = ITEMS.register("sprawl_rod", () -> new SprawlRod(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> MITHRILL_MULTI_TOOL = ITEMS.register("mithrill_multi_tool", () -> new MithrillMultiTool(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> TERRA_SHOVEL = ITEMS.register("terra_shovel", () -> new TerraShovel(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> TERRA_HOE = ITEMS.register("terra_hoe", () -> new TerraHoe(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> RIDEABLE_PEARL = ITEMS.register("rideable_pearl", () -> new RideablePearl(new Item.Properties().durability(30)));
    public static final RegistryObject<Item> SACABAMBASPIS_SPAWN_EGG = ITEMS.register("sacabambaspis_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.SACABAMBASPIS, 0x8B0000, 0x808080, new Item.Properties()));
    public static final RegistryObject<Item> XIAOYANG_010_SPAWN_EGG = ITEMS.register("xiaoyang_010_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.XIAOYANG_010, -1, -1, new Item.Properties()));
    public static final RegistryObject<Item> NEBULA_ROD = ITEMS.register("nebula_rod", () -> new NebulaRod(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> MITHRILL_RING = ITEMS.register("mithrill_ring", () -> new MithrillRing(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> NEBULA_RING = ITEMS.register("nebula_ring", () -> new NebulaRing(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> ADVANCED_SPARK = ITEMS.register("advanced_spark", () -> new AdvancedSpark(new Item.Properties()));
    public static final RegistryObject<Item> ADMIN_CONTROLLER = ITEMS.register("admin_controller", () -> new AdminController(new Item.Properties()));
    public static final RegistryObject<Item> FATE_TOME = ITEMS.register("fate_tome", () -> new FateTome(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> TELEKINESIS_TOME_LEVEL = ITEMS.register("telekinesis_tome_level", () -> new TelekinesisTomeLevel(new Item.Properties().stacksTo(1).rarity(ModRarities.MIRACLE).fireResistant()));
    public static final RegistryObject<Item> BLACK_HALO_TOME = ITEMS.register("black_halo_tome", () -> new BlackHoleGrimoire(new Item.Properties().stacksTo(1).rarity(ModRarities.MIRACLE).fireResistant()));
    public static final RegistryObject<Item> HORN_PLENTY = ITEMS.register("horn_plenty", () -> new HornPlenty(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant()));
    public static final RegistryObject<Item> SPHERE_NAVIGATION = ITEMS.register("sphere_navigation", () -> new SphereNavigation(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant()));
    public static final RegistryObject<Item> FATE_HORN = ITEMS.register("fate_horn", () -> new FateHorn(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant()));
    public static final RegistryObject<Item> SPECTRITE_CRYSTAL = ITEMS.register("spectrite_crystal", SpectriteCrystal::new);
    public static final RegistryObject<Item> BEDROCK_BREAKER = ITEMS.register("bedrock_breaker", BedrockBreaker::new);
    public static final RegistryObject<Item> WEATHER_STONE = ITEMS.register("weather_stone", WeatherStone::new);
    public static final RegistryObject<Item> DIMENSIONAL_MIRROR = ITEMS.register("dimensional_mirror", DimensionalMirror::new);
    public static final RegistryObject<Item> OMEGA_CORE = ITEMS.register("omega_core", OmegaCore::new);
    public static final RegistryObject<Item> CHAOS_CORE = ITEMS.register("chaos_core", ChaosCore::new);
    public static final RegistryObject<Item> STARFLOWER_STONE = ITEMS.register("starflowerstone", StarflowerStone::new);
    public static final RegistryObject<Item> RAINBOW_MANAITA = ITEMS.register("rainbowmanaita", RainbowManaita::new);
    public static final RegistryObject<Item> HOLY_RING = ITEMS.register("holy_ring", HolyRing::new);
    public static final RegistryObject<Item> STAR_FUEL = ITEMS.register("starfuel", StarfuelItem::new);
    public static final RegistryObject<Item> KILLYOU = ITEMS.register("killyou", KillyouItem::new);
    public static final RegistryObject<Item> EGG = ITEMS.register("egg", RainBowEggItem::new);
    public static final RegistryObject<Item> MEMORIZE = ITEMS.register("memorize", Memorize::new);
    public static final RegistryObject<Item> MIAOMIAOTOU = ITEMS.register("miaomiaotou", MiaoMiaoTou::new);
    public static final RegistryObject<Item> LOPPING_PEARL = ITEMS.register("lopping_pearl",LoppingPearl::new);
    public static final RegistryObject<Item> MERCURIAL_EYE = ITEMS.register("mercurial_eye",MercurialEye::new);
    public static final RegistryObject<Item> HEARTH_STONE = ITEMS.register("hearth_stone",HearthStone::new);
    public static final RegistryObject<Item> INFINITY_TOTEM_LEVEL = ITEMS.register("infinity_totem_level",InfinityTotemLevel::new);
    public static final RegistryObject<Item> RADIANT_SACRED_RUBY = ITEMS.register("radiant_sacred_ruby", RadiantSacredRuby::new);
    public static final RegistryObject<Item> MANA_READER = ITEMS.register("mana_reader", () -> new ManaReader(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> INFINITY_ROD = ITEMS.register("infinity_rod", InfinityRod::new);
    public static final RegistryObject<Item> SLING = ITEMS.register("sling", Sling::new);
    public static final RegistryObject<Item> SLIME_CANNON = ITEMS.register("slime_cannon", SlimeCannon::new);
    public static final RegistryObject<Item> SLIME_NECKLACE = ITEMS.register("slime_necklace", SlimeNecklace::new);
    public static final RegistryObject<Item> CONTINUUM_BOMB = ITEMS.register("continuum_bomb", () -> new ContinuumBombItem(new Item.Properties().rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> VOID_GRIMOIRE = ITEMS.register("void_grimoire", () -> new VoidGrimoire(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> PLUMED_BELT = ITEMS.register("plumed_belt", () -> new PlumedBelt(new Item.Properties().rarity(Rarity.EPIC).stacksTo(1)));
    public static final RegistryObject<Item> FREYR_SLINGSHOT = ITEMS.register("freyr_slingshot", () -> new FreyrSlingshot(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).setNoRepair()));
    public static final RegistryObject<Item> ANTIGRAVITY_CHARM = ITEMS.register("antigravity_charm", () -> new AntigravityCharm(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).setNoRepair()));
    public static final RegistryObject<Item> EMPTY_MANA_BUCKET = ITEMS.register("empty_mana_bucket", () -> new ManaBucket(new Item.Properties().stacksTo(16), false));
    public static final RegistryObject<Item> FILLED_MANA_BUCKET = ITEMS.register("filled_mana_bucket", () -> new ManaBucket(new Item.Properties().stacksTo(16), true));
    public static final RegistryObject<Item> MANAITA = ITEMS.register("manaita", () -> new Manaita(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> AESIR_RING = ITEMS.register("aesir_ring", () -> new AesirRing(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> BLACK_HALO = ITEMS.register("black_halo", () -> new BlackHalo(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> ANCIENT_AEGIS = ITEMS.register("ancient_aegis", () -> new AncientAegis(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SCEPTER_OF_SOVEREIGN = ITEMS.register("scepter_of_sovereign", () -> new ScepterOfSovereign(new Item.Properties().stacksTo(1).fireResistant()));
    public static final RegistryObject<Item> TELEPORTATION_TOME = ITEMS.register("teleportation_tome", () -> new TeleportationTome(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> POWER_RING = ITEMS.register("power_ring", () -> new ItemPowerRing(new Item.Properties()));
    public static final RegistryObject<Item> DISCORD_RING = ITEMS.register("discord_ring", () -> new DiscordRing(new Item.Properties().rarity(Rarity.EPIC).stacksTo(1)));
    public static final RegistryObject<Item> COLOURFUL_DICE = ITEMS.register("colourful_dice", () -> new ColourfulDice(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SHINY_STONE = ITEMS.register("shiny_stone", () -> new ShinyStone(new Item.Properties().stacksTo(1).fireResistant()));
    public static final RegistryObject<Item> ELDRITCH_SPELL = ITEMS.register("eldritch_spell", () -> new EldritchSpell(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CRIMSON_SPELL = ITEMS.register("crimson_spell", () -> new CrimsonSpell(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> FALSE_JUSTICE = ITEMS.register("false_justice", () -> new FalseJustice(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> XP_TOME = ITEMS.register("xp_tome", () -> new XPTome(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> GHASTLY_SKULL = ITEMS.register("ghastly_skull", () -> new GhastlySkull(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CHAO_TOME = ITEMS.register("chao_tome", () -> new ChaosTome(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> OVERTHROWER = ITEMS.register("overthrower", () -> new Overthrower(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MANAITA_SHEARS = ITEMS.register("manaita_shears", () -> new ManaitaShears(new Item.Properties().durability(Integer.MAX_VALUE)));
    public static final RegistryObject<Item> MISSILE_TOME = ITEMS.register("missile_tome", () -> new MissileTome(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> APOTHEOSIS = ITEMS.register("apotheosis", () -> new Apotheosis(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> LUNAR_FLARE = ITEMS.register("lunar_flare", () -> new LunarFlares(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DEIFIC_AMULET = ITEMS.register("deific_amulet", () -> new DeificAmulet(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> THUNDER_PEAL = ITEMS.register("thunder_peal", () -> new Thunderpeal(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SOUL_TOME = ITEMS.register("soul_tome", () -> new SoulTome(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SUPERPOSITION_RING = ITEMS.register("superposition_ring", () -> new SuperpositionRing(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DORMANT_ARCANUM = ITEMS.register("dormant_arcanum", () -> new DormantArcanum(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> NEBULOUS_CORE = ITEMS.register("nebulous_core", () -> new NebulousCore(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> TELEKINESIS_TOME = ITEMS.register("telekinesis_tome", () -> new TelekinesisTome(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DARK_SUN_RING = ITEMS.register("dark_sun_ring", () -> new DarkSunRing(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> FLOWER_FINDER_WAND = ITEMS.register("flower_finder_wand", () -> new FlowerFinderWand(new Item.Properties().stacksTo(1).setNoRepair()));
    public static final RegistryObject<Item> GOLDEN_LAUREL = ITEMS.register("golden_laurel", () -> new GoldenLaurel(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> TALISMAN_HIDDEN_RICHES = ITEMS.register("talisman_hidden_riches", () -> new TalismanHiddenRiches(new Item.Properties().stacksTo(1).setNoRepair()));
    public static final RegistryObject<Item> POCKET_WARDROBE = ITEMS.register("pocket_wardrobe", () -> new PocketWardrobe(new Item.Properties().stacksTo(1)));


    //符文
    public static final RegistryObject<Item> RadianceRune = ITEMS.register("radiance_rune", ModRunes::new);
    public static final RegistryObject<Item> ShadowyRune = ITEMS.register("shadowy_rune", ModRunes::new);
    public static final RegistryObject<Item> IllusoryWorldSpiritSovereignRune = ITEMS.register("illusory_world_spirit_sovereign_rune", ModRunes::new);
    public static final RegistryObject<Item> MysticalSacredPactRune = ITEMS.register("mystical_sacred_pact_rune", ModRunes::new);
    public static final RegistryObject<Item> StellarVaultDivineRevelationRune = ITEMS.register("stellar_vault_divine_revelation_rune", ModRunes::new);
    public static final RegistryObject<Item> SereneMysteriousHeavenlyDecreeRune = ITEMS.register("serene_mysterious_heavenly_decree_rune", ModRunes::new);
    public static final RegistryObject<Item> StellarRune = ITEMS.register("stellar_rune", ModRunes::new);
    public static final RegistryObject<Item> HolyBloodCrystalRune = ITEMS.register("holy_blood_crystal_rune", ModRunes::new);
    public static final RegistryObject<Item> VerdantLeafSpiritualRhymeRune = ITEMS.register("verdant_leaf_spiritual_rhyme_rune", ModRunes::new);
    public static final RegistryObject<Item> DreamFeatherBlueButterflyRune = ITEMS.register("dream_feather_blue_butterfly_rune", ModRunes::new);
    public static final RegistryObject<Item> MysteriousPurpleWisteriaSpiritRune = ITEMS.register("mysterious_purple_wisteria_spirit_rune", ModRunes::new);



    //31个物品通用属性
    private static final Item.Properties INGOT_PROPERTIES = new Item.Properties().stacksTo(64);
    public static final RegistryObject<Item> NETHER_STAR_NUGGET = ITEMS.register("nether_star_nugget", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> ENHANCEMENT_CRYSTAL = ITEMS.register("enhancement_crystal", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> FROST_ENCHANTRESS = ITEMS.register("frost_enchantress", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> DEAD_SUBSTANCE = ITEMS.register("dead_substance", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.RARE)));
    public static final RegistryObject<Item> AMETHYST_INGOT = ITEMS.register("amethyst_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.RARE)));
    public static final RegistryObject<Item> BAUXITE_INGOT = ITEMS.register("bauxite_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.RARE)));
    public static final RegistryObject<Item> BRONZE_INGOT = ITEMS.register("bronze_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.RARE)));
    public static final RegistryObject<Item> CHROMITE_INGOT = ITEMS.register("chromite_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.RARE)));
    public static final RegistryObject<Item> FLUORITE_INGOT = ITEMS.register("fluorite_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.RARE)));
    public static final RegistryObject<Item> GYPSUM_INGOT = ITEMS.register("gypsum_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.RARE)));
    public static final RegistryObject<Item> JADE_INGOT = ITEMS.register("jade_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.RARE)));
    public static final RegistryObject<Item> LEAD_INGOT = ITEMS.register("lead_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.RARE)));
    public static final RegistryObject<Item> OPALLY = ITEMS.register("opally", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.RARE)));
    public static final RegistryObject<Item> OPAL_INGOT = ITEMS.register("opal_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.RARE)));
    public static final RegistryObject<Item> ASTRAL_PILE = ITEMS.register("astral_pile", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> ASTRAL_NUGGET = ITEMS.register("astral_nugget", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> ASTRAL_INGOT = ITEMS.register("astral_ingot", () -> new ModIngot(INGOT_PROPERTIES));
    public static final RegistryObject<Item> CUSTOM_INGOT = ITEMS.register("custom_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> CUSTOM_NUGGET = ITEMS.register("custom_nugget", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> COLORFUL_SHADOW_SHARD = ITEMS.register("colorful_shadow_shard", () -> new ModIngot(INGOT_PROPERTIES.fireResistant()));
    public static final RegistryObject<Item> DEATH_INGOT =ITEMS.register("death_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> DIRT_INGOT = ITEMS.register("dirt_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> DYEABLE_REDSTONE = ITEMS.register("dyeable_redstone", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> END_CRYSTAIC = ITEMS.register("end_crystaic", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> ELECTRIC_INGOT = ITEMS.register("electric_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> GRASS_INGOT = ITEMS.register("grass_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> IRIDIUM_INGOT = ITEMS.register("iridium_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> ICE_INGOT = ITEMS.register("ice_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> INGOT_ANIMATION = ITEMS.register("ingot_animation", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> INFINITYDROP = ITEMS.register("infinitydrop", () -> new ModIngot(INGOT_PROPERTIES.fireResistant().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> LAVA_INGOT = ITEMS.register("lava_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> MANA_NETHER_STAR = ITEMS.register("mana_nether_star", () -> new ModIngot(INGOT_PROPERTIES.fireResistant().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> MITHRILL = ITEMS.register("mithrill", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> MITHRILL_NUGGET = ITEMS.register("mithrill_nugget", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> NEBULA_FRAMGMENT = ITEMS.register("nebula_fragment", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> PIECE_OF_NEBULA = ITEMS.register("piece_of_nebula", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> MAT_INSCRIBED_INGOT = ITEMS.register("mat_inscribed_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> DRAGON_CRYTAL = ITEMS.register("dragoncrystal", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> FOCUS_INFUSION = ITEMS.register("focus_infusion", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> NATURE_INGOT = ITEMS.register("natureingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> NICKEL_INGOT = ITEMS.register("nickel_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> PLATINUM_INGOT = ITEMS.register("platinum_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> PRISMATICRADIANCEINGOT = ITEMS.register("prismaticradianceingot", () -> new ModIngot(INGOT_PROPERTIES.fireResistant().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> SPECTRITE_INGOT = ITEMS.register("spectrite_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> SPECTRITE_DUST = ITEMS.register("spectrite_dust", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> SPECTRITE_STAR = ITEMS.register("spectrite_star", () -> new ModIngot(INGOT_PROPERTIES.fireResistant().rarity(ModRarities.MIRACLE)));
    public static final RegistryObject<Item> SPECTRITE_GEM = ITEMS.register("spectrite_gem", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> WHITE_DUST = ITEMS.register("white_dust", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> WOOD_INGOT = ITEMS.register("wood_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.COMMON)));
    public static final RegistryObject<Item> CRYSTALLINE_STARDUST_INGOT = ITEMS.register("crystalline_stardust_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(ModRarities.MIRACLE)));
    public static final RegistryObject<Item> STARLIGHT_CRYSTALLINE_INGOT = ITEMS.register("starlight_crystalline_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(ModRarities.MIRACLE)));
    public static final RegistryObject<Item> TAINTED_ASTRAL_CRYSTAL_INGOT = ITEMS.register("tainted_astral_crystal_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(ModRarities.MIRACLE)));
    public static final RegistryObject<Item> NEBULA_INGOT = ITEMS.register("nebula_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(ModRarities.MIRACLE)));
    public static final RegistryObject<Item> NEBULA_STAR = ITEMS.register("nebula_star", () -> new ModIngot(INGOT_PROPERTIES.rarity(ModRarities.MIRACLE)));
    public static final RegistryObject<Item> NEBULA_NUGGET = ITEMS.register("nebula_nugget", () -> new ModIngot(INGOT_PROPERTIES.rarity(ModRarities.MIRACLE)));
    public static final RegistryObject<Item> PIECE_OF_WILD_HUNT = ITEMS.register("piece_of_wild_hunt", () -> new ModIngot(INGOT_PROPERTIES.rarity(ModRarities.MIRACLE)));
    public static final RegistryObject<Item> WILD_HUNT_NUGGET = ITEMS.register("wild_hunt_nugget", () -> new ModIngot(INGOT_PROPERTIES.rarity(ModRarities.MIRACLE)));
    public static final RegistryObject<Item> WILD_HUNT_FRAGMENT = ITEMS.register("wild_hunt_fragment", () -> new ModIngot(INGOT_PROPERTIES.rarity(ModRarities.MIRACLE)));
    public static final RegistryObject<Item> WILD_HUNT_INGOT = ITEMS.register("wild_hunt_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(ModRarities.MIRACLE)));
    public static final RegistryObject<Item> WILD_HUNT_STAR = ITEMS.register("wild_hunt_star", () -> new ModIngot(INGOT_PROPERTIES.rarity(ModRarities.MIRACLE)));
    public static final RegistryObject<Item> PLAGUE_BANE_INGOT = ITEMS.register("plague_bane_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(ModRarities.MIRACLE)));
    public static final RegistryObject<Item> DEAD_CORAL_INGOT = ITEMS.register("dead_coral_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(ModRarities.MIRACLE)));
    public static final RegistryObject<Item> ETERNAL_CATALYST = ITEMS.register("eternal_catalyst", () -> new ModIngot(INGOT_PROPERTIES.rarity(ModRarities.MIRACLE)));
    public static final RegistryObject<Item> GLACIAL_INGOT = ITEMS.register("glacial_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(ModRarities.MIRACLE)));
    public static final RegistryObject<Item> HALO_CATALYST = ITEMS.register("halo_catalyst", () -> new ModIngot(INGOT_PROPERTIES.rarity(ModRarities.MIRACLE)));
    public static final RegistryObject<Item> RAINBOW_CATALYST = ITEMS.register("rainbow_catalyst", () -> new ModIngot(INGOT_PROPERTIES.rarity(ModRarities.MIRACLE)));
    public static final RegistryObject<Item> FANTASTIC_CATALYST = ITEMS.register("fantastic_catalyst", () -> new ModIngot(INGOT_PROPERTIES.rarity(ModRarities.MIRACLE)));
    public static final RegistryObject<Item> DECODE_MATTER_ENIGMA = ITEMS.register("decode_matter_enigma", () -> new ModIngot(INGOT_PROPERTIES.rarity(ModRarities.MIRACLE)));
    public static final RegistryObject<Item> MYSTERIOUS_PRISM = ITEMS.register("mysterious_prism", () -> new ModIngot(INGOT_PROPERTIES.rarity(ModRarities.MIRACLE)));
    public static final RegistryObject<Item> ABYSS_INGOT = ITEMS.register("abyss_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(ModRarities.MIRACLE)));
    public static final RegistryObject<Item> GYRESTEEL = ITEMS.register("gyresteel", () -> new ModIngot(INGOT_PROPERTIES.rarity(ModRarities.MIRACLE)));
    public static final RegistryObject<Item> EMPYREAN_INGOT = ITEMS.register("empyrean_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(ModRarities.MIRACLE)));
    public static final RegistryObject<Item> MYSTICISM_INGOT = ITEMS.register("mysticism_ingot", () -> new ModIngot(INGOT_PROPERTIES.rarity(ModRarities.MIRACLE)));
    public static final RegistryObject<Item> RAINBOW_NUGGET = ITEMS.register("rainbow_nugget", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> RAINBOW_ORE_ROCK = ITEMS.register("rainbow_ore_rock", () -> new ModIngot(INGOT_PROPERTIES.rarity(Rarity.EPIC)));

    

    //奇点
    public static final RegistryObject<Item> RAINBOW_SINGULARITY = ITEMS.register("rainbow_singularity", ModSingularity::new);
    public static final RegistryObject<Item> WOODEN_SINGULARITY = ITEMS.register("wooden_singularity", ModSingularity::new);
    public static final RegistryObject<Item> MANYYULLYN_SINGULARITY = ITEMS.register("manyyullyn_singularity", ModSingularity::new);
    public static final RegistryObject<Item> COBALT_SINGULARITY = ITEMS.register("cobalt_singularity", ModSingularity::new);
    public static final RegistryObject<Item> GAIA_SINGULARITY = ITEMS.register("gaia_singularity", ModSingularity::new);
    public static final RegistryObject<Item> BEDROCK_SINGULARITY = ITEMS.register("bedrock_singularity", ModSingularity::new);
    public static final RegistryObject<Item> GOBBER_SINGULARITY_END = ITEMS.register("gobber_singularity_end", ModSingularity::new);
    public static final RegistryObject<Item> WHITE_MATTER_SINGULARITY = ITEMS.register("white_matter_singularity", ModSingularity::new);
    public static final RegistryObject<Item> POLONIUM_210_SINGULARITY = ITEMS.register("polonium_singularity", ModSingularity::new);
    public static final RegistryObject<Item> MANAITA_SINGULARITY = ITEMS.register("manaita_singularity", ModSingularity::new);
    public static final RegistryObject<Item> NEUTRON_SINGULARITY = ITEMS.register("neutron_singularity", ModSingularity::new);
    public static final RegistryObject<Item> YUANSHI_SINGULARITY = ITEMS.register("yuanshi_singularity", ModSingularity::new);
    public static final RegistryObject<Item> ORICHALCOS_SINGULARITY = ITEMS.register("orichalcos_singularity", ModSingularity::new);
    public static final RegistryObject<Item> SHADOWIUM_SINGULARITY = ITEMS.register("shadowium_singularity", ModSingularity::new);
    public static final RegistryObject<Item> CRYSTAL_MATRIX_SINGULARITY = ITEMS.register("crystal_matrix_singularity", ModSingularity::new);
    public static final RegistryObject<Item> EVIL_SINGULARITY = ITEMS.register("evil_singularity", ModSingularity::new);
    public static final RegistryObject<Item> COMPRESSED_INFINITY_SINGULARITY = ITEMS.register("compressed_infinity_singularity", ModSingularity::new);


    //决心
    public static final RegistryObject<Item> GREEN_DETERMINATION = ITEMS.register("green_determination", ModDetermination::new);
    public static final RegistryObject<Item> RED_DETERMINATION = ITEMS.register("red_determination", ModDetermination::new);
    public static final RegistryObject<Item> BLUE_DETERMINATION = ITEMS.register("blue_determination", ModDetermination::new);
    public static final RegistryObject<Item> ORANGE_DETERMINATION = ITEMS.register("orange_determination", ModDetermination::new);
    public static final RegistryObject<Item> YELLOW_DETERMINATION = ITEMS.register("yellow_determination", ModDetermination::new);
    public static final RegistryObject<Item> CYAN_DETERMINATION = ITEMS.register("cyan_determination", ModDetermination::new);
    public static final RegistryObject<Item> LIGHTER_PURPLE_DETERMINATION = ITEMS.register("lighter_purple_determination", ModDetermination::new);
    public static final RegistryObject<Item> BLACK_DETERMINATION = ITEMS.register("black_determination", ModDetermination::new);
    public static final RegistryObject<Item> PINK_DETERMINATION = ITEMS.register("pink_determination", ModDetermination::new);
    public static final RegistryObject<Item> DEEPER_PURPLE_DETERMINATION = ITEMS.register("deeper_purple_determination", ModDetermination::new);






    private static RegistryObject<Item> block(RegistryObject<Block> block) {
        return ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static RegistryObject<Item> blockFlower(RegistryObject<Block> block) {
        return ITEMS.register(block.getId().getPath(), () -> new ItemBlockFlower(block.get(), new Item.Properties()));
    }

    public static RegistryObject<Item> blockHyFlower(RegistryObject<Block> block) {
        return ITEMS.register(block.getId().getPath(), () -> new ItemHybridSpecialFlower(block.get(), new Item.Properties()));
    }
}
