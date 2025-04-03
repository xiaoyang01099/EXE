package net.xiaoyang010.ex_enigmaticlegacy.Recipe;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

/**
 * 多色崩解棱镜合成配方接口
 * 类似于泰拉板的配方，但提供更多功能
 */
public interface IPolychromeRecipe extends Recipe<Container> {
    /**
     * 获取完成合成所需的魔力
     */
    int getMana();

    /**
     * 获取完成这个合成所需的时间（以tick为单位）
     */
    default int getProcessTime() {
        return 200;
    }

    /**
     * 获取合成的产物
     */
    @Override
    ItemStack assemble(Container inventory);

    /**
     * 当合成进行中每tick触发的效果
     * @param completion 完成度，范围0-1
     */
    default void onCraftingTick(float completion) {
        // 默认无操作
    }

    /**
     * 当合成完成时触发的效果
     */
    default void onCraftingComplete() {
        // 默认无操作
    }
}