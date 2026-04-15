package org.xiaoyang.ex_enigmaticlegacy.Network.inputMessage;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item.DivineCloak;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class BlinkMessage {
    public BlinkMessage() {}

    public BlinkMessage(FriendlyByteBuf buf) {}

    public void encode(FriendlyByteBuf buf) {}

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            AtomicBoolean hasDivineCloak = new AtomicBoolean(false);

            CuriosApi.getCuriosInventory(player).ifPresent(inv ->
                    inv.findFirstCurio(stack -> {
                        if (stack.getItem() instanceof DivineCloak && stack.getDamageValue() == 3) {
                            hasDivineCloak.set(true);
                            return true;
                        }
                        return false;
                    })
            );

            if (!hasDivineCloak.get()) return;

            Vec3 lookVec = player.getLookAngle();
            Vec3 targetPos = new Vec3(
                    player.getX() + lookVec.x * 6.0,
                    player.getY() + lookVec.y * 6.0,
                    player.getZ() + lookVec.z * 6.0
            );

            BlockPos blockPos = BlockPos.containing(targetPos);
            BlockPos blockPosUp = blockPos.above();
            var level = player.level();

            boolean isSafe = !level.getBlockState(blockPos).isSolidRender(level, blockPos)
                    && !level.getBlockState(blockPosUp).isSolidRender(level, blockPosUp);

            if (isSafe) {
                player.teleportTo(targetPos.x, targetPos.y, targetPos.z);
                player.connection.teleport(targetPos.x, targetPos.y, targetPos.z, player.getYRot(), player.getXRot());
                level.playSound(null, targetPos.x, targetPos.y, targetPos.z,
                        SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        });

        context.setPacketHandled(true);
    }
}