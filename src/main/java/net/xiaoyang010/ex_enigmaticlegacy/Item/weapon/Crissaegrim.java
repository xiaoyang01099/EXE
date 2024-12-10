package net.xiaoyang010.ex_enigmaticlegacy.Item.weapon;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRarities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;

import java.util.List;

public class Crissaegrim extends SwordItem {
    // 极高的攻击伤害和攻速
    private static final int INFINITE_DAMAGE = Integer.MAX_VALUE;
    private static final float INFINITE_ATTACK_SPEED = Float.MAX_VALUE;

    public Crissaegrim() {
        // 设置极高的攻击伤害和攻击速度
        super(Tiers.NETHERITE, INFINITE_DAMAGE, INFINITE_ATTACK_SPEED,
                new Item.Properties()
                        .tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR)
                        .rarity(ModRarities.MIRACLE));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // 绕过无敌状态，确保任何目标都能被攻击
        if (target.isInvulnerableTo(DamageSource.playerAttack((Player)attacker))) {
            target.setInvulnerable(false);  // 暂时移除无敌状态
        }

        // 造成无限伤害
        target.hurt(DamageSource.playerAttack((Player)attacker), INFINITE_DAMAGE);

        // 恢复无敌状态（如果需要）
        target.setInvulnerable(true);
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        Player player = context.getPlayer();

        if (!world.isClientSide && player != null) {
            AABB area = new AABB(context.getClickedPos()).inflate(100); // 半径100的范围伤害
            List<Mob> mobs = world.getEntitiesOfClass(Mob.class, area);

            // 对范围内的所有敌对生物造成伤害
            for (Mob mob : mobs) {
                mob.hurt(DamageSource.playerAttack(player), INFINITE_DAMAGE);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (entity instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) entity;

            // 强制伤害，忽略所有防御
            if (target.isInvulnerableTo(DamageSource.playerAttack(player))) {
                target.setInvulnerable(false);  // 暂时移除无敌状态
            }

            target.hurt(DamageSource.playerAttack(player), INFINITE_DAMAGE);

            // 恢复无敌状态
            target.setInvulnerable(true);
        }

        return super.onLeftClickEntity(stack, player, entity);
    }

    public boolean showDurabilityBar(ItemStack stack) {
        return stack.hasTag() && stack.getTagElement("crissaegrim") != null
                && stack.getTagElement("crissaegrim").contains("durability")
                && stack.getTagElement("crissaegrim").getInt("durability") > 0;
    }

    public double getDurabilityForDisplay(ItemStack stack) {
        return (double)((float)stack.getTagElement("crissaegrim").getInt("durability") / 10917.0F);
    }

}
