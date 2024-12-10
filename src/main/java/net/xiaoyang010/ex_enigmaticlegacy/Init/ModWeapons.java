package net.xiaoyang010.ex_enigmaticlegacy.Init;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Item.weapon.*;

public class ModWeapons {
    public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, ExEnigmaticlegacyMod.MODID);
    public static final RegistryObject<Item> MANAITASWORDGOD = REGISTRY.register("manaitaswordgod", () -> new ManaitaSwordGod());
    public static final RegistryObject<Item> MANAITABOW = REGISTRY.register("manaitabow", () -> new ManaitaBow());
    public static final RegistryObject<Item> REALFINALSWORDGOD = REGISTRY.register("realfinalswordgod", () -> new RealFinalSwordGod());
    public static final RegistryObject<Item> WASTELAYER = REGISTRY.register("wastelayer", () -> new Wastelayer());
    public static final RegistryObject<Item> PARADOX = REGISTRY.register("paradox", () -> new Paradox());
    public static final RegistryObject<Item> CRISSAEGRIM = REGISTRY.register("crissaegrim", () -> new Crissaegrim());
    public static final RegistryObject<Item> OBSIDIAN_EDGE = REGISTRY.register("obsidian_edge", () -> new ObsidianEdge());
    public static final RegistryObject<Item> CRYSTAL_DAGGER = REGISTRY.register("crystal_dagger", () -> new CrystalDagger());
    public static final RegistryObject<Item> CLAYMORE = REGISTRY.register("claymore", () -> new Claymore());
    public static final RegistryObject<Item> WICKEDKRIS= REGISTRY.register("wickedkris", () -> new WickedKris());
    public static final RegistryObject<Item> BLADE_FALLEN_STAR= REGISTRY.register("blade_fallen_star", () -> new BladeFallenStar());
    public static final RegistryObject<Item> KISS_OF_NYX= REGISTRY.register("kiss_of_nyx", () -> new KissOfNyx());
    public static final RegistryObject<Item> ICEFORGED_EXCALIBUR= REGISTRY.register("iceforged_excalibur", () -> new IceforgedExcalibur());
    public static final RegistryObject<Item> JUDGMENT_OF_AURORA= REGISTRY.register("judgment_of_aurora", () -> new JudgmentOfAurora());
    public static final RegistryObject<Item> SONG_OF_THE_ABYSS= REGISTRY.register("song_of_the_abyss", () -> new SongOfTheAbyss());



}
