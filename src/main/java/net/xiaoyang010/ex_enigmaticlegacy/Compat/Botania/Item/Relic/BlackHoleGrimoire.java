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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
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
import net.xiaoyang010.ex_enigmaticlegacy.Entity.others.BlackHoleEntity;
import net.xiaoyang010.ex_enigmaticlegacy.Event.RelicsEventHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEntities;
import net.xiaoyang010.ex_enigmaticlegacy.api.INoEMCItem;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.item.IRelic;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.client.fx.WispParticleData;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.item.relic.RelicImpl;
import vazkii.botania.xplat.IXplatAbstractions;

import javax.annotation.Nullable;
import java.util.*;

public class BlackHoleGrimoire extends Item implements INoEMCItem, ICursed {
    private static final int MANA_PER_5_SEC = 1000;
    private static final int MANA_CHECK_INTERVAL = 100;
    private static final float REPEL_RANGE = 8.0F;
    private static final float REPEL_STRENGTH = 2.5F;
    private static final int MIN_SPAWN_INTERVAL = 10;
    private static final int MAX_SPAWN_INTERVAL = 40;
    private final Map<Player, Integer> nextSpawnTicks = new HashMap<>();
    private final Map<Player, List<BlackHoleSpawnData>> scheduledSpawns = new HashMap<>();

    public BlackHoleGrimoire(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new RelicCapProvider(stack);
    }

