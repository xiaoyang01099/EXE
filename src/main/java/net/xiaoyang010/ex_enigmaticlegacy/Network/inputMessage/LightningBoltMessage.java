package net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class LightningBoltMessage {
    private double x;
    private double y;
    private double z;
    private int amount;

    public LightningBoltMessage() {
    }

    public LightningBoltMessage(double x, double y, double z, int amount) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.amount = amount;
    }

    public static void encode(LightningBoltMessage message, FriendlyByteBuf buf) {
        buf.writeDouble(message.x);
        buf.writeDouble(message.y);
        buf.writeDouble(message.z);
        buf.writeInt(message.amount);
    }

    public static LightningBoltMessage decode(FriendlyByteBuf buf) {
        return new LightningBoltMessage(
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readInt()
        );
    }

    public static void handle(LightningBoltMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            handleClientSide(message);
        });
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClientSide(LightningBoltMessage message) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        Level world = player.level;

        for (int counter = message.amount; counter > 0; --counter) {
            LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(world);
            if (lightning != null) {
                lightning.moveTo(message.x, message.y, message.z);
                world.addFreshEntity(lightning);
            }
        }
    }
}