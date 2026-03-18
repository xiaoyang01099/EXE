package net.xiaoyang010.ex_enigmaticlegacy.Compat.Projecte.Factory;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.MenuType;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Projecte.EMCWandScreen;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Projecte.EMCWandMenu;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModMenus;
import net.xiaoyang010.ex_enigmaticlegacy.Container.MagicTableMenu;
import net.xiaoyang010.ex_enigmaticlegacy.Client.gui.MagicTableScreen;

public class MagicTableScreenFactory {
    public static void register() {
        MenuScreens.register(
                (MenuType<MagicTableMenu>) ModMenus.MAGIC_TABLE_MENU,
                MagicTableScreen::new
        );
        MenuScreens.register(
                (MenuType<EMCWandMenu>) ModMenus.EMC_MENU,
                EMCWandScreen::new
        );
    }
}