    private static class RelicCapProvider implements ICapabilityProvider {
        private final LazyOptional<IRelic> relic;
        public RelicCapProvider(ItemStack stack) {
            this.relic = LazyOptional.of(() -> new RelicImpl(stack, null));
        }
        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction direction) {
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
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        ItemLoreHelper.indicateCursedOnesOnly(tooltip);
        RelicImpl.addDefaultTooltip(stack, tooltip);

        if (Screen.hasShiftDown()) {
            tooltip.add(new TranslatableComponent("item.black_hole_grimoire.lore1")
                    .withStyle(ChatFormatting.DARK_PURPLE));
            tooltip.add(new TranslatableComponent("item.black_hole_grimoire.lore2")
                    .withStyle(ChatFormatting.DARK_PURPLE));
            tooltip.add(new TranslatableComponent("item.black_hole_grimoire.lore3")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(new TranslatableComponent("item.FREmpty.lore"));

            tooltip.add(new TranslatableComponent("item.black_hole_grimoire.ability_title")
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(new TranslatableComponent("item.black_hole_grimoire.ability1")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(new TranslatableComponent("item.black_hole_grimoire.ability2")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(new TranslatableComponent("item.black_hole_grimoire.ability3")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(new TranslatableComponent("item.FREmpty.lore"));

            tooltip.add(new TranslatableComponent("item.black_hole_grimoire.mana_cost")
                    .append(" " + MANA_PER_5_SEC + " ")
                    .append(new TranslatableComponent("item.black_hole_grimoire.mana_per_5sec"))
                    .withStyle(ChatFormatting.AQUA));
            tooltip.add(new TranslatableComponent("item.FREmpty.lore"));

            tooltip.add(new TranslatableComponent("item.black_hole_grimoire.usage_title")
                    .withStyle(ChatFormatting.YELLOW));
            tooltip.add(new TranslatableComponent("item.black_hole_grimoire.usage1")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(new TranslatableComponent("item.black_hole_grimoire.usage2")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(new TranslatableComponent("item.FREmpty.lore"));

            tooltip.add(new TranslatableComponent("item.black_hole_grimoire.warning1")
                    .withStyle(ChatFormatting.RED));
            tooltip.add(new TranslatableComponent("item.black_hole_grimoire.warning2")
                    .withStyle(ChatFormatting.RED));

        } else {
            tooltip.add(new TranslatableComponent("item.FRShiftTooltip.lore")
                    .withStyle(ChatFormatting.GRAY));
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

        if (!level.isClientSide) {
            if (!hasManaSource(player)) {
                player.displayClientMessage(
                        new TranslatableComponent("message.black_hole_grimoire.no_mana_source")
                                .withStyle(ChatFormatting.RED),
                        true
                );
                return InteractionResultHolder.fail(stack);
            }

            if (!ManaItemHandler.instance().requestManaExact(stack, player, MANA_PER_5_SEC, true)) {
                player.displayClientMessage(
                        new TranslatableComponent("message.black_hole_grimoire.insufficient_mana", MANA_PER_5_SEC)
                                .withStyle(ChatFormatting.RED),
                        true
                );
                return InteractionResultHolder.fail(stack);
            }
        }

        player.startUsingItem(hand);
        nextSpawnTicks.put(player, 0);
        scheduledSpawns.put(player, new ArrayList<>());
        return InteractionResultHolder.consume(stack);
    }

    private boolean hasManaSource(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && ManaItemHandler.instance().requestManaExact(stack, player, 0, false)) {
                return true;
            }
        }
        for (ItemStack stack : player.getInventory().armor) {
            if (!stack.isEmpty() && ManaItemHandler.instance().requestManaExact(stack, player, 0, false)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingTicks) {
        if (!(entity instanceof Player player)) return;

        var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
        if (relicCap.isPresent()) {
            IRelic relic = relicCap.orElse(null);
            if (relic != null && !relic.isRightPlayer(player)) {
                player.stopUsingItem();
                cleanupPlayer(player);
                return;
            }
        }

        int useTime = this.getUseDuration(stack) - remainingTicks;

        if (!level.isClientSide) {
            player.invulnerableTime = 20;
        }

        if (useTime % MANA_CHECK_INTERVAL == 0 && useTime > 0) {
            if (!level.isClientSide) {
                if (!hasManaSource(player)) {
                    player.stopUsingItem();
                    player.displayClientMessage(
                            new TranslatableComponent("message.black_hole_grimoire.mana_source_lost")
                                    .withStyle(ChatFormatting.RED),
                            true
                    );
                    cleanupPlayer(player);
                    return;
                }

                if (!ManaItemHandler.instance().requestManaExact(stack, player, MANA_PER_5_SEC, true)) {
                    player.stopUsingItem();
                    player.displayClientMessage(
                            new TranslatableComponent("message.black_hole_grimoire.mana_depleted")
                                    .withStyle(ChatFormatting.RED),
                            true
                    );
                    cleanupPlayer(player);
                    return;
                }
            }
        }

        if (level.isClientSide) {
            spawnSpiralParticles(level, player);
            spawnShieldParticles(level, player);
        } else {
            repelAllEntities(level, player);
            scheduleBlackHoleSpawns(level, player, useTime);
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player) || level.isClientSide) return;

        int useTime = this.getUseDuration(stack) - timeLeft;
        if (useTime < 20) {
            cleanupPlayer(player);
            return;
        }

        spawnMysticalFlowers((ServerLevel) level, player.blockPosition());
        spawnFinalExplosionEffect((ServerLevel) level, player);
        level.playSound(null, player.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL,
                SoundSource.PLAYERS, 1.0F, 0.8F);

        cleanupPlayer(player);
    }

    private void scheduleBlackHoleSpawns(Level level, Player player, int useTime) {
        if (!nextSpawnTicks.containsKey(player)) {
            nextSpawnTicks.put(player, 0);
        }

        int nextSpawn = nextSpawnTicks.get(player);

        if (useTime >= nextSpawn) {
            Entity pointedEntity = RelicsEventHandler.getPointedEntity(
                    level, player, 0, 32, 1.0F
            );

            Vec3 baseCenter;
            if (pointedEntity != null) {
                baseCenter = pointedEntity.position().add(0, pointedEntity.getBbHeight() / 2, 0);
            } else {
                Vec3 lookVec = player.getLookAngle();
                baseCenter = player.getEyePosition().add(lookVec.scale(8.0));
            }

            int spawnCount = 1 + level.random.nextInt(5);

            for (int i = 0; i < spawnCount; i++) {
                double offsetX = (level.random.nextDouble() - 0.5) * 12.0;
                double offsetY = (level.random.nextDouble() - 0.5) * 8.0;
                double offsetZ = (level.random.nextDouble() - 0.5) * 12.0;

                Vec3 spawnPos = baseCenter.add(offsetX, offsetY, offsetZ);
                float randomScale = 0.2F + level.random.nextFloat() * 1.0F;
                int delay = level.random.nextInt(15);

                BlackHoleSpawnData data = new BlackHoleSpawnData(
                        spawnPos, randomScale, useTime + delay
                );

                if (!scheduledSpawns.containsKey(player)) {
                    scheduledSpawns.put(player, new ArrayList<>());
                }
                scheduledSpawns.get(player).add(data);
            }

            int nextInterval = MIN_SPAWN_INTERVAL + level.random.nextInt(MAX_SPAWN_INTERVAL - MIN_SPAWN_INTERVAL);
            nextSpawnTicks.put(player, useTime + nextInterval);
        }

        if (scheduledSpawns.containsKey(player)) {
            List<BlackHoleSpawnData> spawns = scheduledSpawns.get(player);
            List<BlackHoleSpawnData> toRemove = new ArrayList<>();

            for (BlackHoleSpawnData data : spawns) {
                if (useTime >= data.spawnTime) {
                    spawnBlackHoleWithEffect(level, player, data.position, data.scale);
                    toRemove.add(data);
                }
            }

            spawns.removeAll(toRemove);
        }
    }

    private void spawnBlackHoleWithEffect(Level level, Player player, Vec3 pos, float scale) {
        BlackHoleEntity blackHole = new BlackHoleEntity(ModEntities.BLACK_HOLE.get(), level);
        blackHole.setPos(pos.x, pos.y - 2.0, pos.z);
        blackHole.setCustomScale(scale);
        blackHole.setOwnerId(player.getId());
        blackHole.setTargetY((float) pos.y);
        level.addFreshEntity(blackHole);

        for (int i = 0; i < 20; i++) {
            double vx = (level.random.nextDouble() - 0.5) * 0.5;
            double vy = level.random.nextDouble() * 0.5;
            double vz = (level.random.nextDouble() - 0.5) * 0.5;

            SparkleParticleData sparkle = SparkleParticleData.sparkle(
                    (float)(1.0F + level.random.nextFloat()),
                    0.4F, 0.0F, 0.8F, 5
            );
            level.addParticle(sparkle, pos.x, pos.y, pos.z, vx, vy, vz);
        }

        level.playSound(null, new BlockPos(pos), SoundEvents.PORTAL_TRIGGER,
                SoundSource.PLAYERS, 0.5F, 1.5F);
    }

    private void repelAllEntities(Level level, Player player) {
        AABB range = new AABB(player.blockPosition()).inflate(REPEL_RANGE);

        List<LivingEntity> livingEntities = level.getEntitiesOfClass(
                LivingEntity.class, range,
                e -> e != player && e.distanceTo(player) < REPEL_RANGE
        );

        for (LivingEntity entity : livingEntities) {
            Vec3 direction = entity.position().subtract(player.position()).normalize();
            entity.setDeltaMovement(direction.scale(REPEL_STRENGTH));
            entity.hurtMarked = true;
        }

        List<Entity> projectiles = level.getEntitiesOfClass(
                Entity.class, range,
                e -> (e instanceof AbstractArrow || e instanceof ThrownEnderpearl)
                        && e.distanceTo(player) < REPEL_RANGE
        );

        for (Entity projectile : projectiles) {
            Vec3 direction = projectile.position().subtract(player.position()).normalize();
            projectile.setDeltaMovement(direction.scale(REPEL_STRENGTH * 1.5));
            projectile.hurtMarked = true;
        }
    }

    private void spawnShieldParticles(Level level, Player player) {
        long time = level.getGameTime();

        for (int i = 0; i < 8; i++) {
            double angle = (time * 0.2 + i * 45) % 360;
            double radians = Math.toRadians(angle);
            double radius = REPEL_RANGE * 0.8;

            double x = player.getX() + Math.cos(radians) * radius;
            double z = player.getZ() + Math.sin(radians) * radius;
            double y = player.getY() + 1.0 + Math.sin(time * 0.1 + i) * 0.5;

            WispParticleData wisp = WispParticleData.wisp(
                    0.4F, 0.6F, 0.0F, 1.0F, 1.0F, false
            );
            level.addParticle(wisp, x, y, z, 0, 0, 0);
        }
    }

    private void spawnSpiralParticles(Level level, Player player) {
        double radius = 2.0;
        long time = level.getGameTime();

        for (int i = 0; i < 5; i++) {
            double angle = (time * 0.15 + i * 72) % 360;
            double radians = Math.toRadians(angle);
            double x = player.getX() + Math.cos(radians) * radius;
            double z = player.getZ() + Math.sin(radians) * radius;
            double y = player.getY() + (time % 60) * 0.05;

            WispParticleData data = WispParticleData.wisp(0.4F, 0.0F, 0.8F, 1.0F, 1.0F, false);
            level.addParticle(data, x, y, z, 0, 0.08, 0);
        }
    }

    private void spawnFinalExplosionEffect(ServerLevel level, Player player) {
        for (int i = 0; i < 50; i++) {
            double vx = (level.random.nextDouble() - 0.5) * 2.0;
            double vy = level.random.nextDouble() * 2.0;
            double vz = (level.random.nextDouble() - 0.5) * 2.0;

            SparkleParticleData sparkle = SparkleParticleData.sparkle(
                    2.0F, 0.8F, 0.0F, 1.0F, 10
            );
            level.sendParticles(sparkle, player.getX(), player.getY() + 1, player.getZ(),
                    1, vx, vy, vz, 0.1);
        }
    }

    private void spawnMysticalFlowers(ServerLevel level, BlockPos center) {
        Random random = level.random;
        var flowers = List.of(
                ModBlocks.whiteFlower, ModBlocks.orangeFlower, ModBlocks.magentaFlower,
                ModBlocks.lightBlueFlower, ModBlocks.yellowFlower, ModBlocks.limeFlower,
                ModBlocks.pinkFlower, ModBlocks.grayFlower, ModBlocks.lightGrayFlower,
                ModBlocks.cyanFlower, ModBlocks.purpleFlower, ModBlocks.blueFlower,
                ModBlocks.brownFlower, ModBlocks.greenFlower, ModBlocks.redFlower, ModBlocks.blackFlower
        );

        for (int i = 0; i < 40; i++) {
            int xOffset = random.nextInt(12) - 6;
            int zOffset = random.nextInt(12) - 6;
            BlockPos pos = center.offset(xOffset, 0, zOffset);

            while (pos.getY() > level.getMinBuildHeight() && level.isEmptyBlock(pos.below())) {
                pos = pos.below();
            }

            if (level.isEmptyBlock(pos) && level.getBlockState(pos.below()).isSolidRender(level, pos.below())) {
                var flower = flowers.get(random.nextInt(flowers.size()));
                level.setBlock(pos, flower.defaultBlockState(), 3);
            }
        }
    }

    private void cleanupPlayer(Player player) {
        nextSpawnTicks.remove(player);
        scheduledSpawns.remove(player);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    private static class BlackHoleSpawnData {
        final Vec3 position;
        final float scale;
        final int spawnTime;

        BlackHoleSpawnData(Vec3 position, float scale, int spawnTime) {
            this.position = position;
            this.scale = scale;
            this.spawnTime = spawnTime;
        }
    }
}
