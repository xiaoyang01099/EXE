package org.xiaoyang.ex_enigmaticlegacy.Init;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.xiaoyang.ex_enigmaticlegacy.Client.screen.*;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Hud.TChest.GuiItemChest;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over.GuiOverpowered;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Projecte.Factory.ProjecteFactory;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModScreens {
    @SubscribeEvent
    public static void clientLoad(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.STARLIT_SANCTUM_SCREEN.get(), StarlitSanctumScreen::new);
            MenuScreens.register(ModMenus.CELESTIAL_HOLINESS_TRANSMUTE.get(), CelestialHTScreen::new);
            MenuScreens.register(ModMenus.DIMENSIONAL_MIRROR.get(), DimensionalMirrorScreen::new);
            MenuScreens.register(ModMenus.PAGED_CHEST.get(), PagedChestScreen::new);
            MenuScreens.register(ModMenus.RAINBOW_TABLE_CONTAINER.get(), RainbowTableScreen::new);
            MenuScreens.register(ModMenus.SPECTRITE_CHEST_MENU.get(), SpectriteChestScreen::new);
            MenuScreens.register(ModMenus.DECON_TABLE_MENU.get(), DeconTableScreen::new);
            MenuScreens.register(ModMenus.TALISMAN_CHEST.get(), GuiItemChest::new);
            MenuScreens.register(ModMenus.OVERPOWERED_CONTAINER.get(), GuiOverpowered::new);
            MenuScreens.register(ModMenus.EXTREME_AUTO_CRAFTER_MENU.get(), GuiExtremeAutoCrafter::new);
            MenuScreens.register(ModMenus.EXTREME_CRAFTING_DISASSEMBLY_MENU.get(), ExtremeDisassemblyScreen::new);
            MenuScreens.register(ModMenus.NEUTRONIUM_DECOMPRESSOR_MENU.get(), NeutroniumDecompressorScreen::new);


            if (ModList.get().isLoaded("projecte")) {
                ProjecteFactory.ScreenRegister();
            }
        });
    }
}
