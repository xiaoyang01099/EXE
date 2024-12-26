package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import vazkii.botania.common.item.block.ItemBlockSpecialFlower;

public class ItemBlockFlower extends ItemBlockSpecialFlower {
    public ItemBlockFlower(Block block1, Properties props) {
        super(block1, props);
    }

    public int getBarWidth(ItemStack stack) {
        CompoundTag tag = stack.getTagElement("BlockEntityTag");
        if (tag != null) {
            float frac = 1.0F - (float)tag.getInt("passiveDecayTicks") / 48000.0F;
            return Math.round(13.0F * frac);
        } else {
            return 0;
        }
    }

    public int getBarColor(ItemStack stack) {
        CompoundTag tag = stack.getTagElement("BlockEntityTag");
        if (tag != null) {
            float frac = 1.0F - (float)tag.getInt("passiveDecayTicks") / 48000.0F;
            return Mth.hsvToRgb(frac / 3.0F, 1.0F, 1.0F);
        } else {
            return 0;
        }
    }
}
