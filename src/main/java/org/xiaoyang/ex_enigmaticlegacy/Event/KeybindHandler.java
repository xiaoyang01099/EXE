package org.xiaoyang.ex_enigmaticlegacy.Event;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item.Relic.ShinyStone;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item.Relic.TelekinesisTomeLevel;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over.*;
import org.xiaoyang.ex_enigmaticlegacy.ConfigHandler;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModItems;
import org.xiaoyang.ex_enigmaticlegacy.Network.NetworkHandler;
import org.xiaoyang.ex_enigmaticlegacy.Network.inputMessage.DiscordKeybindMessage;
import org.xiaoyang.ex_enigmaticlegacy.Network.inputMessage.TelekinesisTomeLevelAttackMessage;
import org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket.ShinyStoneTogglePacket;
import org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket.SpectatorModePacket;
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

    public static void init() {
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
                KEY_CATEGORY
        );

        toggleSpectatorKey = new KeyMapping(
                KEY_TOGGLE_SPECTATOR,
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                KEY_CATEGORIES_AVARITIA
        );

        keyInventory = new KeyMapping(
                "key.powerinventory.open",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "key.categories.powerinventory"
        );

        keyEnderpearl = new KeyMapping(
                "key.powerinventory.pearl",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_Z,
                "key.categories.powerinventory"
        );

        keyEnderchest = new KeyMapping(
                "key.powerinventory.chest",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_I,
                "key.categories.powerinventory"
        );

        keyHotbar = new KeyMapping(
                "key.powerinventory.hotbar",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "key.categories.powerinventory"
        );
    }

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        init();

        event.register(keyInventory);
        event.register(keyEnderpearl);
        event.register(keyEnderchest);
        event.register(keyHotbar);
        event.register(toggleSpectatorKey);
        event.register(keyShinyStone);
        event.register(discordRingKey);
    }

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseButton event) {
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
    public static void onKeyInput(InputEvent.Key event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        // 观察者模式切换
        if (toggleSpectatorKey != null && toggleSpectatorKey.consumeClick()) {
            NetworkHandler.sendToServer(new SpectatorModePacket());
        }

        handleDiscordRingKey(player);

        // 末影珍珠快捷键
        if (keyEnderpearl != null && keyEnderpearl.consumeClick()) {
            if (ConfigHandler.REQUIRE_RING.get() && !isRingEquipped(player)) {
                player.displayClientMessage(Component.nullToEmpty(""), true);
                return;
            }
            // 修复：统一使用 NetworkHandler.sendToServer
            NetworkHandler.sendToServer(new EnderPearlPacket());
        }

        // 末影箱快捷键
        if (keyEnderchest != null && keyEnderchest.consumeClick()) {
            if (ConfigHandler.REQUIRE_RING.get() && !isRingEquipped(player)) {
                player.displayClientMessage(Component.nullToEmpty(""), true);
                return;
            }
            NetworkHandler.sendToServer(new EnderChestPacket());
        }

        // 快捷栏交换快捷键
        if (keyHotbar != null && keyHotbar.consumeClick()) {
            if (ConfigHandler.REQUIRE_RING.get() && !isRingEquipped(player)) {
                player.displayClientMessage(Component.nullToEmpty(""), true);
                return;
            }
            NetworkHandler.sendToServer(new HotbarSwapPacket());
        }

        // 打开背包快捷键
        if (keyInventory != null && keyInventory.consumeClick()) {
            if (ConfigHandler.REQUIRE_RING.get() && !isRingEquipped(player)) {
                player.displayClientMessage(Component.nullToEmpty(""), true);
                return;
            }
            NetworkHandler.sendToServer(new OpenInventoryPacket());
        }

        handleToggle();
    }

    private static void handleDiscordRingKey(Player player) {
        if (discordRingKey == null) return;

        if (discordRingKey.isDown() && !discordRingPressed) {
            if (isDiscordRingEquipped(player)) {
                NetworkHandler.sendToServer(new DiscordKeybindMessage(true));
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
        if (player == null || mc.screen != null) return;

        if (keyShinyStone != null && keyShinyStone.consumeClick()) {
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
                NetworkHandler.sendToServer(new ShinyStoneTogglePacket());
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