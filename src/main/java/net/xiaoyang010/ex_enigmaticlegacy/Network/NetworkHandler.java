package net.xiaoyang010.ex_enigmaticlegacy.Network;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over.*;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage.*;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputPacket.*;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage.EffectMessage;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputPacket.PortalTraceMessage;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage.TelekinesisTomeLevelParticleMessage;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage.TelekinesisTomeLevelAttackMessage;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputPacket.HornChargeHudPacket;

import java.util.Optional;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ExEnigmaticlegacyMod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        CHANNEL.registerMessage(
                packetId++,
                RecipeTransferPacket.class,
                RecipeTransferPacket::encode,
                RecipeTransferPacket::decode,
                RecipeTransferPacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                BlinkMessage.class,
                BlinkMessage::encode,
                BlinkMessage::new,
                BlinkMessage::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                FindBlocksPacket.class,
                FindBlocksPacket::encode,
                FindBlocksPacket::decode,
                FindBlocksPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        CHANNEL.registerMessage(
                packetId++,
                HornChargeHudPacket.class,
                HornChargeHudPacket::encode,
                HornChargeHudPacket::decode,
                HornChargeHudPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        CHANNEL.registerMessage(
                packetId++,
                TelekinesisTomeLevelParticleMessage.class,
                TelekinesisTomeLevelParticleMessage::encode,
                TelekinesisTomeLevelParticleMessage::decode,
                TelekinesisTomeLevelParticleMessage::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        CHANNEL.registerMessage(
                packetId++,
                TelekinesisTomeLevelAttackMessage.class,
                TelekinesisTomeLevelAttackMessage::encode,
                TelekinesisTomeLevelAttackMessage::decode,
                TelekinesisTomeLevelAttackMessage::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );


        CHANNEL.registerMessage(
                packetId++,
                DiscordKeybindMessage.class,
                DiscordKeybindMessage::encode,
                DiscordKeybindMessage::decode,
                DiscordKeybindMessage::handle
        );

        CHANNEL.registerMessage(packetId++,
                PortalTraceMessage.class,
                PortalTraceMessage::encode,
                PortalTraceMessage::decode,
                PortalTraceMessage::handle
        );

        CHANNEL.registerMessage(packetId++,
                ShinyStoneTogglePacket.class,
                ShinyStoneTogglePacket::toBytes,
                ShinyStoneTogglePacket::decode,
                ShinyStoneTogglePacket::handle
        );

        CHANNEL.registerMessage(packetId++,
                EffectMessage.class,
                EffectMessage::encode,
                EffectMessage::decode,
                EffectMessage::handle
        );


        CHANNEL.registerMessage(packetId++,
                SyncSlotPacket.class,
                SyncSlotPacket::encode,
                SyncSlotPacket::decode,
                SyncSlotPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        CHANNEL.registerMessage(packetId++,
                SyncPearlUnlockPacket.class,
                SyncPearlUnlockPacket::encode,
                SyncPearlUnlockPacket::decode,
                SyncPearlUnlockPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        CHANNEL.registerMessage(packetId++,
                SyncChestUnlockPacket.class,
                SyncChestUnlockPacket::encode,
                SyncChestUnlockPacket::decode,
                SyncChestUnlockPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        CHANNEL.registerMessage(packetId++,
                SyncStorageCountPacket.class,
                SyncStorageCountPacket::encode,
                SyncStorageCountPacket::decode,
                SyncStorageCountPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        CHANNEL.registerMessage(packetId++,
                SwapInvoPacket.class,
                SwapInvoPacket::encode,
                SwapInvoPacket::decode,
                SwapInvoPacket::handle
        );

        CHANNEL.registerMessage(packetId++,
                FilterButtonPacket.class,
                FilterButtonPacket::encode,
                FilterButtonPacket::decode,
                FilterButtonPacket::handle
        );

        CHANNEL.registerMessage(packetId++,
                DumpButtonPacket.class,
                DumpButtonPacket::encode,
                DumpButtonPacket::decode,
                DumpButtonPacket::handle
        );

        CHANNEL.registerMessage(packetId++,
                OpenInventoryPacket.class,
                OpenInventoryPacket::encode,
                OpenInventoryPacket::decode,
                OpenInventoryPacket::handle
        );

        CHANNEL.registerMessage(packetId++,
                EnderPearlPacket.class,
                EnderPearlPacket::encode,
                EnderPearlPacket::decode,
                EnderPearlPacket::handle
        );

        CHANNEL.registerMessage(packetId++,
                EnderChestPacket.class,
                EnderChestPacket::encode,
                EnderChestPacket::decode,
                EnderChestPacket::handle
        );

        CHANNEL.registerMessage(packetId++,
                HotbarSwapPacket.class,
                HotbarSwapPacket::encode,
                HotbarSwapPacket::decode,
                HotbarSwapPacket::handle
        );

        CHANNEL.registerMessage(packetId++,
                UnlockPearlPacket.class,
                UnlockPearlPacket::encode,
                UnlockPearlPacket::decode,
                UnlockPearlPacket::handle
        );

        CHANNEL.registerMessage(packetId++,
                UnlockChestPacket.class,
                UnlockChestPacket::encode,
                UnlockChestPacket::decode,
                UnlockChestPacket::handle
        );

        CHANNEL.registerMessage(packetId++,
                UnlockStoragePacket.class,
                UnlockStoragePacket::encode,
                UnlockStoragePacket::decode,
                UnlockStoragePacket::handle
        );

        CHANNEL.registerMessage(packetId++,
                SortPacket.class,
                SortPacket::encode,
                SortPacket::decode,
                SortPacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                TeleportPacket.class,
                TeleportPacket::encode,
                TeleportPacket::decode,
                TeleportPacket::handle
        );
        CHANNEL.registerMessage(
                packetId++,
                AutoCrafterPacket.class,
                AutoCrafterPacket::toBytes,
                AutoCrafterPacket::new,
                AutoCrafterPacket::handler
        );
        CHANNEL.registerMessage(
                packetId++,
                AutoCrafterRecipePacket.class,
                AutoCrafterRecipePacket::toBytes,
                AutoCrafterRecipePacket::new,
                AutoCrafterRecipePacket::handler
        );
        CHANNEL.registerMessage(
                packetId++,
                PageChestPacket.class,
                PageChestPacket::encode,
                PageChestPacket::decode,
                PageChestPacket::handle
        );
        CHANNEL.registerMessage(
                packetId++,
                JumpPacket.class,
                JumpPacket::encode,
                JumpPacket::new,
                JumpPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
        CHANNEL.registerMessage(
                packetId++,
                CloudJumpParticlePacket.class,
                CloudJumpParticlePacket::encode,
                CloudJumpParticlePacket::decode,
                CloudJumpParticlePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        CHANNEL.registerMessage(
                packetId++,
                PacketIndex.class,
                PacketIndex::encode,
                PacketIndex::decode,
                PacketIndex::handle
        );
        CHANNEL.registerMessage(
                packetId++,
                EXPacketIndex.class,
                EXPacketIndex::encode,
                EXPacketIndex::decode,
                EXPacketIndex::handle
        );
        CHANNEL.registerMessage(
                packetId++,
                SpectatorModePacket.class,
                SpectatorModePacket::encode,
                SpectatorModePacket::decode,
                SpectatorModePacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                GuardianVanishMessage.class,
                GuardianVanishMessage::encode,
                GuardianVanishMessage::decode,
                GuardianVanishMessage::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        CHANNEL.registerMessage(
                packetId++,
                BurstMessage.class,
                BurstMessage::encode,
                BurstMessage::decode,
                BurstMessage::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        CHANNEL.registerMessage(
                packetId++,
                PlayerMotionUpdateMessage.class,
                PlayerMotionUpdateMessage::encode,
                PlayerMotionUpdateMessage::decode,
                PlayerMotionUpdateMessage::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        CHANNEL.registerMessage(
                packetId++,
                TelekinesisAttackMessage.class,
                TelekinesisAttackMessage::encode,
                TelekinesisAttackMessage::decode,
                TelekinesisAttackMessage::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );

        CHANNEL.registerMessage(
                packetId++,
                TelekinesisParticleMessage.class,
                TelekinesisParticleMessage::encode,
                TelekinesisParticleMessage::decode,
                TelekinesisParticleMessage::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        CHANNEL.registerMessage(
                packetId++,
                TelekinesisUseMessage.class,
                TelekinesisUseMessage::encode,
                TelekinesisUseMessage::decode,
                TelekinesisUseMessage::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );

        CHANNEL.registerMessage(
                packetId++,
                OverthrowChatMessage.class,
                OverthrowChatMessage::encode,
                OverthrowChatMessage::decode,
                OverthrowChatMessage::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        CHANNEL.registerMessage(
                packetId++,
                EntityMotionMessage.class,
                EntityMotionMessage::encode,
                EntityMotionMessage::decode,
                EntityMotionMessage::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        CHANNEL.registerMessage(
                packetId++,
                PacketVoidMessage.class,
                PacketVoidMessage::encode,
                PacketVoidMessage::decode,
                PacketVoidMessage::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        CHANNEL.registerMessage(
                packetId++,
                LightningMessage.class,
                LightningMessage::encode,
                LightningMessage::decode,
                LightningMessage::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        CHANNEL.registerMessage(
                packetId++,
                ICanSwingMySwordMessage.class,
                ICanSwingMySwordMessage::encode,
                ICanSwingMySwordMessage::decode,
                ICanSwingMySwordMessage::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        CHANNEL.registerMessage(
                packetId++,
                LunarFlaresParticleMessage.class,
                LunarFlaresParticleMessage::encode,
                LunarFlaresParticleMessage::decode,
                LunarFlaresParticleMessage::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        CHANNEL.registerMessage(
                packetId++,
                LunarBurstMessage.class,
                LunarBurstMessage::encode,
                LunarBurstMessage::decode,
                LunarBurstMessage::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        CHANNEL.registerMessage(
                packetId++,
                ApotheosisParticleMessage.class,
                ApotheosisParticleMessage::encode,
                ApotheosisParticleMessage::decode,
                ApotheosisParticleMessage::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        CHANNEL.registerMessage(
                packetId++,
                LightningBoltMessage.class,
                LightningBoltMessage::encode,
                LightningBoltMessage::decode,
                LightningBoltMessage::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        CHANNEL.registerMessage(
                packetId++,
                InfernalParticleMessage.class,
                InfernalParticleMessage::encode,
                InfernalParticleMessage::decode,
                InfernalParticleMessage::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        CHANNEL.registerMessage(
                packetId++,
                BanishmentCastingMessage.class,
                BanishmentCastingMessage::encode,
                BanishmentCastingMessage::decode,
                BanishmentCastingMessage::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        CHANNEL.registerMessage(
                packetId++,
                StepHeightMessage.class,
                StepHeightMessage::encode,
                StepHeightMessage::decode,
                StepHeightMessage::handle
        );
    }

    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }

    public static void sendTo(FindBlocksPacket packet, ServerPlayer player) {
        CHANNEL.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendToPlayer(ServerPlayer player, Object packet) {
        if (player != null && !player.level.isClientSide) {
            CHANNEL.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
        }
    }

    public static void sendApotheosisParticle(Level level, double x, double y, double z, int quantity, double radius) {
        if (!level.isClientSide) {
            ApotheosisParticleMessage packet = new ApotheosisParticleMessage(x, y, z, quantity);
            CHANNEL.send(PacketDistributor.NEAR.with(() ->
                    new PacketDistributor.TargetPoint(x, y, z, radius, level.dimension())), packet);
        }
    }

    public static void sendToPlayer(PacketVoidMessage packet, Level level, BlockPos pos, double radius) {
        if (!level.isClientSide) {
            CHANNEL.send(PacketDistributor.NEAR.with(() ->
                    new PacketDistributor.TargetPoint(
                            pos.getX(), pos.getY(), pos.getZ(), radius, level.dimension())), packet);
        }
    }

    public static void sendToPlayer(GuardianVanishMessage packet, Level level, BlockPos pos, float radius) {
        if (!level.isClientSide) {
            CHANNEL.send(PacketDistributor.NEAR.with(() ->
                    new PacketDistributor.TargetPoint(
                            pos.getX(), pos.getY(), pos.getZ(), radius, level.dimension())), packet);
        }
    }

    public static void sendToPlayer(GuardianVanishMessage packet) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
    }

    public static void sendOverthrowMessage(OverthrowChatMessage packet) {
    }

    public static void sendPearlSync(ServerPlayer player, boolean unlocked) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new SyncPearlUnlockPacket(unlocked));
    }

    public static void sendChestSync(ServerPlayer player, boolean unlocked) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new SyncChestUnlockPacket(unlocked));
    }

    public static void sendStorageSync(ServerPlayer player, int count) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new SyncStorageCountPacket(count));
    }

    public static void sendSlotSync(ServerPlayer player, int slot, ItemStack stack) {
        CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new SyncSlotPacket(slot, stack)
        );
    }

    public static void sendToPlayer(Object message, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}