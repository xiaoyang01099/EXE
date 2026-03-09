package net.xiaoyang010.ex_enigmaticlegacy.Entity.ai;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.biological.EntityFishBase;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class EatFishFoodAIFish extends Goal {
    private final EntityFishBase entity;
    private final ItemsSorter targetSorter;
    private ItemEntity targetItem;
    private final Level level;

    public EatFishFoodAIFish(EntityFishBase entity) {
        this.entity = entity;
        this.level = entity.level;
        this.targetSorter = new ItemsSorter(entity);
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        this.targetItem = this.getNearestItem(16);
        return this.targetItem != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.targetItem != null && this.targetItem.isAlive();
    }

    @Override
    public void tick() {
        double distance = Math.sqrt(
                Math.pow(this.entity.getX() - this.targetItem.getX(), 2.0D) +
                        Math.pow(this.entity.getY() - this.targetItem.getY(), 2.0D) +
                        Math.pow(this.entity.getZ() - this.targetItem.getZ(), 2.0D)
        );

        this.entity.getNavigation().moveTo(
                this.targetItem.getX(),
                this.targetItem.getY(),
                this.targetItem.getZ(),
                2D
        );

        if (distance < Math.max(this.entity.getBoundingBox().getSize(), 1D)) {
            if (this.targetItem != null) {
                this.entity.eatItem(this.targetItem.getItem());
                this.targetItem.getItem().shrink(1);
            }
        }

        if (this.entity.getNavigation().isDone()) {
            stop();
        }
    }

    private ItemEntity getNearestItem(int range) {
        List<ItemEntity> items = this.level.getEntitiesOfClass(
                ItemEntity.class,
                this.entity.getBoundingBox().inflate(range, range, range)
        );

        items.sort(this.targetSorter);

        for (ItemEntity currentItem : items) {
            ItemStack stack = currentItem.getItem();
            if (!stack.isEmpty()) {
                if (stack.is(Items.KELP) && !cantReachItem(currentItem)) {
                    return currentItem;
                }
            }
        }
        return null;
    }

    public boolean cantReachItem(Entity item) {
        Vec3 entityPos = new Vec3(
                entity.getX(),
                entity.getY() + entity.getBbHeight() / 2,
                entity.getZ()
        );

        Vec3 itemPos = new Vec3(
                item.getX(),
                item.getY() + item.getBbHeight() / 2,
                item.getZ()
        );

        ClipContext context = new ClipContext(
                entityPos,
                itemPos,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.SOURCE_ONLY,
                entity
        );

        HitResult hitResult = level.clip(context);

        if (hitResult != null && hitResult.getType() != HitResult.Type.MISS) {
            BlockPos hitPos = new BlockPos(hitResult.getLocation());
            return !level.getBlockState(hitPos).isAir();
        }
        return false;
    }

    public static class ItemsSorter implements Comparator<Entity> {
        private final Entity entity;

        public ItemsSorter(Entity entity) {
            this.entity = entity;
        }

        @Override
        public int compare(Entity entity1, Entity entity2) {
            double distance1 = this.entity.distanceToSqr(entity1);
            double distance2 = this.entity.distanceToSqr(entity2);
            return Double.compare(distance1, distance2);
        }
    }
}