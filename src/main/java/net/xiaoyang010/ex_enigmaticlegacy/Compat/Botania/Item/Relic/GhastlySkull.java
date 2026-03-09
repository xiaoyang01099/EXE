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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model.Vector3;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModDamageSources;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEffects;
import net.xiaoyang010.ex_enigmaticlegacy.api.INoEMCItem;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.item.IRelic;
import vazkii.botania.api.mana.IManaItem;
import vazkii.botania.client.fx.WispParticleData;
import vazkii.botania.common.block.ModSubtiles;
import vazkii.botania.common.helper.ItemNBTHelper;
import vazkii.botania.common.item.relic.RelicImpl;
import vazkii.botania.xplat.IXplatAbstractions;

import javax.annotation.Nullable;
import java.util.List;

public class GhastlySkull extends Item implements ICurioItem, INoEMCItem, ICursed {

    private static final int MANA_COST = 3000; // 大量魔力消耗
    private static final int COOLDOWN_TICKS = 100; // 30秒冷却
    private static final int DEATH_DELAY_TICKS = 600; // 30秒死亡延迟
    private static final String DEATH_PROTECTION_TAG = "GhastlyDeathProtection";
    private static final String PROTECTION_START_TIME = "ProtectionStartTime";

    public GhastlySkull(Properties properties) {
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
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        ItemLoreHelper.indicateCursedOnesOnly(tooltip);
        RelicImpl.addDefaultTooltip(stack, tooltip);

        if (Screen.hasShiftDown()) {
            tooltip.add(new TranslatableComponent("item.ItemGhastlySkull1.lore").withStyle(ChatFormatting.DARK_RED));
            tooltip.add(new TranslatableComponent("item.ItemGhastlySkull2.lore").withStyle(ChatFormatting.DARK_RED));
            tooltip.add(Component.nullToEmpty(""));
            tooltip.add(new TranslatableComponent("item.ghastly_skull.danger").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
            tooltip.add(new TranslatableComponent("item.ghastly_skull.mana_cost", MANA_COST).withStyle(ChatFormatting.BLUE));
            tooltip.add(new TranslatableComponent("item.ghastly_skull.cooldown", COOLDOWN_TICKS / 20).withStyle(ChatFormatting.GRAY));
            tooltip.add(new TranslatableComponent("item.ghastly_skull.death_protection").withStyle(ChatFormatting.GOLD));
            tooltip.add(new TranslatableComponent("item.ghastly_skull.curios_use").withStyle(ChatFormatting.AQUA));
        } else {
            tooltip.add(new TranslatableComponent("item.FRShiftTooltip.lore").withStyle(ChatFormatting.DARK_GRAY));
        }

        long lastUsed = ItemNBTHelper.getLong(stack, "LastUsed", 0);
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUsed < COOLDOWN_TICKS * 50) {
            long remainingSeconds = (COOLDOWN_TICKS * 50 - (currentTime - lastUsed)) / 1000;
            tooltip.add(new TranslatableComponent("item.ghastly_skull.on_cooldown", remainingSeconds).withStyle(ChatFormatting.RED));
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
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
        if (relicCap.isPresent()) {
            IRelic relic = relicCap.orElse(null);
            if (relic != null && !relic.isRightPlayer(player)) {
                return InteractionResultHolder.fail(stack);
            }
        }

        if (isOnCooldown(stack)) {
            if (!level.isClientSide) {
                player.displayClientMessage(new TranslatableComponent("item.ghastly_skull.still_cooling").withStyle(ChatFormatting.RED), true);
            }
            return InteractionResultHolder.fail(stack);
        }

        if (!consumeMana(player, MANA_COST)) {
            if (!level.isClientSide) {
                player.displayClientMessage(new TranslatableComponent("item.ghastly_skull.no_mana").withStyle(ChatFormatting.BLUE), true);
            }
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide) {
            float currentHealth = player.getHealth();
            float maxHealth = player.getMaxHealth();

            if (currentHealth < maxHealth * 0.3f) {
                player.setHealth(1.0f);
                player.displayClientMessage(new TranslatableComponent("item.ghastly_skull.dangerous_heal").withStyle(ChatFormatting.DARK_RED), false);

                performGhastlyResurrection(level, player);
            } else {
                player.hurt(ModDamageSources.ABSOLUTE, currentHealth - 1.0f);
                player.displayClientMessage(new TranslatableComponent("item.ghastly_skull.punishment").withStyle(ChatFormatting.RED), false);
            }

            performGhastlyGaze(level, player);

            ItemNBTHelper.setLong(stack, "LastUsed", System.currentTimeMillis());
            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.GHAST_SCREAM, SoundSource.PLAYERS, 1.0f, 0.8f);
        }

        return InteractionResultHolder.success(stack);
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
        return slotContext.identifier().equals("charm") || slotContext.identifier().equals("necklace");
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();

        if (entity instanceof Player player) {
            var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
            if (relicCap.isPresent()) {
                IRelic relic = relicCap.orElse(null);
                if (relic != null && !relic.isRightPlayer(player)) {
                    return;
                }
            }
        }

        if (ItemNBTHelper.getBoolean(stack, DEATH_PROTECTION_TAG, false)) {
            long protectionStartTime = ItemNBTHelper.getLong(stack, PROTECTION_START_TIME, 0);
            long currentTime = entity.level.getGameTime();

            if (currentTime - protectionStartTime >= DEATH_DELAY_TICKS) {
                executeDelayedDeath(entity, stack);
                return;
            }

            if (entity.level.isClientSide && entity.tickCount % 5 == 0) {
                spawnDeathProtectionParticles(entity.level, entity);
            }

            if (entity.tickCount % 20 == 0 && entity instanceof Player player) {
                long remainingTicks = DEATH_DELAY_TICKS - (currentTime - protectionStartTime);
                int remainingSeconds = (int) (remainingTicks / 20);

                if (remainingSeconds > 0) {
                    player.displayClientMessage(
                            new TranslatableComponent("item.ghastly_skull.death_countdown", remainingSeconds)
                                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
                            true
                    );
                }
            }
        }

        if (entity.tickCount % 100 == 0) {
            spawnAmbientParticles(entity.level, entity);
        }
    }

