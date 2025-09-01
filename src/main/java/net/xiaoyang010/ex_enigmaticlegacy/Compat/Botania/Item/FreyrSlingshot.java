package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.others.EntityManaVine;
import vazkii.botania.api.mana.IManaItem;
import vazkii.botania.api.mana.ManaItemHandler;

public class FreyrSlingshot extends Item implements IManaItem {
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

        boolean hasEnoughMana = getMana() >= MANA_COST ||
                ManaItemHandler.instance().requestManaExactForTool(stack, player, MANA_COST, false);

        if (hasEnoughMana) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }

        return InteractionResultHolder.fail(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) {
            return;
        }

        int useTicks = this.getUseDuration(stack) - timeLeft;
        float power = getPowerForTime(useTicks);

        if (power >= 1.0F) {
            if (!level.isClientSide) {
                boolean consumed = false;

                if (getMana() >= MANA_COST) {
                    addMana(-MANA_COST);
                    consumed = true;
                }
                else if (ManaItemHandler.instance().requestManaExactForTool(stack, player, MANA_COST, true)) {
                    consumed = true;
                }

                if (consumed) {
                    EntityManaVine manaVine = new EntityManaVine(level, player);

                    manaVine.setDeltaMovement(
                            manaVine.getDeltaMovement().x * 0.9,
                            manaVine.getDeltaMovement().y * 0.9,
                            manaVine.getDeltaMovement().z * 0.9
                    );

                    manaVine.setAttacker(player.getStringUUID());

                    level.addFreshEntity(manaVine);

                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 0.4F, 2.8F);
                }
            }
        }
    }

    public static float getPowerForTime(int useTicks) {
        float power = (float) useTicks / 20.0F;
        power = (power * power + power * 2.0F) / 3.0F;

        if (power > 1.0F) {
            power = 1.0F;
        }

        return power;
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

    @Override
    public int getMana() {
        return 1000;
    }

    @Override
    public int getMaxMana() {
        return MAX_MANA;
    }

    @Override
    public void addMana(int mana) {
    }

    @Override
    public boolean canReceiveManaFromPool(BlockEntity pool) {
        return getMana() < getMaxMana();
    }

    @Override
    public boolean canReceiveManaFromItem(ItemStack stack) {
        return getMana() < getMaxMana();
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

    public boolean hasEnoughMana(Player player, ItemStack stack) {
        return getMana() >= MANA_COST ||
                ManaItemHandler.instance().requestManaExactForTool(stack, player, MANA_COST, false);
    }

    public int getManaCost() {
        return MANA_COST;
    }
}