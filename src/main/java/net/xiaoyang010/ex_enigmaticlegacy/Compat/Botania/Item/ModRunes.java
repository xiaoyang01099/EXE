package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import vazkii.botania.api.recipe.ICustomApothecaryColor;

import java.time.LocalTime;

public class ModRunes extends Item implements ICustomApothecaryColor {
    public ModRunes() {
        super(new Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA)
                .stacksTo(64)
                .rarity(Rarity.EPIC));
    }

    @Override
    public int getParticleColor(ItemStack stack) {
        LocalTime currentTime = LocalTime.now();
        int second = currentTime.getSecond();

        switch (second % 7) {
            case 0:
                return 0xFF0000; // 红色
            case 1:
                return 0xFFA500; // 橙色
            case 2:
                return 0xFFFF00; // 黄色
            case 3:
                return 0x008000; // 绿色
            case 4:
                return 0x0000FF; // 蓝色
            case 5:
                return 0x4B0082; // 靛色
            case 6:
                return 0xEE82EE; // 紫色
            default:
                return 0xA8A8A8;
        }
    }
}
