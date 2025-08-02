package net.xiaoyang010.ex_enigmaticlegacy.Network.inputPacket;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Container.DimensionalMirrorContainer;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;



public class TeleportPacket {
    private final ResourceKey<Level> dimension;

    public TeleportPacket(ResourceKey<Level> dimension) {
        this.dimension = dimension;
    }

    // 将数据编码到 buffer 中
    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(dimension.location());
    }

    // 从 buffer 中解码数据
    public static TeleportPacket decode(FriendlyByteBuf buf) {
        ResourceLocation dimensionRL = buf.readResourceLocation();
        ResourceKey<Level> dimension = null;

        // 根据 ResourceLocation 确定维度
        if (dimensionRL.equals(Level.OVERWORLD.location())) {
            dimension = Level.OVERWORLD;
        } else if (dimensionRL.equals(Level.NETHER.location())) {
            dimension = Level.NETHER;
        } else if (dimensionRL.equals(Level.END.location())) {
            dimension = Level.END;
        }

        return new TeleportPacket(dimension);
    }

    // 处理数据包
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var player = ctx.get().getSender();
            if (player != null && player.containerMenu instanceof DimensionalMirrorContainer container) {
                container.teleportToDimension(this.dimension);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    // getter 方法
    public ResourceKey<Level> getDimension() {
        return dimension;
    }
}