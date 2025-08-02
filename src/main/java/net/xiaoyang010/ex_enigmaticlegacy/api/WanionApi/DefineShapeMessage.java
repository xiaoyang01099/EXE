package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF.IResourceShapedContainer;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class DefineShapeMessage
{
    private int windowId;
    private ResourceLocation resourceLocation;

    public DefineShapeMessage(final int windowId, final ResourceLocation resourceLocation)
    {
        this.windowId = windowId;
        this.resourceLocation = resourceLocation;
    }

    public DefineShapeMessage() {}

    public static void encode(DefineShapeMessage message, FriendlyByteBuf buffer)
    {
        buffer.writeVarInt(message.windowId);
        buffer.writeResourceLocation(message.resourceLocation);
    }

    public static DefineShapeMessage decode(FriendlyByteBuf buffer)
    {
        int windowId = buffer.readVarInt();
        ResourceLocation resourceLocation = buffer.readResourceLocation();
        return new DefineShapeMessage(windowId, resourceLocation);
    }

    public static void handle(DefineShapeMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer serverPlayer = context.getSender();
            if (serverPlayer != null &&
                    serverPlayer.containerMenu instanceof IResourceShapedContainer &&
                    serverPlayer.containerMenu.containerId == message.windowId) {
                ((IResourceShapedContainer) serverPlayer.containerMenu).defineShape(message.resourceLocation);
            }
        });
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    public static void sendToServer(@Nonnull final AbstractContainerMenu container, @Nonnull final ResourceLocation resourceLocation)
    {
        NetworkHandler.CHANNEL.sendToServer(new DefineShapeMessage(container.containerId, resourceLocation));
    }
}