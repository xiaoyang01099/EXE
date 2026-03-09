package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item;

import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.mana.IManaItem;
import vazkii.botania.api.mana.ManaBarTooltip;
import vazkii.botania.common.helper.ItemNBTHelper;
import vazkii.botania.common.item.equipment.bauble.ItemBauble;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class MithrillRing extends ItemBauble implements ICurioItem  {
    private static final String TAG_MANA = "mana";
    private static final int MAX_MANA = 8000000;
    private static final int MAX_DAMAGE = 1000;

    public MithrillRing(Properties properties) {
        super(properties
                .durability(MAX_DAMAGE)
                .setNoRepair()
                .fireResistant()
        );
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        onWornTick(stack, slotContext.entity());
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        onEquipped(stack, slotContext.entity());
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        onUnequipped(stack, slotContext.entity());
    }

    public void onEquipped(ItemStack stack, LivingEntity entity) {
    }

    @Override
    public boolean canEquip(SlotContext context, ItemStack stack) {
        return CuriosApi.getCuriosHelper().findEquippedCurio(this, context.entity()).isEmpty()
                && context.identifier().equals("ring");
    }

    public void onUnequipped(ItemStack stack, LivingEntity entity) {
    }

    @Override
    public void onWornTick(ItemStack stack, LivingEntity entity) {
        if (entity.level.getGameTime() % 20 == 0) {
            stack.getCapability(BotaniaForgeCapabilities.MANA_ITEM).ifPresent(manaItem -> {
                if (manaItem.getMana() < manaItem.getMaxMana()) {
                    manaItem.addMana(5000); // 每10秒恢复5000魔力
                }
            });
        }
    }

    @Override
    public void fillItemCategory(@Nonnull CreativeModeTab tab, @Nonnull NonNullList<ItemStack> stacks) {
        if (allowdedIn(tab)) {
            stacks.add(new ItemStack(this));

            ItemStack fullMana = new ItemStack(this);
            setMana(fullMana, MAX_MANA);
            stacks.add(fullMana);
        }
    }


    @Override
    public int getMaxDamage(ItemStack stack) {
        return MAX_DAMAGE;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * ManaBarTooltip.getFractionForDisplay(getManaCapability(stack)));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        stack.getCapability(BotaniaForgeCapabilities.MANA_ITEM);
        float fraction = ManaBarTooltip.getFractionForDisplay(getManaCapability(stack));
        float hue = 0.35F + (fraction * 0.1F);
        return Mth.hsvToRgb(hue, 1.0F, 1.0F);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getEntityLifespan(ItemStack itemStack, Level level) {
        return Integer.MAX_VALUE;
    }

    public static void setMana(ItemStack stack, int mana) {
        if (mana > 0) {
            ItemNBTHelper.setInt(stack, TAG_MANA, mana);
        } else {
            ItemNBTHelper.removeEntry(stack, TAG_MANA);
        }
    }

    private IManaItem getManaCapability(ItemStack stack) {
        return stack.getCapability(BotaniaForgeCapabilities.MANA_ITEM)
                .orElse(new ManaItem(stack));
    }

    public static class ManaItem implements IManaItem {
        private final ItemStack stack;

        public ManaItem(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public int getMana() {
            return ItemNBTHelper.getInt(stack, TAG_MANA, 0);
        }

        @Override
        public int getMaxMana() {
            return MAX_MANA;
        }

        @Override
        public void addMana(int mana) {
            int newMana = Math.min(getMana() + mana, getMaxMana());
            setMana(stack, newMana);
        }

        @Override
        public boolean canReceiveManaFromPool(BlockEntity pool) {
            return getMana() < getMaxMana();
        }

        @Override
        public boolean canReceiveManaFromItem(ItemStack otherStack) {
            return getMana() < getMaxMana();
        }

        @Override
        public boolean canExportManaToPool(BlockEntity pool) {
            return getMana() > 0;
        }

        @Override
        public boolean canExportManaToItem(ItemStack otherStack) {
            return getMana() > 0;
        }

        @Override
        public boolean isNoExport() {
            return false;
        }
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        LazyOptional<IManaItem> manaItem = stack.getCapability(BotaniaForgeCapabilities.MANA_ITEM);
        if (manaItem.isPresent()) {
            return Optional.of(ManaBarTooltip.fromManaItem(stack));
        }
        return Optional.empty();
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {
            private final LazyOptional<IManaItem> manaHandler = LazyOptional.of(() -> new ManaItem(stack));

            @Override
            public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
                if (cap == BotaniaForgeCapabilities.MANA_ITEM) {
                    return manaHandler.cast();
                }
                return LazyOptional.empty();
            }
        };
    }
}