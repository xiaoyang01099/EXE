package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
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
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import vazkii.botania.api.block.IWandHUD;
import vazkii.botania.api.block.IWandable;


import javax.annotation.Nullable;

public class ManaChargerBlock extends BaseEntityBlock implements IWandHUD, IWandable {
    private static final VoxelShape SHAPE = Block.box(
            3.0D, 3.0D, 3.0D,
            13.0D, 12.0D, 13.0D
    );

    public ManaChargerBlock(Properties properties) {
        super(Properties.of(Material.METAL)
                .strength(6.0f)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .requiresCorrectToolForDrops());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof TileManaCharger tile) {
            Direction side = hit.getDirection();
            int slotSide = side.get3DDataValue();
            if (slotSide < 0) {
                return InteractionResult.PASS;
            }

            ItemStack heldItem = player.getItemInHand(hand);
            ItemStack stackInSlot = tile.getItem(slotSide);

            if (player.isShiftKeyDown()) {
                if (!stackInSlot.isEmpty()) {
                    if (!level.isClientSide) {
                        ItemStack copy = stackInSlot.copy();
                        Vec3 lookVec = player.getLookAngle();
                        double x = player.getX() + lookVec.x;
                        double y = player.getY() + 1.2D;
                        double z = player.getZ() + lookVec.z;

                        ItemEntity itemEntity = new ItemEntity(level, x, y, z, copy);
                        level.addFreshEntity(itemEntity);

                        removeItem(tile, slotSide);
                        tile.markForUpdate();
                    }
                    level.updateNeighbourForOutputSignal(pos, this);
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            } else if (!heldItem.isEmpty() && tile.isItemValidForSlot(slotSide, heldItem) &&
                    stackInSlot.isEmpty() && heldItem.getMaxStackSize() == 1) {
                if (!level.isClientSide) {
                    ItemStack copy = heldItem.copy();
                    copy.setCount(1);
                    setItem(tile, slotSide, copy);

                    if (!player.getAbilities().instabuild) {
                        heldItem.shrink(1);
                    }

                    tile.markForUpdate();
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof TileManaCharger tile) {
                for (int i = 0; i < tile.getItemHandler().getSlots(); i++) {
                    ItemStack stack = tile.getItemHandler().getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        dropItemStack(level, pos, stack);
                    }
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    private void dropItemStack(Level level, BlockPos pos, ItemStack stack) {
        float xOffset = level.random.nextFloat() * 0.8F + 0.1F;
        float yOffset = level.random.nextFloat() * 0.8F + 0.1F;
        float zOffset = level.random.nextFloat() * 0.8F + 0.1F;

        ItemEntity itemEntity = new ItemEntity(level,
                pos.getX() + xOffset,
                pos.getY() + yOffset,
                pos.getZ() + zOffset,
                stack.copy());

        float motion = 0.05F;
        itemEntity.setDeltaMovement(
                level.random.nextGaussian() * motion,
                level.random.nextGaussian() * motion + 0.2F,
                level.random.nextGaussian() * motion
        );

        if (stack.hasTag()) {
            itemEntity.getItem().setTag(stack.getTag().copy());
        }

        level.addFreshEntity(itemEntity);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileManaCharger(ModBlockEntities.MANA_CHARGER_TILE.get(), pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type == ModBlockEntities.MANA_CHARGER_TILE.get()) {
            return (level1, pos, state1, blockEntity) -> {
                if (blockEntity instanceof TileManaCharger manaCharger) {
                    manaCharger.tick();
                }
            };
        }
        return null;
    }

//    private BlockHitResult getPlayerPOVHitResult(Level level, Player player, ClipContext.Fluid fluidMode) {
//        float f = player.getXRot();
//        float f1 = player.getYRot();
//        Vec3 vec3 = player.getEyePosition();
//        float f2 = Mth.cos(-f1 * ((float)Math.PI / 180F) - (float)Math.PI);
//        float f3 = Mth.sin(-f1 * ((float)Math.PI / 180F) - (float)Math.PI);
//        float f4 = -Mth.cos(-f * ((float)Math.PI / 180F));
//        float f5 = Mth.sin(-f * ((float)Math.PI / 180F));
//        float f6 = f3 * f4;
//        float f7 = f2 * f4;
//        double reach = player.getBlockReach().getMaxValue();
//        Vec3 vec31 = vec3.add((double)f6 * reach, (double)f5 * reach, (double)f7 * reach);
//        return level.clip(new ClipContext(vec3, vec31, ClipContext.Block.OUTLINE, fluidMode, player));
//    }


    @Override
    public boolean onUsedByWand(@Nullable Player player, ItemStack stack, Direction side) {
        if (player == null) return false;
        Level level = player.getLevel();

        BlockHitResult hitResult = (BlockHitResult) player.pick(20.0D, 0.0F, false);
        BlockPos pos = hitResult.getBlockPos();

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileManaCharger tile) {
            tile.onWanded(player, stack);
            return true;
        }
        return false;
    }

    @Override
    public void renderHUD(PoseStack ms, Minecraft mc) {
        if (mc.level == null || mc.player == null) return;

        BlockHitResult hitResult = (BlockHitResult) mc.hitResult;
        if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = hitResult.getBlockPos();
            if (mc.level.getBlockEntity(pos) instanceof TileManaCharger tile) {
                tile.renderHUD(ms, mc);
            }
        }
    }

    private ItemStack removeItem(TileManaCharger tile, int slot) {
        return tile.getItemHandler().extractItem(slot, 1, false);
    }

    private void setItem(TileManaCharger tile, int slot, ItemStack stack) {
        tile.getItemHandler().insertItem(slot, stack, false);
    }
}