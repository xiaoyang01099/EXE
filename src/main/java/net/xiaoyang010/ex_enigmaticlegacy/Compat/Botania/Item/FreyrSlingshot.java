package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.others.EntityManaVine;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.mana.IManaItem;
import vazkii.botania.api.mana.ManaBarTooltip;
import vazkii.botania.api.mana.ManaItemHandler;

import javax.annotation.Nonnull;
import java.util.Optional;

public class FreyrSlingshot extends Item {
    protected static final int MAX_MANA = 50000;
    private static final String TAG_MANA = "mana";
    private static final int MANA_COST = 5000;
    private static final int USE_DURATION = 42000;

    public FreyrSlingshot(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!hasSufficientMana(stack, player)) {
            return InteractionResultHolder.fail(stack);
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) {
            return;
        }

        int useTicks = this.getUseDuration(stack) - timeLeft;
        float power = getPowerForTime(useTicks);

        if (power >= 1.0F && !level.isClientSide) {
            if (consumeMana(stack, player)) {
                EntityManaVine manaVine = new EntityManaVine(level, player);
                level.addFreshEntity(manaVine);

                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 0.4F, 2.8F);
            }
        }
    }

    public static float getPowerForTime(int useTicks) {
        float power = (float) useTicks / 20.0F;
        power = (power * power + power * 2.0F) / 3.0F;
        return Math.min(power, 1.0F);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return USE_DURATION;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }

    private boolean hasSufficientMana(ItemStack stack, Player player) {
        int rodMana = getManaInternal(stack);
        if (rodMana >= MANA_COST) {
            return true;
        }

        int remainingCost = MANA_COST - rodMana;
        return ManaItemHandler.instance().requestManaExactForTool(stack, player, remainingCost, false);
    }

    private boolean consumeMana(ItemStack stack, Player player) {
        int rodMana = getManaInternal(stack);

        if (rodMana >= MANA_COST) {
            setManaInternal(stack, rodMana - MANA_COST);
            return true;
        } else {
            int remainingCost = MANA_COST - rodMana;
            if (ManaItemHandler.instance().requestManaExactForTool(stack, player, remainingCost, true)) {
                setManaInternal(stack, 0);
                return true;
            }
        }
        return false;
    }

    protected int getManaInternal(ItemStack stack) {
        return stack.getOrCreateTag().getInt(TAG_MANA);
    }

    protected void setManaInternal(ItemStack stack, int mana) {
        stack.getOrCreateTag().putInt(TAG_MANA, Math.min(mana, MAX_MANA));
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ManaItemCapabilityProvider(stack);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return Optional.of(new ManaBarTooltip(getManaInternal(stack) / (float) MAX_MANA));
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return 0;
    }

    private static class ManaItemCapabilityProvider implements ICapabilityProvider {
        private final ItemStack stack;
        private final LazyOptional<IManaItem> manaItemOptional;

        public ManaItemCapabilityProvider(ItemStack stack) {
            this.stack = stack;
            this.manaItemOptional = LazyOptional.of(() -> new ManaItemImpl(stack));
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            if (cap == BotaniaForgeCapabilities.MANA_ITEM) {
                return manaItemOptional.cast();
            }
            return LazyOptional.empty();
        }
    }

    private static class ManaItemImpl implements IManaItem {
        private final ItemStack stack;

        public ManaItemImpl(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public int getMana() {
            return stack.getOrCreateTag().getInt(TAG_MANA);
        }

        @Override
        public int getMaxMana() {
            return MAX_MANA;
        }

        @Override
        public void addMana(int mana) {
            int current = getMana();
            stack.getOrCreateTag().putInt(TAG_MANA, Math.min(current + mana, MAX_MANA));
        }

        @Override
        public boolean canReceiveManaFromPool(BlockEntity pool) {
            return true;
        }

        @Override
        public boolean canReceiveManaFromItem(ItemStack otherStack) {
            return true;
        }

        @Override
        public boolean canExportManaToPool(BlockEntity pool) {
            return false;
        }

        @Override
        public boolean canExportManaToItem(ItemStack otherStack) {
            return false;
        }

        @Override
        public boolean isNoExport() {
            return true;
        }
    }

    public boolean hasEnoughMana(Player player, ItemStack stack) {
        return hasSufficientMana(stack, player);
    }

    public int getManaCost() {
        return MANA_COST;
    }
}