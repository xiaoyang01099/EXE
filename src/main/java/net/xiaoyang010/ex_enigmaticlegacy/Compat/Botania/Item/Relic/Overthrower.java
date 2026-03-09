package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic;

import com.integral.enigmaticlegacy.api.items.ICursed;
import com.integral.enigmaticlegacy.helpers.ItemLoreHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model.Vector3;
import net.xiaoyang010.ex_enigmaticlegacy.Event.RelicsEventHandler;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage.BanishmentCastingMessage;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage.InfernalParticleMessage;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage.LightningBoltMessage;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage.OverthrowChatMessage;
import net.xiaoyang010.ex_enigmaticlegacy.api.INoEMCItem;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.item.IRelic;
import vazkii.botania.common.item.relic.RelicImpl;
import vazkii.botania.xplat.IXplatAbstractions;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;

public class Overthrower extends Item implements INoEMCItem, ICursed {
    static HashMap<Player, LivingEntity> targetList = new HashMap<>();

    public Overthrower(Properties properties) {
        super(properties);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @org.jetbrains.annotations.Nullable CompoundTag nbt) {
        return new RelicCapProvider(stack);
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
        ItemLoreHelper.indicateCursedOnesOnly(tooltip);
        RelicImpl.addDefaultTooltip(stack, tooltip);

        if (Screen.hasShiftDown()) {
            tooltip.add(new TranslatableComponent("item.ItemOverthrower1.lore").withStyle(ChatFormatting.GRAY));
            tooltip.add(new TranslatableComponent("item.ItemOverthrower2.lore").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.nullToEmpty(""));
            tooltip.add(new TranslatableComponent("item.ItemOverthrower3.lore").withStyle(ChatFormatting.GRAY));
            tooltip.add(new TranslatableComponent("item.ItemOverthrower4.lore").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.nullToEmpty(""));
            tooltip.add(new TranslatableComponent("item.ItemOverthrower5.lore").withStyle(ChatFormatting.GRAY));
            tooltip.add(new TranslatableComponent("item.ItemOverthrower6.lore").withStyle(ChatFormatting.GRAY));
            tooltip.add(new TranslatableComponent("item.ItemOverthrower7.lore").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 150;
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

    public boolean overthrow(LivingEntity entity, Player overthrower) {
        ItemStack stack = overthrower.getMainHandItem();
        if (stack.getItem() instanceof Overthrower) {
            var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
            if (relicCap.isPresent()) {
                IRelic relic = relicCap.orElse(null);
                if (relic != null && !relic.isRightPlayer(overthrower)) {
                    return false;
                }
            }
        }

        int x = (int)((Math.random() - 0.5) * 20002.0);
        int z = (int)((Math.random() - 0.5) * 20002.0);
        int y = 124;

        ServerLevel netherLevel = overthrower.getServer().getLevel(Level.NETHER);
        if (netherLevel == null) return false;

        ChunkPos chunkPos = new ChunkPos(x >> 4, z >> 4);
        netherLevel.getChunkSource().addRegionTicket(
                TicketType.UNKNOWN,
                chunkPos,
                1,
                chunkPos
        );

        for (int counter = 124; counter > 0; --counter) {
            boolean valid = RelicsEventHandler.validatePosition(netherLevel, x, counter, z);
            if (valid) {
                y = counter;
                break;
            }
        }

        if (y == 124) {
            return false;
        } else if (entity instanceof ServerPlayer serverPlayer) {
            serverPlayer.teleportTo(netherLevel, x, y, z, entity.getYRot(), entity.getXRot());

            NetworkHandler.CHANNEL.send(PacketDistributor.ALL.noArg(),
                    new OverthrowChatMessage(serverPlayer.getDisplayName().getString(),
                            overthrower.getDisplayName().getString(), 0));

            ExEnigmaticlegacyMod.LOGGER.info(overthrower.getDisplayName().getString() + " has overthrown " +
                    serverPlayer.getDisplayName().getString() + " into the Nether.");
            return true;
        } else {
            CompoundTag nbt = new CompoundTag();
            entity.saveWithoutId(nbt);

            try {
                int finalY = y;
                Entity newEntity = EntityType.loadEntityRecursive(nbt, netherLevel, (ent) -> {
                    ent.moveTo(x, finalY, z, ent.getYRot(), ent.getXRot());
                    return ent;
                });

                if (newEntity != null) {
                    netherLevel.addFreshEntity(newEntity);
                }
            } catch (Exception e) {
                ExEnigmaticlegacyMod.LOGGER.error("Failed to teleport entity to nether", e);
            }

            entity.discard();

            for (int a = 0; a < 12; ++a) {
                int xx = (int)Math.floor(entity.getX()) + entity.getRandom().nextInt(4) - entity.getRandom().nextInt(4);
                int yy = (int)Math.floor(entity.getY()) + 4;
                int zz = (int)Math.floor(entity.getZ()) + entity.getRandom().nextInt(4) - entity.getRandom().nextInt(4);

                while (entity.level.isEmptyBlock(new BlockPos(xx, yy, zz)) && yy > Math.floor(entity.getY()) - 4) {
                    --yy;
                }

                BlockPos firePos = new BlockPos(xx, yy + 1, zz);
                if (entity.level.isEmptyBlock(firePos) &&
                        !entity.level.isEmptyBlock(new BlockPos(xx, yy, zz)) &&
                        entity.level.getBlockState(firePos).getBlock() != Blocks.FIRE &&
                        RelicsEventHandler.canEntityBeSeen(entity, xx + 0.5, yy + 1.5, zz + 0.5)) {

                    entity.level.setBlock(firePos, Blocks.FIRE.defaultBlockState(), 3);
                }
            }

            return true;
        }
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!(livingEntity instanceof Player player) || level.isClientSide) return;

        var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
        if (relicCap.isPresent()) {
            IRelic relic = relicCap.orElse(null);
            if (relic != null && !relic.isRightPlayer(player)) {
                player.stopUsingItem();
                return;
            }
        }

        if (targetList.containsKey(player)) {
            LivingEntity target = targetList.get(player);
            if (target == null) {
                targetList.put(player, null);
                player.stopUsingItem();
                return;
            }

            if (target.isAlive()) {
                if (!level.isClientSide && remainingUseDuration % 10 == 0 && remainingUseDuration != stack.getUseDuration()) {
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 0.33F, 2.0F);
                    level.playSound(null, target.getX(), target.getY(), target.getZ(),
                            SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 0.33F, 2.0F);
                }

                Vector3 thisPos = Vector3.fromEntityCenter(target);
                if (!level.isClientSide) {
                    NetworkHandler.CHANNEL.send(
                            PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(
                                    target.getX(), target.getY(), target.getZ(), 64.0, level.dimension())),
                            new BanishmentCastingMessage(thisPos.x, thisPos.y, thisPos.z, 5)
                    );
                }

                try {
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 2));
                } catch (Exception ignored) {
                }

                if (remainingUseDuration == 1) {
                    if (!target.level.dimension().equals(Level.NETHER) && !level.isClientSide) {
                        boolean gotEffect = false;

                        for (int counter = 8; counter >= 0; --counter) {
                            if (this.overthrow(target, player)) {
                                gotEffect = true;
                                break;
                            }
                        }

                        if (!gotEffect && !(target instanceof ServerPlayer)) {
                            target.teleportTo(0.0, 0.0, 0.0);
                            target.discard();
                        }
                    } else if (level.isClientSide && !target.level.dimension().equals(Level.NETHER)) {
                        target.teleportTo(0.0, 0.0, 0.0);
                        target.discard();
                    }

                    if (!level.isClientSide) {
                        for (int counter = 3; counter > 0; --counter) {
                            LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
                            if (lightning != null) {
                                lightning.moveTo(thisPos.x - 0.5, thisPos.y - (target.getBbHeight() / 2.0), thisPos.z - 0.5);
                                level.addFreshEntity(lightning);
                            }
                        }

                        NetworkHandler.CHANNEL.send(
                                PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(
                                        player.getX(), player.getY(), player.getZ(), 128.0, level.dimension())),
                                new LightningBoltMessage(thisPos.x - 0.5, thisPos.y - (target.getBbHeight() / 2.0), thisPos.z - 0.5, 3)
                        );

                        NetworkHandler.CHANNEL.send(
                                PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(
                                        player.getX(), player.getY(), player.getZ(), 128.0, level.dimension())),
                                new InfernalParticleMessage(thisPos.x, thisPos.y, thisPos.z, 128)
                        );
                    }
                }
            } else {
                targetList.put(player, null);
                player.stopUsingItem();
            }
        } else {
            player.stopUsingItem();
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

        if (level.dimension().equals(Level.NETHER)) {
            return InteractionResultHolder.pass(stack);
        }

        Entity pointedEntity = RelicsEventHandler.getPointedEntity(level, player, 0.0, 64.0, 3.0F);
        if (pointedEntity instanceof LivingEntity livingEntity) {
            targetList.put(player, livingEntity);
            player.startUsingItem(hand);
        } else {
            targetList.put(player, null);
        }

        return InteractionResultHolder.consume(stack);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }
}