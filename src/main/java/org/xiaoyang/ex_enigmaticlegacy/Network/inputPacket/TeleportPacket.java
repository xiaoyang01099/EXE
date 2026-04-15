package org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import org.xiaoyang.ex_enigmaticlegacy.Container.DimensionalMirrorContainer;

import java.util.function.Supplier;

public class TeleportPacket {
    private final ResourceKey<Level> dimension;

    public TeleportPacket(ResourceKey<Level> dimension) {
        this.dimension = dimension;
    }
    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(dimension.location());
    }

    public static TeleportPacket decode(FriendlyByteBuf buf) {
        ResourceLocation dimensionRL = buf.readResourceLocation();
        ResourceKey<Level> dimension = null;

        if (dimensionRL.equals(Level.OVERWORLD.location())) {
            dimension = Level.OVERWORLD;
        } else if (dimensionRL.equals(Level.NETHER.location())) {
            dimension = Level.NETHER;
        } else if (dimensionRL.equals(Level.END.location())) {
            dimension = Level.END;
        }

        return new TeleportPacket(dimension);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var player = ctx.get().getSender();
            if (player != null && player.containerMenu instanceof DimensionalMirrorContainer container) {
                container.teleportToDimension(this.dimension);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public ResourceKey<Level> getDimension() {
        return dimension;
    }
}