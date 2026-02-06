package net.xiaoyang010.ex_enigmaticlegacy.Network.inputPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Hud.ItemsRemainingRender;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;

import java.util.function.Supplier;

public class HornChargeHudPacket {
    private static final ItemStack HORN = new ItemStack(ModItems.HORN_PLENTY.get());
    private short chargeLoot;
    
    public HornChargeHudPacket() {
        this.chargeLoot = 0;
    }
    
    public HornChargeHudPacket(short chargeLoot) {
        this.chargeLoot = chargeLoot;
    }
    
    public static void encode(HornChargeHudPacket packet, FriendlyByteBuf buf) {
        buf.writeShort(packet.chargeLoot);
    }
    
    public static HornChargeHudPacket decode(FriendlyByteBuf buf) {
        return new HornChargeHudPacket(buf.readShort());
    }
    
    public static void handle(HornChargeHudPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ItemsRemainingRender.set(HORN, String.valueOf(packet.chargeLoot));
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
