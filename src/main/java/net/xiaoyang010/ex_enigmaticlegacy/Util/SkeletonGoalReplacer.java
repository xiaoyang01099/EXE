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

    // 存储玩家攻击过的亡灵生物的映射
    private static final Map<UUID, Set<UUID>> playerAttackedUndeadMap;

    // 静态初始化，从WildHuntArmorEventHandler获取引用
    static {
        playerAttackedUndeadMap = WildHuntArmorEventHandler.getPlayerAttackedUndeadMap();
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinWorldEvent event) {
        // 只在服务器端处理
        if (event.getWorld().isClientSide()) {
            return;
        }

        // 只处理骷髅类型
        if (event.getEntity() instanceof AbstractSkeleton) {
            AbstractSkeleton skeleton = (AbstractSkeleton) event.getEntity();

            // 替换骷髅的AI目标
            replaceSkeletonGoals(skeleton);
        }
    }

    // 替换骷髅的AI目标
    private static void replaceSkeletonGoals(AbstractSkeleton skeleton) {
        // 替换弓箭攻击目标
        for (WrappedGoal wrappedGoal : skeleton.goalSelector.getAvailableGoals()) {
            if (wrappedGoal.getGoal() instanceof RangedBowAttackGoal) {
                int priority = wrappedGoal.getPriority();
                skeleton.goalSelector.removeGoal(wrappedGoal.getGoal());

                skeleton.goalSelector.addGoal(priority, new WildHuntAwareGoal.Ranged<>(
                        skeleton,
                        1.0,  // 速度修饰符
                        20,   // 最小攻击间隔
                        15.0f,// 攻击半径
                        SkeletonGoalReplacer::shouldIgnorePlayer
                ));
                break;
            }
        }

        // 替换近战攻击目标
        for (WrappedGoal wrappedGoal : skeleton.goalSelector.getAvailableGoals()) {
            if (wrappedGoal.getGoal() instanceof MeleeAttackGoal) {
                int priority = wrappedGoal.getPriority();
                skeleton.goalSelector.removeGoal(wrappedGoal.getGoal());

                skeleton.goalSelector.addGoal(priority, new WildHuntAwareGoal.Melee(
                        skeleton,
                        1.2,  // 速度修饰符
                        false,// 是否持续跟踪不可见目标
                        SkeletonGoalReplacer::shouldIgnorePlayer
                ));
                break;
            }
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.world instanceof ServerLevel) {
            // 每10 ticks进行一次检查
            if (event.world.getGameTime() % 10 == 0) {
                // 检查所有骷髭
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

    // 判断是否应该忽略玩家的逻辑
    private static boolean shouldIgnorePlayer(Player player) {
        // 检查玩家是否穿戴全套野猎护甲
        boolean wearingFullSet = WildHuntArmor.isWearingFullSet(player);

        if (!wearingFullSet) {
            return false; // 不穿全套不需要忽略
        }

        // 查看这个玩家是否攻击过骷髅
        UUID playerUUID = player.getUUID();
        Set<UUID> attackedEntities = playerAttackedUndeadMap.get(playerUUID);

        // 如果玩家没有攻击记录，应该忽略
        return attackedEntities == null || attackedEntities.isEmpty();
    }
}