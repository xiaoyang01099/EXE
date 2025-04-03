package net.xiaoyang010.ex_enigmaticlegacy.Entity.creature;

import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class ADGlobal {
    public static final float PI = 3.1415927F;
    public static final float DEG_TO_RAD = 0.017453292F;
    public static final float RAD_TO_DEG = 57.295776F;

    public static float wrapAngleAround(float angle, float target) {
        while (target - angle >= 180.0F) {
            angle += 360.0F;
        }
        while (target - angle < -180.0F) {
            angle -= 360.0F;
        }
        return angle;
    }

    @Nullable
    public static Entity getPointedEntity(LivingEntity living, float maxDist) {
        Vec3 eyePos = living.getEyePosition();
        Vec3 lookVec = living.getViewVector(1.0F);
        Vec3 reachVec = eyePos.add(
                lookVec.x * (double)maxDist,
                lookVec.y * (double)maxDist,
                lookVec.z * (double)maxDist
        );

        Entity pointedEntity = null;
        double distance = (double)maxDist;

        for (Entity entity : living.level.getEntities(living,
                living.getBoundingBox().move(
                        lookVec.x * (double)maxDist,
                        lookVec.y * (double)maxDist,
                        lookVec.z * (double)maxDist
                ).inflate(1.0D))) {

            if (entity.canBeCollidedWith() && !entity.isAlliedTo(living)) {
                AABB bbox = entity.getBoundingBox().inflate((double)entity.getPickRadius());
                Optional<Vec3> hitResult = bbox.clip(eyePos, reachVec);

                if (bbox.contains(eyePos)) {
                    if (0.0D < distance || distance == 0.0D) {
                        pointedEntity = entity;
                        distance = 0.0D;
                    }
                } else if (hitResult.isPresent()) {
                    double d3 = eyePos.distanceTo(hitResult.get());
                    if (d3 < distance || distance == 0.0D) {
                        if (entity == living.getVehicle() && !entity.canRiderInteract()) {
                            if (distance == 0.0D) {
                                pointedEntity = entity;
                            }
                        } else {
                            pointedEntity = entity;
                            distance = d3;
                        }
                    }
                }
            }
        }

        return pointedEntity;
    }
}