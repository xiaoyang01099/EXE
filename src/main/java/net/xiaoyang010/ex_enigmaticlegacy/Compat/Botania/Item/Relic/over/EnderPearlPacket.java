package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EnderPearlPacket {

    public EnderPearlPacket() {}

    public static void encode(EnderPearlPacket msg, FriendlyByteBuf buf) {}

    public static EnderPearlPacket decode(FriendlyByteBuf buf) {
        return new EnderPearlPacket();
    }

    public static void handle(EnderPearlPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            InventoryOverpowered inventory = PlayerInventoryData.getInventory(player);
            ItemStack pearls = inventory.getItem(Const.SLOT_EPEARL);

            if (!pearls.isEmpty()) {
                ThrownEnderpearl pearl = new ThrownEnderpearl(player.level, player);
                pearl.shootFromRotation(player, player.getXRot(), player.getYRot(),
                        0.0F, 1.5F, 1.0F);
                player.level.addFreshEntity(pearl);

                player.level.playSound(null, player.blockPosition(),
                        SoundEvents.ENDER_PEARL_THROW, player.getSoundSource(),
                        0.5F, 0.4F / (player.getRandom().nextFloat() * 0.4F + 0.8F));

                if (!player.isCreative()) {
                    inventory.removeItem(Const.SLOT_EPEARL, 1);
                    PlayerInventoryData.saveInventory(player, inventory);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}