package net.xiaoyang010.ex_enigmaticlegacy.Network.inputPacket;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.network.NetworkEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.SphereNavigation;

import java.util.function.Supplier;

public class FindBlocksPacket {
    private final String blockId;
    private final int meta;

    public FindBlocksPacket(Block block, int meta) {
        this.blockId = Registry.BLOCK.getKey(block).toString();
        this.meta = meta;
    }

    public FindBlocksPacket(FriendlyByteBuf buf) {
        this.blockId = buf.readUtf();
        this.meta = buf.readInt();
    }

    public static void encode(FindBlocksPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.blockId);
        buf.writeInt(packet.meta);
    }

    public static FindBlocksPacket decode(FriendlyByteBuf buf) {
        return new FindBlocksPacket(buf);
    }

    public static void handle(FindBlocksPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                try {
                    Block block = Registry.BLOCK.get(new ResourceLocation(packet.blockId));
                    if (block != null) {
                        SphereNavigation.findBlocks(player.level, block, packet.meta, player);
                    }
                } catch (Exception e) {
                }
            }
        });
        context.setPacketHandled(true);
    }
}