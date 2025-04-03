package net.xiaoyang010.ex_enigmaticlegacy.Entity.creature;


import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.level.Level;

public abstract class Fish extends WaterCreature {
    private static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId(Fish.class, EntityDataSerializers.FLOAT);

    public Fish(EntityType<? extends Fish> type, Level level) {
        super(type, level);
        this.refreshDimensions();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.20000000298023224D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SCALE, 0.5F + this.random.nextFloat() * 0.6F);
    }

    public float getScale() {
        return this.entityData.get(SCALE);
    }

    protected void setScale(float scale) {
        this.entityData.set(SCALE, scale);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (SCALE.equals(key)) {
            this.refreshDimensions();
        }
    }

    @Override
    public void initSchool() {
        super.initSchool();
        this.school.setMaxSize(6 + this.random.nextInt(6));
        this.school.setRadius((float)this.school.getMaxSize());
    }

    @Override
    protected void moveCreature() {
        for (ComplexCreature entityLivingBase : this.level.getEntitiesOfClass(ComplexCreature.class, this.getBoundingBox().inflate(2.0D))) {
            if (this.distanceToSqr(entityLivingBase) <= 4.0D && this.getSensing().hasLineOfSight(entityLivingBase)) {
                this.fleeFromEntity = entityLivingBase;
            }
        }
        super.moveCreature();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("Scale", this.getScale());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Scale")) {
            this.setScale(tag.getFloat("Scale"));
        }
    }

    @Override
    public void refreshDimensions() {
        double width = 0.7D;
        double height = 0.5D;
        float scale = this.getScale();
        super.refreshDimensions();
        this.setBoundingBox(this.getDimensions(this.getPose()).scale(scale).makeBoundingBox(this.position()));
    }
}