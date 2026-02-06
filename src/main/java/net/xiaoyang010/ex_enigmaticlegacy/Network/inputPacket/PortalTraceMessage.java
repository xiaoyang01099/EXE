package net.xiaoyang010.ex_enigmaticlegacy.Network.inputPacket;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model.Vector3;
import net.xiaoyang010.ex_enigmaticlegacy.Network.ClientProxy;

import java.util.function.Supplier;

public class PortalTraceMessage {

    private double x;
    private double y;
    private double z;
    private double xs;
    private double ys;
    private double zs;
    private double distance;

    public PortalTraceMessage() {
    }

    public PortalTraceMessage(double x, double y, double z, double xs, double ys, double zs, double distance) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.xs = xs;
        this.ys = ys;
        this.zs = zs;
        this.distance = distance;
    }

    public static void encode(PortalTraceMessage message, FriendlyByteBuf buf) {
        buf.writeDouble(message.x);
        buf.writeDouble(message.y);
        buf.writeDouble(message.z);
        buf.writeDouble(message.xs);
        buf.writeDouble(message.ys);
        buf.writeDouble(message.zs);
        buf.writeDouble(message.distance);
    }

    public static PortalTraceMessage decode(FriendlyByteBuf buf) {
        return new PortalTraceMessage(
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble()
        );
    }

    public static void handle(PortalTraceMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            handleClient(message);
        });
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(PortalTraceMessage message) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        Vector3 primalVec = new Vector3(message.x, message.y, message.z);
        Vector3 finalVec = new Vector3(message.xs, message.ys, message.zs);
        Vector3 diffVec = finalVec.copy().sub(primalVec);
        Vector3 motionVec = diffVec.copy().multiply(1 / message.distance);

        for (int counterS = (int) message.distance; counterS >= 0; counterS--) {
            for (int ISS = 0; ISS <= 4; ISS++) {
                ClientProxy.spawnSuperParticle(
                        player.level, "portalstuff",
                        primalVec.x, primalVec.y, primalVec.z,
                        (Math.random() - 0.5D), (Math.random() - 0.5D), (Math.random() - 0.5D),
                        1.0F, 64F
                );
            }
            primalVec.add(motionVec);
        }
    }
}
