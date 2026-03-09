package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.ForgeEventFactory;
import net.xiaoyang010.ex_enigmaticlegacy.Util.LinkTimeManager;
import net.xiaoyang010.ex_enigmaticlegacy.api.INoEMCItem;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.item.IRelic;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.item.equipment.tool.bow.ItemLivingwoodBow;
import vazkii.botania.common.item.relic.RelicImpl;
import vazkii.botania.xplat.IXplatAbstractions;

import java.util.List;

public class TerraBow extends ItemLivingwoodBow implements INoEMCItem {
    private static final int ARROW_COST = 400;
    private static final int ARROW_ROWS = 3;
    private static final int ARROW_COLS = 3;
    private static final double ARROW_BASE_DAMAGE = 10.0;
    private static final double INSTANT_KILL_CHANCE = 0.05;
    private static final int TIME_COMPENSATION = 4;

    public TerraBow(Properties builder) {
        super(builder);
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
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        RelicImpl.addDefaultTooltip(stack, tooltip);

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

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        if (!world.isClientSide && entity instanceof Player player) {
            var relic = IXplatAbstractions.INSTANCE.findRelic(stack);
            if (relic != null) {
                relic.tickBinding(player);
            }
        }
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, Player player,
                                                  @Nonnull InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        var relicCap = itemstack.getCapability(BotaniaForgeCapabilities.RELIC);
        if (relicCap.isPresent()) {
            IRelic relic = relicCap.orElse(null);
            if (relic != null && !relic.isRightPlayer(player)) {
                return InteractionResultHolder.fail(itemstack);
            }
        }
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
        }
        player.startUsingItem(hand);

        if (!level.isClientSide) {
            LinkTimeManager.activate(player);
        }

        return InteractionResultHolder.consume(itemstack);
    }

    @Override
    public void releaseUsing(@Nonnull ItemStack stack, @Nonnull Level level, @NotNull LivingEntity entityLiving,
                             int timeLeft) {
        if (!level.isClientSide && entityLiving instanceof Player player) {
            LinkTimeManager.deactivate(player);
        }

        if (!(entityLiving instanceof Player player)) return;

        boolean flag = canFire(stack, player);
        ItemStack itemstack = player.getProjectile(stack);
        int chargeTime = (int) ((getUseDuration(stack) - timeLeft) * chargeVelocityMultiplier() * TIME_COMPENSATION);
        chargeTime = ForgeEventFactory.onArrowLoose(stack, level, player, chargeTime,
                !itemstack.isEmpty() || flag);
        if (chargeTime < 0) return;
        if (itemstack.isEmpty() && flag) {
            itemstack = new ItemStack(Items.ARROW);
        }
        float power = getPowerForTime(chargeTime);
        if (power < 0.1D) return;
        boolean infiniteArrows = player.getAbilities().instabuild ||
                (itemstack.getItem() instanceof ArrowItem arrowItem &&
                        arrowItem.isInfinite(itemstack, stack, player));
        if (!level.isClientSide) {
            ArrowItem arrowitem = (ArrowItem) (itemstack.getItem() instanceof ArrowItem ?
                    itemstack.getItem() : Items.ARROW);
            boolean hasInstantKill = false;
            for (int row = 0; row < ARROW_ROWS; row++) {
                for (int col = 0; col < ARROW_COLS; col++) {
                    float pitchOffset = (row - ARROW_ROWS / 2) * 3F;
                    float yawOffset = (col - ARROW_COLS / 2) * 3F;
                    AbstractArrow arrow = arrowitem.createArrow(level, itemstack, player);
                    arrow = customArrow(arrow);
                    arrow.setBaseDamage(ARROW_BASE_DAMAGE);
                    arrow.life = 1100;
                    if (level.random.nextDouble() < (INSTANT_KILL_CHANCE / 9)) {
                        arrow.setBaseDamage(Float.MAX_VALUE);
                        arrow.addTag("instant_kill");
                        hasInstantKill = true;
                    }
                    arrow.shootFromRotation(player,
                            player.getXRot() + pitchOffset,
                            player.getYRot() + yawOffset,
                            0.0F, power * 3.0F, 1.0F);
                    arrow.setNoGravity(true);
                    arrow.setInvisible(true);
                    if (power == 1.0F) {
                        arrow.setCritArrow(true);
                    }
                    applyEnchantments(stack, arrow);
                    onFire(stack, player, infiniteArrows, arrow);
                    stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
                    if (infiniteArrows) {
                        arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                    }
                    level.addFreshEntity(arrow);
                }
            }
            if (hasInstantKill) {
                player.displayClientMessage(
                        new TranslatableComponent("item.ex_enigmaticlegacy.terra_bow.instant_kill_triggered")
                                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), true);
            }
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F,
                1.0F / (level.random.nextFloat() * 0.4F + 1.2F) + power * 0.5F);
        if (!infiniteArrows && !player.getAbilities().instabuild) {
            itemstack.shrink(1);
            if (itemstack.isEmpty()) {
                player.getInventory().removeItem(itemstack);
            }
        }
        player.awardStat(Stats.ITEM_USED.get(this));
    }

    private void applyEnchantments(ItemStack bow, AbstractArrow arrow) {
        int power = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, bow);
        if (power > 0 && !arrow.getTags().contains("instant_kill")) {
            arrow.setBaseDamage(arrow.getBaseDamage() + power * 0.5D + 0.5D);
        }
        int punch = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, bow);
        if (punch > 0) {
            arrow.setKnockback(punch);
        }
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, bow) > 0) {
            arrow.setSecondsOnFire(100);
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