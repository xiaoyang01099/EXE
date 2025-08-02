package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.NetworkDirection;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model.Vector3;
import net.xiaoyang010.ex_enigmaticlegacy.Event.RelicsEventHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage.EntityMotionMessage;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage.OverthrowChatMessage;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage.PacketVoidMessage;
import net.xiaoyang010.ex_enigmaticlegacy.api.IFE.INoEMCItem;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.item.IRelic;
import vazkii.botania.common.item.relic.RelicImpl;
import vazkii.botania.xplat.IXplatAbstractions;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;

public class VoidGrimoire extends Item implements INoEMCItem {
    public static final int USAGE_DURATION = 100; // 使用持续时间（ticks）
    public static final int COOLDOWN_DURATION = 30; // 冷却时间（ticks）
    public static final int LOCAL_COOLDOWN_DURATION = 60; // 客户端冷却时间（ticks）
    public static final double TELEPORT_RANGE = 64.0; // 目标选择范围
    public static final float TARGET_SELECTION_WIDTH = 3.0F; // 目标选择宽度

    private static final double VOID_TELEPORT_RANGE = 20002.0;
    private static final double VOID_Y_BASE = -100000.0;

    public static int localCooldown = 0;
    static HashMap<Player, LivingEntity> targetList = new HashMap<>();

    public VoidGrimoire(Properties properties) {
        super(properties);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @org.jetbrains.annotations.Nullable CompoundTag nbt) {
        return new VoidGrimoire.RelicCapProvider(stack);
    }

    private static class RelicCapProvider implements ICapabilityProvider {
        private final LazyOptional<IRelic> relic;

