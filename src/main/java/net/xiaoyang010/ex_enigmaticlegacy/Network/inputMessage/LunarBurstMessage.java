package net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Network.ClientProxy;

import java.util.function.Supplier;

public class LunarBurstMessage {
    private final double x;
    private final double y;
    private final double z;
    private final float size;

    public LunarBurstMessage(double x, double y, double z, float size) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.size = size;
    }

    public static void encode(LunarBurstMessage message, FriendlyByteBuf buf) {
        buf.writeDouble(message.x);
        buf.writeDouble(message.y);
        buf.writeDouble(message.z);
        buf.writeFloat(message.size);
    }

    public static LunarBurstMessage decode(FriendlyByteBuf buf) {
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        float size = buf.readFloat();
        return new LunarBurstMessage(x, y, z, size);
    }

    public static void handle(LunarBurstMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    Player player = Minecraft.getInstance().player;
                    if (player != null) {
                        ClientProxy.lunarBurst(player.level, message.x, message.y, message.z, message.size);
                    }
                });
            });
        }
        context.setPacketHandled(true);
    }
}