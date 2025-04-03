package net.xiaoyang010.ex_enigmaticlegacy.Entity.creature;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;

import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraftforge.entity.PartEntity;

public class BodyPart extends PartEntity<ComplexCreature> {
    private int inWaterTick;
    private boolean wasInWater;

    public BodyPart(ComplexCreature parent, float size) {
        super(parent);
        this.dimensions = EntityDimensions.fixed(size, size);
        this.refreshDimensions();
        this.noPhysics = true;
    }

    public ComplexCreature getParent() {
        return this.getParent();
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public ItemStack getPickResult() {
        return this.getParent().getPickResult();
    }

    @Override
    public void tick() {
        boolean wasInWater = this.level.getBlockState(new BlockPos(
                (int)this.getX(),
                (int)this.getBoundingBox().minY,
                (int)this.getZ()
        )).getMaterial() == Material.WATER;

        super.tick();
        this.collideWithNearbyEntities();
        --this.inWaterTick;

        if (this.level.getBlockState(new BlockPos(
                (int)this.getX(),
                (int)this.getBoundingBox().minY,
                (int)this.getZ()
        )).getMaterial() == Material.WATER) {
            this.inWaterTick = 20;
            if (!this.wasInWater) {
                for (int i = 0; i < 12; ++i) {
                    float x = (this.getBbWidth() * 0.4F + this.random.nextFloat() * 0.6F) *
                            (float)(this.random.nextBoolean() ? 1 : -1);
                    float z = (this.getBbWidth() * 0.4F + this.random.nextFloat() * 0.6F) *
                            (float)(this.random.nextBoolean() ? 1 : -1);
                    this.level.addParticle(
                            new BlockParticleOption(ParticleTypes.BLOCK, Blocks.WATER.defaultBlockState()),
                            this.getX() + (double)x,
                            this.getY() + 0.5D + 0.5D * (double)this.getBbWidth() * (double)this.random.nextFloat(),
                            this.getZ() + (double)z,
                            (double)(x * 0.1F),
                            (double)(0.1F + 0.3F * this.getBbWidth() * this.random.nextFloat()),
                            (double)(z * 0.1F)
                    );
                }
            }
        } else if (this.inWaterTick > 0) {
            for (int i = 0; i < 4; ++i) {
                float x = this.getBbWidth() * 1.2F * (this.random.nextFloat() - 0.5F);
                float y = this.getBbWidth() * 0.4F * this.random.nextFloat();
                float z = this.getBbWidth() * 1.2F * (this.random.nextFloat() - 0.5F);
                this.level.addParticle(
                        ParticleTypes.SPLASH,
                        this.getX() + (double)x,
                        this.getBoundingBox().minY + (double)y,
                        this.getZ() + (double)z,
                        0.0D, 0.0D, 0.0D
                );
            }
        }
        this.wasInWater = wasInWater;
    }

    private void collideWithNearbyEntities() {
        for (Entity entity : this.level.getEntities(this, this.getBoundingBox().inflate(0.2D, 0.0D, 0.2D))) {
            if (entity.canBeCollidedWith() && !this.isAlliedTo(entity)) {
                if (entity.isPushable()) {
                    entity.push(this);
                }
                this.getParent().collideWithEntity(this, entity);
            }
        }
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        return super.isAlliedTo(entity) || entity instanceof BodyPart &&
                ((BodyPart)entity).getParent() == this.getParent();
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void defineSynchedData() {
    }
}