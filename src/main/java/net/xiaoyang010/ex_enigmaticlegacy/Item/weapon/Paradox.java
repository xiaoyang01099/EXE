package net.xiaoyang010.ex_enigmaticlegacy.Item.weapon;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModDamageSources;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;

import javax.annotation.Nullable;
import java.util.List;

public class Paradox extends SwordItem {
    private static final float PARADOX_DAMAGE_CAP = 200.0F;

    public Paradox() {
        super(Tiers.NETHERITE,
                3,
                -2.4F,
                new Properties()
                        .stacksTo(1)
                        .rarity(Rarity.EPIC)
                        .tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR)
                        .fireResistant());
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (!player.level.isClientSide && entity instanceof LivingEntity target) {
            double randomDamage = Math.random() * PARADOX_DAMAGE_CAP;
            double playerDamage = PARADOX_DAMAGE_CAP - randomDamage;

            if (randomDamage > 0) {
                target.hurt(new ModDamageSources.DamageSourceParadox(player), (float)randomDamage);
            }

            if (playerDamage > 0) {
                player.hurt(new ModDamageSources.DamageSourceParadoxReflection(), (float)playerDamage);
            }

            spawnParadoxParticles(player.level, entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ());

            return true;
        }
        return false;
    }

    private void spawnParadoxParticles(Level level, double x, double y, double z) {
        if (level.isClientSide) return;

        for (int i = 0; i < 20; i++) {
            level.addParticle(
                    ParticleTypes.WITCH,
                    x + (Math.random() - 0.5) * 2.0,
                    y + (Math.random() - 0.5) * 2.0,
                    z + (Math.random() - 0.5) * 2.0,
                    (Math.random() - 0.5) * 0.5,
                    (Math.random() - 0.5) * 0.5,
                    (Math.random() - 0.5) * 0.5
            );
        }

        for (int i = 0; i < 15; i++) {
            level.addParticle(
                    ParticleTypes.SMOKE,
                    x + (Math.random() - 0.5) * 1.5,
                    y + (Math.random() - 0.5) * 1.5,
                    z + (Math.random() - 0.5) * 1.5,
                    (Math.random() - 0.5) * 0.3,
                    Math.random() * 0.2,
                    (Math.random() - 0.5) * 0.3
            );
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(new TranslatableComponent("item.ItemParadox1.lore"));
            tooltip.add(new TranslatableComponent("item.ItemParadox2.lore"));
            tooltip.add(new TranslatableComponent("item.ItemParadox3.lore"));
            tooltip.add(new TranslatableComponent("item.ItemParadox4.lore"));
            tooltip.add(new TranslatableComponent("item.ItemParadox5.lore"));
        } else {
            tooltip.add(new TranslatableComponent("item.FRShiftTooltip.lore"));
        }

        tooltip.add(new TranslatableComponent("item.FREmpty.lore"));
        tooltip.add(new TranslatableComponent("item.ItemParadoxDamage_1.lore")
                .append(String.valueOf((int)PARADOX_DAMAGE_CAP))
                .append(new TranslatableComponent("item.ItemParadoxDamage_2.lore")));
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.EPIC;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide && stack.isDamaged() && entity.tickCount % 20 == 0) {
            stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return true;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return 2048;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return InteractionResult.PASS;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 22;
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return !player.isCreative();
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player && !attacker.level.isClientSide) {
            if (Math.random() < 0.1) {
                double paradoxDamage = Math.random() * 10.0;
                target.hurt(new ModDamageSources.DamageSourceParadox(player), (float)paradoxDamage);
                player.hurt(new ModDamageSources.DamageSourceParadoxReflection(), (float)(10.0 - paradoxDamage));

                spawnParadoxParticles(attacker.level, target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ());
            }
        }

        stack.hurtAndBreak(1, attacker, (entity) -> {
            entity.broadcastBreakEvent(attacker.getUsedItemHand());
        });

        return true;
    }
}