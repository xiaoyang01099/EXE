package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.network.NetworkEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Config.ConfigHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Util.EComponent;

import java.util.function.Supplier;

public class UnlockStoragePacket {

    private int storageIndex;

    public UnlockStoragePacket() {}

    public UnlockStoragePacket(int index) {
        this.storageIndex = index;
    }

    public static void encode(UnlockStoragePacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.storageIndex);
    }

    public static UnlockStoragePacket decode(FriendlyByteBuf buf) {
        return new UnlockStoragePacket(buf.readInt());
    }

    public static void handle(UnlockStoragePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer p = ctx.get().getSender();
            if (p == null) return;

            int numberPastFirst = msg.storageIndex - 1;
            int expCost = ConfigHandler.EXP_COST_STORAGE_START.get() +
                    ConfigHandler.EXP_COST_STORAGE_INC.get() * numberPastFirst;

            if (UtilExperience.getExpTotal(p) >= expCost) {
                UtilExperience.drainExp(p, expCost);

                int count = PlayerUnlockData.getStorageCount(p);
                PlayerUnlockData.setStorageCount(p, count + 1);

                NetworkHandler.sendStorageSync(p, count + 1);

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