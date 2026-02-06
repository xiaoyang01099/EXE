package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;

public class SlotEnderPearl extends Slot {
    private static final String BACKGROUND_PATH = "textures/gui/overflow/empty_enderpearl.png";
    public static final ResourceLocation background = new ResourceLocation(ExEnigmaticlegacyMod.MODID, BACKGROUND_PATH);
    public static final int posX = 8;
    public static final int posY = 8;
    private final int slotIndex;

    public SlotEnderPearl(Container container, int index) {
        super(container, index, posX, posY);
        this.slotIndex = index;
    }

    @Override
    public int getSlotIndex() {
        return this.slotIndex;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return !stack.isEmpty() && stack.is(Items.ENDER_PEARL);
    }

    @Override
    public int getMaxStackSize() {
        return Items.ENDER_PEARL.getMaxStackSize();
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return Items.ENDER_PEARL.getMaxStackSize();
    }
}