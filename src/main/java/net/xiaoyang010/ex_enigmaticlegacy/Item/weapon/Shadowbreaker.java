package net.xiaoyang010.ex_enigmaticlegacy.Item.weapon;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRarities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import net.xiaoyang010.ex_enigmaticlegacy.api.EXEAPI;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.client.fx.WispParticleData;
import vazkii.botania.common.entity.EntityMagicMissile;
import vazkii.botania.common.entity.EntityManaBurst;
import vazkii.botania.common.entity.EntityPixie;

import java.util.List;

public class Shadowbreaker extends SwordItem {
    private static final int MANA = 100;

    public Shadowbreaker() {
        super(EXEAPI.MIRACLE_ITEM_TIER, 10, -2.4F, new Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR).rarity(ModRarities.MIRACLE));
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);

        if (!(entity instanceof Player player)) {
            return;
        }

        boolean isInMainHand = player.getMainHandItem() == stack;
        boolean isInOffHand = player.getOffhandItem() == stack;

        if (!isInMainHand && !isInOffHand) {
            return;
        }

        AABB boundingBox = player.getBoundingBox().inflate(8.0);
        List<Entity> entitiesAround = level.getEntities(entity, boundingBox,
                e -> shouldPushEntity(e, player));

        if (!entitiesAround.isEmpty()) {
            if (pushEntities(player, entitiesAround, level)) {
                if (!level.isClientSide) {
                    ManaItemHandler.instance().requestManaExact(stack, player, MANA, true);

                    if (level.getGameTime() % 3 == 0) {
                        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.4F, 1.0F);
                    }
                } else {
                    particleRing(level, player.getX(), player.getY(), player.getZ(),
                            5.0, 0F, 0F, 1F, 0.15F, 0.35F, 0.2F);
                }
            }
        }
    }

    private boolean shouldPushEntity(Entity entity, Player player) {
        if (entity == null || entity == player) {
            return false;
        }

        double distSq = entity.distanceToSqr(player);
        if (distSq >= 25.0) {
            return false;
        }

        if (entity instanceof EntityPixie) {
            return true;
        }

        if (entity instanceof Projectile) {
            if (entity instanceof EntityManaBurst) {
                return false;
            }

            if (entity instanceof EntityMagicMissile missile && missile.isEvil()) {
                return false;
            }

            if (entity instanceof AbstractArrow arrow && arrow.inGround) {
                return false;
            }

            return true;
        }

        if (entity instanceof LivingEntity && !(entity instanceof ArmorStand)) {
            Vec3 toEntity = entity.position().subtract(player.position());
            Vec3 lookVec = player.getLookAngle();
            return toEntity.dot(lookVec) < 0;
        }

        return false;
    }

    private boolean pushEntities(Player player, List<Entity> entities, Level level) {
        if (entities.isEmpty()) {
            return false;
        }

        boolean pushed = false;
        Vec3 playerPos = player.position();

        for (Entity entity : entities) {
            Vec3 entityPos = entity.position();
            Vec3 direction = entityPos.subtract(playerPos).normalize();

            double pushStrength = 0.6;
            entity.setDeltaMovement(entity.getDeltaMovement().add(
                    direction.x * pushStrength,
                    direction.y * pushStrength * 0.5,
                    direction.z * pushStrength
            ));
            entity.hurtMarked = true;
            pushed = true;
        }

        return pushed;
    }

    private void particleRing(Level level, double x, double y, double z, double range,
                              float r, float g, float b,
                              float motion, float verticalMotion, float size) {
        if (!level.isClientSide) {
            return;
        }

        int particleCount = (int) (range * 12);

        for (int i = 0; i < particleCount; i++) {
            double angle = (2 * Math.PI * i) / particleCount;
            double offsetX = Math.cos(angle) * range;
            double offsetZ = Math.sin(angle) * range;

            double particleX = x + offsetX;
            double particleY = y + 1.0;
            double particleZ = z + offsetZ;

            double motionX = Math.cos(angle) * motion;
            double motionY = verticalMotion;
            double motionZ = Math.sin(angle) * motion;

            SparkleParticleData sparkle = SparkleParticleData.sparkle(
                    size,
                    r, g, b,
                    8
            );

            level.addParticle(sparkle,
                    particleX, particleY, particleZ,
                    motionX, motionY, motionZ);

            if (i % 4 == 0) {
                WispParticleData wisp = WispParticleData.wisp(
                        size * 0.6F,
                        r, g, b,
                        0.5F
                );

                level.addParticle(wisp,
                        particleX, particleY - 0.2, particleZ,
                        motionX * 0.5, motionY * 0.5, motionZ * 0.5);
            }
        }

        int innerParticles = (int) (range * 3);
        for (int i = 0; i < innerParticles; i++) {
            double randomAngle = level.random.nextDouble() * 2 * Math.PI;
            double randomRadius = level.random.nextDouble() * range * 0.8;
            double offsetX = Math.cos(randomAngle) * randomRadius;
            double offsetZ = Math.sin(randomAngle) * randomRadius;

            SparkleParticleData innerSparkle = SparkleParticleData.sparkle(
                    size * 0.5F,
                    r, g, b,
                    6
            );

            level.addParticle(innerSparkle,
                    x + offsetX, y + 1.0, z + offsetZ,
                    0, verticalMotion * 0.7, 0);
        }
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return false;
    }
}