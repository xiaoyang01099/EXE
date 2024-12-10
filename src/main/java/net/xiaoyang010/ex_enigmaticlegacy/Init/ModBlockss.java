
package net.xiaoyang010.ex_enigmaticlegacy.Init;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.xiaoyang010.ex_enigmaticlegacy.Block.*;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.level.block.Block;
import net.xiaoyang010.ex_enigmaticlegacy.Block.StarlitSanctumOfMystique;
import net.xiaoyang010.ex_enigmaticlegacy.Block.ore.EndOre;
import net.xiaoyang010.ex_enigmaticlegacy.Block.ore.IridiumOre;
import net.xiaoyang010.ex_enigmaticlegacy.Block.ore.NickelOre;
import net.xiaoyang010.ex_enigmaticlegacy.Block.ore.PlatinumOre;
import net.xiaoyang010.ex_enigmaticlegacy.Block.portal.AnotherPortalBlock;
import net.xiaoyang010.ex_enigmaticlegacy.Block.portal.MinersHeavenPortalBlock;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.GenEnergydandron;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.InfinityPotato;
import vazkii.botania.common.block.BlockSpecialFlower;

public class ModBlockss {
	public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, ExEnigmaticlegacyMod.MODID);
	public static final Properties FLOWER_PROPS = Properties.copy(Blocks.POPPY);


	//botania
	public static final RegistryObject<Block> NIGHTSHADE = REGISTRY.register("nightshade",() -> new BlockSpecialFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.NIGHTSHADE_TILE::get));
	public static final RegistryObject<Block> ASTRAL_KILLOP = REGISTRY.register("astral_killop",() -> new BlockSpecialFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.ASTRAL_KILLOP_TILE::get));
	public static final RegistryObject<Block> DAYBLOOM = REGISTRY.register("daybloom",() -> new BlockSpecialFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.DAYBLOOM_TILE::get));
	public static final RegistryObject<Block> ASGARDANDELION = REGISTRY.register("asgardandelion",() -> new BlockSpecialFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.ASGARDANDELIONTILE::get));
	public static final RegistryObject<Block> FLOWEY = REGISTRY.register("flowey",() -> new BlockSpecialFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.FLOWEYTILE::get));
    public static final RegistryObject<Block> BELIEVER = REGISTRY.register("believer",() -> new BlockSpecialFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.BELIEVERTILE::get));
	public static final RegistryObject<Block> SOARLEANDER = REGISTRY.register("soarleander", () -> new BlockSpecialFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.SOARLEANDERTILE::get));
	public static final RegistryObject<Block> ORECHIDENDIUM = REGISTRY.register("orechid_endium", () -> new BlockSpecialFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.ORECHIDENDIUMTILE::get));
	public static final RegistryObject<Block> WITCH_OPOOD = REGISTRY.register("witch_opood", () -> new BlockSpecialFlower(MobEffects.HEALTH_BOOST, 360, FLOWER_PROPS, ModBlockEntities.WITCH_OPOOD_TILE::get));
	public static final RegistryObject<Block> INFINITY_POTATO = REGISTRY.register("infinity_potato", () -> new InfinityPotato());
	public static final RegistryObject<Block> GENENERGYDANDRON = REGISTRY.register("gen_energydandron", () -> new GenEnergydandron());




	//其他
	public static final RegistryObject<Block> INFINITYGlASS = REGISTRY.register("infinityglass",()->new InfinityGlass());
	public static final RegistryObject<Block> STARLITSANCTUM = REGISTRY.register("starlit_sanctum", () -> new StarlitSanctumOfMystique());
	public static final RegistryObject<Block> CELESTIAL_HOLINESS_TRANSMUTER = REGISTRY.register("celestial_holiness_transmuter", () -> new CelestialHolinessTransmuter());
	public static final RegistryObject<Block> ENDLESS_CAKE = REGISTRY.register("endless_cake", () -> new EndlessCakeBlock());
	public static final RegistryObject<Block> FLUFFY_DANDELION = REGISTRY.register("fluffy_dandelion", () -> new FluffyDandelionBlock());
	public static final RegistryObject<Block> COBBLE_STONE = REGISTRY.register("cobble_stone", () -> new Cobblestone());
	public static final RegistryObject<Block> BLOCKNATURE = REGISTRY.register("blocknature", () -> new BlocknatureBlock());
	public static final RegistryObject<Block> EVILBLOCK = REGISTRY.register("evilblock", () -> new EvilBlock());
	public static final RegistryObject<Block> PRISMATICRADIANCEBLOCK = REGISTRY.register("prismaticradianceblock", () -> new PrismaticRadianceBlock());
	public static final RegistryObject<Block> INFINITYCHEST = REGISTRY.register("infinity_chest", () -> new InfinityChest());
	public static final RegistryObject<Block> IRIDIUM_BLOCK = REGISTRY.register("iridium_block", () -> new IridiumBlock());
	public static final RegistryObject<Block> NICKEL_BLOCK = REGISTRY.register("nickel_block", () -> new NickelBlock());
	public static final RegistryObject<Block> AERIALITE_BLOCK = REGISTRY.register("aerialite_block", () -> new AerialiteBlock());




	//矿物
	public static final RegistryObject<Block> IRIDIUM_ORE = REGISTRY.register("iridium_ore", () -> new IridiumOre());
	public static final RegistryObject<Block> NICKEL_ORE = REGISTRY.register("nickel_ore", () -> new NickelOre());
	public static final RegistryObject<Block> PLATINUM_ORE = REGISTRY.register("platinum_ore", () -> new PlatinumOre());
	public static final RegistryObject<Block> END_ORE = REGISTRY.register("end_ore", () -> new EndOre());

	//传送门方块
	public static final RegistryObject<Block> MINERS_HEAVEN_PORTAL = REGISTRY.register("heaven_portal", () -> new MinersHeavenPortalBlock());
	public static final RegistryObject<Block> ANOTHER_PORTAL = REGISTRY.register("another_portal", () -> new AnotherPortalBlock());

}