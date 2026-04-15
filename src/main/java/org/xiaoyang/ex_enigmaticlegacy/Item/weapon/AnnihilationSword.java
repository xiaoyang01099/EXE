package org.xiaoyang.ex_enigmaticlegacy.Item.weapon;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.xiaoyang.ex_enigmaticlegacy.Entity.biological.Xingyun2825Entity;
import org.xiaoyang.ex_enigmaticlegacy.Util.ColorText;

import java.util.List;

public class AnnihilationSword extends SwordItem {

    public AnnihilationSword() {
        super(new Tier() {
            @Override
            public int getUses() { return 0; }

            @Override
            public float getSpeed() { return 500; }

            @Override
            public float getAttackDamageBonus() { return 1023; }

            @Override
            public int getLevel() { return 0; }

            @Override
            public int getEnchantmentValue() { return 0; }

            @Override
            public Ingredient getRepairIngredient() { return Ingredient.EMPTY; }
        }, 0, 0F, new Properties().fireResistant().stacksTo(1));
    }

    @Override
    public Component getName(ItemStack pStack) {
        return Component.translatable(ColorText.GetColor1("湮") + "灭の冲刺者");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack stack = pPlayer.getItemInHand(pUsedHand);

        if (pLevel instanceof ServerLevel serverLevel) {
            pLevel.explode(pPlayer, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), 1, Level.ExplosionInteraction.NONE);

            List<Entity> list = pLevel.getEntities(pPlayer,
                    new AABB(pPlayer.getX() - 40, pPlayer.getY() - 40, pPlayer.getZ() - 40,
                            pPlayer.getX() + 40, pPlayer.getY() + 40, pPlayer.getZ() + 40),
                    e -> !(e instanceof Xingyun2825Entity));

            Vec3 motion = pPlayer.getDeltaMovement();
            pPlayer.setDeltaMovement(motion.x * 2, motion.y * 2, motion.z * 2);

            list.forEach(entity -> {
                if (entity instanceof LivingEntity living) {
                    if (living instanceof Player p && p.isCreative()) return;

                    double dx = pPlayer.getX() - living.getX();
                    double dy = pPlayer.getY() - living.getY();
                    double dz = pPlayer.getZ() - living.getZ();

                    double mx = Math.abs(dx) > 0.001 ? 2 * dx / Math.abs(dx) : 0;
                    double my = Math.abs(dy) > 0.001 ? 2 * dy / Math.abs(dy) : 0;
                    double mz = Math.abs(dz) > 0.001 ? 2 * dz / Math.abs(dz) : 0;

                    living.setDeltaMovement(mx, my, mz);

                    serverLevel.sendParticles(ParticleTypes.PORTAL,
                            living.getX(), living.getY(), living.getZ(),
                            10, 0.5, 0.5, 0.5, 0.1);

                    onEntitySwing(stack, living);
                }
            });
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(Component.literal("§7》右键进行§c湮灭§7冲刺，摧毁撞到的生物和方块(WIP)"));
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
        if (pEntity instanceof LivingEntity living) {
            living.hurtDuration = 2;
            living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 10, 3, false, false));
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 10, 4, false, false));
            living.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 10, 4, false, false));
            living.addEffect(new MobEffectInstance(MobEffects.HEAL, 10, 4, false, false));
        }
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        entity.hurt(entity.level.damageSources().fellOutOfWorld(), entity.getMaxHealth() / 20f);
        return super.onEntitySwing(stack, entity);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (entity instanceof LivingEntity living) {
            living.hurt(living.level.damageSources().fellOutOfWorld(), living.getMaxHealth() / 10f);
            onEntitySwing(stack, living);
        }
        return super.onLeftClickEntity(stack, player, entity);
    }
}