package net.xiaoyang010.ex_enigmaticlegacy.Init;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.NaturalThousandBlades;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.SpaceBlade;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Item.weapon.*;

public class ModWeapons {
    public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, ExEnigmaticlegacyMod.MODID);
    public static final RegistryObject<Item> MANAITASWORDGOD = REGISTRY.register("manaitaswordgod", ManaitaSwordGod::new);
    public static final RegistryObject<Item> MANAITABOW = REGISTRY.register("manaitabow", ManaitaBow::new);
    public static final RegistryObject<Item> REALFINALSWORDGOD = REGISTRY.register("realfinalswordgod", RealFinalSwordGod::new);
    public static final RegistryObject<Item> WASTELAYER = REGISTRY.register("wastelayer", Wastelayer::new);
    public static final RegistryObject<Item> PARADOX = REGISTRY.register("paradox", Paradox::new);
    public static final RegistryObject<Item> CRISSAEGRIM = REGISTRY.register("crissaegrim", Crissaegrim::new);
    public static final RegistryObject<Item> OBSIDIAN_EDGE = REGISTRY.register("obsidian_edge", ObsidianEdge::new);
    public static final RegistryObject<Item> CRYSTAL_DAGGER = REGISTRY.register("crystal_dagger", CrystalDagger::new);
    public static final RegistryObject<Item> CLAYMORE = REGISTRY.register("claymore", Claymore::new);
    public static final RegistryObject<Item> WICKEDKRIS = REGISTRY.register("wickedkris", WickedKris::new);
    public static final RegistryObject<Item> BLADE_FALLEN_STAR = REGISTRY.register("blade_fallen_star", BladeFallenStar::new);
    public static final RegistryObject<Item> KISS_OF_NYX = REGISTRY.register("kiss_of_nyx", KissOfNyx::new);
    public static final RegistryObject<Item> ICEFORGED_EXCALIBUR = REGISTRY.register("iceforged_excalibur", IceforgedExcalibur::new);
    public static final RegistryObject<Item> JUDGMENT_OF_AURORA = REGISTRY.register("judgment_of_aurora", JudgmentOfAurora::new);
    public static final RegistryObject<Item> SONG_OF_THE_ABYSS = REGISTRY.register("song_of_the_abyss", SongOfTheAbyss::new);
    public static final RegistryObject<Item> SPACE_BLADE = REGISTRY.register("space_blade", () -> new SpaceBlade(new Item.Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA)));
    public static final RegistryObject<Item> NATURAL_THOUSAND_BLADES = REGISTRY.register("natural_thousand_blades", () -> new NaturalThousandBlades(new Item.Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA)));

}
