package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.others.EntitySlimeCannonBall;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEffects;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import net.xiaoyang010.ex_enigmaticlegacy.Util.StyleMarker;
import net.xiaoyang010.ex_enigmaticlegacy.api.IWaveName;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import vazkii.botania.client.fx.WispParticleData;

import javax.annotation.Nullable;
import java.util.*;

public class SlimeNecklace extends Item implements ICurioItem, IWaveName {
    private static final Set<UUID> TAMED_SLIMES = Collections.newSetFromMap(new WeakHashMap<>());
    private static final String TAG_ABSORB_TIMER = "NecklaceAbsorbTimer";
    private static final String TAG_GEN_TIMER = "NecklaceGenTimer";
    private static final String TAG_SHIELD_CD = "NecklaceShieldCD";
    private static final String TAG_FUSION_TIMER = "NecklaceFusionTimer";
    private static final String TAG_TIME_FREEZE_CD = "NecklaceTimeFreezeCD";
    private static final String TAG_REFLECT_CD = "NecklaceReflectCD";
    private static final String TAG_RAIN_CD = "NecklaceRainCD";
    private static final String TAG_RAIN_ACTIVE = "NecklaceRainActive";
    private static final String TAG_RAIN_TICK = "NecklaceRainTick";
    private static final String TAG_RAIN_FIRED = "NecklaceRainFired";
    private static final double AUTO_ABSORB_RADIUS = 6.0;
    private static final int AUTO_ABSORB_INTERVAL = 40;
    private static final int PASSIVE_GEN_INTERVAL = 300;
    private static final int PASSIVE_GEN_MANA_COST = 500;
    private static final int PASSIVE_GEN_CAP = 32;
    private static final int SHIELD_SLIME_COST = 3;
    private static final int SHIELD_COOLDOWN = 100;
    private static final int BUFF_THRESHOLD = 10;
    private static final double SLIME_AFFINITY_RADIUS = 24.0;
    private static final double SLIME_FOLLOW_DISTANCE = 5.0;
    private static final int SLIME_AFFINITY_INTERVAL = 10;
    private static final int FUSION_INTERVAL = 200;
    private static final int FUSION_COST_COUNT = 4;
    private static final int FUSION_MANA_COST = 800;
    private static final double TIME_FREEZE_RADIUS = 8.0;
    private static final int TIME_FREEZE_DURATION = 80;
    private static final int TIME_FREEZE_COOLDOWN = 400;
    private static final float TIME_FREEZE_HP_TRIGGER = 0.3F;
    private static final int TIME_FREEZE_SLIME_COST = 2;
    private static final int REFLECT_COOLDOWN = 60;
    private static final float REFLECT_DAMAGE_MULT = 2.0F;
    private static final int REFLECT_MANA_COST = 300;
    private static final int RAIN_SLIME_COST = 10;
    private static final int RAIN_MANA_COST = 5000;
    private static final int RAIN_COOLDOWN = 600;
    private static final int RAIN_PROJECTILE_COUNT = 20;
    private static final float RAIN_DAMAGE_PER_BALL = 25.0F;
    public static final float ABSORB_COOLDOWN_MULTIPLIER = 0.5F;
    public static final float ABSORB_MANA_DISCOUNT = 0.7F;

