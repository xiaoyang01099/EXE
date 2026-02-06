package net.xiaoyang010.ex_enigmaticlegacy.Init;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.DyeColor;
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
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Avaritia.CosmicBlockEntity;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Hybrid.TileEntityAquaticAnglerNarcissus;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Hybrid.TileEntityRuneFlower;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.BlockItemManaBox;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.AstralBlockEntity;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile.*;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Functional.*;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Generating.*;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.*;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.TileEntityExtremeAutoCrafter;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.NeutroniumDecompressorTile;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile.TileLebethronCore;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Avaritia.StarrySkyBlockEntity;
import net.xiaoyang010.ex_enigmaticlegacy.api.test.res.TileCursedGourmaryllis;
import net.xiaoyang010.ex_enigmaticlegacy.api.test.res.TileCursedManaPool;
import net.xiaoyang010.ex_enigmaticlegacy.api.test.res.TileCursedManaSpreader;
import net.xiaoyang010.ex_enigmaticlegacy.api.test.res.TileManaConverter;


public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, ExEnigmaticlegacyMod.MODID);
    public static final RegistryObject<BlockEntityType<InfinityChestTile>> INFINITY_CHEST = register("infinity_chest_entity", ModBlockss.INFINITYCHEST, InfinityChestTile::new);
    public static final RegistryObject<BlockEntityType<StarlitSanctumTile>> STARLIT_SANCTUM_OF_MYSTIQUE = register("starlit_sanctum_of_mystique", ModBlockss.STARLIT_SANCTUM, StarlitSanctumTile::new);
    public static final RegistryObject<BlockEntityType<CelestialHTTile>> CELESTIAL_HOLINESS_TRANSMUTER_TILE = register("celestial_holiness_transmuter_tile", ModBlockss.CELESTIAL_HOLINESS_TRANSMUTER, CelestialHTTile::new);
    public static final RegistryObject<BlockEntityType<PagedChestBlockTile>> PAGED_CHEST = REGISTRY.register("paged_chest", () -> BlockEntityType.Builder.of(PagedChestBlockTile::new, ModBlockss.PAGED_CHEST.get()).build(null));
    public static final RegistryObject<BlockEntityType<RainbowTableTile>> RAINBOW_TABLE_TILE = REGISTRY.register("rainbow_table_tile", () -> BlockEntityType.Builder.of(RainbowTableTile::new, ModBlockss.RAINBOW_TABLE.get()).build(null));
    public static final RegistryObject<BlockEntityType<NidavellirForgeTile>> NIDAVELLIR_FORGE_TILE = REGISTRY.register("nidavellir_forge_tile", () -> BlockEntityType.Builder.of(NidavellirForgeTile::new, ModBlockss.NIDAVELLIR_FORGE.get()).build(null));
    public static final RegistryObject<BlockEntityType<SpectriteChestTile>> SPECTRITE_CHEST_TILE = REGISTRY.register("spectrite_chest", () -> BlockEntityType.Builder.of(SpectriteChestTile::new, ModBlockss.SPECTRITE_CHEST.get()).build(null));
    public static final RegistryObject<BlockEntityType<AstralBlockEntity>> ASTRAL_BLOCK_ENTITY = REGISTRY.register("astral_block", () -> BlockEntityType.Builder.of((pos, state) -> new AstralBlockEntity(ModBlockEntities.ASTRAL_BLOCK_ENTITY.get(), pos, state), ModBlockss.ASTRAL_BLOCK.get()).build(null));
