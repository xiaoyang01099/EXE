package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PacketDistributor;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model.Vector3;
import net.xiaoyang010.ex_enigmaticlegacy.Event.RelicsEventHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputPacket.PortalTraceMessage;
import net.xiaoyang010.ex_enigmaticlegacy.Util.EComponent;

import javax.annotation.Nullable;
import java.util.List;

public class TeleportationTome extends Item {

    public TeleportationTome(Properties tab) {
        super(tab);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(EComponent.translatable("item.ex_enigmaticlegacy.teleportation_tome.lore1"));
            tooltip.add(EComponent.translatable("item.ex_enigmaticlegacy.teleportation_tome.lore2"));
            tooltip.add(EComponent.translatable("item.ex_enigmaticlegacy.teleportation_tome.lore3"));
            tooltip.add(EComponent.literal(""));
            tooltip.add(EComponent.translatable("item.ex_enigmaticlegacy.teleportation_tome.lore4"));
            tooltip.add(EComponent.translatable("item.ex_enigmaticlegacy.teleportation_tome.lore5"));
            tooltip.add(EComponent.translatable("item.ex_enigmaticlegacy.teleportation_tome.lore6"));
        } else {
            tooltip.add(EComponent.translatable("item.ex_enigmaticlegacy.shift_tooltip").withStyle(ChatFormatting.GRAY));
        }

        tooltip.add(EComponent.literal(""));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // 检查冷却
        if (RelicsEventHandler.isOnCoodown(player) || level.isClientSide) {
            return InteractionResultHolder.pass(stack);
        }

        Entity pointedEntity = RelicsEventHandler.getPointedEntity(level, player, 0.0D, 128.0D, 4F);

        // Shift + 右键：向前传送16格
        if (player.isShiftKeyDown()) {
            Vector3 primalVec = Vector3.fromEntityCenter(player);
            primalVec.y -= 0.5;

            Vector3 lookVec = primalVec.copy().add(new Vector3(player.getLookAngle()).multiply(16.0F));

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);

            if (!level.isClientSide) {
                player.teleportTo(lookVec.x, lookVec.y, lookVec.z);
                RelicsEventHandler.imposeBurst(level, player.getX(), player.getY() + 1, player.getZ(), 1.25f);
            }

            level.playSound(null, lookVec.x, lookVec.y, lookVec.z,
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);

            Vector3 finalVec = Vector3.fromEntityCenter(player);
            finalVec.y -= 0.5;

            if (!level.isClientSide) {
                sendPortalTracePacket(level, primalVec, finalVec, player);
            }

            RelicsEventHandler.setCasted(player, 20, false);
            return InteractionResultHolder.success(stack);

        }
        // 指向实体：交换位置
        else if (pointedEntity != null && pointedEntity instanceof LivingEntity) {
            Vector3 primalVec = Vector3.fromEntityCenter(player);
            Vector3 finalVec = Vector3.fromEntityCenter(pointedEntity);

            if (!level.isClientSide) {
                RelicsEventHandler.imposeBurst(level, player.getX(), player.getY() + 1, player.getZ(), 1.25f);
                player.teleportTo(finalVec.x, finalVec.y, finalVec.z);
            }

            pointedEntity.teleportTo(primalVec.x, primalVec.y, primalVec.z);

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
            level.playSound(null, pointedEntity.getX(), pointedEntity.getY(), pointedEntity.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);

            if (!level.isClientSide) {
                sendPortalTracePacket(level, primalVec, finalVec, player);
            }

            RelicsEventHandler.setCasted(player, 20, false);
            return InteractionResultHolder.success(stack);

        }
        // 指向方块：传送到方块位置
        else {
            BlockHitResult pointed = RelicsEventHandler.getPointedBlock(player, level, 128.0F);

            if (pointed != null && pointed.getType() != BlockHitResult.Type.MISS) {
                BlockPos pos = pointed.getBlockPos();
                int x = pos.getX();
                int y = pos.getY();
                int z = pos.getZ();

                for (int counter = 0; counter <= 32; counter++) {
                    BlockPos belowPos = new BlockPos(x, y + counter - 1, z);
                    BlockPos currentPos = new BlockPos(x, y + counter, z);
                    BlockPos abovePos = new BlockPos(x, y + counter + 1, z);

                    BlockState belowState = level.getBlockState(belowPos);
                    BlockState currentState = level.getBlockState(currentPos);
                    BlockState aboveState = level.getBlockState(abovePos);

                    if (!belowState.isAir() &&
                            !belowState.getCollisionShape(level, belowPos).isEmpty() &&
                            currentState.isAir() &&
                            aboveState.isAir()) {

                        if (!level.isClientSide) {
                            RelicsEventHandler.imposeBurst(level, player.getX(), player.getY() + 1, player.getZ(), 1.25f);
                        }

                        Vector3 primalVec = Vector3.fromEntityCenter(player);

                        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);

                        if (!level.isClientSide) {
                            player.teleportTo(x + 0.5, y + counter, z + 0.5);
                        }

                        level.playSound(null, x, y + counter, z,
                                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);

                        Vector3 finalVec = Vector3.fromEntityCenter(player);

                        if (!level.isClientSide) {
                            sendPortalTracePacket(level, primalVec, finalVec, player);
                        }

                        RelicsEventHandler.setCasted(player, 20, false);
                        return InteractionResultHolder.success(stack);
                    }
                }
            }
        }

        return InteractionResultHolder.pass(stack);
    }

    private void sendPortalTracePacket(Level level, Vector3 primalVec, Vector3 finalVec, Player player) {
        double distance = Math.sqrt(
                Math.pow(finalVec.x - primalVec.x, 2) +
                        Math.pow(finalVec.y - primalVec.y, 2) +
                        Math.pow(finalVec.z - primalVec.z, 2)
        );

        NetworkHandler.CHANNEL.send(
                PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(
                        player.getX(), player.getY(), player.getZ(), 128.0D, level.dimension())),
                new PortalTraceMessage(primalVec.x, primalVec.y, primalVec.z,
                        finalVec.x, finalVec.y, finalVec.z, distance)
        );
    }
}
