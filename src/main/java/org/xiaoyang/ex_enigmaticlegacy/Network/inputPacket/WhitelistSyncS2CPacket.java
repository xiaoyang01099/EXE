package org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.xiaoyang.ex_enigmaticlegacy.Util.ClientWhitelistCache;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

// WhitelistSyncS2CPacket 把服务端生效后的实体伤害白名单同步到客户端。
public class WhitelistSyncS2CPacket {
    private final List<String> whitelist; // 服务端生效后的实体伤害白名单。

    public WhitelistSyncS2CPacket() {
        this.whitelist = List.of();
    }

    public WhitelistSyncS2CPacket(List<String> whitelist) {
        this.whitelist = whitelist;
    }

    public WhitelistSyncS2CPacket(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        List<String> entries = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            entries.add(buffer.readUtf());
        }
        this.whitelist = entries;
    }

    // 写入白名单字符串列表。
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(whitelist.size());
        for (String entry : whitelist) {
            buffer.writeUtf(entry);
        }
    }

    // 客户端主线程更新白名单缓存。
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ClientWhitelistCache.updateWhitelist(whitelist));
        context.setPacketHandled(true);
    }
}
