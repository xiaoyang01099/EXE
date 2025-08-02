package net.xiaoyang010.ex_enigmaticlegacy.Network.inputPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;
import net.xiaoyang010.ex_enigmaticlegacy.Container.PagedChestContainer;

import java.util.function.Supplier;

public class PageChestPacket {
    private final int page;

    public PageChestPacket(int page) {
        this.page = page;
    }

    public PageChestPacket(FriendlyByteBuf buf) {
        this.page = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(page);
    }

    public static PageChestPacket decode(FriendlyByteBuf buf) {
        return new PageChestPacket(buf);
    }

    public void handle(Supplier<Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.containerMenu instanceof PagedChestContainer container) {
                container.setPages(page);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}