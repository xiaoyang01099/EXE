package net.xiaoyang010.ex_enigmaticlegacy.Entity.biological;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.network.NetworkHooks;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Mob;

import java.util.Objects;

public class CloneEntity extends Monster {
    private int lifespan = 600;
    private boolean isVanishing = false;
    private int vanishTimer = 40;

    public CloneEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 0;
        this.setCustomName(new TextComponent("§c§l幻影分身"));
        this.setCustomNameVisible(true);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level.isClientSide) {
            if (lifespan > 0) {
                lifespan--;

                if (lifespan < 100 && level.getGameTime() % 20 == 0) {
                    this.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 10, 0));
                }
            } else if (!isVanishing) {
                startVanishing();
            }

            if (isVanishing) {
                handleVanishing();
            }

            if (level.getGameTime() % 5 == 0) {
                spawnCloneParticles();
            }
        }
    }

    private void startVanishing() {
        isVanishing = true;
        this.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40, 0));
    }

    private void handleVanishing() {
        if (vanishTimer > 0) {
            vanishTimer--;
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.CLOUD,
                        this.getX(), this.getY() + 1, this.getZ(),
                        10, 0.2, 0.5, 0.2, 0.02);
            }
        } else {
            this.remove(RemovalReason.DISCARDED);
        }
    }

    private void spawnCloneParticles() {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.PORTAL,
                    this.getX(), this.getY() + 1, this.getZ(),
                    2, 0.2, 0.5, 0.2, 0);
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return super.hurt(source, amount * 0.5F);
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return effect.getEffect().isBeneficial();
    }

    @Override
    public SoundEvent getDeathSound() {
        return Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("ex_enigmaticlegacy:meow")));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.MAX_HEALTH, 100)
                .add(Attributes.ARMOR, 50)
                .add(Attributes.ATTACK_DAMAGE, 30)
                .add(Attributes.FOLLOW_RANGE, 32)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5)
                .add(Attributes.ATTACK_KNOCKBACK, 2);
    }
}