package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModDamageSources;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;
import net.xiaoyang010.ex_enigmaticlegacy.api.INoEMCItem;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.item.IRelic;
import vazkii.botania.common.helper.ItemNBTHelper;
import vazkii.botania.common.item.relic.RelicImpl;
import vazkii.botania.xplat.IXplatAbstractions;

import javax.annotation.Nullable;
import java.util.List;

public class DormantArcanum extends Item implements ICurioItem, INoEMCItem {
    private static final int RECHARGE_INTERVAL = 20; // 每秒减少1点生命值
    private static final double VOID_BANISH_RADIUS = 72.0; // 虚空放逐半径

    public DormantArcanum(Properties properties) {
        super(properties);
        MinecraftForge.EVENT_BUS.register(this);
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
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide && entity instanceof Player player) {
            var relic = IXplatAbstractions.INSTANCE.findRelic(stack);
            if (relic != null) {
                relic.tickBinding(player);
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        RelicImpl.addDefaultTooltip(itemStack, tooltip);

        if (Screen.hasShiftDown()) {
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.dormant_arcanum.lore1")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.dormant_arcanum.lore2")
                    .withStyle(ChatFormatting.DARK_PURPLE));
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.dormant_arcanum.lore3")
                    .withStyle(ChatFormatting.RED));
            tooltip.add(new TranslatableComponent(""));
        } else {
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.shift_tooltip")
                    .withStyle(ChatFormatting.GRAY));
        }

        if (itemStack.hasTag()) {
            int lifetime = ItemNBTHelper.getInt(itemStack, "ILifetime", 0);
            double seconds = (double)(lifetime * 2) / 100.0;
            tooltip.add(new TranslatableComponent(""));
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.dormant_arcanum.time")
                    .append(new TranslatableComponent(": " + String.format("%.1f", seconds) + "s"))
                    .withStyle(ChatFormatting.AQUA));
        }
    }

    @Override
    public Rarity getRarity(ItemStack itemStack) {
        return Rarity.EPIC;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (slotContext.entity() instanceof Player player) {
            var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
            if (relicCap.isPresent()) {
                IRelic relic = relicCap.orElse(null);
                if (relic != null && !relic.isRightPlayer(player)) {
                    return;
                }
            }

            if (!player.level.isClientSide) {
                if (stack.hasTag()) {
                    int lifetime = ItemNBTHelper.getInt(stack, "ILifetime", 0);

                    if (lifetime > 0) {
                        if (player.tickCount % RECHARGE_INTERVAL == 0) {
                            ItemNBTHelper.setInt(stack, "ILifetime", lifetime - 1);

                            if (player.level instanceof ServerLevel serverLevel) {
                                serverLevel.sendParticles(
                                        net.minecraft.core.particles.ParticleTypes.ENCHANT,
                                        player.getX(), player.getY() + 1.0, player.getZ(),
                                        3, 0.3, 0.3, 0.3, 0.02
                                );
                            }
                        }
                    } else {
                        awakeParcanum(player, stack, slotContext);
                    }
                }
            }
        }
    }

    private void awakeParcanum(Player player, ItemStack stack, SlotContext slotContext) {
        ItemStack awakenedStack = new ItemStack(ModItems.NEBULOUS_CORE.get());

        CuriosApi.getCuriosHelper().getCuriosHandler(player).ifPresent(handler -> {
            handler.getStacksHandler(slotContext.identifier()).ifPresent(stacksHandler -> {
                stacksHandler.getStacks().setStackInSlot(slotContext.index(), awakenedStack);
            });
        });

        player.level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_LEVELUP,
                SoundSource.PLAYERS, 0.7F, 1.2F);

        if (player.level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.TOTEM_OF_UNDYING,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    20, 0.5, 0.5, 0.5, 0.1
            );
        }

        player.sendMessage(new TranslatableComponent("item.ex_enigmaticlegacy.dormant_arcanum.awakened")
                .withStyle(ChatFormatting.GOLD), player.getUUID());
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        if (slotContext.entity() instanceof Player player) {
            var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
            if (relicCap.isPresent()) {
                IRelic relic = relicCap.orElse(null);
                if (relic != null && !relic.isRightPlayer(player)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        if (slotContext.entity() instanceof Player player) {
            var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
            if (relicCap.isPresent()) {
                IRelic relic = relicCap.orElse(null);
                if (relic != null && !relic.isRightPlayer(player)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        if (slotContext.entity() instanceof Player player) {
            var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
            if (relicCap.isPresent()) {
                IRelic relic = relicCap.orElse(null);
                if (relic != null && !relic.isRightPlayer(player)) {
                    return;
                }
            }

            int lifetime = ItemNBTHelper.getInt(stack, "ILifetime", 0);
            player.sendMessage(new TranslatableComponent("item.ex_enigmaticlegacy.dormant_arcanum.equipped")
                    .append(new TranslatableComponent(" (" + (lifetime / 10) + "s remaining)"))
                    .withStyle(ChatFormatting.GRAY), player.getUUID());
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        if (slotContext.entity() instanceof Player player) {
            player.sendMessage(new TranslatableComponent("item.ex_enigmaticlegacy.dormant_arcanum.unequipped")
                    .withStyle(ChatFormatting.GRAY), player.getUUID());
        }
    }

    public static ItemStack createWithLifetime(int lifetime) {
        ItemStack stack = new ItemStack(ModItems.DORMANT_ARCANUM.get());
        ItemNBTHelper.setInt(stack, "ILifetime", lifetime);
        return stack;
    }

    private static void voidBanishment(Player dyingPlayer) {
        if (dyingPlayer.level.isClientSide) return;

        ServerLevel level = (ServerLevel) dyingPlayer.level;
        AABB searchArea = new AABB(
                dyingPlayer.getX() - VOID_BANISH_RADIUS,
                dyingPlayer.getY() - VOID_BANISH_RADIUS,
                dyingPlayer.getZ() - VOID_BANISH_RADIUS,
                dyingPlayer.getX() + VOID_BANISH_RADIUS,
                dyingPlayer.getY() + VOID_BANISH_RADIUS,
                dyingPlayer.getZ() + VOID_BANISH_RADIUS
        );

        List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(LivingEntity.class, searchArea);
        nearbyEntities.remove(dyingPlayer);

        if (!nearbyEntities.isEmpty()) {
            level.playSound(null, dyingPlayer.getX(), dyingPlayer.getY(), dyingPlayer.getZ(),
                    SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 2.0F, 0.5F);

            for (int i = 0; i < 100; i++) {
                double angle = Math.random() * Math.PI * 2;
                double radius = Math.random() * VOID_BANISH_RADIUS;
                double x = dyingPlayer.getX() + Math.cos(angle) * radius;
                double z = dyingPlayer.getZ() + Math.sin(angle) * radius;
                double y = dyingPlayer.getY() + (Math.random() - 0.5) * 10;

                level.sendParticles(
                        net.minecraft.core.particles.ParticleTypes.PORTAL,
                        x, y, z, 1,
                        0, -0.5, 0, 0.1
                );

                level.sendParticles(
                        net.minecraft.core.particles.ParticleTypes.SMOKE,
                        x, y, z, 1,
                        0, 0.2, 0, 0.05
                );
            }

            for (LivingEntity entity : nearbyEntities) {
                if (entity instanceof Player) {
                    Player targetPlayer = (Player) entity;
                    targetPlayer.sendMessage(
                            new TranslatableComponent("item.ex_enigmaticlegacy.dormant_arcanum.void_banishment")
                                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD),
                            targetPlayer.getUUID()
                    );
                }

                for (int i = 0; i < 20; i++) {
                    level.sendParticles(
                            net.minecraft.core.particles.ParticleTypes.DRAGON_BREATH,
                            entity.getX() + (Math.random() - 0.5) * 2,
                            entity.getY() + Math.random() * entity.getBbHeight(),
                            entity.getZ() + (Math.random() - 0.5) * 2,
                            1, 0, 0, 0, 0.1
                    );
                }

                float voidDamage = 999999.0F;
                entity.hurt(new ModDamageSources.DamageSourceTrueDamageUndef(), voidDamage);

                level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                        SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.5F, 0.8F);
            }

            level.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER,
                    dyingPlayer.getX(), dyingPlayer.getY(), dyingPlayer.getZ(),
                    5, 2, 2, 2, 0
            );
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player && !player.level.isClientSide) {
            CuriosApi.getCuriosHelper().findEquippedCurio(ModItems.DORMANT_ARCANUM.get(), player)
                    .ifPresent(result -> {
                        ItemStack stack = result.getRight();
                        var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
                        if (relicCap.isPresent()) {
                            IRelic relic = relicCap.orElse(null);
                            if (relic != null && !relic.isRightPlayer(player)) {
                                return;
                            }
                        }

                        voidBanishment(player);

                        player.sendMessage(
                                new TranslatableComponent("item.ex_enigmaticlegacy.dormant_arcanum.death_trigger")
                                        .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
                                player.getUUID()
                        );
                    });
        }
    }
}