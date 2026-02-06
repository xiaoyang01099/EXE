package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncSlotPacket {
    private int slot;
    private ItemStack stack;

    public SyncSlotPacket() {}

    public SyncSlotPacket(int slot, ItemStack stack) {
        this.slot = slot;
        this.stack = stack.copy();
    }

    public static void encode(SyncSlotPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.slot);
        buf.writeItem(msg.stack);
    }

    public static SyncSlotPacket decode(FriendlyByteBuf buf) {
        return new SyncSlotPacket(buf.readInt(), buf.readItem());
    }

    public static void handle(SyncSlotPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var player = Minecraft.getInstance().player;
            if (player == null) return;

            var inventory = PlayerInventoryData.getInventory(player);
            ItemStack stackCopy = msg.stack.copy();

            if (msg.slot == Const.SLOT_EPEARL) {
                inventory.enderPearlStack = stackCopy;
            } else if (msg.slot == Const.SLOT_ECHEST) {
                inventory.enderChestStack = stackCopy;
            } else if (msg.slot >= 0 && msg.slot < inventory.getContainerSize()) {
                inventory.inventory.set(msg.slot, stackCopy);
            }

            if (player.containerMenu instanceof ContainerOverpowered container) {
                container.broadcastChanges();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}