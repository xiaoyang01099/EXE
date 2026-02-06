package net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Client.particle.fx.FXRegistry;

import java.util.function.Function;
import java.util.function.Supplier;

public class EffectMessage {
    public int id;
    public CompoundTag tag;

    public EffectMessage() {
        this.id = 0;
        this.tag = new CompoundTag();
    }

    public EffectMessage(int id, CompoundTag tag) {
        this.id = id;
        this.tag = tag;
    }

    public static void encode(EffectMessage message, FriendlyByteBuf buf) {
        buf.writeInt(message.id);
        buf.writeNbt(message.tag);
    }

    public static EffectMessage decode(FriendlyByteBuf buf) {
        int id = buf.readInt();
        CompoundTag tag = buf.readNbt();
        return new EffectMessage(id, tag);
    }

    public static void handle(EffectMessage message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide().isClient()) {
                handleClient(message);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static void handleClient(EffectMessage message) {
        Minecraft.getInstance().execute(() -> {
            Function<CompoundTag, ?> effect = FXRegistry.effects.get(message.id);
            if (effect != null) {
                effect.apply(message.tag);
            }
        });
    }
}
