package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.xiaoyang010.ex_enigmaticlegacy.api.EXEAPI;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.internal.IManaBurst;
import vazkii.botania.api.mana.BurstProperties;
import vazkii.botania.api.mana.ILensEffect;
import vazkii.botania.api.mana.IManaItem;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.client.fx.WispParticleData;
import vazkii.botania.client.gui.TooltipHandler;
import vazkii.botania.common.entity.EntityManaBurst;
import vazkii.botania.common.handler.ModSounds;

import java.util.List;
import java.util.UUID;

public class NaturalThousandBlades extends SwordItem implements IManaItem, ILensEffect {
    private static final String NBT_MANA = "Mana";
    private static final String NBT_LEVEL = "Level";
    private static final String NBT_TICK = "tick";
    private static final String NBT_BLOOM_ACTIVE = "BloomActive";
    private static final String NBT_NATURE_FURY_COOLDOWN = "NatureFuryCooldown";
    private static final int[] CREATIVE_MANA = new int[]{0, 10000, 1000000, 10000000, 100000000, 1000000000, 2147483646};
    private static final int MAX_MANA = 2147483646;
    private static final int SHIELD_COST = 1000;
    private static final int BLOOM_COST = 50000;
    private static final int NATURE_FURY_COST = 5000;

    public NaturalThousandBlades(Properties properties) {
        super(EXEAPI.MIRACLE_ITEM_TIER, 3, -2.4F, properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slotId, boolean isSelected) {
        if (entity instanceof Player player) {
            handleManaAbsorption(stack, world, player);
            handlePlantShield(stack, world, player);
            handleBloomEffects(stack, world, player);
            handleParticleEffects(stack, world, entity, isSelected);
            updateCooldowns(stack);
        }
    }

    private void updateCooldowns(ItemStack stack) {
        int cooldown = stack.getOrCreateTag().getInt(NBT_NATURE_FURY_COOLDOWN);
        if (cooldown > 0) {
            stack.getOrCreateTag().putInt(NBT_NATURE_FURY_COOLDOWN, cooldown - 1);
        }
    }

    private void handleManaAbsorption(ItemStack stack, Level world, Player player) {
        BlockPos pos = player.getOnPos();
        if (getLevel(stack) < 6 && getManaTag(stack) >= CREATIVE_MANA[getLevel(stack) + 1]) {
            setLevel(stack, getLevel(stack) + 1);
            world.playSound(player, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.0f);
            if (world.isClientSide) {
                player.displayClientMessage(new TranslatableComponent("message.mana_full", getLevel(stack)), false);
            }
        }

        for (BlockPos blockPos : BlockPos.betweenClosed(pos.offset(-2, 0, -2), pos.offset(2, 1, 2))) {
            BlockEntity blockEntity = world.getBlockEntity(blockPos);
            if (blockEntity instanceof IManaPool pool) {
                int mana = pool.getCurrentMana();
                int manaTag = getManaTag(stack);
                int level = getLevel(stack);
                int addMana = 1024 * (level + 1);
                if (mana > addMana && manaTag < MAX_MANA) {
                    int actualAddMana = Math.min(addMana, MAX_MANA - manaTag);
                    if (actualAddMana > 0) {
                        setManaTag(stack, manaTag + actualAddMana);
                        pool.receiveMana(-actualAddMana);
                    }
                }
            }
        }
    }

    private void handlePlantShield(ItemStack stack, Level world, Player player) {
        if (getLevel(stack) >= 4 && getManaTag(stack) >= SHIELD_COST) {
            if (player.tickCount % 20 == 0) {
                List<LivingEntity> nearbyEntities = world.getEntitiesOfClass(
                        LivingEntity.class,
                        player.getBoundingBox().inflate(3.0D),
                        e -> e != player
                );

                if (!nearbyEntities.isEmpty()) {
                    setManaTag(stack, getManaTag(stack) - SHIELD_COST);
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 25, 1));
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 25, 0));

                    if (world.isClientSide) {
                        spawnShieldParticles(world, player);
                    }
                }
            }
        }
    }

    private void handleBloomEffects(ItemStack stack, Level world, Player player) {
        if (stack.getOrCreateTag().getBoolean(NBT_BLOOM_ACTIVE)) {
            if (player.tickCount % 5 == 0 && world.isClientSide) {
                spawnBloomParticles(world, player);
            }

            if (player.tickCount % 20 == 0) {
                // 空间绽放持续伤害效果
                List<LivingEntity> nearbyEntities = world.getEntitiesOfClass(
                        LivingEntity.class,
                        player.getBoundingBox().inflate(5.0D),
                        e -> e != player
                );

                for (LivingEntity target : nearbyEntities) {
                    target.hurt(DamageSource.MAGIC, 4.0f);
                }
            }
        }
    }

    private void spawnShieldParticles(Level world, Player player) {
        for (int i = 0; i < 12; i++) {
            double angle = i * (Math.PI * 2) / 12;
            double x = player.getX() + Math.cos(angle) * 2;
            double z = player.getZ() + Math.sin(angle) * 2;

            WispParticleData wisp = WispParticleData.wisp(
                    0.5F, 0.2F, 0.8F, 0.2F, 1.0F
            );
            world.addParticle(wisp, x, player.getY() + 1, z, 0, 0.05, 0);
        }
    }

    private void spawnBloomParticles(Level world, Player player) {
        for (int i = 0; i < 5; i++) {
            double x = player.getX() + (Math.random() - 0.5) * 4;
            double y = player.getY() + Math.random() * 2;
            double z = player.getZ() + (Math.random() - 0.5) * 4;

            SparkleParticleData sparkle = SparkleParticleData.sparkle(
                    1.5F,
                    (float) Math.random(),
                    (float) Math.random(),
                    (float) Math.random(),
                    5
            );
            world.addParticle(sparkle, x, y, z, 0, 0.05, 0);
        }
    }

    private void handleParticleEffects(ItemStack stack, Level world, Entity entity, boolean isSelected) {
        int tick = stack.getOrCreateTag().getInt(NBT_TICK);
        if (!world.isClientSide) {
            if (tick > 0) {
                stack.getOrCreateTag().putInt(NBT_TICK, tick - 1);
            }
        } else if (tick > 26 && isSelected) {
            for (int i = 0; i < 14; ++i) {
                float r = world.random.nextBoolean() ? 0.88235295F : 0.39607844F;
                float g = world.random.nextBoolean() ? 0.2627451F : 0.81960785F;
                float b = world.random.nextBoolean() ? 0.9411765F : 0.88235295F;

                SparkleParticleData sparkle = SparkleParticleData.sparkle(
                        1.8F * (float) (Math.random() - 0.5F),
                        r + (float) (Math.random() / 4.0F - 0.125F),
                        g + (float) (Math.random() / 4.0F - 0.125F),
                        b + (float) (Math.random() / 4.0F - 0.125F),
                        3
                );

                world.addParticle(sparkle,
                        entity.getX() + (Math.random() - 0.5F),
                        entity.getY() + (Math.random() - 0.5F) * 2.0F - 0.5F,
                        entity.getZ() + (Math.random() - 0.5F),
                        0, 0, 0
                );
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag isadvanced) {
        int lv = getLevel(stack);
        components.add(new TranslatableComponent("info.ex_enigmaticlegacy.natural_thousand_blades.lv", lv)
                .withStyle(ChatFormatting.GOLD));
        components.add(new TranslatableComponent("info.ex_enigmaticlegacy.natural_thousand_blades.mana", getManaTag(stack))
                .withStyle(ChatFormatting.AQUA));

        if (stack.getOrCreateTag().getBoolean(NBT_BLOOM_ACTIVE)) {
            components.add(new TranslatableComponent("info.ex_enigmaticlegacy.natural_thousand_blades.bloom_active")
                    .withStyle(ChatFormatting.YELLOW));
        }

        TooltipHandler.addOnShift(components, () -> {
            components.add(new TranslatableComponent("ex_enigmaticlegacy.swordInfo.1")
                    .withStyle(lv >= 1 ? ChatFormatting.GREEN : ChatFormatting.GRAY));
            components.add(new TranslatableComponent("ex_enigmaticlegacy.swordInfo.2")
                    .withStyle(lv >= 3 ? ChatFormatting.GREEN : ChatFormatting.GRAY));
            components.add(new TranslatableComponent("ex_enigmaticlegacy.swordInfo.3")
                    .withStyle(lv >= 4 ? ChatFormatting.GREEN : ChatFormatting.GRAY));
            components.add(new TranslatableComponent("ex_enigmaticlegacy.swordInfo.4")
                    .withStyle(lv >= 5 ? ChatFormatting.GREEN : ChatFormatting.GRAY));
            components.add(new TranslatableComponent("ex_enigmaticlegacy.swordInfo.5")
                    .withStyle(lv >= 6 ? ChatFormatting.GREEN : ChatFormatting.GRAY));
        });
    }

    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> list) {
        if (this.allowdedIn(tab)) {
            for (int level = 0; level < 7; level++) {
                ItemStack stack = new ItemStack(this);
                setLevel(stack, level);
                setManaTag(stack, CREATIVE_MANA[level]);
                list.add(stack);
            }
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player && !player.level.isClientSide) {
            handleAreaAttack(stack, target, player);
            handleElementalEffects(stack, target, player);
            handleNatureFury(stack, target, player);
            handleManaBurst(stack, player);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    private void handleAreaAttack(ItemStack stack, LivingEntity target, Player player) {
        if (getLevel(stack) >= 3) {
            float size = getLevel(stack) >= 4 ? (getLevel(stack) >= 5 ? 3.5F : 2.5F) : 1.5F;
            AABB aabb = target.getBoundingBox().inflate(size, 1.7F, size);

            for (LivingEntity living : player.level.getEntitiesOfClass(LivingEntity.class, aabb)) {
                if (living.isAlive() && living != player) {
                    living.hurt(DamageSource.playerAttack(player), getSwordDamage(stack));
                }
            }
        }
    }

    private void handleElementalEffects(ItemStack stack, LivingEntity target, Player player) {
        int level = getLevel(stack);
        if (level >= 5) {
            float chance = 0.3f + (level * 0.1f);
            if (player.level.random.nextFloat() < chance) {
                switch (player.level.random.nextInt(6)) {
                    case 0: // 火焰
                        target.setSecondsOnFire(5);
                        break;
                    case 1: // 凋零
                        target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
                        break;
                    case 2: // 虚弱
                        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
                        break;
                    case 3: // 缓慢
                        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
                        break;
                    case 4: // 中毒
                        target.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
                        break;
                    case 5: // 失明
                        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                        break;
                }
            }
        }
    }

    private void handleNatureFury(ItemStack stack, LivingEntity target, Player player) {
        if (getLevel(stack) >= 6 && getManaTag(stack) >= NATURE_FURY_COST) {
            int cooldown = stack.getOrCreateTag().getInt(NBT_NATURE_FURY_COOLDOWN);
            if (cooldown <= 0 && player.level.random.nextFloat() < 0.3f) {
                spawnNaturesFury(player.level, target, player);
                setManaTag(stack, getManaTag(stack) - NATURE_FURY_COST);
                stack.getOrCreateTag().putInt(NBT_NATURE_FURY_COOLDOWN, 100); // 5秒冷却
            }
        }
    }

    private void handleManaBurst(ItemStack stack, Player player) {
        if (getLevel(stack) >= 1 && getManaTag(stack) >= 120) {
            trySpawnBurst(player, 1);
            setManaTag(stack, getManaTag(stack) - 120);
        }
    }

    private void spawnNaturesFury(Level world, LivingEntity target, Player player) {
        // 藤蔓缠绕效果
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
        target.hurt(DamageSource.MAGIC, 20.0f);

        if (world.isClientSide) {
            for (int i = 0; i < 30; i++) {
                double x = target.getX() + (Math.random() - 0.5) * 2;
                double y = target.getY() + Math.random() * 2;
                double z = target.getZ() + (Math.random() - 0.5) * 2;

                WispParticleData wisp = WispParticleData.wisp(
                        1.0F, 0.1F, 0.8F, 0.1F, 1.0F
                );
                world.addParticle(wisp, x, y, z,
                        (Math.random() - 0.5) * 0.1,
                        Math.random() * 0.1,
                        (Math.random() - 0.5) * 0.1
                );
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isCrouching()) {
            if (getLevel(stack) >= 6 && getManaTag(stack) >= BLOOM_COST) {
                activateSpaceBloom(stack, level, player);
                return InteractionResultHolder.success(stack);
            }
        } else {
            return handleTeleport(stack, level, player);
        }

        return InteractionResultHolder.pass(stack);
    }

    private InteractionResultHolder<ItemStack> handleTeleport(ItemStack stack, Level level, Player player) {
        Vec3 scale = player.getLookAngle().scale(3.0d);
        Vec3 startPos = player.position();
        Vec3 endPos = startPos.add(scale);
        stack.getOrCreateTag().putInt(NBT_TICK, 36);

        if (level.isClientSide) {
            spawnTeleportParticles(level, startPos, endPos, player);
        }

        level.playSound(player, player.getOnPos(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
        player.setDeltaMovement(scale);
        player.getCooldowns().addCooldown(stack.getItem(), 20);

        return InteractionResultHolder.success(stack);
    }

    private void spawnTeleportParticles(Level level, Vec3 startPos, Vec3 endPos, Player player) {
        int particleCount = 15;
        for (int i = 0; i < particleCount; i++) {
            double progress = i / (double) particleCount;
            double x = startPos.x + (endPos.x - startPos.x) * progress;
            double y = startPos.y + (endPos.y - startPos.y) * progress;
            double z = startPos.z + (endPos.z - startPos.z) * progress;

            for (int j = 0; j < 8; j++) {
                float r = level.random.nextBoolean() ? 0.88235295F : 0.39607844F;
                float g = level.random.nextBoolean() ? 0.2627451F : 0.81960785F;
                float b = level.random.nextBoolean() ? 0.9411765F : 0.88235295F;

                double offsetX = (Math.random() - 0.5F) * 0.3;
                double offsetY = (Math.random() - 0.5F) * 0.3;
                double offsetZ = (Math.random() - 0.5F) * 0.3;

                float colorVariation = (float) (Math.random() / 4.0F - 0.125F);
                r += colorVariation;
                g += colorVariation;
                b += colorVariation;

                SparkleParticleData sparkle = SparkleParticleData.sparkle(
                        1.8F * (float) (Math.random() - 0.5F),
                        r, g, b,
                        3
                );
                level.addParticle(sparkle,
                        x + offsetX,
                        y + 1.0 + offsetY,
                        z + offsetZ,
                        0, 0, 0
                );

                WispParticleData wisp = WispParticleData.wisp(
                        0.3f * (float) (Math.random() + 0.5f),
                        r, g, b,
                        1.0f
                );
                level.addParticle(wisp,
                        x + offsetX,
                        y + 1.0 + offsetY,
                        z + offsetZ,
                        (Math.random() - 0.5D) * 0.02D,
                        (Math.random() - 0.5D) * 0.01D,
                        (Math.random() - 0.5D) * 0.02D
                );
            }
        }
    }

    private void activateSpaceBloom(ItemStack stack, Level level, Player player) {
        stack.getOrCreateTag().putBoolean(NBT_BLOOM_ACTIVE, true);
        setManaTag(stack, getManaTag(stack) - BLOOM_COST);

        // 增益效果
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 2));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 2));
        player.addEffect(new MobEffectInstance(MobEffects.JUMP, 200, 1));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 1));

        if (level.isClientSide) {
            for (int i = 0; i < 50; i++) {
                double x = player.getX() + (Math.random() - 0.5) * 4;
                double y = player.getY() + Math.random() * 3;
                double z = player.getZ() + (Math.random() - 0.5) * 4;

                WispParticleData wisp = WispParticleData.wisp(
                        2.0F,
                        level.random.nextFloat(),
                        level.random.nextFloat(),
                        level.random.nextFloat(),
                        1.0F
                );
                level.addParticle(wisp, x, y, z, 0, 0.1, 0);
            }
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.terraBlade, SoundSource.PLAYERS, 1.0F, 0.5F);
    }

    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int remainingUseDuration) {
        if (living instanceof Player player && !level.isClientSide &&
                player.getAttackStrengthScale(0.0F) == 1.0F && getLevel(stack) >= 1 && getManaTag(stack) >= 120) {
            trySpawnBurst(player, 1);
            setManaTag(stack, getManaTag(stack) - 120);
        }
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        if (entity instanceof Player player && !entity.level.isClientSide &&
                getLevel(stack) >= 1 && getManaTag(stack) >= 120) {
            trySpawnBurst(player, 1);
            setManaTag(stack, getManaTag(stack) - 120);
            return false;
        }
        return false;
    }

    // 基础能力值计算
    private float calculateBaseDamage(ItemStack stack) {
        int level = getLevel(stack);
        float baseDamage = getTier().getAttackDamageBonus();
        return 5.0F + baseDamage + (level * level * level * 10 + level * 10);
    }

    // 魔力暴击计算
    private float calculateManaCritical(ItemStack stack, float baseDamage) {
        float manaPercent = (float) getManaTag(stack) / MAX_MANA;
        float critChance = manaPercent * 0.5f; // 最高50%暴击率
        float critDamage = 1.5f + (manaPercent * 1.5f); // 暴击伤害1.5x-3x

        if (stack.getOrCreateTag().getBoolean(NBT_BLOOM_ACTIVE)) {
            critChance += 0.2f; // 空间绽放状态下额外20%暴击率
            critDamage += 0.5f; // 空间绽放状态下额外50%暴击伤害
        }

        if (Math.random() < critChance) {
            return baseDamage * critDamage;
        }
        return baseDamage;
    }

    private float getSwordDamage(ItemStack stack) {
        float baseDamage = calculateBaseDamage(stack);
        return calculateManaCritical(stack, baseDamage);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> multimap = HashMultimap.create();
        if (slot == EquipmentSlot.MAINHAND) {
            multimap.put(Attributes.ATTACK_DAMAGE,
                    new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", getSwordDamage(stack), AttributeModifier.Operation.ADDITION));
            multimap.put(Attributes.MOVEMENT_SPEED,
                    new AttributeModifier(UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3"), "Weapon speed", 0.15F, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
        return multimap;
    }

    public static void trySpawnBurst(Player player, int count) {
        for (int i = 0; i < count; i++) {
            EntityManaBurst burst = getBurst(player, player.getMainHandItem());
            player.level.addFreshEntity(burst);
            player.getMainHandItem().hurtAndBreak(1, player, (p) -> {
                p.broadcastBreakEvent(InteractionHand.MAIN_HAND);
            });
            player.level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.terraBlade, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    public static void trySpawnDirectionalBurst(Player player, float angle) {
        EntityManaBurst burst = getDirectionalBurst(player, player.getMainHandItem(), angle);
        player.level.addFreshEntity(burst);
        player.level.playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.terraBlade, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    public static EntityManaBurst getDirectionalBurst(Player player, ItemStack stack, float angle) {
        EntityManaBurst burst = new EntityManaBurst(player);

        double rad = Math.toRadians(angle);
        double dx = Math.cos(rad);
        double dz = Math.sin(rad);

        float motionModifier = 7.0F;
        burst.setColor(0x21D4FA);
        burst.setMana(1024);
        burst.setStartingMana(1024);
        burst.setMinManaLoss(40);
        burst.setManaLossPerTick(4.0F);
        burst.setGravity(0.0F);
        burst.setDeltaMovement(dx * motionModifier, 0, dz * motionModifier);
        burst.setSourceLens(stack.copy());
        return burst;
    }

    public static EntityManaBurst getBurst(Player player, ItemStack stack) {
        EntityManaBurst burst = new EntityManaBurst(player);
        float motionModifier = 7.0F;
        burst.setColor(0x21D4FA);
        burst.setMana(1024);
        burst.setStartingMana(1024);
        burst.setMinManaLoss(40);
        burst.setManaLossPerTick(4.0F);
        burst.setGravity(0.0F);
        burst.setDeltaMovement(burst.getDeltaMovement().scale(motionModifier));
        burst.setSourceLens(stack.copy());
        return burst;
    }

    public int getManaTag(ItemStack stack) {
        return stack.getOrCreateTag().getInt(NBT_MANA);
    }

    public void setManaTag(ItemStack stack, int mana) {
        stack.getOrCreateTag().putInt(NBT_MANA, mana);
    }

    public int getLevel(ItemStack stack) {
        return stack.getOrCreateTag().getInt(NBT_LEVEL);
    }

    public void setLevel(ItemStack stack, int level) {
        stack.getOrCreateTag().putInt(NBT_LEVEL, level);
    }

    @Override
    public int getMana() {
        return getManaTag(new ItemStack(this));
    }

    @Override
    public int getMaxMana() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void addMana(int i) {
    }

    @Override
    public boolean canReceiveManaFromPool(BlockEntity blockEntity) {
        return true;
    }

    @Override
    public boolean canReceiveManaFromItem(ItemStack itemStack) {
        return false;
    }

    @Override
    public boolean canExportManaToPool(BlockEntity blockEntity) {
        return false;
    }

    @Override
    public boolean canExportManaToItem(ItemStack itemStack) {
        return false;
    }

    @Override
    public boolean isNoExport() {
        return false;
    }

    @Override
    public void apply(ItemStack itemStack, BurstProperties burstProperties, Level level) {
    }

    @Override
    public boolean collideBurst(IManaBurst iManaBurst, HitResult hitResult, boolean b, boolean b1, ItemStack itemStack) {
        return b1;
    }

    @Override
    public void updateBurst(IManaBurst burst, ItemStack itemStack) {
        ThrowableProjectile entity = burst.entity();
        AABB axis = (new AABB(entity.getX(), entity.getY(), entity.getZ(), entity.xOld, entity.yOld, entity.zOld)).inflate(1.0);
        List<LivingEntity> entities = entity.level.getEntitiesOfClass(LivingEntity.class, axis);
        Entity thrower = entity.getOwner();

        for (LivingEntity living : entities) {
            if (living != thrower) {
                if (living instanceof Player livingPlayer) {
                    if (thrower instanceof Player throwingPlayer) {
                        if (!throwingPlayer.canHarmPlayer(livingPlayer)) {
                            continue;
                        }
                    }
                }

                if (living.hurtTime == 0) {
                    int cost = 33;
                    int mana = burst.getMana();
                    if (mana >= cost) {
                        burst.setMana(mana - cost);
                        float damage = getSwordDamage(itemStack);
                        if (!burst.isFake() && !entity.level.isClientSide) {
                            DamageSource source = DamageSource.MAGIC;
                            if (thrower instanceof Player player) {
                                source = DamageSource.playerAttack(player).bypassArmor().bypassInvul();
                            } else if (thrower instanceof LivingEntity livingEntity) {
                                source = DamageSource.mobAttack(livingEntity).bypassArmor().bypassInvul();
                            }

                            living.hurt(source, damage);
                            entity.discard();
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean doParticles(IManaBurst iManaBurst, ItemStack itemStack) {
        return true;
    }
}