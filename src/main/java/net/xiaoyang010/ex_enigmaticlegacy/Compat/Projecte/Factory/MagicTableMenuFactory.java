package net.xiaoyang010.ex_enigmaticlegacy.Compat.Projecte.Factory;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.network.IContainerFactory;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Projecte.EMCWandMenu;
import net.xiaoyang010.ex_enigmaticlegacy.Container.MagicTableMenu;

public class MagicTableMenuFactory {
    public static MenuType<MagicTableMenu> MagicRegister(RegistryEvent.Register<MenuType<?>> event) {
        IContainerFactory<MagicTableMenu> factory = MagicTableMenu::new;
        MenuType<MagicTableMenu> menuType = new MenuType<>(factory);
        menuType.setRegistryName("magic_table");
        event.getRegistry().register(menuType);
        return menuType;
    }

    public static MenuType<EMCWandMenu> EmcRegister(RegistryEvent.Register<MenuType<?>> event) {
        IContainerFactory<EMCWandMenu> factory = EMCWandMenu::new;
        MenuType<EMCWandMenu> menuType = new MenuType<>(factory);
        menuType.setRegistryName("emc");
        event.getRegistry().register(menuType);
        return menuType;
    }
}