package org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.xiaoyang.ex_enigmaticlegacy.api.EXEAPI;
import vazkii.botania.api.internal.ManaBurst;
import vazkii.botania.api.mana.BurstProperties;
import vazkii.botania.api.mana.LensEffectItem;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.api.mana.ManaPool;
import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.client.fx.WispParticleData;
import vazkii.botania.client.gui.TooltipHandler;
import vazkii.botania.common.entity.ManaBurstEntity;
import vazkii.botania.common.handler.BotaniaSounds;

import java.util.List;
import java.util.UUID;

public class SpaceBlade extends SwordItem implements ManaItem, LensEffectItem {
    private static final String NBT_MANA = "Mana";
    private static final String NBT_LEVEL = "Level";
    private static final String NBT_TICK = "tick";
    private static final int[] CREATIVE_MANA = new int[]{0, 10000, 1000000, 10000000, 100000000, 1000000000, 2147483646};
    private static final int MAX_MANA = 2147483646;

    public SpaceBlade(Properties properties) {
        super(EXEAPI.MIRACLE_ITEM_TIER, 3, -2.4F, properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slotId, boolean isSelected) {
        if (entity instanceof Player player) {
            BlockPos pos = player.getOnPos();
            if (getLevel(stack) < 6 && getManaTag(stack) >= CREATIVE_MANA[getLevel(stack) + 1]) {
                setLevel(stack, getLevel(stack) + 1);
                world.playSound(player, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.0f);
                if (world.isClientSide) {
                    player.displayClientMessage(
                            Component.translatable("message.mana_full", getLevel(stack)), false);
                }
            }

            for (BlockPos blockPos : BlockPos.betweenClosed(pos.offset(-2, 0, -2), pos.offset(2, 1, 2))) {
                BlockEntity blockEntity = world.getBlockEntity(blockPos);
                if (blockEntity instanceof ManaPool pool) {
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
                            0, 0, 0);
                }
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag isAdvanced) {
        int lv = getLevel(stack);
        components.add(Component.translatable("info.ex_enigmaticlegacy.space_blade.lv", lv));
        components.add(Component.translatable("info.ex_enigmaticlegacy.space_blade.mana", getManaTag(stack)));

        TooltipHandler.addOnShift(components, () -> {
            components.add(Component.translatable("ex_enigmaticlegacy.swordInfo.1")
                    .withStyle(lv >= 1 ? ChatFormatting.GREEN : ChatFormatting.GRAY));
            components.add(Component.translatable("ex_enigmaticlegacy.swordInfo.2")
                    .withStyle(lv >= 3 ? ChatFormatting.GREEN : ChatFormatting.GRAY));
            components.add(Component.translatable("ex_enigmaticlegacy.swordInfo.LEVEL", Math.max(lv, 3))
                    .withStyle(lv >= 5 ? ChatFormatting.GREEN : ChatFormatting.GRAY));
        });
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player && !player.level().isClientSide()) {
            if (getLevel(stack) >= 3) {
                float size = getLevel(stack) >= 5 ? 3.5F : (getLevel(stack) >= 4 ? 2.5F : 1.5F);
                AABB aabb = target.getBoundingBox().inflate(size, 1.7F, size);
                for (LivingEntity living : player.level().getEntitiesOfClass(LivingEntity.class, aabb)) {
                    if (living.isAlive() && living != attacker) {
                        living.hurt(player.level().damageSources().playerAttack(player), getSwordDamage(stack));
                    }
                }
            }

            if (getLevel(stack) >= 5) {
                float chance = 0.3f + (getLevel(stack) * 0.1f);
                if (player.level().random.nextFloat() < chance) {
                    switch (player.level().random.nextInt(4)) {
                        case 0 -> target.setSecondsOnFire(5);
                        case 1 -> target.addEffect(new MobEffectInstance(MobEffects.WITHER, 1000));
                        case 2 -> target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 1000));
                        case 3 -> target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 1000));
                    }
                }
            }

            if (getLevel(stack) >= 1 && getManaTag(stack) >= 120) {
                trySpawnBurst(player, 1);
                setManaTag(stack, getManaTag(stack) - 120);
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isCrouching()) {
            trySpawnBurst(player, 1);
            int manaCost = (int) Math.floor(getSwordDamage(stack) * 100);
            if (getManaTag(stack) >= manaCost * 8) {
                for (int i = 0; i < 8; i++) {
                    trySpawnDirectionalBurst(player, i * 45F);
                }
                setManaTag(stack, getManaTag(stack) - manaCost);
            }

            if (getLevel(stack) >= 1 && getManaTag(stack) <= CREATIVE_MANA[getLevel(stack)]) {
                setLevel(stack, getLevel(stack) - 1);
                level.playSound(player, player.getOnPos(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.0f);
                if (level.isClientSide) {
                    player.displayClientMessage(
                            Component.translatable("message.insufficient_mana", getLevel(stack)), false);
                }
            }
        } else {
            Vec3 scale = player.getLookAngle().scale(3.0d);
            Vec3 startPos = player.position();
            Vec3 endPos = startPos.add(scale);
            stack.getOrCreateTag().putInt(NBT_TICK, 36);

            if (level.isClientSide) {
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
                        float cv = (float) (Math.random() / 4.0F - 0.125F);

                        SparkleParticleData sparkle = SparkleParticleData.sparkle(
                                1.8F * (float) (Math.random() - 0.5F),
                                r + cv, g + cv, b + cv, 3);
                        level.addParticle(sparkle, x + offsetX, y + 1.0 + offsetY, z + offsetZ, 0, 0, 0);

                        WispParticleData wisp = WispParticleData.wisp(
                                0.3f * (float) (Math.random() + 0.5f), r + cv, g + cv, b + cv, 1.0f);
                        level.addParticle(wisp, x + offsetX, y + 1.0 + offsetY, z + offsetZ,
                                (Math.random() - 0.5D) * 0.02D,
                                (Math.random() - 0.5D) * 0.01D,
                                (Math.random() - 0.5D) * 0.02D);
                    }
                }
            }

            level.playSound(player, player.getOnPos(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
            player.setDeltaMovement(scale);
            player.getCooldowns().addCooldown(stack.getItem(), 20);
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int remainingUseDuration) {
        if (living instanceof Player player && !level.isClientSide()
                && player.getAttackStrengthScale(0.0F) == 1.0F
                && getLevel(stack) >= 1 && getManaTag(stack) >= 120) {
            trySpawnBurst(player, 1);
            setManaTag(stack, getManaTag(stack) - 120);
        }
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        if (entity instanceof Player player && !entity.level().isClientSide()
                && getLevel(stack) >= 1 && getManaTag(stack) >= 120) {
            trySpawnBurst(player, 1);
            setManaTag(stack, getManaTag(stack) - 120);
        }
        return false;
    }

    public static void trySpawnBurst(Player player, int count) {
        for (int i = 0; i < count; i++) {
            ManaBurstEntity burst = getBurst(player, player.getMainHandItem());
            player.level().addFreshEntity(burst);
            player.getMainHandItem().hurtAndBreak(1, player,
                    p -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    BotaniaSounds.terraBlade, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    public static void trySpawnDirectionalBurst(Player player, float angle) {
        ManaBurstEntity burst = getDirectionalBurst(player, player.getMainHandItem(), angle);
        player.level().addFreshEntity(burst);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                BotaniaSounds.terraBlade, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    public static ManaBurstEntity getDirectionalBurst(Player player, ItemStack stack, float angle) {
        ManaBurstEntity burst = new ManaBurstEntity(player);
        double rad = Math.toRadians(angle);
        burst.setColor(2165484);
        burst.setMana(1024);
        burst.setStartingMana(1024);
        burst.setMinManaLoss(40);
        burst.setManaLossPerTick(4.0F);
        burst.setGravity(0.0F);
        burst.setDeltaMovement(Math.cos(rad) * 7.0F, 0, Math.sin(rad) * 7.0F);
        burst.setSourceLens(stack.copy());
        return burst;
    }

    public static ManaBurstEntity getBurst(Player player, ItemStack stack) {
        ManaBurstEntity burst = new ManaBurstEntity(player);
        burst.setColor(2165484);
        burst.setMana(1024);
        burst.setStartingMana(1024);
        burst.setMinManaLoss(40);
        burst.setManaLossPerTick(4.0F);
        burst.setGravity(0.0F);
        burst.setDeltaMovement(burst.getDeltaMovement().scale(7.0F));
        burst.setSourceLens(stack.copy());
        return burst;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> multimap = HashMultimap.create();
        if (slot == EquipmentSlot.MAINHAND) {
            multimap.put(Attributes.ATTACK_DAMAGE,
                    new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier",
                            getSwordDamage(stack), AttributeModifier.Operation.ADDITION));
            multimap.put(Attributes.MOVEMENT_SPEED,
                    new AttributeModifier(UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3"),
                            "Weapon speed", 0.15F, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
        return multimap;
    }

    private float getSwordDamage(ItemStack stack) {
        int level = getLevel(stack);
        float baseDamage = getTier().getAttackDamageBonus();
        return 5.0F + baseDamage + (level * level * level * 10 + level * 10);
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
    public void addMana(int i) {}

    @Override
    public boolean canReceiveManaFromPool(BlockEntity blockEntity) { return true; }

    @Override
    public boolean canReceiveManaFromItem(ItemStack itemStack) { return false; }

    @Override
    public boolean canExportManaToPool(BlockEntity blockEntity) { return false; }

    @Override
    public boolean canExportManaToItem(ItemStack itemStack) { return false; }

    @Override
    public boolean isNoExport() { return false; }

    @Override
    public void apply(ItemStack itemStack, BurstProperties burstProperties, Level level) {}

    @Override
    public boolean collideBurst(ManaBurst burst, HitResult hitResult, boolean b, boolean b1, ItemStack itemStack) {
        return b1;
    }

    @Override
    public void updateBurst(ManaBurst burst, ItemStack itemStack) {
        ThrowableProjectile entity = burst.entity();
        AABB axis = new AABB(entity.getX(), entity.getY(), entity.getZ(),
                entity.xOld, entity.yOld, entity.zOld).inflate(1.0);
        List<LivingEntity> entities = entity.level().getEntitiesOfClass(LivingEntity.class, axis);
        Entity thrower = entity.getOwner();

        for (LivingEntity living : entities) {
            if (living == thrower) continue;

            if (living instanceof Player livingPlayer && thrower instanceof Player throwingPlayer) {
                if (!throwingPlayer.canHarmPlayer(livingPlayer)) continue;
            }

            if (living.hurtTime == 0) {
                int cost = 33;
                int mana = burst.getMana();
                if (mana >= cost) {
                    burst.setMana(mana - cost);
                    float damage = getSwordDamage(itemStack);
                    if (!burst.isFake() && !entity.level().isClientSide()) {
                        DamageSource source;
                        if (thrower instanceof Player player) {
                            source = entity.level().damageSources().playerAttack(player);
                        } else if (thrower instanceof LivingEntity livingEntity) {
                            source = entity.level().damageSources().mobAttack(livingEntity);
                        } else {
                            source = entity.level().damageSources().magic();
                        }
                        living.hurt(source, damage);
                        entity.discard();
                        break;
                    }
                }
            }
        }
    }

    @Override
    public boolean doParticles(ManaBurst burst, ItemStack itemStack) {
        return true;
    }
}