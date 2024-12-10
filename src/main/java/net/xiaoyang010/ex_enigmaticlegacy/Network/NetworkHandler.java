package net.xiaoyang010.ex_enigmaticlegacy.Network;


import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage.DamageMessage;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage.PowerGloveMessage;

public class NetworkHandler
{
    public static SimpleChannel INSTANCE;
    private static int ID = 0;

    private static int nextID()
    {
        return ID++;
    }

    public static void registerMessage()
    {
        INSTANCE = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(ExEnigmaticlegacyMod.MODID + ":networking"),
                () -> "1.0",
                (s) -> true,
                (s) -> true
        );
        INSTANCE.registerMessage(
                nextID(),
                PowerGloveMessage.class,
                PowerGloveMessage::encode,
                PowerGloveMessage::decode,
                PowerGloveMessage::handlePowerGLove
        );
        INSTANCE.registerMessage(
                nextID(),
                DamageMessage.class,
                DamageMessage::encode,
                DamageMessage::decode,
                DamageMessage::handleDamage
        );
    }

    private static void sendToNearby(Level level, BlockPos pos, Object toSend)
    {
        if (level instanceof ServerLevel ws)
        {
            ws.getChunkSource().chunkMap.getPlayers(new ChunkPos(pos), false)
                    .stream().filter(p -> p.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < 64 * 64)
                    .forEach(p -> INSTANCE.send(PacketDistributor.PLAYER.with(() -> p), toSend));
        }
    }

    public static void sendToNearby(Level level, Entity e, Object toSend)
    {
        sendToNearby(level, new BlockPos(e.getX(), e.getY(), e.getZ()), toSend);
    }

    public static void sendTo(ServerPlayer playerMP, Object toSend)
    {
        INSTANCE.sendTo(toSend, playerMP.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
    }
}
