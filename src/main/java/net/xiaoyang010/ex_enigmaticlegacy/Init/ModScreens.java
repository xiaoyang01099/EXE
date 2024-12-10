package net.xiaoyang010.ex_enigmaticlegacy.Init;


import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.client.gui.screens.MenuScreens;
import net.xiaoyang010.ex_enigmaticlegacy.Client.gui.CelestialHolinessTransmuteScreen;
import net.xiaoyang010.ex_enigmaticlegacy.Client.gui.CobblestonesScreen;
import net.xiaoyang010.ex_enigmaticlegacy.Client.gui.InfinityChestScreen;
import net.xiaoyang010.ex_enigmaticlegacy.Client.gui.StarlitSanctumScreen;


@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModScreens {
    @SubscribeEvent
    public static void clientLoad(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.INFINITE_CHEST_SCREEN, InfinityChestScreen::new);

            MenuScreens.register(ModMenus.STARLIT_SANCTUM_SCREEN, StarlitSanctumScreen::new);

            MenuScreens.register(ModMenus.CELESTIAL_HOLINESS_TRANSMUTE, CelestialHolinessTransmuteScreen::new);

            MenuScreens.register(ModMenus.COBBLE_STONES, CobblestonesScreen::new);
        });
    }
}
