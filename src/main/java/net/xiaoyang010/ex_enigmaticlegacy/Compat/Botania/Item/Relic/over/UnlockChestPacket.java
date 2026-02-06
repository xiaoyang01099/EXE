package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.network.NetworkEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Config.ConfigHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Util.EComponent;

import java.util.function.Supplier;

public class UnlockChestPacket {

    public UnlockChestPacket() {}

    public static void encode(UnlockChestPacket msg, FriendlyByteBuf buf) {}

    public static UnlockChestPacket decode(FriendlyByteBuf buf) {
        return new UnlockChestPacket();
    }

    public static void handle(UnlockChestPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer p = ctx.get().getSender();
            if (p == null) return;

            if (UtilExperience.getExpTotal(p) >= ConfigHandler.EXP_COST_ECHEST.get()) {
                UtilExperience.drainExp(p, ConfigHandler.EXP_COST_ECHEST.get());

                PlayerUnlockData.setEChestUnlocked(p, true);

                NetworkHandler.sendChestSync(p, true);
                p.level.playSound(null, p.blockPosition(),
                        SoundEvents.PLAYER_LEVELUP,
                        p.getSoundSource(), 1.0F, 1.0F);

                p.closeContainer();
            } else {
                p.displayClientMessage(EComponent.translatable("gui.craftexp"), false);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}