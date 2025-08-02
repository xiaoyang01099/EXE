package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF.IResourceShapedContainer;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class ClearShapeMessage
{
    private int windowId;

    public ClearShapeMessage(final int windowId)
    {
        this.windowId = windowId;
    }

    public ClearShapeMessage() {}

    public static ClearShapeMessage decode(FriendlyByteBuf buf)
    {
        ClearShapeMessage message = new ClearShapeMessage();
        message.windowId = buf.readVarInt();
        return message;
    }

    public static void encode(ClearShapeMessage message, FriendlyByteBuf buf)
    {
        buf.writeVarInt(message.windowId);
    }

    @OnlyIn(Dist.CLIENT)
    public static void sendToServer(@Nonnull final AbstractContainerMenu containerMenu)
    {
       NetworkHandler.sendToServer(new ClearShapeMessage(containerMenu.containerId));
    }

    public static class Handler
    {
        public static void handle(final ClearShapeMessage message, Supplier<NetworkEvent.Context> contextSupplier)
        {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> {
                final Player player = context.getSender();
                if (player != null && player.containerMenu instanceof IResourceShapedContainer && player.containerMenu.containerId == message.windowId)
                    ((IResourceShapedContainer) player.containerMenu).clearShape();
            });
            context.setPacketHandled(true);
        }
    }
}