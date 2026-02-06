package net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.DivineCloak;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.function.Supplier;
import java.util.concurrent.atomic.AtomicBoolean;

public class BlinkMessage {
    public BlinkMessage() {
    }

    public BlinkMessage(FriendlyByteBuf buf) {

    }

    public void encode(FriendlyByteBuf buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }
            AtomicBoolean hasDivineCloak = new AtomicBoolean(false);

            CuriosApi.getCuriosHelper().findFirstCurio(player, stack -> {
                if (stack.getItem() instanceof DivineCloak) {
                    if (stack.getDamageValue() == 3) {
                        hasDivineCloak.set(true);
                        return true;
                    }
                }
                return false;
            });

            if (hasDivineCloak.get()) {
                Vec3 lookVec = player.getLookAngle();
                double distance = 6.0;

                Vec3 targetPos = new Vec3(
                        player.getX() + lookVec.x * distance,
                        player.getY() + lookVec.y * distance,
                        player.getZ() + lookVec.z * distance
                );

                BlockPos blockPos = new BlockPos(targetPos);
                BlockPos blockPosUp = blockPos.above();
                boolean isSafe = !player.level.getBlockState(blockPos).isSolidRender(player.level, blockPos)
                        && !player.level.getBlockState(blockPosUp).isSolidRender(player.level, blockPosUp);

                if (isSafe) {
                    player.teleportTo(targetPos.x, targetPos.y, targetPos.z);

                    player.connection.teleport(
                            targetPos.x,
                            targetPos.y,
                            targetPos.z,
                            player.getYRot(),
                            player.getXRot()
                    );

                    player.level.playSound(
                            null,
                            targetPos.x,
                            targetPos.y,
                            targetPos.z,
                            SoundEvents.ENDERMAN_TELEPORT,
                            SoundSource.PLAYERS,
                            1.0F,
                            1.0F
                    );
                }
            }
        });

        context.setPacketHandled(true);
    }
}
