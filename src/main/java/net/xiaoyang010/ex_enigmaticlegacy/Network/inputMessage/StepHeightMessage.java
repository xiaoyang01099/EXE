package net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class StepHeightMessage {
    private final float height;

    public StepHeightMessage(float height) {
        this.height = height;
    }

    public static void encode(StepHeightMessage msg, FriendlyByteBuf buf) {
        buf.writeFloat(msg.height);
    }

    public static StepHeightMessage decode(FriendlyByteBuf buf) {
        return new StepHeightMessage(buf.readFloat());
    }

    @OnlyIn(Dist.CLIENT)
    public static void handle(StepHeightMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null) {
                minecraft.player.maxUpStep = msg.height;
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
