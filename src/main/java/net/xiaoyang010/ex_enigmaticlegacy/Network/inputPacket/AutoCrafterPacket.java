package net.xiaoyang010.ex_enigmaticlegacy.Network.inputPacket;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent.Context;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.TileEntityExtremeAutoCrafter;

import java.util.function.Supplier;

public class AutoCrafterPacket {
    private final int slot;
    private final BlockPos pos;
    private final ItemStack stack;
    public AutoCrafterPacket(FriendlyByteBuf buffer) {
        Minecraft instance = Minecraft.getInstance();
        slot = buffer.readInt();
        pos = buffer.readBlockPos();
        stack = buffer.readItem();
    }

    public AutoCrafterPacket(int slot, BlockPos pos, ItemStack stack) {
        this.slot = slot;
        this.pos = pos;
        this.stack = stack;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(slot);
        buf.writeBlockPos(pos);
        buf.writeItem(stack);
    }

    public static void handler(AutoCrafterPacket msg, Supplier<Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            ServerLevel level = player.getLevel();
            BlockEntity block = level.getBlockEntity(msg.pos);
            if (block instanceof TileEntityExtremeAutoCrafter autoCrafter){
                autoCrafter.setItem(msg.slot, msg.stack);
                autoCrafter.setChanged();
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
