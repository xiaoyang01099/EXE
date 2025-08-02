package net.xiaoyang010.ex_enigmaticlegacy.Event;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputPacket.SpectatorModePacket;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = "ex_enigmaticlegacy", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class KeybindHandler {

    public static final String KEY_CATEGORIES_AVARITIA = "key.categories.ex_enigmaticlegacy";
    public static final String KEY_TOGGLE_SPECTATOR = "key.ex_enigmaticlegacy.toggle_spectator";

    public static KeyMapping toggleSpectatorKey;

    public static void registerKeybinds(FMLClientSetupEvent event) {
        toggleSpectatorKey = new KeyMapping(
                KEY_TOGGLE_SPECTATOR,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                KEY_CATEGORIES_AVARITIA
        );

        ClientRegistry.registerKeyBinding(toggleSpectatorKey);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        if (toggleSpectatorKey.consumeClick()) {
            NetworkHandler.sendToServer(new SpectatorModePacket());
        }
    }
}