    @SubscribeEvent
    public void onRightClickAir(PlayerInteractEvent.RightClickEmpty event) {
        Player player = event.getPlayer();

        CuriosApi.getCuriosHelper().findFirstCurio(player, this).ifPresent(slotResult -> {
            ItemStack stack = slotResult.stack();

            var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
            if (relicCap.isPresent()) {
                IRelic relic = relicCap.orElse(null);
                if (relic != null && !relic.isRightPlayer(player)) {
                    return;
                }
            }

            if (player.isShiftKeyDown()) {
                Level level = player.level;

                if (attemptUse(level, player, stack)) {
                    if (!level.isClientSide) {
                        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.GHAST_SCREAM, SoundSource.PLAYERS, 1.0f, 0.8f);
                    }
                }
            }
        });
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        CuriosApi.getCuriosHelper().findFirstCurio(player, this).ifPresent(slotResult -> {
            ItemStack stack = slotResult.stack();

            var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
            if (relicCap.isPresent()) {
                IRelic relic = relicCap.orElse(null);
                if (relic != null && !relic.isRightPlayer(player)) {
                    return;
                }
            }

            if (ItemNBTHelper.getBoolean(stack, DEATH_PROTECTION_TAG, false)) {
                return;
            }

            event.setCanceled(true);

            activateDeathProtection(player, stack);
        });
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        CuriosApi.getCuriosHelper().findFirstCurio(player, this).ifPresent(slotResult -> {
            ItemStack stack = slotResult.stack();

            var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
            if (relicCap.isPresent()) {
                IRelic relic = relicCap.orElse(null);
                if (relic != null && !relic.isRightPlayer(player)) {
                    return;
                }
            }

            if (ItemNBTHelper.getBoolean(stack, DEATH_PROTECTION_TAG, false)) {
                float currentHealth = player.getHealth();
                float damage = event.getAmount();

                if (currentHealth - damage < 1.0f) {
                    event.setAmount(Math.max(0, currentHealth - 1.0f));
                }
            }
        });
    }

    private void activateDeathProtection(Player player, ItemStack stack) {
        player.setHealth(1.0f);

        ItemNBTHelper.setBoolean(stack, DEATH_PROTECTION_TAG, true);
        ItemNBTHelper.setLong(stack, PROTECTION_START_TIME, player.level.getGameTime());

        player.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_RESISTANCE, DEATH_DELAY_TICKS, 100)); // 抗性提升IV
        player.addEffect(new MobEffectInstance(
                MobEffects.REGENERATION, DEATH_DELAY_TICKS, 100)); // 生命恢复II
        player.addEffect(new MobEffectInstance(
                MobEffects.GLOWING, DEATH_DELAY_TICKS, 0)); // 发光效果

        player.displayClientMessage(
                new TranslatableComponent("item.ghastly_skull.death_prevented")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                false
        );

        player.level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0f, 0.8f);

        spawnDeathProtectionActivationParticles(player.level, player);
    }

    private void executeDelayedDeath(LivingEntity entity, ItemStack stack) {
        if (entity instanceof Player player) {
            ItemNBTHelper.removeEntry(stack, DEATH_PROTECTION_TAG);
            ItemNBTHelper.removeEntry(stack, PROTECTION_START_TIME);

            stack.shrink(stack.getCount());

            player.displayClientMessage(
                    new TranslatableComponent("item.ghastly_skull.final_death")
                            .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
                    false
            );

            player.level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.GHAST_DEATH, SoundSource.PLAYERS, 1.0f, 0.5f);

            spawnFinalDeathParticles(player.level, player);

            player.hurt(ModDamageSources.ABSOLUTE, Float.MAX_VALUE);
        }
    }

    private void spawnDeathProtectionActivationParticles(Level level, Player player) {
        if (level.isClientSide) {
            for (int i = 0; i < 100; i++) {
                double angle = Math.random() * Math.PI * 2;
                double pitch = Math.random() * Math.PI - Math.PI / 2;
                double radius = Math.random() * 5 + 2;

                double x = player.getX() + Math.cos(angle) * Math.cos(pitch) * radius;
                double y = player.getY() + 1 + Math.sin(pitch) * radius;
                double z = player.getZ() + Math.sin(angle) * Math.cos(pitch) * radius;

                WispParticleData data = WispParticleData.wisp(
                        0.8f + level.random.nextFloat() * 0.4f,
                        1.0f, 0.8f, 0.0f, 3.0f, false);
                level.addParticle(data, x, y, z,
                        (player.getX() - x) * 0.1,
                        (player.getY() + 1 - y) * 0.1,
                        (player.getZ() - z) * 0.1);
            }
        }
    }

    private void spawnDeathProtectionParticles(Level level, LivingEntity entity) {
        if (level.isClientSide) {
            for (int i = 0; i < 5; i++) {
                double angle = (entity.tickCount + i * 72) * Math.PI / 180; // 5个粒子围绕旋转
                double radius = 2.0;

                double x = entity.getX() + Math.cos(angle) * radius;
                double y = entity.getY() + 1 + Math.sin(entity.tickCount * 0.1) * 0.5;
                double z = entity.getZ() + Math.sin(angle) * radius;

                WispParticleData data = WispParticleData.wisp(0.6f,
                        1.0f, 0.6f, 0.0f, 2.0f, false);
                level.addParticle(data, x, y, z, 0, 0.05, 0);
            }
        }
    }

    private void spawnFinalDeathParticles(Level level, LivingEntity entity) {
        if (level.isClientSide) {
            for (int i = 0; i < 50; i++) {
                double angle = Math.random() * Math.PI * 2;
                double speed = Math.random() * 3 + 1;

                double motionX = Math.cos(angle) * speed;
                double motionY = Math.random() * 2;
                double motionZ = Math.sin(angle) * speed;

                WispParticleData data = WispParticleData.wisp(
                        1.0f + level.random.nextFloat() * 0.5f,
                        0.8f, 0.0f, 0.0f, 2.0f, false);
                level.addParticle(data,
                        entity.getX(), entity.getY() + 1, entity.getZ(),
                        motionX * 0.2, motionY * 0.2, motionZ * 0.2);
            }
        }
    }

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getPlayer();

        CuriosApi.getCuriosHelper().findFirstCurio(player, this).ifPresent(slotResult -> {
            ItemStack stack = slotResult.stack();

            var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
            if (relicCap.isPresent()) {
                IRelic relic = relicCap.orElse(null);
                if (relic != null && !relic.isRightPlayer(player)) {
                    return;
                }
            }

            if (player.isShiftKeyDown()) {
                Level level = player.level;

                if (attemptUse(level, player, stack)) {
                    if (!level.isClientSide) {
                        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.GHAST_SCREAM, SoundSource.PLAYERS, 1.0f, 0.8f);
                    }
                    event.setCanceled(true);
                }
            }
        });
    }

    private void spawnAmbientParticles(Level level, LivingEntity entity) {
        if (level.isClientSide) {
            for (int i = 0; i < 3; i++) {
                double offsetX = (Math.random() - 0.5) * 1.5;
                double offsetY = Math.random() * 2;
                double offsetZ = (Math.random() - 0.5) * 1.5;

                WispParticleData data = WispParticleData.wisp(0.2f,
                        0.6f, 0.6f, 0.8f, 3.0f, false);
                level.addParticle(data,
                        entity.getX() + offsetX,
                        entity.getY() + offsetY,
                        entity.getZ() + offsetZ,
                        0, 0.05, 0);
            }
        }
    }

    private boolean attemptUse(Level level, Player player, ItemStack stack) {
        var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
        if (relicCap.isPresent()) {
            IRelic relic = relicCap.orElse(null);
            if (relic != null && !relic.isRightPlayer(player)) {
                return false;
            }
        }

        if (isOnCooldown(stack)) {
            if (!level.isClientSide) {
                player.displayClientMessage(new TranslatableComponent("item.ghastly_skull.still_cooling").withStyle(ChatFormatting.RED), true);
            }
            return false;
        }

        if (!consumeMana(player, MANA_COST)) {
            if (!level.isClientSide) {
                player.displayClientMessage(new TranslatableComponent("item.ghastly_skull.no_mana").withStyle(ChatFormatting.BLUE), true);
            }
            return false;
        }

        if (!level.isClientSide) {
            float currentHealth = player.getHealth();
            float maxHealth = player.getMaxHealth();

            if (currentHealth < maxHealth * 0.3f) {
                player.setHealth(1.0f);
                player.displayClientMessage(new TranslatableComponent("item.ghastly_skull.dangerous_heal").withStyle(ChatFormatting.DARK_RED), false);

                performGhastlyResurrection(level, player);
            } else {
                player.hurt(ModDamageSources.ABSOLUTE, currentHealth - 1.0f);
                player.displayClientMessage(new TranslatableComponent("item.ghastly_skull.punishment").withStyle(ChatFormatting.RED), false);
            }

            performGhastlyGaze(level, player);

            ItemNBTHelper.setLong(stack, "LastUsed", System.currentTimeMillis());
            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        }

        return true;
    }

    private void performGhastlyResurrection(Level level, Player player) {
        BlockPos playerPos = player.blockPosition();

        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4;
            int offsetX = (int) (Math.cos(angle) * 3);
            int offsetZ = (int) (Math.sin(angle) * 3);
            BlockPos flowerPos = playerPos.offset(offsetX, 0, offsetZ);

            if (level.getBlockState(flowerPos).isAir()) {
                Block flowerToPlace = switch (i % 4) {
                    case 0 -> ModSubtiles.bellethorn;
                    case 1 -> ModSubtiles.dreadthorn;
                    case 2 -> ModSubtiles.jadedAmaranthus;
                    default -> ModSubtiles.endoflame;
                };

                level.setBlock(flowerPos, flowerToPlace.defaultBlockState(), 3);

                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.scheduleTick(flowerPos, flowerToPlace, 6000);
                }
            }
        }

        spawnResurrectionParticles(level, player);
    }

    private void performGhastlyGaze(Level level, Player player) {
        Vector3 start = Vector3.fromEntityCenter(player);
        Vec3 lookVec = player.getLookAngle();
        Vector3 direction = new Vector3(lookVec.x, lookVec.y, lookVec.z);

        for (int i = 1; i <= 32; i++) {
            Vector3 currentPos = start.copy().add(direction.copy().multiply(i));
            BlockPos blockPos = new BlockPos(currentPos.x, currentPos.y, currentPos.z);
            BlockState state = level.getBlockState(blockPos);

            if (!state.isAir() && state.getDestroySpeed(level, blockPos) >= 0) {
                Block currentBlock = state.getBlock();
                Block ghostlyVersion = getGhostlyVersion(currentBlock);

                if (ghostlyVersion != null) {
                    level.setBlock(blockPos, ghostlyVersion.defaultBlockState(), 3);

                    if (level instanceof ServerLevel serverLevel) {
                        scheduleBlockRestore(serverLevel, blockPos, state, 1200);
                    }
                }
            }

            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class,
                    new AABB(blockPos).inflate(1.5));

            for (LivingEntity entity : entities) {
                if (entity != player) {
                    entity.hurt(ModDamageSources.ABSOLUTE, 12.0f + level.random.nextFloat() * 8.0f);

                    entity.addEffect(new MobEffectInstance(
                            ModEffects.DROWNING.get(), 200, 2));
                    entity.addEffect(new MobEffectInstance(
                            MobEffects.WITHER, 200, 4));
                    entity.addEffect(new MobEffectInstance(
                            MobEffects.WEAKNESS, 200, 1));
                    entity.addEffect(new MobEffectInstance(
                            MobEffects.MOVEMENT_SLOWDOWN, 200, 2));
                }
            }

            if (i % 4 == 0) {
                spawnGazeParticles(level, currentPos);
            }
        }
    }

    private Block getGhostlyVersion(Block original) {
        if (original == Blocks.STONE) return Blocks.COBBLESTONE;
        if (original == Blocks.COBBLESTONE) return Blocks.MOSSY_COBBLESTONE;
        if (original == Blocks.DIRT) return Blocks.COARSE_DIRT;
        if (original == Blocks.GRASS_BLOCK) return Blocks.MYCELIUM;
        if (original == Blocks.OAK_LOG) return Blocks.DARK_OAK_LOG;
        if (original == Blocks.SAND) return Blocks.SOUL_SAND;
        if (original == Blocks.WATER) return Blocks.AIR;
        if (original == Blocks.LAVA) return Blocks.OBSIDIAN;

        return null;
    }

    private void scheduleBlockRestore(ServerLevel level, BlockPos pos, BlockState originalState, int ticks) {
        level.scheduleTick(pos, originalState.getBlock(), ticks);
    }

    private void spawnResurrectionParticles(Level level, Player player) {
        for (int i = 0; i < 50; i++) {
            double angle = Math.random() * Math.PI * 2;
            double radius = Math.random() * 4 + 1;
            double height = Math.random() * 3;

            double x = player.getX() + Math.cos(angle) * radius;
            double y = player.getY() + height;
            double z = player.getZ() + Math.sin(angle) * radius;

            WispParticleData data = WispParticleData.wisp(0.5f + level.random.nextFloat() * 0.3f,
                    0.8f, 1.0f, 0.9f, 2.0f, false);
            level.addParticle(data, x, y, z, 0, 0.1, 0);
        }
    }

    private void spawnGazeParticles(Level level, Vector3 pos) {
        for (int i = 0; i < 8; i++) {
            double offsetX = (Math.random() - 0.5) * 2;
            double offsetY = (Math.random() - 0.5) * 2;
            double offsetZ = (Math.random() - 0.5) * 2;

            WispParticleData data = WispParticleData.wisp(0.8f,
                    0.2f, 0.0f, 0.0f, 1.5f, true);
            level.addParticle(data,
                    pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ,
                    -offsetX * 0.1, -offsetY * 0.1, -offsetZ * 0.1);
        }
    }

    private boolean isOnCooldown(ItemStack stack) {
        long lastUsed = ItemNBTHelper.getLong(stack, "LastUsed", 0);
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastUsed) < (COOLDOWN_TICKS * 50);
    }

    private boolean consumeMana(Player player, int amount) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            IManaItem manaItem = IXplatAbstractions.INSTANCE.findManaItem(stack);

            if (manaItem != null && manaItem.getMana() >= amount) {
                manaItem.addMana(-amount);
                return true;
            }
        }
        return false;
    }
}