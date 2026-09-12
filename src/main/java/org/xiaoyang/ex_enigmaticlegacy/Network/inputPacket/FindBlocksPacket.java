package org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item.SphereNavigation;

import java.util.function.Supplier;

public class FindBlocksPacket {
    private final String blockId;
    private final int meta;

    public FindBlocksPacket(Block block, int meta) {
        this.blockId = BuiltInRegistries.BLOCK.getKey(block).toString();
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
        if (supplier.get().getDirection().getReceptionSide().isClient()) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> () -> handleClient(packet, supplier));
        }
        supplier.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(FindBlocksPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                ResourceLocation id = ResourceLocation.tryParse(packet.blockId);
                if (id == null || !BuiltInRegistries.BLOCK.containsKey(id)) return;
                Block block = BuiltInRegistries.BLOCK.get(id);
                if (block != Blocks.AIR) {
                    SphereNavigation.findBlocks(player.level(), block, packet.meta, player);
                }
            }
        });
        context.setPacketHandled(true);
    }
}
