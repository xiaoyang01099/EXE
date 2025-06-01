
package net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage;

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

    // 从网络数据包读取数据
    public static PacketIndex decode(FriendlyByteBuf buf) {
        return new PacketIndex(buf.readInt());
    }

    // 将数据写入网络数据包
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.index);
    }

    // 处理收到的数据包
    public static void handle(PacketIndex message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // 确保在服务器线程上运行
            ServerPlayer player = context.getSender();
            if (player != null && player.containerMenu instanceof DeconTableMenu) {
                ((DeconTableMenu) player.containerMenu).setRecipeIndex(message.index);
            }
        });
        context.setPacketHandled(true);
    }
}