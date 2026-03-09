package net.xiaoyang010.ex_enigmaticlegacy.Block.ore;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.OreBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;

import java.util.Collections;
import java.util.List;

public class AmethystOre extends OreBlock {
    public AmethystOre() {
        super(Properties.of(Material.STONE).sound(SoundType.STONE).strength(3f, 3f).requiresCorrectToolForDrops());
    }

    @Override
    public boolean canDropFromExplosion(BlockState state, BlockGetter world, BlockPos pos, net.minecraft.world.level.Explosion explosion) {
        return false;
    }

    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
        return net.minecraftforge.common.TierSortingRegistry.isCorrectTierForDrops(Tiers.IRON, state) &&
                player.getMainHandItem().getItem() instanceof TieredItem tieredItem &&
                tieredItem.getTier().getLevel() >= Tiers.IRON.getLevel();
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, builder.getParameter(LootContextParams.TOOL)) > 0) {
            return Collections.singletonList(new ItemStack(this));
        } else {
            return List.of(new ItemStack(ModItems.AMETHYST_INGOT.get(), 2 + RANDOM.nextInt(2)));
        }
    }
}
