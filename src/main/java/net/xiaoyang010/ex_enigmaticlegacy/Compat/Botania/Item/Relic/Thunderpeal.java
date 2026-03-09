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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model.Vector3;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.others.EntityThunderpealOrb;
import net.xiaoyang010.ex_enigmaticlegacy.Event.RelicsEventHandler;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.item.IRelic;
import vazkii.botania.common.item.relic.RelicImpl;
import vazkii.botania.xplat.IXplatAbstractions;

import javax.annotation.Nullable;
import java.util.List;

public class Thunderpeal extends Item {
    private static final int COOLDOWN_TICKS = 30;

    public Thunderpeal(Properties tab) {
        super(tab);
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

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        RelicImpl.addDefaultTooltip(stack, tooltip);

        if (Screen.hasShiftDown()) {
            tooltip.add(new TranslatableComponent("item.ItemThunderpeal1.lore"));
            tooltip.add(new TranslatableComponent("item.FREmpty.lore"));
            tooltip.add(new TranslatableComponent("item.ItemThunderpeal2.lore"));
            tooltip.add(new TranslatableComponent("item.ItemThunderpeal3_1.lore")
                    .append(" 24 ")
                    .append(new TranslatableComponent("item.ItemThunderpeal3_2.lore")));
            tooltip.add(new TranslatableComponent("item.ItemThunderpeal4.lore"));
            tooltip.add(new TranslatableComponent("item.FREmpty.lore"));
            tooltip.add(new TranslatableComponent("item.ItemThunderpeal5_1.lore")
                    .append(" 16 ")
                    .append(new TranslatableComponent("item.ItemThunderpeal5_2.lore")));
            tooltip.add(new TranslatableComponent("item.ItemThunderpeal6.lore"));
            tooltip.add(new TranslatableComponent("item.ItemThunderpeal7.lore"));
        } else {
            tooltip.add(new TranslatableComponent("item.FRShiftTooltip.lore"));
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

    public boolean spawnOrb(Level level, Player player) {
        if (!level.isClientSide) {
            Vector3 originalPos = Vector3.fromEntityCenter(player);
            Vector3 vector = originalPos.add(new Vector3(player.getLookAngle()).multiply(1.25));
            vector.y += 0.5;
            Vector3 motion = new Vector3(player.getLookAngle()).multiply(1.5);

            EntityThunderpealOrb orb = new EntityThunderpealOrb(level, player);
            orb.setPos(vector.x, vector.y, vector.z);
            orb.area += 2;
            orb.setDeltaMovement(motion.x, motion.y, motion.z);

            level.playSound(null, orb.getX(), orb.getY(), orb.getZ(),
                    SoundEvents.LIGHTNING_BOLT_IMPACT,
                    SoundSource.PLAYERS,
                    1.0F, 1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F);

            level.addFreshEntity(orb);
            return true;
        }
        return false;
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

        if (!RelicsEventHandler.isOnCoodown(player) && !level.isClientSide) {
            this.spawnOrb(level, player);
            RelicsEventHandler.setCasted(player, COOLDOWN_TICKS, true);
            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }
}