        public RelicCapProvider(ItemStack stack) {
            this.relic = LazyOptional.of(() -> new RelicImpl(stack, null));
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @org.jetbrains.annotations.Nullable Direction direction) {
            if (capability == BotaniaForgeCapabilities.RELIC) {
                return relic.cast();
            }
            return LazyOptional.empty();
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        RelicImpl.addDefaultTooltip(stack, tooltip);

        if (Screen.hasShiftDown()) {
            tooltip.add(new TranslatableComponent("item.ItemVoidGrimoire1.lore").withStyle(ChatFormatting.GRAY));
            tooltip.add(new TranslatableComponent("item.ItemVoidGrimoire2.lore").withStyle(ChatFormatting.GRAY));
            tooltip.add(new TranslatableComponent("item.ItemVoidGrimoire3.lore").withStyle(ChatFormatting.GRAY));
            tooltip.add(new TextComponent(""));
            tooltip.add(new TranslatableComponent("item.ItemVoidGrimoire4.lore").withStyle(ChatFormatting.GRAY));
            tooltip.add(new TranslatableComponent("item.ItemVoidGrimoire5.lore").withStyle(ChatFormatting.GRAY));
            tooltip.add(new TextComponent(""));
            tooltip.add(new TranslatableComponent("item.ItemVoidGrimoire6.lore").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(new TranslatableComponent("item.FRShiftTooltip.lore").withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(new TextComponent(""));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide && entity instanceof Player player) {
            var relic = IXplatAbstractions.INSTANCE.findRelic(stack);
            if (relic != null) {
                relic.tickBinding(player);
            }
        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return USAGE_DURATION;
    }

    public void overthrow(LivingEntity entity, Player overthrower) {
        if (overthrower.level.isClientSide) {
            entity.discard();
        } else {
            double x = (Math.random() - 0.5) * VOID_TELEPORT_RANGE;
            double z = (Math.random() - 0.5) * VOID_TELEPORT_RANGE;
            double y = VOID_Y_BASE + (Math.random() - 0.5) * VOID_TELEPORT_RANGE;

            entity.teleportTo(x, y, z);

            if (!(entity instanceof Player)) {
                entity.discard();
            } else if (!overthrower.level.isClientSide) {
                NetworkHandler.sendOverthrowMessage(new OverthrowChatMessage(
                        entity.getDisplayName().getString(),
                        overthrower.getDisplayName().getString(),
                        1
                ));
            }
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (level.isClientSide && timeLeft != 1) {
            Minecraft.getInstance().getSoundManager().stop();
        }
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (!(entity instanceof Player player)) return;

        var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
        if (relicCap.isPresent()) {
            IRelic relic = relicCap.orElse(null);
            if (relic != null && !relic.isRightPlayer(player)) {
                player.stopUsingItem();
                return;
            }
        }

        if (!targetList.containsKey(player)) {
            targetList.put(player, null);
            player.stopUsingItem();
            return;
        }

        LivingEntity target = targetList.get(player);
        if (target != null) {
            target.fallDistance = 0.0F;

            target.setDeltaMovement(target.getDeltaMovement().x, 0.03, target.getDeltaMovement().z);
            target.hasImpulse = true;

            if (!player.level.isClientSide) {
                List<ServerPlayer> nearbyPlayers = level.getEntitiesOfClass(ServerPlayer.class,
                        new AABB(target.blockPosition()).inflate(64.0));

                EntityMotionMessage motionMessage = new EntityMotionMessage(
                        target.getId(),
                        target.getDeltaMovement().x,
                        0.03,
                        target.getDeltaMovement().z,
                        true
                );

                for (ServerPlayer nearbyPlayer : nearbyPlayers) {
                    NetworkHandler.CHANNEL.sendTo(motionMessage,
                            nearbyPlayer.connection.connection,
                            NetworkDirection.PLAY_TO_CLIENT);
                }
            }

            Vector3 thisPos = Vector3.fromEntityCenter(target);
            thisPos.y += 0.03;

            if (!player.level.isClientSide && remainingUseDuration == getUseDuration(stack)) {
                level.playSound(null, thisPos.x, thisPos.y, thisPos.z,
                        SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 4.0F, 0.75F);
            }

            if (!player.level.isClientSide) {
                List<ServerPlayer> nearbyPlayers = level.getEntitiesOfClass(ServerPlayer.class,
                        new AABB(new BlockPos(thisPos.x, thisPos.y, thisPos.z)).inflate(64.0));

                PacketVoidMessage voidMessage = new PacketVoidMessage(thisPos.x, thisPos.y, thisPos.z, false);

                for (ServerPlayer nearbyPlayer : nearbyPlayers) {
                    NetworkHandler.CHANNEL.sendTo(voidMessage,
                            nearbyPlayer.connection.connection,
                            NetworkDirection.PLAY_TO_CLIENT);
                }
            }

            if (remainingUseDuration == 1) {
                if (!player.level.isClientSide) {
                    List<ServerPlayer> nearbyPlayers = level.getEntitiesOfClass(ServerPlayer.class,
                            new AABB(new BlockPos(thisPos.x, thisPos.y, thisPos.z)).inflate(128.0));

                    PacketVoidMessage finalVoidMessage = new PacketVoidMessage(thisPos.x, thisPos.y, thisPos.z, true);

                    for (ServerPlayer nearbyPlayer : nearbyPlayers) {
                        NetworkHandler.CHANNEL.sendTo(finalVoidMessage,
                                nearbyPlayer.connection.connection,
                                NetworkDirection.PLAY_TO_CLIENT);
                    }
                }

                target.level.playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.ENDERMAN_DEATH, SoundSource.HOSTILE, 4.0F,
                        0.8F + level.random.nextFloat() * 0.2F);

                this.overthrow(target, player);

                RelicsEventHandler.setCasted(player, COOLDOWN_DURATION, false);
                if (player.level.isClientSide) {
                    localCooldown = LOCAL_COOLDOWN_DURATION;
                }
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
        if (relicCap.isPresent()) {
            IRelic relic = relicCap.orElse(null);
            if (relic != null && !relic.isRightPlayer(player)) {
                return InteractionResultHolder.fail(stack);
            }
        }

        if (!level.isClientSide && RelicsEventHandler.isOnCoodown(player)) {
            return InteractionResultHolder.fail(stack);
        }
        if (level.isClientSide && localCooldown != 0) {
            return InteractionResultHolder.fail(stack);
        }

        Entity pointedEntity = getPointedEntity(level, player, TELEPORT_RANGE, TARGET_SELECTION_WIDTH);

        if (pointedEntity instanceof LivingEntity livingEntity) {
            targetList.put(player, livingEntity);
            player.startUsingItem(hand);
        } else {
            targetList.put(player, null);
        }

        return InteractionResultHolder.consume(stack);
    }


    private Entity getPointedEntity(Level level, Player player, double range, float width) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = start.add(look.scale(range));

        AABB searchArea = new AABB(start, end).inflate(width);

        Entity closestEntity = null;
        double closestDistance = range;

        for (Entity entity : level.getEntitiesOfClass(Entity.class, searchArea)) {
            if (entity == player) continue;

            AABB entityBox = entity.getBoundingBox().inflate(0.3F);

            if (entityBox.clip(start, end).isPresent()) {
                double distance = start.distanceTo(entity.position());
                if (distance < closestDistance) {
                    closestEntity = entity;
                    closestDistance = distance;
                }
            }
        }

        return closestEntity;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    public static void clientTick() {
        if (localCooldown > 0) {
            localCooldown--;
        }
    }
}