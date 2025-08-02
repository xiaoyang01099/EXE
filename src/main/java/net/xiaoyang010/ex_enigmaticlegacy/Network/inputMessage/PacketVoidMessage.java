package net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model.Vector3;
import net.xiaoyang010.ex_enigmaticlegacy.Network.ClientProxy;
import vazkii.botania.api.BotaniaAPI;

import java.util.function.Supplier;

public class PacketVoidMessage {
    private double x;
    private double y;
    private double z;
    private boolean finish;

    public PacketVoidMessage() {
    }

    public PacketVoidMessage(double x, double y, double z, boolean finish) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.finish = finish;
    }

    public static void encode(PacketVoidMessage msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
        buf.writeBoolean(msg.finish);
    }

    public static PacketVoidMessage decode(FriendlyByteBuf buf) {
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        boolean finish = buf.readBoolean();
        return new PacketVoidMessage(x, y, z, finish);
    }

    public static void handle(PacketVoidMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()) {
                handleClientSide(message);
            }
        });
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClientSide(PacketVoidMessage message) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        Vector3 thisPos = new Vector3(message.x, message.y, message.z);

        if (!message.finish) {
            for (int counter = 0; counter < 8; ++counter) {
                double calculatedPositionX = thisPos.x + (Math.random() - 0.5F) * 12.0F;
                double calculatedPositionY = thisPos.y + (Math.random() - 0.5F) * 12.0F;
                double calculatedPositionZ = thisPos.z + (Math.random() - 0.5F) * 12.0F;

                Vector3 targetPos = new Vector3(calculatedPositionX, calculatedPositionY, calculatedPositionZ);
                Vector3 diff = thisPos.copy().sub(targetPos);
                diff.multiply(0.08F);

                float calculatedMotionX = (float) diff.x;
                float calculatedMotionY = (float) diff.y;
                float calculatedMotionZ = (float) diff.z;

                float r = 0.2F + (float) Math.random() * 0.3F;
                float g = 0.0F;
                float b = 0.5F + (float) Math.random() * 0.2F;
                float s = 0.2F + (float) Math.random() * 0.2F;

                BotaniaAPI.instance().sparkleFX(
                        player.level,
                        calculatedPositionX, calculatedPositionY, calculatedPositionZ,
                        r, g, b, s, 30
                );
            }

            for (int counter = 0; counter < 5; ++counter) {
                ClientProxy proxy = new ClientProxy();
                ClientProxy.spawnSuperParticle(
                        player.level,
                        "portalstuff",
                        thisPos.x, thisPos.y, thisPos.z,
                        (Math.random() - 0.5F) * 8.0F,
                        (Math.random() - 0.5F) * 8.0F,
                        (Math.random() - 0.5F) * 8.0F,
                        1.0F,
                        64.0F
                );
            }
        } else {
            for (int i = 0; i <= 128; ++i) {
                float r = 0.2F + (float) Math.random() * 0.3F;
                float g = 0.0F;
                float b = 0.5F + (float) Math.random() * 0.2F;
                float s = 0.4F + (float) Math.random() * 0.4F;
                float m = 0.5F;

                float xm = ((float) Math.random() - 0.5F) * m;
                float ym = ((float) Math.random() - 0.5F) * m;
                float zm = ((float) Math.random() - 0.5F) * m;

                BotaniaAPI.instance().sparkleFX(
                        player.level,
                        thisPos.x, thisPos.y, thisPos.z,
                        r, g, b, s, 60
                );
            }
        }
    }
}