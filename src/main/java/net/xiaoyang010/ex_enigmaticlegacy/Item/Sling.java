package net.xiaoyang010.ex_enigmaticlegacy.Item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.others.EntitySlingBullet;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage.MessageParticleEntity;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage.MessagePlayerAction;

public class Sling extends Item {
    public static final int USING_COUNT_MIN = (1 * 20);
    public static final int CHARGE_AMOUNT_MIN = 1;
    public static final int CHARGE_AMOUNT_MAX = 10;
    public static final int SWING_ARM = 0;
    public static final int CHARGE_INTERVAL = (20 / 2);
    public static final int ENTITY_SILING_CHAGE = 0;
    public static final int ENTITY_SILING_CHAGE_MAX = 1;

    public Sling() {
        super(new Properties().stacksTo(1).tab(ModTabs.TAB_EXENIGMATICLEGACY_ITEM));
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if (enchantment.category == EnchantmentCategory.BOW) {
            return true;
        }
        return super.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (this.getBullet(player).isEmpty()) {
            return InteractionResultHolder.fail(stack);
        } else {
            player.startUsingItem(hand);
            return InteractionResultHolder.success(stack);
        }
    }

    @Override
    public void onUseTick(Level world, LivingEntity player, ItemStack stack, int count) {
        if (world.isClientSide || !(player instanceof Player)) {
            return;
        }

        int usingCount = this.getUsingCount(stack, count);

        if (usingCount < USING_COUNT_MIN) {
            return;
        }

        if (usingCount % 20 == 0) {
            Player entityPlayer = (Player) player;
            int chargeAmount = this.getChargeAmount(stack, usingCount);

            entityPlayer.causeFoodExhaustion(0.15F);

            if (chargeAmount == CHARGE_AMOUNT_MAX) {
                NetworkHandler.CHANNEL.send(
                        PacketDistributor.ALL.noArg(),
                        new MessageParticleEntity(entityPlayer, ENTITY_SILING_CHAGE_MAX)
                );
                world.playSound(null,
                        entityPlayer.blockPosition(),
                        SoundEvents.PLAYER_LEVELUP,
                        SoundSource.PLAYERS,
                        0.25F, 1.0F);
            } else {
                NetworkHandler.CHANNEL.send(
                        PacketDistributor.ALL.noArg(),
                        new MessageParticleEntity(entityPlayer, ENTITY_SILING_CHAGE)
                );
                world.playSound(null,
                        entityPlayer.blockPosition(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP,
                        SoundSource.PLAYERS,
                        0.25F,
                        (0.5F + ((float) chargeAmount / 10)));
            }
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level world, LivingEntity entityLiving, int timeLeft) {
        if (world.isClientSide || !(entityLiving instanceof Player player)) {
            return;
        }

        int usingCount = this.getUsingCount(stack, timeLeft);

        ItemStack bullet = this.getBullet(entityLiving);

        if ((usingCount < USING_COUNT_MIN) || bullet.isEmpty()) {
            return;
        }

        float chargeAmount = (float) this.getChargeAmount(stack, usingCount);

        EntitySlingBullet entitySlingBullet = new EntitySlingBullet(
                world, player, bullet.copy(), (int) chargeAmount
        );

        int punch = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
        if (punch > 0) {
            entitySlingBullet.setKnockback(punch);
        }

        int flame = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack);
        if (flame > 0) {
            entitySlingBullet.setSecondsOnFire(flame * 10);
        }

        float velocity = (1.0F + (chargeAmount / 10));
        float inaccuracy = (1.1F - (chargeAmount / 10));

        entitySlingBullet.shootFromRotation(
                player, player.getXRot(), player.getYRot(),
                0.0F, velocity, inaccuracy
        );

        world.addFreshEntity(entitySlingBullet);

        if (!player.getAbilities().instabuild) {
            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) <= 0) {
                bullet.shrink(1);
            }
            stack.hurtAndBreak(1, entityLiving,
                    (e) -> e.broadcastBreakEvent(player.getUsedItemHand()));
        }

        player.getCooldowns().addCooldown(this, CHARGE_INTERVAL);
        player.awardStat(Stats.ITEM_USED.get(this));

        NetworkHandler.CHANNEL.send(
                PacketDistributor.ALL.noArg(),
                new MessagePlayerAction(player, SWING_ARM)
        );

        world.playSound(null,
                player.getX(), player.getY(), player.getZ(),
                SoundEvents.EGG_THROW,
                SoundSource.PLAYERS,
                0.5F,
                0.4F / (world.random.nextFloat() * 0.4F + 0.8F));
    }

    public int getUsingCount(ItemStack stack, int timeLeft) {
        return (this.getUseDuration(stack) - timeLeft);
    }

    public int getChargeAmount(ItemStack stack, int usingCount) {
        int chargeAmount = (usingCount / 20)
                + EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
        chargeAmount = Math.min(chargeAmount, CHARGE_AMOUNT_MAX);
        chargeAmount = Math.max(chargeAmount, CHARGE_AMOUNT_MIN);
        return chargeAmount;
    }

    public boolean isBullet(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.getItem() instanceof BlockItem;
    }

    public ItemStack getBullet(LivingEntity entityLiving) {
        if (!(entityLiving instanceof Player player)) return ItemStack.EMPTY;

        ItemStack offHandStack = player.getItemInHand(InteractionHand.OFF_HAND);
        ItemStack mainHandStack = player.getItemInHand(InteractionHand.MAIN_HAND);

        if (this.isBullet(offHandStack)) {
            return offHandStack;
        }

        if (this.isBullet(mainHandStack)
                && !(mainHandStack.getItem() instanceof Sling)) {
            return mainHandStack;
        }

        for (int slot = 0; slot < player.getInventory().getContainerSize(); ++slot) {
            ItemStack slotStack = player.getInventory().getItem(slot);
            if (this.isBullet(slotStack)) {
                return slotStack;
            }
        }

        return ItemStack.EMPTY;
    }
}