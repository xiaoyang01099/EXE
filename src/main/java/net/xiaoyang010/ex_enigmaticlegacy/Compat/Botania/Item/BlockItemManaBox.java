package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item;

import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.mana.IManaItem;
import vazkii.botania.common.helper.ItemNBTHelper;
import vazkii.botania.common.item.ModItems;

import javax.annotation.Nonnull;

public class BlockItemManaBox extends BlockItem implements IManaItem {
    public static final int MAX_MANA = 1000000;
    private static final String TAG_MANA = "mana";
    private static final String TAG_COLOR = "color";
    private static final String TAG_ONE_USE = "oneUse";

    public BlockItemManaBox() {
        super(ModBlockss.MANA_BOX.get(),
                ModItems.defaultBuilder()
                        .stacksTo(1)
                        .rarity(Rarity.EPIC)
                        .tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA));
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {
            private final LazyOptional<IManaItem> manaItem = LazyOptional.of(() -> new IManaItem() {
                @Override
                public int getMana() {
                    return BlockItemManaBox.getMana(stack);
                }

                @Override
                public int getMaxMana() {
                    return BlockItemManaBox.getMaxMana(stack);
                }

                @Override
                public void addMana(int mana) {
                    BlockItemManaBox.addMana(stack, mana);
                }

                @Override
                public boolean canReceiveManaFromPool(BlockEntity pool) {
                    return BlockItemManaBox.canReceiveManaFromPool(stack, pool);
                }

                @Override
                public boolean canReceiveManaFromItem(ItemStack otherStack) {
                    return true;
                }

                @Override
                public boolean canExportManaToPool(BlockEntity pool) {
                    return true;
                }

                @Override
                public boolean canExportManaToItem(ItemStack otherStack) {
                    return true;
                }

                @Override
                public boolean isNoExport() {
                    return false;
                }
            });

            @Override
            public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                if (cap == BotaniaForgeCapabilities.MANA_ITEM) {
                    return manaItem.cast();
                }
                return LazyOptional.empty();
            }
        };
    }

    @Override
    public void fillItemCategory(@Nonnull CreativeModeTab tab, @Nonnull NonNullList<ItemStack> stacks) {
        if (allowdedIn(tab)) {
            stacks.add(new ItemStack(this));

            ItemStack fullPower = new ItemStack(this);
            setMana(fullPower, MAX_MANA);
            stacks.add(fullPower);
        }
    }

    @Override
    public int getEntityLifespan(ItemStack itemStack, Level world) {
        return Integer.MAX_VALUE;
    }


    @Override
    public int getMana() {
        return 0;
    }

    @Override
    public int getMaxMana() {
        return MAX_MANA;
    }

    @Override
    public void addMana(int mana) {
    }

    public static int getMana(ItemStack stack) {
        return ItemNBTHelper.getInt(stack, TAG_MANA, 0) * stack.getCount();
    }

    public static int getMaxMana(ItemStack stack) {
        return MAX_MANA * stack.getCount();
    }

    public static void addMana(ItemStack stack, int mana) {
        setMana(stack, Math.min(getMana(stack) + mana, getMaxMana(stack)) / stack.getCount());
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
        return true;
    }

    @Override
    public boolean canExportManaToItem(ItemStack otherStack) {
        return true;
    }

    @Override
    public boolean isNoExport() {
        return false;
    }

    public static boolean canReceiveManaFromPool(ItemStack stack, BlockEntity pool) {
        return !ItemNBTHelper.getBoolean(stack, TAG_ONE_USE, false);
    }

    public static float getManaFractionForDisplay(ItemStack stack) {
        return (float) getMana(stack) / (float) getMaxMana(stack);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * getManaFractionForDisplay(stack));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return Mth.hsvToRgb(getManaFractionForDisplay(stack) / 3.0F, 1.0F, 1.0F);
    }

    public static int getBlockMana(ItemStack stack) {
        return ItemNBTHelper.getInt(stack, TAG_MANA, 0);
    }

    public static void setMana(ItemStack stack, int mana) {
        ItemNBTHelper.setInt(stack, TAG_MANA, mana);
    }

    public static DyeColor getColor(ItemStack stack) {
        return DyeColor.byId(ItemNBTHelper.getInt(stack, TAG_COLOR, 0));
    }

    public static void setColor(ItemStack stack, DyeColor color) {
        ItemNBTHelper.setInt(stack, TAG_COLOR, color.getId());
    }
}