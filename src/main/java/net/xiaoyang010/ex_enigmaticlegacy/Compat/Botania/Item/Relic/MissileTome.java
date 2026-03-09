package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.others.EntityRageousMissile;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEntities;
import net.xiaoyang010.ex_enigmaticlegacy.api.INoEMCItem;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.item.IRelic;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.item.relic.RelicImpl;
import vazkii.botania.xplat.IXplatAbstractions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MissileTome extends Item implements INoEMCItem {

    private static final int MANA_COST = 150;
    private static final int USE_DURATION = 72000;
    private static final int MISSILE_INTERVAL = 2;
    private static final float DAMAGE_MIN = 24.0F;
    private static final float DAMAGE_MAX = 32.0F;

    public MissileTome(Properties properties) {
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
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level world, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        RelicImpl.addDefaultTooltip(stack, tooltip);

        if (Screen.hasShiftDown()) {
            tooltip.add(new TranslatableComponent("item.missile_tome.description1"));
            tooltip.add(new TranslatableComponent("item.missile_tome.description2"));
            tooltip.add(new TranslatableComponent("item.missile_tome.description3"));
            tooltip.add(new TranslatableComponent(""));
            tooltip.add(new TranslatableComponent("item.missile_tome.damage_info",
                    (int)DAMAGE_MIN, (int)DAMAGE_MAX));
            tooltip.add(new TranslatableComponent(""));
            tooltip.add(new TranslatableComponent("item.missile_tome.mana_cost", MANA_COST));
        } else {
            tooltip.add(new TranslatableComponent("item.FRShiftTooltip.lore"));
        }
        super.appendHoverText(stack, world, tooltip, flag);
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
    public @Nonnull UseAnim getUseAnimation(@Nonnull ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(@Nonnull ItemStack stack) {
        return USE_DURATION;
    }

    @Override
    public @Nonnull InteractionResultHolder<ItemStack> use(@Nonnull Level world, @Nonnull Player player, @Nonnull InteractionHand hand) {
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
    public void onUseTick(@Nonnull Level world, @Nonnull LivingEntity entity, @Nonnull ItemStack stack, int remainingUseDuration) {
        if (!(entity instanceof Player player)) {
            return;
        }

        var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
        if (relicCap.isPresent()) {
            IRelic relic = relicCap.orElse(null);
            if (relic != null && !relic.isRightPlayer(player)) {
                player.stopUsingItem();
                return;
            }
        }

        int usedDuration = this.getUseDuration(stack) - remainingUseDuration;

        if (usedDuration != 0 && usedDuration % MISSILE_INTERVAL == 0 && !world.isClientSide) {
            if (ManaItemHandler.instance().requestManaExact(stack, player, MANA_COST, true)) {
                double x = player.getX() + (Math.random() - 0.5) * 3.1;
                double y = player.getY() + 3.8 + (Math.random() - 1.55);
                double z = player.getZ() + (Math.random() - 0.5) * 3.1;

                this.spawnMissile(world, player, x, y, z);
            }

            BotaniaAPI.instance().sparkleFX(world,
                    player.getX(), player.getY() + 2.4, player.getZ(),
                    1.0F, 0.4F, 1.0F, 6.0F, 6);
        }
    }

    @Override
    public @Nonnull ItemStack finishUsingItem(@Nonnull ItemStack stack, @Nonnull Level world, @Nonnull LivingEntity entity) {
        return stack;
    }

    public boolean spawnMissile(Level world, Player thrower, double x, double y, double z) {
        if (thrower != null) {
            ItemStack stack = thrower.getMainHandItem();
            if (stack.getItem() instanceof MissileTome) {
                var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
                if (relicCap.isPresent()) {
                    IRelic relic = relicCap.orElse(null);
                    if (relic != null && !relic.isRightPlayer(thrower)) {
                        return false;
                    }
                }
            }
        }

        if (thrower != null && !world.isClientSide) {
            EntityRageousMissile missile = new EntityRageousMissile(ModEntities.RAGEOUS_MISSILE.get(), world);
            missile.setOwner(thrower);
            missile.setEvil(false);
            missile.setThrowerName(thrower.getDisplayName().getString());
            missile.setPos(x, y, z);

            world.playSound(null, x, y, z, SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS,
                    0.6F, 0.8F + (float)Math.random() * 0.2F);

            world.addFreshEntity(missile);

            for (int i = 0; i < 10; i++) {
                double offsetX = (Math.random() - 0.5) * 0.5;
                double offsetY = (Math.random() - 0.5) * 0.5;
                double offsetZ = (Math.random() - 0.5) * 0.5;

                BotaniaAPI.instance().sparkleFX(world,
                        x + offsetX, y + offsetY, z + offsetZ,
                        1.0F, 0.8F, 0.2F, 3.0F, 3);
            }

            return true;
        }
        return false;
    }
}