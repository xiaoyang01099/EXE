package org.xiaoyang.ex_enigmaticlegacy.Compat.Projecte;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.xiaoyang.ex_enigmaticlegacy.Network.NetworkHandler;

import java.util.function.Supplier;

public class EMCWandSetValuePacket {
    private final String itemId;
    private final long emcValue;

    public EMCWandSetValuePacket(String itemId, long emcValue) {
        this.itemId = itemId;
        this.emcValue = emcValue;
    }

    public static void encode(EMCWandSetValuePacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.itemId);
        buf.writeLong(msg.emcValue);
    }

    public static EMCWandSetValuePacket decode(FriendlyByteBuf buf) {
        return new EMCWandSetValuePacket(buf.readUtf(256), buf.readLong());
    }

    public static void handle(EMCWandSetValuePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            if (!player.hasPermissions(2) && !EMCWandHelper.isAuthorized(player.getUUID())) {
                player.displayClientMessage(
                        Component.translatable("message.ex_enigmaticlegacy.emc_wand.no_permission"), false);
                return;
            }

            ResourceLocation rl = ResourceLocation.tryParse(msg.itemId);
            if (rl == null) return;

            Item item = ForgeRegistries.ITEMS.getValue(rl);
            if (item == null) return;

            boolean success;
            if (msg.emcValue <= 0) {
                success = EMCWandHelper.removeEmcValue(item);
            } else {
                success = EMCWandHelper.setEmcValue(item, msg.emcValue);
            }

            NetworkHandler.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new EMCWandResultPacket(msg.itemId, msg.emcValue, success)
            );

            if (success) {
                player.displayClientMessage(
                        Component.translatable("message.ex_enigmaticlegacy.emc_wand.set_success",
                                item.getDescription().getString(),
                                msg.emcValue), false);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}