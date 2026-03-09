package net.xiaoyang010.ex_enigmaticlegacy.api.test;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.UUID;

public class VampireWolf extends Wolf {
    private UUID vampireOwnerUUID;
    private boolean isVampireMinion = false;

    public VampireWolf(EntityType<? extends Wolf> type, Level level) {
        super(type, level);
    }

    public void initAsVampireMinion(Player owner) {
        this.vampireOwnerUUID = owner.getUUID();
        this.isVampireMinion = true;

        this.setTame(true);
        this.setOwnerUUID(owner.getUUID());
        this.setHealth(this.getMaxHealth());

        this.setOrderedToSit(false);
        this.setRemainingPersistentAngerTime(0);
        this.setPersistentAngerTarget(null);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(4, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(6, new FollowOwnerGoal(this, 1.2, 8.0F, 2.0F, false));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this).setAlertOthers());
    }


    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!isVampireMinion) {
            return super.mobInteract(player, hand);
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean canMate(Animal other) {
        return false;
    }

    public UUID getVampireOwnerUUID() {
        return vampireOwnerUUID;
    }

    public boolean isVampireMinion() {
        return isVampireMinion;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("IsVampireMinion", isVampireMinion);
        if (vampireOwnerUUID != null) {
            tag.putUUID("VampireOwnerUUID", vampireOwnerUUID);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.isVampireMinion = tag.getBoolean("IsVampireMinion");
        if (tag.hasUUID("VampireOwnerUUID")) {
            this.vampireOwnerUUID = tag.getUUID("VampireOwnerUUID");
        }
    }

    @Nullable
    @Override
    public Wolf getBreedOffspring(ServerLevel level, AgeableMob mob) {
        return null;
    }
}