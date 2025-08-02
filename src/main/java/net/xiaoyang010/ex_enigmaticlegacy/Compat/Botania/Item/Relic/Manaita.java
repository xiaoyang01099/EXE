package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.xiaoyang010.ex_enigmaticlegacy.api.IFE.INoEMCItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.item.IRelic;
import vazkii.botania.common.item.relic.RelicImpl;
import vazkii.botania.xplat.IXplatAbstractions;

import java.util.List;

public class Manaita extends Item implements INoEMCItem {

    public Manaita(Properties properties) {
        super(properties);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @org.jetbrains.annotations.Nullable CompoundTag nbt) {
        return new Manaita.RelicCapProvider(stack);
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
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        RelicImpl.addDefaultTooltip(stack, tooltip);
        super.appendHoverText(stack, level, tooltip, flag);
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
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public boolean hasCraftingRemainingItem() {
        return true;
    }

    @Override
    public boolean canFitInsideContainerItems() {
        return false;
    }

    @Override
    public boolean canBeDepleted() {
        return false;
    }

    @Override
    public ItemStack getContainerItem(ItemStack itemstack) {
        return itemstack.copy();
    }

//    public static boolean canUseInCrafting(ItemStack stack, Player player) {
//        if (stack.getItem() instanceof Manaita) {
//            var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
//            if (relicCap.isPresent()) {
//                IRelic relic = relicCap.orElse(null);
//                return relic == null || relic.isRightPlayer(player);
//            }
//        }
//        return true;
//    }
}