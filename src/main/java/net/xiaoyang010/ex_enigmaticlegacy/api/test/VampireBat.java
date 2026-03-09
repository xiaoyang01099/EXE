package net.xiaoyang010.ex_enigmaticlegacy.api.test;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class VampireBat extends Bat {

    private UUID ownerUUID;

    public VampireBat(EntityType<? extends Bat> type, Level level) {
        super(type, level);
        this.setResting(false);
    }

    public void setOwnerUUID(UUID uuid) {
        this.ownerUUID = uuid;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    @Override
    protected void customServerAiStep() {
        if (ownerUUID == null) {
            super.customServerAiStep();
            return;
        }

        Player owner = this.level.getPlayerByUUID(ownerUUID);
        if (owner == null || !owner.isAlive()) {
            this.discard();
            return;
        }

        double dx = owner.getX() - this.getX();
        double dy = owner.getY() + 1.5 - this.getY();
        double dz = owner.getZ() - this.getZ();
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (dist > 3.0) {
            Vec3 motion = this.getDeltaMovement();
            double speed = 0.1;
            Vec3 newMotion = motion.add(
                    (Math.signum(dx) * 0.5 - motion.x) * speed,
                    (Math.signum(dy) * 0.7 - motion.y) * speed,
                    (Math.signum(dz) * 0.5 - motion.z) * speed
            );
            this.setDeltaMovement(newMotion);
        } else {
            double angle = (this.tickCount * 0.1) + ownerUUID.getLeastSignificantBits() % 6;
            double orbitX = owner.getX() + Math.cos(angle) * 2.0;
            double orbitZ = owner.getZ() + Math.sin(angle) * 2.0;
            double orbitY = owner.getY() + 1.5;

            Vec3 motion = this.getDeltaMovement();
            Vec3 newMotion = motion.add(
                    (orbitX - this.getX()) * 0.05 - motion.x * 0.1,
                    (orbitY - this.getY()) * 0.05 - motion.y * 0.1,
                    (orbitZ - this.getZ()) * 0.05 - motion.z * 0.1
            );
            this.setDeltaMovement(newMotion);
        }

        this.setYRot((float)(Math.atan2(
                this.getDeltaMovement().z,
                this.getDeltaMovement().x
        ) * (180 / Math.PI)) - 90.0f);
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("VampireOwner")) {
            this.ownerUUID = tag.getUUID("VampireOwner");
        }
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (ownerUUID != null) {
            tag.putUUID("VampireOwner", ownerUUID);
        }
    }
}