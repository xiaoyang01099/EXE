package net.xiaoyang010.ex_enigmaticlegacy.Item.weapon.WIP;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;

public class Dragonslayer extends SwordItem {

    public Dragonslayer() {
        super(Tiers.DIAMOND, 70, -2.4F, new Properties()
                .rarity(Rarity.EPIC)
                .stacksTo(1)
                .tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR));
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return super.isValidRepairItem(stack, repairCandidate);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity victim, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, (entity) -> {
            entity.broadcastBreakEvent(entity.getUsedItemHand());
        });

        if (!victim.level.isClientSide && isDragon(victim)) {
            absoluteDamage(victim,
                    new EntityDamageSource("dragonslayer", attacker).bypassArmor(),
                    Math.max(4.0F, victim.getMaxHealth() / 5.0F));

            if (victim instanceof Player) {
                Player target = (Player) victim;
                // 原本的能量消耗逻辑已移除（cofh依赖）
                // 可以在这里添加其他效果
            }

            victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 300, 2));
            victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 300, 1));
        }

        return true;
    }

    public static boolean isDragon(Entity target) {
        if (target == null) {
            return false;
        }

        if (target instanceof Player) {
            Player player = (Player) target;
            for (ItemStack armor : player.getArmorSlots()) {
                if (armor != null && !armor.isEmpty()) {
                    ResourceLocation itemId = armor.getItem().getRegistryName();
                    if (itemId != null && itemId.getNamespace().equals("draconicevolution")) {
                        return true;
                    }
                }
            }
        } else if (target instanceof EnderDragon ||
                target.getName().getString().toLowerCase().contains("dragon") ||
                target.getName().getString().toLowerCase().contains("drake") ||
                target.getName().getString().toLowerCase().contains("dracon")) {
            return true;
        }

        return false;
    }

    public static void absoluteDamage(LivingEntity target, DamageSource src, float damage) {
        target.getCombatTracker().recordDamage(src, target.getHealth(), damage);
        target.setHealth(target.getHealth() - damage);
        if (target.getHealth() < 1.0F) {
            target.die(src);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onGetHurt(LivingHurtEvent event) {
        if (event.getEntityLiving() instanceof Player && event.getSource().getEntity() != null) {
            Player player = (Player) event.getEntityLiving();
            ItemStack equip = player.getMainHandItem();

            if (!equip.isEmpty() && equip.getItem() == this && isDragon(event.getSource().getEntity())) {
                event.setAmount(Math.min(event.getAmount(), 3.0F));
            }
        }
    }

    @SubscribeEvent
    public void onAttacked(LivingAttackEvent event) {
        if (event.getEntityLiving() instanceof Player && event.getSource().getEntity() != null) {
            Player player = (Player) event.getEntityLiving();
            ItemStack equip = player.getMainHandItem();

            if (!equip.isEmpty() && player.isBlocking() &&
                    equip.getItem() == this && isDragon(event.getSource().getEntity())) {
                event.setCanceled(true);

                if (event.getSource().getEntity() instanceof Player) {
                    Player other = (Player) event.getSource().getEntity();
                    other.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 300, 2));
                    other.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 300, 1));
                }
            }
        }
    }
}
