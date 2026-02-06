package net.xiaoyang010.ex_enigmaticlegacy.Network.inputPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosApi;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.ShinyStone;

import java.util.function.Supplier;

public class ShinyStoneTogglePacket {

    public ShinyStoneTogglePacket() {
    }

    public static ShinyStoneTogglePacket decode(FriendlyByteBuf buf) {
        return new ShinyStoneTogglePacket();
    }

    public void toBytes(FriendlyByteBuf buf) {
    }

    public static void handle(ShinyStoneTogglePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            CuriosApi.getCuriosHelper().getEquippedCurios(player).ifPresent(handler -> {
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack stack = handler.getStackInSlot(i);
                    if (stack.getItem() instanceof ShinyStone shinyStone) {
                        shinyStone.togglePermanentDay(stack, player);
                        return;
                    }
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
