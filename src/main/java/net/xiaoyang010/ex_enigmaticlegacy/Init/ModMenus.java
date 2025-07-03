package net.xiaoyang010.ex_enigmaticlegacy.Init;


import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.IContainerFactory;
import net.xiaoyang010.ex_enigmaticlegacy.Container.*;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.SpectriteChestTile;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModMenus {
    private static final List<MenuType<?>> REGISTRY = new ArrayList<>();

    public static final MenuType<InfinityChestMenu> INFINITE_CHEST_SCREEN = register("infinite_chest_screen",
            (id, inv, con) -> new InfinityChestMenu(id, inv));

    public static final MenuType<PagedChestContainer> PAGED_CHEST = register("paged_chest",
            (windowId, inv, data) -> new PagedChestContainer(windowId, inv,
                    (Container)inv.player.level.getBlockEntity(data.readBlockPos())));

    public static final MenuType<StarlitSanctumMenu> STARLIT_SANCTUM_SCREEN = register("starlit_sanctum_screen",
            StarlitSanctumMenu::new);

    public static final MenuType<CelestialHTMenu> CELESTIAL_HOLINESS_TRANSMUTE = register("celestial_holiness_transmute",
            CelestialHTMenu::new);

    public static final MenuType<DeconTableMenu> DECON_TABLE_MENU = register("deconstruction_table", DeconTableMenu::new);

    public static final MenuType<DimensionalMirrorContainer> DIMENSIONAL_MIRROR = register("dimensional_mirror",
            (windowId, inv, data) -> new DimensionalMirrorContainer(windowId, inv, inv.player));

    public static final MenuType<RainbowTableContainer> RAINBOW_TABLE_CONTAINER = register("rainbow_table",
            (windowId, inv, data) -> {
                BlockPos pos = data.readBlockPos();
                BlockEntity blockEntity = inv.player.level.getBlockEntity(pos);
                return new RainbowTableContainer(null, windowId, inv, blockEntity, pos);
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
