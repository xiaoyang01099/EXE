package net.xiaoyang010.ex_enigmaticlegacy.Init;


import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.IContainerFactory;
import net.xiaoyang010.ex_enigmaticlegacy.Container.CelestialHolinessTransmuteMenu;
import net.xiaoyang010.ex_enigmaticlegacy.Container.InfinityChestMenu;
import net.xiaoyang010.ex_enigmaticlegacy.Container.StarlitSanctumMenu;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModMenus {
    private static final List<MenuType<?>> REGISTRY = new ArrayList<>();

    public static final MenuType<InfinityChestMenu> INFINITE_CHEST_SCREEN = register("infinite_chest_screen",
            InfinityChestMenu::new);;

    public static final MenuType<StarlitSanctumMenu> STARLIT_SANCTUM_SCREEN = register("starlit_sanctum_screen",
            StarlitSanctumMenu::new);

    public static final MenuType<CelestialHolinessTransmuteMenu> CELESTIAL_HOLINESS_TRANSMUTE = register("celestial_holiness_transmute",
            CelestialHolinessTransmuteMenu::new);

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
