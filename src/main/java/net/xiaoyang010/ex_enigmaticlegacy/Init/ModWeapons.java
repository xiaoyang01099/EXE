package net.xiaoyang010.ex_enigmaticlegacy.Init;

import net.minecraft.world.item.*;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.*;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.AquaSword;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.TerraBow;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Item.weapon.*;
import net.xiaoyang010.ex_enigmaticlegacy.Item.weapon.WIP.*;

public class ModWeapons {
    public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, ExEnigmaticlegacyMod.MODID);

    public static final RegistryObject<Item> ANNIHILATION_SWORD = REGISTRY.register("annihilation_sword", AnnihilationSword::new);
    public static final RegistryObject<Item> MANAITASWORDGOD = REGISTRY.register("manaitaswordgod", ManaitaSwordGod::new);
    public static final RegistryObject<Item> MANAITABOW = REGISTRY.register("manaitabow", ManaitaBow::new);
    public static final RegistryObject<Item> REALFINALSWORDGOD = REGISTRY.register("realfinalswordgod", RealFinalSwordGod::new);
    public static final RegistryObject<Item> WASTELAYER = REGISTRY.register("wastelayer", Wastelayer::new);
    public static final RegistryObject<Item> PARADOX = REGISTRY.register("paradox", Paradox::new);
    public static final RegistryObject<Item> CRISSAEGRIM = REGISTRY.register("crissaegrim", Crissaegrim::new);
    public static final RegistryObject<Item> OBSIDIAN_EDGE = REGISTRY.register("obsidian_edge", ObsidianEdge::new);
    public static final RegistryObject<Item> CRYSTAL_DAGGER = REGISTRY.register("crystal_dagger", CrystalDagger::new);
    public static final RegistryObject<Item> CLAYMORE = REGISTRY.register("claymore", () -> new Claymore(90.0F));
    public static final RegistryObject<Item> WICKEDKRIS = REGISTRY.register("wickedkris", WickedKris::new);
    public static final RegistryObject<Item> BLADE_FALLEN_STAR = REGISTRY.register("blade_fallen_star", BladeFallenStar::new);
    public static final RegistryObject<Item> KISS_OF_NYX = REGISTRY.register("kiss_of_nyx", KissOfNyx::new);
    public static final RegistryObject<Item> ICEFORGED_EXCALIBUR = REGISTRY.register("iceforged_excalibur", IceforgedExcalibur::new);
    public static final RegistryObject<Item> JUDGMENT_OF_AURORA = REGISTRY.register("judgment_of_aurora", JudgmentOfAurora::new);
    public static final RegistryObject<Item> SONG_OF_THE_ABYSS = REGISTRY.register("song_of_the_abyss", SongOfTheAbyss::new);
    public static final RegistryObject<Item> SPACE_BLADE = REGISTRY.register("space_blade", () -> new SpaceBlade(new Item.Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA)));
    public static final RegistryObject<Item> NATURAL_THOUSAND_BLADES = REGISTRY.register("natural_thousand_blades", () -> new NaturalThousandBlades(new Item.Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA)));
    public static final RegistryObject<Item> AQUA_SWORD = REGISTRY.register("aqua_sword", () -> new AquaSword(new Item.Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA)));
    public static final RegistryObject<Item> TERRA_BOW = REGISTRY.register("terra_bow", () -> new TerraBow(new Item.Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA)));
    public static final RegistryObject<Item> HORN_STONE_SWORD = REGISTRY.register("horn_stone_sword", HornstoneSword::new);
    public static final RegistryObject<Item> COSMIC = REGISTRY.register("cosmic", Cosmic::new);
    public static final RegistryObject<Item> COSMIC_BREAKER = REGISTRY.register("cosmic_breaker", CosmicBreaker::new);
    public static final RegistryObject<Item> COSMIC_ANNIHILATOR_BLADE = REGISTRY.register("cosmic_annihilator_blade", CosmicAnnihilatorBlade::new);
    public static final RegistryObject<Item> END_BROAD_SWORD = REGISTRY.register("end_broad_sword",EndBroadSword::new);
    public static final RegistryObject<Item> HALLOWED_EDGE = REGISTRY.register("hallowed_edge", HallowedEdge::new);
    public static final RegistryObject<Item> HARVESTER = REGISTRY.register("harvester", Harvester::new);
    public static final RegistryObject<Item> HEAVENLY_CHIMES = REGISTRY.register("heavenly_chimes", HeavenlyChimes::new);
    public static final RegistryObject<Item> SWORD_DARK_MATTER = REGISTRY.register("sword_dark_matter", SwordDarkMatter::new);
    public static final RegistryObject<Item> DRAGONS_LAYER = REGISTRY.register("dragons_layer", Dragonslayer::new);
    public static final RegistryObject<Item> FLARE_BRINGER = REGISTRY.register("flare_bringer", Flarebringer::new);
    public static final RegistryObject<Item> SHADOW_BREAKER = REGISTRY.register("shadow_breaker", Shadowbreaker::new);
    public static final RegistryObject<Item> GAIA_KILLER = REGISTRY.register("gaia_killer", GaiaSlayer::new);

}
