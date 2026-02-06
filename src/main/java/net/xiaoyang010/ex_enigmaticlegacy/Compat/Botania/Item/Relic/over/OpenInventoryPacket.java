package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.Supplier;

public class OpenInventoryPacket {

    public OpenInventoryPacket() {}

    public static void encode(OpenInventoryPacket msg, FriendlyByteBuf buf) {}

    public static OpenInventoryPacket decode(FriendlyByteBuf buf) {
        return new OpenInventoryPacket();
    }

    public static void handle(OpenInventoryPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            if (!isRingEquipped(player)) {
                player.displayClientMessage(
                        Component.nullToEmpty(""),
                        true
                );
                return;
            }

            NetworkHooks.openGui(player,
                    new SimpleMenuProvider(
                            (id, inv, p) -> new ContainerOverpowered(id, inv),
                            Component.nullToEmpty("")
                    )
            );
        });
        ctx.get().setPacketHandled(true);
    }

    public static boolean isRingEquipped(Player player) {
        return ItemPowerRing.isRingEquipped(player);
    }
}