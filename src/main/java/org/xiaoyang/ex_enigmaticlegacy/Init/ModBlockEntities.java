package org.xiaoyang.ex_enigmaticlegacy.Init;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Block.tile.*;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Flower.functional.*;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Flower.generating.*;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Flower.hybrid.TileEntityAquaticAnglerNarcissus;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Flower.hybrid.TileEntityRuneFlower;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item.BlockItemManaBox;
import org.xiaoyang.ex_enigmaticlegacy.Exe;
import org.xiaoyang.ex_enigmaticlegacy.Tile.*;


public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Exe.MODID);
    //魔法花实体
    public static final RegistryObject<BlockEntityType<AstralKillopTile>> ASTRAL_KILLOP_TILE = BLOCK_ENTITIES.register("astral_killop", () -> BlockEntityType.Builder.of(AstralKillopTile::new, ModBlocks.ASTRAL_KILLOP.get()).build(null));
    public static final RegistryObject<BlockEntityType<RainbowGeneratingFlowerTile>> RAINBOW_GENERATING_FLOWER_TILE = BLOCK_ENTITIES.register("rainbow_generating_flower", () -> BlockEntityType.Builder.of(RainbowGeneratingFlowerTile::new, ModBlocks.RAINBOW_GENERATING_FLOWER.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileEntityRuneFlower>> RUNE_FLOWER_TILE = BLOCK_ENTITIES.register("rune_flower_tile", () -> BlockEntityType.Builder.of((pos, state) -> new TileEntityRuneFlower(ModBlockEntities.RUNE_FLOWER_TILE.get(), pos, state), ModBlocks.RUNE_FLOWER.get()).build(null));
    public static final RegistryObject<BlockEntityType<CurseThistleTile>> CURSET_THISTLE_TILE = BLOCK_ENTITIES.register("curse_thistle_tile", () -> BlockEntityType.Builder.of((pos, state) -> new CurseThistleTile(ModBlockEntities.CURSET_THISTLE_TILE.get(), pos, state), ModBlocks.CURSET_THISTLE.get()).build(null));
    public static final RegistryObject<BlockEntityType<YushouCloverTile>> YU_SHOU_CLOVER_TILE = BLOCK_ENTITIES.register("yu_shou_clover_tile", () -> BlockEntityType.Builder.of((pos, state) -> new YushouCloverTile(ModBlockEntities.YU_SHOU_CLOVER_TILE.get(), pos, state), ModBlocks.YU_SHOU_CLOVER.get()).build(null));
    public static final RegistryObject<BlockEntityType<VacuityTile>> VACUITY_TILE = BLOCK_ENTITIES.register("vacuity_tile", () -> BlockEntityType.Builder.of((pos, state) -> new VacuityTile(ModBlockEntities.VACUITY_TILE.get(), pos, state), ModBlocks.VACUITY.get()).build(null));
    public static final RegistryObject<BlockEntityType<StreetLightFlowerTile>> STREET_LIGHT_TILE = BLOCK_ENTITIES.register("street_light_tile", () -> BlockEntityType.Builder.of((pos, state) -> new StreetLightFlowerTile(ModBlockEntities.STREET_LIGHT_TILE.get(), pos, state), ModBlocks.STREET_LIGHT.get()).build(null));
    public static final RegistryObject<BlockEntityType<BlazingOrchidFlowerTile>> BLAZING_ORCHID_TILE = BLOCK_ENTITIES.register("blazing_orchid_tile", () -> BlockEntityType.Builder.of((pos, state) -> new BlazingOrchidFlowerTile(ModBlockEntities.BLAZING_ORCHID_TILE.get(), pos, state), ModBlocks.BLAZING_ORCHID.get()).build(null));
    public static final RegistryObject<BlockEntityType<BelieverTile>> BELIEVERTILE = BLOCK_ENTITIES.register("believertile", () -> BlockEntityType.Builder.of((pos, state) -> new BelieverTile(ModBlockEntities.BELIEVERTILE.get(), pos, state), ModBlocks.BELIEVER.get()).build(null));
    public static final RegistryObject<BlockEntityType<MingXianLanTile>> MINGXIANLAN_TILE = BLOCK_ENTITIES.register("mingxianlan_tile", () -> BlockEntityType.Builder.of((pos, state) -> new MingXianLanTile(ModBlockEntities.MINGXIANLAN_TILE.get(), pos, state), ModBlocks.MINGXIANLAN.get()).build(null));
    public static final RegistryObject<BlockEntityType<GenEnergydandronTile>> GENENERGYDANDRON = BLOCK_ENTITIES.register("gen_energydandron", () -> BlockEntityType.Builder.of((pos, state) -> new GenEnergydandronTile(ModBlockEntities.GENENERGYDANDRON.get(), pos, state), ModBlocks.GENENERGYDANDRON.get()).build(null));
    public static final RegistryObject<BlockEntityType<KillingBerryTile>> KILLING_BERRY_TILE = BLOCK_ENTITIES.register("killing_berry_tile", () -> BlockEntityType.Builder.of((pos, state) -> new KillingBerryTile(ModBlockEntities.KILLING_BERRY_TILE.get(), pos, state), ModBlocks.KILLING_BERRY.get()).build(null));
    public static final RegistryObject<BlockEntityType<DarkNightGrassTile>> DARK_NIGHT_GRASS_TILE = BLOCK_ENTITIES.register("dark_night_grass_tile", () -> BlockEntityType.Builder.of((pos, state) -> new DarkNightGrassTile(ModBlockEntities.DARK_NIGHT_GRASS_TILE.get(), pos, state), ModBlocks.DARK_NIGHT_GRASS.get()).build(null));
    public static final RegistryObject<BlockEntityType<FrostLotusFlowerTile>> FROST_LOTUS_TILE = BLOCK_ENTITIES.register("frost_lotus_tile", () -> BlockEntityType.Builder.of((pos, state) -> new FrostLotusFlowerTile(ModBlockEntities.FROST_LOTUS_TILE.get(), pos, state), ModBlocks.FROST_LOTUS.get()).build(null));
    public static final RegistryObject<BlockEntityType<LycorisradiataTile>> LYCORISRADIATA_TILE = BLOCK_ENTITIES.register("lycorisradiata_tile", () -> BlockEntityType.Builder.of((pos, state) -> new LycorisradiataTile(ModBlockEntities.LYCORISRADIATA_TILE.get(), pos, state), ModBlocks.LYCORISRADIATA.get()).build(null));
    public static final RegistryObject<BlockEntityType<FrostBlossomTile>> FROST_BLOSSOM_TILE = BLOCK_ENTITIES.register("frost_blossom_tile", () -> BlockEntityType.Builder.of((pos, state) -> new FrostBlossomTile(ModBlockEntities.FROST_BLOSSOM_TILE.get(), pos, state), ModBlocks.FROST_BLOSSOM.get()).build(null));
    public static final RegistryObject<BlockEntityType<EnderLavenderTile>> ENDER_LAVENDER_TILE = BLOCK_ENTITIES.register("ender_lavender_tile", () -> BlockEntityType.Builder.of((pos, state) -> new EnderLavenderTile(ModBlockEntities.ENDER_LAVENDER_TILE.get(), pos, state), ModBlocks.ENDER_LAVENDER.get()).build(null));
    public static final RegistryObject<BlockEntityType<DaybloomBlockTile>> DAYBLOOM_TILE = register("daybloom_tile", ModBlocks.DAYBLOOM, DaybloomBlockTile::new);
    public static final RegistryObject<BlockEntityType<OrechidEndiumTile>> ORECHIDENDIUMTILE = register("orechidendiumtile", ModBlocks.ORECHIDENDIUM, OrechidEndiumTile::new);
    public static final RegistryObject<BlockEntityType<SoarleanderTile>> SOARLEANDERTILE = register("soarleander", ModBlocks.SOARLEANDER, SoarleanderTile::new);
    public static final RegistryObject<BlockEntityType<FloweyTile>> FLOWEYTILE = register("floweytile", ModBlocks.FLOWEY, FloweyTile::new);
    public static final RegistryObject<BlockEntityType<WitchOpoodTile>> WITCH_OPOOD_TILE = register("witch_opood_tile", ModBlocks.WITCH_OPOOD, WitchOpoodTile::new);
    public static final RegistryObject<BlockEntityType<AureaAmicitiaCarnationTile>> AUREA_AMICITIA_CARNATION_TILE = BLOCK_ENTITIES.register("aurea_amicitia_carnation_tile", () -> BlockEntityType.Builder.of((pos, state) -> new AureaAmicitiaCarnationTile(ModBlockEntities.AUREA_AMICITIA_CARNATION_TILE.get(), pos, state), ModBlocks.AUREA_AMICITIA_CARNATION.get()).build(null));
    public static final RegistryObject<BlockEntityType<MusicalOrchidTile>> MUSICAL_ORCHID_TILE = BLOCK_ENTITIES.register("musical_orchid_tile", () -> BlockEntityType.Builder.of((pos, state) -> new MusicalOrchidTile(ModBlockEntities.MUSICAL_ORCHID_TILE.get(), pos, state), ModBlocks.MUSICAL_ORCHID.get()).build(null));
    public static final RegistryObject<BlockEntityType<CatnipTile>> CATNIP_TILE = BLOCK_ENTITIES.register("catnip_tile", () -> BlockEntityType.Builder.of((pos, state) -> new CatnipTile(ModBlockEntities.CATNIP_TILE.get(), pos, state), ModBlocks.CATNIP.get()).build(null));
    public static final RegistryObject<BlockEntityType<AncientAlphirineTile>> ANCIENT_ALPHIRINE_TILE = BLOCK_ENTITIES.register("ancient_alphirine_tile", () -> BlockEntityType.Builder.of((pos, state) -> new AncientAlphirineTile(ModBlockEntities.ANCIENT_ALPHIRINE_TILE.get(), pos, state), ModBlocks.ANCIENT_ALPHIRINE.get()).build(null));
    public static final RegistryObject<BlockEntityType<DictariusTile>> DICTARIUS_TILE = BLOCK_ENTITIES.register("dictarius_tile", () -> BlockEntityType.Builder.of((pos, state) -> new DictariusTile(ModBlockEntities.DICTARIUS_TILE.get(), pos, state), ModBlocks.DICTARIUS.get()).build(null));
    public static final RegistryObject<BlockEntityType<EvilForgeTile>> EVIL_FORGE_TILE = BLOCK_ENTITIES.register("evil_forge_tile", () -> BlockEntityType.Builder.of((pos, state) -> new EvilForgeTile(ModBlockEntities.EVIL_FORGE_TILE.get(), pos, state), ModBlocks.EVIL_FORGE.get()).build(null));
    public static final RegistryObject<BlockEntityType<EtheriumForgeTile>> ETHERIUM_FORGE_TILE = BLOCK_ENTITIES.register("etherium_forge_tile", () -> BlockEntityType.Builder.of((pos, state) -> new EtheriumForgeTile(ModBlockEntities.ETHERIUM_FORGE_TILE.get(), pos, state), ModBlocks.ETHERIUM_FORGE.get()).build(null));
    public static final RegistryObject<BlockEntityType<ArdentAzarcissusTile>> ARDENT_AZARCISSUS_TILE = BLOCK_ENTITIES.register("ardent_azarcissus_tile", () -> BlockEntityType.Builder.of((pos, state) -> new ArdentAzarcissusTile(ModBlockEntities.ARDENT_AZARCISSUS_TILE.get(), pos, state), ModBlocks.ARDENT_AZARCISSUS.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileEntityAquaticAnglerNarcissus>> AQUATIC_ANGLER_NARCISSUS_TILE = BLOCK_ENTITIES.register("aquatic_angler_narcissus_tile", () -> BlockEntityType.Builder.of((pos, state) -> new TileEntityAquaticAnglerNarcissus(ModBlockEntities.AQUATIC_ANGLER_NARCISSUS_TILE.get(), pos, state), ModBlocks.AQUATIC_ANGLER_NARCISSUS.get()).build(null));
    public static final RegistryObject<BlockEntityType<NightshadeTile>> NIGHTSHADE_TILE = register("nightshade_tile", ModBlocks.NIGHTSHADE, NightshadeTile::new);



    //其他方块
    public static final RegistryObject<BlockEntityType<StarlitSanctumTile>> STARLIT_SANCTUM_OF_MYSTIQUE = register("starlit_sanctum_of_mystique", ModBlocks.STARLIT_SANCTUM, StarlitSanctumTile::new);
    public static final RegistryObject<BlockEntityType<CelestialHTTile>> CELESTIAL_HOLINESS_TRANSMUTER_TILE = register("celestial_holiness_transmuter_tile", ModBlocks.CELESTIAL_HOLINESS_TRANSMUTER, CelestialHTTile::new);
    public static final RegistryObject<BlockEntityType<PagedChestBlockTile>> PAGED_CHEST = BLOCK_ENTITIES.register("paged_chest", () -> BlockEntityType.Builder.of(PagedChestBlockTile::new, ModBlocks.PAGED_CHEST.get()).build(null));
    public static final RegistryObject<BlockEntityType<RainbowTableTile>> RAINBOW_TABLE_TILE = BLOCK_ENTITIES.register("rainbow_table_tile", () -> BlockEntityType.Builder.of(RainbowTableTile::new, ModBlocks.RAINBOW_TABLE.get()).build(null));
    public static final RegistryObject<BlockEntityType<NidavellirForgeTile>> NIDAVELLIR_FORGE_TILE = BLOCK_ENTITIES.register("nidavellir_forge_tile", () -> BlockEntityType.Builder.of(NidavellirForgeTile::new, ModBlocks.NIDAVELLIR_FORGE.get()).build(null));
    public static final RegistryObject<BlockEntityType<SpectriteChestTile>> SPECTRITE_CHEST_TILE = BLOCK_ENTITIES.register("spectrite_chest", () -> BlockEntityType.Builder.of(SpectriteChestTile::new, ModBlocks.SPECTRITE_CHEST.get()).build(null));
    public static final RegistryObject<BlockEntityType<AstralBlockEntity>> ASTRAL_BLOCK_ENTITY = BLOCK_ENTITIES.register("astral_block", () -> BlockEntityType.Builder.of((pos, state) -> new AstralBlockEntity(ModBlockEntities.ASTRAL_BLOCK_ENTITY.get(), pos, state), ModBlocks.ASTRAL_BLOCK.get()).build(null));
//    public static final RegistryObject<BlockEntityType<CosmicBlockEntity>> COSMIC_BLOCK_ENTITY = BLOCK_ENTITIES.register("cosmic_block", () -> BlockEntityType.Builder.of((pos, state) -> new CosmicBlockEntity(ModBlockEntities.COSMIC_BLOCK_ENTITY.get(), pos, state), ModBlockss.COSMIC_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileEntityExtremeAutoCrafter>> EXTREME_AUTO_CRAFTER_TILE = BLOCK_ENTITIES.register("extreme_auto_crafter_tile", () -> BlockEntityType.Builder.of((pos, state) -> new TileEntityExtremeAutoCrafter(ModBlockEntities.EXTREME_AUTO_CRAFTER_TILE.get(), pos, state), ModBlocks.EXTREME_AUTO_CRAFTER.get()).build(null));
//    public static final RegistryObject<BlockEntityType<TileEntityInfinityCompressor>> INFINITY_COMPRESSOR_TILE = BLOCK_ENTITIES.register("infinity_compressor_tile", () -> BlockEntityType.Builder.of((pos, state) -> new TileEntityInfinityCompressor(ModBlockEntities.INFINITY_COMPRESSOR_TILE.get(), pos, state), ModBlockss.INFINITY_COMPRESSOR.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileManaBox>> MANA_BOX_TILE = BLOCK_ENTITIES.register("mana_box_tile", () -> BlockEntityType.Builder.of((pos, state) -> new TileManaBox(ModBlockEntities.MANA_BOX_TILE.get(), pos, state), ModBlocks.MANA_BOX.get()).build(null));
    public static final RegistryObject<BlockEntityType<NeutroniumDecompressorTile>> NEUTRONIUM_DECOMPRESSOR_TILE = BLOCK_ENTITIES.register("neutronium_decompressor_tile", () -> BlockEntityType.Builder.of((pos, state) -> new NeutroniumDecompressorTile(ModBlockEntities.NEUTRONIUM_DECOMPRESSOR_TILE.get(), pos, state), ModBlocks.NEUTRONIUM_DECOMPRESSOR.get()).build(null));
//    public static final RegistryObject<BlockEntityType<StarrySkyBlockEntity>> STARRY_SKY_BLOCK_ENTITY = BLOCK_ENTITIES.register("starry_block", () -> BlockEntityType.Builder.of((pos, state) -> new StarrySkyBlockEntity(ModBlockEntities.STARRY_SKY_BLOCK_ENTITY.get(), pos, state), ModBlockss.STARRY_SKY_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<InfinityPotatoTile>> INFINITY_POTATO = register("infinity_potato", ModBlocks.INFINITY_POTATO, (pos, state) -> new InfinityPotatoTile(ModBlockEntities.INFINITY_POTATO.get(), pos, state));
    public static final RegistryObject<BlockEntityType<ManaChargerTile>> MANA_CHARGER_TILE = BLOCK_ENTITIES.register("mana_charger", () -> BlockEntityType.Builder.of((pos, state) -> new ManaChargerTile(ModBlockEntities.MANA_CHARGER_TILE.get(), pos, state), ModBlocks.MANA_CHARGER.get()).build(null));
    public static final RegistryObject<BlockEntityType<ManaCrystalCubeBlockTile>> MANA_CRYSTAL_TILE = BLOCK_ENTITIES.register("mana_crystal", () -> BlockEntityType.Builder.of((pos, state) -> new ManaCrystalCubeBlockTile(ModBlockEntities.MANA_CRYSTAL_TILE.get(), pos, state), ModBlocks.MANA_CRYSTAL.get()).build(null));
    public static final RegistryObject<BlockEntityType<PolychromeCollapsePrismTile>> POLYCHROME_COLLAPSE_PRISM_TILE = BLOCK_ENTITIES.register("polychrome_collapse_prism", () -> BlockEntityType.Builder.of((pos, state) -> new PolychromeCollapsePrismTile(ModBlockEntities.POLYCHROME_COLLAPSE_PRISM_TILE.get(), pos, state), ModBlocks.POLYCHROME_COLLAPSE_PRISM.get()).build(null));
    public static final RegistryObject<BlockEntityType<FullAltarTile>> FULL_ALTAR_TILE = BLOCK_ENTITIES.register("full_altar", () -> BlockEntityType.Builder.of((pos, state) -> new FullAltarTile(ModBlockEntities.FULL_ALTAR_TILE.get(), pos, state), ModBlocks.FULL_ALTAR.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileBoardFate>> BOARD_FATE_TILE = BLOCK_ENTITIES.register("board_fate", () -> BlockEntityType.Builder.of((pos, state) -> new TileBoardFate(ModBlockEntities.BOARD_FATE_TILE.get(), pos, state), ModBlocks.BOARD_FATE.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileGameBoard>> GAME_BOARD_TILE = BLOCK_ENTITIES.register("game_board", () -> BlockEntityType.Builder.of((pos, state) -> new TileGameBoard(ModBlockEntities.GAME_BOARD_TILE.get(), pos, state), ModBlocks.GAME_BOARD.get()).build(null));
    public static final RegistryObject<BlockEntityType<ManaBracketTile>> MANA_BRACKET_TILE = BLOCK_ENTITIES.register("mana_bracket", () -> BlockEntityType.Builder.of((pos, state) -> new ManaBracketTile(ModBlockEntities.MANA_BRACKET_TILE.get(), pos, state), ModBlocks.MANA_BRACKET.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileEngineerHopper>> ENGINEER_HOPPER_TILE = BLOCK_ENTITIES.register("engineer_hopper", () -> BlockEntityType.Builder.of((pos, state) -> new TileEngineerHopper(ModBlockEntities.ENGINEER_HOPPER_TILE.get(), pos, state), ModBlocks.ENGINEER_HOPPER.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileLebethronCore>> LEBETHRON_CORE = BLOCK_ENTITIES.register("lebethron_core", () -> BlockEntityType.Builder.of(TileLebethronCore::new, ModBlocks.LEBETHRON_CORE.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileAdvancedSpreader>> ADVANCED_SPREADER = register("advanced_spreader", ModBlocks.ADVANCED_SPREADER, TileAdvancedSpreader::new);
    public static final RegistryObject<BlockEntityType<ManaContainerTile>> MANA_CONTAINER_TILE = BLOCK_ENTITIES.register("mana_container", () -> BlockEntityType.Builder.of((pos, state) -> new ManaContainerTile(ModBlockEntities.MANA_CONTAINER_TILE.get(), pos, state), ModBlocks.MANA_CONTAINER.get(), ModBlocks.CREATIVE_CONTAINER.get(), ModBlocks.DILUTED_CONTAINER.get()).build(null));
    public static RegistryObject<BlockEntityType<TileMagicTable>> MAGIC_TABLE_TILE = null;

















    private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> register(String registryName, RegistryObject<Block> block,
                                                                                       BlockEntityType.BlockEntitySupplier<T> supplier) {
        return BLOCK_ENTITIES.register(registryName, () -> BlockEntityType.Builder.of(supplier, block.get()).build(null));
    }

    @Mod.EventBusSubscriber(modid = Exe.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEventSubscriber {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.MANA_BOX.get(), RenderType.translucent());

                Minecraft.getInstance().getBlockColors().register(
                        (state, reader, pos, tintIndex) -> {
                            if (reader != null && pos != null) {
                                BlockEntity be = reader.getBlockEntity(pos);
                                if (be instanceof TileManaBox box) {
                                    return box.getColor()
                                            .map(DyeColor::getTextColor)
                                            .orElse(-1);
                                }
                            }
                            return -1;
                        },
                        ModBlocks.MANA_BOX.get()
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
