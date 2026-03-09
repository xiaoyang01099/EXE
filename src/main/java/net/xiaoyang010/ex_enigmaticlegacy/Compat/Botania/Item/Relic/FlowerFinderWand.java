package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic;

import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.others.NatureBoltEntity;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEffects;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEntities;
import net.xiaoyang010.ex_enigmaticlegacy.api.INoEMCItem;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.item.IRelic;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.item.relic.RelicImpl;
import vazkii.botania.xplat.IXplatAbstractions;


import javax.annotation.Nullable;
import java.util.List;

public class FlowerFinderWand extends Item implements INoEMCItem {
    // 魔力消耗配置
    private static final int HEAL_MANA_COST = 500;
    private static final int ATTACK_MANA_COST = 200;
    private static final int AOE_HEAL_MANA_COST = 1000;
    private static final int RANGED_ATTACK_MANA_COST = 350; // 远程攻击魔力消耗

    // 治疗和伤害值
    private static final float HEAL_AMOUNT = 4.0F; // 2颗心
    private static final float DAMAGE_AMOUNT = 10.0F; // 3颗心

    // 冷却时间(tick)
    private static final int COOLDOWN_ATTACK = 0; // 1秒
    private static final int COOLDOWN_HEAL = 40; // 2秒
    private static final int COOLDOWN_AOE = 40; // 5秒
    private static final int COOLDOWN_RANGED = 30; // 1.5秒

    public FlowerFinderWand(Properties properties) {
        super(properties);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @org.jetbrains.annotations.Nullable CompoundTag nbt) {
        return new RelicCapProvider(stack);
    }

    private static class RelicCapProvider implements ICapabilityProvider {
        private final LazyOptional<IRelic> relic;

        public RelicCapProvider(ItemStack stack) {
            this.relic = LazyOptional.of(() -> new RelicImpl(stack, null));
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @org.jetbrains.annotations.Nullable Direction direction) {
            if (capability == BotaniaForgeCapabilities.RELIC) {
                return relic.cast();
            }
            return LazyOptional.empty();
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        RelicImpl.addDefaultTooltip(stack, tooltip);
        tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.flower_finder_wand.tooltip.1"));
        tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.flower_finder_wand.tooltip.2"));
        tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.flower_finder_wand.tooltip.3"));
        tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.flower_finder_wand.tooltip.4"));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        var relicCap = itemstack.getCapability(BotaniaForgeCapabilities.RELIC);
        if (relicCap.isPresent()) {
            IRelic relic = relicCap.orElse(null);
            if (relic != null && !relic.isRightPlayer(player)) {
                return InteractionResultHolder.fail(itemstack);
            }
        }

        // 检查玩家是否Alt键 - 如果是则开始蓄力远程攻击
        if (player.isCrouching() && player.isUsingItem()) {
            // 蓄力远程攻击的逻辑在onUseTick和onUsingStop中处理
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(itemstack);
        }

        // 检查玩家是否潜行 - 如果是则激活AOE治疗
        if (player.isShiftKeyDown()) {
            if (handleAOEHeal(level, player, itemstack)) {
                player.awardStat(Stats.ITEM_USED.get(this));
                return InteractionResultHolder.success(itemstack);
            }
            return InteractionResultHolder.fail(itemstack);
        }

        // 远程攻击模式
        if (player.isSprinting()) {
            if (shootNatureBolt(level, player, itemstack)) {
                player.awardStat(Stats.ITEM_USED.get(this));
                return InteractionResultHolder.success(itemstack);
            }
            return InteractionResultHolder.fail(itemstack);
        }

        // 正常右键 - 自我治疗
        if (handleSelfHeal(level, player, itemstack)) {
            player.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResultHolder.success(itemstack);
        }

        return InteractionResultHolder.fail(itemstack);
    }

