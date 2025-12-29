package net.xiaoyang010.ex_enigmaticlegacy.Network;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage.*;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputPacket.*;

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
                TeleportPacket.class,
                TeleportPacket::encode,
                TeleportPacket::decode,
                TeleportPacket::handle
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
}