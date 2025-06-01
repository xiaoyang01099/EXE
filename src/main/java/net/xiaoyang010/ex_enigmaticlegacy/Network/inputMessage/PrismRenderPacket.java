package net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;
import net.xiaoyang010.ex_enigmaticlegacy.Tile.PolychromeCollapsePrismTile;

import java.util.function.Supplier;

public class PrismRenderPacket {
    private static BlockPos pos;
    private static NonNullList<ItemStack> stacks;
    public PrismRenderPacket(FriendlyByteBuf buffer) {
        pos = buffer.readBlockPos();
        for (int i = 0; i < 5; i++){
            stacks.set(i, buffer.readItem());
        }
    }

    public PrismRenderPacket(BlockPos pos, NonNullList<ItemStack> stack) {
        PrismRenderPacket.pos = pos;
        PrismRenderPacket.stacks = stack;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        for (int i = 0; i < 5; i++){
            buf.writeItem(stacks.get(i));
        }
    }

    public static void handler(PrismRenderPacket msg, Supplier<Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(msg, ctx)); //处理服务端发送给客户端的消息
        });
        ctx.get().setPacketHandled(true);
    }

    public BlockPos getPos() {
        return pos;
    }

    public NonNullList<ItemStack> getStack() {
        return stacks;
    }

    //客户端控制参与合成物品显示
    public static void handlePacket(PrismRenderPacket msg, Supplier<Context> ctx) {
        ctx.get().enqueueWork(() ->{
            ClientLevel world = Minecraft.getInstance().level;
            if (world != null){
                BlockEntity tileEntity = world.getBlockEntity(msg.getPos());
                if (tileEntity instanceof PolychromeCollapsePrismTile prismTile && !msg.getStack().isEmpty()){
                    for (int i = 0; i < 5; i++){
                        prismTile.setItem(i, msg.getStack().get(i));
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
