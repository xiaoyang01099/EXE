package net.xiaoyang010.ex_enigmaticlegacy.Network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage.*;

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
                PrismRenderPacket.class,
                PrismRenderPacket::encode,
                PrismRenderPacket::new,
                PrismRenderPacket::handler,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
    }

    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }

    public static void sendToPlayer(ServerPlayer player, Object packet) {
        CHANNEL.sendTo(packet, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
    }
}