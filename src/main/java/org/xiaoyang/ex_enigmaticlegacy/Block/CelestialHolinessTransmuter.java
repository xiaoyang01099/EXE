package org.xiaoyang.ex_enigmaticlegacy.Block;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModBlockEntities;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModBlocks;
import org.xiaoyang.ex_enigmaticlegacy.Tile.CelestialHTTile;

public class CelestialHolinessTransmuter extends BaseEntityBlock {
	public CelestialHolinessTransmuter() {
		super(Properties.of().mapColor(MapColor.STONE).sound(SoundType.STONE).strength(3f, 10f).requiresCorrectToolForDrops());
	}

	@Override
	public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
		return 10;
	}


	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos,
								 Player player, InteractionHand hand, BlockHitResult hit) {
		if (!world.isClientSide) {
			MenuProvider containerProvider = this.getMenuProvider(state, world, pos);
			if (containerProvider != null) {
				NetworkHooks.openScreen((ServerPlayer) player, containerProvider, pos);
			} else {
				player.displayClientMessage(Component.translatable("无法打开GUI，MenuProvider为null"), false);
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
		return new CelestialHTTile(pos, state);
	}

	@Override
	public RenderShape getRenderShape(BlockState pState) {
		return RenderShape.MODEL;
	}

	@SuppressWarnings("removal")
	@OnlyIn(Dist.CLIENT)
	public static void registerRenderLayer() {
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.CELESTIAL_HOLINESS_TRANSMUTER.get(), renderType -> renderType == RenderType.cutout());
	}

	@Override
	public void onRemove(BlockState state, Level world, BlockPos pos,
						 BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			BlockEntity tileEntity = world.getBlockEntity(pos);
			if (tileEntity instanceof CelestialHTTile) {
				Containers.dropContents(world, pos, (CelestialHTTile) tileEntity);
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
		if (tileentity instanceof CelestialHTTile) {
			return AbstractContainerMenu.getRedstoneSignalFromContainer((CelestialHTTile) tileentity);
		} else {
			return 0;
		}
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
		return createTicker(pLevel, pBlockEntityType, ModBlockEntities.CELESTIAL_HOLINESS_TRANSMUTER_TILE.get());
	}

	@javax.annotation.Nullable
	protected static <T extends BlockEntity> BlockEntityTicker<T> createTicker(Level pLevel, BlockEntityType<T> pServerType, BlockEntityType<? extends CelestialHTTile> pClientType) {
		return pLevel.isClientSide ? null : createTickerHelper(pServerType, pClientType, CelestialHTTile::serverTick);
	}
}
