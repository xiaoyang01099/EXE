package org.xiaoyang.ex_enigmaticlegacy.Item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.xiaoyang.ex_enigmaticlegacy.Util.PlayerUtil;
import org.xiaoyang.ex_enigmaticlegacy.Config.TridentPlusConfig;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.trident.HeavenlyThunderEntity;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.trident.TridentPlusEntity;
import org.xiaoyang.ex_enigmaticlegacy.Event.registry.ClientKeyChargeRegistry;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModEnchantments;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModEntities;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModWeapons;

import java.util.List;
import java.util.function.Consumer;

public class TridentPlusItem extends TridentItem {
    private static final Component TOOLTIP_ENCHANT = Component.translatable("item.exe.trident_plus.tooltip.1");
    public static final int HEAVENLY_THUNDER_FOOD_COST = 10; // 天雷技能饱食度消耗。
    private final Multimap<Attribute, AttributeModifier> tridentPlusModifiers; // 天雷战戟近战属性修饰器。

    public TridentPlusItem(Properties properties) {
        super(properties);
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", TridentPlusConfig.attackDamage(), AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", -2.9D, AttributeModifier.Operation.ADDITION));
        this.tridentPlusModifiers = builder.build();
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(TOOLTIP_ENCHANT);
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? this.tridentPlusModifiers : super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private final TridentPlusItemRenderer renderer = new TridentPlusItemRenderer();

            @Override
            public TridentPlusItemRenderer getCustomRenderer() {
                return this.renderer;
            }

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
                if (!(entity instanceof Player player)) return null;
                if (!ClientKeyChargeRegistry.isCharging(player)) return null;
                return ClientKeyChargeRegistry.getHand(player) == hand ? HumanoidModel.ArmPose.BOW_AND_ARROW : null;
            }

            @Override
            public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm,
                                                   ItemStack stack, float partialTick, float equipProcess, float swingProcess) {
                if (!ClientKeyChargeRegistry.isCharging(player)) return false;
                InteractionHand hand = ClientKeyChargeRegistry.getHand(player);
                HumanoidArm chargeArm = hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
                if (arm != chargeArm) return false;
                renderer.applyBowChargeTransform(poseStack, player, arm, partialTick, equipProcess);
                return true;
            }
        });
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
    public void releaseUsing(ItemStack stack, Level level, LivingEntity living, int timeLeft) {
        if (!(living instanceof Player player)) return;
        int useTicks = this.getUseDuration(stack) - timeLeft;
        if (useTicks < THROW_THRESHOLD_TIME) return;

        int riptideLevel = EnchantmentHelper.getRiptide(stack);
        if (riptideLevel > 0 && !player.isInWaterOrRain()) return;

        if (!level.isClientSide) {
            stack.hurtAndBreak(1, player, owner -> owner.broadcastBreakEvent(living.getUsedItemHand()));
            if (riptideLevel == 0) {
                TridentPlusEntity trident = new TridentPlusEntity(level, player, stack);
                trident.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, SHOOT_POWER + riptideLevel * 0.5F, 1.0F);
                if (player.getAbilities().instabuild) {
                    trident.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                }

                level.addFreshEntity(trident);
                level.playSound(null, trident, SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
                if (!player.getAbilities().instabuild) {
                    player.getInventory().removeItem(stack);
                }
            }
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        if (riptideLevel > 0) {
            applyRiptideMovement(level, player, riptideLevel);
        }
    }

    public void applyRiptideMovement(Level level, Player player, int riptideLevel) {
        float yaw = player.getYRot();
        float pitch = player.getXRot();
        float motionX = -Mth.sin(yaw * ((float) Math.PI / 180F)) * Mth.cos(pitch * ((float) Math.PI / 180F));
        float motionY = -Mth.sin(pitch * ((float) Math.PI / 180F));
        float motionZ = Mth.cos(yaw * ((float) Math.PI / 180F)) * Mth.cos(pitch * ((float) Math.PI / 180F));
        float motionLength = Mth.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
        float power = 3.0F * ((1.0F + riptideLevel) / 4.0F);
        motionX *= power / motionLength;
        motionY *= power / motionLength;
        motionZ *= power / motionLength;
        player.push(motionX, motionY, motionZ);
        player.startAutoSpinAttack(20);
        if (player.onGround()) {
            player.move(MoverType.SELF, new Vec3(0.0D, 1.1999999D, 0.0D));
        }

        SoundEvent sound = riptideLevel >= 3 ? SoundEvents.TRIDENT_RIPTIDE_3 : riptideLevel == 2 ? SoundEvents.TRIDENT_RIPTIDE_2 : SoundEvents.TRIDENT_RIPTIDE_1;
        level.playSound(null, player, sound, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    public static boolean isHoldingTridentPlus(Player player) {
        if (player == null) return false;
        return player.getMainHandItem().is(ModWeapons.TRIDENT_PLUS.get())
                || player.getOffhandItem().is(ModWeapons.TRIDENT_PLUS.get());
    }

    public static ItemStack getHeldTridentPlusStack(Player player) {
        if (player == null) return ItemStack.EMPTY;
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.is(ModWeapons.TRIDENT_PLUS.get())) return mainHand;
        ItemStack offHand = player.getOffhandItem();
        return offHand.is(ModWeapons.TRIDENT_PLUS.get()) ? offHand : ItemStack.EMPTY;
    }

    public static boolean hasHeavenlyThunder(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return EnchantmentHelper.getTagEnchantmentLevel(ModEnchantments.HEAVENLY_THUNDER.get(), stack) > 0;
    }

    public static boolean isHoldingHeavenlyThunderTrident(Player player) {
        return hasHeavenlyThunder(getHeldTridentPlusStack(player));
    }

    public static InteractionHand getHeldHeavenlyThunderHand(Player player) {
        if (player == null) return null;
        if (hasHeavenlyThunder(player.getMainHandItem())) return InteractionHand.MAIN_HAND;
        if (hasHeavenlyThunder(player.getOffhandItem())) return InteractionHand.OFF_HAND;
        return null;
    }

    public static boolean hasEnoughHeavenlyThunderFood(Player player) {
        if (player == null) return false;
        if (player.isCreative() || player.isSpectator()) return true;
        return player.getFoodData().getFoodLevel() >= HEAVENLY_THUNDER_FOOD_COST;
    }

    public static boolean trySpawnHeavenlyThunder(Player player) {
        if (player == null) return false;
        if (player.level().isClientSide()) return false;
        if (player.isSpectator()) return false;
        if (!isHoldingHeavenlyThunderTrident(player)) return false;
        if (!hasEnoughHeavenlyThunderFood(player)) return false;

        HeavenlyThunderEntity thunder = new HeavenlyThunderEntity(ModEntities.HEAVENLY_THUNDER.get(), player.level());
        thunder.setThunderData(player.position(), player, player.getRandom().nextInt());
        player.level().addFreshEntity(thunder);
        PlayerUtil.deductFood(player, HEAVENLY_THUNDER_FOOD_COST);
        return true;
    }
}
