package net.xiaoyang010.ex_enigmaticlegacy.Item.weapon;

import morph.avaritia.entity.InfinityArrowEntity;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractArrow.Pickup;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.others.ManaitaArrow;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRarities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class ManaitaBow extends BowItem {
    private static final String TAG_ARROW_MODE = "ArrowMode";
    private final Random random = new Random();

    public ManaitaBow() {
        super(new Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR)
                .rarity(ModRarities.MIRACLE)
        );
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isCrouching()) {
            if (!level.isClientSide) {
                toggleArrowMode(stack, player);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        InteractionResultHolder<ItemStack> ret = ForgeEventFactory.onArrowNock(stack, level, player, hand, true);
        if (ret != null) {
            return ret;
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (entity instanceof Player player) {
            if (!level.isClientSide) {
                AbstractArrow arrow;

                if (isManaMode(stack)) {
                    // 魔力箭模式直接发射满威力的魔力箭
                    ManaitaArrow manaitaArrow = new ManaitaArrow(level, player);
                    manaitaArrow.setCritArrow(true);
                    manaitaArrow.setPierceLevel((byte) 5);
                    arrow = manaitaArrow;

                    // 发射特效和音效
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.PLAYERS, 0.5F, 1.0F + random.nextFloat() * 0.2F);

                    // 生成发射粒子效果
                    ((ServerLevel)level).sendParticles(ParticleTypes.PORTAL,
                            player.getX(), player.getY() + 1.0, player.getZ(),
                            20,
                            0.5,
                            0.5,
                            0.5,
                            0.1
                    );

                    arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 4.0F, 1.0F);
                } else {
                    // 普通模式使用无尽箭
                    InfinityArrowEntity infinityArrow = new InfinityArrowEntity(level, player);
                    infinityArrow.setSpectral(200);
                    infinityArrow.setCritArrow(true);
                    infinityArrow.setJumpCount(0);
                    infinityArrow.setBaseDamage(Float.POSITIVE_INFINITY);

                    // 计算力量仅影响速度
                    int i = this.getUseDuration(stack) - timeLeft;
                    float power = getPowerForTime(i);

                    arrow = infinityArrow;
                    arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 3.0F, 1.0F);
                }

                arrow.pickup = Pickup.CREATIVE_ONLY;
                level.addFreshEntity(arrow);

                // 发射音效
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
                player.awardStat(Stats.ITEM_USED.get(this));
            }
        }
    }

    private void toggleArrowMode(ItemStack stack, Player player) {
        CompoundTag tag = stack.getOrCreateTag();
        boolean currentMode = tag.getBoolean(TAG_ARROW_MODE);
        tag.putBoolean(TAG_ARROW_MODE, !currentMode);

        String translationKey = !currentMode ?
                "message.arrow_mode.chopping_block" :
                "message.arrow_mode.infinite";
        player.displayClientMessage(new TranslatableComponent(translationKey), true);

        player.level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5F, 1.0F);
    }

    private boolean isManaMode(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(TAG_ARROW_MODE);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        String modeKey = isManaMode(stack) ?
                "tooltip.bow.mode.chopping_block" :
                "tooltip.bow.mode.infinite";

        tooltip.add(new TranslatableComponent("tooltip.bow.mode.current",
                new TranslatableComponent(modeKey)));
        tooltip.add(new TranslatableComponent("tooltip.bow.mode.switch"));
        tooltip.add(new TranslatableComponent("tooltip.bow.mode.chopping_block.desc"));
        tooltip.add(new TranslatableComponent("tooltip.bow.mode.infinite.desc"));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canBeDepleted() {
        return false;
    }
}