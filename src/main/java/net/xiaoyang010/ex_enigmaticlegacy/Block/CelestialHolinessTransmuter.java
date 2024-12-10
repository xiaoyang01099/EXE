package net.xiaoyang010.ex_enigmaticlegacy.Block;

import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Containers;
import net.minecraft.core.BlockPos;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;
import java.util.Collections;

import net.xiaoyang010.ex_enigmaticlegacy.Tile.CelestialHolinessTransmuterBlockEntity;

public class CelestialHolinessTransmuter extends Block implements EntityBlock {
	public CelestialHolinessTransmuter() {
		super(BlockBehaviour.Properties.of(Material.STONE).sound(SoundType.STONE).strength(5f, 10f).requiresCorrectToolForDrops());
	}

	@Override
	public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
		return 10;
	}

	@Override
	public boolean canHarvestBlock(BlockState state, BlockGetter world, BlockPos pos, Player player) {
		if (player.getInventory().getSelected().getItem() instanceof PickaxeItem)
			return true;
		return false;
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
		List<ItemStack> dropsOriginal = super.getDrops(state, builder);
		if (!dropsOriginal.isEmpty())
			return dropsOriginal;
		return Collections.singletonList(new ItemStack(this, 1));
	}

	// 添加右键点击事件
	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos,
								 Player player, InteractionHand hand, BlockHitResult hit) {
		if (!world.isClientSide) {
			MenuProvider containerProvider = this.getMenuProvider(state, world, pos);
			if (containerProvider != null) {
				NetworkHooks.openGui((ServerPlayer) player, containerProvider, pos);
			} else {
				player.displayClientMessage(new TextComponent("无法打开GUI，MenuProvider为null"), false);
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public MenuProvider getMenuProvider(BlockState state, Level worldIn, BlockPos pos) {
		BlockEntity tileEntity = worldIn.getBlockEntity(pos);
		if (tileEntity instanceof MenuProvider) {
			return (MenuProvider) tileEntity;
		}
		return null;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new CelestialHolinessTransmuterBlockEntity(pos, state);
	}

	@Override
	public void onRemove(BlockState state, Level world, BlockPos pos,
						 BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			BlockEntity tileEntity = world.getBlockEntity(pos);
			if (tileEntity instanceof CelestialHolinessTransmuterBlockEntity) {
				Containers.dropContents(world, pos, (CelestialHolinessTransmuterBlockEntity) tileEntity);
				world.updateNeighbourForOutputSignal(pos, this);
			}
			super.onRemove(state, world, pos, newState, isMoving);
		}
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState blockState, Level world, BlockPos pos) {
		BlockEntity tileentity = world.getBlockEntity(pos);
		if (tileentity instanceof CelestialHolinessTransmuterBlockEntity) {
			return AbstractContainerMenu.getRedstoneSignalFromContainer((CelestialHolinessTransmuterBlockEntity) tileentity);
		} else {
			return 0;
		}
	}
}
