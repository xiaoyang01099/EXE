package net.xiaoyang010.ex_enigmaticlegacy.Capability;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class PacketYuhuaMark {

    private final UUID uuid;
    private final int  layers;
    private final int  maxLayers;

    public PacketYuhuaMark(UUID uuid, int layers, int maxLayers) {
        this.uuid      = uuid;
        this.layers    = layers;
        this.maxLayers = maxLayers;
    }

    public static void encode(PacketYuhuaMark msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.uuid);
        buf.writeInt(msg.layers);
        buf.writeInt(msg.maxLayers);
    }

    public static PacketYuhuaMark decode(FriendlyByteBuf buf) {
        return new PacketYuhuaMark(
                buf.readUUID(),
                buf.readInt(),
                buf.readInt()
        );
    }

    public static void handle(PacketYuhuaMark msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            long clientTick = 0;
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                clientTick = mc.level.getGameTime();
            }

            YuhuaEntityRenderer.markYuhua(msg.uuid, msg.layers, msg.maxLayers, clientTick);

            if (mc.level == null) return;

            for (Entity e : mc.level.entitiesForRendering()) {
                if (e.getUUID().equals(msg.uuid) && e instanceof LivingEntity living) {
                    YuhuaBreakEffect.spawnHitEffect(living);
                    break;
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}