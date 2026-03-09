package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic;

import com.integral.enigmaticlegacy.api.items.ICursed;
import com.integral.enigmaticlegacy.helpers.ItemLoreHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.others.EntityChaoticOrb;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEntities;
import net.xiaoyang010.ex_enigmaticlegacy.api.INoEMCItem;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.item.IRelic;
import vazkii.botania.common.item.relic.RelicImpl;
import vazkii.botania.xplat.IXplatAbstractions;

import java.util.List;

public class ChaosTome extends Item implements INoEMCItem, ICursed {
    private static final float CHAOS_TOME_DAMAGE_CAP = 100.0F;
    private static final int EXPERIENCE_COST_PER_ORB = 2;
    private static final float SEEKER_CHANCE = 0.35F;

    public ChaosTome(Properties properties) {
        super(properties);
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
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide && entity instanceof Player player) {
            var relic = IXplatAbstractions.INSTANCE.findRelic(stack);
            if (relic != null) {
                relic.tickBinding(player);
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
        if (relicCap.isPresent()) {
            IRelic relic = relicCap.orElse(null);
            if (relic != null && !relic.isRightPlayer(player)) {
                return InteractionResultHolder.fail(stack);
            }
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
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
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (!(entity instanceof Player player)) return;

        var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
        if (relicCap.isPresent()) {
            IRelic relic = relicCap.orElse(null);
            if (relic != null && !relic.isRightPlayer(player)) {
                player.stopUsingItem();
                return;
            }
        }

        int usedTicks = getUseDuration(stack) - remainingUseDuration;
        if (usedTicks != 0 && usedTicks % 2 == 0 && !level.isClientSide) {
            if (player.totalExperience >= EXPERIENCE_COST_PER_ORB) {
                player.giveExperiencePoints(-EXPERIENCE_COST_PER_ORB);
                spawnOrb(level, player);
            }
        }
    }

    private void spawnOrb(Level level, Player player) {
        if (level.isClientSide) return;

        boolean isSeeker = Math.random() <= SEEKER_CHANCE;

        EntityChaoticOrb orb = new EntityChaoticOrb(ModEntities.CHAOTIC_ORB.get(), level, player, isSeeker);

        Vec3 playerPos = player.position();
        double offsetX = (Math.random() - 0.5) * 3.0;
        double offsetY = (Math.random() - 0.5) * 1.0;
        double offsetZ = (Math.random() - 0.5) * 3.0;

        orb.setPos(
                playerPos.x + offsetX,
                playerPos.y + offsetY,
                playerPos.z + offsetZ
        );

        Vec3 orbPos = orb.position();
        Vec3 direction = orbPos.subtract(playerPos).normalize();
        double speed = 0.2 + Math.random() * 0.2;

        orb.setDeltaMovement(
                direction.x * speed,
                direction.y * speed,
                direction.z * speed
        );

        level.addFreshEntity(orb);

        if (level instanceof ServerLevel) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENCHANTMENT_TABLE_USE, player.getSoundSource(),
                    0.3F, 0.8F + level.random.nextFloat() * 0.1F);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        ItemLoreHelper.indicateCursedOnesOnly(tooltip);
        RelicImpl.addDefaultTooltip(stack, tooltip);
        if (Screen.hasShiftDown()) {
            tooltip.add(new TranslatableComponent("tooltip.chaos_tome.advanced1")
                    .withStyle(ChatFormatting.DARK_PURPLE));
            tooltip.add(new TranslatableComponent("tooltip.chaos_tome.advanced2")
                    .withStyle(ChatFormatting.DARK_PURPLE));
            tooltip.add(new TranslatableComponent("tooltip.chaos_tome.damage",
                    1, (int)CHAOS_TOME_DAMAGE_CAP).withStyle(ChatFormatting.RED));
            tooltip.add(new TranslatableComponent("tooltip.chaos_tome.cost", EXPERIENCE_COST_PER_ORB)
                    .withStyle(ChatFormatting.GREEN));
        } else {
            tooltip.add(new TranslatableComponent("tooltip.chaos_tome.desc")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(new TranslatableComponent("tooltip.chaos_tome.usage")
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }

    @Override
    public int getEnchantmentValue() {
        return 1;
    }
}