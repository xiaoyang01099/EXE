package net.xiaoyang010.ex_enigmaticlegacy.Item.weapon;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import net.xiaoyang010.ex_enigmaticlegacy.Client.particle.ef.Effect;
import net.xiaoyang010.ex_enigmaticlegacy.Client.particle.ef.EffectSlash;
import net.xiaoyang010.ex_enigmaticlegacy.Client.particle.ef.EntitySlash;
import net.xiaoyang010.ex_enigmaticlegacy.Client.particle.fx.FXHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Init.*;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage.EffectMessage;
import net.xiaoyang010.ex_enigmaticlegacy.api.EXEAPI;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SongOfTheAbyss extends SwordItem implements Vanishable {
    public float range = 8.0F;
    private static final int COMBO_THRESHOLD = 5;
    private static final int SLASH_COOLDOWN = 40;
    private int comboCount = 0;
    private long lastHitTime = 0;
    private static final Random rand = new Random();

    public SongOfTheAbyss() {
        super(EXEAPI.MIRACLE_ITEM_TIER, 200, -2.2F, new Properties()
                .tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR)
                .rarity(ModRarities.MIRACLE)
                .fireResistant());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && player.getCooldowns().getCooldownPercent(this, 0) == 0) {
            spawnSlashEntity(level, player);
            player.getCooldowns().addCooldown(this, SLASH_COOLDOWN);

            level.playSound(null, player.blockPosition(),
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 0.8F);

            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.pass(stack);
    }

    private void spawnSlashEntity(Level level, Player player) {
        EntitySlash slash = new EntitySlash(ModEntities.ENTITY_SLASH.get(), level);
        slash.setPlayer(player);
        slash.setPos(player.getX(), player.getY(), player.getZ());
        level.addFreshEntity(slash);

        Vec3 lookVec = player.getLookAngle();
        double lx = player.getX() + lookVec.x * 4.0;
        double ly = player.getY() + player.getEyeHeight() + lookVec.y * 4.0;
        double lz = player.getZ() + lookVec.z * 4.0;

        AABB damageBox = new AABB(
                lx - 3.0, ly - 3.0, lz - 3.0,
                lx + 3.0, ly + 3.0, lz + 3.0
        );

        List<LivingEntity> entities = level.getEntitiesOfClass(
                LivingEntity.class,
                damageBox,
                entity -> entity != player && !entity.getUUID().equals(player.getUUID())
        );

        for (LivingEntity entity : entities) {
            applyPercentDamage(entity, player, 0.25f);
            entity.addEffect(new MobEffectInstance(ModEffects.DROWNING.get(), 100, 1));
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.level.isClientSide) {
            Level level = target.level;

            long currentTime = level.getGameTime();
            if (currentTime - lastHitTime < 40) {
                comboCount++;
            } else {
                comboCount = 1;
            }
            lastHitTime = currentTime;

            float damageMultiplier = 0.35f + (comboCount * 0.05f);
            damageMultiplier = Math.min(damageMultiplier, 0.65f);

            applyPercentDamage(target, attacker, damageMultiplier);

            target.addEffect(new MobEffectInstance(ModEffects.DROWNING.get(), 200, 2));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 300, 2));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));

            if (attacker instanceof Player player) {
                spawnAttackSlash(level, player, target);

                float currentHealth = target.getHealth();
                float damage = currentHealth * damageMultiplier;
                float healAmount = damage * (0.20f + comboCount * 0.02f);
                healAmount = Math.min(healAmount, damage * 0.35f);
                player.heal(healAmount);

                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 0));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 0));

                if (comboCount >= COMBO_THRESHOLD) {
                    triggerAbyssalWave(level, target, player);
                    comboCount = 0;
                }

                if (player.tickCount % 60 == 0) {
                    triggerAOEDamage(level, target, player);
                }

                if (level.random.nextFloat() < 0.2f) {
                    applyPercentDamage(target, player, damageMultiplier);
                    spawnCriticalSlash(level, player, target);
                }

                stealBuffs(target, player);
            }

            spawnAbyssParticles(level, target);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    private void applyPercentDamage(LivingEntity target, LivingEntity attacker, float percent) {
        float currentHealth = target.getHealth();
        float damage = currentHealth * percent;
        target.setHealth(currentHealth - damage);

        if (attacker instanceof Player player) {
            target.setLastHurtByPlayer(player);
        }
    }

    private void spawnAttackSlash(Level level, Player player, LivingEntity target) {
        Vec3 lookVec = player.getLookAngle();
        float offX = 0.5F * (float)Math.sin(Math.toRadians(-90.0F - player.getYRot()));
        float offZ = 0.5F * (float)Math.cos(Math.toRadians(-90.0F - player.getYRot()));

        double x1 = player.getX() + lookVec.x * 0.5 + offX;
        double y1 = player.getY() + lookVec.y * 0.5 + player.getEyeHeight();
        double z1 = player.getZ() + lookVec.z * 0.5 + offZ;

        double x2 = target.getX();
        double y2 = target.getY() + target.getBbHeight() / 2.0F;
        double z2 = target.getZ();

        Effect slash = new EffectSlash(level.dimension().location().hashCode())
                .setSlashProperties(
                        player.getYRot(),
                        player.getXRot(),
                        30.0F + rand.nextFloat() * 120.0F,
                        2.5F,
                        2.0F,
                        150.0F
                )
                .setPosition(x1, y1, z1)
                .setMotion(
                        (x2 - x1) / 8.0,
                        (y2 - y1) / 8.0,
                        (z2 - z1) / 8.0
                )
                .setLife(8)
                .setAdditive(true)
                .setColor(0.0F, 1.0F, 1.0F, 1.0F);

        NetworkHandler.CHANNEL.send(
                PacketDistributor.ALL.noArg(),
                new EffectMessage(FXHandler.FX_SLASH, slash.write())
        );

        level.playSound(null, target.blockPosition(),
                ModSounds.SONG_OF_THE_ABYSS, SoundSource.PLAYERS, 2.0F, 0.5F);
    }

    private void spawnCriticalSlash(Level level, Player player, LivingEntity target) {
        for (int i = 0; i < 3; i++) {
            float angle = 120.0F * i;

            Effect slash = new EffectSlash(level.dimension().location().hashCode())
                    .setSlashProperties(
                            player.getYRot() + angle,
                            player.getXRot(),
                            rand.nextFloat() * 360.0F,
                            3.0F,
                            2.5F,
                            180.0F
                    )
                    .setPosition(
                            target.getX(),
                            target.getY() + target.getBbHeight() / 2.0F,
                            target.getZ()
                    )
                    .setMotion(0, 0, 0)
                    .setLife(10)
                    .setAdditive(true)
                    .setColor(0.8F, 0.2F, 0.8F, 1.0F);

            NetworkHandler.CHANNEL.send(
                    PacketDistributor.ALL.noArg(),
                    new EffectMessage(FXHandler.FX_SLASH, slash.write())
            );

            applyPercentDamage(target, player, 0.10f);
        }

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CRIT,
                    target.getX(), target.getY() + 1, target.getZ(),
                    30, 0.5, 0.5, 0.5, 0.2);
        }
        level.playSound(null, target.blockPosition(),
                ModSounds.SONG_OF_THE_ABYSS, SoundSource.PLAYERS, 3.0F, 1.0F);
    }

    private void triggerAbyssalWave(Level level, LivingEntity target, Player player) {
        AABB waveArea = new AABB(target.blockPosition()).inflate(range * 1.5);
        List<LivingEntity> entities = level.getEntitiesOfClass(
                LivingEntity.class, waveArea,
                entity -> entity != player && !entity.getUUID().equals(player.getUUID())
        );

        for (int i = 0; i < 8; i++) {
            float angle = 45.0F * i;
            Vec3 direction = new Vec3(
                    Math.cos(Math.toRadians(angle)),
                    0,
                    Math.sin(Math.toRadians(angle))
            ).normalize();

            Effect slash = new EffectSlash(level.dimension().location().hashCode())
                    .setSlashProperties(
                            angle,
                            0,
                            rand.nextFloat() * 360.0F,
                            4.0F,
                            3.0F,
                            200.0F
                    )
                    .setPosition(
                            target.getX(),
                            target.getY() + 1,
                            target.getZ()
                    )
                    .setMotion(
                            direction.x * 2.0,
                            0.5,
                            direction.z * 2.0
                    )
                    .setLife(15)
                    .setAdditive(true)
                    .setColor(0.1F, 0.1F, 1.0F, 1.0F);

            NetworkHandler.CHANNEL.send(
                    PacketDistributor.ALL.noArg(),
                    new EffectMessage(FXHandler.FX_SLASH, slash.write())
            );
        }

        for (LivingEntity entity : entities) {
            applyPercentDamage(entity, player, 0.40f);
            entity.addEffect(new MobEffectInstance(ModEffects.DROWNING.get(), 200, 3));
            entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 2));

            Vec3 knockback = entity.position().subtract(target.position()).normalize().scale(2.0);
            entity.setDeltaMovement(entity.getDeltaMovement().add(knockback.x, 0.8, knockback.z));
        }

        level.playSound(null, target.blockPosition(),
                SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.5F, 0.3F);

        if (level instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 100; i++) {
                double angle = (Math.PI * 2 * i) / 100;
                double x = target.getX() + Math.cos(angle) * range * 1.5;
                double z = target.getZ() + Math.sin(angle) * range * 1.5;
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        x, target.getY() + 1, z, 2, 0, 0, 0, 0);
            }
        }
    }

    private void triggerAOEDamage(Level level, LivingEntity target, Player player) {
        AABB aoe = new AABB(target.blockPosition()).inflate(range);
        List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(
                LivingEntity.class, aoe,
                entity -> entity != player && entity != target && !entity.getUUID().equals(player.getUUID())
        );

        for (LivingEntity entity : nearbyEntities) {
            applyPercentDamage(entity, player, 0.30f);
            entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 1));
            entity.addEffect(new MobEffectInstance(ModEffects.DROWNING.get(), 100, 1));

            Effect slash = new EffectSlash(level.dimension().location().hashCode())
                    .setSlashProperties(
                            rand.nextFloat() * 360.0F,
                            rand.nextFloat() * 180.0F - 90.0F,
                            rand.nextFloat() * 360.0F,
                            1.5F,
                            1.2F,
                            100.0F
                    )
                    .setPosition(
                            entity.getX(),
                            entity.getY() + entity.getBbHeight() / 2.0F,
                            entity.getZ()
                    )
                    .setMotion(0, 0, 0)
                    .setLife(6)
                    .setAdditive(true)
                    .setColor(0.3F, 0.3F, 0.9F, 0.8F);

            NetworkHandler.CHANNEL.send(
                    PacketDistributor.ALL.noArg(),
                    new EffectMessage(FXHandler.FX_SLASH, slash.write())
            );
        }
    }

    private void stealBuffs(LivingEntity target, Player player) {
        if (target.getUUID().equals(player.getUUID())) {
            return;
        }

        List<MobEffectInstance> effectsToSteal = new ArrayList<>();

        for (MobEffectInstance effect : target.getActiveEffects()) {
            if (effect.getEffect().isBeneficial() && effect.getEffect() != ModEffects.DROWNING.get()) {
                effectsToSteal.add(effect);
            }
        }

        for (MobEffectInstance effect : effectsToSteal) {
            player.addEffect(new MobEffectInstance(
                    effect.getEffect(),
                    effect.getDuration() / 2,
                    effect.getAmplifier()
            ));
            target.removeEffect(effect.getEffect());
        }
    }

    private void spawnAbyssParticles(Level level, LivingEntity target) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    target.getX(), target.getY() + 1, target.getZ(),
                    10, 0.5, 0.5, 0.5, 0.02);
            serverLevel.sendParticles(ParticleTypes.WARPED_SPORE,
                    target.getX(), target.getY() + 1, target.getZ(),
                    5, 0.3, 0.3, 0.3, 0.01);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        tooltip.add(new TranslatableComponent("tooltip.song_of_the_abyss.line1"));
        tooltip.add(new TranslatableComponent("tooltip.song_of_the_abyss.line2"));
        tooltip.add(new TranslatableComponent("tooltip.song_of_the_abyss.line3"));
        tooltip.add(new TranslatableComponent("tooltip.song_of_the_abyss.line4"));
        tooltip.add(new TranslatableComponent("tooltip.song_of_the_abyss.line5"));
        tooltip.add(new TranslatableComponent("tooltip.song_of_the_abyss.line6"));
        tooltip.add(new TranslatableComponent("tooltip.song_of_the_abyss.line7"));
    }
}