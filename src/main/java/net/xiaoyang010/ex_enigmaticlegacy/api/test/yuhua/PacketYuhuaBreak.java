package net.xiaoyang010.ex_enigmaticlegacy.api.test.yuhua;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class PacketYuhuaBreak {

    private final UUID uuid;

    public PacketYuhuaBreak(UUID uuid) {
        this.uuid = uuid;
    }

    public static void encode(PacketYuhuaBreak msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.uuid);
    }

    public static PacketYuhuaBreak decode(FriendlyByteBuf buf) {
        return new PacketYuhuaBreak(buf.readUUID());
    }

    public static void handle(PacketYuhuaBreak msg,
                              Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;

            mc.level.entitiesForRendering().forEach(e -> {
                if (e.getUUID().equals(msg.uuid)
                        && e instanceof LivingEntity living) {
                    YuhuaBreakEffect.spawnBreakEffect(living);
                    YuhuaEntityRenderer.clearYuhua(msg.uuid);
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}