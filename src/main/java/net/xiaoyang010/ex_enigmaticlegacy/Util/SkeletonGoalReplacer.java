package net.xiaoyang010.ex_enigmaticlegacy.Util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.ai.WildHuntAwareGoal;
import net.xiaoyang010.ex_enigmaticlegacy.Event.WildHuntArmorEventHandler;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Item.armor.WildHuntArmor;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID)
public class SkeletonGoalReplacer {

    private static final Map<UUID, Set<UUID>> playerAttackedUndeadMap;

    static {
        playerAttackedUndeadMap = WildHuntArmorEventHandler.getPlayerAttackedUndeadMap();
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinWorldEvent event) {
        if (event.getWorld().isClientSide()) {
            return;
        }

        if (event.getEntity() instanceof AbstractSkeleton) {
            AbstractSkeleton skeleton = (AbstractSkeleton) event.getEntity();

            replaceSkeletonGoals(skeleton);
        }
    }

    private static void replaceSkeletonGoals(AbstractSkeleton skeleton) {
        for (WrappedGoal wrappedGoal : skeleton.goalSelector.getAvailableGoals()) {
            if (wrappedGoal.getGoal() instanceof RangedBowAttackGoal) {
                int priority = wrappedGoal.getPriority();
                skeleton.goalSelector.removeGoal(wrappedGoal.getGoal());

                skeleton.goalSelector.addGoal(priority, new WildHuntAwareGoal.Ranged<>(
                        skeleton,
                        1.0,
                        20,
                        15.0f,
                        SkeletonGoalReplacer::shouldIgnorePlayer
                ));
                break;
            }
        }


        for (WrappedGoal wrappedGoal : skeleton.goalSelector.getAvailableGoals()) {
            if (wrappedGoal.getGoal() instanceof MeleeAttackGoal) {
                int priority = wrappedGoal.getPriority();
                skeleton.goalSelector.removeGoal(wrappedGoal.getGoal());

                skeleton.goalSelector.addGoal(priority, new WildHuntAwareGoal.Melee(
                        skeleton,
                        1.2,
                        false,
                        SkeletonGoalReplacer::shouldIgnorePlayer
                ));
                break;
            }
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.world instanceof ServerLevel) {
            if (event.world.getGameTime() % 10 == 0) {
                for (AbstractSkeleton skeleton : ((ServerLevel)event.world).getEntities(
                        EntityTypeTest.forClass(AbstractSkeleton.class),
                        entity -> true)) {

                    LivingEntity target = skeleton.getTarget();
                    if (target instanceof Player) {
                        Player player = (Player) target;
                        if (shouldIgnorePlayer(player)) {
                            skeleton.setTarget(null);
                            skeleton.stopUsingItem();
                            skeleton.getNavigation().stop();
                        }
                    }
                }
            }
        }
    }

    private static boolean shouldIgnorePlayer(Player player) {
        boolean wearingFullSet = WildHuntArmor.isWearingFullSet(player);

        if (!wearingFullSet) {
            return false;
        }

        UUID playerUUID = player.getUUID();
        Set<UUID> attackedEntities = playerAttackedUndeadMap.get(playerUUID);

        return attackedEntities == null || attackedEntities.isEmpty();
    }
}