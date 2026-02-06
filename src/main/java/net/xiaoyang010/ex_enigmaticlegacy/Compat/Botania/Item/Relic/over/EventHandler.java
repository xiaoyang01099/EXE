package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Config.ConfigHandler;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;


@Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandler {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getPlayer();

        if (player instanceof ServerPlayer serverPlayer && !player.level.isClientSide) {
            ServerLevel level = serverPlayer.getLevel();

            PlayerInventoryData.get(level);
            PlayerUnlockData.get(level);

            NetworkHandler.sendPearlSync(serverPlayer,
                    PlayerUnlockData.isEPearlUnlocked(serverPlayer));

            NetworkHandler.sendChestSync(serverPlayer,
                    PlayerUnlockData.isEChestUnlocked(serverPlayer));

            NetworkHandler.sendStorageSync(serverPlayer,
                    PlayerUnlockData.getStorageCount(serverPlayer));


            var inventory = PlayerInventoryData.getInventory(serverPlayer);
            inventory.syncAll(serverPlayer);

            ExEnigmaticlegacyMod.LOGGER.info("Player {} logged in, data synced",
                    player.getName().getString());
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ServerLevel level = event.getServer().overworld();
        PlayerInventoryData.saveAll(level);
        ExEnigmaticlegacyMod.LOGGER.info("Saved all player inventory data");
    }

    @SubscribeEvent
    public static void onConfigChanged(ModConfigEvent event) {
        if (event.getConfig().getModId().equals(ExEnigmaticlegacyMod.MODID)) {
            ExEnigmaticlegacyMod.LOGGER.info("Power Inventory config reloaded");
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        Player newPlayer = event.getPlayer();

        if (newPlayer.level.isClientSide) return;

        if (!event.isWasDeath()) {
            copyPlayerData(original, newPlayer);
            ExEnigmaticlegacyMod.LOGGER.info("Player {} dimension transfer,d",
                    newPlayer.getName().getString());
            return;
        }

        if (ConfigHandler.PERSIST_ON_DEATH.get()) {
            copyPlayerData(original, newPlayer);
            ExEnigmaticlegacyMod.LOGGER.info("Player {} died, data persisted",
                    newPlayer.getName().getString());
        } else {
            PlayerUnlockData.setEPearlUnlocked(newPlayer, false);
            PlayerUnlockData.setEChestUnlocked(newPlayer, false);
            PlayerUnlockData.setStorageCount(newPlayer, 0);

            PlayerInventoryData.clearInventory(newPlayer);

            ExEnigmaticlegacyMod.LOGGER.info("Player {} died, data reset",
                    newPlayer.getName().getString());
        }
    }

    private static void copyPlayerData(Player from, Player to) {
        if (from.level.isClientSide || to.level.isClientSide) return;

        boolean ePearl = PlayerUnlockData.isEPearlUnlocked(from);
        boolean eChest = PlayerUnlockData.isEChestUnlocked(from);
        int storage = PlayerUnlockData.getStorageCount(from);

        PlayerUnlockData.setEPearlUnlocked(to, ePearl);
        PlayerUnlockData.setEChestUnlocked(to, eChest);
        PlayerUnlockData.setStorageCount(to, storage);

        var fromInv = PlayerInventoryData.getInventory(from);
        var toInv = PlayerInventoryData.getInventory(to);

        for (int i = 0; i < fromInv.getContainerSize(); i++) {
            toInv.setItem(i, fromInv.getItem(i).copy());
        }

        toInv.enderPearlStack = fromInv.enderPearlStack.copy();
        toInv.enderChestStack = fromInv.enderChestStack.copy();

        PlayerInventoryData.saveInventory(to, toInv);
    }

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        Entity entity = event.getEntity();

        if (!(entity instanceof Player player)) return;
        if (player.level.isClientSide) return;

        if (!ConfigHandler.PERSIST_ON_DEATH.get()) {
            var inventory = PlayerInventoryData.getInventory(player);

            for (int i = Const.HOTBAR_SIZE; i < inventory.getContainerSize(); i++) {
                var stack = inventory.getItem(i);
                if (!stack.isEmpty()) {
                    player.drop(stack, true, false);
                    inventory.setItem(i, net.minecraft.world.item.ItemStack.EMPTY);
                }
            }

            // 掉落末影珍珠
            if (!inventory.enderPearlStack.isEmpty()) {
                player.drop(inventory.enderPearlStack, true, false);
                inventory.enderPearlStack = net.minecraft.world.item.ItemStack.EMPTY;
            }

            // 掉落末影箱
            if (!inventory.enderChestStack.isEmpty()) {
                player.drop(inventory.enderChestStack, true, false);
                inventory.enderChestStack = net.minecraft.world.item.ItemStack.EMPTY;
            }

            PlayerInventoryData.saveInventory(player, inventory);

            ExEnigmaticlegacyMod.LOGGER.info("Player {} inventory dropped on death",
                    player.getName().getString());
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getPlayer();

        if (!player.level.isClientSide) {
            var inventory = PlayerInventoryData.getInventory(player);
            PlayerInventoryData.saveInventory(player, inventory);

            ExEnigmaticlegacyMod.LOGGER.info("Player {} logged out, data saved",
                    player.getName().getString());
        }
    }


    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getPlayer();

        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHandler.sendPearlSync(serverPlayer,
                    PlayerUnlockData.isEPearlUnlocked(serverPlayer));

            NetworkHandler.sendChestSync(serverPlayer,
                    PlayerUnlockData.isEChestUnlocked(serverPlayer));

            NetworkHandler.sendStorageSync(serverPlayer,
                    PlayerUnlockData.getStorageCount(serverPlayer));

            var inventory = PlayerInventoryData.getInventory(serverPlayer);
            inventory.syncAll(serverPlayer);

            ExEnigmaticlegacyMod.LOGGER.info("Player {} dimension sync complete",
                    player.getName().getString());
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (player instanceof ServerPlayer serverPlayer && !player.level.isClientSide) {
            NetworkHandler.sendPearlSync(serverPlayer,
                    PlayerUnlockData.isEPearlUnlocked(serverPlayer));

            NetworkHandler.sendChestSync(serverPlayer,
                    PlayerUnlockData.isEChestUnlocked(serverPlayer));

            NetworkHandler.sendStorageSync(serverPlayer,
                    PlayerUnlockData.getStorageCount(serverPlayer));

            ExEnigmaticlegacyMod.LOGGER.info("Player {} respawned, data synced",
                    player.getName().getString());
        }
    }
}