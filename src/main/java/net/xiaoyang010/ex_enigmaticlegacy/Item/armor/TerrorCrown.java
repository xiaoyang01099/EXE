package net.xiaoyang010.ex_enigmaticlegacy.Item.armor;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "ex_enigmaticlegacy")
public class TerrorCrown extends ArmorItem {
    public TerrorCrown(ArmorMaterial material, EquipmentSlot slot, Item.Properties properties) {
        super(material, slot, properties);
    }

    // 检查玩家是否穿戴了恐惧皇冠
    public static boolean isWearingTerrorCrown(Player player) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        return !helmet.isEmpty() && helmet.getItem() instanceof TerrorCrown;
    }

    // 取消任何生物对佩戴皇冠玩家的锁定
    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity newTarget = event.getNewTarget();
        if (newTarget instanceof Player player) {
            if (isWearingTerrorCrown(player)) {
                event.setCanceled(true); // 取消目标更改
            }
        }
    }

    // 阻止任何生物对佩戴皇冠玩家的攻击锁定
//    @SubscribeEvent
    public static void onLivingSetAttackTarget(LivingSetAttackTargetEvent event) {
        if (event.getTarget() instanceof Player player) {
            if (isWearingTerrorCrown(player)) {
                event.setCanceled(true); // 阻止设定攻击目标
            }
        }
    }

    // 取消任何攻击事件的仇恨
    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (isWearingTerrorCrown(player)) {
                event.setCanceled(true); // 阻止敌对生物对玩家的攻击
            }
        }
    }

    // 取消来自任何敌对生物的伤害，包括投射物
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (isWearingTerrorCrown(player)) {
                if (event.getSource().getDirectEntity() instanceof Projectile projectile) {
                    // 如果伤害来源是投射物，取消伤害
                    event.setCanceled(true);
                }
            }
        }
    }

    // 取消所有生物的仇恨，不允许任何生物对玩家生成仇恨
    @Override
    public void onArmorTick(ItemStack stack, net.minecraft.world.level.Level world, Player player) {
        super.onArmorTick(stack, world, player);

        if (!world.isClientSide && isWearingTerrorCrown(player)) {
            // 获取玩家附近的生物，并取消其对玩家的仇恨和锁定
            for (LivingEntity entity : world.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(10))) {
                // 如果是敌对生物或Boss，移除对玩家的锁定和仇恨
                if (entity instanceof Mob mob) {
                    if (mob.getTarget() == player) {
                        mob.setTarget(null); // 取消目标锁定
                    }
                    mob.setLastHurtByMob(null); // 移除仇恨
                } else if (entity instanceof EnderDragon || entity instanceof WitherBoss) {
                    if (entity.getLastHurtByMob() == player) {
                        entity.setLastHurtByMob(null); // 移除BOSS对玩家的仇恨
                    }
                }
            }
        }
    }
}
