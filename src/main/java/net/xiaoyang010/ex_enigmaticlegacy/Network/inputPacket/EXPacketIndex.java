package net.xiaoyang010.ex_enigmaticlegacy.Network.inputPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Container.ExtremeDisassemblyMenu;
import java.util.function.Supplier;

public class EXPacketIndex {
    private final int index;

    public EXPacketIndex(int index) {
        this.index = index;
    }

    public static EXPacketIndex decode(FriendlyByteBuf buf) {
        return new EXPacketIndex(buf.readInt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.index);
    }

    public static void handle(EXPacketIndex message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && player.containerMenu instanceof ExtremeDisassemblyMenu) {
                ((ExtremeDisassemblyMenu) player.containerMenu).setRecipeIndex(message.index);
            }
        });
        context.setPacketHandled(true);
    }
}