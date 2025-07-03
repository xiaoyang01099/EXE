package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile.ManaCrystalCubeBlockTile;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.block.IWandable;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.handler.ModSounds;
import vazkii.botania.common.item.ItemTwigWand;

public class ManaCrystalCubeBlock extends BaseEntityBlock implements IWandable {
    private static final VoxelShape SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 16.0D, 13.0D);

    public ManaCrystalCubeBlock(Properties properties) {
        super(Properties.of(Material.METAL)
                .strength(5.5F)
                .sound(ModBlocks.dreamwood.getSoundType(null))
                .noOcclusion()
                .requiresCorrectToolForDrops());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack heldItem = player.getItemInHand(hand);
        boolean isWand = heldItem.getItem() instanceof ItemTwigWand;

        if (isWand) {
            return InteractionResult.PASS;
        } else {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof ManaCrystalCubeBlockTile cube && !world.isClientSide()) {
                CompoundTag tag = new CompoundTag();
                cube.writeCustomNBT(tag);
                int[] mana = cube.getManaAround();
                tag.putInt("knownMana", mana[0]);
                tag.putInt("knownMaxMana", mana[1]);

                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.connection.send(ClientboundBlockEntityDataPacket.create(cube, be -> tag));
                }
            }

            player.playSound(ModSounds.ding, 0.11F, 1.0F);
            return InteractionResult.sidedSuccess(world.isClientSide());
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ManaCrystalCubeBlockTile(ModBlockEntities.MANA_CRYSTAL_TILE.get(), pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.MANA_CRYSTAL_TILE.get(), ManaCrystalCubeBlockTile::tick);
    }

    @Nullable
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> typeCheck, BlockEntityType<E> typeExpected, BlockEntityTicker<? super E> ticker) {
        return typeExpected == typeCheck ? (BlockEntityTicker<A>) ticker : null;
    }

    @Override
    public boolean onUsedByWand(@Nullable Player player, ItemStack stack, Direction side) {
        return false;
    }
}