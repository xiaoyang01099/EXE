package org.xiaoyang.ex_enigmaticlegacy.Item;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.xiaoyang.ex_enigmaticlegacy.Config.MagicBowConfig;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.bow.MagicArrowEntity;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.bow.MagicBowParticleEffectEntity;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModEnchantments;
import org.xiaoyang.ex_enigmaticlegacy.Event.AutoTrackingClientHandler;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModEntities;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

// MagicBowItem 实现魔法弓蓄力、快速装填、自动射击附魔和魔法箭发射。
public class MagicBowItem extends BowItem {
    private static final String TAG_CHARGE_TYPE = "MagicBowChargeType"; // 当前蓄力类型 NBT 字段。
    private static final String TAG_CHARGE_START_TICK = "MagicBowChargeStartTick"; // 当前蓄力轮次开始 tick 字段。
    private static final float AUTO_TRACKING_ARROW_SPEED_MULTIPLIER = 1.35F; // 自动追踪箭速度加成，让命中更快。
    private static final float AUTO_TRACKING_ARROW_INACCURACY = 0.05F; // 自动追踪箭散布，尽量保持直线。
    private static final Component TOOLTIP_USE = Component.translatable("item.exe.magic_bow.tooltip.1"); // 魔法弓用法提示文本。
    private static final Component TOOLTIP_USE2 = Component.translatable("item.exe.magic_bow.tooltip.2");
    private static final int ENCHANTMENT_VALUE = 15; // 魔法弓附魔台权重。
    private static final Set<UUID> SUPPRESS_NEXT_AUTO_TRACKING_VANILLA_SHOT = new HashSet<>(); // 自动追踪 C2S 发射后的服务端防重复玩家集合。

    // 配置缓存字段。
    private int baseFullChargeTicks;
    private boolean quickChargeEnabled;
    private double quickChargeReductionPerLevel;
    private double superChargeTimeMultiplier;
    private double strongChargeChance;
    private double superChargeChance;

    public MagicBowItem(Properties properties) {
        super(properties);
        loadConfigValues();
    }

