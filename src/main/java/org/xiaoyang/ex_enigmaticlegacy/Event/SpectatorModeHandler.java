package org.xiaoyang.ex_enigmaticlegacy.Event;

import com.yuo.endless.Items.Armor.InfinityArmor;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;

public class SpectatorModeHandler {
    public static final Map<UUID, GameType> previousGameModes = new HashMap<>();
    public static final Set<UUID> spectatorModeUsers = new HashSet<>();
    public static final Map<UUID, Long> lastToggleTime = new HashMap<>();
    private static final long TOGGLE_COOLDOWN = 1000;

    public static void init() {
        MinecraftForge.EVENT_BUS.register(new SpectatorModeHandler());
    }

    public static boolean isWearingFullInfinityArmor(Player player) {
        for (ItemStack armorPiece : player.getInventory().armor) {
            if (armorPiece.isEmpty() || !(armorPiece.getItem() instanceof InfinityArmor)) {
                return false;
            }
        }
        return true;
    }

    public static void toggleSpectatorMode(ServerPlayer player) {
        if (!isWearingFullInfinityArmor(player)) {
            player.sendSystemMessage(Component.translatable("message.ex_enigmaticlegacy.spectator_armor_required")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        UUID playerUUID = player.getUUID();
        long currentTime = System.currentTimeMillis();

        if (lastToggleTime.containsKey(playerUUID)) {
            long timeSinceLastToggle = currentTime - lastToggleTime.get(playerUUID);
            if (timeSinceLastToggle < TOGGLE_COOLDOWN) {
                return;
            }
        }

        lastToggleTime.put(playerUUID, currentTime);

        if (spectatorModeUsers.contains(playerUUID)) {
            exitSpectatorMode(player);
        } else {
            enterSpectatorMode(player);
        }
    }

    private static void enterSpectatorMode(ServerPlayer player) {
        UUID playerUUID = player.getUUID();

        previousGameModes.put(playerUUID, player.gameMode.getGameModeForPlayer());

        player.setGameMode(GameType.SPECTATOR);
        spectatorModeUsers.add(playerUUID);

        player.sendSystemMessage(Component.translatable("message.ex_enigmaticlegacy.spectator_enter")
                .withStyle(ChatFormatting.GREEN));
    }

    private static void exitSpectatorMode(ServerPlayer player) {
        UUID playerUUID = player.getUUID();

        GameType previousMode = previousGameModes.getOrDefault(playerUUID, GameType.SURVIVAL);
        player.setGameMode(previousMode);

        spectatorModeUsers.remove(playerUUID);
        previousGameModes.remove(playerUUID);

        player.sendSystemMessage(Component.translatable("message.ex_enigmaticlegacy.spectator_exit")
                .withStyle(ChatFormatting.YELLOW));
    }

    private static void checkSpectatorModeValidity(ServerPlayer player) {
        UUID playerUUID = player.getUUID();
        if (spectatorModeUsers.contains(playerUUID) && !isWearingFullInfinityArmor(player)) {
            exitSpectatorMode(player);
            player.sendSystemMessage(Component.translatable("message.ex_enigmaticlegacy.spectator_auto_exit")
                    .withStyle(ChatFormatting.GOLD));
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level.isClientSide) {
            if (event.player instanceof ServerPlayer serverPlayer) {
                if (event.player.tickCount % 20 == 0) {
                    checkSpectatorModeValidity(serverPlayer);
                }
            }
        }
    }
}