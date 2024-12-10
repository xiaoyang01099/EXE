package net.xiaoyang010.ex_enigmaticlegacy.Block.ore;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;

import java.util.Collections;
import java.util.List;

public class PlatinumOre extends Block {
    public PlatinumOre() {
        super(BlockBehaviour.Properties.of(Material.STONE).sound(SoundType.STONE).strength(2f, 10f).requiresCorrectToolForDrops());
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        // 检查是否使用了有加成的工具，例如精准采集
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, builder.getParameter(LootContextParams.TOOL)) > 0) {
            return Collections.singletonList(new ItemStack(this));
        } else {
            // 自定义掉落物，例如掉落矿石碎片
            return List.of(new ItemStack(ModItems.PLATINUM_INGOT.get(), 1 + RANDOM.nextInt(3)));
        }
    }
}
