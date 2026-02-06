package net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Event.RelicsEventHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;

import java.util.function.Supplier;

public class DiscordKeybindMessage {

    private boolean activated;

    public DiscordKeybindMessage() {
    }

    public DiscordKeybindMessage(boolean activated) {
        this.activated = activated;
    }

    public static void encode(DiscordKeybindMessage message, FriendlyByteBuf buf) {
        buf.writeBoolean(message.activated);
    }

    public static DiscordKeybindMessage decode(FriendlyByteBuf buf) {
        return new DiscordKeybindMessage(buf.readBoolean());
    }

    public static void handle(DiscordKeybindMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            if (!RelicsEventHandler.hasBauble(player, ModItems.DISCORD_RING.get())) {
                return;
            }

            if (RelicsEventHandler.isOnCoodown(player)) {
                return;
            }

            int range = 16;
            RelicsEventHandler.cryHavoc(player.level, player, range);

            player.level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDER_EYE_DEATH, SoundSource.PLAYERS, 1.0F, 0.5F);

            RelicsEventHandler.imposeBurst(player.level, player.getX(), player.getY() + 1, player.getZ(), 2.0f);

            RelicsEventHandler.setCasted(player, 100, true);
        });
        context.setPacketHandled(true);
    }
}
