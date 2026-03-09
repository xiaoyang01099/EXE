package net.xiaoyang010.ex_enigmaticlegacy.Item;

import morph.avaritia.entity.EndestPearlEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Avaritia.shader.CosmicBeamRenderer;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;

public class InfinityRod extends Item {
    public InfinityRod() {
        super(new Properties()
                .tab(ModTabs.TAB_EXENIGMATICLEGACY_ITEM)
                .stacksTo(1));
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        if (entity instanceof Player player) {
            CosmicBeamRenderer.createLaserFromPlayer(player, 64, 2);
            Entity entity1 = getPointedEntity(entity.level,player,64,3);
            if (entity1 instanceof LivingEntity living)
                living.setHealth(entity.getHealth()-10);
        }
        ExEnigmaticlegacyMod.queueServerWork(20, CosmicBeamRenderer::clearAllLasers);
        return super.onEntitySwing(stack, entity);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this))return super.use(level, player, hand);
        level.playSound((Player)null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_PEARL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        player.getCooldowns().addCooldown(this, 20);
        if (!level.isClientSide) {
            EndestPearlEntity thrown = new EndestPearlEntity(level, player);
            thrown.setItem(held);
            thrown.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            level.addFreshEntity(thrown);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
        player.getCooldowns().addCooldown(this,5*20);
        return super.use(level, player, hand);
    }

    private Entity getPointedEntity(Level level, Player player, double range, float width) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = start.add(look.scale(range));

        AABB searchArea = new AABB(start, end).inflate(width);

        Entity closestEntity = null;
        double closestDistance = range;

        for (Entity entity : level.getEntitiesOfClass(Entity.class, searchArea)) {
            if (entity == player) continue;

            AABB entityBox = entity.getBoundingBox().inflate(0.3F);

            if (entityBox.clip(start, end).isPresent()) {
                double distance = start.distanceTo(entity.position());
                if (distance < closestDistance) {
                    closestEntity = entity;
                    closestDistance = distance;
                }
            }
        }

        return closestEntity;
    }
}
