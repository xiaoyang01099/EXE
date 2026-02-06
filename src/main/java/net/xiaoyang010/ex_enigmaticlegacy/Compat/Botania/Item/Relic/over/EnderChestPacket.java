package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import net.xiaoyang010.ex_enigmaticlegacy.Util.EComponent;

import java.util.function.Supplier;

public class EnderChestPacket {

    public EnderChestPacket() {}

    public static void encode(EnderChestPacket msg, FriendlyByteBuf buf) {}

    public static EnderChestPacket decode(FriendlyByteBuf buf) {
        return new EnderChestPacket();
    }

    public static void handle(EnderChestPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            InventoryOverpowered inventory = PlayerInventoryData.getInventory(player);
            ItemStack chest = inventory.getItem(Const.SLOT_ECHEST);

            if (!chest.isEmpty()) {
                NetworkHooks.openGui(player,
                        new SimpleMenuProvider(
                                (id, inv, p) -> new EnderChestContainerWrapper(id, inv, player.getEnderChestInventory()),
                                EComponent.translatable("container.enderchest")
                        )
                );
            } else {
                player.displayClientMessage(
                        EComponent.translatable("message.powerinventory.no_enderchest"),
                        true
                );
            }
        });
        ctx.get().setPacketHandled(true);
    }
}