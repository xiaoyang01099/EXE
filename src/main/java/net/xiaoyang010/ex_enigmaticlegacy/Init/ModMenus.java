package net.xiaoyang010.ex_enigmaticlegacy.Init;


import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.RegistryEvent;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.AbstractContainerMenu;

import net.xiaoyang010.ex_enigmaticlegacy.Menu.CelestialHolinessTransmuteMenu;
import net.xiaoyang010.ex_enigmaticlegacy.Menu.CobblestonesMenu;
import net.xiaoyang010.ex_enigmaticlegacy.Menu.InfinityChestMenu;
import net.xiaoyang010.ex_enigmaticlegacy.Menu.StarlitSanctumMenu;

import java.util.List;
import java.util.ArrayList;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModMenus {
    private static final List<MenuType<?>> REGISTRY = new ArrayList<>();

    public static final MenuType<InfinityChestMenu> INFINITE_CHEST_SCREEN = register("infinite_chest_screen",
            (id, inv, extraData) -> new InfinityChestMenu(id, inv, extraData));;

    public static final MenuType<StarlitSanctumMenu> STARLIT_SANCTUM_SCREEN = register("starlit_sanctum_screen",
            (id, inv, extraData) -> new StarlitSanctumMenu(id, inv, extraData));

    public static final MenuType<CelestialHolinessTransmuteMenu> CELESTIAL_HOLINESS_TRANSMUTE = register("celestial_holiness_transmute",
            (id, inv, extraData) -> new CelestialHolinessTransmuteMenu(id, inv, extraData));

    public static final MenuType<CobblestonesMenu> COBBLE_STONES = register("cobble_stones",
            (id, inv, extraData) -> new CobblestonesMenu(id, inv, extraData));

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
