package net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class StepHeightMessage {
    public float height;

    public StepHeightMessage() {
    }

    public StepHeightMessage(float height) {
        this.height = height;
    }

    public static void encode(StepHeightMessage msg, FriendlyByteBuf buf) {
        buf.writeFloat(msg.height);
    }

    public static StepHeightMessage decode(FriendlyByteBuf buf) {
        return new StepHeightMessage(buf.readFloat());
    }

    public static void handle(StepHeightMessage msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()) {
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.player != null) {
                    Objects.requireNonNull(minecraft.player.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get()))
                            .setBaseValue(msg.height - 0.6);
                }
            }
        });
        context.setPacketHandled(true);
    }
}