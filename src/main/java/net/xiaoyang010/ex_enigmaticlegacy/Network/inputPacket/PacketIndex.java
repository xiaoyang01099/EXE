package net.xiaoyang010.ex_enigmaticlegacy.Network.inputPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Container.DeconTableMenu;

import java.util.function.Supplier;

public class PacketIndex {
    private final int index;

    public PacketIndex(int index) {
        this.index = index;
    }

    public static PacketIndex decode(FriendlyByteBuf buf) {
        return new PacketIndex(buf.readInt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.index);
    }

    public static void handle(PacketIndex message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && player.containerMenu instanceof DeconTableMenu) {
                ((DeconTableMenu) player.containerMenu).setRecipeIndex(message.index);
            }
        });
        context.setPacketHandled(true);
    }
}