    // 远程攻击 - 发射自然能量弹
    private boolean shootNatureBolt(Level level, Player player, ItemStack stack) {
        // 检查冷却
        if (player.getCooldowns().isOnCooldown(this)) {
            return false;
        }

        // 检查魔力
        if (!hasMana(player, RANGED_ATTACK_MANA_COST)) {
            if (!level.isClientSide) {
                player.displayClientMessage(new TranslatableComponent("item.ex_enigmaticlegacy.flower_finder_wand.no_mana"), true);
            }
            return false;
        }

        if (!level.isClientSide) {
            // 消耗魔力
            consumeMana(player, RANGED_ATTACK_MANA_COST);

            // 创建并发射自然能量弹
            NatureBoltEntity bolt = new NatureBoltEntity(ModEntities.NATURE_BOLT.get(), player, level);

            // 设置基础属性
            bolt.setDamage(DAMAGE_AMOUNT + 2.0F); // 远程攻击伤害略高

            // 设置发射方向和速度
            Vec3 lookVec = player.getLookAngle();
            bolt.shoot(lookVec.x, lookVec.y, lookVec.z, 1.5F, 1.0F);

            // 生成实体
            level.addFreshEntity(bolt);

            // 播放声音和粒子效果
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.AZALEA_FALL, SoundSource.PLAYERS, 1.0F, 0.8F);

            if (level instanceof ServerLevel serverLevel) {
                for (int i = 0; i < 10; i++) {
                    serverLevel.sendParticles(ParticleTypes.COMPOSTER,
                            player.getX() + (level.random.nextDouble() - 0.5) * 0.5,
                            player.getY() + 1.0 + (level.random.nextDouble() - 0.5) * 0.5,
                            player.getZ() + (level.random.nextDouble() - 0.5) * 0.5,
                            1, 0, 0, 0, 0);
                }
            }

            // 设置冷却
            player.getCooldowns().addCooldown(this, COOLDOWN_RANGED);
        }

        return true;
    }

    private boolean handleSelfHeal(Level level, Player player, ItemStack stack) {
        // 检查冷却
        if (player.getCooldowns().isOnCooldown(this)) {
            return false;
        }

        // 检查魔力
        if (!hasMana(player, HEAL_MANA_COST)) {
            if (!level.isClientSide) {
                player.displayClientMessage(new TranslatableComponent("item.ex_enigmaticlegacy.flower_finder_wand.no_mana"), true);
            }
            return false;
        }

        // 如果玩家已经满血，不需要治疗
        if (player.getHealth() >= player.getMaxHealth()) {
            if (!level.isClientSide) {
                player.displayClientMessage(new TranslatableComponent("item.ex_enigmaticlegacy.flower_finder_wand.full_health"), true);
            }
            return false;
        }

        // 执行治疗
        if (!level.isClientSide) {
            // 消耗魔力
            consumeMana(player, HEAL_MANA_COST);

            // 治疗玩家
            player.heal(HEAL_AMOUNT);

            // 应用治疗效果
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));

