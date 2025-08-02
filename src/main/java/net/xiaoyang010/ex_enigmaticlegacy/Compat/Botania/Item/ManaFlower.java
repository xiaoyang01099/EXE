package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import vazkii.botania.api.mana.IManaItem;
import vazkii.botania.api.mana.ManaItemHandler;

public class ManaFlower extends Item implements ICurioItem {
    private static final int MAX_MANA = 10000;
    private static final int MANA_GENERATION = 54;
    private static final String TAG_MANA = "mana";

    public ManaFlower() {
        super(new Properties()
                .tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA)
                .stacksTo(1)
                .setNoRepair()
        );
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);

        if (entity instanceof Player player && !level.isClientSide && entity.tickCount % 5 == 0) {
            if (getMana(stack) < getMaxMana(stack)) {
                addMana(stack, MANA_GENERATION);
            }

            if (getMana(stack) > 0) {
                ManaItemHandler.instance().dispatchMana(stack, player, getMana(stack), true);
            }
        }
    }

    public int getMana(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null ? tag.getInt(TAG_MANA) : 0;
    }


    public int getMaxMana(ItemStack stack) {
        return MAX_MANA;
    }

    public void addMana(ItemStack stack, int mana) {
        CompoundTag tag = stack.getOrCreateTag();
        int currentMana = tag.getInt(TAG_MANA);
        int newMana = Math.min(currentMana + mana, getMaxMana(stack));
        tag.putInt(TAG_MANA, newMana);
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public boolean canEquip(SlotContext context, ItemStack stack) {
        return CuriosApi.getCuriosHelper().findEquippedCurio(this, context.entity()).isEmpty()
                && context.identifier().equals("belt");
    }

    public void curioTick(String identifier, int index, LivingEntity livingBase, ItemStack stack) {
        if (livingBase instanceof Player player && !player.level.isClientSide && livingBase.tickCount % 5 == 0) {
            if (getMana(stack) < getMaxMana(stack)) {
                addMana(stack, MANA_GENERATION);
            }

            if (getMana(stack) > 0) {
                ManaItemHandler.instance().dispatchMana(stack, player, getMana(stack), true);
            }
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getMana(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * getMana(stack) / getMaxMana(stack));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x0000FF;
    }
}