package net.xiaoyang010.ex_enigmaticlegacy.Init;


import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.IContainerFactory;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Hud.TChest.ContainerItemChest;
import net.xiaoyang010.ex_enigmaticlegacy.Container.*;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.PagedChestBlockTile;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.SpectriteChestTile;
import net.xiaoyang010.ex_enigmaticlegacy.Container.ContainerExtremeAutoCrafter;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.TileEntityExtremeAutoCrafter;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.TileEntityInfinityCompressor;
import net.xiaoyang010.ex_enigmaticlegacy.Container.NeutroniumDecompressorMenu;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over.ContainerOverpowered;
//import net.xiaoyang010.ex_enigmaticlegacy.Container.ContainerInfinityCompressor;
//import net.xiaoyang010.ex_enigmaticlegacy.Tile.TileEntityInfinityCompressor;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModMenus {
    private static final List<MenuType<?>> REGISTRY = new ArrayList<>();

    public static final MenuType<StarlitSanctumMenu> STARLIT_SANCTUM_SCREEN = register("starlit_sanctum_screen", StarlitSanctumMenu::new);

    public static final MenuType<CelestialHTMenu> CELESTIAL_HOLINESS_TRANSMUTE = register("celestial_holiness_transmute", CelestialHTMenu::new);

    public static final MenuType<DeconTableMenu> DECON_TABLE_MENU = register("deconstruction_table", DeconTableMenu::new);

    public static final MenuType<ExtremeDisassemblyMenu> EXTREME_CRAFTING_DISASSEMBLY_MENU = register("extreme_crafting_disassembly_table", ExtremeDisassemblyMenu::new);

    public static final MenuType<NeutroniumDecompressorMenu> NEUTRONIUM_DECOMPRESSOR_MENU = register("neutronium_decompressor", NeutroniumDecompressorMenu::new);

    public static final MenuType<MagicTableMenu> MAGIC_TABLE_MENU = register("magic_table", MagicTableMenu::new);

    public static final MenuType<ContainerOverpowered> OVERPOWERED_CONTAINER = register("overpowered_container", ((windowId, inv, data) -> new ContainerOverpowered(windowId, inv))
    );

    public static final MenuType<DoubleCraftingMenu> DOUBLE_CRAFTING_MENU =
            register("double_crafting_menu", (windowId, inv, ignored) ->
                    new DoubleCraftingMenu(windowId, inv));

    public static final MenuType<InfinityChestMenu> INFINITE_CHEST_SCREEN = register("infinite_chest_screen",
            (id, inv, con) -> new InfinityChestMenu(id, inv));

    public static final MenuType<ContainerItemChest> TALISMAN_CHEST = register("talisman_chest",
            (windowId, inv, data) -> new ContainerItemChest(windowId, inv, inv.player));

    public static final MenuType<DimensionalMirrorContainer> DIMENSIONAL_MIRROR = register("dimensional_mirror",
            (windowId, inv, data) -> new DimensionalMirrorContainer(windowId, inv, inv.player));

    public static final MenuType<ContainerExtremeAutoCrafter> EXTREME_AUTO_CRAFTER_MENU = register("extreme_auto_crafter_menu",
            (windowId, inv, data) -> {
                BlockPos pos = data.readBlockPos();
                Level world = inv.player.level;
                TileEntityExtremeAutoCrafter tileEntity = (TileEntityExtremeAutoCrafter) world.getBlockEntity(pos);
                return new ContainerExtremeAutoCrafter(windowId, inv, tileEntity);
            });

    public static final MenuType<ContainerInfinityCompressor> INFINITY_COMPRESSOR_MENU = register("infinity_compressor_menu",
            (windowId, inv, data) -> {
                BlockPos pos = data.readBlockPos();
                Level world = inv.player.level;
                TileEntityInfinityCompressor tileEntity = (TileEntityInfinityCompressor) world.getBlockEntity(pos);
                return new ContainerInfinityCompressor(windowId, inv, tileEntity);
            });

    public static final MenuType<RainbowTableContainer> RAINBOW_TABLE_CONTAINER = register("rainbow_table",
            (windowId, inv, data) -> {
                BlockPos pos = data.readBlockPos();
                BlockEntity blockEntity = inv.player.level.getBlockEntity(pos);
                return new RainbowTableContainer(null, windowId, inv, blockEntity, pos);
            });

    public static final MenuType<PagedChestContainer> PAGED_CHEST = register("paged_chest",
            (windowId, inv, data) -> {
                BlockPos pos = data.readBlockPos();
                Level level = inv.player.level;
                return new PagedChestContainer(windowId, inv,
                        (PagedChestBlockTile)level.getBlockEntity(pos));
            });

    public static final MenuType<SpectriteChestContainer> SPECTRITE_CHEST_CONTAINER = register("spectrite_chest",
            (windowId, inv, data) -> {
                BlockPos pos = data.readBlockPos();
                Level level = inv.player.level;
                return new SpectriteChestContainer(windowId, inv,
                        (SpectriteChestTile)level.getBlockEntity(pos));
            });

    private static <T extends AbstractContainerMenu> MenuType<T> register(String registryname, IContainerFactory<T> containerFactory) {
        MenuType<T> menuType = new MenuType<T>(containerFactory);
        menuType.setRegistryName(registryname);
        REGISTRY.add(menuType);
        return menuType;
    }

    @SubscribeEvent
    public static void registerContainers(RegistryEvent.Register<MenuType<?>> event) {
        event.getRegistry().registerAll(REGISTRY.toArray(new MenuType[0]));
    }
}