    public SlimeNecklace() {
        super(new Properties()
                .stacksTo(1)
                .tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA)
        );
    }

    @Override
    public WaveStyle getWaveStyle(ItemStack stack) {
        return WaveStyle.HOLY;
    }

    public static boolean isDamageFromSlime(DamageSource source) {
        Entity entity = source.getEntity();
        Entity direct = source.getDirectEntity();
        return entity instanceof Slime || direct instanceof Slime;
    }

    public static boolean shouldBlockSlimeDamage(Player player, DamageSource source) {
        if (!isDamageFromSlime(source)) return false;
        return hasSlimeNecklace(player);
    }

    @Override
    public void curioTick(SlotContext ctx, ItemStack stack) {
        LivingEntity entity = ctx.entity();
        if (!(entity instanceof Player player)) return;
        Level level = player.level;
        if (level.isClientSide) return;

        CompoundTag tag = stack.getOrCreateTag();
        ItemStack cannon = findCannon(player);

        tickSlimeAffinity(tag, player, level);

        if (cannon != null) {
            tickAutoAbsorb(tag, player, cannon, level);
        }

        if (cannon != null) {
            tickPassiveGen(tag, player, cannon);
        }

        if (cannon != null) {
            tickSlimeFusion(tag, player, cannon);
        }

        decrementCooldown(tag, TAG_SHIELD_CD);
        decrementCooldown(tag, TAG_TIME_FREEZE_CD);
        decrementCooldown(tag, TAG_REFLECT_CD);
        decrementCooldown(tag, TAG_RAIN_CD);

        if (cannon != null) {
            tickSlimeBuff(player, cannon);
        }

        if (cannon != null) {
            tickSlimeRain(tag, player, cannon, level);
        }

        if (cannon != null && level instanceof ServerLevel serverLevel) {
            if (player.tickCount % 10 == 0) {
                spawnAmbientParticles(serverLevel, player);
            }
        }
    }

    private void decrementCooldown(CompoundTag tag, String key) {
        int cd = tag.getInt(key);
        if (cd > 0) tag.putInt(key, cd - 1);
    }

    private void tickSlimeAffinity(CompoundTag tag, Player player, Level level) {
        if (player.tickCount % SLIME_AFFINITY_INTERVAL != 0) return;

        AABB area = player.getBoundingBox().inflate(SLIME_AFFINITY_RADIUS);
        List<Slime> slimes = level.getEntitiesOfClass(Slime.class, area, Slime::isAlive);

        for (Slime slime : slimes) {
            LivingEntity currentTarget = slime.getTarget();
            if (currentTarget instanceof Player targetPlayer) {
                if (hasSlimeNecklace(targetPlayer)) {
                    slime.setTarget(null);
                }
            }

            if (!TAMED_SLIMES.contains(slime.getUUID())) {
                removeSlimeHostileAI(slime);
                TAMED_SLIMES.add(slime.getUUID());
            }

            double dist = slime.distanceTo(player);
            if (dist > SLIME_FOLLOW_DISTANCE + 1.0) {
                Vec3 dir = player.position().subtract(slime.position()).normalize();
                double speed = 0.15 + slime.getSize() * 0.02;
                slime.setDeltaMovement(
                        dir.x * speed,
                        slime.getDeltaMovement().y,
                        dir.z * speed
                );

                if (slime.isOnGround() && level.random.nextInt(5) == 0) {
                    slime.setDeltaMovement(slime.getDeltaMovement().add(0, 0.42, 0));
                }
            } else if (dist < SLIME_FOLLOW_DISTANCE - 1.0) {
                Vec3 away = slime.position().subtract(player.position()).normalize();
                slime.setDeltaMovement(
                        away.x * 0.08,
                        slime.getDeltaMovement().y,
                        away.z * 0.08
                );
            }

            if (level instanceof ServerLevel serverLevel && level.random.nextInt(20) == 0) {
                WispParticleData heart = WispParticleData.wisp(0.3F, 0.2F, 1.0F, 0.5F, 0.8F);
                serverLevel.sendParticles(heart,
                        slime.getX(), slime.getY() + slime.getBbHeight() + 0.3, slime.getZ(),
                        2, 0.2, 0.1, 0.2, 0.01);
            }
        }
    }

    private void removeSlimeHostileAI(Slime slime) {
        try {
            Set<WrappedGoal> targetGoals = new HashSet<>(slime.targetSelector.getAvailableGoals());
            for (WrappedGoal wrappedGoal : targetGoals) {
                slime.targetSelector.removeGoal(wrappedGoal.getGoal());
            }

            Set<WrappedGoal> goals = new HashSet<>(slime.goalSelector.getAvailableGoals());
            for (WrappedGoal wrappedGoal : goals) {
                Goal goal = wrappedGoal.getGoal();
                String className = goal.getClass().getName();
                if (className.contains("Attack") || className.contains("attack")) {
                    slime.goalSelector.removeGoal(goal);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void tickAutoAbsorb(CompoundTag tag, Player player, ItemStack cannon, Level level) {
        int timer = tag.getInt(TAG_ABSORB_TIMER);
        if (timer > 0) {
            tag.putInt(TAG_ABSORB_TIMER, timer - 1);
            return;
        }

        AABB box = player.getBoundingBox().inflate(AUTO_ABSORB_RADIUS);
        List<Slime> slimes = level.getEntitiesOfClass(Slime.class, box,
                s -> s.isAlive() && !s.isInvisible());

        if (slimes.isEmpty()) return;

        slimes.sort((a, b) -> Integer.compare(b.getSize(), a.getSize()));
        Slime target = slimes.get(0);

        int size = target.getSize();
        int manaCost = (int) (SlimeCannon.MANA_PER_ABSORB * size * ABSORB_MANA_DISCOUNT);

        if (SlimeCannon.getMana(cannon) < manaCost) return;

        int clamped = Math.min(size, SlimeCannon.MAX_SLIME_SIZE);
        SlimeCannon.setSlimeCount(cannon, clamped,
                SlimeCannon.getSlimeCount(cannon, clamped) + 1);

        addCannonMana(cannon, -manaCost);
        target.discard();

        level.playSound(null, player.blockPosition(),
                SoundEvents.SLIME_SQUISH, SoundSource.PLAYERS,
                0.7F, 1.0F + size * 0.05F);

        if (level instanceof ServerLevel serverLevel) {
            WispParticleData data = WispParticleData.wisp(0.5F, 0.3F, 1.0F, 0.5F, 1.0F);
            serverLevel.sendParticles(data,
                    target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                    10, 0.3, 0.3, 0.3, 0.06);
        }

        player.displayClientMessage(
                new TranslatableComponent("message.slime_necklace.auto_absorbed", size)
                        .withStyle(ChatFormatting.GREEN),
                true);

        tag.putInt(TAG_ABSORB_TIMER, AUTO_ABSORB_INTERVAL);
    }

    private void tickPassiveGen(CompoundTag tag, Player player, ItemStack cannon) {
        int timer = tag.getInt(TAG_GEN_TIMER);
        timer++;

        if (timer >= PASSIVE_GEN_INTERVAL) {
            timer = 0;

            int currentSmall = SlimeCannon.getSlimeCount(cannon, 1);
            if (currentSmall >= PASSIVE_GEN_CAP) return;

            if (SlimeCannon.getMana(cannon) < PASSIVE_GEN_MANA_COST) return;

            addCannonMana(cannon, -PASSIVE_GEN_MANA_COST);
            SlimeCannon.setSlimeCount(cannon, 1, currentSmall + 1);

            player.displayClientMessage(
                    new TranslatableComponent("message.slime_necklace.condensed")
                            .withStyle(ChatFormatting.DARK_GREEN),
                    true);
        }

        tag.putInt(TAG_GEN_TIMER, timer);
    }


    private void tickSlimeBuff(Player player, ItemStack cannon) {
        int total = SlimeCannon.getTotalSlimeCount(cannon);
        if (total >= BUFF_THRESHOLD) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 0, true, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 100, 0, true, false, true));
        }
    }

    private void tickSlimeFusion(CompoundTag tag, Player player, ItemStack cannon) {
        int timer = tag.getInt(TAG_FUSION_TIMER);
        timer++;

        if (timer >= FUSION_INTERVAL) {
            timer = 0;

            for (int size = 1; size < SlimeCannon.MAX_SLIME_SIZE; size++) {
                int count = SlimeCannon.getSlimeCount(cannon, size);
                if (count >= FUSION_COST_COUNT) {
                    if (SlimeCannon.getMana(cannon) < FUSION_MANA_COST) break;

                    SlimeCannon.setSlimeCount(cannon, size, count - FUSION_COST_COUNT);
                    int nextSize = size + 1;
                    SlimeCannon.setSlimeCount(cannon, nextSize,
                            SlimeCannon.getSlimeCount(cannon, nextSize) + 1);
                    addCannonMana(cannon, -FUSION_MANA_COST);

                    player.displayClientMessage(
                            new TranslatableComponent("message.slime_necklace.fused", size, nextSize)
                                    .withStyle(ChatFormatting.AQUA),
                            true);

                    Level level = player.level;
                    level.playSound(null, player.blockPosition(),
                            SoundEvents.SLIME_SQUISH_SMALL, SoundSource.PLAYERS, 1.0F, 1.5F);

                    if (level instanceof ServerLevel serverLevel) {
                        WispParticleData fusion = WispParticleData.wisp(0.6F, 0.4F, 1.0F, 0.8F, 1.0F);
                        serverLevel.sendParticles(fusion,
                                player.getX(), player.getY() + 1.5, player.getZ(),
                                12, 0.5, 0.5, 0.5, 0.08);
                    }

                    break;
                }
            }
        }

        tag.putInt(TAG_FUSION_TIMER, timer);
    }

    public static boolean tryTimeFreeze(Player player, DamageSource source, float amount) {
        ItemStack necklace = findNecklaceInCurios(player);
        if (necklace == null || necklace.isEmpty()) return false;

        CompoundTag tag = necklace.getOrCreateTag();
        if (tag.getInt(TAG_TIME_FREEZE_CD) > 0) return false;

        float hpAfter = player.getHealth() - amount;
        float maxHp = player.getMaxHealth();
        if (hpAfter > maxHp * TIME_FREEZE_HP_TRIGGER) return false;
        if (hpAfter <= 0) return false;

        ItemStack cannon = findCannon(player);
        if (cannon == null) return false;

        int consumed = consumeSlimes(cannon, TIME_FREEZE_SLIME_COST);
        if (consumed < TIME_FREEZE_SLIME_COST) return false;

        tag.putInt(TAG_TIME_FREEZE_CD, TIME_FREEZE_COOLDOWN);

        Level level = player.level;

        AABB area = player.getBoundingBox().inflate(TIME_FREEZE_RADIUS);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive() && !(e instanceof Player));

        int frozenCount = 0;
        for (LivingEntity target : targets) {
            target.addEffect(new MobEffectInstance(ModEffects.ROOTED.get(), TIME_FREEZE_DURATION, 1, true, true, false));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, TIME_FREEZE_DURATION, 127, true, true, false));
            target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, TIME_FREEZE_DURATION, 5, true, true, false));
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, TIME_FREEZE_DURATION, 0, true, true, false));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, TIME_FREEZE_DURATION, 4, true, true, false));
            target.setDeltaMovement(Vec3.ZERO);
            frozenCount++;
        }

        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 3, true, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 80, 2, true, false, true));

        level.playSound(null, player.blockPosition(),
                SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 2.0F, 0.3F);
        level.playSound(null, player.blockPosition(),
                SoundEvents.SLIME_BLOCK_PLACE, SoundSource.PLAYERS, 2.0F, 0.1F);

        if (level instanceof ServerLevel serverLevel) {
            WispParticleData freeze = WispParticleData.wisp(0.8F, 0.5F, 0.8F, 1.0F, 1.0F);
            for (int ring = 1; ring <= (int) TIME_FREEZE_RADIUS; ring++) {
                int particles = ring * 8;
                for (int i = 0; i < particles; i++) {
                    double angle = 2 * Math.PI * i / particles;
                    double px = player.getX() + ring * Math.cos(angle);
                    double pz = player.getZ() + ring * Math.sin(angle);
                    serverLevel.sendParticles(freeze, px, player.getY() + 0.5, pz,
                            1, 0.05, 0.2, 0.05, 0.0);
                }
            }

            WispParticleData center = WispParticleData.wisp(1.5F, 0.8F, 0.9F, 1.0F, 1.0F);
            serverLevel.sendParticles(center,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    25, 0.3, 0.5, 0.3, 0.15);
        }

        player.displayClientMessage(
                new TranslatableComponent("message.slime_necklace.time_freeze", frozenCount)
                        .withStyle(ChatFormatting.LIGHT_PURPLE),
                true);

        return false;
    }

    public static boolean tryReflectProjectile(Player player, DamageSource source, float amount) {
        if (!source.isProjectile()) return false;

        ItemStack necklace = findNecklaceInCurios(player);
        if (necklace == null || necklace.isEmpty()) return false;

        CompoundTag tag = necklace.getOrCreateTag();
        if (tag.getInt(TAG_REFLECT_CD) > 0) return false;

        ItemStack cannon = findCannon(player);
        if (cannon != null && SlimeCannon.getMana(cannon) >= REFLECT_MANA_COST) {
            addCannonMana(cannon, -REFLECT_MANA_COST);
        } else {
            return false;
        }

        tag.putInt(TAG_REFLECT_CD, REFLECT_COOLDOWN);

        Entity directEntity = source.getDirectEntity();
        Entity sourceEntity = source.getEntity();

        if (directEntity != null) {
            Vec3 motion = directEntity.getDeltaMovement();
            directEntity.setDeltaMovement(motion.scale(-1.5));

            if (sourceEntity instanceof LivingEntity living && sourceEntity != player) {
                living.hurt(DamageSource.thorns(player), amount * REFLECT_DAMAGE_MULT);
            }
        }

        Level level = player.level;
        level.playSound(null, player.blockPosition(),
                SoundEvents.SLIME_BLOCK_STEP, SoundSource.PLAYERS, 1.5F, 2.0F);
        level.playSound(null, player.blockPosition(),
                SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0F, 1.5F);

        if (level instanceof ServerLevel serverLevel) {
            WispParticleData reflect = WispParticleData.wisp(0.7F, 0.3F, 1.0F, 0.3F, 1.0F);
            serverLevel.sendParticles(reflect,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    20, 0.8, 0.8, 0.8, 0.12);

            WispParticleData ripple = WispParticleData.wisp(0.4F, 0.6F, 1.0F, 0.6F, 0.8F);
            for (int i = 0; i < 12; i++) {
                double angle = 2 * Math.PI * i / 12;
                serverLevel.sendParticles(ripple,
                        player.getX() + Math.cos(angle) * 1.2,
                        player.getY() + 1.0,
                        player.getZ() + Math.sin(angle) * 1.2,
                        1, 0, 0, 0, 0);
            }
        }

        player.displayClientMessage(
                new TranslatableComponent("message.slime_necklace.reflected",
                        String.format("%.1f", amount * REFLECT_DAMAGE_MULT))
                        .withStyle(ChatFormatting.AQUA),
                true);

        return true;
    }

    public static boolean tryActivateSlimeRain(Player player) {
        ItemStack necklace = findNecklaceInCurios(player);
        if (necklace == null || necklace.isEmpty()) return false;

        CompoundTag tag = necklace.getOrCreateTag();
        if (tag.getInt(TAG_RAIN_CD) > 0) return false;
        if (tag.getBoolean(TAG_RAIN_ACTIVE)) return false;

        ItemStack cannon = findCannon(player);
        if (cannon == null) return false;

        if (SlimeCannon.getMana(cannon) < RAIN_MANA_COST) return false;
        if (SlimeCannon.getTotalSlimeCount(cannon) < RAIN_SLIME_COST) return false;

        addCannonMana(cannon, -RAIN_MANA_COST);
        consumeSlimes(cannon, RAIN_SLIME_COST);

        tag.putBoolean(TAG_RAIN_ACTIVE, true);
        tag.putInt(TAG_RAIN_TICK, 0);
        tag.putInt(TAG_RAIN_FIRED, 0);

        Level level = player.level;
        level.playSound(null, player.blockPosition(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 2.0F, 0.5F);

        player.displayClientMessage(
                new TranslatableComponent("message.slime_necklace.rain_activated")
                        .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD),
                true);

        return true;
    }

    private void tickSlimeRain(CompoundTag tag, Player player, ItemStack cannon, Level level) {
        if (!tag.getBoolean(TAG_RAIN_ACTIVE)) {
            if (player.isShiftKeyDown() && player.getXRot() < -60.0F) {
                int chargeTime = tag.getInt("RainChargeTime");
                chargeTime++;
                tag.putInt("RainChargeTime", chargeTime);

                if (chargeTime >= 60) {
                    tryActivateSlimeRain(player);
                    tag.putInt("RainChargeTime", 0);
                } else if (chargeTime % 20 == 0) {
                    player.displayClientMessage(
                            new TranslatableComponent("message.slime_necklace.rain_charging",
                                    3 - chargeTime / 20)
                                    .withStyle(ChatFormatting.YELLOW),
                            true);
                }
            } else {
                tag.putInt("RainChargeTime", 0);
            }
            return;
        }

        int tick = tag.getInt(TAG_RAIN_TICK);
        int fired = tag.getInt(TAG_RAIN_FIRED);

        tick++;
        tag.putInt(TAG_RAIN_TICK, tick);

        if (tick % 3 == 0 && fired < RAIN_PROJECTILE_COUNT) {
            spawnRainProjectile(player, level);
            tag.putInt(TAG_RAIN_FIRED, fired + 1);

            if (level instanceof ServerLevel serverLevel) {
                WispParticleData rain = WispParticleData.wisp(0.4F, 0.2F, 0.8F, 0.3F, 0.9F);
                double rx = player.getX() + (level.random.nextDouble() - 0.5) * 10;
                double rz = player.getZ() + (level.random.nextDouble() - 0.5) * 10;
                serverLevel.sendParticles(rain, rx, player.getY() + 15, rz,
                        3, 0.5, 0.2, 0.5, 0.02);
            }
        }

        if (fired >= RAIN_PROJECTILE_COUNT) {
            tag.putBoolean(TAG_RAIN_ACTIVE, false);
            tag.putInt(TAG_RAIN_CD, RAIN_COOLDOWN);
            tag.putInt(TAG_RAIN_TICK, 0);
            tag.putInt(TAG_RAIN_FIRED, 0);

            level.playSound(null, player.blockPosition(),
                    SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.5F, 1.2F);

            player.displayClientMessage(
                    new TranslatableComponent("message.slime_necklace.rain_complete")
                            .withStyle(ChatFormatting.GREEN),
                    true);
        }
    }

    private void spawnRainProjectile(Player player, Level level) {
        double ox = (level.random.nextDouble() - 0.5) * 16.0;
        double oz = (level.random.nextDouble() - 0.5) * 16.0;
        double oy = 12.0 + level.random.nextDouble() * 4.0;

        EntitySlimeCannonBall ball =
                new EntitySlimeCannonBall(level, player, RAIN_DAMAGE_PER_BALL, 2 + level.random.nextInt(3));

        ball.setPos(player.getX() + ox, player.getY() + oy, player.getZ() + oz);

        double vx = (level.random.nextDouble() - 0.5) * 0.3;
        double vz = (level.random.nextDouble() - 0.5) * 0.3;
        ball.setDeltaMovement(vx, -1.5 - level.random.nextDouble() * 0.5, vz);

        level.addFreshEntity(ball);

        level.playSound(null, new BlockPos(ball.getX(), ball.getY(), ball.getZ()),
                SoundEvents.SLIME_SQUISH_SMALL, SoundSource.PLAYERS,
                0.5F, 0.8F + level.random.nextFloat() * 0.4F);
    }

    public static boolean trySlimeShield(Player player, DamageSource source, float amount) {
        ItemStack necklace = findNecklaceInCurios(player);
        if (necklace == null || necklace.isEmpty()) return false;

        CompoundTag tag = necklace.getOrCreateTag();
        if (tag.getInt(TAG_SHIELD_CD) > 0) return false;

        if (player.getHealth() - amount > 0) return false;

        ItemStack cannon = findCannon(player);
        if (cannon == null) return false;

        int consumed = consumeSlimes(cannon, SHIELD_SLIME_COST);
        if (consumed < SHIELD_SLIME_COST) return false;

        tag.putInt(TAG_SHIELD_CD, SHIELD_COOLDOWN);

        player.setHealth(Math.max(player.getHealth(), 4.0F));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 2, true, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 1, true, false, true));

        Level level = player.level;
        level.playSound(null, player.blockPosition(),
                SoundEvents.SLIME_BLOCK_BREAK, SoundSource.PLAYERS, 1.5F, 0.5F);

        if (level instanceof ServerLevel serverLevel) {
            WispParticleData shield = WispParticleData.wisp(1.0F, 0.3F, 1.0F, 0.3F, 1.0F);
            serverLevel.sendParticles(shield,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    30, 1.0, 1.0, 1.0, 0.1);
        }

        player.displayClientMessage(
                new TranslatableComponent("message.slime_necklace.shield_triggered")
                        .withStyle(ChatFormatting.LIGHT_PURPLE),
                true);

        return true;
    }

    private static int consumeSlimes(ItemStack cannon, int count) {
        int consumed = 0;
        for (int size = 1; size <= SlimeCannon.MAX_SLIME_SIZE && consumed < count; size++) {
            int available = SlimeCannon.getSlimeCount(cannon, size);
            int take = Math.min(available, count - consumed);
            if (take > 0) {
                SlimeCannon.setSlimeCount(cannon, size, available - take);
                consumed += take;
            }
        }
        return consumed;
    }

    private static ItemStack findCannon(Player player) {
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof SlimeCannon) return main;
        ItemStack off = player.getOffhandItem();
        if (off.getItem() instanceof SlimeCannon) return off;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack slot = player.getInventory().getItem(i);
            if (slot.getItem() instanceof SlimeCannon) return slot;
        }
        return null;
    }

    public static boolean hasSlimeNecklace(Player player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, stack -> stack.getItem() instanceof SlimeNecklace)
                .isPresent();
    }

    @Nullable
    private static ItemStack findNecklaceInCurios(Player player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, stack -> stack.getItem() instanceof SlimeNecklace)
                .map(SlotResult::stack)
                .orElse(null);
    }

    private static void addCannonMana(ItemStack cannon, int amount) {
        cannon.getCapability(vazkii.botania.api.BotaniaForgeCapabilities.MANA_ITEM)
                .ifPresent(m -> m.addMana(amount));
    }

    private void spawnAmbientParticles(ServerLevel serverLevel, Player player) {
        double angle1 = (player.tickCount % 40) / 40.0 * 2 * Math.PI;
        double angle2 = angle1 + Math.PI;
        double r = 0.8;

        WispParticleData wisp1 = WispParticleData.wisp(0.2F, 0.2F, 0.9F, 0.4F, 0.6F);
        serverLevel.sendParticles(wisp1,
                player.getX() + Math.cos(angle1) * r,
                player.getY() + 1.2,
                player.getZ() + Math.sin(angle1) * r,
                1, 0, 0, 0, 0);

        WispParticleData wisp2 = WispParticleData.wisp(0.2F, 0.4F, 1.0F, 0.6F, 0.6F);
        serverLevel.sendParticles(wisp2,
                player.getX() + Math.cos(angle2) * r,
                player.getY() + 1.2,
                player.getZ() + Math.sin(angle2) * r,
                1, 0, 0, 0, 0);
    }

    @Override
    public boolean canEquip(SlotContext ctx, ItemStack stack) {
        return true;
    }

    @Override
    public boolean canUnequip(SlotContext ctx, ItemStack stack) {
        return true;
    }

    @Override
    public void onUnequip(SlotContext ctx, ItemStack newStack, ItemStack stack) {
        TAMED_SLIMES.clear();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(new TranslatableComponent("tooltip.slime_necklace.title")
                .withStyle(ChatFormatting.GREEN));
        tooltip.add(new TranslatableComponent(""));

        tooltip.add(new TranslatableComponent("tooltip.slime_necklace.affinity")
                .withStyle(ChatFormatting.GREEN));
        tooltip.add(new TranslatableComponent("tooltip.slime_necklace.affinity_desc",
                String.format("%.0f", SLIME_AFFINITY_RADIUS),
                String.format("%.0f", SLIME_FOLLOW_DISTANCE))
                .withStyle(ChatFormatting.GRAY));

        tooltip.add(new TranslatableComponent("tooltip.slime_necklace.auto_absorb",
                String.format("%.0f", AUTO_ABSORB_RADIUS))
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(new TranslatableComponent("tooltip.slime_necklace.auto_absorb_desc")
                .withStyle(ChatFormatting.GRAY));

        tooltip.add(new TranslatableComponent("tooltip.slime_necklace.absorb_enhance",
                String.format("%.0f%%", (1 - ABSORB_MANA_DISCOUNT) * 100),
                String.format("%.0f%%", (1 - ABSORB_COOLDOWN_MULTIPLIER) * 100))
                .withStyle(ChatFormatting.AQUA));

        tooltip.add(new TranslatableComponent("tooltip.slime_necklace.passive_gen",
                PASSIVE_GEN_INTERVAL / 20, PASSIVE_GEN_MANA_COST, PASSIVE_GEN_CAP)
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(new TranslatableComponent("tooltip.slime_necklace.passive_gen_desc")
                .withStyle(ChatFormatting.GRAY));

        tooltip.add(new TranslatableComponent("tooltip.slime_necklace.fusion",
                FUSION_COST_COUNT, FUSION_MANA_COST)
                .withStyle(ChatFormatting.BLUE));
        tooltip.add(new TranslatableComponent("tooltip.slime_necklace.fusion_desc",
                FUSION_INTERVAL / 20)
                .withStyle(ChatFormatting.GRAY));

        tooltip.add(new TranslatableComponent("tooltip.slime_necklace.shield",
                SHIELD_SLIME_COST, SHIELD_COOLDOWN / 20)
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(new TranslatableComponent("tooltip.slime_necklace.shield_desc")
                .withStyle(ChatFormatting.GRAY));

        tooltip.add(new TranslatableComponent("tooltip.slime_necklace.time_freeze",
                String.format("%.0f", TIME_FREEZE_RADIUS),
                TIME_FREEZE_DURATION / 20,
                TIME_FREEZE_SLIME_COST)
                .withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(new TranslatableComponent("tooltip.slime_necklace.time_freeze_desc",
                String.format("%.0f%%", TIME_FREEZE_HP_TRIGGER * 100),
                TIME_FREEZE_COOLDOWN / 20)
                .withStyle(ChatFormatting.GRAY));

        tooltip.add(new TranslatableComponent("tooltip.slime_necklace.reflect",
                String.format("%.0f%%", REFLECT_DAMAGE_MULT * 100),
                REFLECT_MANA_COST)
                .withStyle(ChatFormatting.DARK_AQUA));
        tooltip.add(new TranslatableComponent("tooltip.slime_necklace.reflect_desc",
                REFLECT_COOLDOWN / 20)
                .withStyle(ChatFormatting.GRAY));

        tooltip.add(new TranslatableComponent("tooltip.slime_necklace.rain",
                RAIN_PROJECTILE_COUNT, RAIN_SLIME_COST, RAIN_MANA_COST)
                .withStyle(ChatFormatting.RED));
        tooltip.add(new TranslatableComponent("tooltip.slime_necklace.rain_desc",
                RAIN_COOLDOWN / 20)
                .withStyle(ChatFormatting.GRAY));

        tooltip.add(new TranslatableComponent("tooltip.slime_necklace.buff",
                BUFF_THRESHOLD)
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(new TranslatableComponent("tooltip.slime_necklace.buff_desc")
                .withStyle(ChatFormatting.GRAY));

        tooltip.add(new TranslatableComponent(""));

        CompoundTag tag = stack.getOrCreateTag();
        addCooldownLine(tooltip, tag, TAG_SHIELD_CD, "tooltip.slime_necklace.shield_status");
        addCooldownLine(tooltip, tag, TAG_TIME_FREEZE_CD, "tooltip.slime_necklace.freeze_status");
        addCooldownLine(tooltip, tag, TAG_REFLECT_CD, "tooltip.slime_necklace.reflect_status");
        addCooldownLine(tooltip, tag, TAG_RAIN_CD, "tooltip.slime_necklace.rain_status");

        if (tag.getBoolean(TAG_RAIN_ACTIVE)) {
            int fired = tag.getInt(TAG_RAIN_FIRED);
            tooltip.add(new TranslatableComponent("tooltip.slime_necklace.rain_active",
                    fired, RAIN_PROJECTILE_COUNT)
                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        }

        tooltip.add(new TranslatableComponent(""));
        tooltip.add(new TranslatableComponent("tooltip.slime_necklace.hint_rain")
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(new TranslatableComponent("tooltip.bad_human")
                .withStyle(StyleMarker.glitch()));
    }

    private void addCooldownLine(List<Component> tooltip, CompoundTag tag, String key, String translationKey) {
        int cd = tag.getInt(key);
        if (cd > 0) {
            tooltip.add(new TranslatableComponent(translationKey + "_cd",
                    String.format("%.1f", cd / 20.0))
                    .withStyle(ChatFormatting.RED));
        } else {
            tooltip.add(new TranslatableComponent(translationKey + "_ready")
                    .withStyle(ChatFormatting.GREEN));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        return tag.getBoolean(TAG_RAIN_ACTIVE) || tag.getInt(TAG_SHIELD_CD) <= 0;
    }
}