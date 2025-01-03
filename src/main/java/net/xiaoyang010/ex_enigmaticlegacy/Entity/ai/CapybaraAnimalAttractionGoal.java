package net.xiaoyang010.ex_enigmaticlegacy.Entity.ai;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

public class CapybaraAnimalAttractionGoal extends Goal {
    private final Mob entity;

    public CapybaraAnimalAttractionGoal(Mob entity) {
        this.entity = entity;
    }

    @Override
    public boolean canUse() {
        return entity.tickCount % 60 == 0 && entity.getPassengers().isEmpty();
    }

    @Override
    public boolean canContinueToUse() {
        return entity.tickCount % 80 != 0;
    }

    @Override
    public void start() {
        super.start();
        for (Mob mobEntity : entity.level.getEntitiesOfClass(Mob.class, entity.getBoundingBox().inflate(5),
                e -> e != entity && e.getVehicle() == null)) {
            if (mobEntity.getBbWidth() <= 0.75f && mobEntity.getBbHeight() <= 0.75f) {
                mobEntity.getNavigation().moveTo(entity, mobEntity.getSpeed() + 0.4);
            }
        }
    }
}