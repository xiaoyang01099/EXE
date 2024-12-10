package net.xiaoyang010.ex_enigmaticlegacy.Entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;


public class SpectriteCrystalEntity extends Entity {

    private BlockPos beamTarget;

    public SpectriteCrystalEntity(EntityType<? extends SpectriteCrystalEntity> entityType, Level world) {
        super(entityType, world);
        this.noPhysics = true;
        this.setBoundingBox(new AABB(this.getX() - 0.5, this.getY() - 0.5, this.getZ() - 0.5, this.getX() + 0.5, this.getY() + 1.5, this.getZ() + 0.5));
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("BeamTargetX")) {
            int x = compound.getInt("BeamTargetX");
            int y = compound.getInt("BeamTargetY");
            int z = compound.getInt("BeamTargetZ");
            this.beamTarget = new BlockPos(x, y, z);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        if (this.beamTarget != null) {
            compound.putInt("BeamTargetX", this.beamTarget.getX());
            compound.putInt("BeamTargetY", this.beamTarget.getY());
            compound.putInt("BeamTargetZ", this.beamTarget.getZ());
        }
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return this.getBoundingBox();
    }

    @Override
    public boolean canBeCollidedWith() {
        return true; // 确保实体可以与其他物体发生碰撞
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level.isClientSide && !this.isRemoved()) {
            this.explode();
            return true;
        }
        return false;
    }

    private void explode() {
        this.level.explode(this, this.getX(), this.getY(), this.getZ(), 6.0F, Explosion.BlockInteraction.DESTROY);
        this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 1.0F, 1.0F);
        this.remove(RemovalReason.KILLED);
    }

    public BlockPos getBeamTarget() {
        return this.beamTarget;
    }

    public void setBeamTarget(BlockPos target) {
        this.beamTarget = target;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 4096.0D;
    }
}
