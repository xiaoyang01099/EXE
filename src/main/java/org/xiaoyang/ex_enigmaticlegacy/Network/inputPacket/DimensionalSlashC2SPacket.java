package org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.network.NetworkEvent;
import org.xiaoyang.ex_enigmaticlegacy.Item.armor.ArmorSunmaker;
import org.xiaoyang.ex_enigmaticlegacy.Network.NetworkHandler;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class DimensionalSlashC2SPacket {

    private static final Map<UUID, Long> COOLDOWNS = new ConcurrentHashMap<>();
    private static final long COOLDOWN_MS = 2000L;

    public DimensionalSlashC2SPacket() {}

    public static void encode(DimensionalSlashC2SPacket msg, FriendlyByteBuf buf) {}

    public static DimensionalSlashC2SPacket decode(FriendlyByteBuf buf) {
        return new DimensionalSlashC2SPacket();
    }

    public static void handle(DimensionalSlashC2SPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            // 服务端验证全套
            if (!(player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof ArmorSunmaker)) return;
            if (!(player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ArmorSunmaker)) return;
            if (!(player.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof ArmorSunmaker)) return;
            if (!(player.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof ArmorSunmaker)) return;

            // 服务端验证冷却
            UUID id = player.getUUID();
            long now = System.currentTimeMillis();
            if (now - COOLDOWNS.getOrDefault(id, 0L) < COOLDOWN_MS) return;
            COOLDOWNS.put(id, now);

            // 广播给范围内所有客户端
            double broadcastRange = 96.0;
            long seed = player.level().getGameTime() ^ id.getLeastSignificantBits();
            float facingX = (float) player.getLookAngle().x;
            float facingZ = (float) player.getLookAngle().z;
            double cx = player.getX();
            double cy = player.getY() + 1.0;
            double cz = player.getZ();

            DimensionalSlashS2CPacket pkt = new DimensionalSlashS2CPacket(
                    cx, cy, cz, 46, 80.0f, 10.0f, seed, facingX, facingZ, id
            );

            player.level().players().forEach(p -> {
                if (p.distanceTo(player) <= broadcastRange && p instanceof ServerPlayer sp) {
                    NetworkHandler.CHANNEL.send(
                            net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> sp),
                            pkt
                    );
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}