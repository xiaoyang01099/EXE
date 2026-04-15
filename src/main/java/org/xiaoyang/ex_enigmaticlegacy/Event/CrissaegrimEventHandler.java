package org.xiaoyang.ex_enigmaticlegacy.Event;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.xiaoyang.ex_enigmaticlegacy.Client.particle.ef.EntitySlash;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModEntities;
import org.xiaoyang.ex_enigmaticlegacy.Item.weapon.Crissaegrim;


public class CrissaegrimEventHandler{

    @SubscribeEvent
    public void onLeftClickAir(PlayerInteractEvent.LeftClickEmpty event) {
        Player player = event.getEntity();
        if (player.getMainHandItem().getItem() instanceof Crissaegrim &&
                !player.level.isClientSide) {
            doSlash(player);
        }
    }

    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (player.getMainHandItem().getItem() instanceof Crissaegrim &&
                !player.level.isClientSide) {
            doSlash(player);
        }
    }

    private void doSlash(Player player) {
        var existingSlashes = player.level.getEntitiesOfClass(
                EntitySlash.class,
                player.getBoundingBox().inflate(5)
        );

        boolean hasSlash = false;
        for (EntitySlash slash : existingSlashes) {
            if (slash.player != null && slash.player.getUUID().equals(player.getUUID())) {
                hasSlash = true;
                slash.getEntityData().set(EntitySlash.LIFETIME, 12);
                break;
            }
        }

        if (!hasSlash) {
            EntitySlash slash = new EntitySlash(ModEntities.ENTITY_SLASH.get(), player.level);
            slash.setPlayer(player);
            slash.setPos(player.getX(), player.getY(), player.getZ());
            player.level.addFreshEntity(slash);
        }
    }
}