//    public static final RegistryObject<BlockEntityType<MagicTableBlockEntity>> MAGIC_TABLE_TILE = REGISTRY.register("magic_table_tile", () -> BlockEntityType.Builder.of(MagicTableBlockEntity::new, ModBlockss.MAGIC_TABLE.get()).build(null));
    public static final RegistryObject<BlockEntityType<CosmicBlockEntity>> COSMIC_BLOCK_ENTITY = REGISTRY.register("cosmic_block", () -> BlockEntityType.Builder.of((pos, state) -> new CosmicBlockEntity(ModBlockEntities.COSMIC_BLOCK_ENTITY.get(), pos, state), ModBlockss.COSMIC_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileEntityExtremeAutoCrafter>> EXTREME_AUTO_CRAFTER_TILE = REGISTRY.register("extreme_auto_crafter_tile", () -> BlockEntityType.Builder.of((pos, state) -> new TileEntityExtremeAutoCrafter(ModBlockEntities.EXTREME_AUTO_CRAFTER_TILE.get(), pos, state), ModBlockss.EXTREME_AUTO_CRAFTER.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileEntityInfinityCompressor>> INFINITY_COMPRESSOR_TILE = REGISTRY.register("infinity_compressor_tile", () -> BlockEntityType.Builder.of((pos, state) -> new TileEntityInfinityCompressor(ModBlockEntities.INFINITY_COMPRESSOR_TILE.get(), pos, state), ModBlockss.INFINITY_COMPRESSOR.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileManaBox>> MANA_BOX_TILE = REGISTRY.register("mana_box_tile", () -> BlockEntityType.Builder.of((pos, state) -> new TileManaBox(ModBlockEntities.MANA_BOX_TILE.get(), pos, state), ModBlockss.MANA_BOX.get()).build(null));
    public static final RegistryObject<BlockEntityType<NeutroniumDecompressorTile>> NEUTRONIUM_DECOMPRESSOR_TILE = REGISTRY.register("neutronium_decompressor_tile", () -> BlockEntityType.Builder.of((pos, state) -> new NeutroniumDecompressorTile(ModBlockEntities.NEUTRONIUM_DECOMPRESSOR_TILE.get(), pos, state), ModBlockss.NEUTRONIUM_DECOMPRESSOR.get()).build(null));
    public static final RegistryObject<BlockEntityType<StarrySkyBlockEntity>> STARRY_SKY_BLOCK_ENTITY = REGISTRY.register("starry_block", () -> BlockEntityType.Builder.of((pos, state) -> new StarrySkyBlockEntity(ModBlockEntities.STARRY_SKY_BLOCK_ENTITY.get(), pos, state), ModBlockss.STARRY_SKY_BLOCK.get()).build(null));


    public static final RegistryObject<BlockEntityType<TileCursedManaPool>> CURSED_MANA_POOL =
            REGISTRY.register("cursed_mana_pool", () ->
                    BlockEntityType.Builder.of(TileCursedManaPool::new,
                            ModBlockss.CURSED_MANA_POOL.get(),
                            ModBlockss.CURSED_MANA_POOL_CREATIVE.get(),
                            ModBlockss.CURSED_MANA_POOL_DILUTED.get(),
                            ModBlockss.CURSED_MANA_POOL_CORRUPTED.get()
                    ).build(null));

    public static final RegistryObject<BlockEntityType<TileCursedManaSpreader>> CURSED_SPREADER =
            REGISTRY.register("cursed_spreader", () ->
                    BlockEntityType.Builder.of(TileCursedManaSpreader::new,
                            ModBlockss.CURSED_MANA_SPREADER.get(),
                            ModBlockss.CORRUPTED_MANA_SPREADER.get(),
                            ModBlockss.VOID_MANA_SPREADER.get()
                    ).build(null));

    public static final RegistryObject<BlockEntityType<TileManaConverter>> MANA_CONVERTER =
            REGISTRY.register("mana_converter", () ->
                    BlockEntityType.Builder.of(TileManaConverter::new,
                            ModBlockss.MANA_CONVERTER_NORMAL_TO_CURSED.get(),
                            ModBlockss.MANA_CONVERTER_CURSED_TO_NORMAL.get()
                    ).build(null));


    //botania
    public static final RegistryObject<BlockEntityType<InfinityPotatoTile>> INFINITY_POTATO = register("infinity_potato", ModBlockss.INFINITY_POTATO, InfinityPotatoTile::new);
    public static final RegistryObject<BlockEntityType<NightshadeTile>> NIGHTSHADE_TILE = register("nightshade_tile", ModBlockss.NIGHTSHADE, NightshadeTile::new);
    public static final RegistryObject<BlockEntityType<InfinityGaiaSpreaderTile>> INFINITY_SPREADER = register("infinity_spreader", ModBlockss.infinitySpreader, InfinityGaiaSpreaderTile::new);
    public static final RegistryObject<BlockEntityType<ManaChargerTile>> MANA_CHARGER_TILE = REGISTRY.register("mana_charger", () -> BlockEntityType.Builder.of((pos, state) -> new ManaChargerTile(ModBlockEntities.MANA_CHARGER_TILE.get(), pos, state), ModBlockss.MANA_CHARGER.get()).build(null));
    public static final RegistryObject<BlockEntityType<ManaCrystalCubeBlockTile>> MANA_CRYSTAL_TILE = REGISTRY.register("mana_crystal", () -> BlockEntityType.Builder.of((pos, state) -> new ManaCrystalCubeBlockTile(ModBlockEntities.MANA_CRYSTAL_TILE.get(), pos, state), ModBlockss.MANA_CRYSTAL.get()).build(null));
    public static final RegistryObject<BlockEntityType<PolychromeCollapsePrismTile>> POLYCHROME_COLLAPSE_PRISM_TILE = REGISTRY.register("polychrome_collapse_prism", () -> BlockEntityType.Builder.of((pos, state) -> new PolychromeCollapsePrismTile(ModBlockEntities.POLYCHROME_COLLAPSE_PRISM_TILE.get(), pos, state), ModBlockss.POLYCHROME_COLLAPSE_PRISM.get()).build(null));
    public static final RegistryObject<BlockEntityType<FullAltarTile>> FULL_ALTAR_TILE = REGISTRY.register("full_altar", () -> BlockEntityType.Builder.of((pos, state) -> new FullAltarTile(ModBlockEntities.FULL_ALTAR_TILE.get(), pos, state), ModBlockss.FULL_ALTAR.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileBoardFate>> BOARD_FATE_TILE = REGISTRY.register("board_fate", () -> BlockEntityType.Builder.of((pos, state) -> new TileBoardFate(ModBlockEntities.BOARD_FATE_TILE.get(), pos, state), ModBlockss.BOARD_FATE.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileGameBoard>> GAME_BOARD_TILE = REGISTRY.register("game_board", () -> BlockEntityType.Builder.of((pos, state) -> new TileGameBoard(ModBlockEntities.GAME_BOARD_TILE.get(), pos, state), ModBlockss.GAME_BOARD.get()).build(null));
    public static final RegistryObject<BlockEntityType<ManaBracketTile>> MANA_BRACKET_TILE = REGISTRY.register("mana_bracket", () -> BlockEntityType.Builder.of((pos, state) -> new ManaBracketTile(ModBlockEntities.MANA_BRACKET_TILE.get(), pos, state), ModBlockss.MANA_BRACKET.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileEngineerHopper>> ENGINEER_HOPPER_TILE = REGISTRY.register("engineer_hopper", () -> BlockEntityType.Builder.of((pos, state) -> new TileEngineerHopper(ModBlockEntities.ENGINEER_HOPPER_TILE.get(), pos, state), ModBlockss.ENGINEER_HOPPER.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileLebethronCore>> LEBETHRON_CORE = REGISTRY.register("lebethron_core", () -> BlockEntityType.Builder.of(TileLebethronCore::new, ModBlockss.LEBETHRON_CORE.get()).build(null));




    public static final RegistryObject<BlockEntityType<ManaContainerTile>> MANA_CONTAINER_TILE = REGISTRY.register("mana_container", () -> BlockEntityType.Builder.of((pos, state) -> new ManaContainerTile(ModBlockEntities.MANA_CONTAINER_TILE.get(), pos, state),
                            ModBlockss.MANA_CONTAINER.get(),
                            ModBlockss.CREATIVE_CONTAINER.get(),
                            ModBlockss.DILUTED_CONTAINER.get()
                            //ModBlockss.FABULOUS_CONTAINER.get()
    ).build(null));


    public static final RegistryObject<BlockEntityType<TileCursedGourmaryllis>> CURSED_GOURMARYLLIS_TILE  = REGISTRY.register("cursed", () -> BlockEntityType.Builder.of((pos, state) -> new TileCursedGourmaryllis(ModBlockEntities.CURSED_GOURMARYLLIS_TILE.get(), pos, state), ModBlockss.CURSED_GOURMARYLLIS.get()).build(null));

    public static final RegistryObject<BlockEntityType<TileEntityRuneFlower>> RUNE_FLOWER_TILE = REGISTRY.register("rune_flower_tile", () -> BlockEntityType.Builder.of((pos, state) -> new TileEntityRuneFlower(ModBlockEntities.RUNE_FLOWER_TILE.get(), pos, state), ModBlockss.RUNE_FLOWER.get()).build(null));
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
    public static final RegistryObject<BlockEntityType<EnderLavenderTile>> ENDER_LAVENDER_TILE = REGISTRY.register("ender_lavender_tile", () -> BlockEntityType.Builder.of((pos, state) -> new EnderLavenderTile(ModBlockEntities.ENDER_LAVENDER_TILE.get(), pos, state), ModBlockss.ENDER_LAVENDER.get()).build(null));
    public static final RegistryObject<BlockEntityType<DaybloomBlockTile>> DAYBLOOM_TILE = register("daybloom_tile", ModBlockss.DAYBLOOM, DaybloomBlockTile::new);
    public static final RegistryObject<BlockEntityType<OrechidEndiumTile>> ORECHIDENDIUMTILE = register("orechidendiumtile", ModBlockss.ORECHIDENDIUM, OrechidEndiumTile::new);
    public static final RegistryObject<BlockEntityType<SoarleanderTile>> SOARLEANDERTILE = register("soarleander", ModBlockss.SOARLEANDER, SoarleanderTile::new);
    public static final RegistryObject<BlockEntityType<AsgardFlowerTile>> ASGARDANDELIONTILE = register("asgardandeliontile", ModBlockss.ASGARDANDELION, AsgardFlowerTile::new);
    public static final RegistryObject<BlockEntityType<FloweyTile>> FLOWEYTILE = register("floweytile", ModBlockss.FLOWEY, FloweyTile::new);
    public static final RegistryObject<BlockEntityType<WitchOpoodTile>> WITCH_OPOOD_TILE = register("witch_opood_tile", ModBlockss.WITCH_OPOOD, WitchOpoodTile::new);
    public static final RegistryObject<BlockEntityType<AureaAmicitiaCarnationTile>> AUREA_AMICITIA_CARNATION_TILE = REGISTRY.register("aurea_amicitia_carnation_tile", () -> BlockEntityType.Builder.of((pos, state) -> new AureaAmicitiaCarnationTile(ModBlockEntities.AUREA_AMICITIA_CARNATION_TILE.get(), pos, state), ModBlockss.AUREA_AMICITIA_CARNATION.get()).build(null));
    public static final RegistryObject<BlockEntityType<MusicalOrchidTile>> MUSICAL_ORCHID_TILE = REGISTRY.register("musical_orchid_tile", () -> BlockEntityType.Builder.of((pos, state) -> new MusicalOrchidTile(ModBlockEntities.MUSICAL_ORCHID_TILE.get(), pos, state), ModBlockss.MUSICAL_ORCHID.get()).build(null));
    public static final RegistryObject<BlockEntityType<CatnipTile>> CATNIP_TILE = REGISTRY.register("catnip_tile", () -> BlockEntityType.Builder.of((pos, state) -> new CatnipTile(ModBlockEntities.CATNIP_TILE.get(), pos, state), ModBlockss.CATNIP.get()).build(null));
    public static final RegistryObject<BlockEntityType<AncientAlphirineTile>> ANCIENT_ALPHIRINE_TILE = REGISTRY.register("ancient_alphirine_tile", () -> BlockEntityType.Builder.of((pos, state) -> new AncientAlphirineTile(ModBlockEntities.ANCIENT_ALPHIRINE_TILE.get(), pos, state), ModBlockss.ANCIENT_ALPHIRINE.get()).build(null));
    public static final RegistryObject<BlockEntityType<DictariusTile>> DICTARIUS_TILE = REGISTRY.register("dictarius_tile", () -> BlockEntityType.Builder.of((pos, state) -> new DictariusTile(ModBlockEntities.DICTARIUS_TILE.get(), pos, state), ModBlockss.DICTARIUS.get()).build(null));
    public static final RegistryObject<BlockEntityType<EvilForgeTile>> EVIL_FORGE_TILE = REGISTRY.register("evil_forge_tile", () -> BlockEntityType.Builder.of((pos, state) -> new EvilForgeTile(ModBlockEntities.EVIL_FORGE_TILE.get(), pos, state), ModBlockss.EVIL_FORGE.get()).build(null));
    public static final RegistryObject<BlockEntityType<EtheriumForgeTile>> ETHERIUM_FORGE_TILE = REGISTRY.register("etherium_forge_tile", () -> BlockEntityType.Builder.of((pos, state) -> new EtheriumForgeTile(ModBlockEntities.ETHERIUM_FORGE_TILE.get(), pos, state), ModBlockss.ETHERIUM_FORGE.get()).build(null));
    public static final RegistryObject<BlockEntityType<ArdentAzarcissusTile>> ARDENT_AZARCISSUS_TILE = REGISTRY.register("ardent_azarcissus_tile", () -> BlockEntityType.Builder.of((pos, state) -> new ArdentAzarcissusTile(ModBlockEntities.ARDENT_AZARCISSUS_TILE.get(), pos, state), ModBlockss.ARDENT_AZARCISSUS.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileEntityAquaticAnglerNarcissus>> AQUATIC_ANGLER_NARCISSUS_TILE = REGISTRY.register("aquatic_angler_narcissus_tile", () -> BlockEntityType.Builder.of((pos, state) -> new TileEntityAquaticAnglerNarcissus(ModBlockEntities.AQUATIC_ANGLER_NARCISSUS_TILE.get(), pos, state), ModBlockss.AQUATIC_ANGLER_NARCISSUS.get()).build(null));










    private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> register(String registryName, RegistryObject<Block> block,
                                                                                       BlockEntityType.BlockEntitySupplier<T> supplier) {
        return REGISTRY.register(registryName, () -> BlockEntityType.Builder.of(supplier, block.get()).build(null));
    }

    @Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEventSubscriber {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                ItemBlockRenderTypes.setRenderLayer(ModBlockss.MANA_BOX.get(), RenderType.translucent());

                Minecraft.getInstance().getBlockColors().register(
                        (state, reader, pos, tintIndex) -> {
                            if (reader != null && pos != null) {
                                BlockEntity be = reader.getBlockEntity(pos);
                                if (be instanceof TileManaBox box) {
                                    return box.getColor().getTextColor();
                                }
                            }
                            return -1;
                        },
                        ModBlockss.MANA_BOX.get()
                );

                Minecraft.getInstance().getItemColors().register(
                        (stack, tintIndex) -> {
                            DyeColor color = BlockItemManaBox.getColor(stack);
                            return color.getTextColor();
                        },
                        ModItems.MANA_BOX_ITEM.get()
                );
            });
        }
    }
}
