package net.xiaoyang010.ex_enigmaticlegacy.Init;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.Command.OpenEMCCommand;

@Mod.EventBusSubscriber(modid = "ex_enigmaticlegacy", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        OpenEMCCommand.register(event.getDispatcher());
    }
}