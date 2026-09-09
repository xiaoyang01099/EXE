package org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import org.xiaoyang.ex_enigmaticlegacy.Network.NetworkHandler;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.coffin.CoffinData;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.coffin.CoffinExecutions;

import java.util.UUID;
import java.util.function.Consumer;

public final class CoffinPacket {
    public static Consumer<State> clientReceiver = state -> {};

    public record State(UUID target, ResourceLocation dimension, Vec3 anchor, float yaw, float pitch, float width, float height, long startTime, boolean ended, int form) {
        public static State decode(FriendlyByteBuf buf) {
            return new State(buf.readUUID(), buf.readResourceLocation(),
                    new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                    buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(),
                    buf.readLong(), buf.readBoolean(),buf.readVarInt()==1?1:0);
        }
        public void encode(FriendlyByteBuf buf) {
            buf.writeUUID(target);
            buf.writeResourceLocation(dimension);
            buf.writeDouble(anchor.x);
            buf.writeDouble(anchor.y);
            buf.writeDouble(anchor.z);
            buf.writeFloat(yaw);
            buf.writeFloat(pitch);
            buf.writeFloat(width);
            buf.writeFloat(height);
            buf.writeLong(startTime);
            buf.writeBoolean(ended);
            buf.writeVarInt(form);
        }
    }

    public static State state(CoffinData.Entry entry, ServerLevel level, boolean ended) {
        return new State(entry.target, entry.dimension, entry.anchor,
                entry.yaw, entry.pitch, entry.width, entry.height,
                level.getGameTime() - entry.age, ended,entry.form);
    }

    public static void broadcast(CoffinData.Entry entry, ServerLevel level, boolean ended) {
        NetworkHandler.CHANNEL.send(PacketDistributor.DIMENSION.with(level::dimension),
                state(entry, level, ended));
    }

    public static void send(CoffinData.Entry entry, ServerPlayer player) {
        if (entry.dimension.equals(player.level().dimension().location()) &&
                entry.age < CoffinExecutions.duration(entry.form)) {
            NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                    state(entry, player.serverLevel(), false));
        }
    }

    private CoffinPacket() {}
}
