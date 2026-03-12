package net.xiaoyang010.ex_enigmaticlegacy.Item.weapon.WIP;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.Vec3;
import net.xiaoyang010.ex_enigmaticlegacy.Client.ModParticleTypes;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRarities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import net.xiaoyang010.ex_enigmaticlegacy.api.EXEAPI;

public class Cosmic extends SwordItem {
    public Cosmic() {
        super(EXEAPI.MIRACLE_ITEM_TIER, 99, -2.4F,
                new Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR).rarity(ModRarities.MIRACLE));
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        spawnAttackParticles(entity, player);
        return false;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (target.getHealth() <= 0 || target.isDeadOrDying()) {
            spawnDeathParticles(target, attacker);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    private static void spawnAttackParticles(Entity target, Player player) {
        if (player.level instanceof ServerLevel serverLevel) {
            Vec3 attackerPos = player.position().add(0, 1.0, 0);
            Vec3 targetCenter = target.getBoundingBox().getCenter();
            Vec3 direction = targetCenter.subtract(attackerPos).normalize();
            double bbWidth = target.getBbWidth() * 0.8;
            double bbHeight = target.getBbHeight() * 0.9;

            for (int i = 0; i < 100; ++i) {
                double offsetX = (Math.random() - 0.5) * bbWidth;
                double offsetY = (Math.random() - 0.5) * bbHeight;
                double offsetZ = (Math.random() - 0.5) * bbWidth;
                double speed = 0.3 + Math.random() * 0.2;
                double vx = direction.x * speed + (Math.random() - 0.5) * 0.2;
                double vy = direction.y * speed + (Math.random() - 0.5) * 0.2;
                double vz = direction.z * speed + (Math.random() - 0.5) * 0.2;

                serverLevel.sendParticles(
                        ModParticleTypes.RAINBOW.get(),
                        targetCenter.x + offsetX,
                        targetCenter.y + offsetY,
                        targetCenter.z + offsetZ,
                        0, vx, vy, vz, 0.4
                );
            }
        }
    }

    private static void spawnDeathParticles(LivingEntity target, LivingEntity attacker) {
        if (attacker.level instanceof ServerLevel serverLevel) {
            Vec3 center = target.getBoundingBox().getCenter();
            double bbWidth = target.getBbWidth();
            double bbHeight = target.getBbHeight();

            for (int i = 0; i < 200; ++i) {
                double offsetX = (Math.random() - 0.5) * bbWidth;
                double offsetY = (Math.random() - 0.5) * bbHeight;
                double offsetZ = (Math.random() - 0.5) * bbWidth;

                double vx = (Math.random() - 0.5) * 0.6;
                double vy = Math.random() * 0.4 + 0.1;
                double vz = (Math.random() - 0.5) * 0.6;

                serverLevel.sendParticles(
                        ModParticleTypes.RAINBOW.get(),
                        center.x + offsetX,
                        center.y + offsetY,
                        center.z + offsetZ,
                        0, vx, vy, vz, 0.5
                );
            }
        }
    }
}