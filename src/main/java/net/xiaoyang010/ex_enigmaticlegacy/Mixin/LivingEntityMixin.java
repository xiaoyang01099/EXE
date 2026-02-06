package net.xiaoyang010.ex_enigmaticlegacy.Mixin;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.xiaoyang010.ex_enigmaticlegacy.Item.armor.WildHuntArmor;
import net.xiaoyang010.ex_enigmaticlegacy.Util.EntityRidingData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.UUID;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void syncPlayersOnTop(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity.level.isClientSide) {
            return;
        }

        AABB entityBox = entity.getBoundingBox();
        AABB searchBox = new AABB(
                entityBox.minX - 0.5,
                entityBox.maxY - 0.2,
                entityBox.minZ - 0.5,
                entityBox.maxX + 0.5,
                entityBox.maxY + 2.5,
                entityBox.maxZ + 0.5
        );

        List<Player> players = entity.level.getEntitiesOfClass(
                Player.class,
                searchBox,
                p -> p.isAlive() && !p.isSpectator()
        );

        boolean hasPlayerOnTop = false;
        Player playerOnTop = null;

        for (Player player : players) {
            ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
            if (boots.isEmpty() || !(boots.getItem() instanceof WildHuntArmor)) {
                continue;
            }

            if (player.isShiftKeyDown()) {
                continue;
            }

            AABB playerBox = player.getBoundingBox();

            if (playerBox.maxX > entityBox.minX &&
                    playerBox.minX < entityBox.maxX &&
                    playerBox.maxZ > entityBox.minZ &&
                    playerBox.minZ < entityBox.maxZ) {

                double distY = playerBox.minY - entityBox.maxY;

                if (distY >= -0.5 && distY <= 1.0) {
                    player.setPos(player.getX(), entityBox.maxY, player.getZ());

                    Vec3 entityVel = entity.getDeltaMovement();
                    player.setDeltaMovement(entityVel.x,
                            player.getDeltaMovement().y > 0 ? player.getDeltaMovement().y : 0,
                            entityVel.z);

                    player.fallDistance = 0;
                    player.setOnGround(true);
                    player.hasImpulse = true;

                    hasPlayerOnTop = true;
                    playerOnTop = player;
                }
            }
        }

        if (entity instanceof Mob mob) {
            UUID entityId = entity.getUUID();

            if (hasPlayerOnTop && playerOnTop != null) {
                EntityRidingData.setPlayerOnTop(entityId, playerOnTop);

                if (mob.getTarget() == playerOnTop) {
                    mob.setTarget(null);
                }

                mob.getNavigation().stop();

                mob.setLastHurtByMob(null);

                if (entity.tickCount % 40 == 0) {
                    mob.setYRot(entity.getRandom().nextFloat() * 360F);
                }

            } else {
                EntityRidingData.removePlayerOnTop(entityId);
            }
        }
    }
}