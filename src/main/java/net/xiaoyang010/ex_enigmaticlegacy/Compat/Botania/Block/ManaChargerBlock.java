package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.items.CapabilityItemHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile.ManaChargerTile;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.block.IWandable;

import javax.annotation.Nullable;

public class ManaChargerBlock extends BaseEntityBlock implements IWandable {

    private static final VoxelShape SHAPE = Block.box(3.0D, 3.0D, 3.0D, 13.0D, 13.0D, 13.0D);

    public ManaChargerBlock(Properties sound) {
        super(BlockBehaviour.Properties.of(Material.STONE)
                .strength(6.0F)
                .requiresCorrectToolForDrops());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof ManaChargerTile tile)) {
            return InteractionResult.PASS;
        }

        Direction side = hit.getDirection();
        int slotSide = side.ordinal() - 1;
        if (slotSide < 0) {
            return InteractionResult.PASS;
        }

        ItemStack heldItem = player.getItemInHand(hand);
        var inventory = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
        if (inventory == null) {
            return InteractionResult.PASS;
        }

        ItemStack stackInSlot = inventory.getStackInSlot(slotSide);

        if (player.isShiftKeyDown()) {
            // 提取物品
            if (!stackInSlot.isEmpty()) {
                ItemStack extracted = inventory.extractItem(slotSide, stackInSlot.getCount(), false);

                if (!level.isClientSide) {
                    Vec3 lookVec = player.getLookAngle();
                    ItemEntity entityItem = new ItemEntity(level,
                            player.getX() + lookVec.x,
                            player.getY() + 1.2F,
                            player.getZ() + lookVec.z, extracted);
                    level.addFreshEntity(entityItem);
                    tile.setChanged();
                }

                level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
                return InteractionResult.SUCCESS;
            }
        } else if (!heldItem.isEmpty() && stackInSlot.isEmpty() && heldItem.getCount() == 1) {
            // 检查是否为魔力物品
            var manaCapability = heldItem.getCapability(BotaniaForgeCapabilities.MANA_ITEM);
            if (manaCapability.isPresent()) {
                ItemStack toInsert = heldItem.copy();
                toInsert.setCount(1);

                if (inventory.insertItem(slotSide, toInsert, true).isEmpty()) {
                    inventory.insertItem(slotSide, toInsert, false);
                    heldItem.shrink(1);

                    if (!level.isClientSide) {
                        tile.setChanged();
                    }

                    return InteractionResult.SUCCESS;
                }
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof ManaChargerTile tile) {
                var inventory = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
                if (inventory != null) {
                    for (int i = 0; i < inventory.getSlots(); i++) {
                        ItemStack stack = inventory.getStackInSlot(i);
                        if (!stack.isEmpty()) {
                            double x = pos.getX() + level.random.nextFloat() * 0.8F + 0.1F;
                            double y = pos.getY() + level.random.nextFloat() * 0.8F + 0.1F;
                            double z = pos.getZ() + level.random.nextFloat() * 0.8F + 0.1F;

                            ItemEntity entityItem = new ItemEntity(level, x, y, z, stack.copy());
                            entityItem.setDeltaMovement(
                                    level.random.nextGaussian() * 0.05,
                                    level.random.nextGaussian() * 0.05 + 0.2,
                                    level.random.nextGaussian() * 0.05
                            );
                            level.addFreshEntity(entityItem);
                        }
                    }
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ManaChargerTile(ModBlockEntities.MANA_CHARGER_TILE.get(), pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null :
                (lvl, pos, st, be) -> {
                    if (be instanceof ManaChargerTile tile) {
                        tile.tick();
                    }
                };
    }

    @Override
    public boolean onUsedByWand(@Nullable Player player, ItemStack stack, Direction side) {
        if (player == null || player.level == null) {
            return false;
        }

        Level level = player.level;

        BlockHitResult hitResult = getPlayerBlockHitResult(player);
        if (hitResult == null || hitResult.getType() != BlockHitResult.Type.BLOCK) {
            return false;
        }

        BlockPos pos = hitResult.getBlockPos();
        BlockState state = level.getBlockState(pos);

        if (!(state.getBlock() instanceof ManaChargerBlock)) {
            return false;
        }

        if (level.getBlockEntity(pos) instanceof ManaChargerTile tile) {
            tile.onWanded(player, stack);
            return true;
        }

        return false;
    }

    private BlockHitResult getPlayerBlockHitResult(Player player) {
        double reach = player.isCreative() ? 5.0D : 4.5D;
        Vec3 eyePos = player.getEyePosition(1.0F);
        Vec3 lookVec = player.getViewVector(1.0F);
        Vec3 endPos = eyePos.add(lookVec.scale(reach));

        return player.level.clip(new ClipContext(
                eyePos,
                endPos,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                player
        ));
    }
}