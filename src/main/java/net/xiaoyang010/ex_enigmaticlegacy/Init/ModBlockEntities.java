package net.xiaoyang010.ex_enigmaticlegacy.Init;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.*;

import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Functional.*;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Generating.*;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.*;


public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, ExEnigmaticlegacyMod.MODID);
    public static final RegistryObject<BlockEntityType<InfinityChestEntity>> INFINITY_CHEST = register("infinity_chest_entity", ModBlockss.INFINITYCHEST, InfinityChestEntity::new);
    public static final RegistryObject<BlockEntityType<StarlitSanctumOfMystiqueBlockEntity>> STARLIT_SANCTUM_OF_MYSTIQUE = register("starlit_sanctum_of_mystique", ModBlockss.STARLITSANCTUM, StarlitSanctumOfMystiqueBlockEntity::new);
    public static final RegistryObject<BlockEntityType<CelestialHTTile>> CELESTIAL_HOLINESS_TRANSMUTER_TILE = register("celestial_holiness_transmuter_tile", ModBlockss.CELESTIAL_HOLINESS_TRANSMUTER, CelestialHTTile::new);
    public static final RegistryObject<BlockEntityType<PagedChestBlockEntity>> PAGED_CHEST = REGISTRY.register("paged_chest", () -> BlockEntityType.Builder.of(PagedChestBlockEntity::new, ModBlockss.PAGED_CHEST.get()).build(null));
    public static final RegistryObject<BlockEntityType<RainbowTableTile>> RAINBOW_TABLE_TILE = REGISTRY.register("rainbow_table_tile", () -> BlockEntityType.Builder.of(RainbowTableTile::new, ModBlockss.RAINBOW_TABLE.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileNidavellirForge>> NIDAVELLIR_FORGE_TILE = REGISTRY.register("nidavellir_forge_tile", () -> BlockEntityType.Builder.of(TileNidavellirForge::new, ModBlockss.NIDAVELLIR_FORGE.get()).build(null));
    public static final RegistryObject<BlockEntityType<SpectriteChestTile>> SPECTRITE_CHEST_TILE = REGISTRY.register("spectrite_chest", () -> BlockEntityType.Builder.of(SpectriteChestTile::new, ModBlockss.SPECTRITE_CHEST.get()).build(null));


    //botania
    public static final RegistryObject<BlockEntityType<InfinityPotatoEntity>> INFINITY_POTATO = register("infinity_potato", ModBlockss.INFINITY_POTATO, InfinityPotatoEntity::new);
    public static final RegistryObject<BlockEntityType<NightshadeTile>> NIGHTSHADE_TILE = register("nightshade_tile", ModBlockss.NIGHTSHADE, NightshadeTile::new);
    public static final RegistryObject<BlockEntityType<InfinityGaiaSpreaderTile>> INFINITY_SPREADER = register("infinity_spreader", ModBlockss.infinitySpreader, InfinityGaiaSpreaderTile::new);
    public static final RegistryObject<BlockEntityType<TileManaCharger>> MANA_CHARGER_TILE = REGISTRY.register("mana_charger", () -> BlockEntityType.Builder.of((pos, state) -> new TileManaCharger(ModBlockEntities.MANA_CHARGER_TILE.get(), pos, state), ModBlockss.MANA_CHARGER.get()).build(null));
    public static final RegistryObject<BlockEntityType<ManaCrystalCubeBlockEntity>> MANA_CRYSTAL_TILE = REGISTRY.register("mana_crystal", () -> BlockEntityType.Builder.of((pos, state) -> new ManaCrystalCubeBlockEntity(ModBlockEntities.MANA_CRYSTAL_TILE.get(), pos, state), ModBlockss.MANA_CRYSTAL.get()).build(null));
    public static final RegistryObject<BlockEntityType<PolychromeCollapsePrismTile>> POLYCHROME_COLLAPSE_PRISM_TILE = REGISTRY.register("polychrome_collapse_prism", () -> BlockEntityType.Builder.of((pos, state) -> new PolychromeCollapsePrismTile(ModBlockEntities.POLYCHROME_COLLAPSE_PRISM_TILE.get(), pos, state), ModBlockss.POLYCHROME_COLLAPSE_PRISM.get()).build(null));

    public static final RegistryObject<BlockEntityType<AlchemyAzaleaTile>> ALCHEMY_AZALEA_TILE = REGISTRY.register("alchemy_azalea_tile", () -> BlockEntityType.Builder.of((pos, state) -> new AlchemyAzaleaTile(ModBlockEntities.ALCHEMY_AZALEA_TILE.get(), pos, state), ModBlockss.ALCHEMY_AZALEA.get()).build(null));
    public static final RegistryObject<BlockEntityType<CurseThistleTile>> CURSET_THISTLE_TILE = REGISTRY.register("curse_thistle_tile", () -> BlockEntityType.Builder.of((pos, state) -> new CurseThistleTile(ModBlockEntities.CURSET_THISTLE_TILE.get(), pos, state), ModBlockss.CURSET_THISTLE.get()).build(null));
    public static final RegistryObject<BlockEntityType<YushouCloverTile>> YU_SHOU_CLOVER_TILE = REGISTRY.register("yu_shou_clover_tile", () -> BlockEntityType.Builder.of((pos, state) -> new YushouCloverTile(ModBlockEntities.YU_SHOU_CLOVER_TILE.get(), pos, state), ModBlockss.YU_SHOU_CLOVER.get()).build(null));
    public static final RegistryObject<BlockEntityType<VacuityTile>> VACUITY_TILE = REGISTRY.register("vacuity_tile", () -> BlockEntityType.Builder.of((pos, state) -> new VacuityTile(ModBlockEntities.VACUITY_TILE.get(), pos, state), ModBlockss.VACUITY.get()).build(null));
    public static final RegistryObject<BlockEntityType<RainbowGeneratingFlowerTile>> RAINBOW_GENERATING_FLOWER_TILE = REGISTRY.register("rainbow_generating_flower", () -> BlockEntityType.Builder.of((pos, state) -> new RainbowGeneratingFlowerTile(ModBlockEntities.RAINBOW_GENERATING_FLOWER_TILE.get(), pos, state), ModBlockss.RAINBOW_GENERATING_FLOWER.get()).build(null));
    public static final RegistryObject<BlockEntityType<StreetLightFlowerTile>> STREET_LIGHT_TILE = REGISTRY.register("street_light_tile", () -> BlockEntityType.Builder.of((pos, state) -> new StreetLightFlowerTile(ModBlockEntities.STREET_LIGHT_TILE.get(), pos, state), ModBlockss.STREET_LIGHT.get()).build(null));
    public static final RegistryObject<BlockEntityType<BlazingOrchidFlowerTile>> BLAZING_ORCHID_TILE = REGISTRY.register("blazing_orchid_tile", () -> BlockEntityType.Builder.of((pos, state) -> new BlazingOrchidFlowerTile(ModBlockEntities.BLAZING_ORCHID_TILE.get(), pos, state), ModBlockss.BLAZING_ORCHID.get()).build(null));
    public static final RegistryObject<BlockEntityType<BelieverTile>> BELIEVERTILE = REGISTRY.register("believertile", () -> BlockEntityType.Builder.of((pos, state) -> new BelieverTile(ModBlockEntities.BELIEVERTILE.get(), pos, state), ModBlockss.BELIEVER.get()).build(null));
    public static final RegistryObject<BlockEntityType<MingXianLanTile>> MINGXIANLAN_TILE = REGISTRY.register("mingxianlan_tile", () -> BlockEntityType.Builder.of((pos, state) -> new MingXianLanTile(ModBlockEntities.MINGXIANLAN_TILE.get(), pos, state), ModBlockss.MINGXIANLAN.get()).build(null));
    public static final RegistryObject<BlockEntityType<AstralKillopTile>> ASTRAL_KILLOP_TILE = REGISTRY.register("astral_killop_tile", () -> BlockEntityType.Builder.of((pos, state) -> new AstralKillopTile(ModBlockEntities.ASTRAL_KILLOP_TILE.get(), pos, state), ModBlockss.ASTRAL_KILLOP.get()).build(null));
    public static final RegistryObject<BlockEntityType<GenEnergydandronTile>> GENENERGYDANDRON = REGISTRY.register("gen_energydandron", () -> BlockEntityType.Builder.of((pos, state) -> new GenEnergydandronTile(ModBlockEntities.GENENERGYDANDRON.get(), pos, state), ModBlockss.GENENERGYDANDRON.get()).build(null));
    public static final RegistryObject<BlockEntityType<KillingBerryTile>> KILLING_BERRY_TILE = REGISTRY.register("killing_berry_tile", () -> BlockEntityType.Builder.of((pos, state) -> new KillingBerryTile(ModBlockEntities.KILLING_BERRY_TILE.get(), pos, state), ModBlockss.KILLING_BERRY.get()).build(null));
    public static final RegistryObject<BlockEntityType<DarkNightGrassTile>> DARK_NIGHT_GRASS_TILE = REGISTRY.register("dark_night_grass_tile", () -> BlockEntityType.Builder.of((pos, state) -> new DarkNightGrassTile(ModBlockEntities.DARK_NIGHT_GRASS_TILE.get(), pos, state), ModBlockss.DARK_NIGHT_GRASS.get()).build(null));
    public static final RegistryObject<BlockEntityType<FrostLotusFlowerTile>> FROST_LOTUS_TILE = REGISTRY.register("frost_lotus_tile", () -> BlockEntityType.Builder.of((pos, state) -> new FrostLotusFlowerTile(ModBlockEntities.FROST_LOTUS_TILE.get(), pos, state), ModBlockss.FROST_LOTUS.get()).build(null));
    public static final RegistryObject<BlockEntityType<LycorisradiataTile>> LYCORISRADIATA_TILE = REGISTRY.register("lycorisradiata_tile", () -> BlockEntityType.Builder.of((pos, state) -> new LycorisradiataTile(ModBlockEntities.LYCORISRADIATA_TILE.get(), pos, state), ModBlockss.LYCORISRADIATA.get()).build(null));
    public static final RegistryObject<BlockEntityType<FrostBlossomTile>> FROST_BLOSSOM_TILE = REGISTRY.register("frost_blossom_tile", () -> BlockEntityType.Builder.of((pos, state) -> new FrostBlossomTile(ModBlockEntities.FROST_BLOSSOM_TILE.get(), pos, state), ModBlockss.FROST_BLOSSOM.get()).build(null));
    public static final RegistryObject<BlockEntityType<AlchemySunflowerTile>> ALCHEMY_SUNFLOWER_TILE = REGISTRY.register("alchemy_sunflower_tile", () -> BlockEntityType.Builder.of((pos, state) -> new AlchemySunflowerTile(ModBlockEntities.ALCHEMY_SUNFLOWER_TILE.get(), pos, state), ModBlockss.ALCHEMY_SUNFLOWER.get()).build(null));

    public static final RegistryObject<BlockEntityType<DaybloomBlockTile>> DAYBLOOM_TILE = register("daybloom_tile", ModBlockss.DAYBLOOM, DaybloomBlockTile::new);
    public static final RegistryObject<BlockEntityType<OrechidEndiumTile>> ORECHIDENDIUMTILE = register("orechidendiumtile", ModBlockss.ORECHIDENDIUM, OrechidEndiumTile::new);
    public static final RegistryObject<BlockEntityType<SoarleanderTile>> SOARLEANDERTILE = register("soarleander", ModBlockss.SOARLEANDER, SoarleanderTile::new);
    public static final RegistryObject<BlockEntityType<AsgardFlowerTile>> ASGARDANDELIONTILE = register("asgardandeliontile", ModBlockss.ASGARDANDELION, AsgardFlowerTile::new);
    public static final RegistryObject<BlockEntityType<FloweyTile>> FLOWEYTILE = register("floweytile", ModBlockss.FLOWEY, FloweyTile::new);
    public static final RegistryObject<BlockEntityType<WitchOpoodTile>> WITCH_OPOOD_TILE = register("witch_opood_tile", ModBlockss.WITCH_OPOOD, WitchOpoodTile::new);

    private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> register(String registryName, RegistryObject<Block> block,
                                                                                       BlockEntityType.BlockEntitySupplier<T> supplier) {
        return REGISTRY.register(registryName, () -> BlockEntityType.Builder.of(supplier, block.get()).build(null));
    }

    @Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEventSubscriber {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
        }
    }
}
