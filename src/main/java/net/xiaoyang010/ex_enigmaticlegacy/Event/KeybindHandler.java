package net.xiaoyang010.ex_enigmaticlegacy.Event;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.ShinyStone;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over.*;
import net.xiaoyang010.ex_enigmaticlegacy.Config.ConfigHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage.DiscordKeybindMessage;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputPacket.ShinyStoneTogglePacket;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputPacket.SpectatorModePacket;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage.TelekinesisTomeLevelAttackMessage;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.TelekinesisTomeLevel;
import org.lwjgl.glfw.GLFW;
import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber(modid = "ex_enigmaticlegacy", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class KeybindHandler {
    public static final String SHINY_STONE_TOGGLE = "key.ex_enigmaticlegacy.shiny_stone_toggle";
    public static final String KEY_CATEGORIES_AVARITIA = "key.categories.ex_enigmaticlegacy";
    public static final String KEY_TOGGLE_SPECTATOR = "key.ex_enigmaticlegacy.toggle_spectator";
    public static final String KEY_CATEGORY = "key.categories.ex_enigmaticlegacy";

    public static KeyMapping toggleSpectatorKey;
    public static KeyMapping keyInventory;
    public static KeyMapping keyEnderpearl;
    public static KeyMapping keyEnderchest;
    public static KeyMapping keyHotbar;
    public static KeyMapping keyShinyStone;
    public static KeyMapping discordRingKey;
    private static boolean discordRingPressed = false;
    private static boolean wasAttacking = false;

    public static void registerKeybinds(FMLClientSetupEvent event) {
        discordRingKey = new KeyMapping(
                "key.ex_enigmaticlegacy.discord_ring",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_X,
                KEY_CATEGORY
        );

        keyShinyStone = new KeyMapping(
                SHINY_STONE_TOGGLE,
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                "key.categories.ex_enigmaticlegacy"
        );

        toggleSpectatorKey = new KeyMapping(
                KEY_TOGGLE_SPECTATOR,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                KEY_CATEGORIES_AVARITIA
        );

        keyInventory = new KeyMapping(
                "key.powerinventory.open",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "key.categories.powerinventory"
        );

        keyEnderpearl = new KeyMapping(
                "key.powerinventory.pearl",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_Z,
                "key.categories.powerinventory"
        );

        keyEnderchest = new KeyMapping(
                "key.powerinventory.chest",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_I,
                "key.categories.powerinventory"
        );

        keyHotbar = new KeyMapping(
                "key.powerinventory.hotbar",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "key.categories.powerinventory"
        );

        ClientRegistry.registerKeyBinding(keyInventory);
        ClientRegistry.registerKeyBinding(keyEnderpearl);
        ClientRegistry.registerKeyBinding(keyEnderchest);
        ClientRegistry.registerKeyBinding(keyHotbar);
        ClientRegistry.registerKeyBinding(toggleSpectatorKey);
        ClientRegistry.registerKeyBinding(keyShinyStone);
        ClientRegistry.registerKeyBinding(discordRingKey);
    }

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseInputEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        LocalPlayer player = mc.player;
        ItemStack held = player.getMainHandItem();

        if (!(held.getItem() instanceof TelekinesisTomeLevel)) return;

        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (event.getAction() == GLFW.GLFW_PRESS && !wasAttacking) {
                wasAttacking = true;
                NetworkHandler.sendToServer(new TelekinesisTomeLevelAttackMessage(true));
            } else if (event.getAction() == GLFW.GLFW_RELEASE) {
                wasAttacking = false;
            }
        }
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

        handleDiscordRingKey(player);

        // 末影珍珠快捷键
        if (keyEnderpearl.consumeClick()) {
            if (ConfigHandler.REQUIRE_RING.get() && !isRingEquipped(player)) {
                player.displayClientMessage(
                        Component.nullToEmpty(""),
                        true
                );
                return;
            }
            NetworkHandler.CHANNEL.sendToServer(new EnderPearlPacket());
        }

        // 末影箱快捷键
        if (keyEnderchest.consumeClick()) {
            if (ConfigHandler.REQUIRE_RING.get() && !isRingEquipped(player)) {
                player.displayClientMessage(
                        Component.nullToEmpty(""),
                        true
                );
                return;
            }
            NetworkHandler.CHANNEL.sendToServer(new EnderChestPacket());
        }

        // 快捷栏交换快捷键
        if (keyHotbar.consumeClick()) {
            if (ConfigHandler.REQUIRE_RING.get() && !isRingEquipped(player)) {
                player.displayClientMessage(
                        Component.nullToEmpty(""),
                        true
                );
                return;
            }
            NetworkHandler.CHANNEL.sendToServer(new HotbarSwapPacket());
        }

        // 打开背包快捷键
        if (keyInventory.consumeClick()) {
            if (ConfigHandler.REQUIRE_RING.get() && !isRingEquipped(player)) {
                player.displayClientMessage(
                        Component.nullToEmpty(""),
                        true
                );
                return;
            }
            NetworkHandler.CHANNEL.sendToServer(new OpenInventoryPacket());
        }

        handleToggle();
    }

    private static void handleDiscordRingKey(Player player) {
        if (discordRingKey == null) return;

        if (discordRingKey.isDown() && !discordRingPressed) {
            if (isDiscordRingEquipped(player)) {
                NetworkHandler.CHANNEL.sendToServer(new DiscordKeybindMessage(true));
            }
            discordRingPressed = true;
        } else if (!discordRingKey.isDown() && discordRingPressed) {
            discordRingPressed = false;
        }
    }

    public static boolean isDiscordRingEquipped(Player player) {
        return CuriosApi.getCuriosHelper()
                .findEquippedCurio(ModItems.DISCORD_RING.get(), player)
                .isPresent();
    }

    public static boolean isRingEquipped(Player player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, stack -> stack.getItem() instanceof ItemPowerRing)
                .isPresent();
    }

    private static void handleToggle() {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.screen != null) {
            return;
        }
        if (keyShinyStone.consumeClick()) {
            boolean hasShinyStone = CuriosApi.getCuriosHelper()
                    .getEquippedCurios(player)
                    .map(handler -> {
                        for (int i = 0; i < handler.getSlots(); i++) {
                            if (handler.getStackInSlot(i).getItem() instanceof ShinyStone) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .orElse(false);
            if (hasShinyStone) {
                NetworkHandler.CHANNEL.sendToServer(new ShinyStoneTogglePacket());
            }
        }
    }

    public static String getDiscordRingKeyName() {
        if (discordRingKey != null) {
            return discordRingKey.getTranslatedKeyMessage().getString();
        }
        return "???";
    }
}
