package net.xiaoyang010.ex_enigmaticlegacy.Item;

import morph.avaritia.init.AvaritiaModContent;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRarities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class InfinityTotem extends Item {
    private static final int MAX_PERMANENT_WOLVES = 4;
    private static final int TEMP_WOLF_LIFETIME = 6000;  // 5分钟 = 300秒 = 6000 ticks

    public InfinityTotem() {
        super(new Properties().stacksTo(1).rarity(ModRarities.MIRACLE).tab(ModTabs.TAB_EXENIGMATICLEGACY_ITEM).durability(99));
    }

    private static final List<Supplier<Item>> REPAIR_ITEMS = Arrays.asList(
            AvaritiaModContent.INFINITY_INGOT::get
    );

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return REPAIR_ITEMS.stream()
                .map(Supplier::get)
                .anyMatch(item -> repair.is(item));
    }

    @Override
    public boolean canBeDepleted() {
        return true;
    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        return true;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

    public boolean hasTotemInInventory(Player player) {
        if (!player.getMainHandItem().isEmpty() && player.getMainHandItem().getItem() instanceof InfinityTotem) {
            return true;
        }
        if (!player.getOffhandItem().isEmpty() && player.getOffhandItem().getItem() instanceof InfinityTotem) {
            return true;
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof InfinityTotem) {
                return true;
            }
        }
        return false;
    }

    public ItemStack getTotemFromInventory(Player player) {
        if (!player.getMainHandItem().isEmpty() && player.getMainHandItem().getItem() instanceof InfinityTotem) {
            return player.getMainHandItem();
        }
        if (!player.getOffhandItem().isEmpty() && player.getOffhandItem().getItem() instanceof InfinityTotem) {
            return player.getOffhandItem();
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof InfinityTotem) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isEnchantable(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, net.minecraft.world.item.enchantment.Enchantment enchantment) {
        return false;
    }

    public void triggerTotemEffect(Player player, ItemStack stack, DamageSource source) {

        if (source.getMsgId().equals("command.kill")) {
            player.setHealth(1.0F);
            return;
        }

        int damage = stack.getDamageValue();
        int maxDamage = stack.getMaxDamage();

        player.invulnerableTime = 30;

        if (damage == maxDamage - 1) {
            player.setHealth(player.getMaxHealth());
            List<LivingEntity> entities = player.level.getEntitiesOfClass(
                    LivingEntity.class,
                    player.getBoundingBox().inflate(8.0D),
                    entity -> entity != player
            );
            for (LivingEntity entity : entities) {
                entity.hurt(DamageSource.playerAttack(player), 500.0F);
            }
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 800, 1)); // 40秒
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 800, 1)); // 40秒
        } else {
            player.setHealth(10.0F);
        }

        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 2600, 4));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 1));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 700, 2));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1100, 0));
        player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 15000, 6));
        player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 30, 6));

        stack.hurtAndBreak(1, player, (p) -> {
            p.broadcastBreakEvent(EquipmentSlot.MAINHAND);
        });

        spawnTotemParticles(player.level, player);

        summonWolves(player);

        Minecraft.getInstance().gameRenderer.displayItemActivation(stack);
        player.level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
    }


    private void spawnTotemParticles(Level level, Player player) {
        Random random = player.getRandom();
        for(int i = 0; i < 64; ++i) {
            double d0 = random.nextDouble() * 2.0D - 1.0D;
            double d1 = random.nextDouble() * 2.0D - 1.0D;
            double d2 = random.nextDouble() * 2.0D - 1.0D;
            if (!(d0 * d0 + d1 * d1 + d2 * d2 > 1.0D)) {
                double d3 = player.getX() + d0;
                double d4 = player.getY() + d1 + random.nextDouble();
                double d5 = player.getZ() + d2;
                level.addParticle(ParticleTypes.TOTEM_OF_UNDYING, d3, d4, d5, d0, d1 + 0.2D, d2);
            }
        }
    }

    private void summonWolves(Player player) {
        Level level = player.level;

        List<Wolf> existingWolves = level.getEntitiesOfClass(
                Wolf.class,
                new AABB(player.getX() - 50, player.getY() - 50, player.getZ() - 50,
                        player.getX() + 50, player.getY() + 50, player.getZ() + 50),
                wolf -> wolf.isTame() && wolf.getOwner() == player
        );

        int permanentWolves = 0;
        for (Wolf wolf : existingWolves) {
            if (!wolf.getPersistentData().contains("TemporaryWolf")) {
                permanentWolves++;
            }
        }

        for(int i = 0; i < 3; i++) {
            Wolf wolf = EntityType.WOLF.create(level);
            if(wolf != null) {
                wolf.setPos(
                        player.getX() + (player.getRandom().nextDouble() - 0.5D) * 2.0D,
                        player.getY(),
                        player.getZ() + (player.getRandom().nextDouble() - 0.5D) * 2.0D
                );

                wolf.tame(player);
                wolf.setHealth(wolf.getMaxHealth());

                wolf.getAttribute(Attributes.MAX_HEALTH).setBaseValue(30.0D);
                wolf.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(8.0D);
                wolf.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.4D);

                wolf.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1200, 1));
                wolf.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 1));

                if (permanentWolves >= MAX_PERMANENT_WOLVES) {
                    wolf.getPersistentData().putBoolean("TemporaryWolf", true);
                    wolf.getPersistentData().putInt("DespawnTimer", TEMP_WOLF_LIFETIME);
                } else {
                    permanentWolves++;
                }

                if (player.getLastHurtByMob() != null) {
                    wolf.setTarget(player.getLastHurtByMob());
                }

                level.addFreshEntity(wolf);
                level.addParticle(ParticleTypes.HEART,
                        wolf.getX(), wolf.getY() + 0.5D, wolf.getZ(),
                        0.0D, 0.0D, 0.0D);
                level.addParticle(ParticleTypes.PORTAL,
                        wolf.getX(), wolf.getY(), wolf.getZ(),
                        0.0D, 0.0D, 0.0D);
            }
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.WOLF_WHINE, SoundSource.NEUTRAL, 0.7F,
                1.0F + (player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.2F);
    }
}