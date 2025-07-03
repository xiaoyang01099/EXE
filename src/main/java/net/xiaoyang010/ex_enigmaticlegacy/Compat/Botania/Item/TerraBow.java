package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.item.equipment.tool.bow.ItemLivingwoodBow;
import java.util.List;

public class TerraBow extends ItemLivingwoodBow {
    private static final int ARROW_COST = 400;
    private static final int ARROW_ROWS = 3;
    private static final int ARROW_COLS = 3;
    private static final double ARROW_BASE_DAMAGE = 10.0;
    private static final double INSTANT_KILL_CHANCE = 0.05;

    public TerraBow(Properties builder) {
        super(builder);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);

        tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.terra_bow.tooltip.line1"));
        tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.terra_bow.tooltip.line2"));

        tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.terra_bow.tooltip.multishot")
                .withStyle(ChatFormatting.YELLOW));
        tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.terra_bow.tooltip.pattern",
                ARROW_ROWS + "x" + ARROW_COLS).withStyle(ChatFormatting.GRAY));

        tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.terra_bow.tooltip.instant_kill")
                .withStyle(ChatFormatting.RED));
        tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.terra_bow.tooltip.chance",
                String.format("%.1f%%", INSTANT_KILL_CHANCE * 100)).withStyle(ChatFormatting.GRAY));

        boolean hasInfinity = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0;
        int manaCost = ARROW_COST / (hasInfinity ? 2 : 1);
        tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.terra_bow.tooltip.mana_cost", manaCost)
                .withStyle(ChatFormatting.AQUA));

        if (hasInfinity) {
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.terra_bow.tooltip.infinity_bonus")
                    .withStyle(ChatFormatting.GREEN));
        }

        tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.terra_bow.tooltip.base_damage",
                String.format("%.1f", ARROW_BASE_DAMAGE)).withStyle(ChatFormatting.DARK_RED));
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, Player player,
                                                  @Nonnull InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        boolean flag = canFire(itemstack, player);

        InteractionResultHolder<ItemStack> ret = ForgeEventFactory.onArrowNock(itemstack, level,
                player, hand, flag);
        if (ret != null) {
            return ret;
        }

        if (!player.getAbilities().instabuild && !flag) {
            if (!level.isClientSide) {
                player.displayClientMessage(
                        new TranslatableComponent("item.ex_enigmaticlegacy.terra_bow.insufficient_mana")
                                .withStyle(ChatFormatting.RED), true);
            }
            return InteractionResultHolder.fail(itemstack);
        } else {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(itemstack);
        }
    }

    @Override
    public void releaseUsing(@Nonnull ItemStack stack, @Nonnull Level level, @NotNull LivingEntity entityLiving,
                             int timeLeft) {
        if (entityLiving instanceof Player) {
            Player player = (Player) entityLiving;
            boolean flag = canFire(stack, player);
            ItemStack itemstack = player.getProjectile(stack);

            int i = (int) ((getUseDuration(stack) - timeLeft) * chargeVelocityMultiplier());
            i = ForgeEventFactory.onArrowLoose(stack, level, player, i,
                    !itemstack.isEmpty() || flag);
            if (i < 0)
                return;

            if (!itemstack.isEmpty() || flag) {
                if (itemstack.isEmpty()) {
                    itemstack = new ItemStack(Items.ARROW);
                }

                float f = getPowerForTime(i);
                if (!((double) f < 0.1D)) {
                    boolean flag1 = player.getAbilities().instabuild || (itemstack.getItem() instanceof ArrowItem
                            && ((ArrowItem) itemstack.getItem()).isInfinite(itemstack, stack, player));
                    if (!level.isClientSide) {
                        ArrowItem arrowitem = (ArrowItem) (itemstack.getItem() instanceof ArrowItem
                                ? itemstack.getItem()
                                : Items.ARROW);

                        boolean hasInstantKill = false;
                        int arrowCount = 0;

                        int row = 0;
                        while (row < ARROW_ROWS) {
                            int col = 0;
                            while (col < ARROW_COLS) {
                                float pitchAddition = (row - ARROW_ROWS / 2) * 3F;
                                float yawAddition = (col - ARROW_COLS / 2) * 3F;
                                AbstractArrow abstractarrowentity = arrowitem.createArrow(level, itemstack,
                                        player);

                                abstractarrowentity = customArrow(abstractarrowentity);

                                abstractarrowentity.setBaseDamage(ARROW_BASE_DAMAGE);

                                if (level.random.nextDouble() < (INSTANT_KILL_CHANCE / 9)) {
                                    abstractarrowentity.setBaseDamage(Float.MAX_VALUE);
                                    abstractarrowentity.addTag("terra_bow_instant_kill");
                                    hasInstantKill = true;
                                }

                                abstractarrowentity.shootFromRotation(player,
                                        player.getXRot() + pitchAddition,
                                        player.getYRot() + yawAddition, 0.0F, f * 3.0F, 1.0F);
                                if (f == 1.0F) {
                                    abstractarrowentity.setCritArrow(true);
                                }

                                int j = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
                                if (j > 0 && !abstractarrowentity.getTags().contains("terra_bow_instant_kill")) {
                                    abstractarrowentity
                                            .setBaseDamage(abstractarrowentity.getBaseDamage() + (double) j * 0.5D + 0.5D);
                                }

                                int k = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
                                if (k > 0) {
                                    abstractarrowentity.setKnockback(k);
                                }

                                if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack) > 0) {
                                    abstractarrowentity.setSecondsOnFire(100);
                                }

                                onFire(stack, player, flag1, abstractarrowentity);

                                stack.hurtAndBreak(1, player, (p_220009_1_) -> {
                                    p_220009_1_.broadcastBreakEvent(player.getUsedItemHand());
                                });
                                if (flag1 || player.getAbilities().instabuild
                                        && (itemstack.getItem() == Items.SPECTRAL_ARROW
                                        || itemstack.getItem() == Items.TIPPED_ARROW)) {
                                    abstractarrowentity.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                                }

                                level.addFreshEntity(abstractarrowentity);
                                arrowCount++;
                                col++;
                            }
                            row++;
                        }

                        if (hasInstantKill) {
                            player.displayClientMessage(
                                    new TranslatableComponent("item.ex_enigmaticlegacy.terra_bow.instant_kill_triggered")
                                            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), true);
                        }
                    }

                    level.playSound((Player) null, player.getX(), player.getY(),
                            player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F,
                            1.0F / (level.random.nextFloat() * 0.4F + 1.2F) + f * 0.5F);
                    player.awardStat(Stats.ITEM_USED.get(this));
                }
            }
        }
    }

    @Override
    public float chargeVelocityMultiplier() {
        return 2F;
    }

    private boolean canFire(ItemStack stack, Player player) {
        boolean infinity = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0;
        return player.getAbilities().instabuild || ManaItemHandler.instance().requestManaExactForTool(stack, player,
                ARROW_COST / (infinity ? 2 : 1), false);
    }

    private void onFire(ItemStack stack, LivingEntity living, boolean infinity, AbstractArrow arrow) {
        arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        if (living instanceof Player)
            ManaItemHandler.instance().requestManaExactForTool(stack, (Player) living,
                    ARROW_COST / (infinity ? 2 : 1), true);
    }
}