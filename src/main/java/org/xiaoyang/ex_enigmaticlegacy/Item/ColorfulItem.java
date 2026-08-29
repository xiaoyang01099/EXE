package org.xiaoyang.ex_enigmaticlegacy.Item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.xiaoyang.ex_enigmaticlegacy.Config.ConfigFile;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.colorful.ChargeTracker;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.colorful.ColorfulEntity;
import org.xiaoyang.ex_enigmaticlegacy.Event.registry.ChargeLightningClientRegistry;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModEntities;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModSounds;

import java.util.List;

public class ColorfulItem extends LightingItem {
    private static final Component TOOLTIP_USE = Component.translatable("item.exe.colorful.tooltip.1");
    private static final int ENCHANTMENT_VALUE = 10;
    private static final float POWER_DAMAGE_BONUS_PER_LEVEL = 0.25f;
    private int fullChargeTime = 50;
    private boolean quickChargeEnabled = true;
    private double quickChargeReduction = 0.20D;

    public ColorfulItem(Properties properties) {
        super(properties);
    }

    @Override
    public void loadConfigValues() {
        this.beamDamage = ConfigFile.colorfulBeamDamage();
        this.maxRange = ConfigFile.colorfulMaxRange();
        this.fullChargeTime = ConfigFile.colorfulFullChargeTime();
        this.quickChargeEnabled = ConfigFile.colorfulQuickChargeEnabled();
        this.quickChargeReduction = ConfigFile.colorfulQuickChargeReduction();
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null) {
            ChargeTracker.startCharge(player, player.tickCount, getChargeTime(stack));
            ChargeLightningClientRegistry.start(player, true);
            player.startUsingItem(context.getHand());
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ChargeTracker.startCharge(player, player.tickCount, getChargeTime(stack));
        ChargeLightningClientRegistry.start(player, true);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (livingEntity instanceof Player player) {
            ChargeTracker.updateCharge(player, player.tickCount);
            if (level.isClientSide() && ChargeTracker.isFullyCharged(player)) {
                LightingItem.emitFullChargeParticles(player, true);
            }
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
        if (livingEntity instanceof Player player) {
            float chargeProgress = ChargeTracker.stopCharge(player);
            ChargeLightningClientRegistry.stop(player);
            if (!level.isClientSide() && chargeProgress >= ChargeTracker.MIN_LAUNCH_THRESHOLD) {
                launchBeam(level, player, stack, chargeProgress);
                stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
            }
        }
    }

    private int getChargeTime(ItemStack stack) {
        int chargeTime = Math.max(1, fullChargeTime);
        if (!quickChargeEnabled) {
            return chargeTime;
        }

        int level = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.QUICK_CHARGE, stack);
        double multiplier = 1.0D - level * quickChargeReduction;
        return Math.max(1, (int) Math.ceil(chargeTime * multiplier));
    }

    private float getBeamDamage(ItemStack stack, float chargeProgress) {
        int powerLevel = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
        float multiplier = 1.0f + powerLevel * POWER_DAMAGE_BONUS_PER_LEVEL;
        return beamDamage * chargeProgress * multiplier;
    }

    private void launchBeam(Level level, Player player, ItemStack stack, float chargeProgress) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        Vec3 origin = LightingItem.getBeamHandOrigin(player, eyePos, lookVec);
        Vec3 endPos = eyePos.add(lookVec.scale(maxRange * chargeProgress));

        ColorfulEntity beamEntity = new ColorfulEntity(ModEntities.COLORFUL_COIN_ENTITY.get(), level);
        beamEntity.setBeamData(origin, endPos, player, getBeamDamage(stack, chargeProgress));

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.addFreshEntity(beamEntity);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.AAAAA.get(), SoundSource.PLAYERS, 1.8f, 1.0f);

        player.getCooldowns().addCooldown(this, 10);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public net.minecraft.world.item.UseAnim getUseAnimation(ItemStack stack) {
        return net.minecraft.world.item.UseAnim.BOW;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1;
    }

    @Override
    public int getEnchantmentValue() {
        return ENCHANTMENT_VALUE;
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return ENCHANTMENT_VALUE;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment == Enchantments.QUICK_CHARGE
                || enchantment == Enchantments.UNBREAKING
                || enchantment == Enchantments.MENDING
                || enchantment == Enchantments.POWER_ARROWS;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return repairCandidate.is(Items.GOLD_INGOT) || super.isValidRepairItem(stack, repairCandidate);
    }
}
