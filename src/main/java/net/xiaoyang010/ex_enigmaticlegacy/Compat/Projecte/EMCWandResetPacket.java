package net.xiaoyang010.ex_enigmaticlegacy.Compat.Projecte;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EMCWandResetPacket {

    public static final int RESET_ALL = 0;
    public static final int RESTORE_MODIFIED = 1;

    private final int resetType;

    public EMCWandResetPacket(int resetType) {
        this.resetType = resetType;
    }

    public EMCWandResetPacket(FriendlyByteBuf buf) {
        this.resetType = buf.readVarInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(resetType);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            if (!player.hasPermissions(2) && !player.isCreative()) return;

            if (resetType == RESET_ALL) {
                EMCWandHandler.resetAllEmc(player);
            } else if (resetType == RESTORE_MODIFIED) {
                EMCWandHandler.restoreModifiedEmc(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}