            // 播放声音和粒子效果
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.CHORUS_FLOWER_GROW, SoundSource.PLAYERS, 0.5F, 1.0F);

            if (level instanceof ServerLevel serverLevel) {
                for (int i = 0; i < 20; i++) {
                    serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                            player.getX() + (level.random.nextDouble() - 0.5) * 1.0,
                            player.getY() + level.random.nextDouble() * 2.0,
                            player.getZ() + (level.random.nextDouble() - 0.5) * 1.0,
                            1, 0, 0, 0, 0);
                }
            }

            // 设置冷却
            player.getCooldowns().addCooldown(this, COOLDOWN_HEAL);
        }

        return true;
    }

    private boolean handleAOEHeal(Level level, Player player, ItemStack stack) {
        // 检查冷却
        if (player.getCooldowns().isOnCooldown(this)) {
            return false;
        }

        // 检查魔力
        if (!hasMana(player, AOE_HEAL_MANA_COST)) {
            if (!level.isClientSide) {
                player.displayClientMessage(new TranslatableComponent("item.ex_enigmaticlegacy.flower_finder_wand.no_mana"), true);
            }
            return false;
        }

        // 执行AOE治疗
        if (!level.isClientSide) {
            // 消耗魔力
            consumeMana(player, AOE_HEAL_MANA_COST);

            // 获取周围实体
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class,
                    player.getBoundingBox().inflate(5.0),
                    entity -> !entity.isSpectator() && entity.isAlive());

            boolean anyHealed = false;

            // 对周围实体进行治疗
            for (LivingEntity entity : entities) {
                if (entity.getHealth() < entity.getMaxHealth()) {
                    entity.heal(HEAL_AMOUNT * 0.75F); // AOE治疗效果略低于直接治疗
                    anyHealed = true;

                    // 对友好实体应用再生效果
                    if (!(entity instanceof Player) || player.canHarmPlayer((Player) entity)) {
                        entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0));
                    }
                }
            }

            if (!anyHealed) {
                player.displayClientMessage(new TranslatableComponent("item.ex_enigmaticlegacy.flower_finder_wand.no_targets"), true);
                return false;
            }

            // 播放声音和粒子效果
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);

            if (level instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel) level;
                for (int i = 0; i < 50; i++) {
                    double angle = 2 * Math.PI * i / 50;
                    double radius = 5.0;
                    double x = player.getX() + Math.cos(angle) * radius;
                    double z = player.getZ() + Math.sin(angle) * radius;
                    serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                            x, player.getY() + 0.5, z,
                            1, 0, 0.1, 0, 0.02);
                }
            }

            // 设置冷却
            player.getCooldowns().addCooldown(this, COOLDOWN_AOE);
        }

        return true;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
        if (relicCap.isPresent()) {
            IRelic relic = relicCap.orElse(null);
            if (relic != null && !relic.isRightPlayer(player)) {
                return false; // 阻止攻击
            }
        }

        // 处理近战攻击
        handleAttack(player.level, player, entity, stack);
        return true;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if (!level.isClientSide && entity instanceof Player player) {
            var relic = IXplatAbstractions.INSTANCE.findRelic(stack);
            if (relic != null) {
                relic.tickBinding(player);
            }
        }

        if (entity instanceof Player && isSelected && level.getGameTime() % 80 == 0) {
            Player player = (Player) entity;
            if (player.getHealth() < player.getMaxHealth()) {
                player.heal(0.5F); // 每4秒恢复1/4颗心

                if (!level.isClientSide) {
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.2F, 1.5F);
                }
            }
        }
    }

    // 处理攻击逻辑
    public boolean handleAttack(Level level, Player player, Entity target, ItemStack stack) {
        // 检查冷却
        if (player.getCooldowns().isOnCooldown(this)) {
            return false;
        }

        // 检查魔力
        if (!hasMana(player, ATTACK_MANA_COST)) {
            if (!level.isClientSide) {
                player.displayClientMessage(new TranslatableComponent("item.ex_enigmaticlegacy.flower_finder_wand.no_mana"), true);
            }
            return false;
        }

        if (!level.isClientSide && target instanceof LivingEntity) {
            LivingEntity livingTarget = (LivingEntity) target;

            // 消耗魔力
            consumeMana(player, ATTACK_MANA_COST);

            // 造成伤害
            livingTarget.hurt(DamageSource.indirectMagic(player, player), DAMAGE_AMOUNT);

            // 应用负面效果 - 毒素和缓慢 (代表自然的攻击)
            livingTarget.addEffect(new MobEffectInstance(MobEffects.WITHER, 1200, 5));
            livingTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 1200, 5));
            livingTarget.addEffect(new MobEffectInstance(ModEffects.DROWNING.get(), 1200, 5));


                    // 播放声音和粒子效果
            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.THORNS_HIT, SoundSource.PLAYERS, 1.0F, 1.0F);

            if (level instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel) level;
                for (int i = 0; i < 15; i++) {
                    serverLevel.sendParticles(ParticleTypes.CRIT,
                            livingTarget.getX() + (level.random.nextDouble() - 0.5) * 1.0,
                            livingTarget.getY() + level.random.nextDouble() * 2.0,
                            livingTarget.getZ() + (level.random.nextDouble() - 0.5) * 1.0,
                            1, 0, 0, 0, 0.1);
                }
            }

            // 设置冷却
            player.getCooldowns().addCooldown(this, COOLDOWN_ATTACK);

            return true;
        }

        return false;
    }

    // 蓄力效果 - 当玩家持续使用物品时触发
    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (entity instanceof Player && entity.isCrouching()) {
            Player player = (Player) entity;
            int useDuration = getUseDuration(stack) - remainingUseDuration;

            // 蓄力效果 - 每4tick增加一个粒子效果
            if (level.isClientSide && useDuration % 4 == 0 && useDuration <= 40) {
                // 显示蓄力粒子
                double angle = player.getRandom().nextDouble() * Math.PI * 2;
                double distance = 0.3 + (useDuration / 20.0) * 0.3; // 粒子距离随蓄力时间增加
                double x = player.getX() + Math.cos(angle) * distance;
                double y = player.getY() + 1.0;
                double z = player.getZ() + Math.sin(angle) * distance;

                level.addParticle(
                        ParticleTypes.COMPOSTER,
                        x, y, z,
                        0, 0.1, 0
                );
            }

            // 蓄力音效
            if (!level.isClientSide && useDuration == 20) { // 1秒后的音效提示
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 0.3F, 2.0F);
            }

            // 超级蓄力完成的音效
            if (!level.isClientSide && useDuration == 40) { // 2秒后的音效提示
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 0.5F, 1.5F);
            }
        }
    }

    // 释放蓄力攻击
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (entity instanceof Player && entity.isCrouching()) {
            Player player = (Player) entity;

            var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
            if (relicCap.isPresent()) {
                IRelic relic = relicCap.orElse(null);
                if (relic != null && !relic.isRightPlayer(player)) {
                    return; // 阻止释放蓄力攻击
                }
            }

            int useDuration = getUseDuration(stack) - timeLeft;

            // 检查冷却
            if (player.getCooldowns().isOnCooldown(this)) {
                return;
            }

            // 检查魔力（根据蓄力程度增加消耗）
            int manaCost = RANGED_ATTACK_MANA_COST;
            float powerMultiplier = 1.0F;

            if (useDuration >= 40) { // 完全蓄力 (2秒)
                manaCost = RANGED_ATTACK_MANA_COST * 2;
                powerMultiplier = 2.5F;
            } else if (useDuration >= 20) { // 中等蓄力 (1秒)
                manaCost = (int)(RANGED_ATTACK_MANA_COST * 1.5);
                powerMultiplier = 1.8F;
            } else if (useDuration >= 10) { // 轻度蓄力 (0.5秒)
                manaCost = RANGED_ATTACK_MANA_COST;
                powerMultiplier = 1.3F;
            } else {
                // 蓄力不足，返回
                if (!level.isClientSide) {
                    player.displayClientMessage(new TranslatableComponent("item.ex_enigmaticlegacy.flower_finder_wand.charge_more"), true);
                }
                return;
            }

            // 检查魔力是否足够
            if (!hasMana(player, manaCost)) {
                if (!level.isClientSide) {
                    player.displayClientMessage(new TranslatableComponent("item.ex_enigmaticlegacy.flower_finder_wand.no_mana"), true);
                }
                return;
            }

            if (!level.isClientSide) {
                // 消耗魔力
                consumeMana(player, manaCost);

                // 根据蓄力程度发射不同数量的自然能量弹
                int boltCount = 1;
                if (powerMultiplier >= 2.0F) {
                    boltCount = 3; // 完全蓄力发射3个
                } else if (powerMultiplier >= 1.5F) {
                    boltCount = 2; // 中等蓄力发射2个
                }

                // 发射能量弹
                for (int i = 0; i < boltCount; i++) {
                    NatureBoltEntity bolt = new NatureBoltEntity(ModEntities.NATURE_BOLT.get(), player, level);

                    // 设置伤害 (基于蓄力程度)
                    bolt.setDamage(DAMAGE_AMOUNT * powerMultiplier);

                    // 设置发射方向和速度
                    Vec3 lookVec = player.getLookAngle();

                    // 对于多发射击，稍微调整角度
                    double angleOffset = 0;
                    if (boltCount > 1) {
                        angleOffset = (i - (boltCount - 1) / 2.0) * 0.2; // 调整散射角度

                        // 创建一个以玩家视线方向为轴的旋转向量
                        double yaw = Math.atan2(lookVec.z, lookVec.x) + angleOffset;
                        double pitch = Math.asin(lookVec.y);
                        double newX = Math.cos(yaw) * Math.cos(pitch);
                        double newZ = Math.sin(yaw) * Math.cos(pitch);
                        double newY = Math.sin(pitch);

                        lookVec = new Vec3(newX, newY, newZ);
                    }

                    // 设置射速和精度，蓄力越多精度越高（scatter越小）
                    float velocity = 1.5F + (powerMultiplier - 1.0F) * 0.5F;
                    float scatter = 1.5F - (powerMultiplier - 1.0F) * 0.4F;
                    if (scatter < 0.5F) scatter = 0.5F;

                    bolt.shoot(lookVec.x, lookVec.y, lookVec.z, velocity, scatter);

                    // 生成实体
                    level.addFreshEntity(bolt);
                }

                // 播放声音和粒子效果
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.AZALEA_FALL, SoundSource.PLAYERS, 1.0F, 0.8F);

                if (powerMultiplier >= 2.0F) {
                    // 强力发射音效
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.DROWNED_SHOOT, SoundSource.PLAYERS, 0.8F, 0.6F);
                }

                if (level instanceof ServerLevel) {
                    ServerLevel serverLevel = (ServerLevel) level;
                    int particleCount = 10 + (int)(powerMultiplier * 5);

                    for (int i = 0; i < particleCount; i++) {
                        serverLevel.sendParticles(ParticleTypes.COMPOSTER,
                                player.getX() + (level.random.nextDouble() - 0.5) * 0.5,
                                player.getY() + 1.0 + (level.random.nextDouble() - 0.5) * 0.5,
                                player.getZ() + (level.random.nextDouble() - 0.5) * 0.5,
                                1, 0, 0, 0, 0.1);
                    }

                    // 强力发射的额外粒子
                    if (powerMultiplier >= 2.0F) {
                        for (int i = 0; i < 15; i++) {
                            serverLevel.sendParticles(ParticleTypes.END_ROD,
                                    player.getX() + (level.random.nextDouble() - 0.5) * 1.0,
                                    player.getY() + 1.0 + (level.random.nextDouble() - 0.5) * 1.0,
                                    player.getZ() + (level.random.nextDouble() - 0.5) * 1.0,
                                    1, 0, 0, 0, 0.2);
                        }
                    }
                }

                // 设置冷却（蓄力越多冷却越长）
                int cooldown = COOLDOWN_RANGED;
                if (powerMultiplier >= 2.0F) {
                    cooldown = COOLDOWN_RANGED * 2;
                } else if (powerMultiplier >= 1.5F) {
                    cooldown = (int)(COOLDOWN_RANGED * 1.5);
                }

                player.getCooldowns().addCooldown(this, cooldown);
            }
        }
    }

    // 辅助方法检查玩家是否有足够的魔力
    private boolean hasMana(Player player, int mana) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (ManaItemHandler.instance().requestManaExactForTool(stack, player, mana, true)) {
                return true;
            }
        }
        return false;
    }

    // 辅助方法从玩家物品栏中消耗魔力
    private void consumeMana(Player player, int mana) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (ManaItemHandler.instance().requestManaExactForTool(stack, player, mana, false)) {
                break;
            }
        }
    }
}