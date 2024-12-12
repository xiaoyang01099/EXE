
package net.xiaoyang010.ex_enigmaticlegacy.Block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.BlockHitResult;
import net.xiaoyang010.ex_enigmaticlegacy.Client.gui.CobblesStoneMenu;

import java.util.Collections;
import java.util.List;

public class Cobblestone extends CraftingTableBlock {
    private static final Component CONTAINER_TITLE = new TranslatableComponent("container.crafting");
    public Cobblestone() {
        super(BlockBehaviour.Properties.of(Material.STONE).sound(SoundType.STONE).strength(1.5f, 10f).requiresCorrectToolForDrops());
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pLevel.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            pPlayer.openMenu(pState.getMenuProvider(pLevel, pPos));
            pPlayer.awardStat(Stats.INTERACT_WITH_CRAFTING_TABLE);
            return InteractionResult.CONSUME;
        }
    }

    @Override
    public MenuProvider getMenuProvider(BlockState pState, Level pLevel, BlockPos pPos) {
        return new SimpleMenuProvider((p_52229_, p_52230_, p_52231_) -> {
            return new CobblesStoneMenu(p_52229_, p_52230_, ContainerLevelAccess.create(pLevel, pPos));
        }, CONTAINER_TITLE);
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return 15;
    }

    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter world, BlockPos pos, Player player) {

        ItemStack heldItem = player.getMainHandItem();

        if (heldItem.getItem() instanceof TieredItem) {
            TieredItem tool = (TieredItem) heldItem.getItem();
            return tool.getTier().getLevel() >= 0; // 0表示木镐或更高等级的工具
        }

        return false;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        List<ItemStack> dropsOriginal = super.getDrops(state, builder);
        if (!dropsOriginal.isEmpty())
            return dropsOriginal;
        return Collections.singletonList(new ItemStack(this, 1));
    }

}