    // 读取魔法弓配置，配置重载时也会调用。
    public void loadConfigValues() {
        this.baseFullChargeTicks = MagicBowConfig.fullChargeTime();
        this.quickChargeEnabled = MagicBowConfig.quickChargeEnabled();
        this.quickChargeReductionPerLevel = MagicBowConfig.quickChargeReductionPerLevel();
        this.superChargeTimeMultiplier = MagicBowConfig.superChargeTimeMultiplier();
        this.strongChargeChance = MagicBowConfig.strongChargeChance();
        this.superChargeChance = MagicBowConfig.superChargeChance();
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(TOOLTIP_USE);
        tooltip.add(TOOLTIP_USE2);
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ItemStack projectile = player.getProjectile(stack);
        boolean canShoot = !projectile.isEmpty() || player.getAbilities().instabuild;
        if (!canShoot) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide()) {
            beginCharge(stack, player);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!(livingEntity instanceof Player player)) return;
        if (level.isClientSide()) {
            if (hasAutoShoot(stack) && hasAutoTracking(stack)) {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> AutoTrackingClientHandler.requestAutoShootIfReady(player, stack, remainingUseDuration));
            }
            return;
        }
        if (hasAutoTracking(stack) && consumeSuppressNextAutoTrackingVanillaShot(player)) return;
        if (hasAutoTracking(stack)) return;
        if (!hasAutoShoot(stack)) return;

        int useTicks = getChargeUseTicks(stack, player, remainingUseDuration);
        int fullTicks = getFullChargeTicks(stack, getChargeType(stack));
        if (useTicks >= fullTicks) {
            shootMagicArrow(level, player, stack, true);
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeLeft) {
        if (!(livingEntity instanceof Player player)) return;
        if (level.isClientSide()) {
            if (hasAutoTracking(stack)) {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> AutoTrackingClientHandler.requestShoot(false));
            }
            return;
        }
        if (hasAutoTracking(stack) && consumeSuppressNextAutoTrackingVanillaShot(player)) return;
        if (hasAutoTracking(stack)) return;
        shootMagicArrow(level, player, stack, false);
    }

    // 服务端统一发射入口，松开右键和自动射击附魔共用。
    public boolean shootMagicArrow(Level level, Player player, ItemStack stack, boolean restartUsing) {
        return shootMagicArrow(level, player, stack, restartUsing, null);
    }

    // 服务端统一发射入口，支持自动追踪传入一次性目标。
    public boolean shootMagicArrow(Level level, Player player, ItemStack stack, boolean restartUsing, LivingEntity trackingTarget) {
        if (level.isClientSide()) return false;
        if (!(stack.getItem() instanceof MagicBowItem)) return false;

        ItemStack projectile = player.getProjectile(stack);
        boolean creativeOrInfinity = player.getAbilities().instabuild
                || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0;
        if (projectile.isEmpty()) {
            if (!creativeOrInfinity) {
                clearCharge(stack);
                return false;
            }
            projectile = new ItemStack(Items.ARROW);
        }

        int chargeType = getChargeType(stack);
        int useTicks = getChargeUseTicks(stack, player, player.getUseItemRemainingTicks());
        float power = getMagicPowerForTime(useTicks, getFullChargeTicks(stack, chargeType));
        if (power < 0.1F) {
            clearCharge(stack);
            return false;
        }

        MagicArrowEntity arrow = new MagicArrowEntity(ModEntities.MAGIC_ARROW_ENTITY.get(), level);
        arrow.setOwner(player);
        arrow.setPos(player.getX(), player.getEyeY() - 0.1D, player.getZ());
        arrow.setChargeType(chargeType);
        arrow.setBaseDamage(MagicBowConfig.arrowDamage(chargeType));
        arrow.setAutoTrackingShot(trackingTarget != null && hasAutoTracking(stack));
        shootArrowTowardTargetOrRotation(stack, player, arrow, trackingTarget, power);
        arrow.setCritArrow(power >= 1.0F);
        applyVanillaBowEnchantments(stack, arrow);
        level.addFreshEntity(arrow);

        // 发射后按原版弓规则处理弹药、耐久和统计。
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT,
                SoundSource.PLAYERS, 1.0F, 1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + power * 0.5F);

        if (!creativeOrInfinity && !projectile.isEmpty()) {
            projectile.shrink(1);
            if (projectile.isEmpty()) {
                player.getInventory().removeItem(projectile);
            }
        }
        if (!player.getAbilities().instabuild) {
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        clearCharge(stack);

        if (restartUsing && !stack.isEmpty()) {
            beginCharge(stack, player);
            player.startUsingItem(player.getUsedItemHand());
        } else {
            player.stopUsingItem();
        }
        return true;
    }

    // 兼容旧调用入口，后续代码应改用 shootMagicArrow。
    public boolean tryShoot(Level level, Player player, ItemStack stack, boolean restartUsing) {
        return shootMagicArrow(level, player, stack, restartUsing);
    }

    // 自动追踪 C2S 包发射前登记玩家，防止同一次使用被服务端原逻辑重复发射。
    public static void suppressNextAutoTrackingVanillaShot(Player player) {
        if (player == null) return;
        SUPPRESS_NEXT_AUTO_TRACKING_VANILLA_SHOT.add(player.getUUID());
    }

    // 玩家退出或取消时清理防重复标记。
    public static void clearSuppressNextAutoTrackingVanillaShot(Player player) {
        if (player == null) return;
        SUPPRESS_NEXT_AUTO_TRACKING_VANILLA_SHOT.remove(player.getUUID());
    }

    // 消费自动追踪防重复标记。
    public static boolean consumeSuppressNextAutoTrackingVanillaShot(Player player) {
        return player != null && SUPPRESS_NEXT_AUTO_TRACKING_VANILLA_SHOT.remove(player.getUUID());
    }

    // 开始一轮蓄力时随机确定普通、强蓄力或星辰裁决。
    public void beginCharge(ItemStack stack, Player player) {
        double roll = player.getRandom().nextDouble();
        boolean hasStarJudgement = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.STAR_JUDGEMENT.get(), stack) > 0;
        int chargeType;
        if (hasStarJudgement && roll < superChargeChance) {
            chargeType = MagicBowParticleEffectEntity.CHARGE_SUPER;
        } else if (roll < strongChargeChance + (hasStarJudgement ? superChargeChance : 0.0D)) {
            chargeType = MagicBowParticleEffectEntity.CHARGE_STRONG;
        } else {
            chargeType = MagicBowParticleEffectEntity.CHARGE_NORMAL;
        }
        stack.getOrCreateTag().putInt(TAG_CHARGE_TYPE, chargeType);
        stack.getOrCreateTag().putInt(TAG_CHARGE_START_TICK, player.tickCount);
    }

    // 读取当前蓄力类型，缺省为普通。
    public int getChargeType(ItemStack stack) {
        return stack.getOrCreateTag().getInt(TAG_CHARGE_TYPE);
    }

    // 读取本轮蓄力已经经过的 tick，自动射击重启后依赖 NBT 轮次计时。
    public int getChargeUseTicks(ItemStack stack, Player player, int remainingUseDuration) {
        if (stack.hasTag() && stack.getOrCreateTag().contains(TAG_CHARGE_START_TICK)) {
            return Math.max(0, player.tickCount - stack.getOrCreateTag().getInt(TAG_CHARGE_START_TICK));
        }
        return getUseDuration(stack) - remainingUseDuration;
    }

    // 清理本轮蓄力类型，避免取消发射后 NBT 残留。
    public void clearCharge(ItemStack stack) {
        if (stack.hasTag()) {
            stack.getOrCreateTag().remove(TAG_CHARGE_TYPE);
            stack.getOrCreateTag().remove(TAG_CHARGE_START_TICK);
        }
    }

    // 判断魔法弓是否拥有自动射击附魔。
    public boolean hasAutoShoot(ItemStack stack) {
        return EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.AUTO_SHOOT.get(), stack) > 0;
    }

    // 判断魔法弓是否拥有自动追踪附魔。
    public boolean hasAutoTracking(ItemStack stack) {
        return EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.AUTO_TRACKING.get(), stack) > 0;
    }

    // 有合法锁定目标时朝目标中心射击，否则使用玩家当前朝向射击。
    public void shootArrowTowardTargetOrRotation(ItemStack stack, Player player, MagicArrowEntity arrow, LivingEntity trackingTarget, float power) {
        if (trackingTarget != null && hasAutoTracking(stack)) {
            Vec3 direction = trackingTarget.getBoundingBox().getCenter().subtract(arrow.position());
            if (direction.lengthSqr() > 1.0E-6D) {
                Vec3 normalized = direction.normalize();
                arrow.shoot(normalized.x, normalized.y, normalized.z, power * 3.0F * AUTO_TRACKING_ARROW_SPEED_MULTIPLIER, AUTO_TRACKING_ARROW_INACCURACY);
                return;
            }
        }
        arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 3.0F, 1.0F);
    }

    // 按快速装填百分比和星辰裁决倍率计算本轮满蓄 tick。
    public int getFullChargeTicks(ItemStack stack, int chargeType) {
        double ticks = baseFullChargeTicks;
        if (chargeType == MagicBowParticleEffectEntity.CHARGE_SUPER) {
            ticks *= superChargeTimeMultiplier;
        }
        if (quickChargeEnabled) {
            int quickCharge = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.QUICK_CHARGE, stack);
            double reduction = Mth.clamp(quickCharge * quickChargeReductionPerLevel, 0.0D, 0.95D);
            ticks *= 1.0D - reduction;
        }
        return Math.max(1, Mth.ceil(ticks));
    }

    // 使用原版弓的蓄力曲线，但满蓄时间改为配置值。
    public float getMagicPowerForTime(int useTicks, int fullChargeTicks) {
        float progress = (float) useTicks / Math.max(1, fullChargeTicks);
        progress = (progress * progress + progress * 2.0F) / 3.0F;
        return Math.min(progress, 1.0F);
    }

    // 按原版弓发射期规则写入力量、冲击和火矢附魔效果。
    public void applyVanillaBowEnchantments(ItemStack stack, MagicArrowEntity arrow) {
        int power = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
        if (power > 0) {
            arrow.setBaseDamage(arrow.getBaseDamage() + power * 0.5D + 0.5D);
        }
        int punch = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
        if (punch > 0) {
            arrow.setKnockback(punch);
        }
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack) > 0) {
            arrow.setSecondsOnFire(100);
        }
    }

    // 允许魔法弓进入附魔台。
    @Override
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1;
    }

    // 附魔台使用的附魔价值。
    @Override
    public int getEnchantmentValue() {
        return ENCHANTMENT_VALUE;
    }

    // 附魔台使用的附魔价值。
    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return ENCHANTMENT_VALUE;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment == Enchantments.QUICK_CHARGE
                || enchantment == ModEnchantments.STAR_JUDGEMENT.get()
                || enchantment == ModEnchantments.AUTO_SHOOT.get()
                || enchantment == ModEnchantments.AUTO_TRACKING.get()
                || super.canApplyAtEnchantingTable(stack, enchantment);
    }
}
