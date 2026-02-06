package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.xiaoyang010.ex_enigmaticlegacy.Config.ConfigHandler;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;

public class SlotEnderChest extends Slot {
    private static final String BACKGROUND_PATH = "textures/gui/overflow/empty_enderchest.png";
    public static final ResourceLocation background = new ResourceLocation(ExEnigmaticlegacyMod.MODID, BACKGROUND_PATH);
    public static final int posX = ConfigHandler.INVO_WIDTH.get() - 6 - Const.SQ;
    public static final int posY = 8;

    private final int slotIndex;

    public SlotEnderChest(Container container, int index) {
        super(container, index, posX, posY);
        this.slotIndex = index;
    }

    @Override
    public int getSlotIndex() {
        return this.slotIndex;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return !stack.isEmpty() && stack.is(Blocks.ENDER_CHEST.asItem());
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return 1;
    }
}