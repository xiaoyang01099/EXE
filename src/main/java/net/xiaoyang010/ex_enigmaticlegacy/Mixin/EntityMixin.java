package net.xiaoyang010.ex_enigmaticlegacy.Mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.xiaoyang010.ex_enigmaticlegacy.Item.armor.EntityStandableBoots;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow
    public abstract AABB getBoundingBox();

    @Shadow
    public Level level;

    @Unique
    private LivingEntity ex_enigmaticlegacy$standingOnEntity = null;

    @Inject(method = "isOnGround", at = @At("RETURN"), cancellable = true)
    private void checkEntityGround(CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity) (Object) this;

        if (!(entity instanceof Player player)) {
            return;
        }

        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (boots.isEmpty() || !(boots.getItem() instanceof EntityStandableBoots)) {
            ex_enigmaticlegacy$standingOnEntity = null;
            return;
        }

        if (cir.getReturnValue()) {
            ex_enigmaticlegacy$standingOnEntity = null;
            return;
        }

        AABB playerBox = getBoundingBox();
        AABB searchBox = new AABB(
                playerBox.minX - 0.3,
                playerBox.minY - 0.5,
                playerBox.minZ - 0.3,
                playerBox.maxX + 0.3,
                playerBox.minY + 0.5,
                playerBox.maxZ + 0.3
        );

        List<LivingEntity> entities = level.getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                e -> e != player && e.isAlive() && !e.isSpectator()
        );

        for (LivingEntity targetEntity : entities) {
            AABB entityBox = targetEntity.getBoundingBox();

            double distanceToTop = playerBox.minY - entityBox.maxY;

            if (distanceToTop >= -0.5 && distanceToTop <= 0.5 &&
                    playerBox.maxX > entityBox.minX &&
                    playerBox.minX < entityBox.maxX &&
                    playerBox.maxZ > entityBox.minZ &&
                    playerBox.minZ < entityBox.maxZ) {

                ex_enigmaticlegacy$standingOnEntity = targetEntity;
                cir.setReturnValue(true);
                return;
            }
        }

        ex_enigmaticlegacy$standingOnEntity = null;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void adjustPositionOnEntity(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;

        if (!(entity instanceof Player player)) {
            return;
        }

        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (boots.isEmpty() || !(boots.getItem() instanceof EntityStandableBoots)) {
            return;
        }

        if (ex_enigmaticlegacy$standingOnEntity != null && ex_enigmaticlegacy$standingOnEntity.isAlive()) {
            AABB entityBox = ex_enigmaticlegacy$standingOnEntity.getBoundingBox();
            AABB playerBox = getBoundingBox();

            if (playerBox.maxX > entityBox.minX &&
                    playerBox.minX < entityBox.maxX &&
                    playerBox.maxZ > entityBox.minZ &&
                    playerBox.minZ < entityBox.maxZ) {

                double targetY = entityBox.maxY;
                double currentY = player.getY();

                if (player.getDeltaMovement().y <= 0 && Math.abs(currentY - targetY) < 1.0) {
                    player.setPos(player.getX(), targetY, player.getZ());
                    player.setDeltaMovement(player.getDeltaMovement().x, 0, player.getDeltaMovement().z);
                    player.fallDistance = 0;
                }
            } else {
                ex_enigmaticlegacy$standingOnEntity = null;
            }
        }
    }
}