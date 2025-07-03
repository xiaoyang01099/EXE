package net.xiaoyang010.ex_enigmaticlegacy.Init;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Client.gui.*;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModScreens {
    @SubscribeEvent
    public static void clientLoad(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.INFINITE_CHEST_SCREEN, InfinityChestScreen::new);
            MenuScreens.register(ModMenus.STARLIT_SANCTUM_SCREEN, StarlitSanctumScreen::new);
            MenuScreens.register(ModMenus.CELESTIAL_HOLINESS_TRANSMUTE, CelestialHTScreen::new);
            MenuScreens.register(ModMenus.DIMENSIONAL_MIRROR, DimensionalMirrorScreen::new);
            MenuScreens.register(ModMenus.PAGED_CHEST, PagedChestScreen::new);
            MenuScreens.register(ModMenus.RAINBOW_TABLE_CONTAINER, RainbowTableScreen::new);
            MenuScreens.register(ModMenus.SPECTRITE_CHEST_CONTAINER, ContainerScreen::new);
            MenuScreens.register(ModMenus.DECON_TABLE_MENU, DeconTableScreen::new);

        });
    }
}
