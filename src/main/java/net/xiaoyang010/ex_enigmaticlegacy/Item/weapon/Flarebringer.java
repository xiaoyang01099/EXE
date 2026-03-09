package net.xiaoyang010.ex_enigmaticlegacy.Item.weapon;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRarities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import net.xiaoyang010.ex_enigmaticlegacy.api.EXEAPI;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.client.fx.WispParticleData;

import java.util.Comparator;
import java.util.List;

public class Flarebringer extends SwordItem {
    public static final double RANGE = 16.0;
    public static final int COLOR = 0xFFB43F;
    public static final float R = 0xFF / 255f;
    public static final float G = 0xB4 / 255f;
    public static final float B = 0x3F / 255f;

    public Flarebringer() {
        super(EXEAPI.MIRACLE_ITEM_TIER, 10, -2.4F,
                new Properties()
                        .tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR)
                        .rarity(ModRarities.MIRACLE));
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        if (!(entity instanceof Player)) {
            return super.onEntitySwing(stack, entity);
        }

        Player player = (Player) entity;
        Level level = player.level;

        if (!level.isClientSide) {
            for (int i = 1; i <= (int) RANGE; i++) {
                Vec3 lookPos = getLookPosition(player, i);

                List<Entity> entities = level.getEntitiesOfClass(
                                Entity.class,
                                new AABB(lookPos, lookPos).inflate(1.5),
                                e -> e instanceof LivingEntity && e != player && e.isAlive()
                        ).stream()
                        .sorted(Comparator.comparingDouble(e -> e.distanceToSqr(lookPos)))
                        .toList();

                for (Entity target : entities) {
                    if (target instanceof LivingEntity living) {
                        attackEntity(player, living, stack);

                        return super.onEntitySwing(stack, entity);
                    }
                }
            }
        }

        return super.onEntitySwing(stack, entity);
    }

    private Vec3 getLookPosition(Player player, double distance) {
        Vec3 eyePos = player.getEyePosition(1.0F);
        Vec3 lookVec = player.getViewVector(1.0F);
        Vec3 targetPos = eyePos.add(lookVec.scale(distance));

        ClipContext context = new ClipContext(
                eyePos,
                targetPos,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                player
        );

        Vec3 hitPos = player.level.clip(context).getLocation();

        if (hitPos.distanceToSqr(eyePos) < targetPos.distanceToSqr(eyePos)) {
            return hitPos;
        }

        return targetPos;
    }


    private void attackEntity(Player player, LivingEntity target, ItemStack weapon) {
        float attackDamage = (float) player.getAttributeValue(
                net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE
        );

        float enchantBonus = EnchantmentHelper.getDamageBonus(weapon, target.getMobType());
        float totalDamage = attackDamage + enchantBonus;

        boolean isCritical = player.fallDistance > 0.0F
                && !player.isOnGround()
                && !player.onClimbable()
                && !player.isInWater()
                && !player.hasEffect(net.minecraft.world.effect.MobEffects.BLINDNESS)
                && !player.isPassenger();

        if (isCritical) {
            totalDamage *= 1.5F;
        }

        boolean hurt = target.hurt(DamageSource.playerAttack(player), totalDamage);

        if (hurt) {
            int knockback = EnchantmentHelper.getKnockbackBonus(player);
            if (knockback > 0 || player.isSprinting()) {
                double knockbackStrength = knockback > 0 ? knockback : 1;
                if (player.isSprinting()) {
                    knockbackStrength += 1;
                }

                Vec3 direction = player.getLookAngle().normalize();
                target.knockback(
                        knockbackStrength * 0.5,
                        -direction.x,
                        -direction.z
                );
            }

            int fireAspect = EnchantmentHelper.getFireAspect(player);
            if (fireAspect > 0) {
                target.setSecondsOnFire(fireAspect * 4);
            }

            EnchantmentHelper.doPostHurtEffects(target, player);
            EnchantmentHelper.doPostDamageEffects(player, target);
            target.setLastHurtByPlayer(player);

            if (isCritical) {
                player.level.playSound(null,
                        player.getX(), player.getY(), player.getZ(),
                        SoundEvents.PLAYER_ATTACK_CRIT,
                        SoundSource.PLAYERS, 1.0F, 1.0F);
                player.crit(target);
            } else {
                player.level.playSound(null,
                        player.getX(), player.getY(), player.getZ(),
                        SoundEvents.PLAYER_ATTACK_STRONG,
                        SoundSource.PLAYERS, 1.0F, 1.0F);
            }

            player.magicCrit(target);

            weapon.hurtAndBreak(1, player,
                    p -> p.broadcastBreakEvent(player.getUsedItemHand()));

            player.causeFoodExhaustion(0.1F);
        } else {
            player.level.playSound(null,
                    player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_ATTACK_NODAMAGE,
                    SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level,
                              @NotNull Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);

        if (!selected || !level.isClientSide || !(entity instanceof Player player)) {
            return;
        }

        if (player.getMainHandItem().getItem() != this) {
            return;
        }

        LivingEntity target = findTarget(player);
        if (target == null) {
            return;
        }

        particleRing(level,
                target.getX(),
                target.getY() + target.getBbHeight() / 2,
                target.getZ(),
                target.getBbWidth() + 0.5,
                R, G, B
        );
    }

    @OnlyIn(Dist.CLIENT)
    private LivingEntity findTarget(Player player) {
        Level level = player.level;

        for (int i = 1; i <= (int) RANGE; i++) {
            Vec3 lookPos = getLookPosition(player, i);

            List<Entity> entities = level.getEntitiesOfClass(
                            Entity.class,
                            new AABB(lookPos, lookPos).inflate(1.5),
                            e -> e instanceof LivingEntity && e != player && e.isAlive()
                    ).stream()
                    .sorted(Comparator.comparingDouble(e -> e.distanceToSqr(lookPos)))
                    .toList();

            for (Entity entity : entities) {
                if (entity instanceof LivingEntity living) {
                    return living;
                }
            }
        }

        return null;
    }

    @OnlyIn(Dist.CLIENT)
    private void particleRing(Level level, double x, double y, double z, double range,
                              float r, float g, float b) {
        int particleCount = Math.max(16, (int) (range * 20));

        for (int i = 0; i < particleCount; i++) {
            double angle = (2 * Math.PI * i) / particleCount;
            double offsetX = Math.cos(angle) * range;
            double offsetZ = Math.sin(angle) * range;

            SparkleParticleData sparkle = SparkleParticleData.sparkle(
                    0.15F, r, g, b, 8
            );

            level.addParticle(sparkle,
                    x + offsetX, y, z + offsetZ,
                    0, 0.05, 0);

            if (i % 2 == 0) {
                WispParticleData wisp = WispParticleData.wisp(
                        0.1F, r, g, b, 0.8F
                );

                level.addParticle(wisp,
                        x + offsetX, y, z + offsetZ,
                        0, 0.02, 0);
            }
        }
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return false;
    }
}
