package org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.EntitySlimeCannonBall;
import org.xiaoyang.ex_enigmaticlegacy.Font.StyleMarker;
import org.xiaoyang.ex_enigmaticlegacy.api.exboapi.IWaveName;
import top.theillusivec4.curios.api.CuriosApi;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.client.fx.WispParticleData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class SlimeCannon extends Item implements IWaveName {
    public static final int MAX_SLIME_SIZE    = 10;
    public static final int MANA_PER_ABSORB   = 1000;
    public static final int MANA_PER_SHOT     = 1000;
    public static final int MAX_MANA          = 1000000;
    public static final float BASE_DAMAGE     = 15.0F;
    public static final float DAMAGE_PER_SIZE = 5.0F;
    private static final int ABSORB_COOLDOWN_TICKS = 15;
    private static final String TAG_SLIME_PREFIX = "slime_";
    private static final String TAG_MODE_ABSORB  = "ModeAbsorb";
    private static final String TAG_ABSORB_TIMER = "AbsorbTimer";

    public SlimeCannon() {
        super(new Properties()
                .stacksTo(1)
        );
    }

    @Override
    public WaveStyle getWaveStyle(ItemStack stack) {
        return WaveStyle.HOLY;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {
            private final LazyOptional<SlimeCannonManaItem> opt =
                    LazyOptional.of(() -> new SlimeCannonManaItem(stack));

            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(
                    @Nonnull Capability<T> cap, @Nullable Direction side) {
                return cap == BotaniaForgeCapabilities.MANA_ITEM
                        ? opt.cast()
                        : LazyOptional.empty();
            }
        };
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            if (player.isShiftKeyDown()) {
                boolean newMode = !isAbsorbMode(stack);
                setAbsorbMode(stack, newMode);
                stack.getOrCreateTag().putInt(TAG_ABSORB_TIMER, 0);

                player.displayClientMessage(
                        Component.translatable(
                                newMode
                                        ? "message.slime_cannon.switch_absorb"
                                        : "message.slime_cannon.switch_fire"
                        ).withStyle(newMode ? ChatFormatting.GREEN : ChatFormatting.YELLOW),
                        true);

                return InteractionResultHolder.success(stack);
            }
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (!(entity instanceof Player player)) return;
        if (level.isClientSide) return;

        if (isAbsorbMode(stack)) {
            tickAbsorb(stack, player, level);
        }
    }

    private void tickAbsorb(ItemStack stack, Player player, Level level) {
        CompoundTag tag = stack.getOrCreateTag();
        int timer = tag.getInt(TAG_ABSORB_TIMER);
        if (timer > 0) {
            tag.putInt(TAG_ABSORB_TIMER, timer - 1);
            return;
        }
        var hit = ProjectileUtil.getEntityHitResult(
                level, player,
                player.getEyePosition(),
                player.getEyePosition().add(player.getLookAngle().scale(8.0)),
                player.getBoundingBox().inflate(8.0),
                e -> e instanceof Slime s && s.isAlive() && !s.isInvisible()
        );
        if (hit != null && hit.getEntity() instanceof Slime slime) {
            tryAbsorbSlime(stack, slime, player, level);
            int cooldown = ABSORB_COOLDOWN_TICKS;
            if (hasSlimeNecklace(player)) {
                cooldown = (int) (cooldown * SlimeNecklace.ABSORB_COOLDOWN_MULTIPLIER);
            }
            tag.putInt(TAG_ABSORB_TIMER, cooldown);
        } else {
            player.displayClientMessage(
                    Component.translatable("message.slime_cannon.absorbing")
                            .withStyle(ChatFormatting.GRAY),
                    true);
        }
    }

    private void tryAbsorbSlime(ItemStack stack, Slime slime, Player player, Level level) {
        int slimeSize = slime.getSize();
        int manaRequired = MANA_PER_ABSORB * slimeSize;

        if (hasSlimeNecklace(player)) {
            manaRequired = (int) (manaRequired * SlimeNecklace.ABSORB_MANA_DISCOUNT);
        }

        if (getMana(stack) < manaRequired) {
            player.displayClientMessage(
                    Component.translatable("message.slime_cannon.no_mana_absorb", manaRequired)
                            .withStyle(ChatFormatting.RED),
                    true);
            return;
        }

        addMana(stack, -manaRequired);

        int clamped = Math.min(slimeSize, MAX_SLIME_SIZE);
        setSlimeCount(stack, clamped, getSlimeCount(stack, clamped) + 1);

        slime.discard();

        level.playSound(null, slime.blockPosition(),
                SoundEvents.SLIME_SQUISH, SoundSource.PLAYERS,
                1.0F, 0.8F + slimeSize * 0.1F);

        spawnAbsorbParticles(level, slime);

        player.displayClientMessage(
                Component.translatable("message.slime_cannon.absorbed", slimeSize, getMana(stack))
                        .withStyle(ChatFormatting.GREEN),
                true);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (!(entity instanceof Player player)) return;
        if (level.isClientSide) return;

        if (isAbsorbMode(stack)) {
            player.displayClientMessage(
                    Component.translatable("message.slime_cannon.release_absorb")
                            .withStyle(ChatFormatting.GREEN),
                    true);
            return;
        }

        fireSlime(stack, player, level);
    }

    private void fireSlime(ItemStack stack, Player player, Level level) {
        int shootSize = getLargestStoredSize(stack);

        if (shootSize <= 0) {
            player.displayClientMessage(
                    Component.translatable("message.slime_cannon.no_slime")
                            .withStyle(ChatFormatting.RED),
                    true);
            return;
        }

        if (getMana(stack) < MANA_PER_SHOT) {
            player.displayClientMessage(
                    Component.translatable("message.slime_cannon.no_mana_fire", MANA_PER_SHOT)
                            .withStyle(ChatFormatting.RED),
                    true);
            return;
        }

        addMana(stack, -MANA_PER_SHOT);
        setSlimeCount(stack, shootSize, getSlimeCount(stack, shootSize) - 1);

        float damage = BASE_DAMAGE + shootSize * DAMAGE_PER_SIZE;

        EntitySlimeCannonBall ball = new EntitySlimeCannonBall(level, player, damage, shootSize);
        ball.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 0.3F);
        level.addFreshEntity(ball);

        level.playSound(null, player.blockPosition(),
                SoundEvents.SLIME_ATTACK, SoundSource.PLAYERS, 1.0F, 0.6F);

        int nextSize = getLargestStoredSize(stack);
        if (nextSize > 0) {
            player.displayClientMessage(
                    Component.translatable("message.slime_cannon.fired", shootSize, nextSize)
                            .withStyle(ChatFormatting.YELLOW),
                    true);
        } else {
            player.displayClientMessage(
                    Component.translatable("message.slime_cannon.fired_empty", shootSize)
                            .withStyle(ChatFormatting.YELLOW),
                    true);
        }
    }

    public static boolean isAbsorbMode(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean(TAG_MODE_ABSORB);
    }

    public static void setAbsorbMode(ItemStack stack, boolean absorb) {
        stack.getOrCreateTag().putBoolean(TAG_MODE_ABSORB, absorb);
    }

    public static int getSlimeCount(ItemStack stack, int size) {
        if (size < 1 || size > MAX_SLIME_SIZE) return 0;
        return stack.getOrCreateTag().getInt(TAG_SLIME_PREFIX + size);
    }

    public static void setSlimeCount(ItemStack stack, int size, int count) {
        if (size < 1 || size > MAX_SLIME_SIZE) return;
        stack.getOrCreateTag().putInt(TAG_SLIME_PREFIX + size, Math.max(0, count));
    }

    public static int getLargestStoredSize(ItemStack stack) {
        for (int size = MAX_SLIME_SIZE; size >= 1; size--) {
            if (getSlimeCount(stack, size) > 0) return size;
        }
        return 0;
    }

    public static int getTotalSlimeCount(ItemStack stack) {
        int total = 0;
        for (int size = 1; size <= MAX_SLIME_SIZE; size++) {
            total += getSlimeCount(stack, size);
        }
        return total;
    }

    public static int getMana(ItemStack stack) {
        return stack.getCapability(BotaniaForgeCapabilities.MANA_ITEM)
                .map(ManaItem::getMana).orElse(0);
    }

    private static void addMana(ItemStack stack, int amount) {
        stack.getCapability(BotaniaForgeCapabilities.MANA_ITEM)
                .ifPresent(m -> m.addMana(amount));
    }

    private static boolean hasSlimeNecklace(Player player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, stack -> stack.getItem() instanceof SlimeNecklace)
                .isPresent();
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * getMana(stack) / MAX_MANA);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return isAbsorbMode(stack) ? 0x00FF88 : 0xFF8800;
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
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int mana      = getMana(stack);
        int total     = getTotalSlimeCount(stack);
        int nextSize  = getLargestStoredSize(stack);
        boolean absorbMode = isAbsorbMode(stack);

        tooltip.add(Component.translatable("tooltip.slime_cannon.title")
                .withStyle(ChatFormatting.GOLD));

        tooltip.add(Component.translatable(
                        absorbMode
                                ? "tooltip.slime_cannon.mode_absorb"
                                : "tooltip.slime_cannon.mode_fire")
                .withStyle(absorbMode ? ChatFormatting.GREEN : ChatFormatting.YELLOW));

        tooltip.add(Component.translatable("tooltip.slime_cannon.mana", mana, MAX_MANA)
                .withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("tooltip.slime_cannon.total", total)
                .withStyle(ChatFormatting.GREEN));

        boolean hasAny = false;
        for (int size = MAX_SLIME_SIZE; size >= 1; size--) {
            int count = getSlimeCount(stack, size);
            if (count > 0) {
                float dmg = BASE_DAMAGE + size * DAMAGE_PER_SIZE;
                tooltip.add(Component.translatable(
                                "tooltip.slime_cannon.entry",
                                size, count, String.format("%.1f", dmg))
                        .withStyle(ChatFormatting.WHITE));
                hasAny = true;
            }
        }
        if (!hasAny) {
            tooltip.add(Component.translatable("tooltip.slime_cannon.empty")
                    .withStyle(ChatFormatting.GRAY));
        }

        if (nextSize > 0) {
            tooltip.add(Component.translatable("tooltip.slime_cannon.next", nextSize)
                    .withStyle(ChatFormatting.AQUA));
        }

        tooltip.add(Component.translatable("tooltip.slime_cannon.hint_switch")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.slime_cannon.hint_absorb")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.slime_cannon.hint_fire")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.slime_cannon.cost_absorb", MANA_PER_ABSORB)
                .withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("tooltip.slime_cannon.cost_fire", MANA_PER_SHOT)
                .withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("tooltip.bad_human")
                .withStyle(StyleMarker.glitch()));
    }

    private void spawnAbsorbParticles(Level level, Slime slime) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        WispParticleData data = WispParticleData.wisp(0.6F, 0.2F, 0.9F, 0.4F, 1.0F);
        serverLevel.sendParticles(data,
                slime.getX(), slime.getY() + slime.getBbHeight() * 0.5, slime.getZ(),
                15, 0.4, 0.4, 0.4, 0.08);

        WispParticleData flash = WispParticleData.wisp(1.0F, 1.0F, 1.0F, 1.0F, 1.0F);
        serverLevel.sendParticles(flash,
                slime.getX(), slime.getY() + slime.getBbHeight() * 0.5, slime.getZ(),
                5, 0.1, 0.1, 0.1, 0.15);
    }

    public static class SlimeCannonManaItem implements ManaItem {
        private static final String TAG_MANA = "StoredMana";
        private final ItemStack stack;

        public SlimeCannonManaItem(ItemStack stack) {
            this.stack = stack;
        }

        @Override public int getMana() {
            return stack.getOrCreateTag().getInt(TAG_MANA);
        }

        @Override public int getMaxMana() {
            return MAX_MANA;
        }

        @Override public void addMana(int mana) {
            int newMana = Math.max(0, Math.min(getMana() + mana, MAX_MANA));
            stack.getOrCreateTag().putInt(TAG_MANA, newMana);
        }

        @Override public boolean canReceiveManaFromPool(BlockEntity pool) {
            return getMana() < MAX_MANA;
        }

        @Override public boolean canReceiveManaFromItem(ItemStack other) {
            return getMana() < MAX_MANA;
        }

        @Override public boolean canExportManaToPool(BlockEntity pool) {
            return false;
        }

        @Override public boolean canExportManaToItem(ItemStack other) {
            return false;
        }

        @Override public boolean isNoExport() {
            return true;
        }
    }
}